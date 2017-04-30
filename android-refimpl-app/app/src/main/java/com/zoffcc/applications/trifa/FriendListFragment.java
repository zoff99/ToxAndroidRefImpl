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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class FriendListFragment extends ListFragment
{
    private static final String TAG = "trifa.FriendListFrgnt";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.friend_list_layout, container, false);
        return view;
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        List<FriendList> values = new ArrayList<FriendList>();

        FriendList tmp = new FriendList();

        // tmp.name = "name1";
        // tmp.status_message = "status message 1";
        // tmp.TOXCONNECTION = 1;
        // values.add(tmp);

        FriendlistArrayAdapter a = new FriendlistArrayAdapter(FriendListFragment.this.getContext(), values);
        setListAdapter(a);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        Log.i(TAG, "onListItemClick pos=" + position + " id=" + id);
    }
}
