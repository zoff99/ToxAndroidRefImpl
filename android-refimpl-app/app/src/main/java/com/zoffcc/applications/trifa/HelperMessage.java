/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2020 Zoff <zoff@zoff.cc>
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA  02110-1301, USA.
 */

package com.zoffcc.applications.trifa;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import static com.zoffcc.applications.trifa.TrifaToxService.orma;

public class HelperMessage
{
    private static final String TAG = "trifa.Hlp.Message";

    synchronized static void update_message_in_db(final Message m)
    {
        final Thread t = new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    orma.updateMessage().
                            idEq(m.id).
                            read(m.read).
                            text(m.text).
                            sent_timestamp(m.sent_timestamp).
                            rcvd_timestamp(m.rcvd_timestamp).
                            filename_fullpath(m.filename_fullpath).
                            execute();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };
        t.start();
    }

    synchronized static void update_message_in_db_no_read_recvedts(final Message m)
    {
        final Thread t = new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    orma.updateMessage().
                            idEq(m.id).
                            text(m.text).
                            sent_timestamp(m.sent_timestamp).
                            msg_version(m.msg_version).
                            filename_fullpath(m.filename_fullpath).
                            raw_msgv2_bytes(m.raw_msgv2_bytes).
                            msg_id_hash(m.msg_id_hash).
                            execute();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };
        t.start();
    }

    static void update_message_in_db_filename_fullpath_friendnum_and_filenum(long friend_number, long file_number, String filename_fullpath)
    {
        try
        {
            long ft_id = orma.selectFromFiletransfer().
                    tox_public_key_stringEq(HelperFriend.tox_friend_get_public_key__wrapper(friend_number)).
                    and().file_numberEq(file_number).orderByIdDesc().get(0).id;

            update_message_in_db_filename_fullpath_from_id(orma.selectFromMessage().
                    filetransfer_idEq(ft_id).and().
                    tox_friendpubkeyEq(HelperFriend.tox_friend_get_public_key__wrapper(friend_number)).
                    get(0).id, filename_fullpath);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static void update_message_in_db_filename_fullpath_from_id(long msg_id, String filename_fullpath)
    {
        try
        {
            orma.updateMessage().idEq(msg_id).filename_fullpath(filename_fullpath).execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static void update_message_in_db_filename_fullpath(final Message m)
    {
        try
        {
            orma.updateMessage().
                    idEq(m.id).
                    filename_fullpath(m.filename_fullpath).
                    execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static void update_message_in_db_read_rcvd_timestamp_rawmsgbytes(final Message m)
    {
        try
        {
            orma.updateMessage().
                    idEq(m.id).
                    read(m.read).
                    raw_msgv2_bytes(m.raw_msgv2_bytes).
                    rcvd_timestamp(m.rcvd_timestamp).
                    execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static void update_message_in_db_messageid(final Message m)
    {
        try
        {
            orma.updateMessage().
                    idEq(m.id).
                    message_id(m.message_id).
                    execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static void update_message_in_db_resend_count(final Message m)
    {
        try
        {
            orma.updateMessage().
                    idEq(m.id).
                    resend_count(m.resend_count).
                    execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void add_single_conference_message_from_messge_id(final long message_id, final boolean force)
    {
        try
        {
            if (MainActivity.conference_message_list_fragment != null)
            {
                Thread t = new Thread()
                {
                    @Override
                    public void run()
                    {
                        if (message_id != -1)
                        {
                            try
                            {
                                ConferenceMessage m = orma.selectFromConferenceMessage().idEq(
                                        message_id).orderByIdDesc().get(0);

                                if (m.id != -1)
                                {
                                    if ((force) || (MainActivity.update_all_messages_global_timestamp +
                                                    MainActivity.UPDATE_MESSAGES_NORMAL_MILLIS <
                                                    System.currentTimeMillis()))
                                    {
                                        MainActivity.update_all_messages_global_timestamp = System.currentTimeMillis();
                                        MainActivity.conference_message_list_fragment.add_message(m);
                                    }
                                }
                            }
                            catch (Exception e2)
                            {
                            }
                        }
                    }
                };
                t.start();
            }
        }
        catch (Exception e)
        {
            // e.printStackTrace();
        }
    }

    public static void add_single_message_from_messge_id(final long message_id, final boolean force)
    {
        try
        {
            // Log.i(TAG, "add_single_message_from_messge_id:message_id=" + message_id);
            Thread t = new Thread()
            {
                @Override
                public void run()
                {
                    if (message_id != -1)
                    {
                        // Log.i(TAG, "add_single_message_from_messge_id:message_id=" + message_id);

                        try
                        {
                            Message m = orma.selectFromMessage().idEq(message_id).orderByIdDesc().get(0);

                            if (m.id != -1)
                            {
                                // Log.i(TAG, "add_single_message_from_messge_id:m.id=" + m.id);

                                if ((force) || (MainActivity.update_all_messages_global_timestamp +
                                                MainActivity.UPDATE_MESSAGES_NORMAL_MILLIS <
                                                System.currentTimeMillis()))
                                {
                                    // Log.i(TAG, "add_single_message_from_messge_id:add_message()");

                                    if (MainActivity.message_list_fragment == null)
                                    {
                                        // ok, we need to wait for onResume to finish
                                        // Log.i(TAG,
                                        //       "add_single_message_from_messge_id:ok, we need to wait for onResume to finish");
                                        long loop = 0;

                                        while (loop < 40)  // wait 8 sec., then give up
                                        {
                                            loop++;

                                            try
                                            {
                                                Thread.sleep(200);
                                            }
                                            catch (InterruptedException e)
                                            {
                                                e.printStackTrace();
                                            }

                                            if (MainActivity.message_list_fragment != null)
                                            {
                                                // got it
                                                break;
                                            }
                                        }
                                    }

                                    if (MainActivity.message_list_fragment != null)
                                    {
                                        MainActivity.update_all_messages_global_timestamp = System.currentTimeMillis();
                                        MainActivity.message_list_fragment.add_message(m);
                                    }
                                }
                            }
                        }
                        catch (Exception e2)
                        {
                            Log.i(TAG, "add_single_message_from_messge_id:EE1:" + e2.getMessage());
                        }
                    }
                }
            };
            t.start();
        }
        catch (Exception e)
        {
            // e.printStackTrace();
            Log.i(TAG, "add_single_message_from_messge_id:EE2:" + e.getMessage());
        }
    }

    public static void update_single_message_from_messge_id(final long message_id, final boolean force)
    {
        try
        {
            if (MainActivity.message_list_fragment != null)
            {
                Thread t = new Thread()
                {
                    @Override
                    public void run()
                    {
                        if (message_id != -1)
                        {
                            try
                            {
                                Message m = orma.selectFromMessage().idEq(message_id).orderByIdDesc().get(0);

                                if (m.id != -1)
                                {
                                    if ((force) || (MainActivity.update_all_messages_global_timestamp +
                                                    MainActivity.UPDATE_MESSAGES_NORMAL_MILLIS <
                                                    System.currentTimeMillis()))
                                    {
                                        MainActivity.update_all_messages_global_timestamp = System.currentTimeMillis();
                                        MainActivity.message_list_fragment.modify_message(m);
                                    }
                                }
                            }
                            catch (Exception e2)
                            {
                            }
                        }
                    }
                };
                t.start();
            }
        }
        catch (Exception e)
        {
            // e.printStackTrace();
        }
    }

    public static void update_single_message_from_ftid(final long filetransfer_id, final boolean force)
    {
        try
        {
            if (MainActivity.message_list_fragment != null)
            {
                Thread t = new Thread()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            Message m = orma.selectFromMessage().filetransfer_idEq(filetransfer_id).orderByIdDesc().get(
                                    0);

                            if (m.id != -1)
                            {
                                if ((force) || (MainActivity.update_all_messages_global_timestamp +
                                                MainActivity.UPDATE_MESSAGES_NORMAL_MILLIS <
                                                System.currentTimeMillis()))
                                {
                                    MainActivity.update_all_messages_global_timestamp = System.currentTimeMillis();
                                    MainActivity.message_list_fragment.modify_message(m);
                                }
                            }
                        }
                        catch (Exception e2)
                        {
                            e2.printStackTrace();
                        }
                    }
                };
                t.start();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "update_message_view:EE:" + e.getMessage());
        }
    }

    public static void update_single_message(Message m, boolean force)
    {
        try
        {
            if (MainActivity.message_list_fragment != null)
            {
                if ((force) ||
                    (MainActivity.update_all_messages_global_timestamp + MainActivity.UPDATE_MESSAGES_NORMAL_MILLIS <
                     System.currentTimeMillis()))
                {
                    MainActivity.update_all_messages_global_timestamp = System.currentTimeMillis();
                    MainActivity.message_list_fragment.modify_message(m);
                }
            }
        }
        catch (Exception e)
        {
            // e.printStackTrace();
            Log.i(TAG, "update_message_view:EE:" + e.getMessage());
        }
    }

    public static long get_message_id_from_filetransfer_id_and_friendnum(long filetransfer_id, long friend_number)
    {
        try
        {
            //            Log.i(TAG, "get_message_id_from_filetransfer_id_and_friendnum:messages:=====================================");
            //            Log.i(TAG, "get_message_id_from_filetransfer_id_and_friendnum:messages:" + orma.selectFromMessage().toList().size());
            //
            //            int i = 0;
            //            for (i = 0; i < orma.selectFromMessage().toList().size(); i++)
            //            {
            //                Log.i(TAG, "get_message_id_from_filetransfer_id_and_friendnum:messages:****");
            //                Log.i(TAG, "get_message_id_from_filetransfer_id_and_friendnum:messages:#" + i + ":" + orma.selectFromMessage().toList().get(i));
            //                Log.i(TAG, "get_message_id_from_filetransfer_id_and_friendnum:messages:****");
            //            }
            //
            //            Log.i(TAG, "get_message_id_from_filetransfer_id_and_friendnum:messages:=====================================");
            //
            //            Log.i(TAG, "get_message_id_from_filetransfer_id_and_friendnum:messages:2=====================================");
            //            Log.i(TAG, "get_message_id_from_filetransfer_id_and_friendnum:messages:2" + orma.selectFromMessage().filetransfer_idEq(filetransfer_id).toList().size());
            //
            //            for (i = 0; i < orma.selectFromMessage().filetransfer_idEq(filetransfer_id).toList().size(); i++)
            //            {
            //                Log.i(TAG, "get_message_id_from_filetransfer_id_and_friendnum:messages:2****");
            //                Log.i(TAG, "get_message_id_from_filetransfer_id_and_friendnum:messages:2#" + i + ":" + orma.selectFromMessage().toList().get(i));
            //                Log.i(TAG, "get_message_id_from_filetransfer_id_and_friendnum:messages:2****");
            //            }
            //
            //            Log.i(TAG, "get_message_id_from_filetransfer_id_and_friendnum:messages:2=====================================");
            //


            // Log.i(TAG, "get_message_id_from_filetransfer_id_and_friendnum:messages:filetransfer_id=" + filetransfer_id +
            //            " friend_number=" + friend_number);
            List<Message> m = orma.selectFromMessage().
                    filetransfer_idEq(filetransfer_id).and().
                    tox_friendpubkeyEq(HelperFriend.tox_friend_get_public_key__wrapper(friend_number)).
                    orderByIdDesc().toList();

            if (m.size() == 0)
            {
                return -1;
            }

            return m.get(0).id;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "get_message_id_from_filetransfer_id_and_friendnum:EE:" + e.getMessage());
            return -1;
        }
    }

    public static void set_message_state_from_friendnum_and_filenum(long friend_number, long file_number, int state)
    {
        try
        {
            long ft_id = orma.selectFromFiletransfer().
                    tox_public_key_stringEq(HelperFriend.tox_friend_get_public_key__wrapper(friend_number)).
                    and().file_numberEq(file_number).orderByIdDesc().get(0).id;
            // Log.i(TAG,
            //       "set_message_state_from_friendnum_and_filenum:ft_id=" + ft_id + " friend_number=" + friend_number +
            //       " file_number=" + file_number);
            set_message_state_from_id(orma.selectFromMessage().
                    filetransfer_idEq(ft_id).and().
                    tox_friendpubkeyEq(HelperFriend.tox_friend_get_public_key__wrapper(friend_number)).
                    get(0).id, state);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "set_message_state_from_friendnum_and_filenum:EE:" + e.getMessage());
        }
    }

    public static void set_message_state_from_id(long message_id, int state)
    {
        try
        {
            orma.updateMessage().idEq(message_id).state(state).execute();
            // Log.i(TAG, "set_message_state_from_id:message_id=" + message_id + " state=" + state);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "set_message_state_from_id:EE:" + e.getMessage());
        }
    }

    public static void set_message_start_sending_from_id(long message_id)
    {
        try
        {
            orma.updateMessage().idEq(message_id).ft_outgoing_started(true).execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "set_message_start_sending_from_id:EE:" + e.getMessage());
        }
    }

    public static void set_message_queueing_from_id(long message_id, boolean ft_outgoing_queued)
    {
        try
        {
            orma.updateMessage().idEq(message_id).ft_outgoing_queued(ft_outgoing_queued).execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "set_message_start_queueing_from_id:EE:" + e.getMessage());
        }
    }

    public static void set_message_filedb_from_friendnum_and_filenum(long friend_number, long file_number, long filedb_id)
    {
        try
        {
            long ft_id = orma.selectFromFiletransfer().
                    tox_public_key_stringEq(HelperFriend.tox_friend_get_public_key__wrapper(friend_number)).
                    and().file_numberEq(file_number).
                    orderByIdDesc().
                    get(0).id;
            // Log.i(TAG,
            //       "set_message_filedb_from_friendnum_and_filenum:ft_id=" + ft_id + " friend_number=" + friend_number +
            //       " file_number=" + file_number);
            set_message_filedb_from_id(orma.selectFromMessage().
                    filetransfer_idEq(ft_id).and().
                    tox_friendpubkeyEq(HelperFriend.tox_friend_get_public_key__wrapper(friend_number)).
                    orderByIdDesc().
                    get(0).id, filedb_id);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "set_message_filedb_from_friendnum_and_filenum:EE:" + e.getMessage());
        }
    }

    public static void set_message_filedb_from_id(long message_id, long filedb_id)
    {
        try
        {
            orma.updateMessage().idEq(message_id).filedb_id(filedb_id).execute();
            // Log.i(TAG, "set_message_filedb_from_id:message_id=" + message_id + " filedb_id=" + filedb_id);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "set_message_filedb_from_id:EE:" + e.getMessage());
        }
    }

    static long insert_into_message_db(final Message m, final boolean update_message_view_flag)
    {
        // Thread t = new Thread()
        //{
        //    @Override
        //    public void run()
        //    {
        // Log.i(TAG, "insert_into_message_db:m=" + m);
        long row_id = orma.insertIntoMessage(m);

        try
        {
            Cursor cursor = orma.getConnection().rawQuery("SELECT id FROM Message where rowid='" + row_id + "'");
            cursor.moveToFirst();
            // Log.i(TAG, "insert_into_message_db:id res count=" + cursor.getColumnCount());
            long msg_id = cursor.getLong(0);
            cursor.close();

            if (update_message_view_flag)
            {
                // Log.i(TAG, "insert_into_message_db:add_single_message_from_messge_id, force=true");
                add_single_message_from_messge_id(msg_id, true);
            }

            return msg_id;
        }
        catch (Exception e)
        {
            Log.i(TAG, "insert_into_message_db:EE:" + e.getMessage());
            e.printStackTrace();
            return -1;
        }

        //    }
        //};
        //t.start();
    }

    static void delete_selected_messages(final Context c, final boolean update_message_list, final boolean update_friend_list, final String dialog_text)
    {
        ProgressDialog progressDialog2 = null;

        try
        {
            new MainActivity.delete_selected_messages_asynchtask(c, progressDialog2, update_message_list,
                                                                 update_friend_list, dialog_text).execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "delete_selected_messages:EE2:" + e.getMessage());
        }
    }

    static void copy_selected_messages(Context c)
    {
        try
        {
            if (!MainActivity.selected_messages_text_only.isEmpty())
            {
                // sort ascending (lowest ID on top)
                Collections.sort(MainActivity.selected_messages_text_only, new Comparator<Long>()
                {
                    public int compare(Long o1, Long o2)
                    {
                        return o1.compareTo(o2);
                    }
                });
                StringBuilder copy_text = new StringBuilder();
                boolean first = true;
                Iterator i = MainActivity.selected_messages_text_only.iterator();

                while (i.hasNext())
                {
                    try
                    {
                        if (first)
                        {
                            first = false;
                            copy_text = new StringBuilder(
                                    "" + orma.selectFromMessage().idEq((Long) i.next()).get(0).text);
                        }
                        else
                        {
                            copy_text.append("\n").append(orma.selectFromMessage().idEq((Long) i.next()).get(0).text);
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }

                MainActivity.clipboard.setPrimaryClip(ClipData.newPlainText("", copy_text.toString()));
                Toast.makeText(c, "copied to Clipboard", Toast.LENGTH_SHORT).show();
                MainActivity.selected_messages.clear();
                MainActivity.selected_messages_incoming_file.clear();
                MainActivity.selected_messages_text_only.clear();

                try
                {
                    // need to redraw all items again here, to remove the selections
                    MainActivity.message_list_fragment.adapter.redraw_all_items();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        catch (Exception e2)
        {
            e2.printStackTrace();
        }
    }

    static void save_selected_messages(Context c)
    {
        ProgressDialog progressDialog2 = null;

        try
        {
            try
            {
                progressDialog2 = ProgressDialog.show(c, "", "exporting Messages ...");
                progressDialog2.setCanceledOnTouchOutside(false);
                progressDialog2.setOnCancelListener(new DialogInterface.OnCancelListener()
                {
                    @Override
                    public void onCancel(DialogInterface dialog)
                    {
                    }
                });
            }
            catch (Exception e3)
            {
                e3.printStackTrace();
                Log.i(TAG, "save_selected_messages:EE1:" + e3.getMessage());
            }

            new MainActivity.save_selected_messages_asynchtask(c, progressDialog2).execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "save_selected_messages:EE2:" + e.getMessage());
        }
    }
}
