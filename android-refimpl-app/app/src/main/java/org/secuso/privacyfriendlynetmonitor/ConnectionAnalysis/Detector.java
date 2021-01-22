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

import org.secuso.privacyfriendlynetmonitor.Assistant.Const;
import org.secuso.privacyfriendlynetmonitor.Assistant.ExecCom;
import org.secuso.privacyfriendlynetmonitor.Assistant.TLType;
import org.secuso.privacyfriendlynetmonitor.Assistant.ToolBox;

import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Detects active connections on the device and identifies port-uid-pid realation. Corresponding
 * Apps are identified.
 */

public class Detector
{

    //Members
    //Get commands for shell readin
    private static final String commandTcp = "cat /proc/net/tcp";
    private static final String commandTcp6 = "cat /proc/net/tcp6";
    private static final String commandUdp = "cat /proc/net/udp";
    private static final String commandUdp6 = "cat /proc/net/udp6";

    static HashMap<Integer, Report> sReportMap = new HashMap<>();

    //Update the report HashMap with currently scanned connections
    public static void updateReportMap()
    {
        updateOrAdd(getCurrentConnections());
        // SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(RunStore.getAppContext());
        boolean isLog = false; // prefs.getBoolean(Const.IS_LOG, false);
        boolean isCertVal = false;
        if (!isLog && !isCertVal)
        {
            removeOldReports();
        }
    }

    //Update existing or add new reports
    private static void updateOrAdd(ArrayList<Report> reportList)
    {
        for (int i = 0; i < reportList.size(); i++)
        {
            //Key = source-Port
            int key = reportList.get(i).localPort;
            String package_name = reportList.get(i).packageName;

            if (sReportMap.containsKey(key))
            {
                Report r = sReportMap.get(key);
                r.touch();
                r.state = reportList.get(i).state;
            }
            else
            {
                sReportMap.put(key, reportList.get(i));
            }
        }
    }

    //Remove timed-out connection-reports
    private static void removeOldReports()
    {
        Timestamp thresh = new Timestamp(System.currentTimeMillis() - Const.REPORT_TTL_DEFAULT);

        HashSet<Integer> keySet = new HashSet<>(sReportMap.keySet());
        for (int key : keySet)
        {
            if (sReportMap.get(key).timestamp.compareTo(thresh) < 0)
            {
                sReportMap.remove(key);
            }
        }
    }

    // read the current connections off the designated files
    private static ArrayList<Report> getCurrentConnections()
    {
        ArrayList<Report> fullReportList = new ArrayList<>();

        //generate full report of all tcp/udp connections
        fullReportList.addAll(parseNetOutput(ExecCom.userForResult(commandTcp), TLType.tcp));
        fullReportList.addAll(parseNetOutput(ExecCom.userForResult(commandTcp6), TLType.tcp6));
        fullReportList.addAll(parseNetOutput(ExecCom.userForResult(commandUdp), TLType.udp));
        fullReportList.addAll(parseNetOutput(ExecCom.userForResult(commandUdp6), TLType.udp6));

        return fullReportList;
    }

    //Parse output from /proc/net/tcp and udp files (ip4/6)
    private static List<Report> parseNetOutput(String readIn, TLType type)
    {
        String[] splitLines;
        LinkedList<Report> reportList = new LinkedList<>();

        splitLines = readIn.split("\\n");
        for (int i = 1; i < splitLines.length; i++)
        {
            splitLines[i] = splitLines[i].trim();
            reportList.add(initReport(splitLines[i], type));
        }
        return reportList;
    }

    //Initiate a reports from a read in line
    private static Report initReport(String splitLine, TLType type)
    {
        String splitTabs[];
        while (splitLine.contains("  "))
        {
            splitLine = splitLine.replace("  ", " ");
        }
        splitTabs = splitLine.split("\\s");

        if (type == TLType.tcp || type == TLType.udp)
        {
            //Init IPv4 values
            return initReport4(splitTabs, type);
        }
        else
        {
            //Init IPv6 values
            return initReport6(splitTabs, type);
        }
    }

    //Init parsed data to IPv4 connection report
    private static Report initReport4(String[] splitTabs, TLType type)
    {
        int pos;
        pos = 0;
        //Allocating buffer for 4 Bytes add and 2 bytes port each + 2 bytes UID
        ByteBuffer bb = ByteBuffer.allocate(15);
        bb.position(0);

        //local address
        String hexStr = splitTabs[1].substring(pos, pos + 8);
        bb.put(ToolBox.hexStringToByteArray(hexStr));

        //local port
        pos = splitTabs[1].indexOf(":");
        hexStr = splitTabs[1].substring(pos + 1, pos + 5);
        bb.put(ToolBox.hexStringToByteArray(hexStr));

        //remote address
        pos = 0;
        hexStr = splitTabs[2].substring(pos, pos + 8);
        bb.put(ToolBox.hexStringToByteArray(hexStr));

        //local port
        pos = splitTabs[2].indexOf(":");
        hexStr = splitTabs[2].substring(pos + 1, pos + 5);
        bb.put(ToolBox.hexStringToByteArray(hexStr));

        //UID
        bb.putShort(Short.parseShort(splitTabs[7]));

        //state
        bb.put(ToolBox.hexStringToByteArray(splitTabs[3]));

        return new Report(bb, type);
    }

    //Init parsed data to IPv6 connection report
    private static Report initReport6(String[] splitTabs, TLType type)
    {
        int pos;
        pos = 0;
        //Allocating buffer for 16 Bytes add and 2 bytes port each + 2 bytes UID
        ByteBuffer bb = ByteBuffer.allocate(39);
        bb.position(0);

        //local address
        String hexStr = splitTabs[1].substring(pos, pos + 32);
        bb.put(ToolBox.hexStringToByteArray(hexStr));

        //local port
        pos = splitTabs[1].indexOf(":");
        hexStr = splitTabs[1].substring(pos + 1, pos + 5);
        bb.put(ToolBox.hexStringToByteArray(hexStr));

        //remote address
        pos = 0;
        hexStr = splitTabs[2].substring(pos, pos + 32);
        bb.put(ToolBox.hexStringToByteArray(hexStr));

        //local port
        pos = splitTabs[2].indexOf(":");
        hexStr = splitTabs[2].substring(pos + 1, pos + 5);
        bb.put(ToolBox.hexStringToByteArray(hexStr));

        //UID
        short a = Short.parseShort(splitTabs[7]);
        bb.putShort(a);

        //state
        bb.put(ToolBox.hexStringToByteArray(splitTabs[3]));

        return new Report(bb, type);
    }
}
