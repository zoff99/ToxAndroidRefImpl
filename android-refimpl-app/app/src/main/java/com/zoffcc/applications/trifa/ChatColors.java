package com.zoffcc.applications.trifa;

import android.graphics.Color;

public class ChatColors
{
    static int[] PeerAvatarColors = {
            Color.parseColor("#0000FF"),
            Color.parseColor("#FF00FF"),
            Color.parseColor("#00FFFF"),
            Color.parseColor("#008000"),
            Color.parseColor("#00FF00"),
            Color.parseColor("#800000"),
            Color.parseColor("#000080"),
            Color.parseColor("#808000"),
            Color.parseColor("#800080"),
            Color.parseColor("#FF0000"),
            Color.parseColor("#008080"),
            Color.parseColor("#FFFF00")
    };

    static int get_size()
    {
        return PeerAvatarColors.length;
    }
}
