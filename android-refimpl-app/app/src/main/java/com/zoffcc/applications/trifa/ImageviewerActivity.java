package com.zoffcc.applications.trifa;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.zoffcc.applications.sorm.GroupMessage;
import com.zoffcc.applications.sorm.Message;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_FILE;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;

public class ImageviewerActivity extends AppCompatActivity
{
    private static final String TAG = "trifa.ImageviewerActy";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imageviewer_swipe);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);

        String image_filename = null;
        String friend_pubkey = null;
        String group_identifier = null;

        try
        {
            image_filename = getIntent().getStringExtra("image_filename");
            friend_pubkey = getIntent().getStringExtra("friend_pubkey");
            group_identifier = getIntent().getStringExtra("group_identifier");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        boolean storage_frame_work = false;

        try
        {
            String temp = getIntent().getStringExtra("storage_frame_work");
            storage_frame_work = temp.equals("1");
        }
        catch (Exception e)
        {
            e.getMessage();
        }

        if (image_filename == null || orma == null) {
            finish();
            return;
        }

        // We will store all relevant images in this unified list
        List<Message_for_img> imageMessages = new ArrayList<>();
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
                            // Convert GroupMessage to Message for the adapter
                            Message_for_img m = new Message_for_img();
                            m.filename_fullpath = gm.filename_fullpath;
                            m.id = gm.id;
                            m.sent_timestamp = gm.sent_timestamp;
                            m.sent_timestamp_ms = 0;
                            m.storage_frame_work = storage_frame_work;
                            m.setfile_uri(image_filename);
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
                            m2.sent_timestamp_ms = 0;
                            m2.setfile_uri(image_filename);
                            m2.storage_frame_work = storage_frame_work;
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

        if (imageMessages.isEmpty()) {
            finish();
            return;
        }

        if (currentImageIndex == -1) {
            currentImageIndex = 0;
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);

        ImageViewerPagerAdapter adapter = new ImageViewerPagerAdapter(this, imageMessages);
        recyclerView.setAdapter(adapter);

        layoutManager.scrollToPositionWithOffset(currentImageIndex, 0);
    }
}