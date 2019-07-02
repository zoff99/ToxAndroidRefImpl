/*
    Privacy Friendly Net Monitor (Net Monitor)
    - Copyright (2015 - 2017) Felix Tsala Schiller

    ###################################################################

    This file is part of Net Monitor.

    Net Monitor is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Net Monitor is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Net Monitor.  If not, see <http://www.gnu.org/licenses/>.

    Diese Datei ist Teil von Net Monitor.

    Net Monitor ist Freie Software: Sie können es unter den Bedingungen
    der GNU General Public License, wie von der Free Software Foundation,
    Version 3 der Lizenz oder (nach Ihrer Wahl) jeder späteren
    veröffentlichten Version, weiterverbreiten und/oder modifizieren.

    Net Monitor wird in der Hoffnung, dass es nützlich sein wird, aber
    OHNE JEDE GEWÄHRLEISTUNG, bereitgestellt; sogar ohne die implizite
    Gewährleistung der MARKTFÄHIGKEIT oder EIGNUNG FÜR EINEN BESTIMMTEN ZWECK.
    Siehe die GNU General Public License für weitere Details.

    Sie sollten eine Kopie der GNU General Public License zusammen mit diesem
    Programm erhalten haben. Wenn nicht, siehe <http://www.gnu.org/licenses/>.

    ###################################################################

    This app has been created in affiliation with SecUSo-Department of Technische Universität
    Darmstadt.

    Privacy Friendly Net Monitor is based on TLSMetric by Felix Tsala Schiller
    https://bitbucket.org/schillef/tlsmetric/overview.
 */

package org.secuso.privacyfriendlynetmonitor.Assistant;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Enumeration;

/**
 * Class for all the litte helpers, used by more than one layer
 */
public class ToolBox
{

    private static final char[] hexCode = "0123456789ABCDEF".toCharArray();

    //Convert byte[] to HexString
    public static String printHexBinary(byte[] data)
    {
        StringBuilder r = new StringBuilder(data.length * 2);
        for (byte b : data)
        {
            r.append(hexCode[(b >> 4) & 0xF]);
            r.append(hexCode[(b & 0xF)]);
        }
        return r.toString();
    }

    //Convert HexString to byte[]
    public static byte[] hexStringToByteArray(String s)
    {
        byte[] b = new byte[s.length() / 2];
        for (int i = 0; i < b.length; i++)
        {
            int index = i * 2;
            int v = Integer.parseInt(s.substring(index, index + 2), 16);
            b[i] = (byte) v;
        }
        return b;
    }

    //Convert byte[] to wireshark import-string.
    public static String printExportHexString(byte[] data)
    {
        String hexString = printHexBinary(data);
        StringBuilder export = new StringBuilder("000000 ");
        for (int i = 0; i + 1 < hexString.length(); i += 2)
        {
            export.append(" ").append(hexString.substring(i, i + 2));
        }
        export.append(" ......");
        return export.toString();
    }

    //Returns active network interfaces
    public String getIfs(Context context)
    {
        //read from command: netcfg | grep UP

        String filePath = context.getFilesDir().getAbsolutePath() + File.separator + Const.FILE_IF_LIST;
        if (Const.IS_DEBUG)
        {
            Log.d(Const.LOG_TAG, "Try to get active interfaces to" + filePath);
        }
        ExecCom.user("rm " + filePath);
        ExecCom.user("netcfg | grep UP -> " + filePath);
        String result = ExecCom.userForResult("cat " + filePath);
        return result;
    }

    //Char to value
    private int hexToBin(char ch)
    {
        if ('0' <= ch && ch <= '9')
        {
            return ch - '0';
        }
        if ('A' <= ch && ch <= 'F')
        {
            return ch - 'A' + 10;
        }
        if ('a' <= ch && ch <= 'f')
        {
            return ch - 'a' + 10;
        }
        return -1;
    }

    //Lookup local IP address
    public static InetAddress getLocalAddress()
    {
        try
        {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); )
            {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); )
                {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress())
                    {
                        return inetAddress;
                    }
                }
            }
        }
        catch (Exception e)
        {
            Log.e(Const.LOG_TAG, "Error while obtaining local address");
            e.printStackTrace();
        }
        return null;
    }

    //Search for byte array in given byte array.
    public static int searchByteArray(byte[] input, byte[] searchedFor)
    {
        //convert byte[] to Byte[]
        Byte[] searchedForB = new Byte[searchedFor.length];
        for (int x = 0; x < searchedFor.length; x++)
        {
            searchedForB[x] = searchedFor[x];
        }

        int idx = -1;
        //search:
        Deque<Byte> q = new ArrayDeque<>(input.length);
        for (int i = 0; i < input.length; i++)
        {
            if (q.size() == searchedForB.length)
            {
                //here I can check
                Byte[] cur = q.toArray(new Byte[]{});
                if (Arrays.equals(cur, searchedForB))
                {
                    //found!
                    idx = i - searchedForB.length;
                    break;
                }
                else
                {
                    //not found
                    q.pop();
                    q.addLast(input[i]);
                }
            }
            else
            {
                q.addLast(input[i]);
            }
        }
        if (Const.IS_DEBUG && idx != -1)
        {
            Log.d(Const.LOG_TAG, ToolBox.printHexBinary(searchedFor) + " found at position " + idx);
        }
        return idx;
    }

    //Convert a Java long to a four byte array
    public static byte[] longToFourBytes(long l)
    {
        ByteBuffer bb = ByteBuffer.allocate(8);
        byte[] b = new byte[4];
        bb.putLong(l);
        bb.position(4);
        bb.get(b);
        return b;

    }

    //Convert a Java int to a two byte array
    public static byte[] intToTwoBytes(int i)
    {
        ByteBuffer bb = ByteBuffer.allocate(4);
        byte[] b = new byte[2];
        bb.putInt(i);
        bb.position(2);
        bb.get(b);
        return b;
    }

    //Convert four bytes to a Java Long
    public static long fourBytesToLong(byte[] b)
    {
        ByteBuffer bb = ByteBuffer.allocate(8);
        bb.position(4);
        bb.put(b);
        bb.position(0);
        return bb.getLong();
    }

    //Convert two bytes to a Java int
    public static int twoBytesToInt(byte[] b)
    {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.position(2);
        bb.put(b);
        bb.position(0);
        return bb.getInt();
    }

    //Reverse the order in a Byte array
    public static byte[] reverseByteArray(byte[] b)
    {
        byte[] b0 = new byte[b.length];
        int j = 0;
        for (int i = b.length - 1; i >= 0; i--)
        {
            b0[j] = b[i];
            j++;
        }
        return b0;
    }

    //ipv6 Hex to address String calculator
    //e.g.: B80D01200000000067452301EFCDAB89 -> 2001:0db8:0000:0000:0123:4567:89ab:cdef
    public static String hexToIp6(String hexaIP)
    {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < hexaIP.length(); i = i + 8)
        {
            String word = hexaIP.substring(i, i + 8);
            for (int j = word.length() - 1; j >= 0; j = j - 2)
            {
                result.append(word.substring(j - 1, j + 1));
                result.append((j == 5) ? ":" : "");//in the middle
            }
            result.append(":");
        }
        return result.substring(0, result.length() - 1).toString();
    }

    //ipv4 Hex to address String calculator
    //e.g.: 0100A8C0 -> 192.168.0.1*/
    public static String hexToIp4(String hexa)
    {
        StringBuilder result = new StringBuilder();
        //reverse Little to Big
        for (int i = hexa.length() - 1; i >= 0; i = i - 2)
        {
            String wtf = hexa.substring(i - 1, i + 1);
            result.append(Integer.parseInt(wtf, 16));
            result.append(".");
        }
        //remove last ".";
        return result.substring(0, result.length() - 1).toString();
    }
}
