package com.zoffcc.applications.trifa;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;

import static com.zoffcc.applications.trifa.MainActivity.VFS_ENCRYPT;
import static com.zoffcc.applications.trifa.MainActivity.tox_friend_by_public_key__wrapper;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;

public class FriendListHolder extends RecyclerView.ViewHolder implements View.OnClickListener
{
    private static final String TAG = "trifa.FriendListHolder";

    private FriendList friendlist;
    private Context context;

    private TextView textView;
    private TextView statusText;
    private TextView unread_count;
    private de.hdodenhof.circleimageview.CircleImageView avatar;
    private ImageView imageView;
    private ImageView imageView2;

    public FriendListHolder(View itemView, Context c)
    {
        super(itemView);

        Log.i(TAG, "FriendListHolder");

        this.context = c;

        textView = (TextView) itemView.findViewById(R.id.f_name);
        statusText = (TextView) itemView.findViewById(R.id.f_status_message);
        unread_count = (TextView) itemView.findViewById(R.id.f_unread_count);
        avatar = (de.hdodenhof.circleimageview.CircleImageView) itemView.findViewById(R.id.f_avatar_icon);
        imageView = (ImageView) itemView.findViewById(R.id.f_status_icon);
        imageView2 = (ImageView) itemView.findViewById(R.id.f_user_status_icon);

        itemView.setOnClickListener(this);
    }

    public void bindFriendList(FriendList fl)
    {
        Log.i(TAG, "bindFriendList");

        final Drawable d_lock = new IconicsDrawable(context).icon(FontAwesome.Icon.faw_lock).color(context.getResources().getColor(R.color.colorPrimaryDark)).sizeDp(24);

        this.friendlist = fl;

        textView.setText(fl.name);
        statusText.setText(fl.status_message);

        avatar.setImageDrawable(d_lock);
        try
        {
            if (VFS_ENCRYPT)
            {
                info.guardianproject.iocipher.File f1 = null;
                try
                {
                    f1 = new info.guardianproject.iocipher.File(fl.avatar_pathname + "/" + fl.avatar_filename);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                if ((f1 != null) && (fl.avatar_pathname != null))
                {
                    info.guardianproject.iocipher.FileInputStream fis = new info.guardianproject.iocipher.FileInputStream(f1);

                    if (f1.length() > 0)
                    {
                        byte[] byteArray = new byte[(int) f1.length()];
                        fis.read(byteArray, 0, (int) f1.length());

                        if (context != null)
                        {
                            GlideApp.
                                    with(context).
                                    load(byteArray).
                                    placeholder(d_lock).
                                    diskCacheStrategy(DiskCacheStrategy.NONE).
                                    skipMemoryCache(false).
                                    into(avatar);
                        }
                    }
                }
            }
            else
            {
                java.io.File f1 = null;
                try
                {
                    f1 = new java.io.File(fl.avatar_pathname + "/" + fl.avatar_filename);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                if ((f1 != null) && (fl.avatar_pathname != null))
                {
                    java.io.FileInputStream fis = new java.io.FileInputStream(f1);

                    byte[] byteArray = new byte[(int) f1.length()];
                    fis.read(byteArray, 0, (int) f1.length());

                    GlideApp.
                            with(context).
                            load(byteArray).
                            placeholder(d_lock).
                            diskCacheStrategy(DiskCacheStrategy.NONE).
                            skipMemoryCache(false).
                            into(avatar);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


        if (fl.TOX_CONNECTION == 0)
        {
            imageView.setImageResource(R.drawable.circle_red);
        }
        else
        {
            imageView.setImageResource(R.drawable.circle_green);
        }

        if (fl.TOX_USER_STATUS == 0)
        {
            imageView2.setImageResource(R.drawable.circle_green);
        }
        else if (fl.TOX_USER_STATUS == 1)
        {
            imageView2.setImageResource(R.drawable.circle_orange);
        }
        else
        {
            imageView2.setImageResource(R.drawable.circle_red);
        }

        try
        {
            int new_messages_count = orma.selectFromMessage().tox_friendpubkeyEq(fl.tox_public_key_string).and().is_newEq(true).count();
            if (new_messages_count > 0)
            {
                if (new_messages_count > 300)
                {
                    unread_count.setText("+");
                }
                else
                {
                    unread_count.setText("" + new_messages_count);
                }
                unread_count.setVisibility(View.VISIBLE);
            }
            else
            {
                unread_count.setText("");
                unread_count.setVisibility(View.INVISIBLE);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();

            unread_count.setText("");
            unread_count.setVisibility(View.INVISIBLE);
        }


    }

    @Override
    public void onClick(View v)
    {
        Log.i(TAG, "onClick");
        try
        {
            Intent intent = new Intent(v.getContext(), MessageListActivity.class);
            intent.putExtra("friendnum", tox_friend_by_public_key__wrapper(this.friendlist.tox_public_key_string));
            v.getContext().startActivity(intent);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "onClick:EE:" + e.getMessage());
        }
    }

}
