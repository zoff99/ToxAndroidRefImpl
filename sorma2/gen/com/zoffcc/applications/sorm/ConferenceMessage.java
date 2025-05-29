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
public class ConferenceMessage
{
    private static final String TAG = "DB.ConferenceMessage";
    @PrimaryKey(autoincrement = true, auto = true)
    public long id;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public String message_id_tox;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public String conference_identifier;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public String tox_peerpubkey;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public String tox_peername;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public int direction;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public int TOX_MESSAGE_TYPE;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public int TRIFA_MESSAGE_TYPE;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public long sent_timestamp;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public long rcvd_timestamp;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public boolean read;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public boolean is_new;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public String text;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public boolean was_synced;

    public static ConferenceMessage deep_copy(ConferenceMessage in)
    {
        ConferenceMessage out = new ConferenceMessage();
        out.id = in.id;
        out.message_id_tox = in.message_id_tox;
        out.conference_identifier = in.conference_identifier;
        out.tox_peerpubkey = in.tox_peerpubkey;
        out.tox_peername = in.tox_peername;
        out.direction = in.direction;
        out.TOX_MESSAGE_TYPE = in.TOX_MESSAGE_TYPE;
        out.TRIFA_MESSAGE_TYPE = in.TRIFA_MESSAGE_TYPE;
        out.sent_timestamp = in.sent_timestamp;
        out.rcvd_timestamp = in.rcvd_timestamp;
        out.read = in.read;
        out.is_new = in.is_new;
        out.text = in.text;
        out.was_synced = in.was_synced;

        return out;
    }

    @Override
    public String toString()
    {
        return "id=" + id + ", message_id_tox=" + message_id_tox + ", conference_identifier=" + conference_identifier + ", tox_peerpubkey=" + tox_peerpubkey + ", tox_peername=" + tox_peername + ", direction=" + direction + ", TOX_MESSAGE_TYPE=" + TOX_MESSAGE_TYPE + ", TRIFA_MESSAGE_TYPE=" + TRIFA_MESSAGE_TYPE + ", sent_timestamp=" + sent_timestamp + ", rcvd_timestamp=" + rcvd_timestamp + ", read=" + read + ", is_new=" + is_new + ", text=" + text + ", was_synced=" + was_synced;
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

    public List<ConferenceMessage> toList()
    {
        List<ConferenceMessage> list = new ArrayList<>();
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
                ConferenceMessage out = new ConferenceMessage();
                out.id = rs.getLong("id");
                out.message_id_tox = rs.getString("message_id_tox");
                out.conference_identifier = rs.getString("conference_identifier");
                out.tox_peerpubkey = rs.getString("tox_peerpubkey");
                out.tox_peername = rs.getString("tox_peername");
                out.direction = rs.getInt("direction");
                out.TOX_MESSAGE_TYPE = rs.getInt("TOX_MESSAGE_TYPE");
                out.TRIFA_MESSAGE_TYPE = rs.getInt("TRIFA_MESSAGE_TYPE");
                out.sent_timestamp = rs.getLong("sent_timestamp");
                out.rcvd_timestamp = rs.getLong("rcvd_timestamp");
                out.read = rs.getBoolean("read");
                out.is_new = rs.getBoolean("is_new");
                out.text = rs.getString("text");
                out.was_synced = rs.getBoolean("was_synced");

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
                    + "\"message_id_tox\""
                    + ",\"conference_identifier\""
                    + ",\"tox_peerpubkey\""
                    + ",\"tox_peername\""
                    + ",\"direction\""
                    + ",\"TOX_MESSAGE_TYPE\""
                    + ",\"TRIFA_MESSAGE_TYPE\""
                    + ",\"sent_timestamp\""
                    + ",\"rcvd_timestamp\""
                    + ",\"read\""
                    + ",\"is_new\""
                    + ",\"text\""
                    + ",\"was_synced\""
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
                    + ",?10"
                    + ",?11"
                    + ",?12"
                    + ",?13"
                    + ")";

            insert_pstmt = sqldb.prepareStatement(insert_pstmt_sql);
            insert_pstmt.clearParameters();

            insert_pstmt.setString(1, this.message_id_tox);
            insert_pstmt.setString(2, this.conference_identifier);
            insert_pstmt.setString(3, this.tox_peerpubkey);
            insert_pstmt.setString(4, this.tox_peername);
            insert_pstmt.setInt(5, this.direction);
            insert_pstmt.setInt(6, this.TOX_MESSAGE_TYPE);
            insert_pstmt.setInt(7, this.TRIFA_MESSAGE_TYPE);
            insert_pstmt.setLong(8, this.sent_timestamp);
            insert_pstmt.setLong(9, this.rcvd_timestamp);
            insert_pstmt.setBoolean(10, this.read);
            insert_pstmt.setBoolean(11, this.is_new);
            insert_pstmt.setString(12, this.text);
            insert_pstmt.setBoolean(13, this.was_synced);
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

    public ConferenceMessage get(int i)
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

    public ConferenceMessage limit(int rowcount)
    {
        this.sql_limit = " limit " + rowcount + " ";
        return this;
    }

    public ConferenceMessage limit(int rowcount, int offset)
    {
        this.sql_limit = " limit " + offset + " , " + rowcount;
        return this;
    }

    // ----------------------------------- //
    // ----------------------------------- //
    // ----------------------------------- //


    // ----------------- Set funcs ---------------------- //
    public ConferenceMessage id(long id)
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

    public ConferenceMessage message_id_tox(String message_id_tox)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"message_id_tox\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, message_id_tox));
        bind_set_count++;
        return this;
    }

    public ConferenceMessage conference_identifier(String conference_identifier)
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

    public ConferenceMessage tox_peerpubkey(String tox_peerpubkey)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"tox_peerpubkey\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_peerpubkey));
        bind_set_count++;
        return this;
    }

    public ConferenceMessage tox_peername(String tox_peername)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"tox_peername\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_peername));
        bind_set_count++;
        return this;
    }

    public ConferenceMessage direction(int direction)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"direction\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, direction));
        bind_set_count++;
        return this;
    }

    public ConferenceMessage TOX_MESSAGE_TYPE(int TOX_MESSAGE_TYPE)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"TOX_MESSAGE_TYPE\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_MESSAGE_TYPE));
        bind_set_count++;
        return this;
    }

    public ConferenceMessage TRIFA_MESSAGE_TYPE(int TRIFA_MESSAGE_TYPE)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"TRIFA_MESSAGE_TYPE\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TRIFA_MESSAGE_TYPE));
        bind_set_count++;
        return this;
    }

    public ConferenceMessage sent_timestamp(long sent_timestamp)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"sent_timestamp\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp));
        bind_set_count++;
        return this;
    }

    public ConferenceMessage rcvd_timestamp(long rcvd_timestamp)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"rcvd_timestamp\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp));
        bind_set_count++;
        return this;
    }

    public ConferenceMessage read(boolean read)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"read\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, read));
        bind_set_count++;
        return this;
    }

    public ConferenceMessage is_new(boolean is_new)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"is_new\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, is_new));
        bind_set_count++;
        return this;
    }

    public ConferenceMessage text(String text)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"text\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, text));
        bind_set_count++;
        return this;
    }

    public ConferenceMessage was_synced(boolean was_synced)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"was_synced\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, was_synced));
        bind_set_count++;
        return this;
    }


    // ----------------- Eq/Gt/Lt funcs ----------------- //
    public ConferenceMessage idEq(long id)
    {
        this.sql_where = this.sql_where + " and \"id\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage idNotEq(long id)
    {
        this.sql_where = this.sql_where + " and \"id\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage idLt(long id)
    {
        this.sql_where = this.sql_where + " and \"id\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage idLe(long id)
    {
        this.sql_where = this.sql_where + " and \"id\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage idGt(long id)
    {
        this.sql_where = this.sql_where + " and \"id\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage idGe(long id)
    {
        this.sql_where = this.sql_where + " and \"id\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage idBetween(long id1, long id2)
    {
        this.sql_where = this.sql_where + " and \"id\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and id<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id2));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage idIsNull()
    {
        this.sql_where = this.sql_where + " and \"id\" IS NULL ";
        return this;
    }

    public ConferenceMessage idIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"id\" IS NOT NULL ";
        return this;
    }

    public ConferenceMessage message_id_toxEq(String message_id_tox)
    {
        this.sql_where = this.sql_where + " and \"message_id_tox\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, message_id_tox));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage message_id_toxNotEq(String message_id_tox)
    {
        this.sql_where = this.sql_where + " and \"message_id_tox\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, message_id_tox));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage message_id_toxIsNull()
    {
        this.sql_where = this.sql_where + " and \"message_id_tox\" IS NULL ";
        return this;
    }

    public ConferenceMessage message_id_toxIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"message_id_tox\" IS NOT NULL ";
        return this;
    }

    public ConferenceMessage message_id_toxLike(String message_id_tox)
    {
        this.sql_where = this.sql_where + " and \"message_id_tox\" LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, message_id_tox));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage message_id_toxNotLike(String message_id_tox)
    {
        this.sql_where = this.sql_where + " and \"message_id_tox\" NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, message_id_tox));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage conference_identifierEq(String conference_identifier)
    {
        this.sql_where = this.sql_where + " and \"conference_identifier\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, conference_identifier));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage conference_identifierNotEq(String conference_identifier)
    {
        this.sql_where = this.sql_where + " and \"conference_identifier\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, conference_identifier));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage conference_identifierIsNull()
    {
        this.sql_where = this.sql_where + " and \"conference_identifier\" IS NULL ";
        return this;
    }

    public ConferenceMessage conference_identifierIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"conference_identifier\" IS NOT NULL ";
        return this;
    }

    public ConferenceMessage conference_identifierLike(String conference_identifier)
    {
        this.sql_where = this.sql_where + " and \"conference_identifier\" LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, conference_identifier));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage conference_identifierNotLike(String conference_identifier)
    {
        this.sql_where = this.sql_where + " and \"conference_identifier\" NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, conference_identifier));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage tox_peerpubkeyEq(String tox_peerpubkey)
    {
        this.sql_where = this.sql_where + " and \"tox_peerpubkey\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_peerpubkey));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage tox_peerpubkeyNotEq(String tox_peerpubkey)
    {
        this.sql_where = this.sql_where + " and \"tox_peerpubkey\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_peerpubkey));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage tox_peerpubkeyIsNull()
    {
        this.sql_where = this.sql_where + " and \"tox_peerpubkey\" IS NULL ";
        return this;
    }

    public ConferenceMessage tox_peerpubkeyIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"tox_peerpubkey\" IS NOT NULL ";
        return this;
    }

    public ConferenceMessage tox_peerpubkeyLike(String tox_peerpubkey)
    {
        this.sql_where = this.sql_where + " and \"tox_peerpubkey\" LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_peerpubkey));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage tox_peerpubkeyNotLike(String tox_peerpubkey)
    {
        this.sql_where = this.sql_where + " and \"tox_peerpubkey\" NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_peerpubkey));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage tox_peernameEq(String tox_peername)
    {
        this.sql_where = this.sql_where + " and \"tox_peername\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_peername));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage tox_peernameNotEq(String tox_peername)
    {
        this.sql_where = this.sql_where + " and \"tox_peername\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_peername));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage tox_peernameIsNull()
    {
        this.sql_where = this.sql_where + " and \"tox_peername\" IS NULL ";
        return this;
    }

    public ConferenceMessage tox_peernameIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"tox_peername\" IS NOT NULL ";
        return this;
    }

    public ConferenceMessage tox_peernameLike(String tox_peername)
    {
        this.sql_where = this.sql_where + " and \"tox_peername\" LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_peername));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage tox_peernameNotLike(String tox_peername)
    {
        this.sql_where = this.sql_where + " and \"tox_peername\" NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_peername));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage directionEq(int direction)
    {
        this.sql_where = this.sql_where + " and \"direction\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, direction));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage directionNotEq(int direction)
    {
        this.sql_where = this.sql_where + " and \"direction\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, direction));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage directionLt(int direction)
    {
        this.sql_where = this.sql_where + " and \"direction\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, direction));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage directionLe(int direction)
    {
        this.sql_where = this.sql_where + " and \"direction\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, direction));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage directionGt(int direction)
    {
        this.sql_where = this.sql_where + " and \"direction\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, direction));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage directionGe(int direction)
    {
        this.sql_where = this.sql_where + " and \"direction\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, direction));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage directionBetween(int direction1, int direction2)
    {
        this.sql_where = this.sql_where + " and \"direction\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and direction<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, direction1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, direction2));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage directionIsNull()
    {
        this.sql_where = this.sql_where + " and \"direction\" IS NULL ";
        return this;
    }

    public ConferenceMessage directionIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"direction\" IS NOT NULL ";
        return this;
    }

    public ConferenceMessage TOX_MESSAGE_TYPEEq(int TOX_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and \"TOX_MESSAGE_TYPE\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage TOX_MESSAGE_TYPENotEq(int TOX_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and \"TOX_MESSAGE_TYPE\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage TOX_MESSAGE_TYPELt(int TOX_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and \"TOX_MESSAGE_TYPE\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage TOX_MESSAGE_TYPELe(int TOX_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and \"TOX_MESSAGE_TYPE\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage TOX_MESSAGE_TYPEGt(int TOX_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and \"TOX_MESSAGE_TYPE\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage TOX_MESSAGE_TYPEGe(int TOX_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and \"TOX_MESSAGE_TYPE\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage TOX_MESSAGE_TYPEBetween(int TOX_MESSAGE_TYPE1, int TOX_MESSAGE_TYPE2)
    {
        this.sql_where = this.sql_where + " and \"TOX_MESSAGE_TYPE\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and TOX_MESSAGE_TYPE<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_MESSAGE_TYPE1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_MESSAGE_TYPE2));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage TOX_MESSAGE_TYPEIsNull()
    {
        this.sql_where = this.sql_where + " and \"TOX_MESSAGE_TYPE\" IS NULL ";
        return this;
    }

    public ConferenceMessage TOX_MESSAGE_TYPEIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"TOX_MESSAGE_TYPE\" IS NOT NULL ";
        return this;
    }

    public ConferenceMessage TRIFA_MESSAGE_TYPEEq(int TRIFA_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and \"TRIFA_MESSAGE_TYPE\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TRIFA_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage TRIFA_MESSAGE_TYPENotEq(int TRIFA_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and \"TRIFA_MESSAGE_TYPE\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TRIFA_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage TRIFA_MESSAGE_TYPELt(int TRIFA_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and \"TRIFA_MESSAGE_TYPE\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TRIFA_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage TRIFA_MESSAGE_TYPELe(int TRIFA_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and \"TRIFA_MESSAGE_TYPE\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TRIFA_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage TRIFA_MESSAGE_TYPEGt(int TRIFA_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and \"TRIFA_MESSAGE_TYPE\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TRIFA_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage TRIFA_MESSAGE_TYPEGe(int TRIFA_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and \"TRIFA_MESSAGE_TYPE\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TRIFA_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage TRIFA_MESSAGE_TYPEBetween(int TRIFA_MESSAGE_TYPE1, int TRIFA_MESSAGE_TYPE2)
    {
        this.sql_where = this.sql_where + " and \"TRIFA_MESSAGE_TYPE\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and TRIFA_MESSAGE_TYPE<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TRIFA_MESSAGE_TYPE1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TRIFA_MESSAGE_TYPE2));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage TRIFA_MESSAGE_TYPEIsNull()
    {
        this.sql_where = this.sql_where + " and \"TRIFA_MESSAGE_TYPE\" IS NULL ";
        return this;
    }

    public ConferenceMessage TRIFA_MESSAGE_TYPEIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"TRIFA_MESSAGE_TYPE\" IS NOT NULL ";
        return this;
    }

    public ConferenceMessage sent_timestampEq(long sent_timestamp)
    {
        this.sql_where = this.sql_where + " and \"sent_timestamp\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage sent_timestampNotEq(long sent_timestamp)
    {
        this.sql_where = this.sql_where + " and \"sent_timestamp\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage sent_timestampLt(long sent_timestamp)
    {
        this.sql_where = this.sql_where + " and \"sent_timestamp\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage sent_timestampLe(long sent_timestamp)
    {
        this.sql_where = this.sql_where + " and \"sent_timestamp\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage sent_timestampGt(long sent_timestamp)
    {
        this.sql_where = this.sql_where + " and \"sent_timestamp\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage sent_timestampGe(long sent_timestamp)
    {
        this.sql_where = this.sql_where + " and \"sent_timestamp\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage sent_timestampBetween(long sent_timestamp1, long sent_timestamp2)
    {
        this.sql_where = this.sql_where + " and \"sent_timestamp\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and sent_timestamp<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp2));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage sent_timestampIsNull()
    {
        this.sql_where = this.sql_where + " and \"sent_timestamp\" IS NULL ";
        return this;
    }

    public ConferenceMessage sent_timestampIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"sent_timestamp\" IS NOT NULL ";
        return this;
    }

    public ConferenceMessage rcvd_timestampEq(long rcvd_timestamp)
    {
        this.sql_where = this.sql_where + " and \"rcvd_timestamp\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage rcvd_timestampNotEq(long rcvd_timestamp)
    {
        this.sql_where = this.sql_where + " and \"rcvd_timestamp\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage rcvd_timestampLt(long rcvd_timestamp)
    {
        this.sql_where = this.sql_where + " and \"rcvd_timestamp\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage rcvd_timestampLe(long rcvd_timestamp)
    {
        this.sql_where = this.sql_where + " and \"rcvd_timestamp\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage rcvd_timestampGt(long rcvd_timestamp)
    {
        this.sql_where = this.sql_where + " and \"rcvd_timestamp\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage rcvd_timestampGe(long rcvd_timestamp)
    {
        this.sql_where = this.sql_where + " and \"rcvd_timestamp\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage rcvd_timestampBetween(long rcvd_timestamp1, long rcvd_timestamp2)
    {
        this.sql_where = this.sql_where + " and \"rcvd_timestamp\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and rcvd_timestamp<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp2));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage rcvd_timestampIsNull()
    {
        this.sql_where = this.sql_where + " and \"rcvd_timestamp\" IS NULL ";
        return this;
    }

    public ConferenceMessage rcvd_timestampIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"rcvd_timestamp\" IS NOT NULL ";
        return this;
    }

    public ConferenceMessage readEq(boolean read)
    {
        this.sql_where = this.sql_where + " and \"read\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, read));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage readNotEq(boolean read)
    {
        this.sql_where = this.sql_where + " and \"read\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, read));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage readIsNull()
    {
        this.sql_where = this.sql_where + " and \"read\" IS NULL ";
        return this;
    }

    public ConferenceMessage readIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"read\" IS NOT NULL ";
        return this;
    }

    public ConferenceMessage is_newEq(boolean is_new)
    {
        this.sql_where = this.sql_where + " and \"is_new\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, is_new));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage is_newNotEq(boolean is_new)
    {
        this.sql_where = this.sql_where + " and \"is_new\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, is_new));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage is_newIsNull()
    {
        this.sql_where = this.sql_where + " and \"is_new\" IS NULL ";
        return this;
    }

    public ConferenceMessage is_newIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"is_new\" IS NOT NULL ";
        return this;
    }

    public ConferenceMessage textEq(String text)
    {
        this.sql_where = this.sql_where + " and \"text\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, text));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage textNotEq(String text)
    {
        this.sql_where = this.sql_where + " and \"text\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, text));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage textIsNull()
    {
        this.sql_where = this.sql_where + " and \"text\" IS NULL ";
        return this;
    }

    public ConferenceMessage textIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"text\" IS NOT NULL ";
        return this;
    }

    public ConferenceMessage textLike(String text)
    {
        this.sql_where = this.sql_where + " and \"text\" LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, text));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage textNotLike(String text)
    {
        this.sql_where = this.sql_where + " and \"text\" NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, text));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage was_syncedEq(boolean was_synced)
    {
        this.sql_where = this.sql_where + " and \"was_synced\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, was_synced));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage was_syncedNotEq(boolean was_synced)
    {
        this.sql_where = this.sql_where + " and \"was_synced\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, was_synced));
        bind_where_count++;
        return this;
    }

    public ConferenceMessage was_syncedIsNull()
    {
        this.sql_where = this.sql_where + " and \"was_synced\" IS NULL ";
        return this;
    }

    public ConferenceMessage was_syncedIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"was_synced\" IS NOT NULL ";
        return this;
    }


    // ----------------- OrderBy funcs ------------------ //
    public ConferenceMessage orderByIdAsc()
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

    public ConferenceMessage orderByIdDesc()
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

    public ConferenceMessage orderByMessage_id_toxAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"message_id_tox\" ASC ";
        return this;
    }

    public ConferenceMessage orderByMessage_id_toxDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"message_id_tox\" DESC ";
        return this;
    }

    public ConferenceMessage orderByConference_identifierAsc()
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

    public ConferenceMessage orderByConference_identifierDesc()
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

    public ConferenceMessage orderByTox_peerpubkeyAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"tox_peerpubkey\" ASC ";
        return this;
    }

    public ConferenceMessage orderByTox_peerpubkeyDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"tox_peerpubkey\" DESC ";
        return this;
    }

    public ConferenceMessage orderByTox_peernameAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"tox_peername\" ASC ";
        return this;
    }

    public ConferenceMessage orderByTox_peernameDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"tox_peername\" DESC ";
        return this;
    }

    public ConferenceMessage orderByDirectionAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"direction\" ASC ";
        return this;
    }

    public ConferenceMessage orderByDirectionDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"direction\" DESC ";
        return this;
    }

    public ConferenceMessage orderByTOX_MESSAGE_TYPEAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"TOX_MESSAGE_TYPE\" ASC ";
        return this;
    }

    public ConferenceMessage orderByTOX_MESSAGE_TYPEDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"TOX_MESSAGE_TYPE\" DESC ";
        return this;
    }

    public ConferenceMessage orderByTRIFA_MESSAGE_TYPEAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"TRIFA_MESSAGE_TYPE\" ASC ";
        return this;
    }

    public ConferenceMessage orderByTRIFA_MESSAGE_TYPEDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"TRIFA_MESSAGE_TYPE\" DESC ";
        return this;
    }

    public ConferenceMessage orderBySent_timestampAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"sent_timestamp\" ASC ";
        return this;
    }

    public ConferenceMessage orderBySent_timestampDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"sent_timestamp\" DESC ";
        return this;
    }

    public ConferenceMessage orderByRcvd_timestampAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"rcvd_timestamp\" ASC ";
        return this;
    }

    public ConferenceMessage orderByRcvd_timestampDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"rcvd_timestamp\" DESC ";
        return this;
    }

    public ConferenceMessage orderByReadAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"read\" ASC ";
        return this;
    }

    public ConferenceMessage orderByReadDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"read\" DESC ";
        return this;
    }

    public ConferenceMessage orderByIs_newAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"is_new\" ASC ";
        return this;
    }

    public ConferenceMessage orderByIs_newDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"is_new\" DESC ";
        return this;
    }

    public ConferenceMessage orderByTextAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"text\" ASC ";
        return this;
    }

    public ConferenceMessage orderByTextDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"text\" DESC ";
        return this;
    }

    public ConferenceMessage orderByWas_syncedAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"was_synced\" ASC ";
        return this;
    }

    public ConferenceMessage orderByWas_syncedDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"was_synced\" DESC ";
        return this;
    }



}

