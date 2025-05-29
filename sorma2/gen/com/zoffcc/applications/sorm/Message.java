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
public class Message
{
    private static final String TAG = "DB.Message";
    @PrimaryKey(autoincrement = true, auto = true)
    public long id;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public long message_id;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public String tox_friendpubkey;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public int direction;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public int TOX_MESSAGE_TYPE;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public int TRIFA_MESSAGE_TYPE;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public int state;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public boolean ft_accepted;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public boolean ft_outgoing_started;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public long filedb_id;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public long filetransfer_id;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public long sent_timestamp;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public long sent_timestamp_ms;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public long rcvd_timestamp;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public long rcvd_timestamp_ms;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public boolean read;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public int send_retries;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public boolean is_new;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public String text;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public String filename_fullpath;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public String msg_id_hash;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public String raw_msgv2_bytes;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public int msg_version;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public int resend_count;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public boolean storage_frame_work;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public boolean ft_outgoing_queued;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public boolean msg_at_relay;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public String msg_idv3_hash;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public int sent_push;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public int filetransfer_kind;

    public static Message deep_copy(Message in)
    {
        Message out = new Message();
        out.id = in.id;
        out.message_id = in.message_id;
        out.tox_friendpubkey = in.tox_friendpubkey;
        out.direction = in.direction;
        out.TOX_MESSAGE_TYPE = in.TOX_MESSAGE_TYPE;
        out.TRIFA_MESSAGE_TYPE = in.TRIFA_MESSAGE_TYPE;
        out.state = in.state;
        out.ft_accepted = in.ft_accepted;
        out.ft_outgoing_started = in.ft_outgoing_started;
        out.filedb_id = in.filedb_id;
        out.filetransfer_id = in.filetransfer_id;
        out.sent_timestamp = in.sent_timestamp;
        out.sent_timestamp_ms = in.sent_timestamp_ms;
        out.rcvd_timestamp = in.rcvd_timestamp;
        out.rcvd_timestamp_ms = in.rcvd_timestamp_ms;
        out.read = in.read;
        out.send_retries = in.send_retries;
        out.is_new = in.is_new;
        out.text = in.text;
        out.filename_fullpath = in.filename_fullpath;
        out.msg_id_hash = in.msg_id_hash;
        out.raw_msgv2_bytes = in.raw_msgv2_bytes;
        out.msg_version = in.msg_version;
        out.resend_count = in.resend_count;
        out.storage_frame_work = in.storage_frame_work;
        out.ft_outgoing_queued = in.ft_outgoing_queued;
        out.msg_at_relay = in.msg_at_relay;
        out.msg_idv3_hash = in.msg_idv3_hash;
        out.sent_push = in.sent_push;
        out.filetransfer_kind = in.filetransfer_kind;

        return out;
    }

    @Override
    public String toString()
    {
        return "id=" + id + ", message_id=" + message_id + ", tox_friendpubkey=" + tox_friendpubkey + ", direction=" + direction + ", TOX_MESSAGE_TYPE=" + TOX_MESSAGE_TYPE + ", TRIFA_MESSAGE_TYPE=" + TRIFA_MESSAGE_TYPE + ", state=" + state + ", ft_accepted=" + ft_accepted + ", ft_outgoing_started=" + ft_outgoing_started + ", filedb_id=" + filedb_id + ", filetransfer_id=" + filetransfer_id + ", sent_timestamp=" + sent_timestamp + ", sent_timestamp_ms=" + sent_timestamp_ms + ", rcvd_timestamp=" + rcvd_timestamp + ", rcvd_timestamp_ms=" + rcvd_timestamp_ms + ", read=" + read + ", send_retries=" + send_retries + ", is_new=" + is_new + ", text=" + text + ", filename_fullpath=" + filename_fullpath + ", msg_id_hash=" + msg_id_hash + ", raw_msgv2_bytes=" + raw_msgv2_bytes + ", msg_version=" + msg_version + ", resend_count=" + resend_count + ", storage_frame_work=" + storage_frame_work + ", ft_outgoing_queued=" + ft_outgoing_queued + ", msg_at_relay=" + msg_at_relay + ", msg_idv3_hash=" + msg_idv3_hash + ", sent_push=" + sent_push + ", filetransfer_kind=" + filetransfer_kind;
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

    public List<Message> toList()
    {
        List<Message> list = new ArrayList<>();
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
                Message out = new Message();
                out.id = rs.getLong("id");
                out.message_id = rs.getLong("message_id");
                out.tox_friendpubkey = rs.getString("tox_friendpubkey");
                out.direction = rs.getInt("direction");
                out.TOX_MESSAGE_TYPE = rs.getInt("TOX_MESSAGE_TYPE");
                out.TRIFA_MESSAGE_TYPE = rs.getInt("TRIFA_MESSAGE_TYPE");
                out.state = rs.getInt("state");
                out.ft_accepted = rs.getBoolean("ft_accepted");
                out.ft_outgoing_started = rs.getBoolean("ft_outgoing_started");
                out.filedb_id = rs.getLong("filedb_id");
                out.filetransfer_id = rs.getLong("filetransfer_id");
                out.sent_timestamp = rs.getLong("sent_timestamp");
                out.sent_timestamp_ms = rs.getLong("sent_timestamp_ms");
                out.rcvd_timestamp = rs.getLong("rcvd_timestamp");
                out.rcvd_timestamp_ms = rs.getLong("rcvd_timestamp_ms");
                out.read = rs.getBoolean("read");
                out.send_retries = rs.getInt("send_retries");
                out.is_new = rs.getBoolean("is_new");
                out.text = rs.getString("text");
                out.filename_fullpath = rs.getString("filename_fullpath");
                out.msg_id_hash = rs.getString("msg_id_hash");
                out.raw_msgv2_bytes = rs.getString("raw_msgv2_bytes");
                out.msg_version = rs.getInt("msg_version");
                out.resend_count = rs.getInt("resend_count");
                out.storage_frame_work = rs.getBoolean("storage_frame_work");
                out.ft_outgoing_queued = rs.getBoolean("ft_outgoing_queued");
                out.msg_at_relay = rs.getBoolean("msg_at_relay");
                out.msg_idv3_hash = rs.getString("msg_idv3_hash");
                out.sent_push = rs.getInt("sent_push");
                out.filetransfer_kind = rs.getInt("filetransfer_kind");

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
                    + "\"message_id\""
                    + ",\"tox_friendpubkey\""
                    + ",\"direction\""
                    + ",\"TOX_MESSAGE_TYPE\""
                    + ",\"TRIFA_MESSAGE_TYPE\""
                    + ",\"state\""
                    + ",\"ft_accepted\""
                    + ",\"ft_outgoing_started\""
                    + ",\"filedb_id\""
                    + ",\"filetransfer_id\""
                    + ",\"sent_timestamp\""
                    + ",\"sent_timestamp_ms\""
                    + ",\"rcvd_timestamp\""
                    + ",\"rcvd_timestamp_ms\""
                    + ",\"read\""
                    + ",\"send_retries\""
                    + ",\"is_new\""
                    + ",\"text\""
                    + ",\"filename_fullpath\""
                    + ",\"msg_id_hash\""
                    + ",\"raw_msgv2_bytes\""
                    + ",\"msg_version\""
                    + ",\"resend_count\""
                    + ",\"storage_frame_work\""
                    + ",\"ft_outgoing_queued\""
                    + ",\"msg_at_relay\""
                    + ",\"msg_idv3_hash\""
                    + ",\"sent_push\""
                    + ",\"filetransfer_kind\""
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
                    + ",?14"
                    + ",?15"
                    + ",?16"
                    + ",?17"
                    + ",?18"
                    + ",?19"
                    + ",?20"
                    + ",?21"
                    + ",?22"
                    + ",?23"
                    + ",?24"
                    + ",?25"
                    + ",?26"
                    + ",?27"
                    + ",?28"
                    + ",?29"
                    + ")";

            insert_pstmt = sqldb.prepareStatement(insert_pstmt_sql);
            insert_pstmt.clearParameters();

            insert_pstmt.setLong(1, this.message_id);
            insert_pstmt.setString(2, this.tox_friendpubkey);
            insert_pstmt.setInt(3, this.direction);
            insert_pstmt.setInt(4, this.TOX_MESSAGE_TYPE);
            insert_pstmt.setInt(5, this.TRIFA_MESSAGE_TYPE);
            insert_pstmt.setInt(6, this.state);
            insert_pstmt.setBoolean(7, this.ft_accepted);
            insert_pstmt.setBoolean(8, this.ft_outgoing_started);
            insert_pstmt.setLong(9, this.filedb_id);
            insert_pstmt.setLong(10, this.filetransfer_id);
            insert_pstmt.setLong(11, this.sent_timestamp);
            insert_pstmt.setLong(12, this.sent_timestamp_ms);
            insert_pstmt.setLong(13, this.rcvd_timestamp);
            insert_pstmt.setLong(14, this.rcvd_timestamp_ms);
            insert_pstmt.setBoolean(15, this.read);
            insert_pstmt.setInt(16, this.send_retries);
            insert_pstmt.setBoolean(17, this.is_new);
            insert_pstmt.setString(18, this.text);
            insert_pstmt.setString(19, this.filename_fullpath);
            insert_pstmt.setString(20, this.msg_id_hash);
            insert_pstmt.setString(21, this.raw_msgv2_bytes);
            insert_pstmt.setInt(22, this.msg_version);
            insert_pstmt.setInt(23, this.resend_count);
            insert_pstmt.setBoolean(24, this.storage_frame_work);
            insert_pstmt.setBoolean(25, this.ft_outgoing_queued);
            insert_pstmt.setBoolean(26, this.msg_at_relay);
            insert_pstmt.setString(27, this.msg_idv3_hash);
            insert_pstmt.setInt(28, this.sent_push);
            insert_pstmt.setInt(29, this.filetransfer_kind);
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

    public Message get(int i)
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

    public Message limit(int rowcount)
    {
        this.sql_limit = " limit " + rowcount + " ";
        return this;
    }

    public Message limit(int rowcount, int offset)
    {
        this.sql_limit = " limit " + offset + " , " + rowcount;
        return this;
    }

    // ----------------------------------- //
    // ----------------------------------- //
    // ----------------------------------- //


    // ----------------- Set funcs ---------------------- //
    public Message id(long id)
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

    public Message message_id(long message_id)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"message_id\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, message_id));
        bind_set_count++;
        return this;
    }

    public Message tox_friendpubkey(String tox_friendpubkey)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"tox_friendpubkey\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_friendpubkey));
        bind_set_count++;
        return this;
    }

    public Message direction(int direction)
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

    public Message TOX_MESSAGE_TYPE(int TOX_MESSAGE_TYPE)
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

    public Message TRIFA_MESSAGE_TYPE(int TRIFA_MESSAGE_TYPE)
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

    public Message state(int state)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"state\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, state));
        bind_set_count++;
        return this;
    }

    public Message ft_accepted(boolean ft_accepted)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"ft_accepted\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, ft_accepted));
        bind_set_count++;
        return this;
    }

    public Message ft_outgoing_started(boolean ft_outgoing_started)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"ft_outgoing_started\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, ft_outgoing_started));
        bind_set_count++;
        return this;
    }

    public Message filedb_id(long filedb_id)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"filedb_id\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filedb_id));
        bind_set_count++;
        return this;
    }

    public Message filetransfer_id(long filetransfer_id)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"filetransfer_id\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filetransfer_id));
        bind_set_count++;
        return this;
    }

    public Message sent_timestamp(long sent_timestamp)
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

    public Message sent_timestamp_ms(long sent_timestamp_ms)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"sent_timestamp_ms\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp_ms));
        bind_set_count++;
        return this;
    }

    public Message rcvd_timestamp(long rcvd_timestamp)
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

    public Message rcvd_timestamp_ms(long rcvd_timestamp_ms)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"rcvd_timestamp_ms\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp_ms));
        bind_set_count++;
        return this;
    }

    public Message read(boolean read)
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

    public Message send_retries(int send_retries)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"send_retries\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, send_retries));
        bind_set_count++;
        return this;
    }

    public Message is_new(boolean is_new)
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

    public Message text(String text)
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

    public Message filename_fullpath(String filename_fullpath)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"filename_fullpath\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, filename_fullpath));
        bind_set_count++;
        return this;
    }

    public Message msg_id_hash(String msg_id_hash)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"msg_id_hash\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, msg_id_hash));
        bind_set_count++;
        return this;
    }

    public Message raw_msgv2_bytes(String raw_msgv2_bytes)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"raw_msgv2_bytes\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, raw_msgv2_bytes));
        bind_set_count++;
        return this;
    }

    public Message msg_version(int msg_version)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"msg_version\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, msg_version));
        bind_set_count++;
        return this;
    }

    public Message resend_count(int resend_count)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"resend_count\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, resend_count));
        bind_set_count++;
        return this;
    }

    public Message storage_frame_work(boolean storage_frame_work)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"storage_frame_work\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, storage_frame_work));
        bind_set_count++;
        return this;
    }

    public Message ft_outgoing_queued(boolean ft_outgoing_queued)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"ft_outgoing_queued\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, ft_outgoing_queued));
        bind_set_count++;
        return this;
    }

    public Message msg_at_relay(boolean msg_at_relay)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"msg_at_relay\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, msg_at_relay));
        bind_set_count++;
        return this;
    }

    public Message msg_idv3_hash(String msg_idv3_hash)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"msg_idv3_hash\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, msg_idv3_hash));
        bind_set_count++;
        return this;
    }

    public Message sent_push(int sent_push)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"sent_push\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, sent_push));
        bind_set_count++;
        return this;
    }

    public Message filetransfer_kind(int filetransfer_kind)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"filetransfer_kind\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, filetransfer_kind));
        bind_set_count++;
        return this;
    }


    // ----------------- Eq/Gt/Lt funcs ----------------- //
    public Message idEq(long id)
    {
        this.sql_where = this.sql_where + " and \"id\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public Message idNotEq(long id)
    {
        this.sql_where = this.sql_where + " and \"id\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public Message idLt(long id)
    {
        this.sql_where = this.sql_where + " and \"id\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public Message idLe(long id)
    {
        this.sql_where = this.sql_where + " and \"id\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public Message idGt(long id)
    {
        this.sql_where = this.sql_where + " and \"id\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public Message idGe(long id)
    {
        this.sql_where = this.sql_where + " and \"id\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public Message idBetween(long id1, long id2)
    {
        this.sql_where = this.sql_where + " and \"id\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and id<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id2));
        bind_where_count++;
        return this;
    }

    public Message idIsNull()
    {
        this.sql_where = this.sql_where + " and \"id\" IS NULL ";
        return this;
    }

    public Message idIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"id\" IS NOT NULL ";
        return this;
    }

    public Message message_idEq(long message_id)
    {
        this.sql_where = this.sql_where + " and \"message_id\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, message_id));
        bind_where_count++;
        return this;
    }

    public Message message_idNotEq(long message_id)
    {
        this.sql_where = this.sql_where + " and \"message_id\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, message_id));
        bind_where_count++;
        return this;
    }

    public Message message_idLt(long message_id)
    {
        this.sql_where = this.sql_where + " and \"message_id\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, message_id));
        bind_where_count++;
        return this;
    }

    public Message message_idLe(long message_id)
    {
        this.sql_where = this.sql_where + " and \"message_id\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, message_id));
        bind_where_count++;
        return this;
    }

    public Message message_idGt(long message_id)
    {
        this.sql_where = this.sql_where + " and \"message_id\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, message_id));
        bind_where_count++;
        return this;
    }

    public Message message_idGe(long message_id)
    {
        this.sql_where = this.sql_where + " and \"message_id\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, message_id));
        bind_where_count++;
        return this;
    }

    public Message message_idBetween(long message_id1, long message_id2)
    {
        this.sql_where = this.sql_where + " and \"message_id\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and message_id<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, message_id1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, message_id2));
        bind_where_count++;
        return this;
    }

    public Message message_idIsNull()
    {
        this.sql_where = this.sql_where + " and \"message_id\" IS NULL ";
        return this;
    }

    public Message message_idIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"message_id\" IS NOT NULL ";
        return this;
    }

    public Message tox_friendpubkeyEq(String tox_friendpubkey)
    {
        this.sql_where = this.sql_where + " and \"tox_friendpubkey\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_friendpubkey));
        bind_where_count++;
        return this;
    }

    public Message tox_friendpubkeyNotEq(String tox_friendpubkey)
    {
        this.sql_where = this.sql_where + " and \"tox_friendpubkey\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_friendpubkey));
        bind_where_count++;
        return this;
    }

    public Message tox_friendpubkeyIsNull()
    {
        this.sql_where = this.sql_where + " and \"tox_friendpubkey\" IS NULL ";
        return this;
    }

    public Message tox_friendpubkeyIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"tox_friendpubkey\" IS NOT NULL ";
        return this;
    }

    public Message tox_friendpubkeyLike(String tox_friendpubkey)
    {
        this.sql_where = this.sql_where + " and \"tox_friendpubkey\" LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_friendpubkey));
        bind_where_count++;
        return this;
    }

    public Message tox_friendpubkeyNotLike(String tox_friendpubkey)
    {
        this.sql_where = this.sql_where + " and \"tox_friendpubkey\" NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_friendpubkey));
        bind_where_count++;
        return this;
    }

    public Message directionEq(int direction)
    {
        this.sql_where = this.sql_where + " and \"direction\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, direction));
        bind_where_count++;
        return this;
    }

    public Message directionNotEq(int direction)
    {
        this.sql_where = this.sql_where + " and \"direction\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, direction));
        bind_where_count++;
        return this;
    }

    public Message directionLt(int direction)
    {
        this.sql_where = this.sql_where + " and \"direction\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, direction));
        bind_where_count++;
        return this;
    }

    public Message directionLe(int direction)
    {
        this.sql_where = this.sql_where + " and \"direction\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, direction));
        bind_where_count++;
        return this;
    }

    public Message directionGt(int direction)
    {
        this.sql_where = this.sql_where + " and \"direction\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, direction));
        bind_where_count++;
        return this;
    }

    public Message directionGe(int direction)
    {
        this.sql_where = this.sql_where + " and \"direction\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, direction));
        bind_where_count++;
        return this;
    }

    public Message directionBetween(int direction1, int direction2)
    {
        this.sql_where = this.sql_where + " and \"direction\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and direction<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, direction1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, direction2));
        bind_where_count++;
        return this;
    }

    public Message directionIsNull()
    {
        this.sql_where = this.sql_where + " and \"direction\" IS NULL ";
        return this;
    }

    public Message directionIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"direction\" IS NOT NULL ";
        return this;
    }

    public Message TOX_MESSAGE_TYPEEq(int TOX_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and \"TOX_MESSAGE_TYPE\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public Message TOX_MESSAGE_TYPENotEq(int TOX_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and \"TOX_MESSAGE_TYPE\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public Message TOX_MESSAGE_TYPELt(int TOX_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and \"TOX_MESSAGE_TYPE\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public Message TOX_MESSAGE_TYPELe(int TOX_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and \"TOX_MESSAGE_TYPE\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public Message TOX_MESSAGE_TYPEGt(int TOX_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and \"TOX_MESSAGE_TYPE\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public Message TOX_MESSAGE_TYPEGe(int TOX_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and \"TOX_MESSAGE_TYPE\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public Message TOX_MESSAGE_TYPEBetween(int TOX_MESSAGE_TYPE1, int TOX_MESSAGE_TYPE2)
    {
        this.sql_where = this.sql_where + " and \"TOX_MESSAGE_TYPE\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and TOX_MESSAGE_TYPE<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_MESSAGE_TYPE1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_MESSAGE_TYPE2));
        bind_where_count++;
        return this;
    }

    public Message TOX_MESSAGE_TYPEIsNull()
    {
        this.sql_where = this.sql_where + " and \"TOX_MESSAGE_TYPE\" IS NULL ";
        return this;
    }

    public Message TOX_MESSAGE_TYPEIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"TOX_MESSAGE_TYPE\" IS NOT NULL ";
        return this;
    }

    public Message TRIFA_MESSAGE_TYPEEq(int TRIFA_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and \"TRIFA_MESSAGE_TYPE\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TRIFA_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public Message TRIFA_MESSAGE_TYPENotEq(int TRIFA_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and \"TRIFA_MESSAGE_TYPE\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TRIFA_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public Message TRIFA_MESSAGE_TYPELt(int TRIFA_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and \"TRIFA_MESSAGE_TYPE\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TRIFA_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public Message TRIFA_MESSAGE_TYPELe(int TRIFA_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and \"TRIFA_MESSAGE_TYPE\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TRIFA_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public Message TRIFA_MESSAGE_TYPEGt(int TRIFA_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and \"TRIFA_MESSAGE_TYPE\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TRIFA_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public Message TRIFA_MESSAGE_TYPEGe(int TRIFA_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and \"TRIFA_MESSAGE_TYPE\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TRIFA_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public Message TRIFA_MESSAGE_TYPEBetween(int TRIFA_MESSAGE_TYPE1, int TRIFA_MESSAGE_TYPE2)
    {
        this.sql_where = this.sql_where + " and \"TRIFA_MESSAGE_TYPE\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and TRIFA_MESSAGE_TYPE<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TRIFA_MESSAGE_TYPE1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TRIFA_MESSAGE_TYPE2));
        bind_where_count++;
        return this;
    }

    public Message TRIFA_MESSAGE_TYPEIsNull()
    {
        this.sql_where = this.sql_where + " and \"TRIFA_MESSAGE_TYPE\" IS NULL ";
        return this;
    }

    public Message TRIFA_MESSAGE_TYPEIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"TRIFA_MESSAGE_TYPE\" IS NOT NULL ";
        return this;
    }

    public Message stateEq(int state)
    {
        this.sql_where = this.sql_where + " and \"state\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, state));
        bind_where_count++;
        return this;
    }

    public Message stateNotEq(int state)
    {
        this.sql_where = this.sql_where + " and \"state\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, state));
        bind_where_count++;
        return this;
    }

    public Message stateLt(int state)
    {
        this.sql_where = this.sql_where + " and \"state\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, state));
        bind_where_count++;
        return this;
    }

    public Message stateLe(int state)
    {
        this.sql_where = this.sql_where + " and \"state\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, state));
        bind_where_count++;
        return this;
    }

    public Message stateGt(int state)
    {
        this.sql_where = this.sql_where + " and \"state\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, state));
        bind_where_count++;
        return this;
    }

    public Message stateGe(int state)
    {
        this.sql_where = this.sql_where + " and \"state\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, state));
        bind_where_count++;
        return this;
    }

    public Message stateBetween(int state1, int state2)
    {
        this.sql_where = this.sql_where + " and \"state\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and state<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, state1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, state2));
        bind_where_count++;
        return this;
    }

    public Message stateIsNull()
    {
        this.sql_where = this.sql_where + " and \"state\" IS NULL ";
        return this;
    }

    public Message stateIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"state\" IS NOT NULL ";
        return this;
    }

    public Message ft_acceptedEq(boolean ft_accepted)
    {
        this.sql_where = this.sql_where + " and \"ft_accepted\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, ft_accepted));
        bind_where_count++;
        return this;
    }

    public Message ft_acceptedNotEq(boolean ft_accepted)
    {
        this.sql_where = this.sql_where + " and \"ft_accepted\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, ft_accepted));
        bind_where_count++;
        return this;
    }

    public Message ft_acceptedIsNull()
    {
        this.sql_where = this.sql_where + " and \"ft_accepted\" IS NULL ";
        return this;
    }

    public Message ft_acceptedIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"ft_accepted\" IS NOT NULL ";
        return this;
    }

    public Message ft_outgoing_startedEq(boolean ft_outgoing_started)
    {
        this.sql_where = this.sql_where + " and \"ft_outgoing_started\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, ft_outgoing_started));
        bind_where_count++;
        return this;
    }

    public Message ft_outgoing_startedNotEq(boolean ft_outgoing_started)
    {
        this.sql_where = this.sql_where + " and \"ft_outgoing_started\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, ft_outgoing_started));
        bind_where_count++;
        return this;
    }

    public Message ft_outgoing_startedIsNull()
    {
        this.sql_where = this.sql_where + " and \"ft_outgoing_started\" IS NULL ";
        return this;
    }

    public Message ft_outgoing_startedIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"ft_outgoing_started\" IS NOT NULL ";
        return this;
    }

    public Message filedb_idEq(long filedb_id)
    {
        this.sql_where = this.sql_where + " and \"filedb_id\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filedb_id));
        bind_where_count++;
        return this;
    }

    public Message filedb_idNotEq(long filedb_id)
    {
        this.sql_where = this.sql_where + " and \"filedb_id\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filedb_id));
        bind_where_count++;
        return this;
    }

    public Message filedb_idLt(long filedb_id)
    {
        this.sql_where = this.sql_where + " and \"filedb_id\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filedb_id));
        bind_where_count++;
        return this;
    }

    public Message filedb_idLe(long filedb_id)
    {
        this.sql_where = this.sql_where + " and \"filedb_id\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filedb_id));
        bind_where_count++;
        return this;
    }

    public Message filedb_idGt(long filedb_id)
    {
        this.sql_where = this.sql_where + " and \"filedb_id\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filedb_id));
        bind_where_count++;
        return this;
    }

    public Message filedb_idGe(long filedb_id)
    {
        this.sql_where = this.sql_where + " and \"filedb_id\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filedb_id));
        bind_where_count++;
        return this;
    }

    public Message filedb_idBetween(long filedb_id1, long filedb_id2)
    {
        this.sql_where = this.sql_where + " and \"filedb_id\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and filedb_id<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filedb_id1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filedb_id2));
        bind_where_count++;
        return this;
    }

    public Message filedb_idIsNull()
    {
        this.sql_where = this.sql_where + " and \"filedb_id\" IS NULL ";
        return this;
    }

    public Message filedb_idIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"filedb_id\" IS NOT NULL ";
        return this;
    }

    public Message filetransfer_idEq(long filetransfer_id)
    {
        this.sql_where = this.sql_where + " and \"filetransfer_id\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filetransfer_id));
        bind_where_count++;
        return this;
    }

    public Message filetransfer_idNotEq(long filetransfer_id)
    {
        this.sql_where = this.sql_where + " and \"filetransfer_id\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filetransfer_id));
        bind_where_count++;
        return this;
    }

    public Message filetransfer_idLt(long filetransfer_id)
    {
        this.sql_where = this.sql_where + " and \"filetransfer_id\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filetransfer_id));
        bind_where_count++;
        return this;
    }

    public Message filetransfer_idLe(long filetransfer_id)
    {
        this.sql_where = this.sql_where + " and \"filetransfer_id\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filetransfer_id));
        bind_where_count++;
        return this;
    }

    public Message filetransfer_idGt(long filetransfer_id)
    {
        this.sql_where = this.sql_where + " and \"filetransfer_id\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filetransfer_id));
        bind_where_count++;
        return this;
    }

    public Message filetransfer_idGe(long filetransfer_id)
    {
        this.sql_where = this.sql_where + " and \"filetransfer_id\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filetransfer_id));
        bind_where_count++;
        return this;
    }

    public Message filetransfer_idBetween(long filetransfer_id1, long filetransfer_id2)
    {
        this.sql_where = this.sql_where + " and \"filetransfer_id\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and filetransfer_id<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filetransfer_id1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filetransfer_id2));
        bind_where_count++;
        return this;
    }

    public Message filetransfer_idIsNull()
    {
        this.sql_where = this.sql_where + " and \"filetransfer_id\" IS NULL ";
        return this;
    }

    public Message filetransfer_idIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"filetransfer_id\" IS NOT NULL ";
        return this;
    }

    public Message sent_timestampEq(long sent_timestamp)
    {
        this.sql_where = this.sql_where + " and \"sent_timestamp\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp));
        bind_where_count++;
        return this;
    }

    public Message sent_timestampNotEq(long sent_timestamp)
    {
        this.sql_where = this.sql_where + " and \"sent_timestamp\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp));
        bind_where_count++;
        return this;
    }

    public Message sent_timestampLt(long sent_timestamp)
    {
        this.sql_where = this.sql_where + " and \"sent_timestamp\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp));
        bind_where_count++;
        return this;
    }

    public Message sent_timestampLe(long sent_timestamp)
    {
        this.sql_where = this.sql_where + " and \"sent_timestamp\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp));
        bind_where_count++;
        return this;
    }

    public Message sent_timestampGt(long sent_timestamp)
    {
        this.sql_where = this.sql_where + " and \"sent_timestamp\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp));
        bind_where_count++;
        return this;
    }

    public Message sent_timestampGe(long sent_timestamp)
    {
        this.sql_where = this.sql_where + " and \"sent_timestamp\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp));
        bind_where_count++;
        return this;
    }

    public Message sent_timestampBetween(long sent_timestamp1, long sent_timestamp2)
    {
        this.sql_where = this.sql_where + " and \"sent_timestamp\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and sent_timestamp<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp2));
        bind_where_count++;
        return this;
    }

    public Message sent_timestampIsNull()
    {
        this.sql_where = this.sql_where + " and \"sent_timestamp\" IS NULL ";
        return this;
    }

    public Message sent_timestampIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"sent_timestamp\" IS NOT NULL ";
        return this;
    }

    public Message sent_timestamp_msEq(long sent_timestamp_ms)
    {
        this.sql_where = this.sql_where + " and \"sent_timestamp_ms\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp_ms));
        bind_where_count++;
        return this;
    }

    public Message sent_timestamp_msNotEq(long sent_timestamp_ms)
    {
        this.sql_where = this.sql_where + " and \"sent_timestamp_ms\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp_ms));
        bind_where_count++;
        return this;
    }

    public Message sent_timestamp_msLt(long sent_timestamp_ms)
    {
        this.sql_where = this.sql_where + " and \"sent_timestamp_ms\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp_ms));
        bind_where_count++;
        return this;
    }

    public Message sent_timestamp_msLe(long sent_timestamp_ms)
    {
        this.sql_where = this.sql_where + " and \"sent_timestamp_ms\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp_ms));
        bind_where_count++;
        return this;
    }

    public Message sent_timestamp_msGt(long sent_timestamp_ms)
    {
        this.sql_where = this.sql_where + " and \"sent_timestamp_ms\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp_ms));
        bind_where_count++;
        return this;
    }

    public Message sent_timestamp_msGe(long sent_timestamp_ms)
    {
        this.sql_where = this.sql_where + " and \"sent_timestamp_ms\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp_ms));
        bind_where_count++;
        return this;
    }

    public Message sent_timestamp_msBetween(long sent_timestamp_ms1, long sent_timestamp_ms2)
    {
        this.sql_where = this.sql_where + " and \"sent_timestamp_ms\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and sent_timestamp_ms<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp_ms1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp_ms2));
        bind_where_count++;
        return this;
    }

    public Message sent_timestamp_msIsNull()
    {
        this.sql_where = this.sql_where + " and \"sent_timestamp_ms\" IS NULL ";
        return this;
    }

    public Message sent_timestamp_msIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"sent_timestamp_ms\" IS NOT NULL ";
        return this;
    }

    public Message rcvd_timestampEq(long rcvd_timestamp)
    {
        this.sql_where = this.sql_where + " and \"rcvd_timestamp\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp));
        bind_where_count++;
        return this;
    }

    public Message rcvd_timestampNotEq(long rcvd_timestamp)
    {
        this.sql_where = this.sql_where + " and \"rcvd_timestamp\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp));
        bind_where_count++;
        return this;
    }

    public Message rcvd_timestampLt(long rcvd_timestamp)
    {
        this.sql_where = this.sql_where + " and \"rcvd_timestamp\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp));
        bind_where_count++;
        return this;
    }

    public Message rcvd_timestampLe(long rcvd_timestamp)
    {
        this.sql_where = this.sql_where + " and \"rcvd_timestamp\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp));
        bind_where_count++;
        return this;
    }

    public Message rcvd_timestampGt(long rcvd_timestamp)
    {
        this.sql_where = this.sql_where + " and \"rcvd_timestamp\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp));
        bind_where_count++;
        return this;
    }

    public Message rcvd_timestampGe(long rcvd_timestamp)
    {
        this.sql_where = this.sql_where + " and \"rcvd_timestamp\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp));
        bind_where_count++;
        return this;
    }

    public Message rcvd_timestampBetween(long rcvd_timestamp1, long rcvd_timestamp2)
    {
        this.sql_where = this.sql_where + " and \"rcvd_timestamp\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and rcvd_timestamp<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp2));
        bind_where_count++;
        return this;
    }

    public Message rcvd_timestampIsNull()
    {
        this.sql_where = this.sql_where + " and \"rcvd_timestamp\" IS NULL ";
        return this;
    }

    public Message rcvd_timestampIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"rcvd_timestamp\" IS NOT NULL ";
        return this;
    }

    public Message rcvd_timestamp_msEq(long rcvd_timestamp_ms)
    {
        this.sql_where = this.sql_where + " and \"rcvd_timestamp_ms\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp_ms));
        bind_where_count++;
        return this;
    }

    public Message rcvd_timestamp_msNotEq(long rcvd_timestamp_ms)
    {
        this.sql_where = this.sql_where + " and \"rcvd_timestamp_ms\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp_ms));
        bind_where_count++;
        return this;
    }

    public Message rcvd_timestamp_msLt(long rcvd_timestamp_ms)
    {
        this.sql_where = this.sql_where + " and \"rcvd_timestamp_ms\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp_ms));
        bind_where_count++;
        return this;
    }

    public Message rcvd_timestamp_msLe(long rcvd_timestamp_ms)
    {
        this.sql_where = this.sql_where + " and \"rcvd_timestamp_ms\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp_ms));
        bind_where_count++;
        return this;
    }

    public Message rcvd_timestamp_msGt(long rcvd_timestamp_ms)
    {
        this.sql_where = this.sql_where + " and \"rcvd_timestamp_ms\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp_ms));
        bind_where_count++;
        return this;
    }

    public Message rcvd_timestamp_msGe(long rcvd_timestamp_ms)
    {
        this.sql_where = this.sql_where + " and \"rcvd_timestamp_ms\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp_ms));
        bind_where_count++;
        return this;
    }

    public Message rcvd_timestamp_msBetween(long rcvd_timestamp_ms1, long rcvd_timestamp_ms2)
    {
        this.sql_where = this.sql_where + " and \"rcvd_timestamp_ms\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and rcvd_timestamp_ms<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp_ms1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp_ms2));
        bind_where_count++;
        return this;
    }

    public Message rcvd_timestamp_msIsNull()
    {
        this.sql_where = this.sql_where + " and \"rcvd_timestamp_ms\" IS NULL ";
        return this;
    }

    public Message rcvd_timestamp_msIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"rcvd_timestamp_ms\" IS NOT NULL ";
        return this;
    }

    public Message readEq(boolean read)
    {
        this.sql_where = this.sql_where + " and \"read\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, read));
        bind_where_count++;
        return this;
    }

    public Message readNotEq(boolean read)
    {
        this.sql_where = this.sql_where + " and \"read\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, read));
        bind_where_count++;
        return this;
    }

    public Message readIsNull()
    {
        this.sql_where = this.sql_where + " and \"read\" IS NULL ";
        return this;
    }

    public Message readIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"read\" IS NOT NULL ";
        return this;
    }

    public Message send_retriesEq(int send_retries)
    {
        this.sql_where = this.sql_where + " and \"send_retries\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, send_retries));
        bind_where_count++;
        return this;
    }

    public Message send_retriesNotEq(int send_retries)
    {
        this.sql_where = this.sql_where + " and \"send_retries\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, send_retries));
        bind_where_count++;
        return this;
    }

    public Message send_retriesLt(int send_retries)
    {
        this.sql_where = this.sql_where + " and \"send_retries\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, send_retries));
        bind_where_count++;
        return this;
    }

    public Message send_retriesLe(int send_retries)
    {
        this.sql_where = this.sql_where + " and \"send_retries\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, send_retries));
        bind_where_count++;
        return this;
    }

    public Message send_retriesGt(int send_retries)
    {
        this.sql_where = this.sql_where + " and \"send_retries\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, send_retries));
        bind_where_count++;
        return this;
    }

    public Message send_retriesGe(int send_retries)
    {
        this.sql_where = this.sql_where + " and \"send_retries\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, send_retries));
        bind_where_count++;
        return this;
    }

    public Message send_retriesBetween(int send_retries1, int send_retries2)
    {
        this.sql_where = this.sql_where + " and \"send_retries\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and send_retries<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, send_retries1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, send_retries2));
        bind_where_count++;
        return this;
    }

    public Message send_retriesIsNull()
    {
        this.sql_where = this.sql_where + " and \"send_retries\" IS NULL ";
        return this;
    }

    public Message send_retriesIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"send_retries\" IS NOT NULL ";
        return this;
    }

    public Message is_newEq(boolean is_new)
    {
        this.sql_where = this.sql_where + " and \"is_new\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, is_new));
        bind_where_count++;
        return this;
    }

    public Message is_newNotEq(boolean is_new)
    {
        this.sql_where = this.sql_where + " and \"is_new\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, is_new));
        bind_where_count++;
        return this;
    }

    public Message is_newIsNull()
    {
        this.sql_where = this.sql_where + " and \"is_new\" IS NULL ";
        return this;
    }

    public Message is_newIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"is_new\" IS NOT NULL ";
        return this;
    }

    public Message textEq(String text)
    {
        this.sql_where = this.sql_where + " and \"text\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, text));
        bind_where_count++;
        return this;
    }

    public Message textNotEq(String text)
    {
        this.sql_where = this.sql_where + " and \"text\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, text));
        bind_where_count++;
        return this;
    }

    public Message textIsNull()
    {
        this.sql_where = this.sql_where + " and \"text\" IS NULL ";
        return this;
    }

    public Message textIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"text\" IS NOT NULL ";
        return this;
    }

    public Message textLike(String text)
    {
        this.sql_where = this.sql_where + " and \"text\" LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, text));
        bind_where_count++;
        return this;
    }

    public Message textNotLike(String text)
    {
        this.sql_where = this.sql_where + " and \"text\" NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, text));
        bind_where_count++;
        return this;
    }

    public Message filename_fullpathEq(String filename_fullpath)
    {
        this.sql_where = this.sql_where + " and \"filename_fullpath\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, filename_fullpath));
        bind_where_count++;
        return this;
    }

    public Message filename_fullpathNotEq(String filename_fullpath)
    {
        this.sql_where = this.sql_where + " and \"filename_fullpath\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, filename_fullpath));
        bind_where_count++;
        return this;
    }

    public Message filename_fullpathIsNull()
    {
        this.sql_where = this.sql_where + " and \"filename_fullpath\" IS NULL ";
        return this;
    }

    public Message filename_fullpathIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"filename_fullpath\" IS NOT NULL ";
        return this;
    }

    public Message filename_fullpathLike(String filename_fullpath)
    {
        this.sql_where = this.sql_where + " and \"filename_fullpath\" LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, filename_fullpath));
        bind_where_count++;
        return this;
    }

    public Message filename_fullpathNotLike(String filename_fullpath)
    {
        this.sql_where = this.sql_where + " and \"filename_fullpath\" NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, filename_fullpath));
        bind_where_count++;
        return this;
    }

    public Message msg_id_hashEq(String msg_id_hash)
    {
        this.sql_where = this.sql_where + " and \"msg_id_hash\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, msg_id_hash));
        bind_where_count++;
        return this;
    }

    public Message msg_id_hashNotEq(String msg_id_hash)
    {
        this.sql_where = this.sql_where + " and \"msg_id_hash\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, msg_id_hash));
        bind_where_count++;
        return this;
    }

    public Message msg_id_hashIsNull()
    {
        this.sql_where = this.sql_where + " and \"msg_id_hash\" IS NULL ";
        return this;
    }

    public Message msg_id_hashIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"msg_id_hash\" IS NOT NULL ";
        return this;
    }

    public Message msg_id_hashLike(String msg_id_hash)
    {
        this.sql_where = this.sql_where + " and \"msg_id_hash\" LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, msg_id_hash));
        bind_where_count++;
        return this;
    }

    public Message msg_id_hashNotLike(String msg_id_hash)
    {
        this.sql_where = this.sql_where + " and \"msg_id_hash\" NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, msg_id_hash));
        bind_where_count++;
        return this;
    }

    public Message raw_msgv2_bytesEq(String raw_msgv2_bytes)
    {
        this.sql_where = this.sql_where + " and \"raw_msgv2_bytes\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, raw_msgv2_bytes));
        bind_where_count++;
        return this;
    }

    public Message raw_msgv2_bytesNotEq(String raw_msgv2_bytes)
    {
        this.sql_where = this.sql_where + " and \"raw_msgv2_bytes\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, raw_msgv2_bytes));
        bind_where_count++;
        return this;
    }

    public Message raw_msgv2_bytesIsNull()
    {
        this.sql_where = this.sql_where + " and \"raw_msgv2_bytes\" IS NULL ";
        return this;
    }

    public Message raw_msgv2_bytesIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"raw_msgv2_bytes\" IS NOT NULL ";
        return this;
    }

    public Message raw_msgv2_bytesLike(String raw_msgv2_bytes)
    {
        this.sql_where = this.sql_where + " and \"raw_msgv2_bytes\" LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, raw_msgv2_bytes));
        bind_where_count++;
        return this;
    }

    public Message raw_msgv2_bytesNotLike(String raw_msgv2_bytes)
    {
        this.sql_where = this.sql_where + " and \"raw_msgv2_bytes\" NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, raw_msgv2_bytes));
        bind_where_count++;
        return this;
    }

    public Message msg_versionEq(int msg_version)
    {
        this.sql_where = this.sql_where + " and \"msg_version\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, msg_version));
        bind_where_count++;
        return this;
    }

    public Message msg_versionNotEq(int msg_version)
    {
        this.sql_where = this.sql_where + " and \"msg_version\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, msg_version));
        bind_where_count++;
        return this;
    }

    public Message msg_versionLt(int msg_version)
    {
        this.sql_where = this.sql_where + " and \"msg_version\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, msg_version));
        bind_where_count++;
        return this;
    }

    public Message msg_versionLe(int msg_version)
    {
        this.sql_where = this.sql_where + " and \"msg_version\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, msg_version));
        bind_where_count++;
        return this;
    }

    public Message msg_versionGt(int msg_version)
    {
        this.sql_where = this.sql_where + " and \"msg_version\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, msg_version));
        bind_where_count++;
        return this;
    }

    public Message msg_versionGe(int msg_version)
    {
        this.sql_where = this.sql_where + " and \"msg_version\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, msg_version));
        bind_where_count++;
        return this;
    }

    public Message msg_versionBetween(int msg_version1, int msg_version2)
    {
        this.sql_where = this.sql_where + " and \"msg_version\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and msg_version<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, msg_version1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, msg_version2));
        bind_where_count++;
        return this;
    }

    public Message msg_versionIsNull()
    {
        this.sql_where = this.sql_where + " and \"msg_version\" IS NULL ";
        return this;
    }

    public Message msg_versionIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"msg_version\" IS NOT NULL ";
        return this;
    }

    public Message resend_countEq(int resend_count)
    {
        this.sql_where = this.sql_where + " and \"resend_count\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, resend_count));
        bind_where_count++;
        return this;
    }

    public Message resend_countNotEq(int resend_count)
    {
        this.sql_where = this.sql_where + " and \"resend_count\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, resend_count));
        bind_where_count++;
        return this;
    }

    public Message resend_countLt(int resend_count)
    {
        this.sql_where = this.sql_where + " and \"resend_count\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, resend_count));
        bind_where_count++;
        return this;
    }

    public Message resend_countLe(int resend_count)
    {
        this.sql_where = this.sql_where + " and \"resend_count\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, resend_count));
        bind_where_count++;
        return this;
    }

    public Message resend_countGt(int resend_count)
    {
        this.sql_where = this.sql_where + " and \"resend_count\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, resend_count));
        bind_where_count++;
        return this;
    }

    public Message resend_countGe(int resend_count)
    {
        this.sql_where = this.sql_where + " and \"resend_count\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, resend_count));
        bind_where_count++;
        return this;
    }

    public Message resend_countBetween(int resend_count1, int resend_count2)
    {
        this.sql_where = this.sql_where + " and \"resend_count\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and resend_count<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, resend_count1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, resend_count2));
        bind_where_count++;
        return this;
    }

    public Message resend_countIsNull()
    {
        this.sql_where = this.sql_where + " and \"resend_count\" IS NULL ";
        return this;
    }

    public Message resend_countIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"resend_count\" IS NOT NULL ";
        return this;
    }

    public Message storage_frame_workEq(boolean storage_frame_work)
    {
        this.sql_where = this.sql_where + " and \"storage_frame_work\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, storage_frame_work));
        bind_where_count++;
        return this;
    }

    public Message storage_frame_workNotEq(boolean storage_frame_work)
    {
        this.sql_where = this.sql_where + " and \"storage_frame_work\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, storage_frame_work));
        bind_where_count++;
        return this;
    }

    public Message storage_frame_workIsNull()
    {
        this.sql_where = this.sql_where + " and \"storage_frame_work\" IS NULL ";
        return this;
    }

    public Message storage_frame_workIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"storage_frame_work\" IS NOT NULL ";
        return this;
    }

    public Message ft_outgoing_queuedEq(boolean ft_outgoing_queued)
    {
        this.sql_where = this.sql_where + " and \"ft_outgoing_queued\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, ft_outgoing_queued));
        bind_where_count++;
        return this;
    }

    public Message ft_outgoing_queuedNotEq(boolean ft_outgoing_queued)
    {
        this.sql_where = this.sql_where + " and \"ft_outgoing_queued\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, ft_outgoing_queued));
        bind_where_count++;
        return this;
    }

    public Message ft_outgoing_queuedIsNull()
    {
        this.sql_where = this.sql_where + " and \"ft_outgoing_queued\" IS NULL ";
        return this;
    }

    public Message ft_outgoing_queuedIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"ft_outgoing_queued\" IS NOT NULL ";
        return this;
    }

    public Message msg_at_relayEq(boolean msg_at_relay)
    {
        this.sql_where = this.sql_where + " and \"msg_at_relay\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, msg_at_relay));
        bind_where_count++;
        return this;
    }

    public Message msg_at_relayNotEq(boolean msg_at_relay)
    {
        this.sql_where = this.sql_where + " and \"msg_at_relay\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, msg_at_relay));
        bind_where_count++;
        return this;
    }

    public Message msg_at_relayIsNull()
    {
        this.sql_where = this.sql_where + " and \"msg_at_relay\" IS NULL ";
        return this;
    }

    public Message msg_at_relayIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"msg_at_relay\" IS NOT NULL ";
        return this;
    }

    public Message msg_idv3_hashEq(String msg_idv3_hash)
    {
        this.sql_where = this.sql_where + " and \"msg_idv3_hash\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, msg_idv3_hash));
        bind_where_count++;
        return this;
    }

    public Message msg_idv3_hashNotEq(String msg_idv3_hash)
    {
        this.sql_where = this.sql_where + " and \"msg_idv3_hash\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, msg_idv3_hash));
        bind_where_count++;
        return this;
    }

    public Message msg_idv3_hashIsNull()
    {
        this.sql_where = this.sql_where + " and \"msg_idv3_hash\" IS NULL ";
        return this;
    }

    public Message msg_idv3_hashIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"msg_idv3_hash\" IS NOT NULL ";
        return this;
    }

    public Message msg_idv3_hashLike(String msg_idv3_hash)
    {
        this.sql_where = this.sql_where + " and \"msg_idv3_hash\" LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, msg_idv3_hash));
        bind_where_count++;
        return this;
    }

    public Message msg_idv3_hashNotLike(String msg_idv3_hash)
    {
        this.sql_where = this.sql_where + " and \"msg_idv3_hash\" NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, msg_idv3_hash));
        bind_where_count++;
        return this;
    }

    public Message sent_pushEq(int sent_push)
    {
        this.sql_where = this.sql_where + " and \"sent_push\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, sent_push));
        bind_where_count++;
        return this;
    }

    public Message sent_pushNotEq(int sent_push)
    {
        this.sql_where = this.sql_where + " and \"sent_push\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, sent_push));
        bind_where_count++;
        return this;
    }

    public Message sent_pushLt(int sent_push)
    {
        this.sql_where = this.sql_where + " and \"sent_push\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, sent_push));
        bind_where_count++;
        return this;
    }

    public Message sent_pushLe(int sent_push)
    {
        this.sql_where = this.sql_where + " and \"sent_push\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, sent_push));
        bind_where_count++;
        return this;
    }

    public Message sent_pushGt(int sent_push)
    {
        this.sql_where = this.sql_where + " and \"sent_push\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, sent_push));
        bind_where_count++;
        return this;
    }

    public Message sent_pushGe(int sent_push)
    {
        this.sql_where = this.sql_where + " and \"sent_push\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, sent_push));
        bind_where_count++;
        return this;
    }

    public Message sent_pushBetween(int sent_push1, int sent_push2)
    {
        this.sql_where = this.sql_where + " and \"sent_push\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and sent_push<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, sent_push1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, sent_push2));
        bind_where_count++;
        return this;
    }

    public Message sent_pushIsNull()
    {
        this.sql_where = this.sql_where + " and \"sent_push\" IS NULL ";
        return this;
    }

    public Message sent_pushIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"sent_push\" IS NOT NULL ";
        return this;
    }

    public Message filetransfer_kindEq(int filetransfer_kind)
    {
        this.sql_where = this.sql_where + " and \"filetransfer_kind\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, filetransfer_kind));
        bind_where_count++;
        return this;
    }

    public Message filetransfer_kindNotEq(int filetransfer_kind)
    {
        this.sql_where = this.sql_where + " and \"filetransfer_kind\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, filetransfer_kind));
        bind_where_count++;
        return this;
    }

    public Message filetransfer_kindLt(int filetransfer_kind)
    {
        this.sql_where = this.sql_where + " and \"filetransfer_kind\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, filetransfer_kind));
        bind_where_count++;
        return this;
    }

    public Message filetransfer_kindLe(int filetransfer_kind)
    {
        this.sql_where = this.sql_where + " and \"filetransfer_kind\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, filetransfer_kind));
        bind_where_count++;
        return this;
    }

    public Message filetransfer_kindGt(int filetransfer_kind)
    {
        this.sql_where = this.sql_where + " and \"filetransfer_kind\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, filetransfer_kind));
        bind_where_count++;
        return this;
    }

    public Message filetransfer_kindGe(int filetransfer_kind)
    {
        this.sql_where = this.sql_where + " and \"filetransfer_kind\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, filetransfer_kind));
        bind_where_count++;
        return this;
    }

    public Message filetransfer_kindBetween(int filetransfer_kind1, int filetransfer_kind2)
    {
        this.sql_where = this.sql_where + " and \"filetransfer_kind\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and filetransfer_kind<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, filetransfer_kind1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, filetransfer_kind2));
        bind_where_count++;
        return this;
    }

    public Message filetransfer_kindIsNull()
    {
        this.sql_where = this.sql_where + " and \"filetransfer_kind\" IS NULL ";
        return this;
    }

    public Message filetransfer_kindIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"filetransfer_kind\" IS NOT NULL ";
        return this;
    }


    // ----------------- OrderBy funcs ------------------ //
    public Message orderByIdAsc()
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

    public Message orderByIdDesc()
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

    public Message orderByMessage_idAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"message_id\" ASC ";
        return this;
    }

    public Message orderByMessage_idDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"message_id\" DESC ";
        return this;
    }

    public Message orderByTox_friendpubkeyAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"tox_friendpubkey\" ASC ";
        return this;
    }

    public Message orderByTox_friendpubkeyDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"tox_friendpubkey\" DESC ";
        return this;
    }

    public Message orderByDirectionAsc()
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

    public Message orderByDirectionDesc()
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

    public Message orderByTOX_MESSAGE_TYPEAsc()
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

    public Message orderByTOX_MESSAGE_TYPEDesc()
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

    public Message orderByTRIFA_MESSAGE_TYPEAsc()
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

    public Message orderByTRIFA_MESSAGE_TYPEDesc()
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

    public Message orderByStateAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"state\" ASC ";
        return this;
    }

    public Message orderByStateDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"state\" DESC ";
        return this;
    }

    public Message orderByFt_acceptedAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"ft_accepted\" ASC ";
        return this;
    }

    public Message orderByFt_acceptedDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"ft_accepted\" DESC ";
        return this;
    }

    public Message orderByFt_outgoing_startedAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"ft_outgoing_started\" ASC ";
        return this;
    }

    public Message orderByFt_outgoing_startedDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"ft_outgoing_started\" DESC ";
        return this;
    }

    public Message orderByFiledb_idAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"filedb_id\" ASC ";
        return this;
    }

    public Message orderByFiledb_idDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"filedb_id\" DESC ";
        return this;
    }

    public Message orderByFiletransfer_idAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"filetransfer_id\" ASC ";
        return this;
    }

    public Message orderByFiletransfer_idDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"filetransfer_id\" DESC ";
        return this;
    }

    public Message orderBySent_timestampAsc()
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

    public Message orderBySent_timestampDesc()
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

    public Message orderBySent_timestamp_msAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"sent_timestamp_ms\" ASC ";
        return this;
    }

    public Message orderBySent_timestamp_msDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"sent_timestamp_ms\" DESC ";
        return this;
    }

    public Message orderByRcvd_timestampAsc()
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

    public Message orderByRcvd_timestampDesc()
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

    public Message orderByRcvd_timestamp_msAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"rcvd_timestamp_ms\" ASC ";
        return this;
    }

    public Message orderByRcvd_timestamp_msDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"rcvd_timestamp_ms\" DESC ";
        return this;
    }

    public Message orderByReadAsc()
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

    public Message orderByReadDesc()
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

    public Message orderBySend_retriesAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"send_retries\" ASC ";
        return this;
    }

    public Message orderBySend_retriesDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"send_retries\" DESC ";
        return this;
    }

    public Message orderByIs_newAsc()
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

    public Message orderByIs_newDesc()
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

    public Message orderByTextAsc()
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

    public Message orderByTextDesc()
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

    public Message orderByFilename_fullpathAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"filename_fullpath\" ASC ";
        return this;
    }

    public Message orderByFilename_fullpathDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"filename_fullpath\" DESC ";
        return this;
    }

    public Message orderByMsg_id_hashAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"msg_id_hash\" ASC ";
        return this;
    }

    public Message orderByMsg_id_hashDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"msg_id_hash\" DESC ";
        return this;
    }

    public Message orderByRaw_msgv2_bytesAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"raw_msgv2_bytes\" ASC ";
        return this;
    }

    public Message orderByRaw_msgv2_bytesDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"raw_msgv2_bytes\" DESC ";
        return this;
    }

    public Message orderByMsg_versionAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"msg_version\" ASC ";
        return this;
    }

    public Message orderByMsg_versionDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"msg_version\" DESC ";
        return this;
    }

    public Message orderByResend_countAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"resend_count\" ASC ";
        return this;
    }

    public Message orderByResend_countDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"resend_count\" DESC ";
        return this;
    }

    public Message orderByStorage_frame_workAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"storage_frame_work\" ASC ";
        return this;
    }

    public Message orderByStorage_frame_workDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"storage_frame_work\" DESC ";
        return this;
    }

    public Message orderByFt_outgoing_queuedAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"ft_outgoing_queued\" ASC ";
        return this;
    }

    public Message orderByFt_outgoing_queuedDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"ft_outgoing_queued\" DESC ";
        return this;
    }

    public Message orderByMsg_at_relayAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"msg_at_relay\" ASC ";
        return this;
    }

    public Message orderByMsg_at_relayDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"msg_at_relay\" DESC ";
        return this;
    }

    public Message orderByMsg_idv3_hashAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"msg_idv3_hash\" ASC ";
        return this;
    }

    public Message orderByMsg_idv3_hashDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"msg_idv3_hash\" DESC ";
        return this;
    }

    public Message orderBySent_pushAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"sent_push\" ASC ";
        return this;
    }

    public Message orderBySent_pushDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"sent_push\" DESC ";
        return this;
    }

    public Message orderByFiletransfer_kindAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"filetransfer_kind\" ASC ";
        return this;
    }

    public Message orderByFiletransfer_kindDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"filetransfer_kind\" DESC ";
        return this;
    }



}

