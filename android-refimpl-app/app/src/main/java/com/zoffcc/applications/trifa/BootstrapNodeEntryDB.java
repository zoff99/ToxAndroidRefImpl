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

import static com.zoffcc.applications.trifa.HelperGeneric.validate_ipv4;
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
        // @formatter:off
        n = BootstrapNodeEntryDB_(true, num_, "85.143.221.42",33445,"DA4E4ED4B697F2E9B000EEFE3A34B554ACD3F45F5C96EAEA2516DD7FF9AF7B43");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "tox.verdict.gg",33445,"1C5293AEF2114717547B39DA8EA6F1E331E5E358B35F9B6B5F19317911C5F976");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "78.46.73.141",33445,"02807CF4F8BB8FB390CC3794BDF1E8449E9A8392C5D3F2200019DA9F1E812E46");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "tox.initramfs.io",33445,"3F0A45A268367C1BEA652F258C85F4A66DA76BCAA667A49E770BCC4917AB6A25");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "46.229.52.198",33445,"813C8F4187833EF0655B10F7752141A352248462A567529A38B6BBF73E979307");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "144.217.167.73",33445,"7E5668E0EE09E19F320AD47902419331FFEE147BB3606769CFBE921A2A2FD34C");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "tox.abilinski.com",33445,"10C00EB250C3233E343E2AEBA07115A5C28920E9C8D29492F6D00B29049EDC7E");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "tox.novg.net",33445,"D527E5847F8330D628DAB1814F0A422F6DC9D0A300E6C357634EE2DA88C35463");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "95.31.18.227",33445,"257744DBF57BE3E117FE05D145B5F806089428D4DCE4E3D0D50616AA16D9417E");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "198.199.98.108",33445,"BEF0CFB37AF874BD17B9A8F9FE64C75521DB95A37D33C5BDB00E9CF58659C04F");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "tox.kurnevsky.net",33445,"82EF82BA33445A1F91A7DB27189ECFC0C013E06E3DA71F588ED692BED625EC23");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "81.169.136.229",33445,"E0DB78116AC6500398DDBA2AEEF3220BB116384CAB714C5D1FCD61EA2B69D75E");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "205.185.115.131",53,"3091C6BEB2A993F1C6300C16549FABA67098FF3D62C6D253828B531470B53D68");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "tox2.abilinski.com",33445,"7A6098B590BDC73F9723FC59F82B3F9085A64D1B213AAF8E610FD351930D052D");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "46.101.197.175",33445,"CD133B521159541FB1D326DE9850F5E56A6C724B5B8E5EB5CD8D950408E95707");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "tox1.mf-net.eu",33445,"B3E5FA80DC8EBD1149AD2AB35ED8B85BD546DEDE261CA593234C619249419506");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "tox2.mf-net.eu",33445,"70EA214FDE161E7432530605213F18F7427DC773E276B3E317A07531F548545F");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "195.201.7.101",33445,"B84E865125B4EC4C368CD047C72BCE447644A2DC31EF75BD2CDA345BFD310107");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "168.138.203.178",33445,"6D04D8248E553F6F0BFDDB66FBFB03977E3EE54C432D416BC2444986EF02CC17");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "209.59.144.175",33445,"214B7FEA63227CAEC5BCBA87F7ABEEDB1A2FF6D18377DD86BF551B8E094D5F1E");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "188.225.9.167",33445,"1911341A83E02503AB1FD6561BD64AF3A9D6C3F12B5FBB656976B2E678644A67");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "122.116.39.151",33445,"5716530A10D362867C8E87EE1CD5362A233BAFBBA4CF47FA73B7CAD368BD5E6E");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "195.123.208.139",33445,"534A589BA7427C631773D13083570F529238211893640C99D1507300F055FE73");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "208.38.228.104",33445,"3634666A51CA5BE1579C031BD31B20059280EB7C05406ED466BD9DFA53373271");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "104.225.141.59",43334,"933BA20B2E258B4C0D475B6DECE90C7E827FE83EFA9655414E7841251B19A72C");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "137.74.42.224",33445,"A95177FA018066CF044E811178D26B844CBF7E1E76F140095B3A1807E081A204");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(true, num_, "198.98.49.206",33445,"28DB44A3CEEE69146469855DFFE5F54DA567F5D65E03EFB1D38BBAEFF2553255");insert_node_into_db_real(n);num_++;
        // @formatter:on
    }

    public static void insert_default_tcprelay_nodes_into_db()
    {
        BootstrapNodeEntryDB n;
        int num_ = 0;
        // @formatter:off
        n = BootstrapNodeEntryDB_(false, num_, "85.143.221.42",33445,"DA4E4ED4B697F2E9B000EEFE3A34B554ACD3F45F5C96EAEA2516DD7FF9AF7B43");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "tox.verdict.gg",33445,"1C5293AEF2114717547B39DA8EA6F1E331E5E358B35F9B6B5F19317911C5F976");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "78.46.73.141",3389,"02807CF4F8BB8FB390CC3794BDF1E8449E9A8392C5D3F2200019DA9F1E812E46");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "tox.initramfs.io",33445,"3F0A45A268367C1BEA652F258C85F4A66DA76BCAA667A49E770BCC4917AB6A25");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "144.217.167.73",33445,"7E5668E0EE09E19F320AD47902419331FFEE147BB3606769CFBE921A2A2FD34C");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "tox.abilinski.com",33445,"10C00EB250C3233E343E2AEBA07115A5C28920E9C8D29492F6D00B29049EDC7E");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "tox.novg.net",33445,"D527E5847F8330D628DAB1814F0A422F6DC9D0A300E6C357634EE2DA88C35463");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "95.31.18.227",33445,"257744DBF57BE3E117FE05D145B5F806089428D4DCE4E3D0D50616AA16D9417E");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "198.199.98.108",3389,"BEF0CFB37AF874BD17B9A8F9FE64C75521DB95A37D33C5BDB00E9CF58659C04F");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "tox.kurnevsky.net",33445,"82EF82BA33445A1F91A7DB27189ECFC0C013E06E3DA71F588ED692BED625EC23");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "81.169.136.229",3389,"E0DB78116AC6500398DDBA2AEEF3220BB116384CAB714C5D1FCD61EA2B69D75E");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "205.185.115.131",443,"3091C6BEB2A993F1C6300C16549FABA67098FF3D62C6D253828B531470B53D68");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "tox2.abilinski.com",33445,"7A6098B590BDC73F9723FC59F82B3F9085A64D1B213AAF8E610FD351930D052D");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "46.101.197.175",33445,"CD133B521159541FB1D326DE9850F5E56A6C724B5B8E5EB5CD8D950408E95707");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "tox1.mf-net.eu",3389,"B3E5FA80DC8EBD1149AD2AB35ED8B85BD546DEDE261CA593234C619249419506");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "tox2.mf-net.eu",3389,"70EA214FDE161E7432530605213F18F7427DC773E276B3E317A07531F548545F");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "195.201.7.101",33445,"B84E865125B4EC4C368CD047C72BCE447644A2DC31EF75BD2CDA345BFD310107");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "168.138.203.178",33445,"6D04D8248E553F6F0BFDDB66FBFB03977E3EE54C432D416BC2444986EF02CC17");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "209.59.144.175",33445,"214B7FEA63227CAEC5BCBA87F7ABEEDB1A2FF6D18377DD86BF551B8E094D5F1E");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "188.225.9.167",33445,"1911341A83E02503AB1FD6561BD64AF3A9D6C3F12B5FBB656976B2E678644A67");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "122.116.39.151",3389,"5716530A10D362867C8E87EE1CD5362A233BAFBBA4CF47FA73B7CAD368BD5E6E");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "195.123.208.139",3389,"534A589BA7427C631773D13083570F529238211893640C99D1507300F055FE73");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "208.38.228.104",33445,"3634666A51CA5BE1579C031BD31B20059280EB7C05406ED466BD9DFA53373271");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "137.74.42.224",3389,"A95177FA018066CF044E811178D26B844CBF7E1E76F140095B3A1807E081A204");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "198.98.49.206",33445,"28DB44A3CEEE69146469855DFFE5F54DA567F5D65E03EFB1D38BBAEFF2553255");insert_node_into_db_real(n);num_++;
        n = BootstrapNodeEntryDB_(false, num_, "5.19.249.240",38296,"DA98A4C0CD7473A133E115FEA2EBDAEEA2EF4F79FD69325FC070DA4DE4BA3238");insert_node_into_db_real(n);num_++;
        // @formatter:on
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
            if (host_or_ip.equals("127.0.0.1"))
            {
                Log.i(TAG, "dns_lookup_via_tor:TorResolve:" + host_or_ip + " == 127.0.0.1");
                return "127.0.0.1";
            }
            else if (validate_ipv4(host_or_ip))
            {
                Log.i(TAG, "dns_lookup_via_tor:TorResolve:" + host_or_ip + " is already an IPv4 address");
                return host_or_ip;
            }

            // TODO: TorResolve can NOT resolve IPv6 address like its written now
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
        // TODO: TorResolve can NOT resolve IPv6 address like its written now
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
            out.print("Connection: Keep-Alive\r\n");
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
