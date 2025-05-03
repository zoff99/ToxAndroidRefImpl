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
public class ConferencePeerCacheDB
{
    private static final String TAG = "DB.ConferencePeerCac";
    @PrimaryKey(autoincrement = true, auto = true)
    public long id;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public String conference_identifier;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public String peer_pubkey;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public String peer_name;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public long last_update_timestamp;

    static ConferencePeerCacheDB deep_copy(ConferencePeerCacheDB in)
    {
        ConferencePeerCacheDB out = new ConferencePeerCacheDB();
        out.id = in.id;
        out.conference_identifier = in.conference_identifier;
        out.peer_pubkey = in.peer_pubkey;
        out.peer_name = in.peer_name;
        out.last_update_timestamp = in.last_update_timestamp;

        return out;
    }

    @Override
    public String toString()
    {
        return "id=" + id + ", conference_identifier=" + conference_identifier + ", peer_pubkey=" + peer_pubkey + ", peer_name=" + peer_name + ", last_update_timestamp=" + last_update_timestamp;
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

    public List<ConferencePeerCacheDB> toList()
    {
        List<ConferencePeerCacheDB> list = new ArrayList<>();
        try
        {
            final String sql = this.sql_start + " " + this.sql_where + " " + this.sql_orderby + " " + this.sql_limit;
            log_bindvars_where(sql, bind_where_count, bind_where_vars);
            final long t1 = System.currentTimeMillis();
            PreparedStatement statement = sqldb.prepareStatement(sql);
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
                ConferencePeerCacheDB out = new ConferencePeerCacheDB();
                out.id = rs.getLong("id");
                out.conference_identifier = rs.getString("conference_identifier");
                out.peer_pubkey = rs.getString("peer_pubkey");
                out.peer_name = rs.getString("peer_name");
                out.last_update_timestamp = rs.getLong("last_update_timestamp");

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
                statement.close();
            }
            catch (Exception ignored)
            {
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return list;
    }


    public long insert()
    {
        long ret = -1;

        try
        {
            String insert_pstmt_sql = null;
            PreparedStatement insert_pstmt = null;

            // @formatter:off
            insert_pstmt_sql ="insert into " + this.getClass().getSimpleName() +
                    "("
                    + "conference_identifier"
                    + ",peer_pubkey"
                    + ",peer_name"
                    + ",last_update_timestamp"
                    + ")" +
                    "values" +
                    "("
                    + "?1"
                    + ",?2"
                    + ",?3"
                    + ",?4"
                    + ")";

            insert_pstmt = sqldb.prepareStatement(insert_pstmt_sql);
            insert_pstmt.clearParameters();

            insert_pstmt.setString(1, this.conference_identifier);
            insert_pstmt.setString(2, this.peer_pubkey);
            insert_pstmt.setString(3, this.peer_name);
            insert_pstmt.setLong(4, this.last_update_timestamp);
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
            throw new RuntimeException(e);
        }

        return ret;
    }

    public ConferencePeerCacheDB get(int i)
    {
        this.sql_limit = " limit " + i + ",1 ";
        return this.toList().get(0);
    }

    public void execute()
    {
        try
        {
            final String sql = this.sql_start + " " + this.sql_set + " " + this.sql_where;
            log_bindvars_where_and_set(sql, bind_where_count, bind_where_vars, bind_set_count, bind_set_vars);
            PreparedStatement statement = sqldb.prepareStatement(sql);
            if (!set_bindvars_where_and_set(statement, bind_where_count, bind_where_vars, bind_set_count, bind_set_vars))
            {
                try
                {
                    statement.close();
                }
                catch (Exception ignored)
                {
                }
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
            e2.printStackTrace();
            Log.i(TAG, "EE1:" + e2.getMessage());
        }
    }

    public int count()
    {
        int ret = 0;

        try
        {
            this.sql_start = "SELECT count(*) as count FROM " + this.getClass().getSimpleName();

            final String sql = this.sql_start + " " + this.sql_where + " " + this.sql_orderby + " " + this.sql_limit;
            log_bindvars_where(sql, bind_where_count, bind_where_vars);
            PreparedStatement statement = sqldb.prepareStatement(sql);
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
                statement.close();
            }
            catch (Exception ignored)
            {
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return ret;
    }

    public ConferencePeerCacheDB limit(int rowcount)
    {
        this.sql_limit = " limit " + rowcount + " ";
        return this;
    }

    public ConferencePeerCacheDB limit(int rowcount, int offset)
    {
        this.sql_limit = " limit " + offset + " , " + rowcount;
        return this;
    }

    // ----------------------------------- //
    // ----------------------------------- //
    // ----------------------------------- //


    // ----------------- Set funcs ---------------------- //
    public ConferencePeerCacheDB id(long id)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " id=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_set_count++;
        return this;
    }

    public ConferencePeerCacheDB conference_identifier(String conference_identifier)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " conference_identifier=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, conference_identifier));
        bind_set_count++;
        return this;
    }

    public ConferencePeerCacheDB peer_pubkey(String peer_pubkey)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " peer_pubkey=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, peer_pubkey));
        bind_set_count++;
        return this;
    }

    public ConferencePeerCacheDB peer_name(String peer_name)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " peer_name=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, peer_name));
        bind_set_count++;
        return this;
    }

    public ConferencePeerCacheDB last_update_timestamp(long last_update_timestamp)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " last_update_timestamp=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, last_update_timestamp));
        bind_set_count++;
        return this;
    }


    // ----------------- Eq/Gt/Lt funcs ----------------- //
    public ConferencePeerCacheDB idEq(long id)
    {
        this.sql_where = this.sql_where + " and id=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public ConferencePeerCacheDB idNotEq(long id)
    {
        this.sql_where = this.sql_where + " and id<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public ConferencePeerCacheDB idLt(long id)
    {
        this.sql_where = this.sql_where + " and id<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public ConferencePeerCacheDB idLe(long id)
    {
        this.sql_where = this.sql_where + " and id<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public ConferencePeerCacheDB idGt(long id)
    {
        this.sql_where = this.sql_where + " and id>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public ConferencePeerCacheDB idGe(long id)
    {
        this.sql_where = this.sql_where + " and id>=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public ConferencePeerCacheDB idBetween(long id1, long id2)
    {
        this.sql_where = this.sql_where + " and id>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and id<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id2));
        bind_where_count++;
        return this;
    }

    public ConferencePeerCacheDB idIsNull()
    {
        this.sql_where = this.sql_where + " and id IS NULL ";
        return this;
    }

    public ConferencePeerCacheDB idIsNotNull()
    {
        this.sql_where = this.sql_where + " and id IS NOT NULL ";
        return this;
    }

    public ConferencePeerCacheDB conference_identifierEq(String conference_identifier)
    {
        this.sql_where = this.sql_where + " and conference_identifier=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, conference_identifier));
        bind_where_count++;
        return this;
    }

    public ConferencePeerCacheDB conference_identifierNotEq(String conference_identifier)
    {
        this.sql_where = this.sql_where + " and conference_identifier<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, conference_identifier));
        bind_where_count++;
        return this;
    }

    public ConferencePeerCacheDB conference_identifierIsNull()
    {
        this.sql_where = this.sql_where + " and conference_identifier IS NULL ";
        return this;
    }

    public ConferencePeerCacheDB conference_identifierIsNotNull()
    {
        this.sql_where = this.sql_where + " and conference_identifier IS NOT NULL ";
        return this;
    }

    public ConferencePeerCacheDB conference_identifierLike(String conference_identifier)
    {
        this.sql_where = this.sql_where + " and conference_identifier LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, conference_identifier));
        bind_where_count++;
        return this;
    }

    public ConferencePeerCacheDB conference_identifierNotLike(String conference_identifier)
    {
        this.sql_where = this.sql_where + " and conference_identifier NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, conference_identifier));
        bind_where_count++;
        return this;
    }

    public ConferencePeerCacheDB peer_pubkeyEq(String peer_pubkey)
    {
        this.sql_where = this.sql_where + " and peer_pubkey=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, peer_pubkey));
        bind_where_count++;
        return this;
    }

    public ConferencePeerCacheDB peer_pubkeyNotEq(String peer_pubkey)
    {
        this.sql_where = this.sql_where + " and peer_pubkey<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, peer_pubkey));
        bind_where_count++;
        return this;
    }

    public ConferencePeerCacheDB peer_pubkeyIsNull()
    {
        this.sql_where = this.sql_where + " and peer_pubkey IS NULL ";
        return this;
    }

    public ConferencePeerCacheDB peer_pubkeyIsNotNull()
    {
        this.sql_where = this.sql_where + " and peer_pubkey IS NOT NULL ";
        return this;
    }

    public ConferencePeerCacheDB peer_pubkeyLike(String peer_pubkey)
    {
        this.sql_where = this.sql_where + " and peer_pubkey LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, peer_pubkey));
        bind_where_count++;
        return this;
    }

    public ConferencePeerCacheDB peer_pubkeyNotLike(String peer_pubkey)
    {
        this.sql_where = this.sql_where + " and peer_pubkey NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, peer_pubkey));
        bind_where_count++;
        return this;
    }

    public ConferencePeerCacheDB peer_nameEq(String peer_name)
    {
        this.sql_where = this.sql_where + " and peer_name=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, peer_name));
        bind_where_count++;
        return this;
    }

    public ConferencePeerCacheDB peer_nameNotEq(String peer_name)
    {
        this.sql_where = this.sql_where + " and peer_name<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, peer_name));
        bind_where_count++;
        return this;
    }

    public ConferencePeerCacheDB peer_nameIsNull()
    {
        this.sql_where = this.sql_where + " and peer_name IS NULL ";
        return this;
    }

    public ConferencePeerCacheDB peer_nameIsNotNull()
    {
        this.sql_where = this.sql_where + " and peer_name IS NOT NULL ";
        return this;
    }

    public ConferencePeerCacheDB peer_nameLike(String peer_name)
    {
        this.sql_where = this.sql_where + " and peer_name LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, peer_name));
        bind_where_count++;
        return this;
    }

    public ConferencePeerCacheDB peer_nameNotLike(String peer_name)
    {
        this.sql_where = this.sql_where + " and peer_name NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, peer_name));
        bind_where_count++;
        return this;
    }

    public ConferencePeerCacheDB last_update_timestampEq(long last_update_timestamp)
    {
        this.sql_where = this.sql_where + " and last_update_timestamp=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, last_update_timestamp));
        bind_where_count++;
        return this;
    }

    public ConferencePeerCacheDB last_update_timestampNotEq(long last_update_timestamp)
    {
        this.sql_where = this.sql_where + " and last_update_timestamp<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, last_update_timestamp));
        bind_where_count++;
        return this;
    }

    public ConferencePeerCacheDB last_update_timestampLt(long last_update_timestamp)
    {
        this.sql_where = this.sql_where + " and last_update_timestamp<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, last_update_timestamp));
        bind_where_count++;
        return this;
    }

    public ConferencePeerCacheDB last_update_timestampLe(long last_update_timestamp)
    {
        this.sql_where = this.sql_where + " and last_update_timestamp<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, last_update_timestamp));
        bind_where_count++;
        return this;
    }

    public ConferencePeerCacheDB last_update_timestampGt(long last_update_timestamp)
    {
        this.sql_where = this.sql_where + " and last_update_timestamp>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, last_update_timestamp));
        bind_where_count++;
        return this;
    }

    public ConferencePeerCacheDB last_update_timestampGe(long last_update_timestamp)
    {
        this.sql_where = this.sql_where + " and last_update_timestamp>=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, last_update_timestamp));
        bind_where_count++;
        return this;
    }

    public ConferencePeerCacheDB last_update_timestampBetween(long last_update_timestamp1, long last_update_timestamp2)
    {
        this.sql_where = this.sql_where + " and last_update_timestamp>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and last_update_timestamp<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, last_update_timestamp1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, last_update_timestamp2));
        bind_where_count++;
        return this;
    }

    public ConferencePeerCacheDB last_update_timestampIsNull()
    {
        this.sql_where = this.sql_where + " and last_update_timestamp IS NULL ";
        return this;
    }

    public ConferencePeerCacheDB last_update_timestampIsNotNull()
    {
        this.sql_where = this.sql_where + " and last_update_timestamp IS NOT NULL ";
        return this;
    }


    // ----------------- OrderBy funcs ------------------ //
    public ConferencePeerCacheDB orderByIdAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " id ASC ";
        return this;
    }

    public ConferencePeerCacheDB orderByIdDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " id DESC ";
        return this;
    }

    public ConferencePeerCacheDB orderByConference_identifierAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " conference_identifier ASC ";
        return this;
    }

    public ConferencePeerCacheDB orderByConference_identifierDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " conference_identifier DESC ";
        return this;
    }

    public ConferencePeerCacheDB orderByPeer_pubkeyAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " peer_pubkey ASC ";
        return this;
    }

    public ConferencePeerCacheDB orderByPeer_pubkeyDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " peer_pubkey DESC ";
        return this;
    }

    public ConferencePeerCacheDB orderByPeer_nameAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " peer_name ASC ";
        return this;
    }

    public ConferencePeerCacheDB orderByPeer_nameDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " peer_name DESC ";
        return this;
    }

    public ConferencePeerCacheDB orderByLast_update_timestampAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " last_update_timestamp ASC ";
        return this;
    }

    public ConferencePeerCacheDB orderByLast_update_timestampDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " last_update_timestamp DESC ";
        return this;
    }



}

