/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2026 Zoff <zoff@zoff.cc>
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

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.zoffcc.applications.sorm.GroupMessage;
import com.zoffcc.applications.sorm.Message;

import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_FILE;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;

/** @noinspection ExtractMethodRecommender*/
public class ImageviewerActivity extends AppCompatActivity
{
    private static final String TAG = "trifa.ImageviewerActy";

    private List<Message_for_img> imageMessages = new ArrayList<>();
    private LinearLayoutManager layoutManager;

    // Bottom info views
    private TextView tvFilename;
    private TextView tvDatetime;
    private ImageButton btnInfo;

    // Metadata for current chat context (used in detail dialog)
    private String currentFriendPubkey;
    private String currentGroupIdentifier;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imageviewer_swipe);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        ImageButton btnShare = findViewById(R.id.btn_share);

        // Initialize bottom info views
        tvFilename = findViewById(R.id.tv_filename);
        tvDatetime = findViewById(R.id.tv_datetime);
        btnInfo = findViewById(R.id.btn_info);

        String image_filename = null;
        String friend_pubkey = null;
        String group_identifier = null;
        String file_uri_extra = null;
        boolean storage_frame_work = false;

        try
        {
            image_filename = getIntent().getStringExtra("image_filename");
            friend_pubkey = getIntent().getStringExtra("friend_pubkey");
            group_identifier = getIntent().getStringExtra("group_identifier");
            file_uri_extra = getIntent().getStringExtra("file_uri");

            String temp = getIntent().getStringExtra("storage_frame_work");
            if (temp != null && temp.equals("1"))
            {
                storage_frame_work = true;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if (image_filename == null || orma == null)
        {
            finish();
            return;
        }

        // Store chat context for detail popup
        currentFriendPubkey = friend_pubkey;
        currentGroupIdentifier = group_identifier;

        int currentImageIndex = -1;

        try
        {
            if (group_identifier != null && !group_identifier.isEmpty())
            {
                // It's a Group Chat
                List<GroupMessage> allGroupMessages = orma.selectFromGroupMessage()
                        .group_identifierEq(group_identifier)
                        .TRIFA_MESSAGE_TYPEEq(TRIFA_MSG_FILE.value)
                        .orderBySent_timestampAsc()
                        .toList();

                for (int i = 0; i < allGroupMessages.size(); i++)
                {
                    GroupMessage gm = allGroupMessages.get(i);
                    if (gm.filename_fullpath != null && isImage(gm.filename_fullpath))
                    {
                        Message_for_img m = new Message_for_img();
                        m.filename_fullpath = gm.filename_fullpath;
                        m.id = gm.id;
                        m.sent_timestamp = gm.sent_timestamp;
                        m.sent_timestamp_ms = 0;
                        m.direction = gm.direction;

                        boolean is_uri = gm.filename_fullpath.startsWith("content://")
                                         || gm.filename_fullpath.startsWith("file://");
                        if (is_uri)
                        {
                            m.setfile_uri(gm.filename_fullpath);
                            if (gm.direction == TRIFAGlobals.TRIFA_FT_DIRECTION.TRIFA_FT_DIRECTION_OUTGOING.value)
                            {
                                m.storage_frame_work = true;
                            }
                        }
                        else if (gm.direction == TRIFAGlobals.TRIFA_FT_DIRECTION.TRIFA_FT_DIRECTION_OUTGOING.value)
                        {
                            if (gm.filename_fullpath.contains(MainActivity.SD_CARD_FILES_OUTGOING_WRAPPER_DIR))
                            {
                                m.storage_frame_work = true;
                                m.setfile_uri(gm.filename_fullpath);
                            }
                        }

                        imageMessages.add(m);
                        if (gm.filename_fullpath.equals(image_filename))
                        {
                            currentImageIndex = imageMessages.size() - 1;
                        }
                    }
                }
            }
            else if (friend_pubkey != null && !friend_pubkey.isEmpty())
            {
                // It's a Friend Chat
                List<Message> allMessages = orma.selectFromMessage()
                        .tox_friendpubkeyEq(friend_pubkey)
                        .TRIFA_MESSAGE_TYPEEq(TRIFA_MSG_FILE.value)
                        .orderBySent_timestampAsc()
                        .orderBySent_timestamp_msAsc()
                        .toList();

                for (int i = 0; i < allMessages.size(); i++)
                {
                    Message msg = allMessages.get(i);
                    if (msg.filename_fullpath != null && isImage(msg.filename_fullpath))
                    {
                        Message_for_img m2 = new Message_for_img();
                        m2.filename_fullpath = msg.filename_fullpath;
                        m2.id = msg.id;
                        m2.sent_timestamp = msg.sent_timestamp;
                        m2.sent_timestamp_ms = msg.sent_timestamp_ms;
                        m2.direction = msg.direction;
                        m2.storage_frame_work = msg.storage_frame_work;

                        boolean is_uri = msg.filename_fullpath.startsWith("content://")
                                         || msg.filename_fullpath.startsWith("file://");
                        if (is_uri)
                        {
                            m2.setfile_uri(msg.filename_fullpath);
                            if (msg.direction == TRIFAGlobals.TRIFA_FT_DIRECTION.TRIFA_FT_DIRECTION_OUTGOING.value)
                            {
                                m2.storage_frame_work = true;
                            }
                        }
                        else if (msg.storage_frame_work)
                        {
                            m2.setfile_uri(msg.filename_fullpath);
                        }

                        imageMessages.add(m2);
                        if (msg.filename_fullpath.equals(image_filename))
                        {
                            currentImageIndex = imageMessages.size() - 1;
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        // Fallback: If no images were found in DB but we have a filename, show just that one
        if (imageMessages.isEmpty())
        {
            Message_for_img singleImg = new Message_for_img();
            singleImg.filename_fullpath = image_filename;
            singleImg.storage_frame_work = storage_frame_work;

            if (storage_frame_work || (file_uri_extra != null && !file_uri_extra.isEmpty()))
            {
                singleImg.setfile_uri(file_uri_extra != null ? file_uri_extra : image_filename);
                singleImg.storage_frame_work = true;
            }

            if (image_filename.startsWith("content://"))
            {
                singleImg.direction = TRIFAGlobals.TRIFA_FT_DIRECTION.TRIFA_FT_DIRECTION_OUTGOING.value;
            }

            imageMessages.add(singleImg);
            currentImageIndex = 0;
        }

        if (currentImageIndex == -1)
        {
            currentImageIndex = 0;
        }

        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setClipChildren(false);
        recyclerView.setClipToPadding(false);

        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);

        ImageViewerPagerAdapter adapter = new ImageViewerPagerAdapter(this, imageMessages);
        recyclerView.setAdapter(adapter);

        layoutManager.scrollToPositionWithOffset(currentImageIndex, 0);

        // Show info for initial image
        updateBottomInfo(currentImageIndex);

        // Update info when user swipes to a different image
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState)
            {
                if (newState == RecyclerView.SCROLL_STATE_IDLE)
                {
                    int pos = layoutManager.findFirstCompletelyVisibleItemPosition();
                    if (pos == RecyclerView.NO_POSITION)
                    {
                        pos = layoutManager.findFirstVisibleItemPosition();
                    }
                    if (pos != RecyclerView.NO_POSITION)
                    {
                        updateBottomInfo(pos);
                    }
                }
            }
        });

        // --- Share Button Logic ---
        btnShare.setOnClickListener(v ->
                                    {
                                        int currentPosition = layoutManager.findFirstCompletelyVisibleItemPosition();
                                        if (currentPosition == RecyclerView.NO_POSITION)
                                        {
                                            currentPosition = layoutManager.findFirstVisibleItemPosition();
                                        }
                                        if (currentPosition >= 0 && currentPosition < imageMessages.size())
                                        {
                                            Message_for_img msg = imageMessages.get(currentPosition);
                                            shareImage(msg);
                                        }
                                    });

        // --- Info Button Logic ---
        btnInfo.setOnClickListener(v ->
                                   {
                                       int currentPosition = layoutManager.findFirstCompletelyVisibleItemPosition();
                                       if (currentPosition == RecyclerView.NO_POSITION)
                                       {
                                           currentPosition = layoutManager.findFirstVisibleItemPosition();
                                       }
                                       if (currentPosition >= 0 && currentPosition < imageMessages.size())
                                       {
                                           showImageDetailDialog(imageMessages.get(currentPosition));
                                       }
                                   });
    }

    /**
     * Updates the bottom filename and date/time display.
     * Called on initial load and after each swipe settles.
     */
    private void updateBottomInfo(int position)
    {
        if (position < 0 || position >= imageMessages.size())
        {
            tvFilename.setText("");
            tvDatetime.setText("");
            return;
        }

        Message_for_img msg = imageMessages.get(position);

        // Filename: extract just the name from full path
        String displayName = msg.filename_fullpath;
        if (displayName != null)
        {
            int lastSep = Math.max(displayName.lastIndexOf('/'), displayName.lastIndexOf('\\'));
            if (lastSep >= 0 && lastSep < displayName.length() - 1)
            {
                displayName = displayName.substring(lastSep + 1);
            }
        }
        else
        {
            displayName = "";
        }
        tvFilename.setText(displayName);

        // Date/time: sent_timestamp is already in milliseconds
        long timestampMs = msg.sent_timestamp;
        if (timestampMs > 0)
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            tvDatetime.setText(sdf.format(new Date(timestampMs)));
        }
        else
        {
            tvDatetime.setText("");
        }
    }

    /**
     * Shows a detail dialog with all available metadata for the current image.
     * Uses a custom ScrollView + TextView for reliable scrolling and text selection.
     */
    private void showImageDetailDialog(Message_for_img msg)
    {
        StringBuilder sb = new StringBuilder();

        // Direction
        boolean isOutgoing = msg.direction == TRIFAGlobals.TRIFA_FT_DIRECTION.TRIFA_FT_DIRECTION_OUTGOING.value;
        sb.append("Direction: ").append(isOutgoing ? "Outgoing" : "Incoming").append("\n\n");

        // Full path
        sb.append("Path:\n").append(msg.filename_fullpath != null ? msg.filename_fullpath : "N/A").append("\n\n");

        // File URI (if different from path)
        String uri = msg.getfile_uri();
        if (uri != null && !uri.isEmpty() && !uri.equals(msg.filename_fullpath))
        {
            sb.append("URI:\n").append(uri).append("\n\n");
        }

        // Storage framework flag
        //* this value is not correct *// sb.append("Storage Framework: ").append(msg.storage_frame_work ? "Yes" : "No").append("\n\n");

        // Timestamps
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        if (msg.sent_timestamp > 0)
        {
            sb.append("Sent: ").append(sdf.format(new Date(msg.sent_timestamp))).append("\n");
        }
        sb.append("\n");

        // Message ID
        sb.append("Message ID: ").append(msg.id).append("\n\n");

        // Chat context
        if (currentGroupIdentifier != null && !currentGroupIdentifier.isEmpty())
        {
            sb.append("Group ID:\n").append(currentGroupIdentifier).append("\n");
        }
        else if (currentFriendPubkey != null && !currentFriendPubkey.isEmpty())
        {
            sb.append("Friend Pubkey:\n").append(currentFriendPubkey).append("\n");
        }

        // Extract display name for dialog title
        String displayName = msg.filename_fullpath;
        if (displayName != null)
        {
            int lastSep = Math.max(displayName.lastIndexOf('/'), displayName.lastIndexOf('\\'));
            if (lastSep >= 0 && lastSep < displayName.length() - 1)
            {
                displayName = displayName.substring(lastSep + 1);
            }
        }

        // Build custom scrollable, selectable content view
        android.widget.ScrollView scrollView = new android.widget.ScrollView(this);
        scrollView.setPadding(
                (int) (10 * getResources().getDisplayMetrics().density),
                (int) (8 * getResources().getDisplayMetrics().density),
                (int) (10 * getResources().getDisplayMetrics().density),
                0
        );
        scrollView.setFillViewport(true);

        TextView contentView = new TextView(this);
        contentView.setText(sb.toString());
        contentView.setTextSize(13);

        // Use theme-resolved text color instead of hardcoded light/dark
        android.util.TypedValue typedValue = new android.util.TypedValue();
        getTheme().resolveAttribute(android.R.attr.textColorPrimary, typedValue, true);
        contentView.setTextColor(typedValue.data);

        contentView.setTextIsSelectable(true);
        contentView.setAutoLinkMask(android.text.util.Linkify.ALL);
        contentView.setTypeface(android.graphics.Typeface.MONOSPACE);

        scrollView.addView(contentView);

        new AlertDialog.Builder(this)
                .setTitle(displayName != null ? displayName : "File Details")
                .setView(scrollView)
                .setPositiveButton("OK", null)
                .setNeutralButton("Copy All", (dialog, which) ->
                {
                    android.content.ClipboardManager clipboard =
                            (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    clipboard.setPrimaryClip(android.content.ClipData.newPlainText("file_info", sb.toString()));
                    android.widget.Toast.makeText(this, "Copied to clipboard",
                                                  android.widget.Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private boolean isImage(String path)
    {
        if (path == null) return false;
        try
        {
            String lower = path.toLowerCase(Locale.US);

            // Fast extension check (helps with URIs that have extensions at the end)
            if (lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg")
                || lower.endsWith(".gif") || lower.endsWith(".bmp") || lower.endsWith(".webp")
                || lower.endsWith(".heic") || lower.endsWith(".heif"))
            {
                return true;
            }

            // MIME type from name
            String mimeType = URLConnection.guessContentTypeFromName(lower);
            if (mimeType != null && mimeType.startsWith("image/"))
            {
                return true;
            }

            // Content resolver for URIs (Crucial for SAF files)
            if (lower.startsWith("content://"))
            {
                try
                {
                    String type = getContentResolver().getType(Uri.parse(path));
                    if (type != null && type.startsWith("image/"))
                    {
                        return true;
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
        return false;
    }

    private void shareImage(Message_for_img msg)
    {
        try
        {
            android.content.Intent shareIntent = new android.content.Intent(android.content.Intent.ACTION_SEND);
            android.net.Uri shareUri = null;
            String mimeType = "image/*";

            String path = msg.filename_fullpath;
            String uriStr = msg.getfile_uri();
            if (uriStr == null || uriStr.isEmpty()) uriStr = path;

            // Strictly check if the strings are actual URI schemes
            boolean is_uri = uriStr != null
                             && (uriStr.startsWith("content://") || uriStr.startsWith("file://"));
            boolean path_is_uri = path != null
                                  && (path.startsWith("content://") || path.startsWith("file://"));

            if (msg.direction == TRIFAGlobals.TRIFA_FT_DIRECTION.TRIFA_FT_DIRECTION_OUTGOING.value)
            {
                if (is_uri || path_is_uri)
                {
                    // It's a SAF / content URI. Do NOT wrap in java.io.File!
                    String actualUriStr = is_uri ? uriStr : path;
                    shareUri = android.net.Uri.parse(actualUriStr);

                    // Try to get MIME type from ContentResolver
                    try
                    {
                        String type = getContentResolver().getType(shareUri);
                        if (type != null) mimeType = type;
                    }
                    catch (Exception ignored)
                    {
                    }
                }
                else if (path != null)
                {
                    // It's a real local file path
                    java.io.File file = new java.io.File(path);
                    if (file.exists())
                    {
                        shareUri = androidx.core.content.FileProvider.getUriForFile(
                                this,
                                "com.zoffcc.applications.trifa.ext2_provider",
                                file
                        );

                        // MIME type detection for local file
                        String fileName = file.getName();
                        int lastDotIndex = fileName.lastIndexOf('.');
                        if (lastDotIndex > 0)
                        {
                            String ext = fileName.substring(lastDotIndex + 1).toLowerCase();
                            android.webkit.MimeTypeMap myMime = android.webkit.MimeTypeMap.getSingleton();
                            String detectedMimeType = myMime.getMimeTypeFromExtension(ext);
                            if (detectedMimeType != null)
                            {
                                mimeType = detectedMimeType;
                            }
                        }
                    }
                }
            }
            else
            {
                // Incoming file (VFS)
                if (path != null && !path_is_uri)
                {
                    shareUri = android.net.Uri.parse(IOCipherContentProvider.FILES_URI + path);

                    // MIME type detection for VFS
                    String fileName = path;
                    int lastDotIndex = fileName.lastIndexOf('.');
                    if (lastDotIndex > 0)
                    {
                        String ext = fileName.substring(lastDotIndex + 1).toLowerCase();
                        android.webkit.MimeTypeMap myMime = android.webkit.MimeTypeMap.getSingleton();
                        String detectedMimeType = myMime.getMimeTypeFromExtension(ext);
                        if (detectedMimeType != null)
                        {
                            mimeType = detectedMimeType;
                        }
                    }
                }
                else if (path_is_uri)
                {
                    // Rare case: incoming URI
                    shareUri = android.net.Uri.parse(path);
                    try
                    {
                        String type = getContentResolver().getType(shareUri);
                        if (type != null) mimeType = type;
                    }
                    catch (Exception ignored)
                    {
                    }
                }
            }

            if (shareUri != null)
            {
                shareIntent.setType(mimeType);
                shareIntent.putExtra(android.content.Intent.EXTRA_STREAM, shareUri);

                // Explicitly set ClipData so system UI can read the URI
                shareIntent.setClipData(android.content.ClipData.newRawUri("", shareUri));

                // Grant read permission to the receiving app
                shareIntent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);

                // Also grant permission to the chooser itself to ensure smooth handoff
                android.content.Intent chooser = android.content.Intent.createChooser(shareIntent, "Share Image");
                chooser.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);

                startActivity(chooser);
            }
            else
            {
                android.widget.Toast.makeText(this, "Cannot share: File not found or inaccessible",
                                              android.widget.Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            android.widget.Toast.makeText(this, "Error sharing image: " + e.getMessage(),
                                          android.widget.Toast.LENGTH_SHORT).show();
        }
    }
}