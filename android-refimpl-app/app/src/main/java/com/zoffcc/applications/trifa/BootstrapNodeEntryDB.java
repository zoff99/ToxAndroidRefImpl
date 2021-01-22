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

package com.zoffcc.applications.trifa;

import android.util.Log;

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.PrimaryKey;
import com.github.gfx.android.orma.annotation.Table;

import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Iterator;

import static com.zoffcc.applications.trifa.MainActivity.PREF__orbot_enabled;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TOX_NODELIST_HOST;
import static com.zoffcc.applications.trifa.TRIFAGlobals.bootstrap_node_list;
import static com.zoffcc.applications.trifa.TRIFAGlobals.tcprelay_node_list;
import static com.zoffcc.applications.trifa.TorHelper.TorResolve;
import static com.zoffcc.applications.trifa.TorHelper.TorSocket;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;

@Table
public class BootstrapNodeEntryDB
{
    static final String TAG = "trifa.BtpNodeEDB";

    @PrimaryKey(autoincrement = true, auto = true)
    long id;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    long num;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    boolean udp_node; // true -> UDP bootstrap node, false -> TCP relay node

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    String ip;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    long port;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    String key_hex;

    @Override
    public String toString()
    {
        // return "" + num + ":" + ip + " port=" + port + " udp_node=" + udp_node + " key_hex=" + key_hex;
        // return "" + num + ":" + ip + " port=" + port + " udp_node="+  udp_node;
        return "" + num + ":" + ip + " port=" + port + " udp_node=" + udp_node + "\n";
    }

    static void insert_node_into_db_real(BootstrapNodeEntryDB n)
    {
        try
        {
            orma.insertIntoBootstrapNodeEntryDB(n);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void insert_default_udp_nodes_into_db()
    {
        BootstrapNodeEntryDB n;
        int num_ = 0;
        n = BootstrapNodeEntryDB_(true, num_, "85.172.30.117", 33445, "8E7D0B859922EF569298B4D261A8CCB5FEA14FB91ED412A7603A585A25698832");
        insert_node_into_db_real(n);
        num_++;
        n = BootstrapNodeEntryDB_(true, num_, "tox.abilinski.com", 33445, "10C00EB250C3233E343E2AEBA07115A5C28920E9C8D29492F6D00B29049EDC7E");
        insert_node_into_db_real(n);
        num_++;
        n = BootstrapNodeEntryDB_(true, num_, "tox.verdict.gg", 33445, "1C5293AEF2114717547B39DA8EA6F1E331E5E358B35F9B6B5F19317911C5F976");
        insert_node_into_db_real(n);
        num_++;
        n = BootstrapNodeEntryDB_(true, num_, "tox.novg.net", 33445, "D527E5847F8330D628DAB1814F0A422F6DC9D0A300E6C357634EE2DA88C35463");
        insert_node_into_db_real(n);
        num_++;
        n = BootstrapNodeEntryDB_(true, num_, "205.185.115.131", 53, "3091C6BEB2A993F1C6300C16549FABA67098FF3D62C6D253828B531470B53D68");
        insert_node_into_db_real(n);
        num_++;
        n = BootstrapNodeEntryDB_(true, num_, "tox.initramfs.io", 33445, "3F0A45A268367C1BEA652F258C85F4A66DA76BCAA667A49E770BCC4917AB6A25");
        insert_node_into_db_real(n);
        num_++;
    }

    public static void insert_default_tcprelay_nodes_into_db()
    {
        BootstrapNodeEntryDB n;
        int num_ = 0;
        n = BootstrapNodeEntryDB_(false, num_, "85.172.30.117", 33445, "8E7D0B859922EF569298B4D261A8CCB5FEA14FB91ED412A7603A585A25698832");
        insert_node_into_db_real(n);
        num_++;
        n = BootstrapNodeEntryDB_(false, num_, "tox.abilinski.com", 33445, "10C00EB250C3233E343E2AEBA07115A5C28920E9C8D29492F6D00B29049EDC7E");
        insert_node_into_db_real(n);
        num_++;
        n = BootstrapNodeEntryDB_(false, num_, "tox.verdict.gg", 33445, "1C5293AEF2114717547B39DA8EA6F1E331E5E358B35F9B6B5F19317911C5F976");
        insert_node_into_db_real(n);
        num_++;
        n = BootstrapNodeEntryDB_(false, num_, "tox.novg.net", 33445, "D527E5847F8330D628DAB1814F0A422F6DC9D0A300E6C357634EE2DA88C35463");
        insert_node_into_db_real(n);
        num_++;
        n = BootstrapNodeEntryDB_(false, num_, "205.185.115.131", 443, "3091C6BEB2A993F1C6300C16549FABA67098FF3D62C6D253828B531470B53D68");
        insert_node_into_db_real(n);
        num_++;
        n = BootstrapNodeEntryDB_(false, num_, "tox.initramfs.io", 3389, "3F0A45A268367C1BEA652F258C85F4A66DA76BCAA667A49E770BCC4917AB6A25");
        insert_node_into_db_real(n);
        num_++;
    }

    public static BootstrapNodeEntryDB BootstrapNodeEntryDB_(boolean udp_node_, int num_, String ip_, long port_, String key_hex_)
    {
        BootstrapNodeEntryDB n = new BootstrapNodeEntryDB();
        n.num = num_;
        n.udp_node = udp_node_;
        n.ip = ip_;
        n.port = port_;
        n.key_hex = key_hex_;

        return n;
    }

    public static String dns_lookup_via_tor(String host_or_ip)
    {
        try
        {
            String IP_address = TorResolve(host_or_ip);
            Log.i(TAG, "dns_lookup_via_tor:TorResolve:" + host_or_ip + " -> " + IP_address);

            if ((IP_address == null) || (IP_address.equals("")))
            {
                // if there is some error, use localhost -> which kind of disables this host
                Log.i(TAG, "dns_lookup_via_tor:EE2:IP_address=" + IP_address);
                return "127.0.0.1";
            }
            else
            {
                return IP_address;
            }
        }
        catch (Exception e)
        {
            // if there is some error, use localhost -> which kind of disables this host
            e.printStackTrace();
            Log.i(TAG, "dns_lookup_via_tor:EE1:" + e.getMessage());
            return "127.0.0.1";
        }
    }

    public static void update_nodelist_from_internet_https_dummy_XXXX()
    {
        // this should be using TOR proxy, if tor is enabled in options!
        String IP_address = TorResolve(TOX_NODELIST_HOST);
        Log.i(TAG, "update_nodelist_from_internet:TorResolve:" + TOX_NODELIST_HOST + " -> " + IP_address);


        try
        {
            Socket s = TorSocket(TOX_NODELIST_HOST, 443);
            DataInputStream is = new DataInputStream(s.getInputStream());
            PrintStream out = new java.io.PrintStream(s.getOutputStream());

            //Construct an HTTP request
            out.print("GET  /" + "json" + " HTTP/1.0\r\n");
            out.print("Host: " + TOX_NODELIST_HOST + ":" + "443" + "\r\n");
            out.print("Accept: */*\r\n");
            out.print("Connection: Keep-Aliv\r\n");
            out.print("Pragma: no-cache\r\n");
            out.print("\r\n");
            out.flush();

            // this is from Java Examples In a Nutshell
            final InputStreamReader from_server = new InputStreamReader(is);
            char[] buffer = new char[1024];
            int chars_read;

            StringBuilder response_text = new StringBuilder();

            // read until stream closes
            while ((chars_read = from_server.read(buffer)) != -1)
            {
                // loop through array of chars
                // change \n to local platform terminator
                // this is a nieve implementation
                for (int j = 0; j < chars_read; j++)
                {
                    if (buffer[j] == '\n')
                    {
                        // System.out.println();
                        response_text.append("\n");
                    }
                    else
                    {
                        // System.out.print(buffer[j]);
                        response_text.append(buffer[j]);
                    }
                }
                // System.out.flush();
            }
            s.close();

            Log.i(TAG, "" + response_text);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void get_tcprelay_nodelist_from_db()
    {
        tcprelay_node_list.clear();

        long tcprelay_nodelist_count = 0;
        try
        {
            tcprelay_nodelist_count = orma.selectFromBootstrapNodeEntryDB().
                    udp_nodeEq(false).count();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        Log.i(TAG, "get_tcprelay_nodelist_from_db:tcprelay_nodelist_count=" + tcprelay_nodelist_count);

        if (tcprelay_nodelist_count == 0)
        {
            Log.i(TAG, "get_tcprelay_nodelist_from_db:insert_default_tcprelay_nodes_into_db");
            insert_default_tcprelay_nodes_into_db();
        }

        // fill tcprelay_node_list with values from DB -----------------
        try
        {
            tcprelay_node_list.addAll(orma.selectFromBootstrapNodeEntryDB().udp_nodeEq(false).orderByNumAsc().toList());
            Log.i(TAG, "get_tcprelay_nodelist_from_db:tcprelay_node_list.addAll");

            if (PREF__orbot_enabled)
            {
                Iterator i = bootstrap_node_list.iterator();
                BootstrapNodeEntryDB e2;
                while (i.hasNext())
                {
                    e2 = (BootstrapNodeEntryDB) i.next();
                    e2.ip = dns_lookup_via_tor(e2.ip);

                }
            }
        }
        catch (Exception e)
        {
        }
        // fill tcprelay_node_list with values from DB -----------------
    }

    public static void get_udp_nodelist_from_db()
    {
        bootstrap_node_list.clear();

        long udp_nodelist_count = 0;
        try
        {
            udp_nodelist_count = orma.selectFromBootstrapNodeEntryDB().
                    udp_nodeEq(true).count();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        Log.i(TAG, "get_udp_nodelist_from_db:udp_nodelist_count=" + udp_nodelist_count);

        if (udp_nodelist_count == 0)
        {
            Log.i(TAG, "get_udp_nodelist_from_db:insert_default_udp_nodes_into_db");
            insert_default_udp_nodes_into_db();
        }

        // fill bootstrap_node_list with values from DB -----------------
        try
        {
            bootstrap_node_list.addAll(orma.selectFromBootstrapNodeEntryDB().udp_nodeEq(true).orderByNumAsc().toList());
            Log.i(TAG, "get_udp_nodelist_from_db:bootstrap_node_list.addAll");

            if (PREF__orbot_enabled)
            {
                Iterator i = bootstrap_node_list.iterator();
                BootstrapNodeEntryDB e2;
                while (i.hasNext())
                {
                    e2 = (BootstrapNodeEntryDB) i.next();
                    e2.ip = dns_lookup_via_tor(e2.ip);
                }
            }
        }
        catch (Exception e)
        {
        }
        // fill bootstrap_node_list with values from DB -----------------
    }
}
