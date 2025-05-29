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
public class GroupPeerDB
{
    private static final String TAG = "DB.GroupPeerDB";
    @PrimaryKey(autoincrement = true, auto = true)
    public long id;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public String group_identifier;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public String tox_group_peer_pubkey;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public String peer_name;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public long last_update_timestamp;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public long first_join_timestamp;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public int Tox_Group_Role;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public boolean notification_silent;

    public static GroupPeerDB deep_copy(GroupPeerDB in)
    {
        GroupPeerDB out = new GroupPeerDB();
        out.id = in.id;
        out.group_identifier = in.group_identifier;
        out.tox_group_peer_pubkey = in.tox_group_peer_pubkey;
        out.peer_name = in.peer_name;
        out.last_update_timestamp = in.last_update_timestamp;
        out.first_join_timestamp = in.first_join_timestamp;
        out.Tox_Group_Role = in.Tox_Group_Role;
        out.notification_silent = in.notification_silent;

        return out;
    }

    @Override
    public String toString()
    {
        return "id=" + id + ", group_identifier=" + group_identifier + ", tox_group_peer_pubkey=" + tox_group_peer_pubkey + ", peer_name=" + peer_name + ", last_update_timestamp=" + last_update_timestamp + ", first_join_timestamp=" + first_join_timestamp + ", Tox_Group_Role=" + Tox_Group_Role + ", notification_silent=" + notification_silent;
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

    public List<GroupPeerDB> toList()
    {
        List<GroupPeerDB> list = new ArrayList<>();
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
                GroupPeerDB out = new GroupPeerDB();
                out.id = rs.getLong("id");
                out.group_identifier = rs.getString("group_identifier");
                out.tox_group_peer_pubkey = rs.getString("tox_group_peer_pubkey");
                out.peer_name = rs.getString("peer_name");
                out.last_update_timestamp = rs.getLong("last_update_timestamp");
                out.first_join_timestamp = rs.getLong("first_join_timestamp");
                out.Tox_Group_Role = rs.getInt("Tox_Group_Role");
                out.notification_silent = rs.getBoolean("notification_silent");

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
                    + "\"group_identifier\""
                    + ",\"tox_group_peer_pubkey\""
                    + ",\"peer_name\""
                    + ",\"last_update_timestamp\""
                    + ",\"first_join_timestamp\""
                    + ",\"Tox_Group_Role\""
                    + ",\"notification_silent\""
                    + ")" +
                    "values" +
                    "("
                    + "?1"
                    + ",?2"
                    + ",?3"
                    + ",?4"
                    + ",?5"
                    + ",?6"
                    + ",?7"
                    + ")";

            insert_pstmt = sqldb.prepareStatement(insert_pstmt_sql);
            insert_pstmt.clearParameters();

            insert_pstmt.setString(1, this.group_identifier);
            insert_pstmt.setString(2, this.tox_group_peer_pubkey);
            insert_pstmt.setString(3, this.peer_name);
            insert_pstmt.setLong(4, this.last_update_timestamp);
            insert_pstmt.setLong(5, this.first_join_timestamp);
            insert_pstmt.setInt(6, this.Tox_Group_Role);
            insert_pstmt.setBoolean(7, this.notification_silent);
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

    public GroupPeerDB get(int i)
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

    public GroupPeerDB limit(int rowcount)
    {
        this.sql_limit = " limit " + rowcount + " ";
        return this;
    }

    public GroupPeerDB limit(int rowcount, int offset)
    {
        this.sql_limit = " limit " + offset + " , " + rowcount;
        return this;
    }

    // ----------------------------------- //
    // ----------------------------------- //
    // ----------------------------------- //


    // ----------------- Set funcs ---------------------- //
    public GroupPeerDB id(long id)
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

    public GroupPeerDB group_identifier(String group_identifier)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"group_identifier\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, group_identifier));
        bind_set_count++;
        return this;
    }

    public GroupPeerDB tox_group_peer_pubkey(String tox_group_peer_pubkey)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"tox_group_peer_pubkey\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_group_peer_pubkey));
        bind_set_count++;
        return this;
    }

    public GroupPeerDB peer_name(String peer_name)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"peer_name\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, peer_name));
        bind_set_count++;
        return this;
    }

    public GroupPeerDB last_update_timestamp(long last_update_timestamp)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"last_update_timestamp\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, last_update_timestamp));
        bind_set_count++;
        return this;
    }

    public GroupPeerDB first_join_timestamp(long first_join_timestamp)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"first_join_timestamp\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, first_join_timestamp));
        bind_set_count++;
        return this;
    }

    public GroupPeerDB Tox_Group_Role(int Tox_Group_Role)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"Tox_Group_Role\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, Tox_Group_Role));
        bind_set_count++;
        return this;
    }

    public GroupPeerDB notification_silent(boolean notification_silent)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"notification_silent\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, notification_silent));
        bind_set_count++;
        return this;
    }


    // ----------------- Eq/Gt/Lt funcs ----------------- //
    public GroupPeerDB idEq(long id)
    {
        this.sql_where = this.sql_where + " and \"id\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public GroupPeerDB idNotEq(long id)
    {
        this.sql_where = this.sql_where + " and \"id\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public GroupPeerDB idLt(long id)
    {
        this.sql_where = this.sql_where + " and \"id\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public GroupPeerDB idLe(long id)
    {
        this.sql_where = this.sql_where + " and \"id\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public GroupPeerDB idGt(long id)
    {
        this.sql_where = this.sql_where + " and \"id\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public GroupPeerDB idGe(long id)
    {
        this.sql_where = this.sql_where + " and \"id\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public GroupPeerDB idBetween(long id1, long id2)
    {
        this.sql_where = this.sql_where + " and \"id\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and id<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id2));
        bind_where_count++;
        return this;
    }

    public GroupPeerDB idIsNull()
    {
        this.sql_where = this.sql_where + " and \"id\" IS NULL ";
        return this;
    }

    public GroupPeerDB idIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"id\" IS NOT NULL ";
        return this;
    }

    public GroupPeerDB group_identifierEq(String group_identifier)
    {
        this.sql_where = this.sql_where + " and \"group_identifier\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, group_identifier));
        bind_where_count++;
        return this;
    }

    public GroupPeerDB group_identifierNotEq(String group_identifier)
    {
        this.sql_where = this.sql_where + " and \"group_identifier\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, group_identifier));
        bind_where_count++;
        return this;
    }

    public GroupPeerDB group_identifierIsNull()
    {
        this.sql_where = this.sql_where + " and \"group_identifier\" IS NULL ";
        return this;
    }

    public GroupPeerDB group_identifierIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"group_identifier\" IS NOT NULL ";
        return this;
    }

    public GroupPeerDB group_identifierLike(String group_identifier)
    {
        this.sql_where = this.sql_where + " and \"group_identifier\" LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, group_identifier));
        bind_where_count++;
        return this;
    }

    public GroupPeerDB group_identifierNotLike(String group_identifier)
    {
        this.sql_where = this.sql_where + " and \"group_identifier\" NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, group_identifier));
        bind_where_count++;
        return this;
    }

    public GroupPeerDB tox_group_peer_pubkeyEq(String tox_group_peer_pubkey)
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_pubkey\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_group_peer_pubkey));
        bind_where_count++;
        return this;
    }

    public GroupPeerDB tox_group_peer_pubkeyNotEq(String tox_group_peer_pubkey)
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_pubkey\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_group_peer_pubkey));
        bind_where_count++;
        return this;
    }

    public GroupPeerDB tox_group_peer_pubkeyIsNull()
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_pubkey\" IS NULL ";
        return this;
    }

    public GroupPeerDB tox_group_peer_pubkeyIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_pubkey\" IS NOT NULL ";
        return this;
    }

    public GroupPeerDB tox_group_peer_pubkeyLike(String tox_group_peer_pubkey)
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_pubkey\" LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_group_peer_pubkey));
        bind_where_count++;
        return this;
    }

    public GroupPeerDB tox_group_peer_pubkeyNotLike(String tox_group_peer_pubkey)
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_pubkey\" NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_group_peer_pubkey));
        bind_where_count++;
        return this;
    }

    public GroupPeerDB peer_nameEq(String peer_name)
    {
        this.sql_where = this.sql_where + " and \"peer_name\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, peer_name));
        bind_where_count++;
        return this;
    }

    public GroupPeerDB peer_nameNotEq(String peer_name)
    {
        this.sql_where = this.sql_where + " and \"peer_name\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, peer_name));
        bind_where_count++;
        return this;
    }

    public GroupPeerDB peer_nameIsNull()
    {
        this.sql_where = this.sql_where + " and \"peer_name\" IS NULL ";
        return this;
    }

    public GroupPeerDB peer_nameIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"peer_name\" IS NOT NULL ";
        return this;
    }

    public GroupPeerDB peer_nameLike(String peer_name)
    {
        this.sql_where = this.sql_where + " and \"peer_name\" LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, peer_name));
        bind_where_count++;
        return this;
    }

    public GroupPeerDB peer_nameNotLike(String peer_name)
    {
        this.sql_where = this.sql_where + " and \"peer_name\" NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, peer_name));
        bind_where_count++;
        return this;
    }

    public GroupPeerDB last_update_timestampEq(long last_update_timestamp)
    {
        this.sql_where = this.sql_where + " and \"last_update_timestamp\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, last_update_timestamp));
        bind_where_count++;
        return this;
    }

    public GroupPeerDB last_update_timestampNotEq(long last_update_timestamp)
    {
        this.sql_where = this.sql_where + " and \"last_update_timestamp\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, last_update_timestamp));
        bind_where_count++;
        return this;
    }

    public GroupPeerDB last_update_timestampLt(long last_update_timestamp)
    {
        this.sql_where = this.sql_where + " and \"last_update_timestamp\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, last_update_timestamp));
        bind_where_count++;
        return this;
    }

    public GroupPeerDB last_update_timestampLe(long last_update_timestamp)
    {
        this.sql_where = this.sql_where + " and \"last_update_timestamp\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, last_update_timestamp));
        bind_where_count++;
        return this;
    }

    public GroupPeerDB last_update_timestampGt(long last_update_timestamp)
    {
        this.sql_where = this.sql_where + " and \"last_update_timestamp\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, last_update_timestamp));
        bind_where_count++;
        return this;
    }

    public GroupPeerDB last_update_timestampGe(long last_update_timestamp)
    {
        this.sql_where = this.sql_where + " and \"last_update_timestamp\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, last_update_timestamp));
        bind_where_count++;
        return this;
    }

    public GroupPeerDB last_update_timestampBetween(long last_update_timestamp1, long last_update_timestamp2)
    {
        this.sql_where = this.sql_where + " and \"last_update_timestamp\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and last_update_timestamp<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, last_update_timestamp1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, last_update_timestamp2));
        bind_where_count++;
        return this;
    }

    public GroupPeerDB last_update_timestampIsNull()
    {
        this.sql_where = this.sql_where + " and \"last_update_timestamp\" IS NULL ";
        return this;
    }

    public GroupPeerDB last_update_timestampIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"last_update_timestamp\" IS NOT NULL ";
        return this;
    }

    public GroupPeerDB first_join_timestampEq(long first_join_timestamp)
    {
        this.sql_where = this.sql_where + " and \"first_join_timestamp\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, first_join_timestamp));
        bind_where_count++;
        return this;
    }

    public GroupPeerDB first_join_timestampNotEq(long first_join_timestamp)
    {
        this.sql_where = this.sql_where + " and \"first_join_timestamp\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, first_join_timestamp));
        bind_where_count++;
        return this;
    }

    public GroupPeerDB first_join_timestampLt(long first_join_timestamp)
    {
        this.sql_where = this.sql_where + " and \"first_join_timestamp\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, first_join_timestamp));
        bind_where_count++;
        return this;
    }

    public GroupPeerDB first_join_timestampLe(long first_join_timestamp)
    {
        this.sql_where = this.sql_where + " and \"first_join_timestamp\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, first_join_timestamp));
        bind_where_count++;
        return this;
    }

    public GroupPeerDB first_join_timestampGt(long first_join_timestamp)
    {
        this.sql_where = this.sql_where + " and \"first_join_timestamp\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, first_join_timestamp));
        bind_where_count++;
        return this;
    }

    public GroupPeerDB first_join_timestampGe(long first_join_timestamp)
    {
        this.sql_where = this.sql_where + " and \"first_join_timestamp\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, first_join_timestamp));
        bind_where_count++;
        return this;
    }

    public GroupPeerDB first_join_timestampBetween(long first_join_timestamp1, long first_join_timestamp2)
    {
        this.sql_where = this.sql_where + " and \"first_join_timestamp\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and first_join_timestamp<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, first_join_timestamp1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, first_join_timestamp2));
        bind_where_count++;
        return this;
    }

    public GroupPeerDB first_join_timestampIsNull()
    {
        this.sql_where = this.sql_where + " and \"first_join_timestamp\" IS NULL ";
        return this;
    }

    public GroupPeerDB first_join_timestampIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"first_join_timestamp\" IS NOT NULL ";
        return this;
    }

    public GroupPeerDB Tox_Group_RoleEq(int Tox_Group_Role)
    {
        this.sql_where = this.sql_where + " and \"Tox_Group_Role\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, Tox_Group_Role));
        bind_where_count++;
        return this;
    }

    public GroupPeerDB Tox_Group_RoleNotEq(int Tox_Group_Role)
    {
        this.sql_where = this.sql_where + " and \"Tox_Group_Role\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, Tox_Group_Role));
        bind_where_count++;
        return this;
    }

    public GroupPeerDB Tox_Group_RoleLt(int Tox_Group_Role)
    {
        this.sql_where = this.sql_where + " and \"Tox_Group_Role\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, Tox_Group_Role));
        bind_where_count++;
        return this;
    }

    public GroupPeerDB Tox_Group_RoleLe(int Tox_Group_Role)
    {
        this.sql_where = this.sql_where + " and \"Tox_Group_Role\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, Tox_Group_Role));
        bind_where_count++;
        return this;
    }

    public GroupPeerDB Tox_Group_RoleGt(int Tox_Group_Role)
    {
        this.sql_where = this.sql_where + " and \"Tox_Group_Role\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, Tox_Group_Role));
        bind_where_count++;
        return this;
    }

    public GroupPeerDB Tox_Group_RoleGe(int Tox_Group_Role)
    {
        this.sql_where = this.sql_where + " and \"Tox_Group_Role\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, Tox_Group_Role));
        bind_where_count++;
        return this;
    }

    public GroupPeerDB Tox_Group_RoleBetween(int Tox_Group_Role1, int Tox_Group_Role2)
    {
        this.sql_where = this.sql_where + " and \"Tox_Group_Role\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and Tox_Group_Role<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, Tox_Group_Role1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, Tox_Group_Role2));
        bind_where_count++;
        return this;
    }

    public GroupPeerDB Tox_Group_RoleIsNull()
    {
        this.sql_where = this.sql_where + " and \"Tox_Group_Role\" IS NULL ";
        return this;
    }

    public GroupPeerDB Tox_Group_RoleIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"Tox_Group_Role\" IS NOT NULL ";
        return this;
    }

    public GroupPeerDB notification_silentEq(boolean notification_silent)
    {
        this.sql_where = this.sql_where + " and \"notification_silent\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, notification_silent));
        bind_where_count++;
        return this;
    }

    public GroupPeerDB notification_silentNotEq(boolean notification_silent)
    {
        this.sql_where = this.sql_where + " and \"notification_silent\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, notification_silent));
        bind_where_count++;
        return this;
    }

    public GroupPeerDB notification_silentIsNull()
    {
        this.sql_where = this.sql_where + " and \"notification_silent\" IS NULL ";
        return this;
    }

    public GroupPeerDB notification_silentIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"notification_silent\" IS NOT NULL ";
        return this;
    }


    // ----------------- OrderBy funcs ------------------ //
    public GroupPeerDB orderByIdAsc()
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

    public GroupPeerDB orderByIdDesc()
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

    public GroupPeerDB orderByGroup_identifierAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"group_identifier\" ASC ";
        return this;
    }

    public GroupPeerDB orderByGroup_identifierDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"group_identifier\" DESC ";
        return this;
    }

    public GroupPeerDB orderByTox_group_peer_pubkeyAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"tox_group_peer_pubkey\" ASC ";
        return this;
    }

    public GroupPeerDB orderByTox_group_peer_pubkeyDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"tox_group_peer_pubkey\" DESC ";
        return this;
    }

    public GroupPeerDB orderByPeer_nameAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"peer_name\" ASC ";
        return this;
    }

    public GroupPeerDB orderByPeer_nameDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"peer_name\" DESC ";
        return this;
    }

    public GroupPeerDB orderByLast_update_timestampAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"last_update_timestamp\" ASC ";
        return this;
    }

    public GroupPeerDB orderByLast_update_timestampDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"last_update_timestamp\" DESC ";
        return this;
    }

    public GroupPeerDB orderByFirst_join_timestampAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"first_join_timestamp\" ASC ";
        return this;
    }

    public GroupPeerDB orderByFirst_join_timestampDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"first_join_timestamp\" DESC ";
        return this;
    }

    public GroupPeerDB orderByTox_Group_RoleAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"Tox_Group_Role\" ASC ";
        return this;
    }

    public GroupPeerDB orderByTox_Group_RoleDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"Tox_Group_Role\" DESC ";
        return this;
    }

    public GroupPeerDB orderByNotification_silentAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"notification_silent\" ASC ";
        return this;
    }

    public GroupPeerDB orderByNotification_silentDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"notification_silent\" DESC ";
        return this;
    }



}

