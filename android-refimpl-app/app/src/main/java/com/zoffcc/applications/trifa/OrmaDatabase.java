package com.zoffcc.applications.trifa;

import android.database.Cursor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class OrmaDatabase extends com.zoffcc.applications.sorm.OrmaDatabase
{
    private final String TAG = "OrmaDatabase_:";

    public OrmaDatabase(final String db_file_path, final String secrect_key, boolean wal_mode)
    {
        java.io.File db = new java.io.File(db_file_path);
        try
        {
            String class_sqlite = String.valueOf(Class.forName("org.sqlite.JDBC"));
            System.out.println(TAG + class_sqlite);

            open_db(db_file_path, secrect_key, wal_mode);
            boolean db_open = check_db_open();
            if (!db_open)
            {
                System.out.println(TAG + "error opening DB");
                throw new RuntimeException();
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

    }

    public OrmaDatabase getConnection()
    {
        return this;
    }

    public void execSQL(String s)
    {
        run_multi_sql(s);
    }

    public Cursor rawQuery(String s)
    {
        // TODO: !!!!!!!!write me!!!!!!!!
        Cursor c = null;
        return c;
    }

    private boolean check_db_open()
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
                System.out.println(TAG + "sqlite_master_count: " + ret3);
                ret2 = true;
            }
            else
            {
                throw new RuntimeException();
            }

            try
            {
                statement.close();
            }
            catch (Exception ignored)
            {
                throw new RuntimeException();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println(TAG + "DBERR: database could not be opened!!");
            throw new RuntimeException(e);
        }
        return ret2;
    }

    private void open_db(final String db_file_path, final String password, boolean wal_mode)
    {
        try
        {
            sqldb = DriverManager.getConnection("jdbc:sqlite:" + db_file_path);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        // set password
        // TODO: escape password, but we can not use prepared statement here :-(
        final String set_key = "PRAGMA key = '" + password + "';";
        run_multi_sql(set_key);

        if  (wal_mode)
        {
            // set WAL mode
            final String set_wal_mode = "PRAGMA journal_mode = WAL;";
            run_multi_sql(set_wal_mode);
        }
    }

    private void close_db()
    {
        shutdown();
    }
}
