/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2021 Zoff <zoff@zoff.cc>
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

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import info.guardianproject.iocipher.File;

import static com.zoffcc.applications.trifa.ToxVars.TOX_PUBLIC_KEY_SIZE;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;

public class IOBrowser extends ListActivity
{
    private static final String TAG = "trifa.IOBrowser";

    private List<String> item = null;
    private List<String> path = null;
    private TextView fileInfo;
    private String[] items;
    private String root = "/";
    private String cur_path = root;
    private boolean at_root_dir = true;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iobrowser);
        fileInfo = (TextView) findViewById(R.id.iobrowser_info);

        cur_path = root;
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        try
        {
            Thread.sleep(300);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        at_root_dir = true;
        getFileList(cur_path);
    }

    public void getFileList(String dirPath)
    {

        cur_path = dirPath;

        item = new ArrayList<String>();
        path = new ArrayList<String>();

        info.guardianproject.iocipher.File file = new info.guardianproject.iocipher.File(dirPath);
        info.guardianproject.iocipher.File[] files = file.listFiles();
        Arrays.sort(files);

        if (!dirPath.equals(root))
        {
            item.add(root);
            path.add(root);// to get back to main list

            item.add("..");
            path.add(file.getParent()); // back one level

            at_root_dir = false;
        }
        else
        {
            at_root_dir = true;
        }

        for (File fileItem : files)
        {
            path.add(fileItem.getPath());

            String item_name = fileItem.getName();
            String display_name = item_name;

            if (fileItem.isDirectory())
            {
                // input name directory to array list
                // Log.i(TAG, "dir:" + item_name);

                if (item_name.length() == (TOX_PUBLIC_KEY_SIZE * 2))
                {
                    try
                    {
                        display_name = orma.selectFromFriendList().
                                tox_public_key_stringEq(item_name).
                                toList().get(0).name;

                        String alias_name = orma.selectFromFriendList().
                                tox_public_key_stringEq(item_name).
                                toList().get(0).alias_name;

                        if ((alias_name != null) && (alias_name.length() > 0))
                        {
                            display_name = alias_name;
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }

                item.add("[" + display_name + "]");
            }
            else
            {
                // input name file to array list
                item.add(item_name);
            }
        }
        fileInfo.setText("Info: " + dirPath + " [ " + files.length + " item ]");
        // declare array with specific number of items
        items = new String[item.size()];
        // send data arraylist(item) to array(items)
        item.toArray(items);
        setListAdapter(new IconicList());
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id)
    {
        File file = new File(path.get(position));
        if (file.isDirectory())
        {
            if (file.canRead())
            {
                getFileList(path.get(position));
            }
            else
            {
                new AlertDialog.Builder(this).setIcon(R.mipmap.ic_launcher).setTitle(
                        "[" + file.getName() + "] folder can't be read").setPositiveButton("OK",
                                                                                           new DialogInterface.OnClickListener()
                                                                                           {

                                                                                               @Override
                                                                                               public void onClick(DialogInterface dialog, int which)
                                                                                               {
                                                                                                   // TODO Auto-generated method stub
                                                                                               }
                                                                                           }).show();

            }
        }
        else
        {
            // Log.i(TAG, "open URL: " + Uri.parse(IOCipherContentProvider.FILES_URI + cur_path + "/" + file.getName()));
            final Uri uri = Uri.parse(IOCipherContentProvider.FILES_URI + cur_path + "/" + file.getName());
            new AlertDialog.Builder(this).setIcon(R.mipmap.ic_launcher).setTitle(file.getName()).setNeutralButton(
                    "View", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            try
                            {
                                Intent sendIntent = new Intent(Intent.ACTION_VIEW, uri);
                                startActivity(sendIntent);
                            }
                            catch (ActivityNotFoundException e)
                            {
                                Log.e(TAG, "No relevant Activity found", e);
                            }
                        }
                    }).setPositiveButton("Share...", new DialogInterface.OnClickListener()
            {

                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    Intent intent = new Intent(Intent.ACTION_SEND, uri);
                    // intent.putExtra(Intent.EXTRA_STREAM, uri);
                    String fileExtension = MimeTypeMap.getFileExtensionFromUrl(file.getName());
                    String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
                    if (mimeType == null)
                    {
                        mimeType = "application/octet-stream";
                    }
                    // Log.i(TAG, "mime type: " + mimeType);
                    intent.setType(mimeType);
                    try
                    {
                        startActivity(Intent.createChooser(intent, "Share this!"));
                    }
                    catch (ActivityNotFoundException e)
                    {
                        Log.e(TAG, "No relevant Activity found", e);
                    }
                }
            }).show();
        }
    }

    class IconicList extends ArrayAdapter
    {

        public IconicList()
        {
            super(IOBrowser.this, R.layout.iobrowser_row, items);
            // TODO Auto-generated constructor stub
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            LayoutInflater inflater = getLayoutInflater();
            View row = inflater.inflate(R.layout.iobrowser_row, null);
            TextView label = (TextView) row.findViewById(R.id.io_browser_label);
            ImageView icon = (ImageView) row.findViewById(R.id.io_browser_icon);
            TextView filesize = (TextView) row.findViewById(R.id.io_browser_filesize);
            label.setText(items[position]);

            try
            {
                info.guardianproject.iocipher.File f = new info.guardianproject.iocipher.File(path.get(position));

                if (f.isDirectory())
                {
                    icon.setImageResource(R.drawable.iobrowser_folder);
                    filesize.setText("");
                }
                else
                {
                    icon.setImageResource(R.drawable.iobrowser_text);
                    long fsize = f.length();

                    if (fsize > 1024)
                    {
                        if (fsize > (1024 * 1024))
                        {
                            filesize.setText("" + (f.length() / (1024 * 1024)) + " MB");
                        }
                        else
                        {
                            filesize.setText("" + (f.length() / 1024) + " KB");
                        }
                    }
                    else
                    {
                        filesize.setText("" + f.length() + " B");
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            return (row);
        }
    }

    @Override
    public void onBackPressed()
    {
        if (!at_root_dir)
        {
            getFileList(path.get(1));
        }
        else
        {
            super.onBackPressed();
        }
    }
}