package com.zoffcc.applications.trifa;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class OwnStatusSpinnerAdapter extends ArrayAdapter<String>
{
    private List<String> objects;
    private Context context;

    public OwnStatusSpinnerAdapter(Context context, int resourceId, List<String> objects)
    {
        super(context, resourceId, objects);
        this.objects = objects;
        this.context = context;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent)
    {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        return getCustomHeaderView(position, convertView, parent);
    }

    public View getCustomHeaderView(int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.spinner_own_status_header, parent, false);
        ImageView icon = (ImageView) row.findViewById(R.id.spinner_item_icon_02);

        if (position == 0)
        {
            icon.setImageResource(R.drawable.circle_green);
        }
        else if (position == 1)
        {
            icon.setImageResource(R.drawable.circle_yellow);
        }
        else if (position == 2)
        {
            icon.setImageResource(R.drawable.circle_red);
        }

        return row;
    }

    public View getCustomView(int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.own_status_spinner_item, parent, false);
        TextView label = (TextView) row.findViewById(R.id.spinner_item_text_01);
        ImageView icon = (ImageView) row.findViewById(R.id.spinner_item_icon_01);
        label.setText(objects.get(position));

        if (position == 0)
        {
            // label.setTextColor(Color.parseColor("#04B431"));
            icon.setImageResource(R.drawable.circle_green);
        }
        else if (position == 1)
        {
            // label.setTextColor(Color.parseColor("#F7FE2E"));
            icon.setImageResource(R.drawable.circle_yellow);
        }
        else if (position == 2)
        {
            // label.setTextColor(Color.parseColor("#FF0000"));
            icon.setImageResource(R.drawable.circle_red);
        }

        return row;
    }


}
