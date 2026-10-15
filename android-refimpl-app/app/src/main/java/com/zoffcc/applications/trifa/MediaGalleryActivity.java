package com.zoffcc.applications.trifa;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.l4digital.fastscroll.FastScrollRecyclerView;
import com.zoffcc.applications.sorm.GroupMessage;
import com.zoffcc.applications.sorm.Message;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_FILE;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;

public class MediaGalleryActivity extends AppCompatActivity {

    private static final String TAG = "trifa.MediaGalleryActy";
    private List<Message_for_img> allImages = new ArrayList<>();
    private MediaGalleryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_gallery);

        FastScrollRecyclerView recyclerView = findViewById(R.id.gallery_recycler_view);

        // 3 columns for the grid
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(layoutManager);

        String friend_pubkey = getIntent().getStringExtra("friend_pubkey");
        String group_identifier = getIntent().getStringExtra("group_identifier");

        if (orma == null) {
            finish();
            return;
        }

        loadImages(friend_pubkey, group_identifier);

        // Sort by newest first (descending timestamp)
        Collections.sort(allImages, new Comparator<Message_for_img>() {
            @Override
            public int compare(Message_for_img m1, Message_for_img m2) {
                long t1 = m1.sent_timestamp;
                long t2 = m2.sent_timestamp;
                // If timestamps are equal, use ms if available or id
                if (t1 == t2) {
                    return Long.compare(m2.id, m1.id);
                }
                return Long.compare(t2, t1); // Descending
            }
        });

        adapter = new MediaGalleryAdapter(this, allImages);
        recyclerView.setAdapter(adapter);
    }

    private void loadImages(String friend_pubkey, String group_identifier) {
        allImages.clear();
        try {
            if (group_identifier != null && !group_identifier.isEmpty()) {
                List<GroupMessage> groupMsgs = orma.selectFromGroupMessage()
                        .group_identifierEq(group_identifier)
                        .TRIFA_MESSAGE_TYPEEq(TRIFA_MSG_FILE.value)
                        .orderBySent_timestampDesc()
                        .toList();

                for (GroupMessage gm : groupMsgs) {
                    if (gm.filename_fullpath != null && isImage(gm.filename_fullpath)) {
                        Message_for_img m = new Message_for_img();
                        m.filename_fullpath = gm.filename_fullpath;
                        m.id = gm.id;
                        m.sent_timestamp = gm.sent_timestamp;
                        m.sent_timestamp_ms = 0;
                        m.direction = gm.direction;
                        if (gm.direction == TRIFAGlobals.TRIFA_FT_DIRECTION.TRIFA_FT_DIRECTION_OUTGOING.value) {
                            if (gm.filename_fullpath.contains(MainActivity.SD_CARD_FILES_OUTGOING_WRAPPER_DIR)) {
                                m.storage_frame_work = true;
                                m.setfile_uri(gm.filename_fullpath);
                            }
                        }
                        allImages.add(m);
                    }
                }
            } else if (friend_pubkey != null && !friend_pubkey.isEmpty()) {
                List<Message> friendMsgs = orma.selectFromMessage()
                        .tox_friendpubkeyEq(friend_pubkey)
                        .TRIFA_MESSAGE_TYPEEq(TRIFA_MSG_FILE.value)
                        .orderBySent_timestampDesc()
                        .orderBySent_timestamp_msDesc()
                        .toList();

                for (Message m_orig : friendMsgs) {
                    if (m_orig.filename_fullpath != null && isImage(m_orig.filename_fullpath)) {
                        Message_for_img m = new Message_for_img();
                        m.filename_fullpath = m_orig.filename_fullpath;
                        m.id = m_orig.id;
                        m.sent_timestamp = m_orig.sent_timestamp;
                        m.sent_timestamp_ms = m_orig.sent_timestamp_ms;
                        m.direction = m_orig.direction;
                        m.storage_frame_work = m_orig.storage_frame_work;

                        if (m.storage_frame_work) {
                            m.setfile_uri(m_orig.filename_fullpath);
                        }

                        allImages.add(m);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isImage(String path) {
        try {
            String mimeType = URLConnection.guessContentTypeFromName(path.toLowerCase());
            return mimeType != null && mimeType.startsWith("image/");
        } catch (Exception e) {
            return false;
        }
    }

    public void openImageInViewer(Message_for_img msg) {
        Intent intent = new Intent(this, ImageviewerActivity.class);
        intent.putExtra("image_filename", msg.filename_fullpath);

        if (msg.direction == TRIFAGlobals.TRIFA_FT_DIRECTION.TRIFA_FT_DIRECTION_OUTGOING.value) {
            if (msg.storage_frame_work) {
                intent.putExtra("storage_frame_work", "1");
            }
        }

        String friend_pubkey = getIntent().getStringExtra("friend_pubkey");
        String group_identifier = getIntent().getStringExtra("group_identifier");

        if (friend_pubkey != null) intent.putExtra("friend_pubkey", friend_pubkey);
        if (group_identifier != null) intent.putExtra("group_identifier", group_identifier);

        startActivity(intent);
    }
}