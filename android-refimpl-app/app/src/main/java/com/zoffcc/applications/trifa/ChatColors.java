package com.zoffcc.applications.trifa;

import android.graphics.Color;

public class ChatColors
{
    static int[] PeerAvatarColors = {
            //
            // https://www.w3schools.com/colors/colors_picker.asp
            //
            // ** too dark ** // Color.parseColor("#0000FF"), // Blue
            Color.parseColor("#6666ff"), // * lighter blue *
            //
            // ** // Color.parseColor("#FF00FF"), // Fuchsia
            //
            Color.parseColor("#00FFFF"), // Aqua
            //
            Color.parseColor("#008000"), // Green
            //
            Color.parseColor("#dce775"), // Lime
            //
            // ** too dark ** // Color.parseColor("#800000"), // Maroon
            Color.parseColor("#f06292"), // * lighter red *
            //
            // ** too dark ** // Color.parseColor("#000080"), // Navy
            Color.parseColor("#42a5f5"), // * lighter blue *
            //
            Color.parseColor("#808000"), // Olive
            //
            Color.parseColor("#800080"), // Purple
            //
            // ** too dark ** // Color.parseColor("#FF0000"), // Red
            Color.parseColor("#ff4d4d"), // * lighter red *
            //
            Color.parseColor("#008080"), // Teal
            //
            // ** too bright ** // Color.parseColor("#FFFF00")  // Yellow
            Color.parseColor("#cccc00"), // * darker yellow *
            //
    };

    static int get_size()
    {
        return PeerAvatarColors.length;
    }
}
