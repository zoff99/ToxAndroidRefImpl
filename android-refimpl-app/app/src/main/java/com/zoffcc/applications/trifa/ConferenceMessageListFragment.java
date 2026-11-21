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
import com.zoffcc.applications.sorm.ConferenceMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.zoffcc.applications.trifa.HelperGeneric.do_fade_anim_on_fab;
import static com.zoffcc.applications.trifa.HelperGeneric.get_sqlite_search_string;
import static com.zoffcc.applications.trifa.MainActivity.PREF__message_paging_num_msgs_per_page;
import static com.zoffcc.applications.trifa.MainActivity.PREF__conference_show_system_messages;
import static com.zoffcc.applications.trifa.MainActivity.PREF__messageview_paging;
import static com.zoffcc.applications.trifa.MainActivity.context_s;
import static com.zoffcc.applications.trifa.MainActivity.main_handler_s;
import static com.zoffcc.applications.trifa.TRIFAGlobals.MESSAGE_PAGING_LOAD_ALL_HASH;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_SYSTEM_MESSAGE_PEER_PUBKEY;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_showing_anygroupview;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;

public class ConferenceMessageListFragment extends Fragment
{
    private static final String TAG = "trifa.CnfMsgListFrgnt";
    List<ConferenceMessage> data_values = null;
    String current_conf_id = "-1";
    com.l4digital.fastscroll.FastScrollRecyclerView listingsView = null;
    ConferenceMessagelistAdapter adapter = null;
    static boolean is_at_bottom = true;
    // when true, the full message history is loaded for this conference (via the "load all messages" dummy entry).
    // reset to false every time the conference is opened (see reset_paging())
    static boolean load_all_messages = false;
    static boolean faded_in = false;
    TextView scrollDateHeader = null;
    ConversationDateHeader conversationDateHeader = null;
    boolean is_data_loaded = false;
    // C: full message count at the last load; used by onResume to detect whether anything changed while away
    private int messages_count_at_last_load = -1;
    static String conf_search_messages_text = null;
    FloatingActionButton unread_messages_notice_button = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Log.i(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.conference_message_list_layout, container, false);
        final long perf_t0 = SystemClock.elapsedRealtime();
        long perf_t = perf_t0;

        reset_paging();

        unread_messages_notice_button = view.findViewById(R.id.unread_messages_notice_button);
        unread_messages_notice_button.setAnimation(null);
        unread_messages_notice_button.setVisibility(View.INVISIBLE);
        unread_messages_notice_button.setSupportBackgroundTintList(
                (ContextCompat.getColorStateList(context_s, R.color.message_list_scroll_to_bottom_fab_bg_normal)));

        ConferenceMessageListActivity mla = (ConferenceMessageListActivity) (getActivity());
        if (mla != null)
        {
            current_conf_id = mla.get_current_conf_id();
        }
        // Log.i(TAG, "current_conf_id=" + current_conf_id);

        // default is: at bottom
        is_at_bottom = true;
        faded_in = false;

        try
        {
            // reset "new" flags for messages -------
            if (orma != null)
            {
                int count_read = orma.selectFromConferenceMessage().
                        conference_identifierEq(current_conf_id).is_newEq(true).count();
                orma.updateConferenceMessage().
                        conference_identifierEq(current_conf_id).
                        is_newEq(true).
                        is_new(false).execute();
                HelperMsgNotification.subtract_unread_count_for_key(current_conf_id, count_read);
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
                data_values = new ArrayList<ConferenceMessage>();
                data_values.clear();
            }
        }
        catch (Exception ignored)
        {
            ignored.printStackTrace();
        }

        // --------------
        // --------------
        // --------------
        adapter = new ConferenceMessagelistAdapter(view.getContext(), data_values);
        listingsView = (com.l4digital.fastscroll.FastScrollRecyclerView) view.findViewById(R.id.msg_rv_list);

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
                            do_fade_anim_on_fab(
                                    MainActivity.conference_message_list_fragment.unread_messages_notice_button, false,
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
                            do_fade_anim_on_fab(
                                    MainActivity.conference_message_list_fragment.unread_messages_notice_button, true,
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
                                do_fade_anim_on_fab(
                                        MainActivity.conference_message_list_fragment.unread_messages_notice_button,
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
                                do_fade_anim_on_fab(
                                        MainActivity.conference_message_list_fragment.unread_messages_notice_button,
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

        // MainActivity.conference_message_list_fragment = this;

        is_data_loaded = false;

        HelperGeneric.perf_log(TAG, "conference/frag:onCreateView:TOTAL +" + (SystemClock.elapsedRealtime() - perf_t0) + "ms");
        HelperGeneric.perf_log_first_layout(TAG, "conference/frag:onCreateView", listingsView, perf_t0);
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
        global_showing_anygroupview = true;

        Log.i(TAG, "onResume");
        super.onResume();
        final long perf_t0 = SystemClock.elapsedRealtime();
        long perf_t = perf_t0;

        HelperGeneric.perf_log(TAG, "conference/frag:onResume:reload-check +" + (SystemClock.elapsedRealtime() - perf_t) + "ms");
        perf_t = SystemClock.elapsedRealtime();
        // C: skip the full reload when nothing changed while we were away
        boolean reload_needed = (!is_data_loaded)
                                || MainActivity.conference_message_list_dirty_while_away
                                || ((conf_search_messages_text != null) && (conf_search_messages_text.length() > 0))
                                || (get_messages_count_full() != messages_count_at_last_load);
        if (reload_needed)
        {
            try
            {
                // reset "new" flags for messages -------
                if (orma != null)
                {
                    int count_read = orma.selectFromConferenceMessage().
                            conference_identifierEq(current_conf_id).is_newEq(true).count();
                    orma.updateConferenceMessage().
                            conference_identifierEq(current_conf_id).
                            is_newEq(true).
                            is_new(false).execute();
                    HelperMsgNotification.subtract_unread_count_for_key(current_conf_id, count_read);
                    // Log.i(TAG, "loading data:002");
                }
                // reset "new" flags for messages -------
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            HelperGeneric.perf_log(TAG, "conference/frag:onResume:mark-read +" + (SystemClock.elapsedRealtime() - perf_t) + "ms");
        perf_t = SystemClock.elapsedRealtime();
            update_all_messages(false, PREF__messageview_paging);
            HelperGeneric.perf_log(TAG, "conference/frag:onResume:update_all_messages +" + (SystemClock.elapsedRealtime() - perf_t) + "ms");
        perf_t = SystemClock.elapsedRealtime();

            // default is: at bottom
            is_at_bottom = true;
        }

        else
        {
            // nothing changed: just rebind visible rows (cheap), no DB reload
            HelperGeneric.perf_log(TAG, "conference/frag:onResume:redraw-only +" + (SystemClock.elapsedRealtime() - perf_t) + "ms");
        perf_t = SystemClock.elapsedRealtime();
            adapter.redraw_all_items();
        }

        is_data_loaded = true;

        HelperGeneric.perf_log(TAG, "conference/frag:onResume:TOTAL +" + (SystemClock.elapsedRealtime() - perf_t0) + "ms");
        MainActivity.conference_message_list_fragment = this;
    }

    @Override
    public void onPause()
    {
        super.onPause();

        global_showing_anygroupview = false;
        MainActivity.conference_message_list_fragment = null;
    }

    synchronized void modify_message(final ConferenceMessage m)
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

    synchronized void add_message(final ConferenceMessage m, final boolean is_real_new_message, final boolean stay_at_top)
    {
        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
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
                    e.printStackTrace();
                }
            }
        };

        if (main_handler_s != null)
        {
            main_handler_s.post(myRunnable);
        }
    }

    private int get_messages_count_full()
    {
        int c = 0;
        try
        {
            c = orma.selectFromConferenceMessage().
                    conference_identifierEq(current_conf_id).
                    tox_peerpubkeyNotEq(TRIFA_SYSTEM_MESSAGE_PEER_PUBKEY).
                    count();
        }
        catch (Exception e)
        {
        }
        return c;
    }

    void reset_paging()
    {
        load_all_messages = false; // reset "load all messages" when we change conference that is shown
    }

    void update_all_messages(final boolean stay_at_top, final boolean paging)
    {
        Log.i(TAG, "update_all_messages");
        final long perf_t0 = SystemClock.elapsedRealtime();
        long perf_t = perf_t0;

        try
        {
            // reset "new" flags for messages -------
            // C: searching does not mark the whole chat as read
            if ((orma != null) && ((conf_search_messages_text == null) || (conf_search_messages_text.length() == 0)))
            {
                int count_read = orma.selectFromConferenceMessage().
                        conference_identifierEq(current_conf_id).is_newEq(true).count();
                orma.updateConferenceMessage().
                        conference_identifierEq(current_conf_id).
                        is_newEq(true).
                        is_new(false).execute();
                HelperMsgNotification.subtract_unread_count_for_key(current_conf_id, count_read);
            }
            // reset "new" flags for messages -------
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        HelperGeneric.perf_log(TAG, "conference/load:mark-read +" + (SystemClock.elapsedRealtime() - perf_t) + "ms");
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
        HelperGeneric.perf_log(TAG, "conference/load:count-full +" + (SystemClock.elapsedRealtime() - perf_t) + "ms");
        perf_t = SystemClock.elapsedRealtime();
        try
        {
            if (data_values != null)
            {
                data_values.clear();
            }

            List<com.zoffcc.applications.sorm.ConferenceMessage> ml = null;
            boolean show_load_all_dummy = false;
            int older_messages_count = 0;

            if ((paging) && (!load_all_messages) &&
                ((conf_search_messages_text == null) || (conf_search_messages_text.length() == 0)))
            {
                // ---- message load limit: only load the newest N messages ----
                int count_messages = orma.selectFromConferenceMessage().
                        conference_identifierEq(current_conf_id).
                        tox_peerpubkeyNotEq(TRIFA_SYSTEM_MESSAGE_PEER_PUBKEY).
                        count();

                HelperGeneric.perf_log(TAG, "conference/query:count n=" + count_messages + " +" + (SystemClock.elapsedRealtime() - perf_t) + "ms");
        perf_t = SystemClock.elapsedRealtime();
                if (count_messages > PREF__message_paging_num_msgs_per_page)
                {
                    older_messages_count = count_messages - PREF__message_paging_num_msgs_per_page;

                    ml = orma.selectFromConferenceMessage().
                            conference_identifierEq(current_conf_id).
                            tox_peerpubkeyNotEq(TRIFA_SYSTEM_MESSAGE_PEER_PUBKEY).
                            orderBySent_timestampDesc().
                            limit(PREF__message_paging_num_msgs_per_page).
                            toList();

                    if (ml != null)
                    {
                        // newest N via index tail scan (DESC + LIMIT), then reverse to ascending.
                        // the old ASC + OFFSET walked and discarded all "older" rows on every open,
                        // and its stale offset could miss messages arriving between COUNT and SELECT.
                        Collections.reverse(ml);
                    }

                    show_load_all_dummy = true;
                }
                else
                {
                    ml = orma.selectFromConferenceMessage().
                            conference_identifierEq(current_conf_id).
                            tox_peerpubkeyNotEq(TRIFA_SYSTEM_MESSAGE_PEER_PUBKEY).
                            orderBySent_timestampAsc().
                            toList();
                }
                // ---- message load limit ----
            }
            else
            {
                if ((conf_search_messages_text == null) || (conf_search_messages_text.length() == 0))
                {
                    ml = orma.selectFromConferenceMessage().
                            conference_identifierEq(current_conf_id).
                            tox_peerpubkeyNotEq(TRIFA_SYSTEM_MESSAGE_PEER_PUBKEY).
                            orderBySent_timestampAsc().
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
                    ml = orma.selectFromConferenceMessage().
                            conference_identifierEq(current_conf_id).
                            tox_peerpubkeyNotEq(TRIFA_SYSTEM_MESSAGE_PEER_PUBKEY).
                            orderBySent_timestampAsc().
                            textLike(get_sqlite_search_string(conf_search_messages_text)).
                            toList();
                }
            }

            HelperGeneric.perf_log(TAG, "conference/query:select rows=" + ((ml != null) ? ml.size() : -1) + " +" + (SystemClock.elapsedRealtime() - perf_t) + "ms");
        perf_t = SystemClock.elapsedRealtime();
            if (ml != null)
            {
                if (show_load_all_dummy)
                {
                    // dummy entry at the top: clicking it loads the full history
                    ConferenceMessage m_load_all = new ConferenceMessage();
                    m_load_all.tox_peerpubkey = TRIFA_SYSTEM_MESSAGE_PEER_PUBKEY;
                    m_load_all.is_new = false;
                    m_load_all.direction = 0;
                    m_load_all.message_id_tox = MESSAGE_PAGING_LOAD_ALL_HASH;
                    m_load_all.text = "Load all messages (" + older_messages_count + " older)";
                    ml.add(0, m_load_all);
                }

                adapter.add_list_clear(ml);
                HelperGeneric.perf_log(TAG, "conference/load:add_list_clear +" + (SystemClock.elapsedRealtime() - perf_t) + "ms");
        perf_t = SystemClock.elapsedRealtime();
                HelperGeneric.perf_log_first_layout(TAG, "conference/load", listingsView, perf_t0);

                if (stay_at_top)
                {
                    // used by the "load all messages" dummy entry: keep the user at the top
                    is_at_bottom = false;
                    listingsView.scrollToPosition(0);
                }
                else if (is_at_bottom)
                {
                    listingsView.scrollToPosition(adapter.getItemCount() - 1);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "data_values:005:EE1:" + e.getMessage());
        }

        HelperGeneric.perf_log(TAG, "conference/load:TOTAL +" + (SystemClock.elapsedRealtime() - perf_t0) + "ms");
        MainActivity.conference_message_list_dirty_while_away = false;

    }

}
