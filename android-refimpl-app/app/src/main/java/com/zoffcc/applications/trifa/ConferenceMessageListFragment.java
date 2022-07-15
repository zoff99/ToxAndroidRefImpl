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

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.zoffcc.applications.trifa.HelperGeneric.get_sqlite_search_string;
import static com.zoffcc.applications.trifa.MainActivity.PREF__conference_show_system_messages;
import static com.zoffcc.applications.trifa.MainActivity.main_handler_s;
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
    TextView scrollDateHeader = null;
    ConversationDateHeader conversationDateHeader = null;
    static String conf_search_messages_text = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Log.i(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.conference_message_list_layout, container, false);


        ConferenceMessageListActivity mla = (ConferenceMessageListActivity) (getActivity());
        if (mla != null)
        {
            current_conf_id = mla.get_current_conf_id();
        }
        // Log.i(TAG, "current_conf_id=" + current_conf_id);

        // default is: at bottom
        is_at_bottom = true;


        try
        {
            // reset "new" flags for messages -------
            if (orma != null)
            {
                orma.updateConferenceMessage().
                        conference_identifierEq(current_conf_id).
                        is_new(false).execute();
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
                if (PREF__conference_show_system_messages)
                {
                    if ((conf_search_messages_text == null) || (conf_search_messages_text.length() == 0))
                    {
                        // TODO: sort by sent_timestamp ?
                        data_values = orma.selectFromConferenceMessage().
                                conference_identifierEq(current_conf_id).
                                orderBySent_timestampAsc().
                                toList();
                    }
                    else
                    {
                        // TODO: sort by sent_timestamp ?
                        /*
                         searching for case-IN-sensitive non ascii chars is not working:

                         https://sqlite.org/lang_expr.html#like

                         Important Note: SQLite only understands upper/lower case for ASCII characters by default.
                         The LIKE operator is case sensitive by default for unicode characters that are beyond
                         the ASCII range. For example, the expression 'a' LIKE 'A' is TRUE but 'æ' LIKE 'Æ' is FALSE
                         */
                        data_values = orma.selectFromConferenceMessage().
                                conference_identifierEq(current_conf_id).
                                orderBySent_timestampAsc().
                                where(" like('" + get_sqlite_search_string(conf_search_messages_text) +
                                      "', text, '\\')").
                                toList();
                    }
                }
                else
                {
                    if ((conf_search_messages_text == null) || (conf_search_messages_text.length() == 0))
                    {
                        // TODO: sort by sent_timestamp ?
                        data_values = orma.selectFromConferenceMessage().
                                conference_identifierEq(current_conf_id).
                                and().
                                tox_peerpubkeyNotEq(TRIFA_SYSTEM_MESSAGE_PEER_PUBKEY).
                                orderBySent_timestampAsc().
                                toList();
                    }
                    else
                    {
                        // TODO: sort by sent_timestamp ?
                        /*
                         searching for case-IN-sensitive non ascii chars is not working:

                         https://sqlite.org/lang_expr.html#like

                         Important Note: SQLite only understands upper/lower case for ASCII characters by default.
                         The LIKE operator is case sensitive by default for unicode characters that are beyond
                         the ASCII range. For example, the expression 'a' LIKE 'A' is TRUE but 'æ' LIKE 'Æ' is FALSE
                         */
                        data_values = orma.selectFromConferenceMessage().
                                conference_identifierEq(current_conf_id).
                                and().
                                tox_peerpubkeyNotEq(TRIFA_SYSTEM_MESSAGE_PEER_PUBKEY).
                                orderBySent_timestampAsc().
                                where(" like('" + get_sqlite_search_string(conf_search_messages_text) +
                                      "', text, '\\')").
                                toList();
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            // data_values is NULL here!!
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

        RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState)
            {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == RecyclerView.SCROLL_STATE_DRAGGING)
                {
                    conversationDateHeader.show();
                }
                else if (newState == RecyclerView.SCROLL_STATE_IDLE)
                {
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
                        view.findViewById(R.id.unread_messages_notice_button).setVisibility(view.INVISIBLE);
                    }
                }
                else
                {
                    if (is_at_bottom)
                    {
                        // Log.i(TAG, "onScrolled:NOT at bottom");
                        is_at_bottom = false;
                        view.findViewById(R.id.unread_messages_notice_button).setVisibility(view.VISIBLE);
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

        MainActivity.conference_message_list_fragment = this;

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

    @Override
    public void onResume()
    {
        Log.i(TAG, "onResume");
        super.onResume();

        global_showing_anygroupview = true;
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

    synchronized void add_message(final ConferenceMessage m)
    {
        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    adapter.add_item(m);
                    if (is_at_bottom)
                    {
                        listingsView.scrollToPosition(adapter.getItemCount() - 1);
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

    void update_all_messages(boolean always)
    {
        Log.i(TAG, "update_all_messages");

        try
        {
            // reset "new" flags for messages -------
            if (orma != null)
            {
                orma.updateConferenceMessage().
                        conference_identifierEq(current_conf_id).
                        is_new(false).execute();
            }
            // reset "new" flags for messages -------
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            if ((always) || (data_values != null))
            {
                Log.i(TAG, "data_values:005a");
                if (data_values != null)
                {
                    data_values.clear();
                }
                Log.i(TAG, "data_values:005b");

                // -------------------------------------------------
                // HINT: this one does not respect ordering?!
                // -------------------------------------------------
                if ((conf_search_messages_text == null) || (conf_search_messages_text.length() == 0))
                {
                    adapter.add_list_clear(orma.selectFromConferenceMessage().
                            conference_identifierEq(current_conf_id).
                            and().
                            tox_peerpubkeyNotEq(TRIFA_SYSTEM_MESSAGE_PEER_PUBKEY).
                            orderBySent_timestampAsc().
                            toList());
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
                    adapter.add_list_clear(orma.selectFromConferenceMessage().
                            conference_identifierEq(current_conf_id).
                            and().
                            tox_peerpubkeyNotEq(TRIFA_SYSTEM_MESSAGE_PEER_PUBKEY).
                            orderBySent_timestampAsc().
                            where(" like('" + get_sqlite_search_string(conf_search_messages_text) + "', text, '\\')").
                            toList());
                }
                Log.i(TAG, "data_values:005c");
            }
            Log.i(TAG, "data_values:005d");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "data_values:005:EE1:" + e.getMessage());
        }

    }

}
