/* SPDX-License-Identifier: GPL-3.0-or-later
 * [sorma2], Java part of sorma2
 * Copyright (C) 2024 Zoff <zoff@zoff.cc>
 */

package com.zoffcc.applications.sorm;

import android.os.Build;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.zoffcc.applications.sorm.Log;

public class OrmaDatabase
{
    private static final String TAG = "sorm.OrmaDatabase";
    public static final String OrmaDatabaseVersion = "1.0.2";

    final static boolean ORMA_TRACE = false; // set "false" for release builds
    final static boolean ORMA_LONG_RUNNING_TRACE = false; // set "false" for release builds
    final static long ORMA_LONG_RUNNING_MS = 180;

    public static Connection sqldb = null;
    static int THIS_DB_SCHEMA_VERSION = 1; // HINT: this is the version the database schema should be upgraded to
    static int current_db_schema_version = 0; // HINT: this is current database schema version,
                                              // if its lower than THIS_DB_SCHEMA_VERSION some upgrade steps have to be made
    static Semaphore orma_semaphore_lastrowid_on_insert = new Semaphore(1);
    //
    static ReentrantReadWriteLock orma_global_readwritelock = new ReentrantReadWriteLock(true);
    //
    static final Lock orma_global_readLock = orma_global_readwritelock.readLock();
    public static final Lock orma_global_writeLock = orma_global_readwritelock.writeLock();
    // --- read locks ---
    static final Lock orma_global_sqlcount_lock = orma_global_readLock;
    static final Lock orma_global_sqltolist_lock = orma_global_readLock;
    // static final Lock orma_global_sqlgetlastrowid_lock = orma_global_readLock;
    static final Lock orma_global_sqlexecute_lock = orma_global_readLock;
    static final Lock orma_global_sqlinsert_lock = orma_global_readLock;
    // --- read locks ---
    //
    // --- write locks ---
    static final Lock orma_global_sqlfreehand_lock = orma_global_writeLock;
    // --- write locks ---
    //

    private static String db_file_path = null;
    private static String secrect_key = null;
    private static boolean wal_mode = false; // default mode is WAL off!

    public static String getVersion()
    {
        return OrmaDatabase.OrmaDatabaseVersion;
    }

    public OrmaDatabase(final String db_file_path, final String secrect_key, boolean wal_mode)
    {
        OrmaDatabase.db_file_path = db_file_path;
        OrmaDatabase.secrect_key = secrect_key;
        OrmaDatabase.wal_mode = wal_mode;
    }

    public static interface schema_upgrade_callback {
        void upgrade(int old_version, int new_version);
    }
    static schema_upgrade_callback schema_upgrade_callback_function = null;

    public static void set_schema_upgrade_callback(schema_upgrade_callback callback)
    {
        schema_upgrade_callback_function = callback;
    }

    public static Connection getSqldb()
    {
        return sqldb;
    }

    public static String bytesToString(byte[] bytes)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            return Base64.getEncoder().encodeToString(bytes);
        }
        return null;
    }

    public static String sha256sum_of_file(String filename_with_path)
    {
        try
        {
            byte[] buffer = new byte[8192];
            int count;
            long bytes_read_total = 0;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filename_with_path));
            while ((count = bis.read(buffer)) > 0)
            {
                digest.update(buffer, 0, count);
                bytes_read_total = bytes_read_total + count;
            }
            bis.close();
            Log.i(TAG, "sha256sum_of_file:bytes_read_total=" + bytes_read_total);
            byte[] hash = digest.digest();
            return (bytesToString(hash));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    static final int BINDVAR_TYPE_Int = 0;
    static final int BINDVAR_TYPE_Long = 1;
    static final int BINDVAR_TYPE_String = 2;
    static final int BINDVAR_TYPE_Boolean = 3;
    static final int BINDVAR_OFFSET_WHERE = 400;
    static final int BINDVAR_OFFSET_SET = 600;

    public static class OrmaBindvar
    {
        int type;
        Object value;

        OrmaBindvar(final int type, final Object value)
        {
            this.type = type;
            this.value = value;
        }
    }

    /*
     * repair or finally replace a string that is not correct UTF-8
     */
    @Deprecated
    static String safe_string_sql(String in)
    {
        if (in == null)
        {
            return null;
        }

        if (in.equals(""))
        {
            return "";
        }

        try
        {
            byte[] bytes = in.getBytes(StandardCharsets.UTF_8);
            for (int i = 0; i < bytes.length; i++)
            {
                if (bytes[i] == 0)
                {
                    bytes[i] = '_';
                }
            }
            return (new String(bytes, StandardCharsets.UTF_8));
        }
        catch (Exception e)
        {
            Log.i(TAG, "safe_string_sql:EE:" + e.getMessage());
            e.printStackTrace();
        }
        return "__ERROR_IN_STRING__";
    }

    public static long get_last_rowid_pstmt()
    {
        // orma_global_sqlgetlastrowid_lock.lock();
        try
        {
            long ret = -1;
            PreparedStatement lastrowid_pstmt = sqldb.prepareStatement("select last_insert_rowid() as lastrowid");
            try
            {
                ResultSet rs = lastrowid_pstmt.executeQuery();
                if (rs.next())
                {
                    ret = rs.getLong("lastrowid");
                }
                rs.close();
                lastrowid_pstmt.close();
                // Log.i(TAG, "get_last_rowid_pstmt:ret=" + ret);
            }
            catch(Exception e3)
            {
                Log.i(TAG, "ERR:GLRI:001:" + e3.getMessage());
                try
                {
                    lastrowid_pstmt.close();
                }
                catch(Exception e4)
                {
                }
            }
            return ret;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "ERR:GLRI:002:" + e.getMessage());
            return -1;
        }
        finally
        {
            // orma_global_sqlgetlastrowid_lock.unlock();
        }
    }

    /*
     * escape to prevent SQL injection, very basic and bad!
     * TODO: make me better (and later use prepared statements)
     */
    @Deprecated
    public static String s(String str)
    {
        // TODO: bad!! use prepared statements
        String data = "";

        str = safe_string_sql(str);

        if (str == null || str.length() == 0)
        {
            return "";
        }

        if (str != null && str.length() > 0)
        {
            str = str.
                    // replace("\\", "\\\\"). // \ -> \\
                    // replace("%", "\\%"). // % -> \%
                    // replace("_", "\\_"). // _ -> \_
                            replace("'", "''"). // ' -> ''
                            replace("\\x1a", "\\Z"); // \\x1a --> EOF char
            data = str;
        }

        return data;
    }

    public static String s(int i)
    {
        return "" + i;
    }

    public static String s(long l)
    {
        return "" + l;
    }

    public static int b(boolean in)
    {
        if (in == true)
        {
            return 1;
        }
        else
        {
            return 0;
        }
    }

    public static String readSQLFileAsString(String filePath) throws java.io.IOException
    {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line, results = "";
        while ((line = reader.readLine()) != null)
        {
            results += line;
        }
        reader.close();
        return results;
    }

    public static String get_current_sqlite_version()
    {
        String ret = "unknown";

        orma_global_sqlfreehand_lock.lock();
        Statement statement = null;
        try
        {
            statement = sqldb.createStatement();
            final ResultSet rs = statement.executeQuery("SELECT sqlite_version()");
            if (rs.next())
            {
                ret = rs.getString(1);
            }
            try
            {
                rs.close();
            }
            catch (Exception e)
            {
                Log.i(TAG, "ERR:CSQLV:001:" + e.getMessage());
                e.printStackTrace();
            }
            return ret;
        }
        catch (Exception e)
        {
            Log.i(TAG, "ERR:CSQLV:002:" + e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                statement.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            orma_global_sqlfreehand_lock.unlock();
        }

        return ret;
    }

    public static int get_current_db_version()
    {
        int ret = 0;

        orma_global_sqlfreehand_lock.lock();
        Statement statement = null;
        try
        {
            statement = sqldb.createStatement();
            ResultSet rs = statement.executeQuery(
                    "select db_version from orma_schema order by db_version desc limit 1");
            if (rs.next())
            {
                ret = rs.getInt("db_version");
            }
            try
            {
                rs.close();
            }
            catch (Exception e)
            {
                Log.i(TAG, "ERR:CDBV:001:" + e.getMessage());
                e.printStackTrace();
            }

            try
            {
                statement.close();
            }
            catch (Exception e)
            {
                Log.i(TAG, "ERR:CDBV:002:" + e.getMessage());
            }

            return ret;
        }
        catch (Exception e)
        {
            try
            {
                statement.close();
            }
            catch (Exception e2)
            {
                e2.printStackTrace();
            }

            ret = 0;

            try
            {
                final String update_001 = "CREATE TABLE orma_schema (db_version INTEGER NOT NULL);";
                run_multi_sql(update_001);
                final String update_002 = "insert into orma_schema values ('0');";
                run_multi_sql(update_002);
            }
            catch (Exception e2)
            {
                Log.i(TAG, "ERR:CDBV:003:" + e2.getMessage());
                e2.printStackTrace();
            }
        }
        finally
        {
            try
            {
                statement.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            orma_global_sqlfreehand_lock.unlock();
        }

        return ret;
    }

    public static int get_current_db_legacy_version()
    {
        int ret = 0;

        orma_global_sqlfreehand_lock.lock();
        Statement statement = null;
        try
        {
            statement = sqldb.createStatement();
            ResultSet rs = statement.executeQuery(
                    "PRAGMA user_version");
            if (rs.next())
            {
                ret = rs.getInt(1);
            }
            try
            {
                rs.close();
            }
            catch (Exception e)
            {
                Log.i(TAG, "ERR:CLDBV:001:" + e.getMessage());
                e.printStackTrace();
            }

            try
            {
                statement.close();
            }
            catch (Exception e)
            {
                Log.i(TAG, "ERR:CLDBV:002:" + e.getMessage());
            }

            try
            {
                if (ret > 0)
                {
                    set_new_db_version(ret);
                }
            }
            catch (Exception e2)
            {
                e2.printStackTrace();
            }

            return ret;
        }
        catch (Exception e2)
        {
            e2.printStackTrace();
        }
        finally
        {
            try
            {
                statement.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            orma_global_sqlfreehand_lock.unlock();
        }

        return ret;
    }

    public static void set_new_db_version(int new_version)
    {
        orma_global_sqlfreehand_lock.lock();
        try
        {
            final String update_001 = "update orma_schema set db_version='" + new_version + "';";
            run_multi_sql(update_001);
        }
        catch (Exception e2)
        {
            Log.i(TAG, "ERR:SNDBV:001:" + e2.getMessage());
            e2.printStackTrace();
        }
        finally
        {
            orma_global_sqlfreehand_lock.unlock();
        }
    }

    public static int update_db(final int current_db_version)
    {
        set_new_db_version(THIS_DB_SCHEMA_VERSION);
        // return the updated DB VERSION
        return THIS_DB_SCHEMA_VERSION;
    }

    private static boolean check_db_open()
    {
        boolean ret2 = false;
        try
        {
            Statement statement = sqldb.createStatement();
            ResultSet rs = statement.executeQuery(
                    "SELECT count(*) as sqlite_master_count FROM sqlite_master");
            if (rs.next())
            {
                long ret3 = rs.getLong("sqlite_master_count");
                ret2 = true;
            }
            else
            {
                Log.i(TAG, "ERR:CHECK_DB_OPEN:001:can not read sqlite_master table");
                throw new RuntimeException();
            }

            try
            {
                statement.close();
            }
            catch (Exception ignored)
            {
                Log.i(TAG, "ERR:CHECK_DB_OPEN:002:can not close statement");
                throw new RuntimeException();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "ERR:CHECK_DB_OPEN:003:some other error opening the DB");
            throw new RuntimeException(e);
        }
        Log.i(TAG, "INFO:CHECK_DB_OPEN:003:DB is open");
        return ret2;
    }

    public static void shutdown()
    {
        Log.i(TAG, "SHUTDOWN:start");
        try
        {
            sqldb.close();
        }
        catch (Exception e2)
        {
            Log.i(TAG, "ERR:SHUTDOWN:001:" + e2.getMessage());
            e2.printStackTrace();
        }
        Log.i(TAG, "SHUTDOWN:finished");
    }

    public static void init(final int db_schema_version) throws Exception
    {
        THIS_DB_SCHEMA_VERSION = db_schema_version;

        Log.i(TAG, "INIT:start");
        // create a database connection
        try
        {
            Class.forName("org.sqlite.JDBC");
        }
        catch(Exception e)
        {
        }

        try
        {
            // HINT: "user" is always "NULL" here
            sqldb = DriverManager.getConnection("jdbc:sqlite:" + OrmaDatabase.db_file_path, null, OrmaDatabase.secrect_key);
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }

        // HINT: check if the database can be opened (e.g. the password for an sqlcipher db is correct), if not throw RuntimeException
        check_db_open();

        if (OrmaDatabase.wal_mode)
        {
            Log.i(TAG, "INIT:journal_mode=" + run_query_for_single_result("PRAGMA journal_mode;"));
            Log.i(TAG, "INIT:journal_size_limit=" + run_query_for_single_result("PRAGMA journal_size_limit;"));

            // set WAL mode
            final String set_wal_mode = "PRAGMA journal_mode = WAL;";
            run_multi_sql(set_wal_mode);
            Log.i(TAG, "INIT:setting WAL mode");

            Log.i(TAG, "INIT:journal_mode=" + run_query_for_single_result("PRAGMA journal_mode;"));
            Log.i(TAG, "INIT:journal_size_limit=" + run_query_for_single_result("PRAGMA journal_size_limit;"));
            Log.i(TAG, "INIT:wal_autocheckpoint=" + run_query_for_single_result("PRAGMA wal_autocheckpoint;"));

            // set journal and wal size limit to 10 MB
            final String set_journal_size_limit = "PRAGMA journal_size_limit = " + (10 * 1024 * 1024) + ";";
            run_multi_sql(set_journal_size_limit);
            Log.i(TAG, "INIT:setting journal_size_limit");

            // set wal_autocheckpoint
            final String set_wal_autocheckpoint = "PRAGMA wal_autocheckpoint = 1000;";
            run_multi_sql(set_wal_autocheckpoint);
            Log.i(TAG, "INIT:setting wal_autocheckpoint");


            Log.i(TAG, "INIT:journal_mode=" + run_query_for_single_result("PRAGMA journal_mode;"));
            Log.i(TAG, "INIT:journal_size_limit=" + run_query_for_single_result("PRAGMA journal_size_limit;"));
            Log.i(TAG, "INIT:wal_autocheckpoint=" + run_query_for_single_result("PRAGMA wal_autocheckpoint;"));
        } else {
            // turn off WAL mode (since this setting will persist inside the database even after a restart)
            final String set_wal_mode = "PRAGMA journal_mode = DELETE;";
            run_multi_sql(set_wal_mode);
            Log.i(TAG, "INIT:turning OFF WAL mode");
        }

        Log.i(TAG, "loaded:sqlite:" + get_current_sqlite_version());

        // --------------- CREATE THE DATABASE ---------------
        // --------------- CREATE THE DATABASE ---------------
        // --------------- CREATE THE DATABASE ---------------
        // --------------- CREATE THE DATABASE ---------------
        current_db_schema_version = get_current_db_version();
        if (current_db_schema_version == 0)
        {
            // HINT: try to read "PRAGMA user_version" and see if there is some legacy value there
            current_db_schema_version = get_current_db_legacy_version();
        }
        Log.i(TAG, "trifa:current_db_version=" + current_db_schema_version);
        if ((current_db_schema_version < 0) || (THIS_DB_SCHEMA_VERSION < 0))
        {
            Log.i(TAG, "trifa:current_db_schema_version and/or THIS_DB_SCHEMA_VERSION are negative numbers, this is not allowed!");
        }
        if ((current_db_schema_version == 0) && (THIS_DB_SCHEMA_VERSION == 0))
        {
            Log.i(TAG, "trifa:current_db_schema_version and THIS_DB_SCHEMA_VERSION are both 0, this is not allowed!");
        }
        if (current_db_schema_version < THIS_DB_SCHEMA_VERSION)
        {
            for (int cur=current_db_schema_version;cur<THIS_DB_SCHEMA_VERSION;cur++)
            {
                Log.i(TAG, "trifa:calling schema upgrade callback function for " + cur + " -> " + (cur + 1));
                if (schema_upgrade_callback_function != null)
                {
                    schema_upgrade_callback_function.upgrade(cur, (cur + 1));
                }
            }
        }
        current_db_schema_version = update_db(current_db_schema_version);
        Log.i(TAG, "trifa:new_db_version=" + current_db_schema_version);
        // --------------- CREATE THE DATABASE ---------------
        // --------------- CREATE THE DATABASE ---------------
        // --------------- CREATE THE DATABASE ---------------
        // --------------- CREATE THE DATABASE ---------------
        Log.i(TAG, "INIT:finished");
    }

    /*
     * Runs SQL statements that are seperated by ";" character
     */
    public static void run_multi_sql(String sql_multi)
    {
        orma_global_sqlfreehand_lock.lock();
        try
        {
            Statement statement = null;

            String[] queries = sql_multi.split(";");
            for (String query : queries)
            {
                try
                {
                    statement = sqldb.createStatement();
                    statement.setQueryTimeout(10);  // set timeout to x sec.
                }
                catch (Exception e)
                {
                    Log.i(TAG, "ERR:MS:001:" + e.getMessage());
                }

                try
                {
                    if (ORMA_TRACE)
                    {
                        Log.i(TAG, "sql=" + query);
                    }
                    statement.executeUpdate(query);
                }
                catch (Exception e)
                {
                    Log.i(TAG, "ERR:MS:002:" + e.getMessage());
                }

                try
                {
                    statement.close();
                }
                catch (Exception e)
                {
                    Log.i(TAG, "ERR:MS:003:" + e.getMessage());
                }
            }
        }
        catch (Exception e)
        {
            Log.i(TAG, "ERR:MS:004:" + e.getMessage());
        }
        finally
        {
            orma_global_sqlfreehand_lock.unlock();
        }
    }

    public static String run_query_for_single_result(String sql_query)
    {
        String text_result = null;

        orma_global_sqlfreehand_lock.lock();
        try
        {
            Statement statement = null;
            try
            {
                statement = sqldb.createStatement();
                statement.setQueryTimeout(10);  // set timeout to x sec.
            }
            catch (Exception e)
            {
                Log.i(TAG, "ERR:QSL:001:" + e.getMessage());
            }

            try
            {
                if (ORMA_TRACE)
                {
                    Log.i(TAG, "sql=" + sql_query);
                }
                ResultSet rs = statement.executeQuery(sql_query);
                if (rs.next())
                {
                    text_result = rs.getObject(1).toString();
                }
                rs.close();
            }
            catch (Exception e)
            {
                Log.i(TAG, "ERR:QSL:002:" + e.getMessage());
            }

            try
            {
                statement.close();
            }
            catch (Exception e)
            {
                Log.i(TAG, "ERR:QSL:003:" + e.getMessage());
            }
        }
        catch (Exception e)
        {
            Log.i(TAG, "ERR:QSL:004:" + e.getMessage());
        }
        finally
        {
            orma_global_sqlfreehand_lock.unlock();
        }

        return text_result;
    }

    public static long run_query_for_single_result_l(String sql_query)
    {
        long long_result = 0L;

        orma_global_sqlfreehand_lock.lock();
        try
        {
            Statement statement = null;
            try
            {
                statement = sqldb.createStatement();
                statement.setQueryTimeout(10);  // set timeout to x sec.
            }
            catch (Exception e)
            {
                Log.i(TAG, "ERR:QSLL:001:" + e.getMessage());
            }

            try
            {
                if (ORMA_TRACE)
                {
                    Log.i(TAG, "sql=" + sql_query);
                }
                ResultSet rs = statement.executeQuery(sql_query);
                if (rs.next())
                {
                    long_result = rs.getLong(1);
                }
                rs.close();
            }
            catch (Exception e)
            {
                Log.i(TAG, "ERR:QSLL:002:" + e.getMessage());
            }

            try
            {
                statement.close();
            }
            catch (Exception e)
            {
                Log.i(TAG, "ERR:QSLL:003:" + e.getMessage());
            }
        }
        catch (Exception e)
        {
            Log.i(TAG, "ERR:QSLL:004:" + e.getMessage());
        }
        finally
        {
            orma_global_sqlfreehand_lock.unlock();
        }

        return long_result;
    }

    public static String now_datetime_utc()
    {
        return run_query_for_single_result("select datetime('now')");
    }

    public static String now_datetime_localtime()
    {
        return run_query_for_single_result("select datetime('now','localtime')");
    }

    public static long now_unixepoch_utc()
    {
        return run_query_for_single_result_l("select unixepoch('now')");
    }

    public static long now_unixepoch_localtime()
    {
        return run_query_for_single_result_l("select unixepoch('now','localtime')");
    }

    public static boolean set_bindvars_where(final PreparedStatement statement,
                                             final int bind_where_count,
                                             final List<OrmaBindvar> bind_where_vars)
    {
        try {
            statement.clearParameters();
            if (bind_where_count > 0) {
                try {
                    for (int jj = 0; jj < bind_where_count; jj++) {
                        int type = bind_where_vars.get(jj).type;
                        if (type == BINDVAR_TYPE_Int) {
                            statement.setInt((jj + BINDVAR_OFFSET_WHERE), (int) bind_where_vars.get(jj).value);
                        } else if (type == BINDVAR_TYPE_Long) {
                            statement.setLong((jj + BINDVAR_OFFSET_WHERE), (long) bind_where_vars.get(jj).value);
                        } else if (type == BINDVAR_TYPE_String) {
                            statement.setString((jj + BINDVAR_OFFSET_WHERE), (String) bind_where_vars.get(jj).value);
                        } else if (type == BINDVAR_TYPE_Boolean) {
                            statement.setBoolean((jj + BINDVAR_OFFSET_WHERE), (boolean) bind_where_vars.get(jj).value);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERR:SBV:001:" + e.getMessage());
                }
            }
        }
        catch(Exception e1)
        {
            Log.i(TAG, "ERR:SBV:002:" + e1.getMessage());
            return false;
        }
        return true;
    }

    public static boolean set_bindvars_where_and_set(final PreparedStatement statement,
                                                     final int bind_where_count,
                                                     final List<OrmaBindvar> bind_where_vars,
                                                     final int bind_set_count,
                                                     final List<OrmaBindvar> bind_set_vars)
    {
        try {
            statement.clearParameters();
            if (bind_set_count > 0)
            {
                try {
                    for (int jj = 0; jj < bind_set_count; jj++) {
                        int type = bind_set_vars.get(jj).type;
                        if (type == BINDVAR_TYPE_Int) {
                            statement.setInt((jj + BINDVAR_OFFSET_SET),
                                    (int) bind_set_vars.get(jj).value);
                        } else if (type == BINDVAR_TYPE_Long) {
                            statement.setLong((jj + BINDVAR_OFFSET_SET),
                                    (long) bind_set_vars.get(jj).value);
                        } else if (type == BINDVAR_TYPE_String) {
                            statement.setString((jj + BINDVAR_OFFSET_SET),
                                    (String) bind_set_vars.get(jj).value);
                        } else if (type == BINDVAR_TYPE_Boolean) {
                            statement.setBoolean((jj + BINDVAR_OFFSET_SET),
                                    (boolean) bind_set_vars.get(jj).value);
                        }
                    }
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                    Log.i(TAG, "ERR:SBVWS:001:" + e.getMessage());
                }
            }
            if (bind_where_count > 0)
            {
                try {
                    for (int jj = 0; jj < bind_where_count; jj++) {
                        int type = bind_where_vars.get(jj).type;
                        if (type == BINDVAR_TYPE_Int) {
                            statement.setInt((jj + BINDVAR_OFFSET_WHERE),
                                    (int) bind_where_vars.get(jj).value);
                        } else if (type == BINDVAR_TYPE_Long) {
                            statement.setLong((jj + BINDVAR_OFFSET_WHERE),
                                    (long) bind_where_vars.get(jj).value);
                        } else if (type == BINDVAR_TYPE_String) {
                            statement.setString((jj + BINDVAR_OFFSET_WHERE),
                                    (String) bind_where_vars.get(jj).value);
                        } else if (type == BINDVAR_TYPE_Boolean) {
                            statement.setBoolean((jj + BINDVAR_OFFSET_WHERE),
                                    (boolean) bind_where_vars.get(jj).value);
                        }
                    }
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                    Log.i(TAG, "ERR:SBVWS:002:" + e.getMessage());
                }
            }
        }
        catch(Exception e1)
        {
            Log.i(TAG, "ERR:SBVWS:003:" + e1.getMessage());
            return false;
        }
        return true;
    }

    public static void log_bindvars_where(final String sql, final int bind_where_count, final List<OrmaBindvar> bind_where_vars)
    {
        if (ORMA_TRACE)
        {
            Log.i(TAG, "sql=" + sql + " bindvar count=" + bind_where_count);
            if (bind_where_count > 0)
            {
                for(int jj=0;jj<bind_where_count;jj++) {
                    Log.i(TAG, "bindvar ?" + (jj + BINDVAR_OFFSET_WHERE) +
                            " = " + bind_where_vars.get(jj).value);
                }
            }
        }
    }

    public static void log_bindvars_where_and_set(final String sql, final int bind_where_count,
                                                  final List<OrmaBindvar> bind_where_vars,
                                                  final int bind_set_count,
                                                  final List<OrmaBindvar> bind_set_vars)
    {
        if (ORMA_TRACE)
        {
            Log.i(TAG, "sql=" + sql + " bindvar count=" + (bind_set_count + bind_where_count));
            if (bind_set_count > 0)
            {
                for(int jj=0;jj<bind_set_count;jj++) {
                    Log.i(TAG, "bindvar set ?" + (jj + BINDVAR_OFFSET_SET) +
                            " = " + bind_set_vars.get(jj).value);
                }
            }
            if (bind_where_count > 0)
            {
                for(int jj=0;jj<bind_where_count;jj++) {
                    Log.i(TAG, "bindvar where ?" + (jj + BINDVAR_OFFSET_WHERE) +
                            " = " + bind_where_vars.get(jj).value);
                }
            }
        }
    }


    public Message selectFromMessage()
    {
        Message ret = new Message();
        ret.sql_start = "SELECT * FROM \"Message\"";
        return ret;
    }

    public long insertIntoMessage(Message obj)
    {
        return obj.insert();
    }

    public Message updateMessage()
    {
        Message ret = new Message();
        ret.sql_start = "UPDATE \"Message\"";
        return ret;
    }

    public Message deleteFromMessage()
    {
        Message ret = new Message();
        ret.sql_start = "DELETE FROM \"Message\"";
        return ret;
    }


    public TRIFADatabaseGlobalsNew selectFromTRIFADatabaseGlobalsNew()
    {
        TRIFADatabaseGlobalsNew ret = new TRIFADatabaseGlobalsNew();
        ret.sql_start = "SELECT * FROM \"TRIFADatabaseGlobalsNew\"";
        return ret;
    }

    public long insertIntoTRIFADatabaseGlobalsNew(TRIFADatabaseGlobalsNew obj)
    {
        return obj.insert();
    }

    public TRIFADatabaseGlobalsNew updateTRIFADatabaseGlobalsNew()
    {
        TRIFADatabaseGlobalsNew ret = new TRIFADatabaseGlobalsNew();
        ret.sql_start = "UPDATE \"TRIFADatabaseGlobalsNew\"";
        return ret;
    }

    public TRIFADatabaseGlobalsNew deleteFromTRIFADatabaseGlobalsNew()
    {
        TRIFADatabaseGlobalsNew ret = new TRIFADatabaseGlobalsNew();
        ret.sql_start = "DELETE FROM \"TRIFADatabaseGlobalsNew\"";
        return ret;
    }


    public FileDB selectFromFileDB()
    {
        FileDB ret = new FileDB();
        ret.sql_start = "SELECT * FROM \"FileDB\"";
        return ret;
    }

    public long insertIntoFileDB(FileDB obj)
    {
        return obj.insert();
    }

    public FileDB updateFileDB()
    {
        FileDB ret = new FileDB();
        ret.sql_start = "UPDATE \"FileDB\"";
        return ret;
    }

    public FileDB deleteFromFileDB()
    {
        FileDB ret = new FileDB();
        ret.sql_start = "DELETE FROM \"FileDB\"";
        return ret;
    }


    public GroupDB selectFromGroupDB()
    {
        GroupDB ret = new GroupDB();
        ret.sql_start = "SELECT * FROM \"GroupDB\"";
        return ret;
    }

    public long insertIntoGroupDB(GroupDB obj)
    {
        return obj.insert();
    }

    public GroupDB updateGroupDB()
    {
        GroupDB ret = new GroupDB();
        ret.sql_start = "UPDATE \"GroupDB\"";
        return ret;
    }

    public GroupDB deleteFromGroupDB()
    {
        GroupDB ret = new GroupDB();
        ret.sql_start = "DELETE FROM \"GroupDB\"";
        return ret;
    }


    public ConferenceMessage selectFromConferenceMessage()
    {
        ConferenceMessage ret = new ConferenceMessage();
        ret.sql_start = "SELECT * FROM \"ConferenceMessage\"";
        return ret;
    }

    public long insertIntoConferenceMessage(ConferenceMessage obj)
    {
        return obj.insert();
    }

    public ConferenceMessage updateConferenceMessage()
    {
        ConferenceMessage ret = new ConferenceMessage();
        ret.sql_start = "UPDATE \"ConferenceMessage\"";
        return ret;
    }

    public ConferenceMessage deleteFromConferenceMessage()
    {
        ConferenceMessage ret = new ConferenceMessage();
        ret.sql_start = "DELETE FROM \"ConferenceMessage\"";
        return ret;
    }


    public FriendList selectFromFriendList()
    {
        FriendList ret = new FriendList();
        ret.sql_start = "SELECT * FROM \"FriendList\"";
        return ret;
    }

    public long insertIntoFriendList(FriendList obj)
    {
        return obj.insert();
    }

    public FriendList updateFriendList()
    {
        FriendList ret = new FriendList();
        ret.sql_start = "UPDATE \"FriendList\"";
        return ret;
    }

    public FriendList deleteFromFriendList()
    {
        FriendList ret = new FriendList();
        ret.sql_start = "DELETE FROM \"FriendList\"";
        return ret;
    }


    public RelayListDB selectFromRelayListDB()
    {
        RelayListDB ret = new RelayListDB();
        ret.sql_start = "SELECT * FROM \"RelayListDB\"";
        return ret;
    }

    public long insertIntoRelayListDB(RelayListDB obj)
    {
        return obj.insert();
    }

    public RelayListDB updateRelayListDB()
    {
        RelayListDB ret = new RelayListDB();
        ret.sql_start = "UPDATE \"RelayListDB\"";
        return ret;
    }

    public RelayListDB deleteFromRelayListDB()
    {
        RelayListDB ret = new RelayListDB();
        ret.sql_start = "DELETE FROM \"RelayListDB\"";
        return ret;
    }


    public GroupPeerDB selectFromGroupPeerDB()
    {
        GroupPeerDB ret = new GroupPeerDB();
        ret.sql_start = "SELECT * FROM \"GroupPeerDB\"";
        return ret;
    }

    public long insertIntoGroupPeerDB(GroupPeerDB obj)
    {
        return obj.insert();
    }

    public GroupPeerDB updateGroupPeerDB()
    {
        GroupPeerDB ret = new GroupPeerDB();
        ret.sql_start = "UPDATE \"GroupPeerDB\"";
        return ret;
    }

    public GroupPeerDB deleteFromGroupPeerDB()
    {
        GroupPeerDB ret = new GroupPeerDB();
        ret.sql_start = "DELETE FROM \"GroupPeerDB\"";
        return ret;
    }


    public ConferenceDB selectFromConferenceDB()
    {
        ConferenceDB ret = new ConferenceDB();
        ret.sql_start = "SELECT * FROM \"ConferenceDB\"";
        return ret;
    }

    public long insertIntoConferenceDB(ConferenceDB obj)
    {
        return obj.insert();
    }

    public ConferenceDB updateConferenceDB()
    {
        ConferenceDB ret = new ConferenceDB();
        ret.sql_start = "UPDATE \"ConferenceDB\"";
        return ret;
    }

    public ConferenceDB deleteFromConferenceDB()
    {
        ConferenceDB ret = new ConferenceDB();
        ret.sql_start = "DELETE FROM \"ConferenceDB\"";
        return ret;
    }


    public Filetransfer selectFromFiletransfer()
    {
        Filetransfer ret = new Filetransfer();
        ret.sql_start = "SELECT * FROM \"Filetransfer\"";
        return ret;
    }

    public long insertIntoFiletransfer(Filetransfer obj)
    {
        return obj.insert();
    }

    public Filetransfer updateFiletransfer()
    {
        Filetransfer ret = new Filetransfer();
        ret.sql_start = "UPDATE \"Filetransfer\"";
        return ret;
    }

    public Filetransfer deleteFromFiletransfer()
    {
        Filetransfer ret = new Filetransfer();
        ret.sql_start = "DELETE FROM \"Filetransfer\"";
        return ret;
    }


    public BootstrapNodeEntryDB selectFromBootstrapNodeEntryDB()
    {
        BootstrapNodeEntryDB ret = new BootstrapNodeEntryDB();
        ret.sql_start = "SELECT * FROM \"BootstrapNodeEntryDB\"";
        return ret;
    }

    public long insertIntoBootstrapNodeEntryDB(BootstrapNodeEntryDB obj)
    {
        return obj.insert();
    }

    public BootstrapNodeEntryDB updateBootstrapNodeEntryDB()
    {
        BootstrapNodeEntryDB ret = new BootstrapNodeEntryDB();
        ret.sql_start = "UPDATE \"BootstrapNodeEntryDB\"";
        return ret;
    }

    public BootstrapNodeEntryDB deleteFromBootstrapNodeEntryDB()
    {
        BootstrapNodeEntryDB ret = new BootstrapNodeEntryDB();
        ret.sql_start = "DELETE FROM \"BootstrapNodeEntryDB\"";
        return ret;
    }


    public ConferencePeerCacheDB selectFromConferencePeerCacheDB()
    {
        ConferencePeerCacheDB ret = new ConferencePeerCacheDB();
        ret.sql_start = "SELECT * FROM \"ConferencePeerCacheDB\"";
        return ret;
    }

    public long insertIntoConferencePeerCacheDB(ConferencePeerCacheDB obj)
    {
        return obj.insert();
    }

    public ConferencePeerCacheDB updateConferencePeerCacheDB()
    {
        ConferencePeerCacheDB ret = new ConferencePeerCacheDB();
        ret.sql_start = "UPDATE \"ConferencePeerCacheDB\"";
        return ret;
    }

    public ConferencePeerCacheDB deleteFromConferencePeerCacheDB()
    {
        ConferencePeerCacheDB ret = new ConferencePeerCacheDB();
        ret.sql_start = "DELETE FROM \"ConferencePeerCacheDB\"";
        return ret;
    }


    public GroupMessage selectFromGroupMessage()
    {
        GroupMessage ret = new GroupMessage();
        ret.sql_start = "SELECT * FROM \"GroupMessage\"";
        return ret;
    }

    public long insertIntoGroupMessage(GroupMessage obj)
    {
        return obj.insert();
    }

    public GroupMessage updateGroupMessage()
    {
        GroupMessage ret = new GroupMessage();
        ret.sql_start = "UPDATE \"GroupMessage\"";
        return ret;
    }

    public GroupMessage deleteFromGroupMessage()
    {
        GroupMessage ret = new GroupMessage();
        ret.sql_start = "DELETE FROM \"GroupMessage\"";
        return ret;
    }

}

