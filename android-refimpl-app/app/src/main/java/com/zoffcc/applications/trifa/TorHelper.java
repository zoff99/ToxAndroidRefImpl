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
 * <p>
 * Tinfoil is an RFID Privacy and Security Enhancement library.
 * <p>
 * Copyright (c) 2005 Joe Foley, MIT AutoID Labs
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

/**
 * Tinfoil is an RFID Privacy and Security Enhancement library.
 *
 *     Copyright (c) 2005 Joe Foley, MIT AutoID Labs

 Permission is hereby granted, free of charge, to any person obtaining a
 copy of this software and associated documentation files (the "Software"),
 to deal in the Software without restriction, including without limitation
 the rights to use, copy, modify, merge, publish, distribute, sublicense,
 and/or sell copies of the Software, and to permit persons to whom the
 Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included
 in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 IN THE SOFTWARE.
 */

// ---------------------------------------------------------
//
// http://www.mit.edu/~foley/TinFoil/src/tinfoil/TorLib.java
//
// ---------------------------------------------------------

package com.zoffcc.applications.trifa;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import static com.zoffcc.applications.trifa.TRIFAGlobals.ORBOT_PROXY_HOST;
import static com.zoffcc.applications.trifa.TRIFAGlobals.ORBOT_PROXY_PORT;


public class TorHelper
{

    /**
     * Default TOR Proxy port.
     */
    static int proxyPort = (int) ORBOT_PROXY_PORT; // 9050;
    /**
     * Default TOR Proxy hostaddr.
     */
    static String proxyAddr = ORBOT_PROXY_HOST;// "localhost";
    /**
     * Constant tells SOCKS4/4a to connect. Use it in the <i>req</i> parameter.
     */
    final static byte TOR_CONNECT = (byte) 0x01;
    /**
     * Constant tells TOR to do a DNS resolve.  Use it in the <i>req</i> parameter.
     */
    final static byte TOR_RESOLVE = (byte) 0xF0;
    /**
     * Constant indicates what SOCKS version are talking
     * Either SOCKS4 or SOCKS4a
     */
    final static byte SOCKS_VERSION = (byte) 0x04;
    /**
     * SOCKS uses Nulls as field delimiters
     */
    final static byte SOCKS_DELIM = (byte) 0x00;
    /**
     * Setting the IP field to 0.0.0.1 causes SOCKS4a to
     * be enabled.
     */
    final static int SOCKS4A_FAKEIP = (int) 0x01;

    /**
     * This method opens a TOR socket, and does an anonymous DNS resolve through it.
     * Since Tor caches things, this is a very fast lookup if we've already connected there
     * The resolve does a gethostbyname() on the exit node.
     *
     * @param targetHostname String containing the hostname to look up.
     * @return String representation of the IP address: "x.x.x.x"
     */
    static String TorResolve(String targetHostname)
    {
        int targetPort = 0; // we dont need a port to resolve

        try
        {
            Socket s = TorSocketPre(targetHostname, targetPort, TOR_RESOLVE);
            DataInputStream is = new DataInputStream(s.getInputStream());

            byte version = is.readByte();
            byte status = is.readByte();
            if (status != (byte) 90)
            {
                //failed for some reason, return useful exception
                throw (new IOException(ParseSOCKSStatus(status)));
            }
            int port = is.readShort();
            byte[] ipAddrBytes = new byte[4];
            is.read(ipAddrBytes);
            InetAddress ia = InetAddress.getByAddress(ipAddrBytes);
            //System.out.println("Resolved into:"+ia);
            is.close();
            String addr = ia.toString().substring(1); // clip off the "/"
            return (addr);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (null);
    }

    /**
     * This helper method allows us to decode the SOCKS4 status codes into
     * Human readible input.<br />
     * Based upon info from http://archive.socks.permeo.com/protocol/socks4.protocol
     *
     * @param status Byte containing the status code.
     * @return String human-readible representation of the error.
     */
    static String ParseSOCKSStatus(byte status)
    {
        // func to turn the status codes into useful output
        // reference
        String retval;
        switch (status)
        {
            case 90:
                retval = status + " Request granted.";
                break;
            case 91:
                retval = status + " Request rejected/failed - unknown reason.";
                break;
            case 92:
                retval = status + " Request rejected: SOCKS server cannot connect to identd on the client.";
                break;
            case 93:
                retval = status + " Request rejected: the client program and identd report different user-ids.";
                break;
            default:
                retval = status + " Unknown SOCKS status code.";
        }
        return (retval);

    }

    /**
     * This method Creates a socket, then sends the inital SOCKS request info
     * It stops before reading so that other methods may
     * differently interpret the results.  It returns the open socket.
     *
     * @param targetHostname The hostname of the destination host.
     * @param targetPort     The port to connect to
     * @param req            SOCKS/TOR request code
     * @return An open Socket that has been sent the SOCK4a init codes.
     * @throws IOException from any Socket problems
     */
    static Socket TorSocketPre(String targetHostname, int targetPort, byte req) throws IOException
    {

        Socket s;
        //		System.out.println("Opening connection to "+targetHostname+":"+targetPort+
        //				" via proxy "+proxyAddr+":"+proxyPort+" of type "+req);
        s = new Socket(proxyAddr, proxyPort);
        DataOutputStream os = new DataOutputStream(s.getOutputStream());
        os.writeByte(SOCKS_VERSION);
        os.writeByte(req);
        // 2 bytes
        os.writeShort(targetPort);
        // 4 bytes, high byte first
        os.writeInt(SOCKS4A_FAKEIP);
        os.writeByte(SOCKS_DELIM);
        os.writeBytes(targetHostname);
        os.writeByte(SOCKS_DELIM);
        return (s);
    }


    /**
     * This method creates a socket to the target host and port using TorSocketPre, then reads
     * the SOCKS information.
     *
     * @param targetHostname Hostname of destination host.
     * @param targetPort     Port on remote destination host.
     * @return Fully initialized TCP Socket that tunnels to the target Host/Port via the Tor Proxy host/port.
     * @throws IOException when Socket and Read/Write exceptions occur.
     */
    static Socket TorSocket(String targetHostname, int targetPort) throws IOException
    {
        Socket s = TorSocketPre(targetHostname, targetPort, TOR_CONNECT);
        DataInputStream is = new DataInputStream(s.getInputStream());

        // only the status is useful on a TOR CONNECT
        byte version = is.readByte();
        byte status = is.readByte();
        if (status != (byte) 90)
        {
            //failed for some reason, return useful exception
            throw (new IOException(ParseSOCKSStatus(status)));
        }
        //		System.out.println("status: "+ParseSOCKSStatus(status));
        int port = is.readShort();
        int ipAddr = is.readInt();
        return (s);
    }

}
