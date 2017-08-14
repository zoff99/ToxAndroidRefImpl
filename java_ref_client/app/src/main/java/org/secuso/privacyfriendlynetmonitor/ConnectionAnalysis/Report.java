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
package org.secuso.privacyfriendlynetmonitor.ConnectionAnalysis;

import android.graphics.drawable.Drawable;
import android.util.Log;

import org.secuso.privacyfriendlynetmonitor.Assistant.Const;
import org.secuso.privacyfriendlynetmonitor.Assistant.TLType;
import org.secuso.privacyfriendlynetmonitor.Assistant.ToolBox;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.sql.Timestamp;

import static java.lang.Math.abs;

/**
 * Information class for acquired connection data. Full report on available information from device.
 */
public class Report implements Serializable
{

    //Constructor parses HEX-data from /proc/net/*type* scan
    Report(ByteBuffer bb, TLType type)
    {
        touch();
        this.type = type;
        // Fill with bytebuffer data
        if (type == TLType.tcp || type == TLType.udp)
        {
            initIP4(bb);
        }
        else
        {
            initIP6(bb);
        }

        //Init InetAddresses
        try
        {
            if (type == TLType.tcp || type == TLType.udp)
            {
                localAdd = InetAddress.getByName(ToolBox.hexToIp4(ToolBox.printHexBinary(localAddHex)));
                remoteAdd = InetAddress.getByName(ToolBox.hexToIp4(ToolBox.printHexBinary(remoteAddHex)));
            }
            else
            {
                localAdd = InetAddress.getByName(ToolBox.hexToIp6(ToolBox.printHexBinary(localAddHex)));
                remoteAdd = InetAddress.getByName(ToolBox.hexToIp6(ToolBox.printHexBinary(remoteAddHex)));
            }
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
        }
        if (Const.IS_DEBUG)
        {
            Log.d(Const.LOG_TAG, "Report (" + type + "):" + localAdd.getHostAddress() + ":" + localPort + " " + remoteAdd.getHostAddress() + ":" + remotePort + " - UID: " + uid);
        }
    }

    //Members
    public TLType type;
    public Timestamp timestamp;

    public byte[] localAddHex;
    public InetAddress localAdd;
    public int localPort;
    public byte[] state;

    public byte[] remoteAddHex;
    public InetAddress remoteAdd;

    public int remotePort;

    public int pid;
    public int uid;

    public Drawable icon;
    public String appName;

    public String packageName;

    //Set current timestamp
    public void touch()
    {
        timestamp = new Timestamp(System.currentTimeMillis());
    }

    // -----------------------
    // Init Methods
    // -----------------------

    //Fill report with Ip4 - tcp/udp connection data from bytebuffer readin
    private void initIP4(ByteBuffer bb)
    {
        bb.position(0);
        byte[] b = new byte[2];
        localAddHex = new byte[4];
        bb.get(localAddHex);
        bb.get(b);
        localPort = ToolBox.twoBytesToInt(b);
        remoteAddHex = new byte[4];
        bb.get(remoteAddHex);
        bb.get(b);
        remotePort = ToolBox.twoBytesToInt(b);
        uid = abs(bb.getShort());
        state = new byte[1];
        bb.get(state);
    }

    //Fill report with Ip6 - tcp/udp connection data from bytebuffer readin
    private void initIP6(ByteBuffer bb)
    {
        bb.position(0);
        byte[] b = new byte[2];
        localAddHex = new byte[16];
        bb.get(localAddHex);
        bb.get(b);
        localPort = ToolBox.twoBytesToInt(b);
        remoteAddHex = new byte[16];
        bb.get(remoteAddHex);
        bb.get(b);
        remotePort = ToolBox.twoBytesToInt(b);
        uid = abs((bb.getShort()));
        state = new byte[1];
        bb.get(state);
    }
}