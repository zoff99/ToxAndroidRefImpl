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

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.zoffcc.applications.trifa.HelperFriend.main_get_friend;
import static com.zoffcc.applications.trifa.HelperFriend.tox_friend_by_public_key__wrapper;
import static com.zoffcc.applications.trifa.HelperRelay.get_pushurl_for_friend;
import static com.zoffcc.applications.trifa.HelperRelay.get_relay_for_friend;
import static com.zoffcc.applications.trifa.Identicon.create_avatar_identicon_for_pubkey;
import static com.zoffcc.applications.trifa.MainActivity.VFS_ENCRYPT;
import static com.zoffcc.applications.trifa.TRIFAGlobals.FRIEND_AVATAR_FILENAME;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VFS_FILE_DIR;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VFS_PREFIX;
import static com.zoffcc.applications.trifa.ToxVars.TOX_CONNECTION.TOX_CONNECTION_NONE;
import static com.zoffcc.applications.trifa.ToxVars.TOX_CONNECTION.TOX_CONNECTION_TCP;

public class FriendSelectSingleAdapter extends ArrayAdapter<FriendSelectSingle>
{
    List<FriendSelectSingle> datalist;
    Context context;
    int resource;

    public FriendSelectSingleAdapter(Context context, int resource, List<FriendSelectSingle> input_datalist)
    {
        super(context, resource, input_datalist);
        this.context = context;
        this.resource = resource;
        this.datalist = input_datalist;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(resource, null, false);
        TextView textViewName = view.findViewById(R.id.textViewName);
        de.hdodenhof.circleimageview.CircleImageView avatar = (de.hdodenhof.circleimageview.CircleImageView) view.findViewById(
                R.id.f_avatar_icon);
        ImageView f_status_icon = (ImageView) view.findViewById(R.id.f_status_icon);
        ImageView f_relay_icon = (ImageView) view.findViewById(R.id.f_relay_icon);
        FriendSelectSingle friend_entry = datalist.get(position);
        final Drawable d_lock = new IconicsDrawable(context).
                icon(FontAwesome.Icon.faw_lock).color(context.getResources().
                getColor(R.color.colorPrimaryDark)).sizeDp(80);
        final FriendList fl = main_get_friend(friend_entry.pubkey);
        // ------ now fill with data ------
        textViewName.setText(friend_entry.getName());
        avatar.setImageDrawable(d_lock);
        try
        {
            if (VFS_ENCRYPT)
            {
                boolean need_create_identicon = true;

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
                    if (f1.length() > 0)
                    {
                        // Log.i(TAG, "AVATAR_GLIDE:" + ":" + fl.name + ":" + fl.avatar_filename);
                        final RequestOptions glide_options = new RequestOptions().fitCenter();
                        GlideApp.
                                with(avatar.getContext()).
                                load(f1).
                                diskCacheStrategy(DiskCacheStrategy.RESOURCE).
                                placeholder(d_lock).
                                priority(Priority.HIGH).
                                skipMemoryCache(false).
                                apply(glide_options).
                                into(avatar);

                        need_create_identicon = false;
                    }
                    else
                    {
                        avatar.setImageDrawable(d_lock);
                    }
                }

                if (need_create_identicon)
                {
                    // no avatar icon? create and use Identicon ------------
                    create_avatar_identicon_for_pubkey(fl.tox_public_key_string);


                    // -- ok, now try to show the avtar icon again --

                    String new_avatar_pathname = VFS_PREFIX + VFS_FILE_DIR + "/" + fl.tox_public_key_string + "/";
                    String new_avatar_filename = FRIEND_AVATAR_FILENAME;
                    f1 = null;
                    try
                    {
                        f1 = new info.guardianproject.iocipher.File(new_avatar_pathname + "/" + new_avatar_filename);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                    if ((f1 != null) && (new_avatar_pathname != null))
                    {
                        if (f1.length() > 0)
                        {
                            // Log.i(TAG, "AVATAR_GLIDE:" + ":" + fl.name + ":" + new_avatar_filename);

                            final RequestOptions glide_options = new RequestOptions().fitCenter();
                            GlideApp.
                                    with(avatar.getContext()).
                                    load(f1).
                                    diskCacheStrategy(DiskCacheStrategy.RESOURCE).
                                    signature(new com.bumptech.glide.signature.StringSignatureZ(
                                            "_avatar_" + new_avatar_pathname + "/" + FRIEND_AVATAR_FILENAME + "_" +
                                            fl.avatar_update_timestamp)).
                                    placeholder(d_lock).
                                    priority(Priority.HIGH).
                                    skipMemoryCache(false).
                                    apply(glide_options).
                                    into(avatar);
                        }
                        else
                        {
                            // ok still nothing, show that default "lock" icon
                            avatar.setImageDrawable(d_lock);
                        }
                    }
                    // -- ok, now try to show the avtar icon again --

                    // no avatar icon? create and use Identicon ------------
                }


            } // VFS_ENCRYPT -- END --
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
                    fis.close();

                    final RequestOptions glide_options = new RequestOptions().fitCenter();
                    GlideApp.
                            with(context).
                            load(byteArray).
                            placeholder(d_lock).
                            diskCacheStrategy(DiskCacheStrategy.RESOURCE).
                            signature(new com.bumptech.glide.signature.StringSignatureZ(
                                    "_avatar_" + fl.avatar_pathname + "/" + fl.avatar_filename + "_" +
                                    fl.avatar_update_timestamp)).
                            skipMemoryCache(false).
                            apply(glide_options).
                            into(avatar);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        f_status_icon.setVisibility(View.VISIBLE);
        f_relay_icon.setVisibility(View.INVISIBLE);

        String relay_ = get_relay_for_friend(fl.tox_public_key_string);

        if (relay_ != null) // friend HAS a relay
        {
            FriendList relay_fl = main_get_friend(tox_friend_by_public_key__wrapper(relay_));

            if (relay_fl != null)
            {
                if (fl.TOX_CONNECTION_real == 0)
                {
                    f_status_icon.setImageResource(R.drawable.circle_red);
                }
                else
                {
                    f_status_icon.setImageResource(R.drawable.circle_green);
                }

                if (relay_fl.TOX_CONNECTION_real == 0)
                {
                    f_relay_icon.setImageResource(R.drawable.circle_red);
                }
                else
                {
                    f_relay_icon.setImageResource(R.drawable.circle_green);
                }

                f_status_icon.setVisibility(View.VISIBLE);
                f_relay_icon.setVisibility(View.VISIBLE);
            }
        }
        else // friend has no relay
        {
            // Log.d(TAG, "004");

            String get_pushurl_for_friend = get_pushurl_for_friend(fl.tox_public_key_string);

            if ((get_pushurl_for_friend != null) && (get_pushurl_for_friend.length() > "https:".length()))
            {
                // friend has push support
                f_relay_icon.setImageResource(R.drawable.circle_orange);
                f_relay_icon.setVisibility(View.VISIBLE);
            }
            else
            {
                if (fl.TOX_CONNECTION == 0)
                {
                    f_status_icon.setImageResource(R.drawable.circle_red);
                }
                else
                {
                    f_status_icon.setImageResource(R.drawable.circle_green);
                }
            }
        }

        try
        {
            if (fl.TOX_CONNECTION_real == TOX_CONNECTION_NONE.value)
            {
                avatar.setBorderColor(Color.parseColor("#40000000"));
            }
            else if (fl.TOX_CONNECTION_real == TOX_CONNECTION_TCP.value)
            {
                avatar.setBorderColor(Color.parseColor("#FFCE00"));
            }
            else // UDP
            {
                avatar.setBorderColor(Color.parseColor("#04B431"));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        // ------ now fill with data ------
        return view;
    }
}
