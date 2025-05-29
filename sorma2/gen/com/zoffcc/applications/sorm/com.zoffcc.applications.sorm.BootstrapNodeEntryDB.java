/* SPDX-License-Identifier: GPL-3.0-or-later
 * [sorma2], Java part of sorma2
 * Copyright (C) 2024 Zoff <zoff@zoff.cc>
 */

package com.zoffcc.applications.sorm;

import com.zoffcc.applications.sorm.Log;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import static com.zoffcc.applications.sorm.OrmaDatabase.*;


@Table
public class com.zoffcc.applications.sorm.BootstrapNodeEntryDB
{
    private static final String TAG = "DB.com.zoffcc.applic";
    @PrimaryKey(autoincrement = true, auto = true)
    public long id;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public long num;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public boolean udp_node;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public String ip;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public long port;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public String key_hex;

    static com.zoffcc.applications.sorm.BootstrapNodeEntryDB deep_copy(com.zoffcc.applications.sorm.BootstrapNodeEntryDB in)
    {
        com.zoffcc.applications.sorm.BootstrapNodeEntryDB out = new com.zoffcc.applications.sorm.BootstrapNodeEntryDB();
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

    public List<com.zoffcc.applications.sorm.BootstrapNodeEntryDB> toList()
    {
        List<com.zoffcc.applications.sorm.BootstrapNodeEntryDB> list = new ArrayList<>();
        orma_global_sqltolist_lock.lock();
        PreparedStatement statement = null;
        try
        {
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
                com.zoffcc.applications.sorm.BootstrapNodeEntryDB out = new com.zoffcc.applications.sorm.BootstrapNodeEntryDB();
                out.id = rs.getLong("id");
                out.num = rs.getLong("num");
                out.udp_node = rs.getBoolean("udp_node");
                out.ip = rs.getString("ip");
                out.port = rs.getLong("port");
                out.key_hex = rs.getString("key_hex");

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

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB get(int i)
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

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB limit(int rowcount)
    {
        this.sql_limit = " limit " + rowcount + " ";
        return this;
    }

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB limit(int rowcount, int offset)
    {
        this.sql_limit = " limit " + offset + " , " + rowcount;
        return this;
    }

    // ----------------------------------- //
    // ----------------------------------- //
    // ----------------------------------- //


    // ----------------- Set funcs ---------------------- //
    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB id(long id)
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

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB num(long num)
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

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB udp_node(boolean udp_node)
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

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB ip(String ip)
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

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB port(long port)
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

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB key_hex(String key_hex)
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
    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB idEq(long id)
    {
        this.sql_where = this.sql_where + " and \"id\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB idNotEq(long id)
    {
        this.sql_where = this.sql_where + " and \"id\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB idLt(long id)
    {
        this.sql_where = this.sql_where + " and \"id\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB idLe(long id)
    {
        this.sql_where = this.sql_where + " and \"id\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB idGt(long id)
    {
        this.sql_where = this.sql_where + " and \"id\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB idGe(long id)
    {
        this.sql_where = this.sql_where + " and \"id\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB idBetween(long id1, long id2)
    {
        this.sql_where = this.sql_where + " and \"id\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and id<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id2));
        bind_where_count++;
        return this;
    }

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB idIsNull()
    {
        this.sql_where = this.sql_where + " and \"id\" IS NULL ";
        return this;
    }

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB idIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"id\" IS NOT NULL ";
        return this;
    }

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB numEq(long num)
    {
        this.sql_where = this.sql_where + " and \"num\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, num));
        bind_where_count++;
        return this;
    }

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB numNotEq(long num)
    {
        this.sql_where = this.sql_where + " and \"num\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, num));
        bind_where_count++;
        return this;
    }

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB numLt(long num)
    {
        this.sql_where = this.sql_where + " and \"num\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, num));
        bind_where_count++;
        return this;
    }

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB numLe(long num)
    {
        this.sql_where = this.sql_where + " and \"num\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, num));
        bind_where_count++;
        return this;
    }

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB numGt(long num)
    {
        this.sql_where = this.sql_where + " and \"num\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, num));
        bind_where_count++;
        return this;
    }

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB numGe(long num)
    {
        this.sql_where = this.sql_where + " and \"num\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, num));
        bind_where_count++;
        return this;
    }

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB numBetween(long num1, long num2)
    {
        this.sql_where = this.sql_where + " and \"num\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and num<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, num1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, num2));
        bind_where_count++;
        return this;
    }

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB numIsNull()
    {
        this.sql_where = this.sql_where + " and \"num\" IS NULL ";
        return this;
    }

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB numIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"num\" IS NOT NULL ";
        return this;
    }

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB udp_nodeEq(boolean udp_node)
    {
        this.sql_where = this.sql_where + " and \"udp_node\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, udp_node));
        bind_where_count++;
        return this;
    }

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB udp_nodeNotEq(boolean udp_node)
    {
        this.sql_where = this.sql_where + " and \"udp_node\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, udp_node));
        bind_where_count++;
        return this;
    }

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB udp_nodeIsNull()
    {
        this.sql_where = this.sql_where + " and \"udp_node\" IS NULL ";
        return this;
    }

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB udp_nodeIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"udp_node\" IS NOT NULL ";
        return this;
    }

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB ipEq(String ip)
    {
        this.sql_where = this.sql_where + " and \"ip\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, ip));
        bind_where_count++;
        return this;
    }

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB ipNotEq(String ip)
    {
        this.sql_where = this.sql_where + " and \"ip\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, ip));
        bind_where_count++;
        return this;
    }

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB ipIsNull()
    {
        this.sql_where = this.sql_where + " and \"ip\" IS NULL ";
        return this;
    }

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB ipIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"ip\" IS NOT NULL ";
        return this;
    }

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB ipLike(String ip)
    {
        this.sql_where = this.sql_where + " and \"ip\" LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, ip));
        bind_where_count++;
        return this;
    }

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB ipNotLike(String ip)
    {
        this.sql_where = this.sql_where + " and \"ip\" NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, ip));
        bind_where_count++;
        return this;
    }

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB portEq(long port)
    {
        this.sql_where = this.sql_where + " and \"port\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, port));
        bind_where_count++;
        return this;
    }

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB portNotEq(long port)
    {
        this.sql_where = this.sql_where + " and \"port\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, port));
        bind_where_count++;
        return this;
    }

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB portLt(long port)
    {
        this.sql_where = this.sql_where + " and \"port\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, port));
        bind_where_count++;
        return this;
    }

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB portLe(long port)
    {
        this.sql_where = this.sql_where + " and \"port\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, port));
        bind_where_count++;
        return this;
    }

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB portGt(long port)
    {
        this.sql_where = this.sql_where + " and \"port\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, port));
        bind_where_count++;
        return this;
    }

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB portGe(long port)
    {
        this.sql_where = this.sql_where + " and \"port\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, port));
        bind_where_count++;
        return this;
    }

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB portBetween(long port1, long port2)
    {
        this.sql_where = this.sql_where + " and \"port\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and port<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, port1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, port2));
        bind_where_count++;
        return this;
    }

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB portIsNull()
    {
        this.sql_where = this.sql_where + " and \"port\" IS NULL ";
        return this;
    }

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB portIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"port\" IS NOT NULL ";
        return this;
    }

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB key_hexEq(String key_hex)
    {
        this.sql_where = this.sql_where + " and \"key_hex\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, key_hex));
        bind_where_count++;
        return this;
    }

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB key_hexNotEq(String key_hex)
    {
        this.sql_where = this.sql_where + " and \"key_hex\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, key_hex));
        bind_where_count++;
        return this;
    }

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB key_hexIsNull()
    {
        this.sql_where = this.sql_where + " and \"key_hex\" IS NULL ";
        return this;
    }

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB key_hexIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"key_hex\" IS NOT NULL ";
        return this;
    }

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB key_hexLike(String key_hex)
    {
        this.sql_where = this.sql_where + " and \"key_hex\" LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, key_hex));
        bind_where_count++;
        return this;
    }

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB key_hexNotLike(String key_hex)
    {
        this.sql_where = this.sql_where + " and \"key_hex\" NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, key_hex));
        bind_where_count++;
        return this;
    }


    // ----------------- OrderBy funcs ------------------ //
    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB orderByIdAsc()
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

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB orderByIdDesc()
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

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB orderByNumAsc()
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

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB orderByNumDesc()
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

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB orderByUdp_nodeAsc()
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

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB orderByUdp_nodeDesc()
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

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB orderByIpAsc()
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

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB orderByIpDesc()
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

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB orderByPortAsc()
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

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB orderByPortDesc()
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

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB orderByKey_hexAsc()
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

    public com.zoffcc.applications.sorm.BootstrapNodeEntryDB orderByKey_hexDesc()
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



}

