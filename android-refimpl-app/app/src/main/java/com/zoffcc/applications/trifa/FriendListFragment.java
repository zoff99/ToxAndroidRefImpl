package com.zoffcc.applications.trifa;

import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import static com.zoffcc.applications.trifa.FriendList.deep_copy;
import static com.zoffcc.applications.trifa.MainActivity.main_handler_s;

public class FriendListFragment extends ListFragment
{
    private static final String TAG = "trifa.FriendListFrgnt";
    List<FriendList> data_values = null;
    FriendlistArrayAdapter a = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.friend_list_layout, container, false);
        MainActivity.friend_list_fragment = this;
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
        super.onAttach(context);
        data_values = new ArrayList<FriendList>();
        a = new FriendlistArrayAdapter(context, data_values);
        setListAdapter(a);
    }

    void modify_friend(final FriendList f, final long friendnum)
    {
        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                boolean found_friend = false;
                int size = data_values.size();
                int i = 0;
                for (i = 0; i < size; i++)
                {
                    if (data_values.get(i).tox_friendnum == friendnum)
                    {
                        found_friend = true;
                        FriendList n = deep_copy(f);
                        data_values.set(i, n);
                        Log.i(TAG, "modify_friend:found friend:" + friendnum);
                        a.notifyDataSetChanged();
                    }
                }

                if (!found_friend)
                {
                    add_friends(f);
                }
            }
        };
        main_handler_s.post(myRunnable);
    }

    //    FriendList get_friend(long friendnum)
    //    {
    //        int size = data_values.size();
    //        int i = 0;
    //        for (i = 0; i < size; i++)
    //        {
    //            if (data_values.get(i).tox_friendnum == friendnum)
    //            {
    //                Log.i(TAG, "get_friend:found friend:" + friendnum);
    //                FriendList n = deep_copy(data_values.get(i));
    //                return n;
    //            }
    //        }
    //        return null;
    //    }


    void add_friends(final FriendList f)
    {
        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                FriendList n = deep_copy(f);
                data_values.add(n);
                a.notifyDataSetChanged();
            }
        };

        main_handler_s.post(myRunnable);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        Log.i(TAG, "onListItemClick pos=" + position + " id=" + id);
    }
}
