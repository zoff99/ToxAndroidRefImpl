/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2023 Zoff <zoff@zoff.cc>
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

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.luseen.autolinklibrary.AutoLinkMode;
import com.luseen.autolinklibrary.EmojiTextViewLinks;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.net.URLConnection;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import static com.zoffcc.applications.trifa.HelperFiletransfer.check_if_incoming_file_was_exported;
import static com.zoffcc.applications.trifa.HelperGeneric.darkenColor;
import static com.zoffcc.applications.trifa.HelperGeneric.dp2px;
import static com.zoffcc.applications.trifa.HelperGeneric.hash_to_bucket;
import static com.zoffcc.applications.trifa.HelperGeneric.isColorDarkBrightness;
import static com.zoffcc.applications.trifa.HelperGeneric.long_date_time_format;
import static com.zoffcc.applications.trifa.HelperGroup.tox_group_peer_get_name__wrapper;
import static com.zoffcc.applications.trifa.MainActivity.PREF__compact_chatlist;
import static com.zoffcc.applications.trifa.MainActivity.PREF__global_font_size;
import static com.zoffcc.applications.trifa.MainActivity.SD_CARD_FILES_EXPORT_DIR;
import static com.zoffcc.applications.trifa.MainActivity.VFS_ENCRYPT;
import static com.zoffcc.applications.trifa.MainActivity.selected_messages;
import static com.zoffcc.applications.trifa.TRIFAGlobals.CONFERENCE_CHAT_BG_CORNER_RADIUS_IN_PX;
import static com.zoffcc.applications.trifa.TRIFAGlobals.MESSAGE_TEXT_SIZE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.MESSAGE_TEXT_SIZE_FT_NORMAL;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_SYSTEM_MESSAGE_PEER_CHATCOLOR;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_FTV2;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;

public class GroupMessageListHolder_file_incoming_state_cancel extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener
{
    private static final String TAG = "trifa.MessageListHolder";

    private GroupMessage message_;
    private Context context;

    ImageButton button_ok;
    ImageButton button_cancel;
    com.daimajia.numberprogressbar.NumberProgressBar ft_progressbar;
    ViewGroup ft_preview_container;
    ViewGroup ft_export_button_container;
    ViewGroup ft_buttons_container;
    ImageButton ft_preview_image;
    ImageButton ft_export_button;
    ImageButton ft_share_button;
    EmojiTextViewLinks textView;
    ImageView imageView;
    de.hdodenhof.circleimageview.CircleImageView img_avatar;
    TextView date_time;
    ViewGroup layout_message_container;
    ViewGroup rounded_bg_container;
    boolean is_selected = false;
    TextView message_text_date_string;
    ViewGroup message_text_date;

    public GroupMessageListHolder_file_incoming_state_cancel(View itemView, Context c)
    {
        super(itemView);
        this.context = c;

        button_ok = (ImageButton) itemView.findViewById(R.id.ft_button_ok);
        button_cancel = (ImageButton) itemView.findViewById(R.id.ft_button_cancel);
        ft_progressbar = (com.daimajia.numberprogressbar.NumberProgressBar) itemView.findViewById(R.id.ft_progressbar);
        ft_preview_container = (ViewGroup) itemView.findViewById(R.id.ft_preview_container);
        ft_buttons_container = (ViewGroup) itemView.findViewById(R.id.ft_buttons_container);
        ft_preview_image = (ImageButton) itemView.findViewById(R.id.ft_preview_image);
        rounded_bg_container = (ViewGroup) itemView.findViewById(R.id.ft_incoming_rounded_bg);
        textView = (EmojiTextViewLinks) itemView.findViewById(R.id.m_text);
        imageView = (ImageView) itemView.findViewById(R.id.m_icon);
        img_avatar = (de.hdodenhof.circleimageview.CircleImageView) itemView.findViewById(R.id.img_avatar);
        date_time = (TextView) itemView.findViewById(R.id.date_time);
        layout_message_container = (ViewGroup) itemView.findViewById(R.id.layout_message_container);
        message_text_date_string = (TextView) itemView.findViewById(R.id.message_text_date_string);
        message_text_date = (ViewGroup) itemView.findViewById(R.id.message_text_date);
        ft_export_button_container = (ViewGroup) itemView.findViewById(R.id.ft_export_button_container);
        ft_export_button = (ImageButton) itemView.findViewById(R.id.ft_export_button);
        ft_share_button = (ImageButton) itemView.findViewById(R.id.ft_share_button);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void bindMessageList(GroupMessage m)
    {
        // Log.i(TAG, "bindMessageList");

        if (m == null)
        {
            // TODO: should never be null!!
            // only afer a crash
            m = new GroupMessage();
        }

        message_ = m;

        String message__tox_peername = m.tox_group_peername;
        String message__tox_peerpubkey = m.tox_group_peer_pubkey;

        int drawable_id = R.drawable.rounded_orange_bg;
        try
        {
            final int sdk = android.os.Build.VERSION.SDK_INT;
            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN)
            {
                rounded_bg_container.setBackgroundDrawable(ContextCompat.getDrawable(context, drawable_id));
            }
            else
            {
                rounded_bg_container.setBackground(ContextCompat.getDrawable(context, drawable_id));
            }
        }
        catch (Exception e)
        {
            final int sdk = android.os.Build.VERSION.SDK_INT;
            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN)
            {
                rounded_bg_container.setBackgroundDrawable(ContextCompat.getDrawable(context, drawable_id));
            }
            else
            {
                rounded_bg_container.setBackground(ContextCompat.getDrawable(context, drawable_id));
            }
        }

        is_selected = false;

        resize_viewgroup(ft_preview_container, 150);
        resize_view(ft_preview_image, 150);

        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);

        date_time.setText(long_date_time_format(m.rcvd_timestamp));

        final GroupMessage message = m;

        button_ok.setVisibility(View.GONE);
        button_cancel.setVisibility(View.GONE);
        ft_progressbar.setVisibility(View.GONE);
        ft_buttons_container.setVisibility(View.GONE);
        textView.setVisibility(View.VISIBLE);
        imageView.setVisibility(View.VISIBLE);

        final GroupMessage message2 = message;

        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, MESSAGE_TEXT_SIZE[PREF__global_font_size]);

            textView.setAutoLinkText("" + message.text + "\n OK");
            if (MESSAGE_TEXT_SIZE[PREF__global_font_size] > MESSAGE_TEXT_SIZE_FT_NORMAL)
            {
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, MESSAGE_TEXT_SIZE_FT_NORMAL);
            }

            boolean is_image = false;
            boolean is_video = false;
            try
            {
                String mimeType = URLConnection.guessContentTypeFromName(message.filename_fullpath.toLowerCase());
                // Log.i(TAG, "mimetype=" + mimeType + " " + message.filename_fullpath.toLowerCase());
                if (mimeType.startsWith("image/"))
                {
                    is_image = true;
                }
            }
            catch (Exception e)
            {
                // e.printStackTrace();
            }

            if (is_image)
            {

                ft_preview_image.setImageResource(R.drawable.round_loading_animation);

                if (PREF__compact_chatlist)
                {
                    textView.setVisibility(View.GONE);
                    imageView.setVisibility(View.GONE);
                }
                else
                {
                    textView.setVisibility(View.VISIBLE);
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, MESSAGE_TEXT_SIZE[PREF__global_font_size]);
                }

                if (VFS_ENCRYPT)
                {
                    ft_preview_image.setOnTouchListener(new View.OnTouchListener()
                    {
                        @Override
                        public boolean onTouch(View v, MotionEvent event)
                        {
                            if (event.getAction() == MotionEvent.ACTION_UP)
                            {
                                try
                                {
                                    Intent intent = new Intent(v.getContext(), ImageviewerActivity.class);
                                    intent.putExtra("image_filename", message2.filename_fullpath);
                                    v.getContext().startActivity(intent);
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                    Log.i(TAG, "open_attachment_intent:EE:" + e.getMessage());
                                }
                            }
                            else
                            {
                            }
                            return true;
                        }
                    });


                    info.guardianproject.iocipher.File f2 = new info.guardianproject.iocipher.File(
                            message2.filename_fullpath);
                    try
                    {
                        // Log.i(TAG, "glide:img:001");

                        final RequestOptions glide_options = new RequestOptions().fitCenter().optionalTransform(
                                new RoundedCorners((int) dp2px(20)));
                        // apply(glide_options).

                        // loadImageFromUri(context, Uri.fromFile(new File(message2.filename_fullpath)), ft_preview_image,
                        //                  true);
                        GlideApp.
                                with(context).
                                load(f2).
                                diskCacheStrategy(DiskCacheStrategy.RESOURCE).
                                skipMemoryCache(false).
                                priority(Priority.LOW).
                                placeholder(R.drawable.round_loading_animation).
                                into(ft_preview_image);
                        // Log.i(TAG, "glide:img:002");

                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            else if (is_video)  // ---- a video ----
            {
            }
            else // ---- not an image or a video ----
            {
            }

            ft_export_button_container.setVisibility(View.VISIBLE);
            ft_export_button.setVisibility(View.GONE);
            ft_share_button.setVisibility(View.GONE);

            ft_preview_container.setVisibility(View.VISIBLE);
            ft_preview_image.setVisibility(View.VISIBLE);


        try
        {
            String peer_name = tox_group_peer_get_name__wrapper(m.group_identifier, message__tox_peerpubkey);

            if (peer_name == null)
            {
                peer_name = message__tox_peername;

                if ((peer_name == null) || (message__tox_peername.equals("")) || (peer_name.equals("-1")))
                {
                    peer_name = "Unknown";
                }
            }
            else
            {
                if (peer_name.equals("-1"))
                {
                    if ((message__tox_peername == null) || (message__tox_peername.equals("")))
                    {
                        peer_name = "Unknown";
                    }
                    else
                    {
                        peer_name = message__tox_peername;
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "bindMessageList:EE:" + e.getMessage());
        }


            textView.setTextColor(Color.BLACK);

        int peer_color_fg = context.getResources().getColor(R.color.colorPrimaryDark);
        int peer_color_bg = context.getResources().getColor(R.color.material_drawer_background);

        try
        {
            if (message__tox_peerpubkey.compareTo("-1") == 0)
            {
                peer_color_bg = TRIFA_SYSTEM_MESSAGE_PEER_CHATCOLOR;
            }
            else
            {
                peer_color_bg = ChatColors.get_shade(
                        ChatColors.PeerAvatarColors[hash_to_bucket(message__tox_peerpubkey, ChatColors.get_size())],
                        message__tox_peerpubkey);
            }
            // peer_color_bg_with_alpha = (peer_color_bg & 0x00FFFFFF) | (alpha_value << 24);
            textView.setTextColor(Color.BLACK);

            if (isColorDarkBrightness(peer_color_bg))
            {
                textView.setTextColor(darkenColor(Color.WHITE, 0.1f));
                //
                final int linkcolor_for_other_bg = darkenColor(Color.YELLOW, 0.1f);
                textView.setMentionModeColor(linkcolor_for_other_bg);
                textView.setHashtagModeColor(linkcolor_for_other_bg);
                textView.setUrlModeColor(linkcolor_for_other_bg);
                textView.setPhoneModeColor(linkcolor_for_other_bg);
                textView.setEmailModeColor(linkcolor_for_other_bg);
                textView.setCustomModeColor(linkcolor_for_other_bg);
                textView.setLinkTextColor(linkcolor_for_other_bg);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


        // we need to do the rounded corner background manually here, to change the color ---------------
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadii(
                new float[]{CONFERENCE_CHAT_BG_CORNER_RADIUS_IN_PX, CONFERENCE_CHAT_BG_CORNER_RADIUS_IN_PX, CONFERENCE_CHAT_BG_CORNER_RADIUS_IN_PX, CONFERENCE_CHAT_BG_CORNER_RADIUS_IN_PX, CONFERENCE_CHAT_BG_CORNER_RADIUS_IN_PX, CONFERENCE_CHAT_BG_CORNER_RADIUS_IN_PX, CONFERENCE_CHAT_BG_CORNER_RADIUS_IN_PX, CONFERENCE_CHAT_BG_CORNER_RADIUS_IN_PX});
        shape.setColor(peer_color_bg);
        // shape.setStroke(3, borderColor);
        rounded_bg_container.setBackground(shape);
        // we need to do the rounded corner background manually here, to change the color ---------------

        final Drawable smiley_face = new IconicsDrawable(context).
                icon(GoogleMaterial.Icon.gmd_sentiment_satisfied).
                backgroundColor(peer_color_bg).
                color(peer_color_fg).sizeDp(70);

        date_time.setVisibility(View.VISIBLE);
        img_avatar.setVisibility(View.VISIBLE);

        message_text_date.setVisibility(View.GONE);

        img_avatar.setImageDrawable(smiley_face);
        HelperGeneric.set_avatar_img_height_in_chat(img_avatar);
    }

    @Override
    public void onClick(View v)
    {
        //  Log.i(TAG, "onClick");
    }

    @Override
    public boolean onLongClick(final View v)
    {
        // Log.i(TAG, "onLongClick");
        return true;
    }

    private void resize_viewgroup(ViewGroup vg, int height_in_dp)
    {
        try
        {
            float pixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, height_in_dp,
                                                     vg.getResources().getDisplayMetrics());
            LinearLayout.LayoutParams params3 = (LinearLayout.LayoutParams) vg.getLayoutParams();
            if (params3.height != (int) pixels)
            {
                params3.height = (int) pixels;
                vg.setLayoutParams(params3);
            }
        }
        catch (Exception e)
        {
        }
    }

    private void resize_view(View vg, int height_in_dp)
    {
        try
        {
            float pixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, height_in_dp,
                                                     vg.getResources().getDisplayMetrics());
            LinearLayout.LayoutParams params3 = (LinearLayout.LayoutParams) vg.getLayoutParams();
            if (params3.height != (int) pixels)
            {
                params3.height = (int) pixels;
                vg.setLayoutParams(params3);
            }
        }
        catch (Exception e)
        {
        }
    }
}
