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
public class ConferenceDB
{
    private static final String TAG = "DB.ConferenceDB";
    @PrimaryKey
    public String conference_identifier;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public String who_invited__tox_public_key_string;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public String name;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public long peer_count;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public long own_peer_number;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public int kind;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public long tox_conference_number;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public boolean conference_active;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public boolean notification_silent;

    public static ConferenceDB deep_copy(ConferenceDB in)
    {
        ConferenceDB out = new ConferenceDB();
        out.conference_identifier = in.conference_identifier;
        out.who_invited__tox_public_key_string = in.who_invited__tox_public_key_string;
        out.name = in.name;
        out.peer_count = in.peer_count;
        out.own_peer_number = in.own_peer_number;
        out.kind = in.kind;
        out.tox_conference_number = in.tox_conference_number;
        out.conference_active = in.conference_active;
        out.notification_silent = in.notification_silent;

        return out;
    }

    @Override
    public String toString()
    {
        return "conference_identifier=" + conference_identifier + ", who_invited__tox_public_key_string=" + who_invited__tox_public_key_string + ", name=" + name + ", peer_count=" + peer_count + ", own_peer_number=" + own_peer_number + ", kind=" + kind + ", tox_conference_number=" + tox_conference_number + ", conference_active=" + conference_active + ", notification_silent=" + notification_silent;
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

    public List<ConferenceDB> toList()
    {
        List<ConferenceDB> list = new ArrayList<>();
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
                ConferenceDB out = new ConferenceDB();
                out.conference_identifier = rs.getString("conference_identifier");
                out.who_invited__tox_public_key_string = rs.getString("who_invited__tox_public_key_string");
                out.name = rs.getString("name");
                out.peer_count = rs.getLong("peer_count");
                out.own_peer_number = rs.getLong("own_peer_number");
                out.kind = rs.getInt("kind");
                out.tox_conference_number = rs.getLong("tox_conference_number");
                out.conference_active = rs.getBoolean("conference_active");
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
                    + "\"conference_identifier\""
                    + ",\"who_invited__tox_public_key_string\""
                    + ",\"name\""
                    + ",\"peer_count\""
                    + ",\"own_peer_number\""
                    + ",\"kind\""
                    + ",\"tox_conference_number\""
                    + ",\"conference_active\""
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
                    + ",?8"
                    + ",?9"
                    + ")";

            insert_pstmt = sqldb.prepareStatement(insert_pstmt_sql);
            insert_pstmt.clearParameters();

            insert_pstmt.setString(1, this.conference_identifier);
            insert_pstmt.setString(2, this.who_invited__tox_public_key_string);
            insert_pstmt.setString(3, this.name);
            insert_pstmt.setLong(4, this.peer_count);
            insert_pstmt.setLong(5, this.own_peer_number);
            insert_pstmt.setInt(6, this.kind);
            insert_pstmt.setLong(7, this.tox_conference_number);
            insert_pstmt.setBoolean(8, this.conference_active);
            insert_pstmt.setBoolean(9, this.notification_silent);
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

    public ConferenceDB get(int i)
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

    public ConferenceDB limit(int rowcount)
    {
        this.sql_limit = " limit " + rowcount + " ";
        return this;
    }

    public ConferenceDB limit(int rowcount, int offset)
    {
        this.sql_limit = " limit " + offset + " , " + rowcount;
        return this;
    }

    // ----------------------------------- //
    // ----------------------------------- //
    // ----------------------------------- //


    // ----------------- Set funcs ---------------------- //
    public ConferenceDB conference_identifier(String conference_identifier)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"conference_identifier\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, conference_identifier));
        bind_set_count++;
        return this;
    }

    public ConferenceDB who_invited__tox_public_key_string(String who_invited__tox_public_key_string)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"who_invited__tox_public_key_string\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, who_invited__tox_public_key_string));
        bind_set_count++;
        return this;
    }

    public ConferenceDB name(String name)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"name\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, name));
        bind_set_count++;
        return this;
    }

    public ConferenceDB peer_count(long peer_count)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"peer_count\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, peer_count));
        bind_set_count++;
        return this;
    }

    public ConferenceDB own_peer_number(long own_peer_number)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"own_peer_number\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, own_peer_number));
        bind_set_count++;
        return this;
    }

    public ConferenceDB kind(int kind)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"kind\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, kind));
        bind_set_count++;
        return this;
    }

    public ConferenceDB tox_conference_number(long tox_conference_number)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"tox_conference_number\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, tox_conference_number));
        bind_set_count++;
        return this;
    }

    public ConferenceDB conference_active(boolean conference_active)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"conference_active\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, conference_active));
        bind_set_count++;
        return this;
    }

    public ConferenceDB notification_silent(boolean notification_silent)
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
    public ConferenceDB conference_identifierEq(String conference_identifier)
    {
        this.sql_where = this.sql_where + " and \"conference_identifier\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, conference_identifier));
        bind_where_count++;
        return this;
    }

    public ConferenceDB conference_identifierNotEq(String conference_identifier)
    {
        this.sql_where = this.sql_where + " and \"conference_identifier\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, conference_identifier));
        bind_where_count++;
        return this;
    }

    public ConferenceDB conference_identifierIsNull()
    {
        this.sql_where = this.sql_where + " and \"conference_identifier\" IS NULL ";
        return this;
    }

    public ConferenceDB conference_identifierIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"conference_identifier\" IS NOT NULL ";
        return this;
    }

    public ConferenceDB conference_identifierLike(String conference_identifier)
    {
        this.sql_where = this.sql_where + " and \"conference_identifier\" LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, conference_identifier));
        bind_where_count++;
        return this;
    }

    public ConferenceDB conference_identifierNotLike(String conference_identifier)
    {
        this.sql_where = this.sql_where + " and \"conference_identifier\" NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, conference_identifier));
        bind_where_count++;
        return this;
    }

    public ConferenceDB who_invited__tox_public_key_stringEq(String who_invited__tox_public_key_string)
    {
        this.sql_where = this.sql_where + " and \"who_invited__tox_public_key_string\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, who_invited__tox_public_key_string));
        bind_where_count++;
        return this;
    }

    public ConferenceDB who_invited__tox_public_key_stringNotEq(String who_invited__tox_public_key_string)
    {
        this.sql_where = this.sql_where + " and \"who_invited__tox_public_key_string\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, who_invited__tox_public_key_string));
        bind_where_count++;
        return this;
    }

    public ConferenceDB who_invited__tox_public_key_stringIsNull()
    {
        this.sql_where = this.sql_where + " and \"who_invited__tox_public_key_string\" IS NULL ";
        return this;
    }

    public ConferenceDB who_invited__tox_public_key_stringIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"who_invited__tox_public_key_string\" IS NOT NULL ";
        return this;
    }

    public ConferenceDB who_invited__tox_public_key_stringLike(String who_invited__tox_public_key_string)
    {
        this.sql_where = this.sql_where + " and \"who_invited__tox_public_key_string\" LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, who_invited__tox_public_key_string));
        bind_where_count++;
        return this;
    }

    public ConferenceDB who_invited__tox_public_key_stringNotLike(String who_invited__tox_public_key_string)
    {
        this.sql_where = this.sql_where + " and \"who_invited__tox_public_key_string\" NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, who_invited__tox_public_key_string));
        bind_where_count++;
        return this;
    }

    public ConferenceDB nameEq(String name)
    {
        this.sql_where = this.sql_where + " and \"name\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, name));
        bind_where_count++;
        return this;
    }

    public ConferenceDB nameNotEq(String name)
    {
        this.sql_where = this.sql_where + " and \"name\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, name));
        bind_where_count++;
        return this;
    }

    public ConferenceDB nameIsNull()
    {
        this.sql_where = this.sql_where + " and \"name\" IS NULL ";
        return this;
    }

    public ConferenceDB nameIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"name\" IS NOT NULL ";
        return this;
    }

    public ConferenceDB nameLike(String name)
    {
        this.sql_where = this.sql_where + " and \"name\" LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, name));
        bind_where_count++;
        return this;
    }

    public ConferenceDB nameNotLike(String name)
    {
        this.sql_where = this.sql_where + " and \"name\" NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, name));
        bind_where_count++;
        return this;
    }

    public ConferenceDB peer_countEq(long peer_count)
    {
        this.sql_where = this.sql_where + " and \"peer_count\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, peer_count));
        bind_where_count++;
        return this;
    }

    public ConferenceDB peer_countNotEq(long peer_count)
    {
        this.sql_where = this.sql_where + " and \"peer_count\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, peer_count));
        bind_where_count++;
        return this;
    }

    public ConferenceDB peer_countLt(long peer_count)
    {
        this.sql_where = this.sql_where + " and \"peer_count\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, peer_count));
        bind_where_count++;
        return this;
    }

    public ConferenceDB peer_countLe(long peer_count)
    {
        this.sql_where = this.sql_where + " and \"peer_count\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, peer_count));
        bind_where_count++;
        return this;
    }

    public ConferenceDB peer_countGt(long peer_count)
    {
        this.sql_where = this.sql_where + " and \"peer_count\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, peer_count));
        bind_where_count++;
        return this;
    }

    public ConferenceDB peer_countGe(long peer_count)
    {
        this.sql_where = this.sql_where + " and \"peer_count\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, peer_count));
        bind_where_count++;
        return this;
    }

    public ConferenceDB peer_countBetween(long peer_count1, long peer_count2)
    {
        this.sql_where = this.sql_where + " and \"peer_count\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and peer_count<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, peer_count1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, peer_count2));
        bind_where_count++;
        return this;
    }

    public ConferenceDB peer_countIsNull()
    {
        this.sql_where = this.sql_where + " and \"peer_count\" IS NULL ";
        return this;
    }

    public ConferenceDB peer_countIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"peer_count\" IS NOT NULL ";
        return this;
    }

    public ConferenceDB own_peer_numberEq(long own_peer_number)
    {
        this.sql_where = this.sql_where + " and \"own_peer_number\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, own_peer_number));
        bind_where_count++;
        return this;
    }

    public ConferenceDB own_peer_numberNotEq(long own_peer_number)
    {
        this.sql_where = this.sql_where + " and \"own_peer_number\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, own_peer_number));
        bind_where_count++;
        return this;
    }

    public ConferenceDB own_peer_numberLt(long own_peer_number)
    {
        this.sql_where = this.sql_where + " and \"own_peer_number\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, own_peer_number));
        bind_where_count++;
        return this;
    }

    public ConferenceDB own_peer_numberLe(long own_peer_number)
    {
        this.sql_where = this.sql_where + " and \"own_peer_number\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, own_peer_number));
        bind_where_count++;
        return this;
    }

    public ConferenceDB own_peer_numberGt(long own_peer_number)
    {
        this.sql_where = this.sql_where + " and \"own_peer_number\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, own_peer_number));
        bind_where_count++;
        return this;
    }

    public ConferenceDB own_peer_numberGe(long own_peer_number)
    {
        this.sql_where = this.sql_where + " and \"own_peer_number\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, own_peer_number));
        bind_where_count++;
        return this;
    }

    public ConferenceDB own_peer_numberBetween(long own_peer_number1, long own_peer_number2)
    {
        this.sql_where = this.sql_where + " and \"own_peer_number\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and own_peer_number<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, own_peer_number1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, own_peer_number2));
        bind_where_count++;
        return this;
    }

    public ConferenceDB own_peer_numberIsNull()
    {
        this.sql_where = this.sql_where + " and \"own_peer_number\" IS NULL ";
        return this;
    }

    public ConferenceDB own_peer_numberIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"own_peer_number\" IS NOT NULL ";
        return this;
    }

    public ConferenceDB kindEq(int kind)
    {
        this.sql_where = this.sql_where + " and \"kind\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, kind));
        bind_where_count++;
        return this;
    }

    public ConferenceDB kindNotEq(int kind)
    {
        this.sql_where = this.sql_where + " and \"kind\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, kind));
        bind_where_count++;
        return this;
    }

    public ConferenceDB kindLt(int kind)
    {
        this.sql_where = this.sql_where + " and \"kind\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, kind));
        bind_where_count++;
        return this;
    }

    public ConferenceDB kindLe(int kind)
    {
        this.sql_where = this.sql_where + " and \"kind\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, kind));
        bind_where_count++;
        return this;
    }

    public ConferenceDB kindGt(int kind)
    {
        this.sql_where = this.sql_where + " and \"kind\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, kind));
        bind_where_count++;
        return this;
    }

    public ConferenceDB kindGe(int kind)
    {
        this.sql_where = this.sql_where + " and \"kind\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, kind));
        bind_where_count++;
        return this;
    }

    public ConferenceDB kindBetween(int kind1, int kind2)
    {
        this.sql_where = this.sql_where + " and \"kind\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and kind<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, kind1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, kind2));
        bind_where_count++;
        return this;
    }

    public ConferenceDB kindIsNull()
    {
        this.sql_where = this.sql_where + " and \"kind\" IS NULL ";
        return this;
    }

    public ConferenceDB kindIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"kind\" IS NOT NULL ";
        return this;
    }

    public ConferenceDB tox_conference_numberEq(long tox_conference_number)
    {
        this.sql_where = this.sql_where + " and \"tox_conference_number\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, tox_conference_number));
        bind_where_count++;
        return this;
    }

    public ConferenceDB tox_conference_numberNotEq(long tox_conference_number)
    {
        this.sql_where = this.sql_where + " and \"tox_conference_number\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, tox_conference_number));
        bind_where_count++;
        return this;
    }

    public ConferenceDB tox_conference_numberLt(long tox_conference_number)
    {
        this.sql_where = this.sql_where + " and \"tox_conference_number\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, tox_conference_number));
        bind_where_count++;
        return this;
    }

    public ConferenceDB tox_conference_numberLe(long tox_conference_number)
    {
        this.sql_where = this.sql_where + " and \"tox_conference_number\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, tox_conference_number));
        bind_where_count++;
        return this;
    }

    public ConferenceDB tox_conference_numberGt(long tox_conference_number)
    {
        this.sql_where = this.sql_where + " and \"tox_conference_number\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, tox_conference_number));
        bind_where_count++;
        return this;
    }

    public ConferenceDB tox_conference_numberGe(long tox_conference_number)
    {
        this.sql_where = this.sql_where + " and \"tox_conference_number\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, tox_conference_number));
        bind_where_count++;
        return this;
    }

    public ConferenceDB tox_conference_numberBetween(long tox_conference_number1, long tox_conference_number2)
    {
        this.sql_where = this.sql_where + " and \"tox_conference_number\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and tox_conference_number<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, tox_conference_number1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, tox_conference_number2));
        bind_where_count++;
        return this;
    }

    public ConferenceDB tox_conference_numberIsNull()
    {
        this.sql_where = this.sql_where + " and \"tox_conference_number\" IS NULL ";
        return this;
    }

    public ConferenceDB tox_conference_numberIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"tox_conference_number\" IS NOT NULL ";
        return this;
    }

    public ConferenceDB conference_activeEq(boolean conference_active)
    {
        this.sql_where = this.sql_where + " and \"conference_active\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, conference_active));
        bind_where_count++;
        return this;
    }

    public ConferenceDB conference_activeNotEq(boolean conference_active)
    {
        this.sql_where = this.sql_where + " and \"conference_active\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, conference_active));
        bind_where_count++;
        return this;
    }

    public ConferenceDB conference_activeIsNull()
    {
        this.sql_where = this.sql_where + " and \"conference_active\" IS NULL ";
        return this;
    }

    public ConferenceDB conference_activeIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"conference_active\" IS NOT NULL ";
        return this;
    }

    public ConferenceDB notification_silentEq(boolean notification_silent)
    {
        this.sql_where = this.sql_where + " and \"notification_silent\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, notification_silent));
        bind_where_count++;
        return this;
    }

    public ConferenceDB notification_silentNotEq(boolean notification_silent)
    {
        this.sql_where = this.sql_where + " and \"notification_silent\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, notification_silent));
        bind_where_count++;
        return this;
    }

    public ConferenceDB notification_silentIsNull()
    {
        this.sql_where = this.sql_where + " and \"notification_silent\" IS NULL ";
        return this;
    }

    public ConferenceDB notification_silentIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"notification_silent\" IS NOT NULL ";
        return this;
    }


    // ----------------- OrderBy funcs ------------------ //
    public ConferenceDB orderByConference_identifierAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"conference_identifier\" ASC ";
        return this;
    }

    public ConferenceDB orderByConference_identifierDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"conference_identifier\" DESC ";
        return this;
    }

    public ConferenceDB orderByWho_invited__tox_public_key_stringAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"who_invited__tox_public_key_string\" ASC ";
        return this;
    }

    public ConferenceDB orderByWho_invited__tox_public_key_stringDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"who_invited__tox_public_key_string\" DESC ";
        return this;
    }

    public ConferenceDB orderByNameAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"name\" ASC ";
        return this;
    }

    public ConferenceDB orderByNameDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"name\" DESC ";
        return this;
    }

    public ConferenceDB orderByPeer_countAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"peer_count\" ASC ";
        return this;
    }

    public ConferenceDB orderByPeer_countDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"peer_count\" DESC ";
        return this;
    }

    public ConferenceDB orderByOwn_peer_numberAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"own_peer_number\" ASC ";
        return this;
    }

    public ConferenceDB orderByOwn_peer_numberDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"own_peer_number\" DESC ";
        return this;
    }

    public ConferenceDB orderByKindAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"kind\" ASC ";
        return this;
    }

    public ConferenceDB orderByKindDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"kind\" DESC ";
        return this;
    }

    public ConferenceDB orderByTox_conference_numberAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"tox_conference_number\" ASC ";
        return this;
    }

    public ConferenceDB orderByTox_conference_numberDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"tox_conference_number\" DESC ";
        return this;
    }

    public ConferenceDB orderByConference_activeAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"conference_active\" ASC ";
        return this;
    }

    public ConferenceDB orderByConference_activeDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"conference_active\" DESC ";
        return this;
    }

    public ConferenceDB orderByNotification_silentAsc()
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

    public ConferenceDB orderByNotification_silentDesc()
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

