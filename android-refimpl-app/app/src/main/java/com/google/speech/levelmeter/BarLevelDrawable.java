/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2017 Zoff <zoff@zoff.cc>
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

// Copyright 2011 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.


package com.google.speech.levelmeter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.util.AttributeSet;
import android.view.View;

/**
 * This class draws a colorful graphical level indicator similar to an
 * LED VU bar graph.
 * <p>
 * This is a user defined View UI element that contains a ShapeDrawable, which
 * means it can be placed using in the XML UI configuration and updated
 * dynamically at runtime.
 * <p>
 * To set the level, use setLevel(level). Level should be in the range
 * [0.0 ; 1.0].
 * <p>
 * To change the number of segments or colors, change the segmentColors array.
 *
 * @author Trausti Kristjansson
 */
public final class BarLevelDrawable extends View
{
    // private ShapeDrawable mDrawable;
    private ShapeDrawable mDrawable_off;
    private ShapeDrawable mDrawable_green;
    private ShapeDrawable mDrawable_yellow;
    private ShapeDrawable mDrawable_red;
    private double mLevel = 0.1;

    final int[] segmentColors = {0xff00ff00, 0xff00ff00, 0xff00ff00, 0xff00ff00, 0xff00ff00, 0xff00ff00, // green
            0xffffff00, 0xffffff00, 0xffffff00, // yellow
            0xffff0000}; // red
    // final int segmentOffColor = 0xff555555;

    public BarLevelDrawable(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        initBarLevelDrawable();
    }

    public BarLevelDrawable(Context context)
    {
        super(context);
        initBarLevelDrawable();
    }

    /**
     * Set the bar level. The level should be in the range [0.0 ; 1.0], i.e.
     * 0.0 gives no lit LEDs and 1.0 gives full scale.
     *
     * @param level the LED level in the range [0.0 ; 1.0].
     */
    public void setLevel(double level)
    {
        if (level < 0)
        {
            mLevel = 0;
        }
        else
        {
            if (level > 1.0)
            {
                mLevel = 1.0;
            }
            else
            {
                mLevel = level;
            }
        }
        invalidate();
    }

    public double getLevel()
    {
        return mLevel;
    }

    private void initBarLevelDrawable()
    {
        mLevel = 0.1;
        mDrawable_off = new ShapeDrawable(new RectShape());
        mDrawable_off.getPaint().setColor(0xff555555);
        mDrawable_green = new ShapeDrawable(new RectShape());
        mDrawable_green.getPaint().setColor(0xff00ff00);
        mDrawable_yellow = new ShapeDrawable(new RectShape());
        mDrawable_yellow.getPaint().setColor(0xffffff00);
        mDrawable_red = new ShapeDrawable(new RectShape());
        mDrawable_red.getPaint().setColor(0xffff0000);
    }

    private void drawBar(Canvas canvas)
    {
        // System.out.println("BarLevelDrawable:drawBar");

        int padding = 4; // Padding on both sides.
        int x = 0;
        int y = 0;

        int num_bars_div = 2;
        int num_bars = segmentColors.length * num_bars_div;
        int height = (int) (Math.floor(getHeight() / num_bars)) - (2 * padding);
        int width = getWidth();

        // System.out.println("BarLevelDrawable:w=" + getWidth() + " h=" + getHeight() + " width=" + width + " height=" + height);

        // mDrawable = new ShapeDrawable(new RectShape());
        for (int i = 0; i < num_bars; i++)
        {
            y = y + padding;
            if ((mLevel * num_bars) > (i + 0.5))
            {
                if (segmentColors[i / num_bars_div] == 0xffffff00) // yellow
                {
                    mDrawable_yellow.setBounds(x, getHeight() - y - height, x + width, getHeight() - y);
                    mDrawable_yellow.draw(canvas);
                }
                else if (segmentColors[i / num_bars_div] == 0xffff0000) // red
                {
                    mDrawable_red.setBounds(x, getHeight() - y - height, x + width, getHeight() - y);
                    mDrawable_red.draw(canvas);
                }
                else // green
                {
                    mDrawable_green.setBounds(x, getHeight() - y - height, x + width, getHeight() - y);
                    mDrawable_green.draw(canvas);
                }
            }
            else
            {
                mDrawable_off.setBounds(x, getHeight() - y - height, x + width, getHeight() - y);
                mDrawable_off.draw(canvas);
            }
            y = y + height + padding;
        }
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        // System.out.println("BarLevelDrawable:onDraw");
        drawBar(canvas);
    }
}