package com.zoffcc.applications.trifa;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import static com.zoffcc.applications.trifa.MainActivity.main_handler_s;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;

public class FriendInfoActivity extends AppCompatActivity
{
    static final String TAG = "trifa.FriendInfoActy";
    ImageView profile_icon = null;
    TextView mytoxid = null;
    TextView mynick = null;
    TextView mystatus_message = null;
    long friendnum = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friendinfo);

        Intent intent = getIntent();
        friendnum = intent.getLongExtra("friendnum", -1);

        profile_icon = (ImageView) findViewById(R.id.fi_profile_icon);
        mytoxid = (TextView) findViewById(R.id.fi_toxprvkey_textview);
        mynick = (TextView) findViewById(R.id.fi_nick_text);
        mystatus_message = (TextView) findViewById(R.id.fi_status_message_text);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mytoxid.setText("");
        mynick.setText("");
        mystatus_message.setText("");

        Drawable d1 = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_face).color(getResources().getColor(R.color.colorPrimaryDark)).sizeDp(24);
        profile_icon.setImageDrawable(d1);

        try
        {
            final long friendnum_ = friendnum;
            Thread t = new Thread()
            {
                @Override
                public void run()
                {
                    final FriendList f = orma.selectFromFriendList().tox_friendnumEq(friendnum_).toList().get(0);

                    Runnable myRunnable = new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                mytoxid.setText(f.tox_public_key_string);
                                mynick.setText(f.name);
                                mystatus_message.setText(f.status_message);
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                                Log.i(TAG, "CALL:start:EE:" + e.getMessage());
                            }
                        }
                    };
                    main_handler_s.post(myRunnable);
                }
            };
            t.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }
}