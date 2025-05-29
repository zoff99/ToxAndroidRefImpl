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
public class GroupMessage
{
    private static final String TAG = "DB.GroupMessage";
    @PrimaryKey(autoincrement = true, auto = true)
    public long id;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public String message_id_tox;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public String group_identifier;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public String tox_group_peer_pubkey;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public int tox_group_peer_role;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public int private_message;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public String tox_group_peername;

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

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public int TRIFA_SYNC_TYPE;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public int sync_confirmations;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public String tox_group_peer_pubkey_syncer_01;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public String tox_group_peer_pubkey_syncer_02;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public String tox_group_peer_pubkey_syncer_03;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public long tox_group_peer_pubkey_syncer_01_sent_timestamp;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public long tox_group_peer_pubkey_syncer_02_sent_timestamp;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public long tox_group_peer_pubkey_syncer_03_sent_timestamp;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public String msg_id_hash;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public String sent_privately_to_tox_group_peer_pubkey;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public String path_name;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public String file_name;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public String filename_fullpath;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public long filesize;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public boolean storage_frame_work;

    public static GroupMessage deep_copy(GroupMessage in)
    {
        GroupMessage out = new GroupMessage();
        out.id = in.id;
        out.message_id_tox = in.message_id_tox;
        out.group_identifier = in.group_identifier;
        out.tox_group_peer_pubkey = in.tox_group_peer_pubkey;
        out.tox_group_peer_role = in.tox_group_peer_role;
        out.private_message = in.private_message;
        out.tox_group_peername = in.tox_group_peername;
        out.direction = in.direction;
        out.TOX_MESSAGE_TYPE = in.TOX_MESSAGE_TYPE;
        out.TRIFA_MESSAGE_TYPE = in.TRIFA_MESSAGE_TYPE;
        out.sent_timestamp = in.sent_timestamp;
        out.rcvd_timestamp = in.rcvd_timestamp;
        out.read = in.read;
        out.is_new = in.is_new;
        out.text = in.text;
        out.was_synced = in.was_synced;
        out.TRIFA_SYNC_TYPE = in.TRIFA_SYNC_TYPE;
        out.sync_confirmations = in.sync_confirmations;
        out.tox_group_peer_pubkey_syncer_01 = in.tox_group_peer_pubkey_syncer_01;
        out.tox_group_peer_pubkey_syncer_02 = in.tox_group_peer_pubkey_syncer_02;
        out.tox_group_peer_pubkey_syncer_03 = in.tox_group_peer_pubkey_syncer_03;
        out.tox_group_peer_pubkey_syncer_01_sent_timestamp = in.tox_group_peer_pubkey_syncer_01_sent_timestamp;
        out.tox_group_peer_pubkey_syncer_02_sent_timestamp = in.tox_group_peer_pubkey_syncer_02_sent_timestamp;
        out.tox_group_peer_pubkey_syncer_03_sent_timestamp = in.tox_group_peer_pubkey_syncer_03_sent_timestamp;
        out.msg_id_hash = in.msg_id_hash;
        out.sent_privately_to_tox_group_peer_pubkey = in.sent_privately_to_tox_group_peer_pubkey;
        out.path_name = in.path_name;
        out.file_name = in.file_name;
        out.filename_fullpath = in.filename_fullpath;
        out.filesize = in.filesize;
        out.storage_frame_work = in.storage_frame_work;

        return out;
    }

    @Override
    public String toString()
    {
        return "id=" + id + ", message_id_tox=" + message_id_tox + ", group_identifier=" + group_identifier + ", tox_group_peer_pubkey=" + tox_group_peer_pubkey + ", tox_group_peer_role=" + tox_group_peer_role + ", private_message=" + private_message + ", tox_group_peername=" + tox_group_peername + ", direction=" + direction + ", TOX_MESSAGE_TYPE=" + TOX_MESSAGE_TYPE + ", TRIFA_MESSAGE_TYPE=" + TRIFA_MESSAGE_TYPE + ", sent_timestamp=" + sent_timestamp + ", rcvd_timestamp=" + rcvd_timestamp + ", read=" + read + ", is_new=" + is_new + ", text=" + text + ", was_synced=" + was_synced + ", TRIFA_SYNC_TYPE=" + TRIFA_SYNC_TYPE + ", sync_confirmations=" + sync_confirmations + ", tox_group_peer_pubkey_syncer_01=" + tox_group_peer_pubkey_syncer_01 + ", tox_group_peer_pubkey_syncer_02=" + tox_group_peer_pubkey_syncer_02 + ", tox_group_peer_pubkey_syncer_03=" + tox_group_peer_pubkey_syncer_03 + ", tox_group_peer_pubkey_syncer_01_sent_timestamp=" + tox_group_peer_pubkey_syncer_01_sent_timestamp + ", tox_group_peer_pubkey_syncer_02_sent_timestamp=" + tox_group_peer_pubkey_syncer_02_sent_timestamp + ", tox_group_peer_pubkey_syncer_03_sent_timestamp=" + tox_group_peer_pubkey_syncer_03_sent_timestamp + ", msg_id_hash=" + msg_id_hash + ", sent_privately_to_tox_group_peer_pubkey=" + sent_privately_to_tox_group_peer_pubkey + ", path_name=" + path_name + ", file_name=" + file_name + ", filename_fullpath=" + filename_fullpath + ", filesize=" + filesize + ", storage_frame_work=" + storage_frame_work;
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

    public List<GroupMessage> toList()
    {
        List<GroupMessage> list = new ArrayList<>();
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
                GroupMessage out = new GroupMessage();
                out.id = rs.getLong("id");
                out.message_id_tox = rs.getString("message_id_tox");
                out.group_identifier = rs.getString("group_identifier");
                out.tox_group_peer_pubkey = rs.getString("tox_group_peer_pubkey");
                out.tox_group_peer_role = rs.getInt("tox_group_peer_role");
                out.private_message = rs.getInt("private_message");
                out.tox_group_peername = rs.getString("tox_group_peername");
                out.direction = rs.getInt("direction");
                out.TOX_MESSAGE_TYPE = rs.getInt("TOX_MESSAGE_TYPE");
                out.TRIFA_MESSAGE_TYPE = rs.getInt("TRIFA_MESSAGE_TYPE");
                out.sent_timestamp = rs.getLong("sent_timestamp");
                out.rcvd_timestamp = rs.getLong("rcvd_timestamp");
                out.read = rs.getBoolean("read");
                out.is_new = rs.getBoolean("is_new");
                out.text = rs.getString("text");
                out.was_synced = rs.getBoolean("was_synced");
                out.TRIFA_SYNC_TYPE = rs.getInt("TRIFA_SYNC_TYPE");
                out.sync_confirmations = rs.getInt("sync_confirmations");
                out.tox_group_peer_pubkey_syncer_01 = rs.getString("tox_group_peer_pubkey_syncer_01");
                out.tox_group_peer_pubkey_syncer_02 = rs.getString("tox_group_peer_pubkey_syncer_02");
                out.tox_group_peer_pubkey_syncer_03 = rs.getString("tox_group_peer_pubkey_syncer_03");
                out.tox_group_peer_pubkey_syncer_01_sent_timestamp = rs.getLong("tox_group_peer_pubkey_syncer_01_sent_timestamp");
                out.tox_group_peer_pubkey_syncer_02_sent_timestamp = rs.getLong("tox_group_peer_pubkey_syncer_02_sent_timestamp");
                out.tox_group_peer_pubkey_syncer_03_sent_timestamp = rs.getLong("tox_group_peer_pubkey_syncer_03_sent_timestamp");
                out.msg_id_hash = rs.getString("msg_id_hash");
                out.sent_privately_to_tox_group_peer_pubkey = rs.getString("sent_privately_to_tox_group_peer_pubkey");
                out.path_name = rs.getString("path_name");
                out.file_name = rs.getString("file_name");
                out.filename_fullpath = rs.getString("filename_fullpath");
                out.filesize = rs.getLong("filesize");
                out.storage_frame_work = rs.getBoolean("storage_frame_work");

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
                    + ",\"group_identifier\""
                    + ",\"tox_group_peer_pubkey\""
                    + ",\"tox_group_peer_role\""
                    + ",\"private_message\""
                    + ",\"tox_group_peername\""
                    + ",\"direction\""
                    + ",\"TOX_MESSAGE_TYPE\""
                    + ",\"TRIFA_MESSAGE_TYPE\""
                    + ",\"sent_timestamp\""
                    + ",\"rcvd_timestamp\""
                    + ",\"read\""
                    + ",\"is_new\""
                    + ",\"text\""
                    + ",\"was_synced\""
                    + ",\"TRIFA_SYNC_TYPE\""
                    + ",\"sync_confirmations\""
                    + ",\"tox_group_peer_pubkey_syncer_01\""
                    + ",\"tox_group_peer_pubkey_syncer_02\""
                    + ",\"tox_group_peer_pubkey_syncer_03\""
                    + ",\"tox_group_peer_pubkey_syncer_01_sent_timestamp\""
                    + ",\"tox_group_peer_pubkey_syncer_02_sent_timestamp\""
                    + ",\"tox_group_peer_pubkey_syncer_03_sent_timestamp\""
                    + ",\"msg_id_hash\""
                    + ",\"sent_privately_to_tox_group_peer_pubkey\""
                    + ",\"path_name\""
                    + ",\"file_name\""
                    + ",\"filename_fullpath\""
                    + ",\"filesize\""
                    + ",\"storage_frame_work\""
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
                    + ",?30"
                    + ")";

            insert_pstmt = sqldb.prepareStatement(insert_pstmt_sql);
            insert_pstmt.clearParameters();

            insert_pstmt.setString(1, this.message_id_tox);
            insert_pstmt.setString(2, this.group_identifier);
            insert_pstmt.setString(3, this.tox_group_peer_pubkey);
            insert_pstmt.setInt(4, this.tox_group_peer_role);
            insert_pstmt.setInt(5, this.private_message);
            insert_pstmt.setString(6, this.tox_group_peername);
            insert_pstmt.setInt(7, this.direction);
            insert_pstmt.setInt(8, this.TOX_MESSAGE_TYPE);
            insert_pstmt.setInt(9, this.TRIFA_MESSAGE_TYPE);
            insert_pstmt.setLong(10, this.sent_timestamp);
            insert_pstmt.setLong(11, this.rcvd_timestamp);
            insert_pstmt.setBoolean(12, this.read);
            insert_pstmt.setBoolean(13, this.is_new);
            insert_pstmt.setString(14, this.text);
            insert_pstmt.setBoolean(15, this.was_synced);
            insert_pstmt.setInt(16, this.TRIFA_SYNC_TYPE);
            insert_pstmt.setInt(17, this.sync_confirmations);
            insert_pstmt.setString(18, this.tox_group_peer_pubkey_syncer_01);
            insert_pstmt.setString(19, this.tox_group_peer_pubkey_syncer_02);
            insert_pstmt.setString(20, this.tox_group_peer_pubkey_syncer_03);
            insert_pstmt.setLong(21, this.tox_group_peer_pubkey_syncer_01_sent_timestamp);
            insert_pstmt.setLong(22, this.tox_group_peer_pubkey_syncer_02_sent_timestamp);
            insert_pstmt.setLong(23, this.tox_group_peer_pubkey_syncer_03_sent_timestamp);
            insert_pstmt.setString(24, this.msg_id_hash);
            insert_pstmt.setString(25, this.sent_privately_to_tox_group_peer_pubkey);
            insert_pstmt.setString(26, this.path_name);
            insert_pstmt.setString(27, this.file_name);
            insert_pstmt.setString(28, this.filename_fullpath);
            insert_pstmt.setLong(29, this.filesize);
            insert_pstmt.setBoolean(30, this.storage_frame_work);
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

    public GroupMessage get(int i)
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

    public GroupMessage limit(int rowcount)
    {
        this.sql_limit = " limit " + rowcount + " ";
        return this;
    }

    public GroupMessage limit(int rowcount, int offset)
    {
        this.sql_limit = " limit " + offset + " , " + rowcount;
        return this;
    }

    // ----------------------------------- //
    // ----------------------------------- //
    // ----------------------------------- //


    // ----------------- Set funcs ---------------------- //
    public GroupMessage id(long id)
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

    public GroupMessage message_id_tox(String message_id_tox)
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

    public GroupMessage group_identifier(String group_identifier)
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

    public GroupMessage tox_group_peer_pubkey(String tox_group_peer_pubkey)
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

    public GroupMessage tox_group_peer_role(int tox_group_peer_role)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"tox_group_peer_role\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, tox_group_peer_role));
        bind_set_count++;
        return this;
    }

    public GroupMessage private_message(int private_message)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"private_message\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, private_message));
        bind_set_count++;
        return this;
    }

    public GroupMessage tox_group_peername(String tox_group_peername)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"tox_group_peername\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_group_peername));
        bind_set_count++;
        return this;
    }

    public GroupMessage direction(int direction)
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

    public GroupMessage TOX_MESSAGE_TYPE(int TOX_MESSAGE_TYPE)
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

    public GroupMessage TRIFA_MESSAGE_TYPE(int TRIFA_MESSAGE_TYPE)
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

    public GroupMessage sent_timestamp(long sent_timestamp)
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

    public GroupMessage rcvd_timestamp(long rcvd_timestamp)
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

    public GroupMessage read(boolean read)
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

    public GroupMessage is_new(boolean is_new)
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

    public GroupMessage text(String text)
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

    public GroupMessage was_synced(boolean was_synced)
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

    public GroupMessage TRIFA_SYNC_TYPE(int TRIFA_SYNC_TYPE)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"TRIFA_SYNC_TYPE\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TRIFA_SYNC_TYPE));
        bind_set_count++;
        return this;
    }

    public GroupMessage sync_confirmations(int sync_confirmations)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"sync_confirmations\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, sync_confirmations));
        bind_set_count++;
        return this;
    }

    public GroupMessage tox_group_peer_pubkey_syncer_01(String tox_group_peer_pubkey_syncer_01)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"tox_group_peer_pubkey_syncer_01\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_group_peer_pubkey_syncer_01));
        bind_set_count++;
        return this;
    }

    public GroupMessage tox_group_peer_pubkey_syncer_02(String tox_group_peer_pubkey_syncer_02)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"tox_group_peer_pubkey_syncer_02\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_group_peer_pubkey_syncer_02));
        bind_set_count++;
        return this;
    }

    public GroupMessage tox_group_peer_pubkey_syncer_03(String tox_group_peer_pubkey_syncer_03)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"tox_group_peer_pubkey_syncer_03\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_group_peer_pubkey_syncer_03));
        bind_set_count++;
        return this;
    }

    public GroupMessage tox_group_peer_pubkey_syncer_01_sent_timestamp(long tox_group_peer_pubkey_syncer_01_sent_timestamp)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"tox_group_peer_pubkey_syncer_01_sent_timestamp\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, tox_group_peer_pubkey_syncer_01_sent_timestamp));
        bind_set_count++;
        return this;
    }

    public GroupMessage tox_group_peer_pubkey_syncer_02_sent_timestamp(long tox_group_peer_pubkey_syncer_02_sent_timestamp)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"tox_group_peer_pubkey_syncer_02_sent_timestamp\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, tox_group_peer_pubkey_syncer_02_sent_timestamp));
        bind_set_count++;
        return this;
    }

    public GroupMessage tox_group_peer_pubkey_syncer_03_sent_timestamp(long tox_group_peer_pubkey_syncer_03_sent_timestamp)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"tox_group_peer_pubkey_syncer_03_sent_timestamp\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, tox_group_peer_pubkey_syncer_03_sent_timestamp));
        bind_set_count++;
        return this;
    }

    public GroupMessage msg_id_hash(String msg_id_hash)
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

    public GroupMessage sent_privately_to_tox_group_peer_pubkey(String sent_privately_to_tox_group_peer_pubkey)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"sent_privately_to_tox_group_peer_pubkey\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, sent_privately_to_tox_group_peer_pubkey));
        bind_set_count++;
        return this;
    }

    public GroupMessage path_name(String path_name)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"path_name\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, path_name));
        bind_set_count++;
        return this;
    }

    public GroupMessage file_name(String file_name)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"file_name\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, file_name));
        bind_set_count++;
        return this;
    }

    public GroupMessage filename_fullpath(String filename_fullpath)
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

    public GroupMessage filesize(long filesize)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"filesize\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filesize));
        bind_set_count++;
        return this;
    }

    public GroupMessage storage_frame_work(boolean storage_frame_work)
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


    // ----------------- Eq/Gt/Lt funcs ----------------- //
    public GroupMessage idEq(long id)
    {
        this.sql_where = this.sql_where + " and \"id\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public GroupMessage idNotEq(long id)
    {
        this.sql_where = this.sql_where + " and \"id\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public GroupMessage idLt(long id)
    {
        this.sql_where = this.sql_where + " and \"id\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public GroupMessage idLe(long id)
    {
        this.sql_where = this.sql_where + " and \"id\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public GroupMessage idGt(long id)
    {
        this.sql_where = this.sql_where + " and \"id\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public GroupMessage idGe(long id)
    {
        this.sql_where = this.sql_where + " and \"id\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public GroupMessage idBetween(long id1, long id2)
    {
        this.sql_where = this.sql_where + " and \"id\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and id<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id2));
        bind_where_count++;
        return this;
    }

    public GroupMessage idIsNull()
    {
        this.sql_where = this.sql_where + " and \"id\" IS NULL ";
        return this;
    }

    public GroupMessage idIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"id\" IS NOT NULL ";
        return this;
    }

    public GroupMessage message_id_toxEq(String message_id_tox)
    {
        this.sql_where = this.sql_where + " and \"message_id_tox\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, message_id_tox));
        bind_where_count++;
        return this;
    }

    public GroupMessage message_id_toxNotEq(String message_id_tox)
    {
        this.sql_where = this.sql_where + " and \"message_id_tox\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, message_id_tox));
        bind_where_count++;
        return this;
    }

    public GroupMessage message_id_toxIsNull()
    {
        this.sql_where = this.sql_where + " and \"message_id_tox\" IS NULL ";
        return this;
    }

    public GroupMessage message_id_toxIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"message_id_tox\" IS NOT NULL ";
        return this;
    }

    public GroupMessage message_id_toxLike(String message_id_tox)
    {
        this.sql_where = this.sql_where + " and \"message_id_tox\" LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, message_id_tox));
        bind_where_count++;
        return this;
    }

    public GroupMessage message_id_toxNotLike(String message_id_tox)
    {
        this.sql_where = this.sql_where + " and \"message_id_tox\" NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, message_id_tox));
        bind_where_count++;
        return this;
    }

    public GroupMessage group_identifierEq(String group_identifier)
    {
        this.sql_where = this.sql_where + " and \"group_identifier\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, group_identifier));
        bind_where_count++;
        return this;
    }

    public GroupMessage group_identifierNotEq(String group_identifier)
    {
        this.sql_where = this.sql_where + " and \"group_identifier\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, group_identifier));
        bind_where_count++;
        return this;
    }

    public GroupMessage group_identifierIsNull()
    {
        this.sql_where = this.sql_where + " and \"group_identifier\" IS NULL ";
        return this;
    }

    public GroupMessage group_identifierIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"group_identifier\" IS NOT NULL ";
        return this;
    }

    public GroupMessage group_identifierLike(String group_identifier)
    {
        this.sql_where = this.sql_where + " and \"group_identifier\" LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, group_identifier));
        bind_where_count++;
        return this;
    }

    public GroupMessage group_identifierNotLike(String group_identifier)
    {
        this.sql_where = this.sql_where + " and \"group_identifier\" NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, group_identifier));
        bind_where_count++;
        return this;
    }

    public GroupMessage tox_group_peer_pubkeyEq(String tox_group_peer_pubkey)
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_pubkey\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_group_peer_pubkey));
        bind_where_count++;
        return this;
    }

    public GroupMessage tox_group_peer_pubkeyNotEq(String tox_group_peer_pubkey)
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_pubkey\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_group_peer_pubkey));
        bind_where_count++;
        return this;
    }

    public GroupMessage tox_group_peer_pubkeyIsNull()
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_pubkey\" IS NULL ";
        return this;
    }

    public GroupMessage tox_group_peer_pubkeyIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_pubkey\" IS NOT NULL ";
        return this;
    }

    public GroupMessage tox_group_peer_pubkeyLike(String tox_group_peer_pubkey)
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_pubkey\" LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_group_peer_pubkey));
        bind_where_count++;
        return this;
    }

    public GroupMessage tox_group_peer_pubkeyNotLike(String tox_group_peer_pubkey)
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_pubkey\" NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_group_peer_pubkey));
        bind_where_count++;
        return this;
    }

    public GroupMessage tox_group_peer_roleEq(int tox_group_peer_role)
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_role\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, tox_group_peer_role));
        bind_where_count++;
        return this;
    }

    public GroupMessage tox_group_peer_roleNotEq(int tox_group_peer_role)
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_role\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, tox_group_peer_role));
        bind_where_count++;
        return this;
    }

    public GroupMessage tox_group_peer_roleLt(int tox_group_peer_role)
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_role\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, tox_group_peer_role));
        bind_where_count++;
        return this;
    }

    public GroupMessage tox_group_peer_roleLe(int tox_group_peer_role)
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_role\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, tox_group_peer_role));
        bind_where_count++;
        return this;
    }

    public GroupMessage tox_group_peer_roleGt(int tox_group_peer_role)
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_role\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, tox_group_peer_role));
        bind_where_count++;
        return this;
    }

    public GroupMessage tox_group_peer_roleGe(int tox_group_peer_role)
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_role\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, tox_group_peer_role));
        bind_where_count++;
        return this;
    }

    public GroupMessage tox_group_peer_roleBetween(int tox_group_peer_role1, int tox_group_peer_role2)
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_role\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and tox_group_peer_role<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, tox_group_peer_role1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, tox_group_peer_role2));
        bind_where_count++;
        return this;
    }

    public GroupMessage tox_group_peer_roleIsNull()
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_role\" IS NULL ";
        return this;
    }

    public GroupMessage tox_group_peer_roleIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_role\" IS NOT NULL ";
        return this;
    }

    public GroupMessage private_messageEq(int private_message)
    {
        this.sql_where = this.sql_where + " and \"private_message\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, private_message));
        bind_where_count++;
        return this;
    }

    public GroupMessage private_messageNotEq(int private_message)
    {
        this.sql_where = this.sql_where + " and \"private_message\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, private_message));
        bind_where_count++;
        return this;
    }

    public GroupMessage private_messageLt(int private_message)
    {
        this.sql_where = this.sql_where + " and \"private_message\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, private_message));
        bind_where_count++;
        return this;
    }

    public GroupMessage private_messageLe(int private_message)
    {
        this.sql_where = this.sql_where + " and \"private_message\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, private_message));
        bind_where_count++;
        return this;
    }

    public GroupMessage private_messageGt(int private_message)
    {
        this.sql_where = this.sql_where + " and \"private_message\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, private_message));
        bind_where_count++;
        return this;
    }

    public GroupMessage private_messageGe(int private_message)
    {
        this.sql_where = this.sql_where + " and \"private_message\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, private_message));
        bind_where_count++;
        return this;
    }

    public GroupMessage private_messageBetween(int private_message1, int private_message2)
    {
        this.sql_where = this.sql_where + " and \"private_message\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and private_message<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, private_message1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, private_message2));
        bind_where_count++;
        return this;
    }

    public GroupMessage private_messageIsNull()
    {
        this.sql_where = this.sql_where + " and \"private_message\" IS NULL ";
        return this;
    }

    public GroupMessage private_messageIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"private_message\" IS NOT NULL ";
        return this;
    }

    public GroupMessage tox_group_peernameEq(String tox_group_peername)
    {
        this.sql_where = this.sql_where + " and \"tox_group_peername\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_group_peername));
        bind_where_count++;
        return this;
    }

    public GroupMessage tox_group_peernameNotEq(String tox_group_peername)
    {
        this.sql_where = this.sql_where + " and \"tox_group_peername\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_group_peername));
        bind_where_count++;
        return this;
    }

    public GroupMessage tox_group_peernameIsNull()
    {
        this.sql_where = this.sql_where + " and \"tox_group_peername\" IS NULL ";
        return this;
    }

    public GroupMessage tox_group_peernameIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"tox_group_peername\" IS NOT NULL ";
        return this;
    }

    public GroupMessage tox_group_peernameLike(String tox_group_peername)
    {
        this.sql_where = this.sql_where + " and \"tox_group_peername\" LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_group_peername));
        bind_where_count++;
        return this;
    }

    public GroupMessage tox_group_peernameNotLike(String tox_group_peername)
    {
        this.sql_where = this.sql_where + " and \"tox_group_peername\" NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_group_peername));
        bind_where_count++;
        return this;
    }

    public GroupMessage directionEq(int direction)
    {
        this.sql_where = this.sql_where + " and \"direction\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, direction));
        bind_where_count++;
        return this;
    }

    public GroupMessage directionNotEq(int direction)
    {
        this.sql_where = this.sql_where + " and \"direction\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, direction));
        bind_where_count++;
        return this;
    }

    public GroupMessage directionLt(int direction)
    {
        this.sql_where = this.sql_where + " and \"direction\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, direction));
        bind_where_count++;
        return this;
    }

    public GroupMessage directionLe(int direction)
    {
        this.sql_where = this.sql_where + " and \"direction\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, direction));
        bind_where_count++;
        return this;
    }

    public GroupMessage directionGt(int direction)
    {
        this.sql_where = this.sql_where + " and \"direction\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, direction));
        bind_where_count++;
        return this;
    }

    public GroupMessage directionGe(int direction)
    {
        this.sql_where = this.sql_where + " and \"direction\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, direction));
        bind_where_count++;
        return this;
    }

    public GroupMessage directionBetween(int direction1, int direction2)
    {
        this.sql_where = this.sql_where + " and \"direction\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and direction<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, direction1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, direction2));
        bind_where_count++;
        return this;
    }

    public GroupMessage directionIsNull()
    {
        this.sql_where = this.sql_where + " and \"direction\" IS NULL ";
        return this;
    }

    public GroupMessage directionIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"direction\" IS NOT NULL ";
        return this;
    }

    public GroupMessage TOX_MESSAGE_TYPEEq(int TOX_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and \"TOX_MESSAGE_TYPE\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public GroupMessage TOX_MESSAGE_TYPENotEq(int TOX_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and \"TOX_MESSAGE_TYPE\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public GroupMessage TOX_MESSAGE_TYPELt(int TOX_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and \"TOX_MESSAGE_TYPE\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public GroupMessage TOX_MESSAGE_TYPELe(int TOX_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and \"TOX_MESSAGE_TYPE\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public GroupMessage TOX_MESSAGE_TYPEGt(int TOX_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and \"TOX_MESSAGE_TYPE\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public GroupMessage TOX_MESSAGE_TYPEGe(int TOX_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and \"TOX_MESSAGE_TYPE\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public GroupMessage TOX_MESSAGE_TYPEBetween(int TOX_MESSAGE_TYPE1, int TOX_MESSAGE_TYPE2)
    {
        this.sql_where = this.sql_where + " and \"TOX_MESSAGE_TYPE\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and TOX_MESSAGE_TYPE<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_MESSAGE_TYPE1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TOX_MESSAGE_TYPE2));
        bind_where_count++;
        return this;
    }

    public GroupMessage TOX_MESSAGE_TYPEIsNull()
    {
        this.sql_where = this.sql_where + " and \"TOX_MESSAGE_TYPE\" IS NULL ";
        return this;
    }

    public GroupMessage TOX_MESSAGE_TYPEIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"TOX_MESSAGE_TYPE\" IS NOT NULL ";
        return this;
    }

    public GroupMessage TRIFA_MESSAGE_TYPEEq(int TRIFA_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and \"TRIFA_MESSAGE_TYPE\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TRIFA_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public GroupMessage TRIFA_MESSAGE_TYPENotEq(int TRIFA_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and \"TRIFA_MESSAGE_TYPE\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TRIFA_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public GroupMessage TRIFA_MESSAGE_TYPELt(int TRIFA_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and \"TRIFA_MESSAGE_TYPE\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TRIFA_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public GroupMessage TRIFA_MESSAGE_TYPELe(int TRIFA_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and \"TRIFA_MESSAGE_TYPE\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TRIFA_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public GroupMessage TRIFA_MESSAGE_TYPEGt(int TRIFA_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and \"TRIFA_MESSAGE_TYPE\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TRIFA_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public GroupMessage TRIFA_MESSAGE_TYPEGe(int TRIFA_MESSAGE_TYPE)
    {
        this.sql_where = this.sql_where + " and \"TRIFA_MESSAGE_TYPE\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TRIFA_MESSAGE_TYPE));
        bind_where_count++;
        return this;
    }

    public GroupMessage TRIFA_MESSAGE_TYPEBetween(int TRIFA_MESSAGE_TYPE1, int TRIFA_MESSAGE_TYPE2)
    {
        this.sql_where = this.sql_where + " and \"TRIFA_MESSAGE_TYPE\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and TRIFA_MESSAGE_TYPE<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TRIFA_MESSAGE_TYPE1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TRIFA_MESSAGE_TYPE2));
        bind_where_count++;
        return this;
    }

    public GroupMessage TRIFA_MESSAGE_TYPEIsNull()
    {
        this.sql_where = this.sql_where + " and \"TRIFA_MESSAGE_TYPE\" IS NULL ";
        return this;
    }

    public GroupMessage TRIFA_MESSAGE_TYPEIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"TRIFA_MESSAGE_TYPE\" IS NOT NULL ";
        return this;
    }

    public GroupMessage sent_timestampEq(long sent_timestamp)
    {
        this.sql_where = this.sql_where + " and \"sent_timestamp\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp));
        bind_where_count++;
        return this;
    }

    public GroupMessage sent_timestampNotEq(long sent_timestamp)
    {
        this.sql_where = this.sql_where + " and \"sent_timestamp\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp));
        bind_where_count++;
        return this;
    }

    public GroupMessage sent_timestampLt(long sent_timestamp)
    {
        this.sql_where = this.sql_where + " and \"sent_timestamp\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp));
        bind_where_count++;
        return this;
    }

    public GroupMessage sent_timestampLe(long sent_timestamp)
    {
        this.sql_where = this.sql_where + " and \"sent_timestamp\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp));
        bind_where_count++;
        return this;
    }

    public GroupMessage sent_timestampGt(long sent_timestamp)
    {
        this.sql_where = this.sql_where + " and \"sent_timestamp\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp));
        bind_where_count++;
        return this;
    }

    public GroupMessage sent_timestampGe(long sent_timestamp)
    {
        this.sql_where = this.sql_where + " and \"sent_timestamp\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp));
        bind_where_count++;
        return this;
    }

    public GroupMessage sent_timestampBetween(long sent_timestamp1, long sent_timestamp2)
    {
        this.sql_where = this.sql_where + " and \"sent_timestamp\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and sent_timestamp<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, sent_timestamp2));
        bind_where_count++;
        return this;
    }

    public GroupMessage sent_timestampIsNull()
    {
        this.sql_where = this.sql_where + " and \"sent_timestamp\" IS NULL ";
        return this;
    }

    public GroupMessage sent_timestampIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"sent_timestamp\" IS NOT NULL ";
        return this;
    }

    public GroupMessage rcvd_timestampEq(long rcvd_timestamp)
    {
        this.sql_where = this.sql_where + " and \"rcvd_timestamp\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp));
        bind_where_count++;
        return this;
    }

    public GroupMessage rcvd_timestampNotEq(long rcvd_timestamp)
    {
        this.sql_where = this.sql_where + " and \"rcvd_timestamp\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp));
        bind_where_count++;
        return this;
    }

    public GroupMessage rcvd_timestampLt(long rcvd_timestamp)
    {
        this.sql_where = this.sql_where + " and \"rcvd_timestamp\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp));
        bind_where_count++;
        return this;
    }

    public GroupMessage rcvd_timestampLe(long rcvd_timestamp)
    {
        this.sql_where = this.sql_where + " and \"rcvd_timestamp\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp));
        bind_where_count++;
        return this;
    }

    public GroupMessage rcvd_timestampGt(long rcvd_timestamp)
    {
        this.sql_where = this.sql_where + " and \"rcvd_timestamp\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp));
        bind_where_count++;
        return this;
    }

    public GroupMessage rcvd_timestampGe(long rcvd_timestamp)
    {
        this.sql_where = this.sql_where + " and \"rcvd_timestamp\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp));
        bind_where_count++;
        return this;
    }

    public GroupMessage rcvd_timestampBetween(long rcvd_timestamp1, long rcvd_timestamp2)
    {
        this.sql_where = this.sql_where + " and \"rcvd_timestamp\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and rcvd_timestamp<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, rcvd_timestamp2));
        bind_where_count++;
        return this;
    }

    public GroupMessage rcvd_timestampIsNull()
    {
        this.sql_where = this.sql_where + " and \"rcvd_timestamp\" IS NULL ";
        return this;
    }

    public GroupMessage rcvd_timestampIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"rcvd_timestamp\" IS NOT NULL ";
        return this;
    }

    public GroupMessage readEq(boolean read)
    {
        this.sql_where = this.sql_where + " and \"read\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, read));
        bind_where_count++;
        return this;
    }

    public GroupMessage readNotEq(boolean read)
    {
        this.sql_where = this.sql_where + " and \"read\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, read));
        bind_where_count++;
        return this;
    }

    public GroupMessage readIsNull()
    {
        this.sql_where = this.sql_where + " and \"read\" IS NULL ";
        return this;
    }

    public GroupMessage readIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"read\" IS NOT NULL ";
        return this;
    }

    public GroupMessage is_newEq(boolean is_new)
    {
        this.sql_where = this.sql_where + " and \"is_new\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, is_new));
        bind_where_count++;
        return this;
    }

    public GroupMessage is_newNotEq(boolean is_new)
    {
        this.sql_where = this.sql_where + " and \"is_new\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, is_new));
        bind_where_count++;
        return this;
    }

    public GroupMessage is_newIsNull()
    {
        this.sql_where = this.sql_where + " and \"is_new\" IS NULL ";
        return this;
    }

    public GroupMessage is_newIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"is_new\" IS NOT NULL ";
        return this;
    }

    public GroupMessage textEq(String text)
    {
        this.sql_where = this.sql_where + " and \"text\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, text));
        bind_where_count++;
        return this;
    }

    public GroupMessage textNotEq(String text)
    {
        this.sql_where = this.sql_where + " and \"text\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, text));
        bind_where_count++;
        return this;
    }

    public GroupMessage textIsNull()
    {
        this.sql_where = this.sql_where + " and \"text\" IS NULL ";
        return this;
    }

    public GroupMessage textIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"text\" IS NOT NULL ";
        return this;
    }

    public GroupMessage textLike(String text)
    {
        this.sql_where = this.sql_where + " and \"text\" LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, text));
        bind_where_count++;
        return this;
    }

    public GroupMessage textNotLike(String text)
    {
        this.sql_where = this.sql_where + " and \"text\" NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, text));
        bind_where_count++;
        return this;
    }

    public GroupMessage was_syncedEq(boolean was_synced)
    {
        this.sql_where = this.sql_where + " and \"was_synced\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, was_synced));
        bind_where_count++;
        return this;
    }

    public GroupMessage was_syncedNotEq(boolean was_synced)
    {
        this.sql_where = this.sql_where + " and \"was_synced\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, was_synced));
        bind_where_count++;
        return this;
    }

    public GroupMessage was_syncedIsNull()
    {
        this.sql_where = this.sql_where + " and \"was_synced\" IS NULL ";
        return this;
    }

    public GroupMessage was_syncedIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"was_synced\" IS NOT NULL ";
        return this;
    }

    public GroupMessage TRIFA_SYNC_TYPEEq(int TRIFA_SYNC_TYPE)
    {
        this.sql_where = this.sql_where + " and \"TRIFA_SYNC_TYPE\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TRIFA_SYNC_TYPE));
        bind_where_count++;
        return this;
    }

    public GroupMessage TRIFA_SYNC_TYPENotEq(int TRIFA_SYNC_TYPE)
    {
        this.sql_where = this.sql_where + " and \"TRIFA_SYNC_TYPE\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TRIFA_SYNC_TYPE));
        bind_where_count++;
        return this;
    }

    public GroupMessage TRIFA_SYNC_TYPELt(int TRIFA_SYNC_TYPE)
    {
        this.sql_where = this.sql_where + " and \"TRIFA_SYNC_TYPE\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TRIFA_SYNC_TYPE));
        bind_where_count++;
        return this;
    }

    public GroupMessage TRIFA_SYNC_TYPELe(int TRIFA_SYNC_TYPE)
    {
        this.sql_where = this.sql_where + " and \"TRIFA_SYNC_TYPE\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TRIFA_SYNC_TYPE));
        bind_where_count++;
        return this;
    }

    public GroupMessage TRIFA_SYNC_TYPEGt(int TRIFA_SYNC_TYPE)
    {
        this.sql_where = this.sql_where + " and \"TRIFA_SYNC_TYPE\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TRIFA_SYNC_TYPE));
        bind_where_count++;
        return this;
    }

    public GroupMessage TRIFA_SYNC_TYPEGe(int TRIFA_SYNC_TYPE)
    {
        this.sql_where = this.sql_where + " and \"TRIFA_SYNC_TYPE\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TRIFA_SYNC_TYPE));
        bind_where_count++;
        return this;
    }

    public GroupMessage TRIFA_SYNC_TYPEBetween(int TRIFA_SYNC_TYPE1, int TRIFA_SYNC_TYPE2)
    {
        this.sql_where = this.sql_where + " and \"TRIFA_SYNC_TYPE\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and TRIFA_SYNC_TYPE<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TRIFA_SYNC_TYPE1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, TRIFA_SYNC_TYPE2));
        bind_where_count++;
        return this;
    }

    public GroupMessage TRIFA_SYNC_TYPEIsNull()
    {
        this.sql_where = this.sql_where + " and \"TRIFA_SYNC_TYPE\" IS NULL ";
        return this;
    }

    public GroupMessage TRIFA_SYNC_TYPEIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"TRIFA_SYNC_TYPE\" IS NOT NULL ";
        return this;
    }

    public GroupMessage sync_confirmationsEq(int sync_confirmations)
    {
        this.sql_where = this.sql_where + " and \"sync_confirmations\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, sync_confirmations));
        bind_where_count++;
        return this;
    }

    public GroupMessage sync_confirmationsNotEq(int sync_confirmations)
    {
        this.sql_where = this.sql_where + " and \"sync_confirmations\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, sync_confirmations));
        bind_where_count++;
        return this;
    }

    public GroupMessage sync_confirmationsLt(int sync_confirmations)
    {
        this.sql_where = this.sql_where + " and \"sync_confirmations\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, sync_confirmations));
        bind_where_count++;
        return this;
    }

    public GroupMessage sync_confirmationsLe(int sync_confirmations)
    {
        this.sql_where = this.sql_where + " and \"sync_confirmations\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, sync_confirmations));
        bind_where_count++;
        return this;
    }

    public GroupMessage sync_confirmationsGt(int sync_confirmations)
    {
        this.sql_where = this.sql_where + " and \"sync_confirmations\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, sync_confirmations));
        bind_where_count++;
        return this;
    }

    public GroupMessage sync_confirmationsGe(int sync_confirmations)
    {
        this.sql_where = this.sql_where + " and \"sync_confirmations\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, sync_confirmations));
        bind_where_count++;
        return this;
    }

    public GroupMessage sync_confirmationsBetween(int sync_confirmations1, int sync_confirmations2)
    {
        this.sql_where = this.sql_where + " and \"sync_confirmations\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and sync_confirmations<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, sync_confirmations1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, sync_confirmations2));
        bind_where_count++;
        return this;
    }

    public GroupMessage sync_confirmationsIsNull()
    {
        this.sql_where = this.sql_where + " and \"sync_confirmations\" IS NULL ";
        return this;
    }

    public GroupMessage sync_confirmationsIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"sync_confirmations\" IS NOT NULL ";
        return this;
    }

    public GroupMessage tox_group_peer_pubkey_syncer_01Eq(String tox_group_peer_pubkey_syncer_01)
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_pubkey_syncer_01\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_group_peer_pubkey_syncer_01));
        bind_where_count++;
        return this;
    }

    public GroupMessage tox_group_peer_pubkey_syncer_01NotEq(String tox_group_peer_pubkey_syncer_01)
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_pubkey_syncer_01\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_group_peer_pubkey_syncer_01));
        bind_where_count++;
        return this;
    }

    public GroupMessage tox_group_peer_pubkey_syncer_01IsNull()
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_pubkey_syncer_01\" IS NULL ";
        return this;
    }

    public GroupMessage tox_group_peer_pubkey_syncer_01IsNotNull()
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_pubkey_syncer_01\" IS NOT NULL ";
        return this;
    }

    public GroupMessage tox_group_peer_pubkey_syncer_01Like(String tox_group_peer_pubkey_syncer_01)
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_pubkey_syncer_01\" LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_group_peer_pubkey_syncer_01));
        bind_where_count++;
        return this;
    }

    public GroupMessage tox_group_peer_pubkey_syncer_01NotLike(String tox_group_peer_pubkey_syncer_01)
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_pubkey_syncer_01\" NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_group_peer_pubkey_syncer_01));
        bind_where_count++;
        return this;
    }

    public GroupMessage tox_group_peer_pubkey_syncer_02Eq(String tox_group_peer_pubkey_syncer_02)
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_pubkey_syncer_02\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_group_peer_pubkey_syncer_02));
        bind_where_count++;
        return this;
    }

    public GroupMessage tox_group_peer_pubkey_syncer_02NotEq(String tox_group_peer_pubkey_syncer_02)
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_pubkey_syncer_02\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_group_peer_pubkey_syncer_02));
        bind_where_count++;
        return this;
    }

    public GroupMessage tox_group_peer_pubkey_syncer_02IsNull()
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_pubkey_syncer_02\" IS NULL ";
        return this;
    }

    public GroupMessage tox_group_peer_pubkey_syncer_02IsNotNull()
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_pubkey_syncer_02\" IS NOT NULL ";
        return this;
    }

    public GroupMessage tox_group_peer_pubkey_syncer_02Like(String tox_group_peer_pubkey_syncer_02)
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_pubkey_syncer_02\" LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_group_peer_pubkey_syncer_02));
        bind_where_count++;
        return this;
    }

    public GroupMessage tox_group_peer_pubkey_syncer_02NotLike(String tox_group_peer_pubkey_syncer_02)
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_pubkey_syncer_02\" NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_group_peer_pubkey_syncer_02));
        bind_where_count++;
        return this;
    }

    public GroupMessage tox_group_peer_pubkey_syncer_03Eq(String tox_group_peer_pubkey_syncer_03)
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_pubkey_syncer_03\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_group_peer_pubkey_syncer_03));
        bind_where_count++;
        return this;
    }

    public GroupMessage tox_group_peer_pubkey_syncer_03NotEq(String tox_group_peer_pubkey_syncer_03)
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_pubkey_syncer_03\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_group_peer_pubkey_syncer_03));
        bind_where_count++;
        return this;
    }

    public GroupMessage tox_group_peer_pubkey_syncer_03IsNull()
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_pubkey_syncer_03\" IS NULL ";
        return this;
    }

    public GroupMessage tox_group_peer_pubkey_syncer_03IsNotNull()
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_pubkey_syncer_03\" IS NOT NULL ";
        return this;
    }

    public GroupMessage tox_group_peer_pubkey_syncer_03Like(String tox_group_peer_pubkey_syncer_03)
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_pubkey_syncer_03\" LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_group_peer_pubkey_syncer_03));
        bind_where_count++;
        return this;
    }

    public GroupMessage tox_group_peer_pubkey_syncer_03NotLike(String tox_group_peer_pubkey_syncer_03)
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_pubkey_syncer_03\" NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_group_peer_pubkey_syncer_03));
        bind_where_count++;
        return this;
    }

    public GroupMessage tox_group_peer_pubkey_syncer_01_sent_timestampEq(long tox_group_peer_pubkey_syncer_01_sent_timestamp)
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_pubkey_syncer_01_sent_timestamp\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, tox_group_peer_pubkey_syncer_01_sent_timestamp));
        bind_where_count++;
        return this;
    }

    public GroupMessage tox_group_peer_pubkey_syncer_01_sent_timestampNotEq(long tox_group_peer_pubkey_syncer_01_sent_timestamp)
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_pubkey_syncer_01_sent_timestamp\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, tox_group_peer_pubkey_syncer_01_sent_timestamp));
        bind_where_count++;
        return this;
    }

    public GroupMessage tox_group_peer_pubkey_syncer_01_sent_timestampLt(long tox_group_peer_pubkey_syncer_01_sent_timestamp)
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_pubkey_syncer_01_sent_timestamp\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, tox_group_peer_pubkey_syncer_01_sent_timestamp));
        bind_where_count++;
        return this;
    }

    public GroupMessage tox_group_peer_pubkey_syncer_01_sent_timestampLe(long tox_group_peer_pubkey_syncer_01_sent_timestamp)
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_pubkey_syncer_01_sent_timestamp\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, tox_group_peer_pubkey_syncer_01_sent_timestamp));
        bind_where_count++;
        return this;
    }

    public GroupMessage tox_group_peer_pubkey_syncer_01_sent_timestampGt(long tox_group_peer_pubkey_syncer_01_sent_timestamp)
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_pubkey_syncer_01_sent_timestamp\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, tox_group_peer_pubkey_syncer_01_sent_timestamp));
        bind_where_count++;
        return this;
    }

    public GroupMessage tox_group_peer_pubkey_syncer_01_sent_timestampGe(long tox_group_peer_pubkey_syncer_01_sent_timestamp)
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_pubkey_syncer_01_sent_timestamp\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, tox_group_peer_pubkey_syncer_01_sent_timestamp));
        bind_where_count++;
        return this;
    }

    public GroupMessage tox_group_peer_pubkey_syncer_01_sent_timestampBetween(long tox_group_peer_pubkey_syncer_01_sent_timestamp1, long tox_group_peer_pubkey_syncer_01_sent_timestamp2)
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_pubkey_syncer_01_sent_timestamp\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and tox_group_peer_pubkey_syncer_01_sent_timestamp<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, tox_group_peer_pubkey_syncer_01_sent_timestamp1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, tox_group_peer_pubkey_syncer_01_sent_timestamp2));
        bind_where_count++;
        return this;
    }

    public GroupMessage tox_group_peer_pubkey_syncer_01_sent_timestampIsNull()
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_pubkey_syncer_01_sent_timestamp\" IS NULL ";
        return this;
    }

    public GroupMessage tox_group_peer_pubkey_syncer_01_sent_timestampIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_pubkey_syncer_01_sent_timestamp\" IS NOT NULL ";
        return this;
    }

    public GroupMessage tox_group_peer_pubkey_syncer_02_sent_timestampEq(long tox_group_peer_pubkey_syncer_02_sent_timestamp)
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_pubkey_syncer_02_sent_timestamp\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, tox_group_peer_pubkey_syncer_02_sent_timestamp));
        bind_where_count++;
        return this;
    }

    public GroupMessage tox_group_peer_pubkey_syncer_02_sent_timestampNotEq(long tox_group_peer_pubkey_syncer_02_sent_timestamp)
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_pubkey_syncer_02_sent_timestamp\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, tox_group_peer_pubkey_syncer_02_sent_timestamp));
        bind_where_count++;
        return this;
    }

    public GroupMessage tox_group_peer_pubkey_syncer_02_sent_timestampLt(long tox_group_peer_pubkey_syncer_02_sent_timestamp)
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_pubkey_syncer_02_sent_timestamp\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, tox_group_peer_pubkey_syncer_02_sent_timestamp));
        bind_where_count++;
        return this;
    }

    public GroupMessage tox_group_peer_pubkey_syncer_02_sent_timestampLe(long tox_group_peer_pubkey_syncer_02_sent_timestamp)
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_pubkey_syncer_02_sent_timestamp\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, tox_group_peer_pubkey_syncer_02_sent_timestamp));
        bind_where_count++;
        return this;
    }

    public GroupMessage tox_group_peer_pubkey_syncer_02_sent_timestampGt(long tox_group_peer_pubkey_syncer_02_sent_timestamp)
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_pubkey_syncer_02_sent_timestamp\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, tox_group_peer_pubkey_syncer_02_sent_timestamp));
        bind_where_count++;
        return this;
    }

    public GroupMessage tox_group_peer_pubkey_syncer_02_sent_timestampGe(long tox_group_peer_pubkey_syncer_02_sent_timestamp)
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_pubkey_syncer_02_sent_timestamp\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, tox_group_peer_pubkey_syncer_02_sent_timestamp));
        bind_where_count++;
        return this;
    }

    public GroupMessage tox_group_peer_pubkey_syncer_02_sent_timestampBetween(long tox_group_peer_pubkey_syncer_02_sent_timestamp1, long tox_group_peer_pubkey_syncer_02_sent_timestamp2)
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_pubkey_syncer_02_sent_timestamp\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and tox_group_peer_pubkey_syncer_02_sent_timestamp<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, tox_group_peer_pubkey_syncer_02_sent_timestamp1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, tox_group_peer_pubkey_syncer_02_sent_timestamp2));
        bind_where_count++;
        return this;
    }

    public GroupMessage tox_group_peer_pubkey_syncer_02_sent_timestampIsNull()
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_pubkey_syncer_02_sent_timestamp\" IS NULL ";
        return this;
    }

    public GroupMessage tox_group_peer_pubkey_syncer_02_sent_timestampIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_pubkey_syncer_02_sent_timestamp\" IS NOT NULL ";
        return this;
    }

    public GroupMessage tox_group_peer_pubkey_syncer_03_sent_timestampEq(long tox_group_peer_pubkey_syncer_03_sent_timestamp)
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_pubkey_syncer_03_sent_timestamp\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, tox_group_peer_pubkey_syncer_03_sent_timestamp));
        bind_where_count++;
        return this;
    }

    public GroupMessage tox_group_peer_pubkey_syncer_03_sent_timestampNotEq(long tox_group_peer_pubkey_syncer_03_sent_timestamp)
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_pubkey_syncer_03_sent_timestamp\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, tox_group_peer_pubkey_syncer_03_sent_timestamp));
        bind_where_count++;
        return this;
    }

    public GroupMessage tox_group_peer_pubkey_syncer_03_sent_timestampLt(long tox_group_peer_pubkey_syncer_03_sent_timestamp)
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_pubkey_syncer_03_sent_timestamp\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, tox_group_peer_pubkey_syncer_03_sent_timestamp));
        bind_where_count++;
        return this;
    }

    public GroupMessage tox_group_peer_pubkey_syncer_03_sent_timestampLe(long tox_group_peer_pubkey_syncer_03_sent_timestamp)
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_pubkey_syncer_03_sent_timestamp\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, tox_group_peer_pubkey_syncer_03_sent_timestamp));
        bind_where_count++;
        return this;
    }

    public GroupMessage tox_group_peer_pubkey_syncer_03_sent_timestampGt(long tox_group_peer_pubkey_syncer_03_sent_timestamp)
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_pubkey_syncer_03_sent_timestamp\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, tox_group_peer_pubkey_syncer_03_sent_timestamp));
        bind_where_count++;
        return this;
    }

    public GroupMessage tox_group_peer_pubkey_syncer_03_sent_timestampGe(long tox_group_peer_pubkey_syncer_03_sent_timestamp)
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_pubkey_syncer_03_sent_timestamp\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, tox_group_peer_pubkey_syncer_03_sent_timestamp));
        bind_where_count++;
        return this;
    }

    public GroupMessage tox_group_peer_pubkey_syncer_03_sent_timestampBetween(long tox_group_peer_pubkey_syncer_03_sent_timestamp1, long tox_group_peer_pubkey_syncer_03_sent_timestamp2)
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_pubkey_syncer_03_sent_timestamp\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and tox_group_peer_pubkey_syncer_03_sent_timestamp<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, tox_group_peer_pubkey_syncer_03_sent_timestamp1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, tox_group_peer_pubkey_syncer_03_sent_timestamp2));
        bind_where_count++;
        return this;
    }

    public GroupMessage tox_group_peer_pubkey_syncer_03_sent_timestampIsNull()
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_pubkey_syncer_03_sent_timestamp\" IS NULL ";
        return this;
    }

    public GroupMessage tox_group_peer_pubkey_syncer_03_sent_timestampIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"tox_group_peer_pubkey_syncer_03_sent_timestamp\" IS NOT NULL ";
        return this;
    }

    public GroupMessage msg_id_hashEq(String msg_id_hash)
    {
        this.sql_where = this.sql_where + " and \"msg_id_hash\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, msg_id_hash));
        bind_where_count++;
        return this;
    }

    public GroupMessage msg_id_hashNotEq(String msg_id_hash)
    {
        this.sql_where = this.sql_where + " and \"msg_id_hash\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, msg_id_hash));
        bind_where_count++;
        return this;
    }

    public GroupMessage msg_id_hashIsNull()
    {
        this.sql_where = this.sql_where + " and \"msg_id_hash\" IS NULL ";
        return this;
    }

    public GroupMessage msg_id_hashIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"msg_id_hash\" IS NOT NULL ";
        return this;
    }

    public GroupMessage msg_id_hashLike(String msg_id_hash)
    {
        this.sql_where = this.sql_where + " and \"msg_id_hash\" LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, msg_id_hash));
        bind_where_count++;
        return this;
    }

    public GroupMessage msg_id_hashNotLike(String msg_id_hash)
    {
        this.sql_where = this.sql_where + " and \"msg_id_hash\" NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, msg_id_hash));
        bind_where_count++;
        return this;
    }

    public GroupMessage sent_privately_to_tox_group_peer_pubkeyEq(String sent_privately_to_tox_group_peer_pubkey)
    {
        this.sql_where = this.sql_where + " and \"sent_privately_to_tox_group_peer_pubkey\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, sent_privately_to_tox_group_peer_pubkey));
        bind_where_count++;
        return this;
    }

    public GroupMessage sent_privately_to_tox_group_peer_pubkeyNotEq(String sent_privately_to_tox_group_peer_pubkey)
    {
        this.sql_where = this.sql_where + " and \"sent_privately_to_tox_group_peer_pubkey\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, sent_privately_to_tox_group_peer_pubkey));
        bind_where_count++;
        return this;
    }

    public GroupMessage sent_privately_to_tox_group_peer_pubkeyIsNull()
    {
        this.sql_where = this.sql_where + " and \"sent_privately_to_tox_group_peer_pubkey\" IS NULL ";
        return this;
    }

    public GroupMessage sent_privately_to_tox_group_peer_pubkeyIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"sent_privately_to_tox_group_peer_pubkey\" IS NOT NULL ";
        return this;
    }

    public GroupMessage sent_privately_to_tox_group_peer_pubkeyLike(String sent_privately_to_tox_group_peer_pubkey)
    {
        this.sql_where = this.sql_where + " and \"sent_privately_to_tox_group_peer_pubkey\" LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, sent_privately_to_tox_group_peer_pubkey));
        bind_where_count++;
        return this;
    }

    public GroupMessage sent_privately_to_tox_group_peer_pubkeyNotLike(String sent_privately_to_tox_group_peer_pubkey)
    {
        this.sql_where = this.sql_where + " and \"sent_privately_to_tox_group_peer_pubkey\" NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, sent_privately_to_tox_group_peer_pubkey));
        bind_where_count++;
        return this;
    }

    public GroupMessage path_nameEq(String path_name)
    {
        this.sql_where = this.sql_where + " and \"path_name\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, path_name));
        bind_where_count++;
        return this;
    }

    public GroupMessage path_nameNotEq(String path_name)
    {
        this.sql_where = this.sql_where + " and \"path_name\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, path_name));
        bind_where_count++;
        return this;
    }

    public GroupMessage path_nameIsNull()
    {
        this.sql_where = this.sql_where + " and \"path_name\" IS NULL ";
        return this;
    }

    public GroupMessage path_nameIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"path_name\" IS NOT NULL ";
        return this;
    }

    public GroupMessage path_nameLike(String path_name)
    {
        this.sql_where = this.sql_where + " and \"path_name\" LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, path_name));
        bind_where_count++;
        return this;
    }

    public GroupMessage path_nameNotLike(String path_name)
    {
        this.sql_where = this.sql_where + " and \"path_name\" NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, path_name));
        bind_where_count++;
        return this;
    }

    public GroupMessage file_nameEq(String file_name)
    {
        this.sql_where = this.sql_where + " and \"file_name\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, file_name));
        bind_where_count++;
        return this;
    }

    public GroupMessage file_nameNotEq(String file_name)
    {
        this.sql_where = this.sql_where + " and \"file_name\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, file_name));
        bind_where_count++;
        return this;
    }

    public GroupMessage file_nameIsNull()
    {
        this.sql_where = this.sql_where + " and \"file_name\" IS NULL ";
        return this;
    }

    public GroupMessage file_nameIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"file_name\" IS NOT NULL ";
        return this;
    }

    public GroupMessage file_nameLike(String file_name)
    {
        this.sql_where = this.sql_where + " and \"file_name\" LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, file_name));
        bind_where_count++;
        return this;
    }

    public GroupMessage file_nameNotLike(String file_name)
    {
        this.sql_where = this.sql_where + " and \"file_name\" NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, file_name));
        bind_where_count++;
        return this;
    }

    public GroupMessage filename_fullpathEq(String filename_fullpath)
    {
        this.sql_where = this.sql_where + " and \"filename_fullpath\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, filename_fullpath));
        bind_where_count++;
        return this;
    }

    public GroupMessage filename_fullpathNotEq(String filename_fullpath)
    {
        this.sql_where = this.sql_where + " and \"filename_fullpath\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, filename_fullpath));
        bind_where_count++;
        return this;
    }

    public GroupMessage filename_fullpathIsNull()
    {
        this.sql_where = this.sql_where + " and \"filename_fullpath\" IS NULL ";
        return this;
    }

    public GroupMessage filename_fullpathIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"filename_fullpath\" IS NOT NULL ";
        return this;
    }

    public GroupMessage filename_fullpathLike(String filename_fullpath)
    {
        this.sql_where = this.sql_where + " and \"filename_fullpath\" LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, filename_fullpath));
        bind_where_count++;
        return this;
    }

    public GroupMessage filename_fullpathNotLike(String filename_fullpath)
    {
        this.sql_where = this.sql_where + " and \"filename_fullpath\" NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, filename_fullpath));
        bind_where_count++;
        return this;
    }

    public GroupMessage filesizeEq(long filesize)
    {
        this.sql_where = this.sql_where + " and \"filesize\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filesize));
        bind_where_count++;
        return this;
    }

    public GroupMessage filesizeNotEq(long filesize)
    {
        this.sql_where = this.sql_where + " and \"filesize\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filesize));
        bind_where_count++;
        return this;
    }

    public GroupMessage filesizeLt(long filesize)
    {
        this.sql_where = this.sql_where + " and \"filesize\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filesize));
        bind_where_count++;
        return this;
    }

    public GroupMessage filesizeLe(long filesize)
    {
        this.sql_where = this.sql_where + " and \"filesize\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filesize));
        bind_where_count++;
        return this;
    }

    public GroupMessage filesizeGt(long filesize)
    {
        this.sql_where = this.sql_where + " and \"filesize\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filesize));
        bind_where_count++;
        return this;
    }

    public GroupMessage filesizeGe(long filesize)
    {
        this.sql_where = this.sql_where + " and \"filesize\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filesize));
        bind_where_count++;
        return this;
    }

    public GroupMessage filesizeBetween(long filesize1, long filesize2)
    {
        this.sql_where = this.sql_where + " and \"filesize\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and filesize<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filesize1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filesize2));
        bind_where_count++;
        return this;
    }

    public GroupMessage filesizeIsNull()
    {
        this.sql_where = this.sql_where + " and \"filesize\" IS NULL ";
        return this;
    }

    public GroupMessage filesizeIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"filesize\" IS NOT NULL ";
        return this;
    }

    public GroupMessage storage_frame_workEq(boolean storage_frame_work)
    {
        this.sql_where = this.sql_where + " and \"storage_frame_work\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, storage_frame_work));
        bind_where_count++;
        return this;
    }

    public GroupMessage storage_frame_workNotEq(boolean storage_frame_work)
    {
        this.sql_where = this.sql_where + " and \"storage_frame_work\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, storage_frame_work));
        bind_where_count++;
        return this;
    }

    public GroupMessage storage_frame_workIsNull()
    {
        this.sql_where = this.sql_where + " and \"storage_frame_work\" IS NULL ";
        return this;
    }

    public GroupMessage storage_frame_workIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"storage_frame_work\" IS NOT NULL ";
        return this;
    }


    // ----------------- OrderBy funcs ------------------ //
    public GroupMessage orderByIdAsc()
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

    public GroupMessage orderByIdDesc()
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

    public GroupMessage orderByMessage_id_toxAsc()
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

    public GroupMessage orderByMessage_id_toxDesc()
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

    public GroupMessage orderByGroup_identifierAsc()
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

    public GroupMessage orderByGroup_identifierDesc()
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

    public GroupMessage orderByTox_group_peer_pubkeyAsc()
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

    public GroupMessage orderByTox_group_peer_pubkeyDesc()
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

    public GroupMessage orderByTox_group_peer_roleAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"tox_group_peer_role\" ASC ";
        return this;
    }

    public GroupMessage orderByTox_group_peer_roleDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"tox_group_peer_role\" DESC ";
        return this;
    }

    public GroupMessage orderByPrivate_messageAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"private_message\" ASC ";
        return this;
    }

    public GroupMessage orderByPrivate_messageDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"private_message\" DESC ";
        return this;
    }

    public GroupMessage orderByTox_group_peernameAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"tox_group_peername\" ASC ";
        return this;
    }

    public GroupMessage orderByTox_group_peernameDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"tox_group_peername\" DESC ";
        return this;
    }

    public GroupMessage orderByDirectionAsc()
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

    public GroupMessage orderByDirectionDesc()
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

    public GroupMessage orderByTOX_MESSAGE_TYPEAsc()
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

    public GroupMessage orderByTOX_MESSAGE_TYPEDesc()
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

    public GroupMessage orderByTRIFA_MESSAGE_TYPEAsc()
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

    public GroupMessage orderByTRIFA_MESSAGE_TYPEDesc()
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

    public GroupMessage orderBySent_timestampAsc()
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

    public GroupMessage orderBySent_timestampDesc()
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

    public GroupMessage orderByRcvd_timestampAsc()
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

    public GroupMessage orderByRcvd_timestampDesc()
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

    public GroupMessage orderByReadAsc()
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

    public GroupMessage orderByReadDesc()
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

    public GroupMessage orderByIs_newAsc()
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

    public GroupMessage orderByIs_newDesc()
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

    public GroupMessage orderByTextAsc()
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

    public GroupMessage orderByTextDesc()
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

    public GroupMessage orderByWas_syncedAsc()
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

    public GroupMessage orderByWas_syncedDesc()
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

    public GroupMessage orderByTRIFA_SYNC_TYPEAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"TRIFA_SYNC_TYPE\" ASC ";
        return this;
    }

    public GroupMessage orderByTRIFA_SYNC_TYPEDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"TRIFA_SYNC_TYPE\" DESC ";
        return this;
    }

    public GroupMessage orderBySync_confirmationsAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"sync_confirmations\" ASC ";
        return this;
    }

    public GroupMessage orderBySync_confirmationsDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"sync_confirmations\" DESC ";
        return this;
    }

    public GroupMessage orderByTox_group_peer_pubkey_syncer_01Asc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"tox_group_peer_pubkey_syncer_01\" ASC ";
        return this;
    }

    public GroupMessage orderByTox_group_peer_pubkey_syncer_01Desc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"tox_group_peer_pubkey_syncer_01\" DESC ";
        return this;
    }

    public GroupMessage orderByTox_group_peer_pubkey_syncer_02Asc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"tox_group_peer_pubkey_syncer_02\" ASC ";
        return this;
    }

    public GroupMessage orderByTox_group_peer_pubkey_syncer_02Desc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"tox_group_peer_pubkey_syncer_02\" DESC ";
        return this;
    }

    public GroupMessage orderByTox_group_peer_pubkey_syncer_03Asc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"tox_group_peer_pubkey_syncer_03\" ASC ";
        return this;
    }

    public GroupMessage orderByTox_group_peer_pubkey_syncer_03Desc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"tox_group_peer_pubkey_syncer_03\" DESC ";
        return this;
    }

    public GroupMessage orderByTox_group_peer_pubkey_syncer_01_sent_timestampAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"tox_group_peer_pubkey_syncer_01_sent_timestamp\" ASC ";
        return this;
    }

    public GroupMessage orderByTox_group_peer_pubkey_syncer_01_sent_timestampDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"tox_group_peer_pubkey_syncer_01_sent_timestamp\" DESC ";
        return this;
    }

    public GroupMessage orderByTox_group_peer_pubkey_syncer_02_sent_timestampAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"tox_group_peer_pubkey_syncer_02_sent_timestamp\" ASC ";
        return this;
    }

    public GroupMessage orderByTox_group_peer_pubkey_syncer_02_sent_timestampDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"tox_group_peer_pubkey_syncer_02_sent_timestamp\" DESC ";
        return this;
    }

    public GroupMessage orderByTox_group_peer_pubkey_syncer_03_sent_timestampAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"tox_group_peer_pubkey_syncer_03_sent_timestamp\" ASC ";
        return this;
    }

    public GroupMessage orderByTox_group_peer_pubkey_syncer_03_sent_timestampDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"tox_group_peer_pubkey_syncer_03_sent_timestamp\" DESC ";
        return this;
    }

    public GroupMessage orderByMsg_id_hashAsc()
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

    public GroupMessage orderByMsg_id_hashDesc()
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

    public GroupMessage orderBySent_privately_to_tox_group_peer_pubkeyAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"sent_privately_to_tox_group_peer_pubkey\" ASC ";
        return this;
    }

    public GroupMessage orderBySent_privately_to_tox_group_peer_pubkeyDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"sent_privately_to_tox_group_peer_pubkey\" DESC ";
        return this;
    }

    public GroupMessage orderByPath_nameAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"path_name\" ASC ";
        return this;
    }

    public GroupMessage orderByPath_nameDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"path_name\" DESC ";
        return this;
    }

    public GroupMessage orderByFile_nameAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"file_name\" ASC ";
        return this;
    }

    public GroupMessage orderByFile_nameDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"file_name\" DESC ";
        return this;
    }

    public GroupMessage orderByFilename_fullpathAsc()
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

    public GroupMessage orderByFilename_fullpathDesc()
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

    public GroupMessage orderByFilesizeAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"filesize\" ASC ";
        return this;
    }

    public GroupMessage orderByFilesizeDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"filesize\" DESC ";
        return this;
    }

    public GroupMessage orderByStorage_frame_workAsc()
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

    public GroupMessage orderByStorage_frame_workDesc()
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



}

