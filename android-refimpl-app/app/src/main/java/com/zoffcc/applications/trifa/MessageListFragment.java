/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2017 Zoff <zoff@zoff.cc>
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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.l4digital.fastscroll.FastScroller;

import java.util.ArrayList;
import java.util.List;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.zoffcc.applications.trifa.HelperFriend.tox_friend_get_public_key__wrapper;
import static com.zoffcc.applications.trifa.HelperGeneric.do_fade_anim_on_fab;
import static com.zoffcc.applications.trifa.HelperGeneric.get_sqlite_search_string;
import static com.zoffcc.applications.trifa.MainActivity.PREF__message_paging_num_msgs_per_page;
import static com.zoffcc.applications.trifa.MainActivity.PREF__messageview_paging;
import static com.zoffcc.applications.trifa.MainActivity.context_s;
import static com.zoffcc.applications.trifa.MainActivity.main_handler_s;
import static com.zoffcc.applications.trifa.TRIFAGlobals.MESSAGE_PAGING_LAST_PAGE_MARGIN;
import static com.zoffcc.applications.trifa.TRIFAGlobals.MESSAGE_PAGING_SHOW_NEWER_HASH;
import static com.zoffcc.applications.trifa.TRIFAGlobals.MESSAGE_PAGING_SHOW_OLDER_HASH;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_FILE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_SYSTEM_MESSAGE_PEER_PUBKEY;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_showing_messageview;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;

public class MessageListFragment extends Fragment
{
    private static final String TAG = "trifa.MsgListFrgnt";
    List<Message> data_values = null;
    // MessagelistArrayAdapter a = null;
    long current_friendnum = -1;
    com.l4digital.fastscroll.FastScrollRecyclerView listingsView = null;
    MessagelistAdapter adapter = null;
    static boolean is_at_bottom = true;
    static int current_page_offset = -1;
    static boolean faded_in = false;
    TextView scrollDateHeader = null;
    ConversationDateHeader conversationDateHeader = null;
    MessageListActivity mla = null;
    boolean is_data_loaded = false;
    static boolean show_only_files = false;
    static String search_messages_text = null;
    FloatingActionButton unread_messages_notice_button = null;

    @SuppressLint("WrongThread")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Log.i(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.message_list_layout, container, false);

        reset_paging();

        unread_messages_notice_button = view.findViewById(R.id.unread_messages_notice_button);
        unread_messages_notice_button.setAnimation(null);
        unread_messages_notice_button.setVisibility(View.INVISIBLE);
        unread_messages_notice_button.setSupportBackgroundTintList(
                (ContextCompat.getColorStateList(context_s, R.color.message_list_scroll_to_bottom_fab_bg_normal)));


        mla = (MessageListActivity) (getActivity());
        if (mla != null)
        {
            current_friendnum = mla.get_current_friendnum();
        }
        // Log.i(TAG, "current_friendnum=" + current_friendnum);

        // default is: at bottom
        is_at_bottom = true;
        faded_in = false;

        try
        {
            // reset "new" flags for messages -------
            if (orma != null)
            {
                orma.updateMessage().
                        tox_friendpubkeyEq(tox_friend_get_public_key__wrapper(current_friendnum)).
                        is_new(false).
                        execute();
            }
            // reset "new" flags for messages -------
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            if (orma != null)
            {
                data_values = new ArrayList<Message>();
                data_values.clear();
            }
        }
        catch (Exception ignored)
        {
        }

        if (2 == 1 + 4)
        {
            // Log.i(TAG, "loaded_messages:start");
            try
            {
                if (orma != null)
                {
                    // Log.i(TAG, "current_friendpublic_key=" + tox_friend_get_public_key__wrapper(current_friendnum));
                    // -------------------------------------------------
                    // HINT: here ordering of messages is applied !!
                    // -------------------------------------------------
                    if (show_only_files)
                    {
                        data_values = orma.selectFromMessage().tox_friendpubkeyEq(
                                tox_friend_get_public_key__wrapper(current_friendnum)).
                                and().
                                TRIFA_MESSAGE_TYPEEq(TRIFA_MSG_FILE.value).
                                orderBySent_timestampAsc().
                                orderBySent_timestamp_msAsc().
                                toList();
                    }
                    else
                    {
                        if ((search_messages_text == null) || (search_messages_text.length() == 0))
                        {
                            data_values = orma.selectFromMessage().tox_friendpubkeyEq(
                                    tox_friend_get_public_key__wrapper(current_friendnum)).
                                    orderBySent_timestampAsc().
                                    orderBySent_timestamp_msAsc().
                                    toList();
                        }
                        else
                        {
                        /*
                         searching for case-IN-sensitive non ascii chars is not working:

                         https://sqlite.org/lang_expr.html#like

                         Important Note: SQLite only understands upper/lower case for ASCII characters by default.
                         The LIKE operator is case sensitive by default for unicode characters that are beyond
                         the ASCII range. For example, the expression 'a' LIKE 'A' is TRUE but 'æ' LIKE 'Æ' is FALSE
                         */
                            data_values = orma.selectFromMessage().tox_friendpubkeyEq(
                                    tox_friend_get_public_key__wrapper(current_friendnum)).
                                    orderBySent_timestampAsc().
                                    orderBySent_timestamp_msAsc().
                                    where(" like('" + get_sqlite_search_string(search_messages_text) +
                                          "', text, '\\')").
                                    toList();
                        }
                    }
                    // Log.i(TAG, "loading data:001");
                    // Log.i(TAG, "current_friendpublic_key:data_values=" + data_values);
                    // Log.i(TAG, "current_friendpublic_key:data_values size=" + data_values.size());
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                // data_values is NULL here!!
            }
        }
        // Log.i(TAG, "loaded_messages:ready");

        // --------------
        // --------------
        // --------------
        adapter = new MessagelistAdapter(view.getContext(), data_values);
        // Log.i(TAG, "onCreateView:adapter=" + adapter);
        listingsView = (com.l4digital.fastscroll.FastScrollRecyclerView) view.findViewById(R.id.msg_rv_list);
        // Log.i(TAG, "onCreateView:listingsView=" + listingsView);

        scrollDateHeader = (TextView) view.findViewById(R.id.scroll_date_header);
        scrollDateHeader.setText("");
        scrollDateHeader.setVisibility(View.INVISIBLE);
        conversationDateHeader = new ConversationDateHeader(view.getContext(), scrollDateHeader);

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setStackFromEnd(true); // pin to bottom element
        listingsView.setLayoutManager(linearLayoutManager);
        listingsView.setItemAnimator(new DefaultItemAnimator());
        listingsView.setHasFixedSize(false);

        listingsView.setFastScrollListener(new FastScroller.FastScrollListener()
        {
            @Override
            public void onFastScrollStart(FastScroller fastScroller)
            {
                if (!is_at_bottom)
                {
                    if (faded_in)
                    {
                        try
                        {
                            do_fade_anim_on_fab(MainActivity.message_list_fragment.unread_messages_notice_button, false,
                                                this.getClass().getName());
                        }
                        catch (Exception ignored)
                        {
                        }
                    }
                }
            }

            @Override
            public void onFastScrollStop(FastScroller fastScroller)
            {
                if (!is_at_bottom)
                {
                    if (!faded_in)
                    {
                        try
                        {
                            do_fade_anim_on_fab(MainActivity.message_list_fragment.unread_messages_notice_button, true,
                                                this.getClass().getName());
                        }
                        catch (Exception ignored)
                        {
                        }
                    }
                }
            }
        });

        RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState)
            {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == RecyclerView.SCROLL_STATE_DRAGGING)
                {
                    if (!is_at_bottom)
                    {
                        if (faded_in)
                        {
                            try
                            {
                                do_fade_anim_on_fab(MainActivity.message_list_fragment.unread_messages_notice_button,
                                                    false, this.getClass().getName());
                            }
                            catch (Exception ignored)
                            {
                            }
                        }
                    }
                    conversationDateHeader.show();
                }
                else if (newState == RecyclerView.SCROLL_STATE_IDLE)
                {
                    if (!is_at_bottom)
                    {
                        if (!faded_in)
                        {
                            try
                            {
                                do_fade_anim_on_fab(MainActivity.message_list_fragment.unread_messages_notice_button,
                                                    true, this.getClass().getName());
                            }
                            catch (Exception ignored)
                            {

                            }
                        }
                    }
                    conversationDateHeader.hide();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                int visibleItemCount = linearLayoutManager.getChildCount();
                int totalItemCount = linearLayoutManager.getItemCount();
                int pastVisibleItems = linearLayoutManager.findFirstVisibleItemPosition();

                scrollDateHeader.setText(adapter.getDateHeaderText(pastVisibleItems));

                if (pastVisibleItems + visibleItemCount >= totalItemCount)
                {
                    // Bottom of the list
                    if (!is_at_bottom)
                    {
                        // Log.i(TAG, "onScrolled:at bottom");
                        is_at_bottom = true;
                        try
                        {
                            do_fade_anim_on_fab(unread_messages_notice_button, false, this.getClass().getName());
                            unread_messages_notice_button.setSupportBackgroundTintList(
                                    (ContextCompat.getColorStateList(context_s,
                                                                     R.color.message_list_scroll_to_bottom_fab_bg_normal)));
                        }
                        catch (Exception ignored)
                        {
                        }
                    }
                }
                else
                {
                    if (is_at_bottom)
                    {
                        // Log.i(TAG, "onScrolled:NOT at bottom");
                        is_at_bottom = false;
                        try
                        {
                            do_fade_anim_on_fab(unread_messages_notice_button, true, this.getClass().getName());
                            unread_messages_notice_button.setVisibility(View.VISIBLE);
                        }
                        catch (Exception ignored)
                        {
                        }
                    }
                }
            }
        };

        listingsView.addOnScrollListener(mScrollListener);
        listingsView.setAdapter(adapter);
        // --------------
        // --------------


        // a = new MessagelistArrayAdapter(context, data_values);
        // setListAdapter(a);

        // MainActivity.message_list_fragment = this;

        is_data_loaded = false;

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Context context)
    {
        Log.i(TAG, "onAttach(Context)");
        super.onAttach(context);
    }

    @Override
    public void onAttach(Activity activity)
    {
        Log.i(TAG, "onAttach(Activity)");
        super.onAttach(activity);
    }

    @SuppressLint("WrongThread")
    @Override
    public void onResume()
    {
        global_showing_messageview = true;

        Log.i(TAG, "onResume");
        super.onResume();
        if (!is_data_loaded)
        {
            try
            {
                // reset "new" flags for messages -------
                if (orma != null)
                {
                    orma.updateMessage().tox_friendpubkeyEq(
                            tox_friend_get_public_key__wrapper(current_friendnum)).is_new(false).execute();
                    // Log.i(TAG, "loading data:002");
                }
                // reset "new" flags for messages -------
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            update_all_messages(true, false, PREF__messageview_paging);

            // default is: at bottom
            is_at_bottom = true;
        }

        is_data_loaded = true;

        MainActivity.message_list_fragment = this;
        // Log.i(TAG, "onResume:099");
    }

    @Override
    public void onPause()
    {
        Log.i(TAG, "onPause");
        super.onPause();

        global_showing_messageview = false;
        MainActivity.message_list_fragment = null;
    }

    void reset_paging()
    {
        current_page_offset = -1; // reset paging when we change friend that is shown
    }

    void update_all_messages(final boolean from_resume_fragment, final boolean stay_at_top, final boolean paging)
    {
        Log.i(TAG, "update_all_messages");

        try
        {
            // reset "new" flags for messages -------
            orma.updateMessage().tox_friendpubkeyEq(tox_friend_get_public_key__wrapper(current_friendnum)).is_new(
                    false).execute();
            // reset "new" flags for messages -------
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            final int number_of_items_old = data_values.size();
            data_values.clear();

            boolean later_messages = false;
            boolean older_messages = false;
            List<Message> ml = null;

            if ((paging) && ((search_messages_text == null) || (search_messages_text.length() == 0)))
            {
                later_messages = true;
                older_messages = true;
                int count_messages = 0;

                if (show_only_files)
                {
                    count_messages = orma.selectFromMessage().
                            tox_friendpubkeyEq(tox_friend_get_public_key__wrapper(current_friendnum)).
                            TRIFA_MESSAGE_TYPEEq(TRIFA_MSG_FILE.value).
                            orderBySent_timestampAsc().
                            orderBySent_timestamp_msAsc().
                            count();
                }
                else
                {
                    count_messages = orma.selectFromMessage().
                            tox_friendpubkeyEq(tox_friend_get_public_key__wrapper(current_friendnum)).
                            orderBySent_timestampAsc().
                            orderBySent_timestamp_msAsc().
                            count();
                }

                int offset = 0;
                int rowcount = PREF__message_paging_num_msgs_per_page;

                if (current_page_offset == -1) // HINT: page at the bottom (latest messages shown)
                {
                    later_messages = false;
                    offset = count_messages - PREF__message_paging_num_msgs_per_page;
                    if (offset < 0)
                    {
                        offset = 0;
                    }
                    current_page_offset = offset;
                    // HINT: we need MESSAGE_PAGING_LAST_PAGE_MARGIN in case new messages arrived
                    //       since "count_messages" was calculated above
                    rowcount = PREF__message_paging_num_msgs_per_page + MESSAGE_PAGING_LAST_PAGE_MARGIN;
                }
                else
                {
                    if ((count_messages - current_page_offset) < PREF__message_paging_num_msgs_per_page)
                    {
                        current_page_offset = count_messages - PREF__message_paging_num_msgs_per_page;
                        rowcount = PREF__message_paging_num_msgs_per_page + MESSAGE_PAGING_LAST_PAGE_MARGIN;
                    }
                    offset = current_page_offset;
                }

                if ((count_messages - offset) <= PREF__message_paging_num_msgs_per_page)
                {
                    later_messages = false;
                }

                if (offset < 1)
                {
                    older_messages = false;
                }

                if (show_only_files)
                {
                    ml = orma.selectFromMessage().
                            tox_friendpubkeyEq(tox_friend_get_public_key__wrapper(current_friendnum)).
                            TRIFA_MESSAGE_TYPEEq(TRIFA_MSG_FILE.value).
                            orderBySent_timestampAsc().
                            orderBySent_timestamp_msAsc().
                            offset(offset).
                            limit(rowcount).
                            toList();
                }
                else
                {
                    ml = orma.selectFromMessage().
                            tox_friendpubkeyEq(tox_friend_get_public_key__wrapper(current_friendnum)).
                            orderBySent_timestampAsc().
                            orderBySent_timestamp_msAsc().
                            offset(offset).
                            limit(rowcount).
                            toList();
                }
            }
            else
            {
                if (show_only_files)
                {
                    ml = orma.selectFromMessage().
                            tox_friendpubkeyEq(tox_friend_get_public_key__wrapper(current_friendnum)).
                            and().
                            TRIFA_MESSAGE_TYPEEq(TRIFA_MSG_FILE.value).
                            orderBySent_timestampAsc().
                            orderBySent_timestamp_msAsc().
                            toList();
                }
                else
                {
                    if ((search_messages_text == null) || (search_messages_text.length() == 0))
                    {
                        ml = orma.selectFromMessage().
                                tox_friendpubkeyEq(tox_friend_get_public_key__wrapper(current_friendnum)).
                                orderBySent_timestampAsc().
                                orderBySent_timestamp_msAsc().
                                toList();
                    }
                    else
                    {
                        /*
                         searching for case-IN-sensitive non ascii chars is not working:

                         https://sqlite.org/lang_expr.html#like

                         Important Note: SQLite only understands upper/lower case for ASCII characters by default.
                         The LIKE operator is case sensitive by default for unicode characters that are beyond
                         the ASCII range. For example, the expression 'a' LIKE 'A' is TRUE but 'æ' LIKE 'Æ' is FALSE
                         */
                        ml = orma.selectFromMessage().
                                tox_friendpubkeyEq(tox_friend_get_public_key__wrapper(current_friendnum)).
                                orderBySent_timestampAsc().
                                orderBySent_timestamp_msAsc().
                                where(" like('" + get_sqlite_search_string(search_messages_text) + "', text, '\\')").
                                toList();
                    }
                }
            }

            if (ml != null)
            {
                if (older_messages)
                {
                    Message m_older = new Message();
                    m_older.tox_friendpubkey = TRIFA_SYSTEM_MESSAGE_PEER_PUBKEY;
                    m_older.is_new = false;
                    m_older.direction = 0;
                    m_older.msg_idv3_hash = MESSAGE_PAGING_SHOW_OLDER_HASH;
                    m_older.text = "older Messages";
                    add_message(m_older, false, stay_at_top);
                }

                for (Message message : ml)
                {
                    if (message == ml.get(ml.size() - 1))
                    {
                        add_message(message, false, stay_at_top);
                    }
                    else
                    {
                        if (later_messages)
                        {
                            add_message(message, false, stay_at_top);
                        }
                        else
                        {
                            add_message(message, false, stay_at_top);
                        }
                    }
                }

                if (later_messages)
                {
                    Message m_later = new Message();
                    m_later.tox_friendpubkey = TRIFA_SYSTEM_MESSAGE_PEER_PUBKEY;
                    m_later.is_new = false;
                    m_later.direction = 0;
                    m_later.msg_idv3_hash = MESSAGE_PAGING_SHOW_NEWER_HASH;
                    m_later.text = "newer Messages";
                    add_message(m_later, false, stay_at_top);
                }
            }

            try
            {
                if (from_resume_fragment)
                {
                    if ((number_of_items_old > 0) && (data_values.size() > number_of_items_old))
                    {
                        if (is_at_bottom)
                        {
                            is_at_bottom = false;
                        }

                        if (!faded_in)
                        {
                            try
                            {
                                do_fade_anim_on_fab(unread_messages_notice_button, true, this.getClass().getName());
                                unread_messages_notice_button.setVisibility(View.VISIBLE);
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }

                        try
                        {
                            // set color of FAB to "red"-ish color, to indicate that there are also new messages/FTs
                            unread_messages_notice_button.setSupportBackgroundTintList(
                                    (ContextCompat.getColorStateList(context_s,
                                                                     R.color.message_list_scroll_to_bottom_fab_bg_new_message)));
                        }
                        catch (Exception ignored)
                        {
                        }
                    }
                }
            }
            catch (Exception ignored)
            {
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "data_values:005:EE1:" + e.getMessage());
        }

    }

    synchronized void modify_message(final Message m)
    {
        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    adapter.update_item(m);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        if (main_handler_s != null)
        {
            main_handler_s.post(myRunnable);
        }
    }

    synchronized void add_message(final Message m, final boolean is_real_new_message, final boolean stay_at_top)
    {
        // Log.i(TAG, "add_message:001");
        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    // Log.i(TAG, "add_message:002");
                    adapter.add_item(m);

                    if (stay_at_top)
                    {
                        is_at_bottom = false;
                        listingsView.scrollToPosition(0);
                    }

                    if (is_at_bottom)
                    {
                        listingsView.scrollToPosition(adapter.getItemCount() - 1);
                    }
                    else
                    {
                        try
                        {
                            if (is_real_new_message)
                            {
                                // set color of FAB to "red"-ish color, to indicate that there are also new messages/FTs
                                unread_messages_notice_button.setSupportBackgroundTintList(
                                        (ContextCompat.getColorStateList(context_s,
                                                                         R.color.message_list_scroll_to_bottom_fab_bg_new_message)));
                            }
                        }
                        catch (Exception ignored)
                        {
                        }
                    }
                }
                catch (Exception e)
                {
                    Log.i(TAG, "add_message:EE1:" + e.getMessage());
                    e.printStackTrace();
                }
            }
        };

        if (main_handler_s != null)
        {
            main_handler_s.post(myRunnable);
        }
    }
}
