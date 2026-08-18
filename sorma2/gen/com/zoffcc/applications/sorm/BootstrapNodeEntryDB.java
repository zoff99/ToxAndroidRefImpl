/* SPDX-License-Identifier: GPL-3.0-or-later
 * [sorma2], Java part of sorma2
 * Copyright (C) 2024 Zoff <zoff@zoff.cc>
 */

package com.zoffcc.applications.sorm;

import com.zoffcc.applications.sorm.Log;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.zoffcc.applications.sorm.OrmaDatabase.*;


@Table
public class BootstrapNodeEntryDB
{
    private static final String TAG = "DB.BootstrapNodeEntr";
    @PrimaryKey(autoincrement = true, auto = true)
    public long id;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public long num;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public boolean udp_node; // true -> UDP bootstrap node, false -> TCP relay node

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public String ip;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public long port;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public String key_hex;

    public static BootstrapNodeEntryDB deep_copy(BootstrapNodeEntryDB in)
    {
        BootstrapNodeEntryDB out = new BootstrapNodeEntryDB();
        out.id = in.id;
        out.num = in.num;
        out.udp_node = in.udp_node;
        out.ip = in.ip;
        out.port = in.port;
        out.key_hex = in.key_hex;

        return out;
    }

    @Override
    public String toString()
    {
        return "id=" + id + ", num=" + num + ", udp_node=" + udp_node + ", ip=" + ip + ", port=" + port + ", key_hex=" + key_hex;
    }



    String sql_start = "";
    String sql_set = "";
    String sql_where = "where 1=1 "; // where
    String sql_orderby = ""; // order by
    String sql_limit = ""; // limit
    List<OrmaBindvar> bind_where_vars = new ArrayList<>();
    int bind_where_count = 0;
    List<OrmaBindvar> bind_set_vars = new ArrayList<>();
    int bind_set_count = 0;

    private String sanitizeColumnName(String input)
    {
        if (input == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i++)
        {
            char c = input.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_' || c == '-')
            {
                sb.append(c);
            }
            else
            {
                sb.append('_');
            }
        }
        return sb.toString();
    }

    public List<BootstrapNodeEntryDB> toList()
    {
        return toList(null);
    }

    public List<BootstrapNodeEntryDB> toList(String[] columns)
    {
        List<BootstrapNodeEntryDB> list = new ArrayList<>();
        orma_global_sqltolist_lock.lock();
        PreparedStatement statement = null;
        boolean selectAll = (columns == null || columns.length == 0);
        Set<String> selectedCols = new LinkedHashSet<>();
        if (!selectAll) {
            for (String c : columns) {
                if (c == null || c.length() == 0) continue;
                selectedCols.add(sanitizeColumnName(c.toLowerCase()));
            }
            if (selectedCols.isEmpty()) selectAll = true;
        }
        try
        {
            if (!selectAll)
            {
                StringBuilder cols = new StringBuilder();
                boolean firstColumn = true;
                for (String col : selectedCols)
                {
                    if (!firstColumn) cols.append(", ");
                    cols.append("\"").append(col).append("\"");
                    firstColumn = false;
                }
                if (this.sql_start != null && this.sql_start.contains("*"))
                {
                    this.sql_start = this.sql_start.replace("*", cols.toString());
                }
                else
                {
                    this.sql_start = "SELECT " + cols.toString() + " FROM \"" + this.getClass().getSimpleName() + "\"";
                }
            }

            final String sql = this.sql_start + " " + this.sql_where + " " + this.sql_orderby + " " + this.sql_limit;
            log_bindvars_where(sql, bind_where_count, bind_where_vars);
            final long t1 = System.currentTimeMillis();
            statement = sqldb.prepareStatement(sql);
            if (!set_bindvars_where(statement, bind_where_count, bind_where_vars))
            {
                try
                {
                    statement.close();
                }
                catch (Exception ignored)
                {
                }
                return null;
            }
            ResultSet rs = statement.executeQuery();
            final long t2 = System.currentTimeMillis();
            if (ORMA_LONG_RUNNING_TRACE)
            {
                if ((t2 - t1) > ORMA_LONG_RUNNING_MS)
                {
                    Log.i(TAG, "long running (" + (t2 - t1)+ " ms) sql=" + sql);
                }
            }
            final long t3 = System.currentTimeMillis();
            while (rs.next())
            {
                BootstrapNodeEntryDB out = new BootstrapNodeEntryDB();
                if (selectAll || selectedCols.contains("id".toLowerCase())) out.id = rs.getLong("id");
                if (selectAll || selectedCols.contains("num".toLowerCase())) out.num = rs.getLong("num");
                if (selectAll || selectedCols.contains("udp_node".toLowerCase())) out.udp_node = rs.getBoolean("udp_node");
                if (selectAll || selectedCols.contains("ip".toLowerCase())) out.ip = rs.getString("ip");
                if (selectAll || selectedCols.contains("port".toLowerCase())) out.port = rs.getLong("port");
                if (selectAll || selectedCols.contains("key_hex".toLowerCase())) out.key_hex = rs.getString("key_hex");

                list.add(out);
            }
            final long t4 = System.currentTimeMillis();
            if (ORMA_LONG_RUNNING_TRACE)
            {
                if ((t4 - t3) > ORMA_LONG_RUNNING_MS)
                {
                    Log.i(TAG, "long running (" + (t4 - t3)+ " ms) fetch=" + sql);
                }
            }
            try
            {
                rs.close();
            }
            catch (Exception ignored)
            {
            }

            try
            {
                statement.close();
            }
            catch (Exception ignored)
            {
            }
        }
        catch (Exception e)
        {
            Log.i(TAG, "ERR:toList:001:" + e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                statement.close();
            }
            catch (Exception ignored)
            {
            }
            orma_global_sqltolist_lock.unlock();
        }

        return list;
    }


    public long insert()
    {
        long ret = -1;

        orma_global_sqlinsert_lock.lock();
        PreparedStatement insert_pstmt = null;
        try
        {
            String insert_pstmt_sql = null;

            // @formatter:off
            insert_pstmt_sql ="insert into \"" + this.getClass().getSimpleName() + "\"" +
                    "("
                    + "\"num\""
                    + ",\"udp_node\""
                    + ",\"ip\""
                    + ",\"port\""
                    + ",\"key_hex\""
                    + ")" +
                    "values" +
                    "("
                    + "?1"
                    + ",?2"
                    + ",?3"
                    + ",?4"
                    + ",?5"
                    + ")";

            insert_pstmt = sqldb.prepareStatement(insert_pstmt_sql);
            insert_pstmt.clearParameters();

            insert_pstmt.setLong(1, this.num);
            insert_pstmt.setBoolean(2, this.udp_node);
            insert_pstmt.setString(3, this.ip);
            insert_pstmt.setLong(4, this.port);
            insert_pstmt.setString(5, this.key_hex);
            // @formatter:on

            if (ORMA_TRACE)
            {
                Log.i(TAG, "sql=" + insert_pstmt);
            }

            final long t1 = System.currentTimeMillis();
            orma_semaphore_lastrowid_on_insert.acquire();
            final long t2 = System.currentTimeMillis();
            if (ORMA_LONG_RUNNING_TRACE)
            {
                if ((t2 - t1) > ORMA_LONG_RUNNING_MS)
                {
                    Log.i(TAG, "insertInto"+this.getClass().getSimpleName()+" acquire running long (" + (t2 - t1)+ " ms)");
                }
            }

            final long t3 = System.currentTimeMillis();
            insert_pstmt.executeUpdate();
            final long t4 = System.currentTimeMillis();
            if (ORMA_LONG_RUNNING_TRACE)
            {
                if ((t4 - t3) > ORMA_LONG_RUNNING_MS)
                {
                    Log.i(TAG, "insertInto"+this.getClass().getSimpleName()+" sql running long (" + (t4 - t3)+ " ms)");
                }
            }

            final long t5 = System.currentTimeMillis();
            insert_pstmt.close();
            final long t6 = System.currentTimeMillis();
            if (ORMA_LONG_RUNNING_TRACE)
            {
                if ((t6 - t5) > ORMA_LONG_RUNNING_MS)
                {
                    Log.i(TAG, "insertInto"+this.getClass().getSimpleName()+" statement close running long (" + (t6 - t5)+ " ms)");
                }
            }

            final long t7 = System.currentTimeMillis();
            ret = get_last_rowid_pstmt();
            final long t8 = System.currentTimeMillis();
            if (ORMA_LONG_RUNNING_TRACE)
            {
                if ((t8 - t7) > ORMA_LONG_RUNNING_MS)
                {
                    Log.i(TAG, "insertInto"+this.getClass().getSimpleName()+" getLastRowId running long (" + (t8 - t7)+ " ms)");
                }
            }

            orma_semaphore_lastrowid_on_insert.release();
        }
        catch (Exception e)
        {
            orma_semaphore_lastrowid_on_insert.release();
            Log.i(TAG, "ERR:insert:001:" + e.getMessage());
            throw new RuntimeException(e);
        }
        finally
        {
            try
            {
                insert_pstmt.close();
            }
            catch (Exception ignored)
            {
            }
            orma_global_sqlinsert_lock.unlock();
        }

        return ret;
    }

    public BootstrapNodeEntryDB get(int i)
    {
        this.sql_limit = " limit " + i + ",1 ";
        return this.toList().get(0);
    }

    public void execute()
    {
        orma_global_sqlexecute_lock.lock();
        PreparedStatement statement = null;
        try
        {
            final String sql = this.sql_start + " " + this.sql_set + " " + this.sql_where;
            log_bindvars_where_and_set(sql, bind_where_count, bind_where_vars, bind_set_count, bind_set_vars);
            statement = sqldb.prepareStatement(sql);
            if (!set_bindvars_where_and_set(statement, bind_where_count, bind_where_vars, bind_set_count, bind_set_vars))
            {
                try
                {
                    statement.close();
                }
                catch (Exception ignored)
                {
                }
                orma_semaphore_lastrowid_on_insert.release();
                return;
            }
            statement.executeUpdate();
            try
            {
                statement.close();
            }
            catch (Exception ignored)
            {
            }
        }
        catch (Exception e2)
        {
            Log.i(TAG, "ERR:execute:001:" + e2.getMessage());
            e2.printStackTrace();
        }
        finally
        {
            try
            {
                statement.close();
            }
            catch (Exception ignored)
            {
            }
            orma_global_sqlexecute_lock.unlock();
        }
    }

    public int count()
    {
        int ret = 0;

        orma_global_sqlcount_lock.lock();
        PreparedStatement statement = null;
        try
        {
            this.sql_start = "SELECT count(*) as count FROM \"" + this.getClass().getSimpleName() + "\"";

            final String sql = this.sql_start + " " + this.sql_where + " " + this.sql_orderby + " " + this.sql_limit;
            log_bindvars_where(sql, bind_where_count, bind_where_vars);
            statement = sqldb.prepareStatement(sql);
            if (!set_bindvars_where(statement, bind_where_count, bind_where_vars))
            {
                try
                {
                    statement.close();
                }
                catch (Exception ignored)
                {
                }
                return 0;
            }
            ResultSet rs = statement.executeQuery();
            if (rs.next())
            {
                ret = rs.getInt("count");
            }
            try
            {
                rs.close();
            }
            catch (Exception ignored)
            {
            }

            try
            {
                statement.close();
            }
            catch (Exception ignored)
            {
            }
        }
        catch (Exception e)
        {
            Log.i(TAG, "ERR:count:001:" + e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                statement.close();
            }
            catch (Exception ignored)
            {
            }
            orma_global_sqlcount_lock.unlock();
        }

        return ret;
    }

    public BootstrapNodeEntryDB limit(int rowcount)
    {
        this.sql_limit = " limit " + rowcount + " ";
        return this;
    }

    public BootstrapNodeEntryDB limit(int rowcount, int offset)
    {
        this.sql_limit = " limit " + offset + " , " + rowcount;
        return this;
    }

    // ----------------------------------- //
    // ----------------------------------- //
    // ----------------------------------- //


    // ----------------- Set funcs ---------------------- //
    public BootstrapNodeEntryDB id(long id)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"id\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_set_count++;
        return this;
    }

    public BootstrapNodeEntryDB num(long num)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"num\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, num));
        bind_set_count++;
        return this;
    }

    public BootstrapNodeEntryDB udp_node(boolean udp_node)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"udp_node\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, udp_node));
        bind_set_count++;
        return this;
    }

    public BootstrapNodeEntryDB ip(String ip)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"ip\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, ip));
        bind_set_count++;
        return this;
    }

    public BootstrapNodeEntryDB port(long port)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"port\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, port));
        bind_set_count++;
        return this;
    }

    public BootstrapNodeEntryDB key_hex(String key_hex)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"key_hex\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, key_hex));
        bind_set_count++;
        return this;
    }


    // ----------------- Eq/Gt/Lt funcs ----------------- //
    public BootstrapNodeEntryDB idEq(long id)
    {
        this.sql_where = this.sql_where + " and \"id\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public BootstrapNodeEntryDB idNotEq(long id)
    {
        this.sql_where = this.sql_where + " and \"id\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public BootstrapNodeEntryDB idLt(long id)
    {
        this.sql_where = this.sql_where + " and \"id\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public BootstrapNodeEntryDB idLe(long id)
    {
        this.sql_where = this.sql_where + " and \"id\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public BootstrapNodeEntryDB idGt(long id)
    {
        this.sql_where = this.sql_where + " and \"id\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public BootstrapNodeEntryDB idGe(long id)
    {
        this.sql_where = this.sql_where + " and \"id\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public BootstrapNodeEntryDB idBetween(long id1, long id2)
    {
        this.sql_where = this.sql_where + " and \"id\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and id<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id2));
        bind_where_count++;
        return this;
    }

    public BootstrapNodeEntryDB idIsNull()
    {
        this.sql_where = this.sql_where + " and \"id\" IS NULL ";
        return this;
    }

    public BootstrapNodeEntryDB idIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"id\" IS NOT NULL ";
        return this;
    }

    public BootstrapNodeEntryDB numEq(long num)
    {
        this.sql_where = this.sql_where + " and \"num\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, num));
        bind_where_count++;
        return this;
    }

    public BootstrapNodeEntryDB numNotEq(long num)
    {
        this.sql_where = this.sql_where + " and \"num\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, num));
        bind_where_count++;
        return this;
    }

    public BootstrapNodeEntryDB numLt(long num)
    {
        this.sql_where = this.sql_where + " and \"num\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, num));
        bind_where_count++;
        return this;
    }

    public BootstrapNodeEntryDB numLe(long num)
    {
        this.sql_where = this.sql_where + " and \"num\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, num));
        bind_where_count++;
        return this;
    }

    public BootstrapNodeEntryDB numGt(long num)
    {
        this.sql_where = this.sql_where + " and \"num\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, num));
        bind_where_count++;
        return this;
    }

    public BootstrapNodeEntryDB numGe(long num)
    {
        this.sql_where = this.sql_where + " and \"num\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, num));
        bind_where_count++;
        return this;
    }

    public BootstrapNodeEntryDB numBetween(long num1, long num2)
    {
        this.sql_where = this.sql_where + " and \"num\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and num<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, num1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, num2));
        bind_where_count++;
        return this;
    }

    public BootstrapNodeEntryDB numIsNull()
    {
        this.sql_where = this.sql_where + " and \"num\" IS NULL ";
        return this;
    }

    public BootstrapNodeEntryDB numIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"num\" IS NOT NULL ";
        return this;
    }

    public BootstrapNodeEntryDB udp_nodeEq(boolean udp_node)
    {
        this.sql_where = this.sql_where + " and \"udp_node\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, udp_node));
        bind_where_count++;
        return this;
    }

    public BootstrapNodeEntryDB udp_nodeNotEq(boolean udp_node)
    {
        this.sql_where = this.sql_where + " and \"udp_node\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, udp_node));
        bind_where_count++;
        return this;
    }

    public BootstrapNodeEntryDB udp_nodeIsNull()
    {
        this.sql_where = this.sql_where + " and \"udp_node\" IS NULL ";
        return this;
    }

    public BootstrapNodeEntryDB udp_nodeIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"udp_node\" IS NOT NULL ";
        return this;
    }

    public BootstrapNodeEntryDB ipEq(String ip)
    {
        this.sql_where = this.sql_where + " and \"ip\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, ip));
        bind_where_count++;
        return this;
    }

    public BootstrapNodeEntryDB ipNotEq(String ip)
    {
        this.sql_where = this.sql_where + " and \"ip\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, ip));
        bind_where_count++;
        return this;
    }

    public BootstrapNodeEntryDB ipIsNull()
    {
        this.sql_where = this.sql_where + " and \"ip\" IS NULL ";
        return this;
    }

    public BootstrapNodeEntryDB ipIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"ip\" IS NOT NULL ";
        return this;
    }

    public BootstrapNodeEntryDB ipLike(String ip)
    {
        this.sql_where = this.sql_where + " and \"ip\" LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, ip));
        bind_where_count++;
        return this;
    }

    public BootstrapNodeEntryDB ipNotLike(String ip)
    {
        this.sql_where = this.sql_where + " and \"ip\" NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, ip));
        bind_where_count++;
        return this;
    }

    public BootstrapNodeEntryDB portEq(long port)
    {
        this.sql_where = this.sql_where + " and \"port\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, port));
        bind_where_count++;
        return this;
    }

    public BootstrapNodeEntryDB portNotEq(long port)
    {
        this.sql_where = this.sql_where + " and \"port\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, port));
        bind_where_count++;
        return this;
    }

    public BootstrapNodeEntryDB portLt(long port)
    {
        this.sql_where = this.sql_where + " and \"port\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, port));
        bind_where_count++;
        return this;
    }

    public BootstrapNodeEntryDB portLe(long port)
    {
        this.sql_where = this.sql_where + " and \"port\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, port));
        bind_where_count++;
        return this;
    }

    public BootstrapNodeEntryDB portGt(long port)
    {
        this.sql_where = this.sql_where + " and \"port\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, port));
        bind_where_count++;
        return this;
    }

    public BootstrapNodeEntryDB portGe(long port)
    {
        this.sql_where = this.sql_where + " and \"port\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, port));
        bind_where_count++;
        return this;
    }

    public BootstrapNodeEntryDB portBetween(long port1, long port2)
    {
        this.sql_where = this.sql_where + " and \"port\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and port<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, port1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, port2));
        bind_where_count++;
        return this;
    }

    public BootstrapNodeEntryDB portIsNull()
    {
        this.sql_where = this.sql_where + " and \"port\" IS NULL ";
        return this;
    }

    public BootstrapNodeEntryDB portIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"port\" IS NOT NULL ";
        return this;
    }

    public BootstrapNodeEntryDB key_hexEq(String key_hex)
    {
        this.sql_where = this.sql_where + " and \"key_hex\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, key_hex));
        bind_where_count++;
        return this;
    }

    public BootstrapNodeEntryDB key_hexNotEq(String key_hex)
    {
        this.sql_where = this.sql_where + " and \"key_hex\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, key_hex));
        bind_where_count++;
        return this;
    }

    public BootstrapNodeEntryDB key_hexIsNull()
    {
        this.sql_where = this.sql_where + " and \"key_hex\" IS NULL ";
        return this;
    }

    public BootstrapNodeEntryDB key_hexIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"key_hex\" IS NOT NULL ";
        return this;
    }

    public BootstrapNodeEntryDB key_hexLike(String key_hex)
    {
        this.sql_where = this.sql_where + " and \"key_hex\" LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, key_hex));
        bind_where_count++;
        return this;
    }

    public BootstrapNodeEntryDB key_hexNotLike(String key_hex)
    {
        this.sql_where = this.sql_where + " and \"key_hex\" NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, key_hex));
        bind_where_count++;
        return this;
    }


    // ----------------- OrderBy funcs ------------------ //
    public BootstrapNodeEntryDB orderByIdAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"id\" ASC ";
        return this;
    }

    public BootstrapNodeEntryDB orderByIdDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"id\" DESC ";
        return this;
    }

    public BootstrapNodeEntryDB orderByNumAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"num\" ASC ";
        return this;
    }

    public BootstrapNodeEntryDB orderByNumDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"num\" DESC ";
        return this;
    }

    public BootstrapNodeEntryDB orderByUdp_nodeAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"udp_node\" ASC ";
        return this;
    }

    public BootstrapNodeEntryDB orderByUdp_nodeDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"udp_node\" DESC ";
        return this;
    }

    public BootstrapNodeEntryDB orderByIpAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"ip\" ASC ";
        return this;
    }

    public BootstrapNodeEntryDB orderByIpDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"ip\" DESC ";
        return this;
    }

    public BootstrapNodeEntryDB orderByPortAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"port\" ASC ";
        return this;
    }

    public BootstrapNodeEntryDB orderByPortDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"port\" DESC ";
        return this;
    }

    public BootstrapNodeEntryDB orderByKey_hexAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"key_hex\" ASC ";
        return this;
    }

    public BootstrapNodeEntryDB orderByKey_hexDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"key_hex\" DESC ";
        return this;
    }


    // =========== CUSTOM USER CODE START ===========


    @Override
    public String toString()
    {
        // return "" + num + ":" + ip + " port=" + port + " udp_node=" + udp_node + " key_hex=" + key_hex;
        // return "" + num + ":" + ip + " port=" + port + " udp_node="+  udp_node;
        return "" + num + ":" + ip + " port=" + port + " udp_node=" + udp_node + "\n";
    }

    public long get_port()
    {
        return port;
    }

    public String get_ip()
    {
        return ip;
    }

    static void insert_node_into_db_real(BootstrapNodeEntryDB n)
    {
        try
        {
            orma.insertIntoBootstrapNodeEntryDB(n);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void insert_default_udp_nodes_into_db()
    {
        BootstrapNodeEntryDB n;
        int num_ = 0;
        // @formatter:off
        n = BootstrapNodeEntryDB_(true, num_, "144.217.167.73",33445,"7E5668E0EE09E19F320AD47902419331FFEE147BB3606769CFBE921A2A2FD34C");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "tox.abilinski.com",33445,"10C00EB250C3233E343E2AEBA07115A5C28920E9C8D29492F6D00B29049EDC7E");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "198.199.98.108",33445,"BEF0CFB37AF874BD17B9A8F9FE64C75521DB95A37D33C5BDB00E9CF58659C04F");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "2604:a880:1:20::32f:1001",33445,"BEF0CFB37AF874BD17B9A8F9FE64C75521DB95A37D33C5BDB00E9CF58659C04F");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "tox.kurnevsky.net",33445,"82EF82BA33445A1F91A7DB27189ECFC0C013E06E3DA71F588ED692BED625EC23");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "2a03:b0c0:0:1010::4c:5001",33445,"82EF82BA33445A1F91A7DB27189ECFC0C013E06E3DA71F588ED692BED625EC23");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "205.185.115.131",53,"3091C6BEB2A993F1C6300C16549FABA67098FF3D62C6D253828B531470B53D68");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "tox2.abilinski.com",33445,"7A6098B590BDC73F9723FC59F82B3F9085A64D1B213AAF8E610FD351930D052D");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "2604:180:1:4ab::2",33445,"7A6098B590BDC73F9723FC59F82B3F9085A64D1B213AAF8E610FD351930D052D");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "46.101.197.175",33445,"CD133B521159541FB1D326DE9850F5E56A6C724B5B8E5EB5CD8D950408E95707");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "2a03:b0c0:3:d0::ac:5001",33445,"CD133B521159541FB1D326DE9850F5E56A6C724B5B8E5EB5CD8D950408E95707");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "tox1.mf-net.eu",33445,"B3E5FA80DC8EBD1149AD2AB35ED8B85BD546DEDE261CA593234C619249419506");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "2a01:4f8:c2c:89f7::1",33445,"B3E5FA80DC8EBD1149AD2AB35ED8B85BD546DEDE261CA593234C619249419506");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "tox2.mf-net.eu",33445,"70EA214FDE161E7432530605213F18F7427DC773E276B3E317A07531F548545F");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "2a01:4f8:c012:cb9::",33445,"70EA214FDE161E7432530605213F18F7427DC773E276B3E317A07531F548545F");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "195.201.7.101",33445,"B84E865125B4EC4C368CD047C72BCE447644A2DC31EF75BD2CDA345BFD310107");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "tox4.plastiras.org",33445,"836D1DA2BE12FE0E669334E437BE3FB02806F1528C2B2782113E0910C7711409");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "188.225.9.167",33445,"1911341A83E02503AB1FD6561BD64AF3A9D6C3F12B5FBB656976B2E678644A67");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "209:dead:ded:4991:49f3:b6c0:9869:3019",33445,"1911341A83E02503AB1FD6561BD64AF3A9D6C3F12B5FBB656976B2E678644A67");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "122.116.39.151",33445,"5716530A10D362867C8E87EE1CD5362A233BAFBBA4CF47FA73B7CAD368BD5E6E");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "2001:b011:8:2f22:1957:7f9d:e31f:96dd",33445,"5716530A10D362867C8E87EE1CD5362A233BAFBBA4CF47FA73B7CAD368BD5E6E");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "tox3.plastiras.org",33445,"4B031C96673B6FF123269FF18F2847E1909A8A04642BBECD0189AC8AEEADAF64");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "2a02:587:4c0d:3e8c:2edc:1bfb:ccb6:7e38",33445,"4B031C96673B6FF123269FF18F2847E1909A8A04642BBECD0189AC8AEEADAF64");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "104.225.141.59",43334,"933BA20B2E258B4C0D475B6DECE90C7E827FE83EFA9655414E7841251B19A72C");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "139.162.110.188",33445,"F76A11284547163889DDC89A7738CF271797BF5E5E220643E97AD3C7E7903D55");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "2400:8902::f03c:93ff:fe69:bf77",33445,"F76A11284547163889DDC89A7738CF271797BF5E5E220643E97AD3C7E7903D55");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "198.98.49.206",33445,"28DB44A3CEEE69146469855DFFE5F54DA567F5D65E03EFB1D38BBAEFF2553255");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "2605:6400:10:caa:1:be:a:7001",33445,"28DB44A3CEEE69146469855DFFE5F54DA567F5D65E03EFB1D38BBAEFF2553255");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "172.105.109.31",33445,"D46E97CF995DC1820B92B7D899E152A217D36ABE22730FEA4B6BF1BFC06C617C");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "2600:3c04::f03c:92ff:fe30:5df",33445,"D46E97CF995DC1820B92B7D899E152A217D36ABE22730FEA4B6BF1BFC06C617C");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "91.146.66.26",33445,"B5E7DAC610DBDE55F359C7F8690B294C8E4FCEC4385DE9525DBFA5523EAD9D53");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "tox01.ky0uraku.xyz",33445,"FD04EB03ABC5FC5266A93D37B4D6D6171C9931176DC68736629552D8EF0DE174");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "tox02.ky0uraku.xyz",33445,"D3D6D7C0C7009FC75406B0A49E475996C8C4F8BCE1E6FC5967DE427F8F600527");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "tox.plastiras.org",33445,"8E8B63299B3D520FB377FE5100E65E3322F7AE5B20A0ACED2981769FC5B43725");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "2605:6400:30:f7f4:c24:f413:e44d:a91f",33445,"8E8B63299B3D520FB377FE5100E65E3322F7AE5B20A0ACED2981769FC5B43725");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "kusoneko.moe",33445,"BE7ED53CD924813507BA711FD40386062E6DC6F790EFA122C78F7CDEEE4B6D1B");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "2001:19f0:b002:17a:5400:4ff:fecf:6eda",33445,"BE7ED53CD924813507BA711FD40386062E6DC6F790EFA122C78F7CDEEE4B6D1B");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "tox2.plastiras.org",33445,"B6626D386BE7E3ACA107B46F48A5C4D522D29281750D44A0CBA6A2721E79C951");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "2605:6400:30:ea2a:cef4:520c:f4ad:923b",33445,"B6626D386BE7E3ACA107B46F48A5C4D522D29281750D44A0CBA6A2721E79C951");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "172.104.215.182",33445,"DA2BD927E01CD05EBCC2574EBE5BEBB10FF59AE0B2105A7D1E2B40E49BB20239");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "2600:3c03::f03c:93ff:fe7f:6096",33445,"DA2BD927E01CD05EBCC2574EBE5BEBB10FF59AE0B2105A7D1E2B40E49BB20239");insert_node_into_db_real(n);num_++;
        // @formatter:on
    }

    public static void insert_default_tcprelay_nodes_into_db()
    {
        BootstrapNodeEntryDB n;
        int num_ = 0;
        // @formatter:off
        n = BootstrapNodeEntryDB_(false, num_, "144.217.167.73",33445,"7E5668E0EE09E19F320AD47902419331FFEE147BB3606769CFBE921A2A2FD34C");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "144.217.167.73",3389,"7E5668E0EE09E19F320AD47902419331FFEE147BB3606769CFBE921A2A2FD34C");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "tox.abilinski.com",33445,"10C00EB250C3233E343E2AEBA07115A5C28920E9C8D29492F6D00B29049EDC7E");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "198.199.98.108",33445,"BEF0CFB37AF874BD17B9A8F9FE64C75521DB95A37D33C5BDB00E9CF58659C04F");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "2604:a880:1:20::32f:1001",33445,"BEF0CFB37AF874BD17B9A8F9FE64C75521DB95A37D33C5BDB00E9CF58659C04F");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "198.199.98.108",3389,"BEF0CFB37AF874BD17B9A8F9FE64C75521DB95A37D33C5BDB00E9CF58659C04F");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "2604:a880:1:20::32f:1001",3389,"BEF0CFB37AF874BD17B9A8F9FE64C75521DB95A37D33C5BDB00E9CF58659C04F");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "205.185.115.131",443,"3091C6BEB2A993F1C6300C16549FABA67098FF3D62C6D253828B531470B53D68");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "205.185.115.131",3389,"3091C6BEB2A993F1C6300C16549FABA67098FF3D62C6D253828B531470B53D68");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "205.185.115.131",53,"3091C6BEB2A993F1C6300C16549FABA67098FF3D62C6D253828B531470B53D68");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "205.185.115.131",33445,"3091C6BEB2A993F1C6300C16549FABA67098FF3D62C6D253828B531470B53D68");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "tox2.abilinski.com",33445,"7A6098B590BDC73F9723FC59F82B3F9085A64D1B213AAF8E610FD351930D052D");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "2604:180:1:4ab::2",33445,"7A6098B590BDC73F9723FC59F82B3F9085A64D1B213AAF8E610FD351930D052D");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "46.101.197.175",3389,"CD133B521159541FB1D326DE9850F5E56A6C724B5B8E5EB5CD8D950408E95707");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "2a03:b0c0:3:d0::ac:5001",3389,"CD133B521159541FB1D326DE9850F5E56A6C724B5B8E5EB5CD8D950408E95707");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "46.101.197.175",33445,"CD133B521159541FB1D326DE9850F5E56A6C724B5B8E5EB5CD8D950408E95707");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "2a03:b0c0:3:d0::ac:5001",33445,"CD133B521159541FB1D326DE9850F5E56A6C724B5B8E5EB5CD8D950408E95707");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "tox1.mf-net.eu",33445,"B3E5FA80DC8EBD1149AD2AB35ED8B85BD546DEDE261CA593234C619249419506");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "2a01:4f8:c2c:89f7::1",33445,"B3E5FA80DC8EBD1149AD2AB35ED8B85BD546DEDE261CA593234C619249419506");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "tox1.mf-net.eu",3389,"B3E5FA80DC8EBD1149AD2AB35ED8B85BD546DEDE261CA593234C619249419506");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "2a01:4f8:c2c:89f7::1",3389,"B3E5FA80DC8EBD1149AD2AB35ED8B85BD546DEDE261CA593234C619249419506");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "tox2.mf-net.eu",33445,"70EA214FDE161E7432530605213F18F7427DC773E276B3E317A07531F548545F");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "2a01:4f8:c012:cb9::",33445,"70EA214FDE161E7432530605213F18F7427DC773E276B3E317A07531F548545F");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "tox2.mf-net.eu",3389,"70EA214FDE161E7432530605213F18F7427DC773E276B3E317A07531F548545F");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "2a01:4f8:c012:cb9::",3389,"70EA214FDE161E7432530605213F18F7427DC773E276B3E317A07531F548545F");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "195.201.7.101",33445,"B84E865125B4EC4C368CD047C72BCE447644A2DC31EF75BD2CDA345BFD310107");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "195.201.7.101",3389,"B84E865125B4EC4C368CD047C72BCE447644A2DC31EF75BD2CDA345BFD310107");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "tox4.plastiras.org",3389,"836D1DA2BE12FE0E669334E437BE3FB02806F1528C2B2782113E0910C7711409");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "tox4.plastiras.org",443,"836D1DA2BE12FE0E669334E437BE3FB02806F1528C2B2782113E0910C7711409");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "tox4.plastiras.org",33445,"836D1DA2BE12FE0E669334E437BE3FB02806F1528C2B2782113E0910C7711409");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "188.225.9.167",3389,"1911341A83E02503AB1FD6561BD64AF3A9D6C3F12B5FBB656976B2E678644A67");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "209:dead:ded:4991:49f3:b6c0:9869:3019",3389,"1911341A83E02503AB1FD6561BD64AF3A9D6C3F12B5FBB656976B2E678644A67");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "188.225.9.167",33445,"1911341A83E02503AB1FD6561BD64AF3A9D6C3F12B5FBB656976B2E678644A67");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "209:dead:ded:4991:49f3:b6c0:9869:3019",33445,"1911341A83E02503AB1FD6561BD64AF3A9D6C3F12B5FBB656976B2E678644A67");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "122.116.39.151",3389,"5716530A10D362867C8E87EE1CD5362A233BAFBBA4CF47FA73B7CAD368BD5E6E");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "2001:b011:8:2f22:1957:7f9d:e31f:96dd",3389,"5716530A10D362867C8E87EE1CD5362A233BAFBBA4CF47FA73B7CAD368BD5E6E");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "122.116.39.151",33445,"5716530A10D362867C8E87EE1CD5362A233BAFBBA4CF47FA73B7CAD368BD5E6E");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "2001:b011:8:2f22:1957:7f9d:e31f:96dd",33445,"5716530A10D362867C8E87EE1CD5362A233BAFBBA4CF47FA73B7CAD368BD5E6E");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "tox3.plastiras.org",33445,"4B031C96673B6FF123269FF18F2847E1909A8A04642BBECD0189AC8AEEADAF64");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "2a02:587:4c0d:3e8c:2edc:1bfb:ccb6:7e38",33445,"4B031C96673B6FF123269FF18F2847E1909A8A04642BBECD0189AC8AEEADAF64");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "tox3.plastiras.org",3389,"4B031C96673B6FF123269FF18F2847E1909A8A04642BBECD0189AC8AEEADAF64");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "2a02:587:4c0d:3e8c:2edc:1bfb:ccb6:7e38",3389,"4B031C96673B6FF123269FF18F2847E1909A8A04642BBECD0189AC8AEEADAF64");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "139.162.110.188",443,"F76A11284547163889DDC89A7738CF271797BF5E5E220643E97AD3C7E7903D55");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "2400:8902::f03c:93ff:fe69:bf77",443,"F76A11284547163889DDC89A7738CF271797BF5E5E220643E97AD3C7E7903D55");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "139.162.110.188",33445,"F76A11284547163889DDC89A7738CF271797BF5E5E220643E97AD3C7E7903D55");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "2400:8902::f03c:93ff:fe69:bf77",33445,"F76A11284547163889DDC89A7738CF271797BF5E5E220643E97AD3C7E7903D55");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "139.162.110.188",3389,"F76A11284547163889DDC89A7738CF271797BF5E5E220643E97AD3C7E7903D55");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "2400:8902::f03c:93ff:fe69:bf77",3389,"F76A11284547163889DDC89A7738CF271797BF5E5E220643E97AD3C7E7903D55");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "198.98.49.206",33445,"28DB44A3CEEE69146469855DFFE5F54DA567F5D65E03EFB1D38BBAEFF2553255");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "2605:6400:10:caa:1:be:a:7001",33445,"28DB44A3CEEE69146469855DFFE5F54DA567F5D65E03EFB1D38BBAEFF2553255");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "172.105.109.31",33445,"D46E97CF995DC1820B92B7D899E152A217D36ABE22730FEA4B6BF1BFC06C617C");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "2600:3c04::f03c:92ff:fe30:5df",33445,"D46E97CF995DC1820B92B7D899E152A217D36ABE22730FEA4B6BF1BFC06C617C");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "tox01.ky0uraku.xyz",33445,"FD04EB03ABC5FC5266A93D37B4D6D6171C9931176DC68736629552D8EF0DE174");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "tox02.ky0uraku.xyz",33445,"D3D6D7C0C7009FC75406B0A49E475996C8C4F8BCE1E6FC5967DE427F8F600527");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "tox.plastiras.org",33445,"8E8B63299B3D520FB377FE5100E65E3322F7AE5B20A0ACED2981769FC5B43725");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "2605:6400:30:f7f4:c24:f413:e44d:a91f",33445,"8E8B63299B3D520FB377FE5100E65E3322F7AE5B20A0ACED2981769FC5B43725");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "tox.plastiras.org",443,"8E8B63299B3D520FB377FE5100E65E3322F7AE5B20A0ACED2981769FC5B43725");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "2605:6400:30:f7f4:c24:f413:e44d:a91f",443,"8E8B63299B3D520FB377FE5100E65E3322F7AE5B20A0ACED2981769FC5B43725");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "kusoneko.moe",33445,"BE7ED53CD924813507BA711FD40386062E6DC6F790EFA122C78F7CDEEE4B6D1B");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "2001:19f0:b002:17a:5400:4ff:fecf:6eda",33445,"BE7ED53CD924813507BA711FD40386062E6DC6F790EFA122C78F7CDEEE4B6D1B");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "tox2.plastiras.org",33445,"B6626D386BE7E3ACA107B46F48A5C4D522D29281750D44A0CBA6A2721E79C951");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "2605:6400:30:ea2a:cef4:520c:f4ad:923b",33445,"B6626D386BE7E3ACA107B46F48A5C4D522D29281750D44A0CBA6A2721E79C951");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "tox2.plastiras.org",3389,"B6626D386BE7E3ACA107B46F48A5C4D522D29281750D44A0CBA6A2721E79C951");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "2605:6400:30:ea2a:cef4:520c:f4ad:923b",3389,"B6626D386BE7E3ACA107B46F48A5C4D522D29281750D44A0CBA6A2721E79C951");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "172.104.215.182",33445,"DA2BD927E01CD05EBCC2574EBE5BEBB10FF59AE0B2105A7D1E2B40E49BB20239");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "2600:3c03::f03c:93ff:fe7f:6096",33445,"DA2BD927E01CD05EBCC2574EBE5BEBB10FF59AE0B2105A7D1E2B40E49BB20239");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "172.104.215.182",3389,"DA2BD927E01CD05EBCC2574EBE5BEBB10FF59AE0B2105A7D1E2B40E49BB20239");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "2600:3c03::f03c:93ff:fe7f:6096",3389,"DA2BD927E01CD05EBCC2574EBE5BEBB10FF59AE0B2105A7D1E2B40E49BB20239");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "172.104.215.182",443,"DA2BD927E01CD05EBCC2574EBE5BEBB10FF59AE0B2105A7D1E2B40E49BB20239");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "2600:3c03::f03c:93ff:fe7f:6096",443,"DA2BD927E01CD05EBCC2574EBE5BEBB10FF59AE0B2105A7D1E2B40E49BB20239");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "5.19.249.240",38296,"DA98A4C0CD7473A133E115FEA2EBDAEEA2EF4F79FD69325FC070DA4DE4BA3238");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "5.19.249.240",3389,"DA98A4C0CD7473A133E115FEA2EBDAEEA2EF4F79FD69325FC070DA4DE4BA3238");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "2607:f130:0:f8::4c85:a645",3389,"8AFE1FC6426E5B77AB80318ED64F5F76341695B9FB47AB8AC9537BF5EE9E9D29");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "2607:f130:0:f8::4c85:a645",33445,"8AFE1FC6426E5B77AB80318ED64F5F76341695B9FB47AB8AC9537BF5EE9E9D29");insert_node_into_db_real(n);num_++;
        // @formatter:on
    }

    public static BootstrapNodeEntryDB BootstrapNodeEntryDB_(boolean udp_node_, int num_, String ip_, long port_, String key_hex_)
    {
        BootstrapNodeEntryDB n = new BootstrapNodeEntryDB();
        n.num = num_;
        n.udp_node = udp_node_;
        n.ip = ip_;
        n.port = port_;
        n.key_hex = key_hex_;

        return n;
    }

    public static String dns_lookup_via_tor(String host_or_ip)
    {
        try
        {
            if (host_or_ip.equals("127.0.0.1"))
            {
                Log.i(TAG, "dns_lookup_via_tor:TorResolve:" + host_or_ip + " == 127.0.0.1");
                return "127.0.0.1";
            }
            else if (validate_ipv4(host_or_ip))
            {
                Log.i(TAG, "dns_lookup_via_tor:TorResolve:" + host_or_ip + " is already an IPv4 address");
                return host_or_ip;
            }

            // TODO: TorResolve can NOT resolve IPv6 address like its written now
            String IP_address = TorResolve(host_or_ip);
            Log.i(TAG, "dns_lookup_via_tor:TorResolve:" + host_or_ip + " -> " + IP_address);

            if ((IP_address == null) || (IP_address.equals("")))
            {
                // if there is some error, use localhost -> which kind of disables this host
                Log.i(TAG, "dns_lookup_via_tor:EE2:IP_address=" + IP_address);
                return "127.0.0.1";
            }
            else
            {
                return IP_address;
            }
        }
        catch (Exception e)
        {
            // if there is some error, use localhost -> which kind of disables this host
            e.printStackTrace();
            Log.i(TAG, "dns_lookup_via_tor:EE1:" + e.getMessage());
            return "127.0.0.1";
        }
    }

    public static void update_nodelist_from_internet_https_dummy_XXXX()
    {
        // this should be using TOR proxy, if tor is enabled in options!
        // TODO: TorResolve can NOT resolve IPv6 address like its written now
        String IP_address = TorResolve(TOX_NODELIST_HOST);
        Log.i(TAG, "update_nodelist_from_internet:TorResolve:" + TOX_NODELIST_HOST + " -> " + IP_address);


        try
        {
            Socket s = TorSocket(TOX_NODELIST_HOST, 443);
            DataInputStream is = new DataInputStream(s.getInputStream());
            PrintStream out = new java.io.PrintStream(s.getOutputStream());

            //Construct an HTTP request
            out.print("GET  /" + "json" + " HTTP/1.0\r\n");
            out.print("Host: " + TOX_NODELIST_HOST + ":" + "443" + "\r\n");
            out.print("Accept: */*\r\n");
            out.print("Connection: Keep-Alive\r\n");
            out.print("Pragma: no-cache\r\n");
            out.print("\r\n");
            out.flush();

            // this is from Java Examples In a Nutshell
            final InputStreamReader from_server = new InputStreamReader(is);
            char[] buffer = new char[1024];
            int chars_read;

            StringBuilder response_text = new StringBuilder();

            // read until stream closes
            while ((chars_read = from_server.read(buffer)) != -1)
            {
                // loop through array of chars
                // change \n to local platform terminator
                // this is a nieve implementation
                for (int j = 0; j < chars_read; j++)
                {
                    if (buffer[j] == '\n')
                    {
                        // System.out.println();
                        response_text.append("\n");
                    }
                    else
                    {
                        // System.out.print(buffer[j]);
                        response_text.append(buffer[j]);
                    }
                }
                // System.out.flush();
            }
            s.close();

            Log.i(TAG, "" + response_text);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void get_tcprelay_nodelist_from_db()
    {
        tcprelay_node_list.clear();

        long tcprelay_nodelist_count = 0;
        try
        {
            tcprelay_nodelist_count = orma.selectFromBootstrapNodeEntryDB().
                    udp_nodeEq(false).count();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        Log.i(TAG, "get_tcprelay_nodelist_from_db:tcprelay_nodelist_count=" + tcprelay_nodelist_count);

        if (tcprelay_nodelist_count == 0)
        {
            Log.i(TAG, "get_tcprelay_nodelist_from_db:insert_default_tcprelay_nodes_into_db");
            insert_default_tcprelay_nodes_into_db();
        }

        // fill tcprelay_node_list with values from DB -----------------
        try
        {
            tcprelay_node_list.addAll(orma.selectFromBootstrapNodeEntryDB().udp_nodeEq(false).orderByNumAsc().toList());
            Log.i(TAG, "get_tcprelay_nodelist_from_db:tcprelay_node_list.addAll " + tcprelay_node_list);

            if (PREF__orbot_enabled)
            {
                Iterator i = bootstrap_node_list.iterator();
                com.zoffcc.applications.sorm.BootstrapNodeEntryDB e2;
                while (i.hasNext())
                {
                    e2 = (com.zoffcc.applications.sorm.BootstrapNodeEntryDB) i.next();
                    e2.ip = dns_lookup_via_tor(e2.ip);

                }
            }
        }
        catch (Exception e)
        {
        }
        // fill tcprelay_node_list with values from DB -----------------
    }

    public static void get_udp_nodelist_from_db()
    {
        bootstrap_node_list.clear();

        long udp_nodelist_count = 0;
        try
        {
            udp_nodelist_count = orma.selectFromBootstrapNodeEntryDB().
                    udp_nodeEq(true).count();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        Log.i(TAG, "get_udp_nodelist_from_db:udp_nodelist_count=" + udp_nodelist_count);

        if (udp_nodelist_count == 0)
        {
            Log.i(TAG, "get_udp_nodelist_from_db:insert_default_udp_nodes_into_db");
            insert_default_udp_nodes_into_db();
        }

        // fill bootstrap_node_list with values from DB -----------------
        try
        {
            bootstrap_node_list.addAll(orma.selectFromBootstrapNodeEntryDB().udp_nodeEq(true).orderByNumAsc().toList());
            Log.i(TAG, "get_udp_nodelist_from_db:bootstrap_node_list.addAll");

            if (PREF__orbot_enabled)
            {
                Iterator i = bootstrap_node_list.iterator();
                com.zoffcc.applications.sorm.BootstrapNodeEntryDB e2;
                while (i.hasNext())
                {
                    e2 = (com.zoffcc.applications.sorm.BootstrapNodeEntryDB) i.next();
                    e2.ip = dns_lookup_via_tor(e2.ip);
                }
            }
        }
        catch (Exception e)
        {
        }
        // fill bootstrap_node_list with values from DB -----------------
    }
}

    // =========== CUSTOM USER CODE END ===========

}

