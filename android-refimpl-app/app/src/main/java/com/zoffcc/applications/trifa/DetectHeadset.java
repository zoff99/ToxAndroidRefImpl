/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2019 Zoff <zoff@zoff.cc>
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

/**
 *   https://github.com/DaVikingCode/UnityDetectHeadset/blob/master/Android/src/com/davikingcode/DetectHeadset/DetectHeadset.java
 *
 *   MIT License
 *
 * Copyright (c) 2017 Da Viking Code
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.zoffcc.applications.trifa;

import android.content.Context;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;

public class DetectHeadset
{
    static AudioManager myAudioManager;

    public DetectHeadset(Context context)
    {
        myAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    public boolean _Detect()
    {
        //Added validation for newer api's above 26.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
        {
            AudioDeviceInfo[] audioDeviceInfos = myAudioManager.getDevices(AudioManager.GET_DEVICES_INPUTS);
            for (int i = 0; i < audioDeviceInfos.length; i++)
            {
                if (audioDeviceInfos[i].getType() == AudioDeviceInfo.TYPE_BLUETOOTH_SCO ||
                    audioDeviceInfos[i].getType() == AudioDeviceInfo.TYPE_WIRED_HEADSET)
                {
                    return true;
                }
            }
        }
        else
        {
            //This should work as expected for the older api's
            if (myAudioManager.isWiredHeadsetOn() || myAudioManager.isBluetoothA2dpOn())
            {
                return true;
            }
        }
        return false;
    }
}
