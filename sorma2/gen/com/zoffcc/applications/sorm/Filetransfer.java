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
public class Filetransfer
{
    private static final String TAG = "DB.Filetransfer";
    @PrimaryKey(autoincrement = true, auto = true)
    public long id;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public String tox_public_key_string;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public int direction;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public long file_number;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public int kind;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public int state;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public boolean ft_accepted;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public boolean ft_outgoing_started;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public String path_name;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public String file_name;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public boolean fos_open;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public long filesize;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public long current_position;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public long message_id;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public boolean storage_frame_work;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    public String tox_file_id_hex;

    public static Filetransfer deep_copy(Filetransfer in)
    {
        Filetransfer out = new Filetransfer();
        out.id = in.id;
        out.tox_public_key_string = in.tox_public_key_string;
        out.direction = in.direction;
        out.file_number = in.file_number;
        out.kind = in.kind;
        out.state = in.state;
        out.ft_accepted = in.ft_accepted;
        out.ft_outgoing_started = in.ft_outgoing_started;
        out.path_name = in.path_name;
        out.file_name = in.file_name;
        out.fos_open = in.fos_open;
        out.filesize = in.filesize;
        out.current_position = in.current_position;
        out.message_id = in.message_id;
        out.storage_frame_work = in.storage_frame_work;
        out.tox_file_id_hex = in.tox_file_id_hex;

        return out;
    }

    @Override
    public String toString()
    {
        return "id=" + id + ", tox_public_key_string=" + tox_public_key_string + ", direction=" + direction + ", file_number=" + file_number + ", kind=" + kind + ", state=" + state + ", ft_accepted=" + ft_accepted + ", ft_outgoing_started=" + ft_outgoing_started + ", path_name=" + path_name + ", file_name=" + file_name + ", fos_open=" + fos_open + ", filesize=" + filesize + ", current_position=" + current_position + ", message_id=" + message_id + ", storage_frame_work=" + storage_frame_work + ", tox_file_id_hex=" + tox_file_id_hex;
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

    public List<Filetransfer> toList()
    {
        List<Filetransfer> list = new ArrayList<>();
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
                Filetransfer out = new Filetransfer();
                out.id = rs.getLong("id");
                out.tox_public_key_string = rs.getString("tox_public_key_string");
                out.direction = rs.getInt("direction");
                out.file_number = rs.getLong("file_number");
                out.kind = rs.getInt("kind");
                out.state = rs.getInt("state");
                out.ft_accepted = rs.getBoolean("ft_accepted");
                out.ft_outgoing_started = rs.getBoolean("ft_outgoing_started");
                out.path_name = rs.getString("path_name");
                out.file_name = rs.getString("file_name");
                out.fos_open = rs.getBoolean("fos_open");
                out.filesize = rs.getLong("filesize");
                out.current_position = rs.getLong("current_position");
                out.message_id = rs.getLong("message_id");
                out.storage_frame_work = rs.getBoolean("storage_frame_work");
                out.tox_file_id_hex = rs.getString("tox_file_id_hex");

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
                    + ",\"direction\""
                    + ",\"file_number\""
                    + ",\"kind\""
                    + ",\"state\""
                    + ",\"ft_accepted\""
                    + ",\"ft_outgoing_started\""
                    + ",\"path_name\""
                    + ",\"file_name\""
                    + ",\"fos_open\""
                    + ",\"filesize\""
                    + ",\"current_position\""
                    + ",\"message_id\""
                    + ",\"storage_frame_work\""
                    + ",\"tox_file_id_hex\""
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
                    + ")";

            insert_pstmt = sqldb.prepareStatement(insert_pstmt_sql);
            insert_pstmt.clearParameters();

            insert_pstmt.setString(1, this.tox_public_key_string);
            insert_pstmt.setInt(2, this.direction);
            insert_pstmt.setLong(3, this.file_number);
            insert_pstmt.setInt(4, this.kind);
            insert_pstmt.setInt(5, this.state);
            insert_pstmt.setBoolean(6, this.ft_accepted);
            insert_pstmt.setBoolean(7, this.ft_outgoing_started);
            insert_pstmt.setString(8, this.path_name);
            insert_pstmt.setString(9, this.file_name);
            insert_pstmt.setBoolean(10, this.fos_open);
            insert_pstmt.setLong(11, this.filesize);
            insert_pstmt.setLong(12, this.current_position);
            insert_pstmt.setLong(13, this.message_id);
            insert_pstmt.setBoolean(14, this.storage_frame_work);
            insert_pstmt.setString(15, this.tox_file_id_hex);
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

    public Filetransfer get(int i)
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

    public Filetransfer limit(int rowcount)
    {
        this.sql_limit = " limit " + rowcount + " ";
        return this;
    }

    public Filetransfer limit(int rowcount, int offset)
    {
        this.sql_limit = " limit " + offset + " , " + rowcount;
        return this;
    }

    // ----------------------------------- //
    // ----------------------------------- //
    // ----------------------------------- //


    // ----------------- Set funcs ---------------------- //
    public Filetransfer id(long id)
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

    public Filetransfer tox_public_key_string(String tox_public_key_string)
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

    public Filetransfer direction(int direction)
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

    public Filetransfer file_number(long file_number)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"file_number\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, file_number));
        bind_set_count++;
        return this;
    }

    public Filetransfer kind(int kind)
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

    public Filetransfer state(int state)
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

    public Filetransfer ft_accepted(boolean ft_accepted)
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

    public Filetransfer ft_outgoing_started(boolean ft_outgoing_started)
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

    public Filetransfer path_name(String path_name)
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

    public Filetransfer file_name(String file_name)
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

    public Filetransfer fos_open(boolean fos_open)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"fos_open\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, fos_open));
        bind_set_count++;
        return this;
    }

    public Filetransfer filesize(long filesize)
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

    public Filetransfer current_position(long current_position)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"current_position\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, current_position));
        bind_set_count++;
        return this;
    }

    public Filetransfer message_id(long message_id)
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

    public Filetransfer storage_frame_work(boolean storage_frame_work)
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

    public Filetransfer tox_file_id_hex(String tox_file_id_hex)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " \"tox_file_id_hex\"=?" + (BINDVAR_OFFSET_SET + bind_set_count) + " ";
        bind_set_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_file_id_hex));
        bind_set_count++;
        return this;
    }


    // ----------------- Eq/Gt/Lt funcs ----------------- //
    public Filetransfer idEq(long id)
    {
        this.sql_where = this.sql_where + " and \"id\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public Filetransfer idNotEq(long id)
    {
        this.sql_where = this.sql_where + " and \"id\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public Filetransfer idLt(long id)
    {
        this.sql_where = this.sql_where + " and \"id\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public Filetransfer idLe(long id)
    {
        this.sql_where = this.sql_where + " and \"id\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public Filetransfer idGt(long id)
    {
        this.sql_where = this.sql_where + " and \"id\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public Filetransfer idGe(long id)
    {
        this.sql_where = this.sql_where + " and \"id\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id));
        bind_where_count++;
        return this;
    }

    public Filetransfer idBetween(long id1, long id2)
    {
        this.sql_where = this.sql_where + " and \"id\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and id<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, id2));
        bind_where_count++;
        return this;
    }

    public Filetransfer idIsNull()
    {
        this.sql_where = this.sql_where + " and \"id\" IS NULL ";
        return this;
    }

    public Filetransfer idIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"id\" IS NOT NULL ";
        return this;
    }

    public Filetransfer tox_public_key_stringEq(String tox_public_key_string)
    {
        this.sql_where = this.sql_where + " and \"tox_public_key_string\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_public_key_string));
        bind_where_count++;
        return this;
    }

    public Filetransfer tox_public_key_stringNotEq(String tox_public_key_string)
    {
        this.sql_where = this.sql_where + " and \"tox_public_key_string\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_public_key_string));
        bind_where_count++;
        return this;
    }

    public Filetransfer tox_public_key_stringIsNull()
    {
        this.sql_where = this.sql_where + " and \"tox_public_key_string\" IS NULL ";
        return this;
    }

    public Filetransfer tox_public_key_stringIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"tox_public_key_string\" IS NOT NULL ";
        return this;
    }

    public Filetransfer tox_public_key_stringLike(String tox_public_key_string)
    {
        this.sql_where = this.sql_where + " and \"tox_public_key_string\" LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_public_key_string));
        bind_where_count++;
        return this;
    }

    public Filetransfer tox_public_key_stringNotLike(String tox_public_key_string)
    {
        this.sql_where = this.sql_where + " and \"tox_public_key_string\" NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_public_key_string));
        bind_where_count++;
        return this;
    }

    public Filetransfer directionEq(int direction)
    {
        this.sql_where = this.sql_where + " and \"direction\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, direction));
        bind_where_count++;
        return this;
    }

    public Filetransfer directionNotEq(int direction)
    {
        this.sql_where = this.sql_where + " and \"direction\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, direction));
        bind_where_count++;
        return this;
    }

    public Filetransfer directionLt(int direction)
    {
        this.sql_where = this.sql_where + " and \"direction\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, direction));
        bind_where_count++;
        return this;
    }

    public Filetransfer directionLe(int direction)
    {
        this.sql_where = this.sql_where + " and \"direction\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, direction));
        bind_where_count++;
        return this;
    }

    public Filetransfer directionGt(int direction)
    {
        this.sql_where = this.sql_where + " and \"direction\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, direction));
        bind_where_count++;
        return this;
    }

    public Filetransfer directionGe(int direction)
    {
        this.sql_where = this.sql_where + " and \"direction\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, direction));
        bind_where_count++;
        return this;
    }

    public Filetransfer directionBetween(int direction1, int direction2)
    {
        this.sql_where = this.sql_where + " and \"direction\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and direction<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, direction1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, direction2));
        bind_where_count++;
        return this;
    }

    public Filetransfer directionIsNull()
    {
        this.sql_where = this.sql_where + " and \"direction\" IS NULL ";
        return this;
    }

    public Filetransfer directionIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"direction\" IS NOT NULL ";
        return this;
    }

    public Filetransfer file_numberEq(long file_number)
    {
        this.sql_where = this.sql_where + " and \"file_number\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, file_number));
        bind_where_count++;
        return this;
    }

    public Filetransfer file_numberNotEq(long file_number)
    {
        this.sql_where = this.sql_where + " and \"file_number\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, file_number));
        bind_where_count++;
        return this;
    }

    public Filetransfer file_numberLt(long file_number)
    {
        this.sql_where = this.sql_where + " and \"file_number\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, file_number));
        bind_where_count++;
        return this;
    }

    public Filetransfer file_numberLe(long file_number)
    {
        this.sql_where = this.sql_where + " and \"file_number\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, file_number));
        bind_where_count++;
        return this;
    }

    public Filetransfer file_numberGt(long file_number)
    {
        this.sql_where = this.sql_where + " and \"file_number\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, file_number));
        bind_where_count++;
        return this;
    }

    public Filetransfer file_numberGe(long file_number)
    {
        this.sql_where = this.sql_where + " and \"file_number\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, file_number));
        bind_where_count++;
        return this;
    }

    public Filetransfer file_numberBetween(long file_number1, long file_number2)
    {
        this.sql_where = this.sql_where + " and \"file_number\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and file_number<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, file_number1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, file_number2));
        bind_where_count++;
        return this;
    }

    public Filetransfer file_numberIsNull()
    {
        this.sql_where = this.sql_where + " and \"file_number\" IS NULL ";
        return this;
    }

    public Filetransfer file_numberIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"file_number\" IS NOT NULL ";
        return this;
    }

    public Filetransfer kindEq(int kind)
    {
        this.sql_where = this.sql_where + " and \"kind\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, kind));
        bind_where_count++;
        return this;
    }

    public Filetransfer kindNotEq(int kind)
    {
        this.sql_where = this.sql_where + " and \"kind\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, kind));
        bind_where_count++;
        return this;
    }

    public Filetransfer kindLt(int kind)
    {
        this.sql_where = this.sql_where + " and \"kind\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, kind));
        bind_where_count++;
        return this;
    }

    public Filetransfer kindLe(int kind)
    {
        this.sql_where = this.sql_where + " and \"kind\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, kind));
        bind_where_count++;
        return this;
    }

    public Filetransfer kindGt(int kind)
    {
        this.sql_where = this.sql_where + " and \"kind\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, kind));
        bind_where_count++;
        return this;
    }

    public Filetransfer kindGe(int kind)
    {
        this.sql_where = this.sql_where + " and \"kind\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, kind));
        bind_where_count++;
        return this;
    }

    public Filetransfer kindBetween(int kind1, int kind2)
    {
        this.sql_where = this.sql_where + " and \"kind\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and kind<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, kind1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, kind2));
        bind_where_count++;
        return this;
    }

    public Filetransfer kindIsNull()
    {
        this.sql_where = this.sql_where + " and \"kind\" IS NULL ";
        return this;
    }

    public Filetransfer kindIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"kind\" IS NOT NULL ";
        return this;
    }

    public Filetransfer stateEq(int state)
    {
        this.sql_where = this.sql_where + " and \"state\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, state));
        bind_where_count++;
        return this;
    }

    public Filetransfer stateNotEq(int state)
    {
        this.sql_where = this.sql_where + " and \"state\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, state));
        bind_where_count++;
        return this;
    }

    public Filetransfer stateLt(int state)
    {
        this.sql_where = this.sql_where + " and \"state\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, state));
        bind_where_count++;
        return this;
    }

    public Filetransfer stateLe(int state)
    {
        this.sql_where = this.sql_where + " and \"state\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, state));
        bind_where_count++;
        return this;
    }

    public Filetransfer stateGt(int state)
    {
        this.sql_where = this.sql_where + " and \"state\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, state));
        bind_where_count++;
        return this;
    }

    public Filetransfer stateGe(int state)
    {
        this.sql_where = this.sql_where + " and \"state\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, state));
        bind_where_count++;
        return this;
    }

    public Filetransfer stateBetween(int state1, int state2)
    {
        this.sql_where = this.sql_where + " and \"state\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and state<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, state1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Int, state2));
        bind_where_count++;
        return this;
    }

    public Filetransfer stateIsNull()
    {
        this.sql_where = this.sql_where + " and \"state\" IS NULL ";
        return this;
    }

    public Filetransfer stateIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"state\" IS NOT NULL ";
        return this;
    }

    public Filetransfer ft_acceptedEq(boolean ft_accepted)
    {
        this.sql_where = this.sql_where + " and \"ft_accepted\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, ft_accepted));
        bind_where_count++;
        return this;
    }

    public Filetransfer ft_acceptedNotEq(boolean ft_accepted)
    {
        this.sql_where = this.sql_where + " and \"ft_accepted\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, ft_accepted));
        bind_where_count++;
        return this;
    }

    public Filetransfer ft_acceptedIsNull()
    {
        this.sql_where = this.sql_where + " and \"ft_accepted\" IS NULL ";
        return this;
    }

    public Filetransfer ft_acceptedIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"ft_accepted\" IS NOT NULL ";
        return this;
    }

    public Filetransfer ft_outgoing_startedEq(boolean ft_outgoing_started)
    {
        this.sql_where = this.sql_where + " and \"ft_outgoing_started\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, ft_outgoing_started));
        bind_where_count++;
        return this;
    }

    public Filetransfer ft_outgoing_startedNotEq(boolean ft_outgoing_started)
    {
        this.sql_where = this.sql_where + " and \"ft_outgoing_started\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, ft_outgoing_started));
        bind_where_count++;
        return this;
    }

    public Filetransfer ft_outgoing_startedIsNull()
    {
        this.sql_where = this.sql_where + " and \"ft_outgoing_started\" IS NULL ";
        return this;
    }

    public Filetransfer ft_outgoing_startedIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"ft_outgoing_started\" IS NOT NULL ";
        return this;
    }

    public Filetransfer path_nameEq(String path_name)
    {
        this.sql_where = this.sql_where + " and \"path_name\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, path_name));
        bind_where_count++;
        return this;
    }

    public Filetransfer path_nameNotEq(String path_name)
    {
        this.sql_where = this.sql_where + " and \"path_name\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, path_name));
        bind_where_count++;
        return this;
    }

    public Filetransfer path_nameIsNull()
    {
        this.sql_where = this.sql_where + " and \"path_name\" IS NULL ";
        return this;
    }

    public Filetransfer path_nameIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"path_name\" IS NOT NULL ";
        return this;
    }

    public Filetransfer path_nameLike(String path_name)
    {
        this.sql_where = this.sql_where + " and \"path_name\" LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, path_name));
        bind_where_count++;
        return this;
    }

    public Filetransfer path_nameNotLike(String path_name)
    {
        this.sql_where = this.sql_where + " and \"path_name\" NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, path_name));
        bind_where_count++;
        return this;
    }

    public Filetransfer file_nameEq(String file_name)
    {
        this.sql_where = this.sql_where + " and \"file_name\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, file_name));
        bind_where_count++;
        return this;
    }

    public Filetransfer file_nameNotEq(String file_name)
    {
        this.sql_where = this.sql_where + " and \"file_name\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, file_name));
        bind_where_count++;
        return this;
    }

    public Filetransfer file_nameIsNull()
    {
        this.sql_where = this.sql_where + " and \"file_name\" IS NULL ";
        return this;
    }

    public Filetransfer file_nameIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"file_name\" IS NOT NULL ";
        return this;
    }

    public Filetransfer file_nameLike(String file_name)
    {
        this.sql_where = this.sql_where + " and \"file_name\" LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, file_name));
        bind_where_count++;
        return this;
    }

    public Filetransfer file_nameNotLike(String file_name)
    {
        this.sql_where = this.sql_where + " and \"file_name\" NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, file_name));
        bind_where_count++;
        return this;
    }

    public Filetransfer fos_openEq(boolean fos_open)
    {
        this.sql_where = this.sql_where + " and \"fos_open\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, fos_open));
        bind_where_count++;
        return this;
    }

    public Filetransfer fos_openNotEq(boolean fos_open)
    {
        this.sql_where = this.sql_where + " and \"fos_open\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, fos_open));
        bind_where_count++;
        return this;
    }

    public Filetransfer fos_openIsNull()
    {
        this.sql_where = this.sql_where + " and \"fos_open\" IS NULL ";
        return this;
    }

    public Filetransfer fos_openIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"fos_open\" IS NOT NULL ";
        return this;
    }

    public Filetransfer filesizeEq(long filesize)
    {
        this.sql_where = this.sql_where + " and \"filesize\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filesize));
        bind_where_count++;
        return this;
    }

    public Filetransfer filesizeNotEq(long filesize)
    {
        this.sql_where = this.sql_where + " and \"filesize\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filesize));
        bind_where_count++;
        return this;
    }

    public Filetransfer filesizeLt(long filesize)
    {
        this.sql_where = this.sql_where + " and \"filesize\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filesize));
        bind_where_count++;
        return this;
    }

    public Filetransfer filesizeLe(long filesize)
    {
        this.sql_where = this.sql_where + " and \"filesize\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filesize));
        bind_where_count++;
        return this;
    }

    public Filetransfer filesizeGt(long filesize)
    {
        this.sql_where = this.sql_where + " and \"filesize\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filesize));
        bind_where_count++;
        return this;
    }

    public Filetransfer filesizeGe(long filesize)
    {
        this.sql_where = this.sql_where + " and \"filesize\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filesize));
        bind_where_count++;
        return this;
    }

    public Filetransfer filesizeBetween(long filesize1, long filesize2)
    {
        this.sql_where = this.sql_where + " and \"filesize\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and filesize<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filesize1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, filesize2));
        bind_where_count++;
        return this;
    }

    public Filetransfer filesizeIsNull()
    {
        this.sql_where = this.sql_where + " and \"filesize\" IS NULL ";
        return this;
    }

    public Filetransfer filesizeIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"filesize\" IS NOT NULL ";
        return this;
    }

    public Filetransfer current_positionEq(long current_position)
    {
        this.sql_where = this.sql_where + " and \"current_position\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, current_position));
        bind_where_count++;
        return this;
    }

    public Filetransfer current_positionNotEq(long current_position)
    {
        this.sql_where = this.sql_where + " and \"current_position\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, current_position));
        bind_where_count++;
        return this;
    }

    public Filetransfer current_positionLt(long current_position)
    {
        this.sql_where = this.sql_where + " and \"current_position\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, current_position));
        bind_where_count++;
        return this;
    }

    public Filetransfer current_positionLe(long current_position)
    {
        this.sql_where = this.sql_where + " and \"current_position\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, current_position));
        bind_where_count++;
        return this;
    }

    public Filetransfer current_positionGt(long current_position)
    {
        this.sql_where = this.sql_where + " and \"current_position\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, current_position));
        bind_where_count++;
        return this;
    }

    public Filetransfer current_positionGe(long current_position)
    {
        this.sql_where = this.sql_where + " and \"current_position\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, current_position));
        bind_where_count++;
        return this;
    }

    public Filetransfer current_positionBetween(long current_position1, long current_position2)
    {
        this.sql_where = this.sql_where + " and \"current_position\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and current_position<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, current_position1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, current_position2));
        bind_where_count++;
        return this;
    }

    public Filetransfer current_positionIsNull()
    {
        this.sql_where = this.sql_where + " and \"current_position\" IS NULL ";
        return this;
    }

    public Filetransfer current_positionIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"current_position\" IS NOT NULL ";
        return this;
    }

    public Filetransfer message_idEq(long message_id)
    {
        this.sql_where = this.sql_where + " and \"message_id\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, message_id));
        bind_where_count++;
        return this;
    }

    public Filetransfer message_idNotEq(long message_id)
    {
        this.sql_where = this.sql_where + " and \"message_id\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, message_id));
        bind_where_count++;
        return this;
    }

    public Filetransfer message_idLt(long message_id)
    {
        this.sql_where = this.sql_where + " and \"message_id\"<?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, message_id));
        bind_where_count++;
        return this;
    }

    public Filetransfer message_idLe(long message_id)
    {
        this.sql_where = this.sql_where + " and \"message_id\"<=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, message_id));
        bind_where_count++;
        return this;
    }

    public Filetransfer message_idGt(long message_id)
    {
        this.sql_where = this.sql_where + " and \"message_id\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, message_id));
        bind_where_count++;
        return this;
    }

    public Filetransfer message_idGe(long message_id)
    {
        this.sql_where = this.sql_where + " and \"message_id\">=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, message_id));
        bind_where_count++;
        return this;
    }

    public Filetransfer message_idBetween(long message_id1, long message_id2)
    {
        this.sql_where = this.sql_where + " and \"message_id\">?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " and message_id<?" + (BINDVAR_OFFSET_WHERE + 1 + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, message_id1));
        bind_where_count++;
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Long, message_id2));
        bind_where_count++;
        return this;
    }

    public Filetransfer message_idIsNull()
    {
        this.sql_where = this.sql_where + " and \"message_id\" IS NULL ";
        return this;
    }

    public Filetransfer message_idIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"message_id\" IS NOT NULL ";
        return this;
    }

    public Filetransfer storage_frame_workEq(boolean storage_frame_work)
    {
        this.sql_where = this.sql_where + " and \"storage_frame_work\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, storage_frame_work));
        bind_where_count++;
        return this;
    }

    public Filetransfer storage_frame_workNotEq(boolean storage_frame_work)
    {
        this.sql_where = this.sql_where + " and \"storage_frame_work\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_Boolean, storage_frame_work));
        bind_where_count++;
        return this;
    }

    public Filetransfer storage_frame_workIsNull()
    {
        this.sql_where = this.sql_where + " and \"storage_frame_work\" IS NULL ";
        return this;
    }

    public Filetransfer storage_frame_workIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"storage_frame_work\" IS NOT NULL ";
        return this;
    }

    public Filetransfer tox_file_id_hexEq(String tox_file_id_hex)
    {
        this.sql_where = this.sql_where + " and \"tox_file_id_hex\"=?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_file_id_hex));
        bind_where_count++;
        return this;
    }

    public Filetransfer tox_file_id_hexNotEq(String tox_file_id_hex)
    {
        this.sql_where = this.sql_where + " and \"tox_file_id_hex\"<>?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_file_id_hex));
        bind_where_count++;
        return this;
    }

    public Filetransfer tox_file_id_hexIsNull()
    {
        this.sql_where = this.sql_where + " and \"tox_file_id_hex\" IS NULL ";
        return this;
    }

    public Filetransfer tox_file_id_hexIsNotNull()
    {
        this.sql_where = this.sql_where + " and \"tox_file_id_hex\" IS NOT NULL ";
        return this;
    }

    public Filetransfer tox_file_id_hexLike(String tox_file_id_hex)
    {
        this.sql_where = this.sql_where + " and \"tox_file_id_hex\" LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_file_id_hex));
        bind_where_count++;
        return this;
    }

    public Filetransfer tox_file_id_hexNotLike(String tox_file_id_hex)
    {
        this.sql_where = this.sql_where + " and \"tox_file_id_hex\" NOT LIKE ?" + (BINDVAR_OFFSET_WHERE + bind_where_count) + " ESCAPE '\\' ";
        bind_where_vars.add(new OrmaBindvar(BINDVAR_TYPE_String, tox_file_id_hex));
        bind_where_count++;
        return this;
    }


    // ----------------- OrderBy funcs ------------------ //
    public Filetransfer orderByIdAsc()
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

    public Filetransfer orderByIdDesc()
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

    public Filetransfer orderByTox_public_key_stringAsc()
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

    public Filetransfer orderByTox_public_key_stringDesc()
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

    public Filetransfer orderByDirectionAsc()
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

    public Filetransfer orderByDirectionDesc()
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

    public Filetransfer orderByFile_numberAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"file_number\" ASC ";
        return this;
    }

    public Filetransfer orderByFile_numberDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"file_number\" DESC ";
        return this;
    }

    public Filetransfer orderByKindAsc()
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

    public Filetransfer orderByKindDesc()
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

    public Filetransfer orderByStateAsc()
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

    public Filetransfer orderByStateDesc()
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

    public Filetransfer orderByFt_acceptedAsc()
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

    public Filetransfer orderByFt_acceptedDesc()
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

    public Filetransfer orderByFt_outgoing_startedAsc()
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

    public Filetransfer orderByFt_outgoing_startedDesc()
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

    public Filetransfer orderByPath_nameAsc()
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

    public Filetransfer orderByPath_nameDesc()
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

    public Filetransfer orderByFile_nameAsc()
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

    public Filetransfer orderByFile_nameDesc()
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

    public Filetransfer orderByFos_openAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"fos_open\" ASC ";
        return this;
    }

    public Filetransfer orderByFos_openDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"fos_open\" DESC ";
        return this;
    }

    public Filetransfer orderByFilesizeAsc()
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

    public Filetransfer orderByFilesizeDesc()
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

    public Filetransfer orderByCurrent_positionAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"current_position\" ASC ";
        return this;
    }

    public Filetransfer orderByCurrent_positionDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"current_position\" DESC ";
        return this;
    }

    public Filetransfer orderByMessage_idAsc()
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

    public Filetransfer orderByMessage_idDesc()
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

    public Filetransfer orderByStorage_frame_workAsc()
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

    public Filetransfer orderByStorage_frame_workDesc()
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

    public Filetransfer orderByTox_file_id_hexAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"tox_file_id_hex\" ASC ";
        return this;
    }

    public Filetransfer orderByTox_file_id_hexDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " \"tox_file_id_hex\" DESC ";
        return this;
    }



}

