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
public class RelayListDB
{
    private static final String TAG = "DB.RelayListDB";
    @PrimaryKey
    public String tox_public_key_string;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public int TOX_CONNECTION;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public int TOX_CONNECTION_on_off;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public boolean own_relay;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public long last_online_timestamp;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public String tox_public_key_string_of_owner;

    public static RelayListDB deep_copy(RelayListDB in)
    {
        RelayListDB out = new RelayListDB();
        out.tox_public_key_string = in.tox_public_key_string;
        out.TOX_CONNECTION = in.TOX_CONNECTION;
        out.TOX_CONNECTION_on_off = in.TOX_CONNECTION_on_off;
        out.own_relay = in.own_relay;
        out.last_online_timestamp = in.last_online_timestamp;
        out.tox_public_key_string_of_owner = in.tox_public_key_string_of_owner;

        return out;
    }

    @Override
    public String toString()
    {
        return "tox_public_key_string=" + tox_public_key_string + ", TOX_CONNECTION=" + TOX_CONNECTION + ", TOX_CONNECTION_on_off=" + TOX_CONNECTION_on_off + ", own_relay=" + own_relay + ", last_online_timestamp=" + last_online_timestamp + ", tox_public_key_string_of_owner=" + tox_public_key_string_of_owner;
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

    public List<RelayListDB> toList()
    {
        List<RelayListDB> list = new ArrayList<>();
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
                RelayListDB out = new RelayListDB();
                out.tox_public_key_string = rs.getString("tox_public_key_string");
                out.TOX_CONNECTION = rs.getInt("TOX_CONNECTION");
                out.TOX_CONNECTION_on_off = rs.getInt("TOX_CONNECTION_on_off");
                out.own_relay = rs.getBoolean("own_relay");
                out.last_online_timestamp = rs.getLong("last_online_timestamp");
                out.tox_public_key_string_of_owner = rs.getString("tox_public_key_string_of_owner");

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
                    + "\"tox_public_key_string\""
                    + ",\"TOX_CONNECTION\""
                    + ",\"TOX_CONNECTION_on_off\""
                    + ",\"own_relay\""
                    + ",\"last_online_timestamp\""
                    + ",\"tox_public_key_string_of_owner\""
                    + ")" +
                    "values" +
                    "("
                    + "?1"
                    + ",?2"
                    + ",?3"
                    + ",?4"
                    + ",?5"
                    + ",?6"
                    + ")";

            insert_pstmt = sqldb.prepareStatement(insert_pstmt_sql);
            insert_pstmt.clearParameters();

            insert_pstmt.setString(1, this.tox_public_key_string);
            insert_pstmt.setInt(2, this.TOX_CONNECTION);
            insert_pstmt.setInt(3, this.TOX_CONNECTION_on_off);
            insert_pstmt.setBoolean(4, this.own_relay);
            insert_pstmt.setLong(5, this.last_online_timestamp);
            insert_pstmt.setString(6, this.tox_public_key_string_of_owner);
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

    public RelayListDB get(int i)
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

    public RelayListDB limit(int rowcount)
    {
        this.sql_limit = " limit " + rowcount + " ";
        return this;
    }

    public RelayListDB limit(int rowcount, int offset)
    {
        this.sql_limit = " limit " + offset + " , " + rowcount;
        return this;
    }

    // ----------------------------------- //
    // ----------------------------------- //
    // ----------------------------------- //


    // ----------------- Set funcs ---------------------- //
    public RelayListDB tox_public_key_string(String tox_public_key_string)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"tox_public_key_string\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_public_key_string));
        bind_set_count++;
        return this;
    }

    public RelayListDB TOX_CONNECTION(int TOX_CONNECTION)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"TOX_CONNECTION\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_CONNECTION));
        bind_set_count++;
        return this;
    }

    public RelayListDB TOX_CONNECTION_on_off(int TOX_CONNECTION_on_off)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"TOX_CONNECTION_on_off\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_CONNECTION_on_off));
        bind_set_count++;
        return this;
    }

    public RelayListDB own_relay(boolean own_relay)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"own_relay\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, own_relay));
        bind_set_count++;
        return this;
    }

    public RelayListDB last_online_timestamp(long last_online_timestamp)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"last_online_timestamp\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, last_online_timestamp));
        bind_set_count++;
        return this;
    }

    public RelayListDB tox_public_key_string_of_owner(String tox_public_key_string_of_owner)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"tox_public_key_string_of_owner\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_public_key_string_of_owner));
        bind_set_count++;
        return this;
    }


    // ----------------- Eq/Gt/Lt funcs ----------------- //
    public RelayListDB tox_public_key_stringEq(String tox_public_key_string)
    {
        this.sql_where = this.sql_where + " and \"tox_public_key_string\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_public_key_string));
        bind_where_count++;
        return this;
    }

    public RelayListDB tox_public_key_stringNotEq(String tox_public_key_string)
    {
        this.sql_where = this.sql_where + " and \"tox_public_key_string\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_public_key_string));
        bind_where_count++;
        return this;
    }

    public RelayListDB tox_public_key_stringIsNull()
    {
        this.sql_where = this.sql_where + " and \"tox_public_key_string\" IS NULL ";
        return this;
    }

    public RelayListDB tox_public_key_stringIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"tox_public_key_string\" IS NOT NULL ";
        return this;
    }

    public RelayListDB tox_public_key_stringLike(String tox_public_key_string)
    {
        this.sql_where = this.sql_where + " and \"tox_public_key_string\" LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_public_key_string));
        bind_where_count++;
        return this;
    }

    public RelayListDB tox_public_key_stringNotLike(String tox_public_key_string)
    {
        this.sql_where = this.sql_where + " and \"tox_public_key_string\" NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_public_key_string));
        bind_where_count++;
        return this;
    }

    public RelayListDB TOX_CONNECTIONEq(int TOX_CONNECTION)
    {
        this.sql_where = this.sql_where + " and \"TOX_CONNECTION\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_CONNECTION));
        bind_where_count++;
        return this;
    }

    public RelayListDB TOX_CONNECTIONNotEq(int TOX_CONNECTION)
    {
        this.sql_where = this.sql_where + " and \"TOX_CONNECTION\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_CONNECTION));
        bind_where_count++;
        return this;
    }

    public RelayListDB TOX_CONNECTIONLt(int TOX_CONNECTION)
    {
        this.sql_where = this.sql_where + " and \"TOX_CONNECTION\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_CONNECTION));
        bind_where_count++;
        return this;
    }

    public RelayListDB TOX_CONNECTIONLe(int TOX_CONNECTION)
    {
        this.sql_where = this.sql_where + " and \"TOX_CONNECTION\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_CONNECTION));
        bind_where_count++;
        return this;
    }

    public RelayListDB TOX_CONNECTIONGt(int TOX_CONNECTION)
    {
        this.sql_where = this.sql_where + " and \"TOX_CONNECTION\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_CONNECTION));
        bind_where_count++;
        return this;
    }

    public RelayListDB TOX_CONNECTIONGe(int TOX_CONNECTION)
    {
        this.sql_where = this.sql_where + " and \"TOX_CONNECTION\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_CONNECTION));
        bind_where_count++;
        return this;
    }

    public RelayListDB TOX_CONNECTIONBetween(int TOX_CONNECTION1, int TOX_CONNECTION2)
    {
        this.sql_where = this.sql_where + " and \"TOX_CONNECTION\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and TOX_CONNECTION<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_CONNECTION1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_CONNECTION2));
        bind_where_count++;
        return this;
    }

    public RelayListDB TOX_CONNECTIONIsNull()
    {
        this.sql_where = this.sql_where + " and \"TOX_CONNECTION\" IS NULL ";
        return this;
    }

    public RelayListDB TOX_CONNECTIONIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"TOX_CONNECTION\" IS NOT NULL ";
        return this;
    }

    public RelayListDB TOX_CONNECTION_on_offEq(int TOX_CONNECTION_on_off)
    {
        this.sql_where = this.sql_where + " and \"TOX_CONNECTION_on_off\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_CONNECTION_on_off));
        bind_where_count++;
        return this;
    }

    public RelayListDB TOX_CONNECTION_on_offNotEq(int TOX_CONNECTION_on_off)
    {
        this.sql_where = this.sql_where + " and \"TOX_CONNECTION_on_off\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_CONNECTION_on_off));
        bind_where_count++;
        return this;
    }

    public RelayListDB TOX_CONNECTION_on_offLt(int TOX_CONNECTION_on_off)
    {
        this.sql_where = this.sql_where + " and \"TOX_CONNECTION_on_off\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_CONNECTION_on_off));
        bind_where_count++;
        return this;
    }

    public RelayListDB TOX_CONNECTION_on_offLe(int TOX_CONNECTION_on_off)
    {
        this.sql_where = this.sql_where + " and \"TOX_CONNECTION_on_off\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_CONNECTION_on_off));
        bind_where_count++;
        return this;
    }

    public RelayListDB TOX_CONNECTION_on_offGt(int TOX_CONNECTION_on_off)
    {
        this.sql_where = this.sql_where + " and \"TOX_CONNECTION_on_off\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_CONNECTION_on_off));
        bind_where_count++;
        return this;
    }

    public RelayListDB TOX_CONNECTION_on_offGe(int TOX_CONNECTION_on_off)
    {
        this.sql_where = this.sql_where + " and \"TOX_CONNECTION_on_off\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_CONNECTION_on_off));
        bind_where_count++;
        return this;
    }

    public RelayListDB TOX_CONNECTION_on_offBetween(int TOX_CONNECTION_on_off1, int TOX_CONNECTION_on_off2)
    {
        this.sql_where = this.sql_where + " and \"TOX_CONNECTION_on_off\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and TOX_CONNECTION_on_off<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_CONNECTION_on_off1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_CONNECTION_on_off2));
        bind_where_count++;
        return this;
    }

    public RelayListDB TOX_CONNECTION_on_offIsNull()
    {
        this.sql_where = this.sql_where + " and \"TOX_CONNECTION_on_off\" IS NULL ";
        return this;
    }

    public RelayListDB TOX_CONNECTION_on_offIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"TOX_CONNECTION_on_off\" IS NOT NULL ";
        return this;
    }

    public RelayListDB own_relayEq(boolean own_relay)
    {
        this.sql_where = this.sql_where + " and \"own_relay\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, own_relay));
        bind_where_count++;
        return this;
    }

    public RelayListDB own_relayNotEq(boolean own_relay)
    {
        this.sql_where = this.sql_where + " and \"own_relay\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, own_relay));
        bind_where_count++;
        return this;
    }

    public RelayListDB own_relayIsNull()
    {
        this.sql_where = this.sql_where + " and \"own_relay\" IS NULL ";
        return this;
    }

    public RelayListDB own_relayIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"own_relay\" IS NOT NULL ";
        return this;
    }

    public RelayListDB last_online_timestampEq(long last_online_timestamp)
    {
        this.sql_where = this.sql_where + " and \"last_online_timestamp\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, last_online_timestamp));
        bind_where_count++;
        return this;
    }

    public RelayListDB last_online_timestampNotEq(long last_online_timestamp)
    {
        this.sql_where = this.sql_where + " and \"last_online_timestamp\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, last_online_timestamp));
        bind_where_count++;
        return this;
    }

    public RelayListDB last_online_timestampLt(long last_online_timestamp)
    {
        this.sql_where = this.sql_where + " and \"last_online_timestamp\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, last_online_timestamp));
        bind_where_count++;
        return this;
    }

    public RelayListDB last_online_timestampLe(long last_online_timestamp)
    {
        this.sql_where = this.sql_where + " and \"last_online_timestamp\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, last_online_timestamp));
        bind_where_count++;
        return this;
    }

    public RelayListDB last_online_timestampGt(long last_online_timestamp)
    {
        this.sql_where = this.sql_where + " and \"last_online_timestamp\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, last_online_timestamp));
        bind_where_count++;
        return this;
    }

    public RelayListDB last_online_timestampGe(long last_online_timestamp)
    {
        this.sql_where = this.sql_where + " and \"last_online_timestamp\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, last_online_timestamp));
        bind_where_count++;
        return this;
    }

    public RelayListDB last_online_timestampBetween(long last_online_timestamp1, long last_online_timestamp2)
    {
        this.sql_where = this.sql_where + " and \"last_online_timestamp\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and last_online_timestamp<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, last_online_timestamp1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, last_online_timestamp2));
        bind_where_count++;
        return this;
    }

    public RelayListDB last_online_timestampIsNull()
    {
        this.sql_where = this.sql_where + " and \"last_online_timestamp\" IS NULL ";
        return this;
    }

    public RelayListDB last_online_timestampIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"last_online_timestamp\" IS NOT NULL ";
        return this;
    }

    public RelayListDB tox_public_key_string_of_ownerEq(String tox_public_key_string_of_owner)
    {
        this.sql_where = this.sql_where + " and \"tox_public_key_string_of_owner\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_public_key_string_of_owner));
        bind_where_count++;
        return this;
    }

    public RelayListDB tox_public_key_string_of_ownerNotEq(String tox_public_key_string_of_owner)
    {
        this.sql_where = this.sql_where + " and \"tox_public_key_string_of_owner\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_public_key_string_of_owner));
        bind_where_count++;
        return this;
    }

    public RelayListDB tox_public_key_string_of_ownerIsNull()
    {
        this.sql_where = this.sql_where + " and \"tox_public_key_string_of_owner\" IS NULL ";
        return this;
    }

    public RelayListDB tox_public_key_string_of_ownerIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"tox_public_key_string_of_owner\" IS NOT NULL ";
        return this;
    }

    public RelayListDB tox_public_key_string_of_ownerLike(String tox_public_key_string_of_owner)
    {
        this.sql_where = this.sql_where + " and \"tox_public_key_string_of_owner\" LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_public_key_string_of_owner));
        bind_where_count++;
        return this;
    }

    public RelayListDB tox_public_key_string_of_ownerNotLike(String tox_public_key_string_of_owner)
    {
        this.sql_where = this.sql_where + " and \"tox_public_key_string_of_owner\" NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_public_key_string_of_owner));
        bind_where_count++;
        return this;
    }


    // ----------------- OrderBy funcs ------------------ //
    public RelayListDB orderByTox_public_key_stringAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"tox_public_key_string\" ASC ";
        return this;
    }

    public RelayListDB orderByTox_public_key_stringDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"tox_public_key_string\" DESC ";
        return this;
    }

    public RelayListDB orderByTOX_CONNECTIONAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"TOX_CONNECTION\" ASC ";
        return this;
    }

    public RelayListDB orderByTOX_CONNECTIONDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"TOX_CONNECTION\" DESC ";
        return this;
    }

    public RelayListDB orderByTOX_CONNECTION_on_offAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"TOX_CONNECTION_on_off\" ASC ";
        return this;
    }

    public RelayListDB orderByTOX_CONNECTION_on_offDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"TOX_CONNECTION_on_off\" DESC ";
        return this;
    }

    public RelayListDB orderByOwn_relayAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"own_relay\" ASC ";
        return this;
    }

    public RelayListDB orderByOwn_relayDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"own_relay\" DESC ";
        return this;
    }

    public RelayListDB orderByLast_online_timestampAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"last_online_timestamp\" ASC ";
        return this;
    }

    public RelayListDB orderByLast_online_timestampDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"last_online_timestamp\" DESC ";
        return this;
    }

    public RelayListDB orderByTox_public_key_string_of_ownerAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"tox_public_key_string_of_owner\" ASC ";
        return this;
    }

    public RelayListDB orderByTox_public_key_string_of_ownerDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"tox_public_key_string_of_owner\" DESC ";
        return this;
    }



}

