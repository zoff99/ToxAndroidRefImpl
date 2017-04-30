package com.zoffcc.applications.trifa;

import android.annotation.TargetApi;
import android.app.ListFragment;
import android.os.Build;
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

    // TODO make it work with API less than 23 (M) !!
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        data_values = new ArrayList<FriendList>();

        FriendList tmp = new FriendList();
        // tmp.name = "name1";
        // tmp.status_message = "status message 1";
        // tmp.TOXCONNECTION = 1;
        // values.add(tmp);

        a = new FriendlistArrayAdapter(FriendListFragment.this.getContext(), data_values);
        setListAdapter(a);
    }

    void modify_friend(FriendList f, long friendnum)
    {
        int size = data_values.size();
        int i = 0;
        for (i = 0; i < size; i++)
        {
            if (data_values.get(i).tox_friendnum == friendnum)
            {
                FriendList n = deep_copy(f);
                data_values.set(i, n);

                Log.i(TAG, "modify_friend:found friend:" + friendnum);

                Runnable myRunnable = new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Log.i(TAG, "modify_friend:notifyDataSetChanged");
                        a.notifyDataSetChanged();
                    }
                };
                main_handler_s.post(myRunnable);
            }
        }
    }

    FriendList get_friend(long friendnum)
    {
        int size = data_values.size();
        int i = 0;
        for (i = 0; i < size; i++)
        {
            if (data_values.get(i).tox_friendnum == friendnum)
            {
                Log.i(TAG, "get_friend:found friend:" + friendnum);
                FriendList n = deep_copy(data_values.get(i));
                return n;
            }
        }
        return null;
    }


    void add_friends(FriendList f)
    {
        FriendList n = deep_copy(f);
        data_values.add(n);

        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
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
