package com.zoffcc.applications.trifa;

import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.zoffcc.applications.sorm.GroupMessage;
import com.zoffcc.applications.sorm.Message;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_FILE;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;

public class ImageviewerActivity extends AppCompatActivity
{
    private static final String TAG = "trifa.ImageviewerActy";

    // Class-level variables to access them in the share button listener
    private List<Message_for_img> imageMessages = new ArrayList<>();
    private LinearLayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imageviewer_swipe);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        ImageButton btnShare = findViewById(R.id.btn_share);

        String image_filename = null;
        String friend_pubkey = null;
        String group_identifier = null;
        boolean storage_frame_work = false;

        try
        {
            image_filename = getIntent().getStringExtra("image_filename");
            friend_pubkey = getIntent().getStringExtra("friend_pubkey");
            group_identifier = getIntent().getStringExtra("group_identifier");

            String temp = getIntent().getStringExtra("storage_frame_work");
            if (temp != null && temp.equals("1")) {
                storage_frame_work = true;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if (image_filename == null || orma == null) {
            finish();
            return;
        }

        int currentImageIndex = -1;

        try {
            if (group_identifier != null && !group_identifier.isEmpty()) {
                // It's a Group Chat
                List<GroupMessage> allGroupMessages = orma.selectFromGroupMessage()
                        .group_identifierEq(group_identifier)
                        .TRIFA_MESSAGE_TYPEEq(TRIFA_MSG_FILE.value)
                        .orderBySent_timestampAsc()
                        .toList();

                for (int i = 0; i < allGroupMessages.size(); i++) {
                    GroupMessage gm = allGroupMessages.get(i);
                    if (gm.filename_fullpath != null) {
                        boolean is_image = false;
                        try {
                            String mimeType = URLConnection.guessContentTypeFromName(gm.filename_fullpath.toLowerCase());
                            if (mimeType != null && mimeType.startsWith("image/")) {
                                is_image = true;
                            }
                        } catch (Exception e) {
                            // Ignore
                        }

                        if (is_image) {
                            Message_for_img m = new Message_for_img();
                            m.filename_fullpath = gm.filename_fullpath;
                            m.id = gm.id;
                            m.sent_timestamp = gm.sent_timestamp;
                            m.sent_timestamp_ms = 0;
                            m.direction = gm.direction;

                            // Check if it's an outgoing SAF file
                            if (gm.direction == TRIFAGlobals.TRIFA_FT_DIRECTION.TRIFA_FT_DIRECTION_OUTGOING.value) {
                                if (gm.filename_fullpath.contains(MainActivity.SD_CARD_FILES_OUTGOING_WRAPPER_DIR)) {
                                    m.storage_frame_work = true;
                                    m.setfile_uri(gm.filename_fullpath);
                                }
                            }

                            imageMessages.add(m);
                            if (gm.filename_fullpath.equals(image_filename)) {
                                currentImageIndex = imageMessages.size() - 1;
                            }
                        }
                    }
                }

            } else if (friend_pubkey != null && !friend_pubkey.isEmpty()) {
                // It's a Friend Chat
                List<Message> allMessages = orma.selectFromMessage()
                        .tox_friendpubkeyEq(friend_pubkey)
                        .TRIFA_MESSAGE_TYPEEq(TRIFA_MSG_FILE.value)
                        .orderBySent_timestampAsc()
                        .orderBySent_timestamp_msAsc()
                        .toList();

                for (int i = 0; i < allMessages.size(); i++) {
                    Message m = allMessages.get(i);
                    if (m.filename_fullpath != null) {
                        boolean is_image = false;
                        try {
                            String mimeType = URLConnection.guessContentTypeFromName(m.filename_fullpath.toLowerCase());
                            if (mimeType != null && mimeType.startsWith("image/")) {
                                is_image = true;
                            }
                        } catch (Exception e) {
                            // Ignore
                        }

                        if (is_image) {
                            Message_for_img m2 = new Message_for_img();
                            m2.filename_fullpath = m.filename_fullpath;
                            m2.id = m.id;
                            m2.sent_timestamp = m.sent_timestamp;
                            m2.sent_timestamp_ms = m.sent_timestamp_ms;
                            m2.direction = m.direction;
                            m2.storage_frame_work = m.storage_frame_work;

                            if (m.storage_frame_work) {
                                m2.setfile_uri(m.filename_fullpath);
                            }

                            imageMessages.add(m2);
                            if (m.filename_fullpath.equals(image_filename)) {
                                currentImageIndex = imageMessages.size() - 1;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Fallback: If no images were found in DB but we have a filename, show just that one
        if (imageMessages.isEmpty()) {
            Message_for_img singleImg = new Message_for_img();
            singleImg.filename_fullpath = image_filename;
            singleImg.storage_frame_work = storage_frame_work;
            if (storage_frame_work) {
                singleImg.setfile_uri(image_filename);
            }
            imageMessages.add(singleImg);
            currentImageIndex = 0;
        }

        if (currentImageIndex == -1) {
            currentImageIndex = 0;
        }

        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);

        ImageViewerPagerAdapter adapter = new ImageViewerPagerAdapter(this, imageMessages);
        recyclerView.setAdapter(adapter);

        layoutManager.scrollToPositionWithOffset(currentImageIndex, 0);

        // --- Share Button Logic ---
        btnShare.setOnClickListener(v -> {
            int currentPosition = layoutManager.findFirstCompletelyVisibleItemPosition();
            if (currentPosition == RecyclerView.NO_POSITION) {
                currentPosition = layoutManager.findFirstVisibleItemPosition();
            }
            if (currentPosition >= 0 && currentPosition < imageMessages.size()) {
                Message_for_img msg = imageMessages.get(currentPosition);
                shareImage(msg);
            }
        });
    }

    private void shareImage(Message_for_img msg) {
        try {
            android.content.Intent shareIntent = new android.content.Intent(android.content.Intent.ACTION_SEND);

            android.net.Uri shareUri = null;
            String mimeType = "image/*"; // Default fallback

            if (msg.direction == TRIFAGlobals.TRIFA_FT_DIRECTION.TRIFA_FT_DIRECTION_OUTGOING.value) {
                if (msg.storage_frame_work && msg.getfile_uri() != null) {
                    // Outgoing file via Storage Access Framework (SAF)
                    shareUri = android.net.Uri.parse(msg.getfile_uri());
                } else if (msg.filename_fullpath != null) {
                    // Outgoing file stored on local disk
                    java.io.File file = new java.io.File(msg.filename_fullpath);
                    if (file.exists()) {
                        shareUri = androidx.core.content.FileProvider.getUriForFile(
                                this,
                                "com.zoffcc.applications.trifa.ext2_provider",
                                file
                        );

                        // Robust MIME type detection (more reliable than getFileExtensionFromUrl)
                        String fileName = file.getName();
                        int lastDotIndex = fileName.lastIndexOf('.');
                        if (lastDotIndex > 0) {
                            String ext = fileName.substring(lastDotIndex + 1).toLowerCase();
                            android.webkit.MimeTypeMap myMime = android.webkit.MimeTypeMap.getSingleton();
                            String detectedMimeType = myMime.getMimeTypeFromExtension(ext);
                            if (detectedMimeType != null) {
                                mimeType = detectedMimeType;
                            }
                        }
                    }
                }
            } else {
                // Incoming file stored in encrypted VFS
                if (msg.filename_fullpath != null) {
                    shareUri = android.net.Uri.parse(IOCipherContentProvider.FILES_URI + msg.filename_fullpath);

                    // MIME type detection for VFS
                    String fileName = msg.filename_fullpath;
                    int lastDotIndex = fileName.lastIndexOf('.');
                    if (lastDotIndex > 0) {
                        String ext = fileName.substring(lastDotIndex + 1).toLowerCase();
                        android.webkit.MimeTypeMap myMime = android.webkit.MimeTypeMap.getSingleton();
                        String detectedMimeType = myMime.getMimeTypeFromExtension(ext);
                        if (detectedMimeType != null) {
                            mimeType = detectedMimeType;
                        }
                    }
                }
            }

            if (shareUri != null) {
                shareIntent.setType(mimeType);
                shareIntent.putExtra(android.content.Intent.EXTRA_STREAM, shareUri);

                // FIX for Share Sheet Previews:
                // Explicitly set ClipData so the system UI can read the URI to generate the thumbnail
                shareIntent.setClipData(android.content.ClipData.newRawUri("", shareUri));

                // Grant read permission to the receiving app
                shareIntent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);

                // Also grant permission to the chooser itself to ensure smooth handoff
                android.content.Intent chooser = android.content.Intent.createChooser(shareIntent, "Share Image");
                chooser.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);

                startActivity(chooser);
            } else {
                android.widget.Toast.makeText(this, "Cannot share: File not found or inaccessible", android.widget.Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            android.widget.Toast.makeText(this, "Error sharing image: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
        }
    }
}
