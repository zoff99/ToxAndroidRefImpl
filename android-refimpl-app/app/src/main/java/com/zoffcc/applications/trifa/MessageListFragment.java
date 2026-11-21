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

import android.os.SystemClock;
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
import com.zoffcc.applications.sorm.Message;

import java.util.ArrayList;
import java.util.Collections;
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
import static com.zoffcc.applications.trifa.TRIFAGlobals.MESSAGE_PAGING_LOAD_ALL_HASH;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_FILE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_SYSTEM_MESSAGE_PEER_PUBKEY;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_showing_messageview;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;

public class MessageListFragment extends Fragment
{
    private static final String TAG = "trifa.MsgListFrgnt";
    List<com.zoffcc.applications.sorm.Message> data_values = null;
    // MessagelistArrayAdapter a = null;
    long current_friendnum = -1;
    com.l4digital.fastscroll.FastScrollRecyclerView listingsView = null;
    MessagelistAdapter adapter = null;
    static boolean is_at_bottom = true;
    // when true, the full message history is loaded for this chat (via the "load all messages" dummy entry).
    // reset to false every time the chat is opened (see reset_paging())
    static boolean load_all_messages = false;
    static int messages_count_old = 0;
    static int count_messages_full = 0;
    static boolean faded_in = false;
    TextView scrollDateHeader = null;
    ConversationDateHeader conversationDateHeader = null;
    MessageListActivity mla = null;
    boolean is_data_loaded = false;
    // C: full message count at the last load; used by onResume to detect whether anything changed while away
    private int messages_count_at_last_load = -1;
    static boolean show_only_files = false;
    static String search_messages_text = null;
    FloatingActionButton unread_messages_notice_button = null;

    @SuppressLint("WrongThread")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Log.i(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.message_list_layout, container, false);
        final long perf_t0 = SystemClock.elapsedRealtime();
        long perf_t = perf_t0;

        reset_paging();

        if (PREF__messageview_paging)
        {
            try
            {
                messages_count_old = get_messages_count_full();
            }
            catch (Exception e)
            {
            }
        }

        try
        {
            unread_messages_notice_button = view.findViewById(R.id.unread_messages_notice_button);
            unread_messages_notice_button.setAnimation(null);
            unread_messages_notice_button.setVisibility(View.INVISIBLE);
            unread_messages_notice_button.setSupportBackgroundTintList(
                    (ContextCompat.getColorStateList(context_s, R.color.message_list_scroll_to_bottom_fab_bg_normal)));
        }
        catch (Exception e)
        {
        }

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
                String friend_pubkey = tox_friend_get_public_key__wrapper(current_friendnum);
                int count_read = orma.selectFromMessage().tox_friendpubkeyEq(friend_pubkey).is_newEq(true).count();
                orma.updateMessage().
                        tox_friendpubkeyEq(friend_pubkey).
                        is_newEq(true).
                        is_new(false).
                        execute();
                HelperMsgNotification.subtract_unread_count_for_key(friend_pubkey, count_read);
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
                data_values = new ArrayList<>();
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
                                    textLike(get_sqlite_search_string(search_messages_text)).
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
        // G: keep more recycled text rows around to avoid re-inflation when scrolling back
        listingsView.getRecycledViewPool().setMaxRecycledViews(Message_model.TEXT_INCOMING_NOT_READ, 12);
        listingsView.getRecycledViewPool().setMaxRecycledViews(Message_model.TEXT_OUTGOING_NOT_READ, 12);
        listingsView.getRecycledViewPool().setMaxRecycledViews(Message_model.TEXT_OUTGOING_HAVE_READ, 12);

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

        HelperGeneric.perf_log(TAG, "friend/frag:onCreateView:TOTAL +" + (SystemClock.elapsedRealtime() - perf_t0) + "ms");
        HelperGeneric.perf_log_first_layout(TAG, "friend/frag:onCreateView", listingsView, perf_t0);
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
        final long perf_t0 = SystemClock.elapsedRealtime();
        long perf_t = perf_t0;

        try
        {
            // reset "new" flags for messages -------
            if (orma != null)
            {
                String friend_pubkey = tox_friend_get_public_key__wrapper(current_friendnum);
                int count_read = orma.selectFromMessage().tox_friendpubkeyEq(friend_pubkey).is_newEq(true).count();
                orma.updateMessage().tox_friendpubkeyEq(friend_pubkey).
                        is_newEq(true).is_new(false).execute();
                HelperMsgNotification.subtract_unread_count_for_key(friend_pubkey, count_read);
                // Log.i(TAG, "loading data:002");
            }
            // reset "new" flags for messages -------
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        HelperGeneric.perf_log(TAG, "friend/frag:onResume:mark-read +" + (SystemClock.elapsedRealtime() - perf_t) + "ms");
        perf_t = SystemClock.elapsedRealtime();
        // C: skip the full reload when nothing changed while we were away
        boolean reload_needed = (!is_data_loaded)
                                || MainActivity.message_list_dirty_while_away
                                || ((search_messages_text != null) && (search_messages_text.length() > 0))
                                || show_only_files
                                || (get_messages_count_full() != messages_count_at_last_load);
        if (reload_needed)
        {
            update_all_messages(true, false, PREF__messageview_paging);
            HelperGeneric.perf_log(TAG, "friend/frag:onResume:update_all_messages +" + (SystemClock.elapsedRealtime() - perf_t) + "ms");
        perf_t = SystemClock.elapsedRealtime();
        }
        else
        {
            // nothing changed: just rebind visible rows (cheap), no DB reload
            HelperGeneric.perf_log(TAG, "friend/frag:onResume:redraw-only +" + (SystemClock.elapsedRealtime() - perf_t) + "ms");
        perf_t = SystemClock.elapsedRealtime();
            adapter.redraw_all_items();
        }

        if (!is_data_loaded)
        {
            is_at_bottom = true;
            is_data_loaded = true;
        }

        HelperGeneric.perf_log(TAG, "friend/frag:onResume:TOTAL +" + (SystemClock.elapsedRealtime() - perf_t0) + "ms");
        MainActivity.message_list_fragment = this;

        // D: re-check the dirty flag after the ref assignment (see group note). the friend wait-loop
        //    normally appends after onResume, but a dirty flag set in the window (e.g. an out-of-order
        //    or edit update skipped while the ref was null) should still trigger a reload here.
        if (MainActivity.message_list_dirty_while_away)
        {
            update_all_messages(true, false, PREF__messageview_paging);
        }
        // Log.i(TAG, "onResume:099");
    }

    @Override
    public void onPause()
    {
        try
        {
            if (PREF__messageview_paging)
            {
                messages_count_old = get_messages_count_full();
            }
        }
        catch (Exception e)
        {
        }

        Log.i(TAG, "onPause");
        super.onPause();

        // HINT: super ugly hack to find all audioplay recylerviews and stop any audio playing
        // you have a better solution? let me hear it.
        try
        {
            View child;
            for (int i = 0; i < listingsView.getChildCount(); i++)
            {
                child = listingsView.getChildAt(i);
                try
                {
                    RecyclerView.ViewHolder vh = listingsView.getChildViewHolder(child);
                    ((MessageListHolder_file_outgoing_state_cancel) vh).DetachedFromWindow(true);
                }
                catch(Exception e1)
                {
                }
                try
                {
                    RecyclerView.ViewHolder vh = listingsView.getChildViewHolder(child);
                    ((MessageListHolder_file_incoming_state_cancel) vh).DetachedFromWindow(true);
                }
                catch(Exception e1)
                {
                }
            }
        }
        catch(Exception e2)
        {
        }
        // HINT: super ugly hack to find all audioplay recylerviews and stop any audio playing

        global_showing_messageview = false;
        MainActivity.message_list_fragment = null;
    }

    void reset_paging()
    {
        load_all_messages = false; // reset "load all messages" when we change friend that is shown
    }

    void update_all_messages(final boolean from_resume_fragment, final boolean stay_at_top, final boolean paging)
    {
        Log.i(TAG, "update_all_messages");
        final long perf_t0 = SystemClock.elapsedRealtime();
        long perf_t = perf_t0;

        try
        {
            // reset "new" flags for messages -------
            // C: searching does not mark the whole chat as read
            if ((search_messages_text == null) || (search_messages_text.length() == 0))
            {
                String friend_pubkey = tox_friend_get_public_key__wrapper(current_friendnum);
                int count_read = orma.selectFromMessage().tox_friendpubkeyEq(friend_pubkey).is_newEq(true).count();
                orma.updateMessage().tox_friendpubkeyEq(friend_pubkey).
                        is_newEq(true).is_new(false).execute();
                HelperMsgNotification.subtract_unread_count_for_key(friend_pubkey, count_read);
            }
            // reset "new" flags for messages -------
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        HelperGeneric.perf_log(TAG, "friend/load:mark-read +" + (SystemClock.elapsedRealtime() - perf_t) + "ms");
        perf_t = SystemClock.elapsedRealtime();

        // B: snapshot the full count BEFORE the SELECT (see group onCreateView note): "counted => fetched".
        try
        {
            messages_count_at_last_load = get_messages_count_full();
        }
        catch (Exception e)
        {
            messages_count_at_last_load = -1;
        }
        HelperGeneric.perf_log(TAG, "friend/load:count-full +" + (SystemClock.elapsedRealtime() - perf_t) + "ms");
        perf_t = SystemClock.elapsedRealtime();
        try
        {
            int number_of_items_old = data_values.size();
            int count_messages_full_old = messages_count_old;
            data_values.clear();

            List<com.zoffcc.applications.sorm.Message> ml = get_messages();
            HelperGeneric.perf_log(TAG, "friend/load:get-messages rows=" + ((ml != null) ? ml.size() : -1) + " +" + (SystemClock.elapsedRealtime() - perf_t) + "ms");
            perf_t = SystemClock.elapsedRealtime();
            adapter.add_list_clear(ml);
            HelperGeneric.perf_log(TAG, "friend/load:add_list_clear +" + (SystemClock.elapsedRealtime() - perf_t) + "ms");
        perf_t = SystemClock.elapsedRealtime();
            HelperGeneric.perf_log_first_layout(TAG, "friend/load", listingsView, perf_t0);

            if (stay_at_top)
            {
                // used by the "load all messages" dummy entry: keep the user at the top
                is_at_bottom = false;
                listingsView.scrollToPosition(0);
            }

            try
            {
                if (from_resume_fragment)
                {
                    boolean has_new_messages;
                    if ((PREF__messageview_paging) && (!load_all_messages) && (!show_only_files) &&
                        ((search_messages_text == null) || (search_messages_text.length() == 0)))
                    {
                        // limited view: the list size stays ~constant, so compare FULL message counts instead
                        has_new_messages = (count_messages_full_old > 0) &&
                                           (get_messages_count_full() > count_messages_full_old);
                    }
                    else
                    {
                        has_new_messages = (number_of_items_old > 0) && (data_values.size() > number_of_items_old);
                    }

                    if (has_new_messages)
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

        HelperGeneric.perf_log(TAG, "friend/load:TOTAL +" + (SystemClock.elapsedRealtime() - perf_t0) + "ms");
        MainActivity.message_list_dirty_while_away = false;

    }

    private List<com.zoffcc.applications.sorm.Message> get_messages()
    {
        final long perf_t0 = SystemClock.elapsedRealtime();
        long perf_t = perf_t0;
        List<com.zoffcc.applications.sorm.Message> ml;

        // ---- message load limit: only load the newest N messages ----
        // HINT: applies to the default view only (not to search results or the "files only" filter)
        if ((PREF__messageview_paging) && (!load_all_messages) && (!show_only_files) &&
            ((search_messages_text == null) || (search_messages_text.length() == 0)))
        {
            int count_messages = orma.selectFromMessage().
                    tox_friendpubkeyEq(tox_friend_get_public_key__wrapper(current_friendnum)).
                    count();

            HelperGeneric.perf_log(TAG, "friend/query:count n=" + count_messages + " +" + (SystemClock.elapsedRealtime() - perf_t) + "ms");
        perf_t = SystemClock.elapsedRealtime();
            if (count_messages > PREF__message_paging_num_msgs_per_page)
            {
                int older_messages_count = count_messages - PREF__message_paging_num_msgs_per_page;

                ml = orma.selectFromMessage().
                        tox_friendpubkeyEq(tox_friend_get_public_key__wrapper(current_friendnum)).
                        orderBySent_timestampDesc().
                        orderBySent_timestamp_msDesc().
                        limit(PREF__message_paging_num_msgs_per_page).
                        toList();

                if (ml != null)
                {
                    // newest N via index tail scan (DESC + LIMIT), then reverse to ascending.
                    // the old ASC + OFFSET walked and discarded all "older" rows on every open,
                    // and its stale offset could miss messages arriving between COUNT and SELECT.
                    Collections.reverse(ml);

                    // dummy entry at the top: clicking it loads the full history
                    Message m_load_all = new Message();
                    m_load_all.tox_friendpubkey = TRIFA_SYSTEM_MESSAGE_PEER_PUBKEY;
                    m_load_all.msg_idv3_hash = MESSAGE_PAGING_LOAD_ALL_HASH;
                    m_load_all.is_new = false;
                    m_load_all.direction = 0;
                    m_load_all.text = "Load all messages (" + older_messages_count + " older)";
                    ml.add(0, m_load_all);
                }

                HelperGeneric.perf_log(TAG, "friend/query:select-limited rows=" + ((ml != null) ? ml.size() : -1) + " +" + (SystemClock.elapsedRealtime() - perf_t) + "ms (total " + (SystemClock.elapsedRealtime() - perf_t0) + "ms)");
                return ml;
            }
        }
        // ---- message load limit ----

        if (show_only_files)
        {
            ml = orma.selectFromMessage().
                    tox_friendpubkeyEq(tox_friend_get_public_key__wrapper(current_friendnum)).
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
                        textLike(get_sqlite_search_string(search_messages_text)).
                        toList();
            }
        }
        HelperGeneric.perf_log(TAG, "friend/query:select-full-or-search rows=" + ((ml != null) ? ml.size() : -1) + " +" + (SystemClock.elapsedRealtime() - perf_t) + "ms (total " + (SystemClock.elapsedRealtime() - perf_t0) + "ms)");
        return ml;
    }

    private int get_messages_count_full()
    {
        int c = 0;
        if (show_only_files)
        {
            c = orma.selectFromMessage().
                    tox_friendpubkeyEq(tox_friend_get_public_key__wrapper(current_friendnum)).
                    TRIFA_MESSAGE_TYPEEq(TRIFA_MSG_FILE.value).
                    orderBySent_timestampAsc().
                    orderBySent_timestamp_msAsc().
                    count();
        }
        else
        {
            if ((search_messages_text == null) || (search_messages_text.length() == 0))
            {
                c = orma.selectFromMessage().
                        tox_friendpubkeyEq(tox_friend_get_public_key__wrapper(current_friendnum)).
                        orderBySent_timestampAsc().
                        orderBySent_timestamp_msAsc().
                        count();
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
                c = orma.selectFromMessage().
                        tox_friendpubkeyEq(tox_friend_get_public_key__wrapper(current_friendnum)).
                        orderBySent_timestampAsc().
                        orderBySent_timestamp_msAsc().
                        textLike(get_sqlite_search_string(search_messages_text)).
                        count();
            }
        }
        return c;
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
                    Log.i(TAG, "add_message:002");
                    // A: only advance the resume-skip count when the append actually happened
                    //    (add_item dedups by row id and returns false on a duplicate)
                    if (adapter.add_item(m))
                    {
                        // C: keep the resume-skip count in sync with live appends
                        if (messages_count_at_last_load >= 0)
                        {
                            messages_count_at_last_load++;
                        }
                    }

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
