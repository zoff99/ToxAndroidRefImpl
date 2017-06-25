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

import java.util.HashMap;

/**
 * Assistant class to resolve well-known (reserved) ports
 */
public class KnownPorts
{


    private static HashMap<Integer, String> m;

    //get protocol desc based on port number
    public static String resolvePort(int port)
    {
        if (m.containsKey(port))
        {
            return m.get(port);
        }
        else
        {
            return "unknown";
        }
    }

    public static String CompileConnectionInfo(int remotePort, TLType type)
    {
        String info;
        if (isTlsPort(remotePort))
        {
            info = Const.STATUS_TLS;
        }
        else if (isUnsecurePort(remotePort))
        {
            info = Const.STATUS_UNSECURE;
        }
        else if (isInconclusivePort(remotePort))
        {
            info = Const.STATUS_INCONCLUSIVE;
        }
        else
        {
            info = Const.STATUS_UNKNOWN;
        }

        return info + " (" + resolvePort(remotePort) + ", " + type + ")";
    }

    //Test if port number is well known for TLS connection
    public static boolean isTlsPort(int i)
    {
        return Const.TLS_PORTS.contains(i);
    }

    //Test if port number is inconclusive (e.g. STARTTLS)
    public static boolean isInconclusivePort(int i)
    {
        return Const.INCONCUSIVE_PORTS.contains(i);
    }

    //Test if port number is well known for unencrypted connection
    public static boolean isUnsecurePort(int i)
    {
        return Const.UNSECURE_PORTS.contains(i);
    }


    //init hash map with reserved ports (1-1024) and protocol identifiers
    //based on: http://www.iana.org/assignments/service-names-port-numbers/service-names-port-numbers.xhtml
    public static void initPortMap()
    {
        m = new HashMap<>();

        m.put(1, "tcpmux");
        m.put(2, "compressnet");
        m.put(3, "compressnet");
        m.put(5, "rje");
        m.put(7, "echo");
        m.put(9, "discard");
        m.put(11, "systat");
        m.put(13, "daytime");
        m.put(17, "qotd");
        m.put(18, "msp");
        m.put(19, "chargen");
        m.put(20, "ftp-data");
        m.put(21, "ftp");
        m.put(22, "ssh");
        m.put(23, "telnet");
        m.put(25, "smtp");
        m.put(27, "nsw-fe");
        m.put(29, "msg-icp");
        m.put(31, "msg-auth");
        m.put(33, "dsp");
        m.put(37, "time");
        m.put(38, "rap");
        m.put(39, "rlp");
        m.put(41, "graphics");
        m.put(42, "name");
        m.put(43, "nicname");
        m.put(44, "mpm-flags");
        m.put(45, "mpm");
        m.put(46, "mpm-snd");
        m.put(47, "ni-ftp");
        m.put(48, "auditd");
        m.put(49, "tacacs");
        m.put(50, "re-mail-ck");
        m.put(52, "xns-time");
        m.put(53, "domain");
        m.put(54, "xns-ch");
        m.put(55, "isi-gl");
        m.put(56, "xns-auth");
        m.put(58, "xns-mail");
        m.put(61, "ni-mail");
        m.put(62, "acas");
        m.put(63, "whoispp");
        m.put(64, "covia");
        m.put(65, "tacacs-ds");
        m.put(66, "sql-net");
        m.put(67, "bootps");
        m.put(68, "bootpc");
        m.put(69, "tftp");
        m.put(70, "gopher");
        m.put(71, "netrjs-1");
        m.put(72, "netrjs-2");
        m.put(73, "netrjs-3");
        m.put(74, "netrjs-4");
        m.put(76, "deos");
        m.put(78, "vettcp");
        m.put(79, "finger");
        m.put(80, "http");
        m.put(82, "xfer");
        m.put(83, "mit-ml-dev");
        m.put(84, "ctf");
        m.put(85, "mit-ml-dev");
        m.put(86, "mfcobol");
        m.put(88, "kerberos");
        m.put(89, "su-mit-tg");
        m.put(90, "dnsix");
        m.put(91, "mit-dov");
        m.put(92, "npp");
        m.put(93, "dcp");
        m.put(94, "objcall");
        m.put(95, "supdup");
        m.put(96, "dixie");
        m.put(97, "swift-rvf");
        m.put(98, "tacnews");
        m.put(99, "metagram");
        m.put(101, "hostname");
        m.put(102, "iso-tsap");
        m.put(103, "gppitnp");
        m.put(104, "acr-nema");
        m.put(105, "cso");
        m.put(106, "3com-tsmux");
        m.put(107, "rtelnet");
        m.put(108, "snagas");
        m.put(109, "pop2");
        m.put(110, "pop3");
        m.put(111, "sunrpc");
        m.put(112, "mcidas");
        m.put(113, "ident");
        m.put(115, "sftp");
        m.put(116, "ansanotify");
        m.put(117, "uucp-path");
        m.put(118, "sqlserv");
        m.put(119, "nntp");
        m.put(120, "cfdptkt");
        m.put(121, "erpc");
        m.put(122, "smakynet");
        m.put(123, "ntp");
        m.put(124, "ansatrader");
        m.put(125, "locus-map");
        m.put(126, "nxedit");
        m.put(127, "locus-con");
        m.put(128, "gss-xlicen");
        m.put(129, "pwdgen");
        m.put(130, "cisco-fna");
        m.put(131, "cisco-tna");
        m.put(132, "cisco-sys");
        m.put(133, "statsrv");
        m.put(134, "ingres-net");
        m.put(135, "epmap");
        m.put(136, "profile");
        m.put(137, "netbios-ns");
        m.put(138, "netbios-dgm");
        m.put(139, "netbios-ssn");
        m.put(140, "emfis-data");
        m.put(141, "emfis-cntl");
        m.put(142, "bl-idm");
        m.put(143, "imap");
        m.put(144, "uma");
        m.put(145, "uaac");
        m.put(146, "iso-tp0");
        m.put(147, "iso-ip");
        m.put(148, "jargon");
        m.put(149, "aed-512");
        m.put(150, "sql-net");
        m.put(151, "hems");
        m.put(152, "bftp");
        m.put(153, "sgmp");
        m.put(154, "netsc-prod");
        m.put(155, "netsc-dev");
        m.put(156, "sqlsrv");
        m.put(157, "knet-cmp");
        m.put(158, "pcmail-srv");
        m.put(159, "nss-routing");
        m.put(160, "sgmp-traps");
        m.put(161, "snmp");
        m.put(162, "snmptrap");
        m.put(163, "cmip-man");
        m.put(164, "cmip-agent");
        m.put(165, "xns-courier");
        m.put(166, "s-net");
        m.put(167, "namp");
        m.put(168, "rsvd");
        m.put(169, "send");
        m.put(170, "print-srv");
        m.put(171, "multiplex");
        m.put(172, "cl-1");
        m.put(173, "xyplex-mux");
        m.put(174, "mailq");
        m.put(175, "vmnet");
        m.put(176, "genrad-mux");
        m.put(177, "xdmcp");
        m.put(178, "nextstep");
        m.put(179, "bgp");
        m.put(180, "ris");
        m.put(181, "unify");
        m.put(182, "audit");
        m.put(183, "ocbinder");
        m.put(184, "ocserver");
        m.put(185, "remote-kis");
        m.put(186, "kis");
        m.put(187, "aci");
        m.put(188, "mumps");
        m.put(189, "qft");
        m.put(190, "gacp");
        m.put(191, "prospero");
        m.put(192, "osu-nms");
        m.put(193, "srmp");
        m.put(194, "irc");
        m.put(195, "dn6-nlm-aud");
        m.put(196, "dn6-smm-red");
        m.put(197, "dls");
        m.put(198, "dls-mon");
        m.put(199, "smux");
        m.put(200, "src");
        m.put(201, "at-rtmp");
        m.put(202, "at-nbp");
        m.put(203, "at-3");
        m.put(204, "at-echo");
        m.put(205, "at-5");
        m.put(206, "at-zis");
        m.put(207, "at-7");
        m.put(208, "at-8");
        m.put(209, "qmtp");
        m.put(210, "z39-50");
        m.put(211, "914c-g");
        m.put(212, "anet");
        m.put(213, "ipx");
        m.put(214, "vmpwscs");
        m.put(215, "softpc");
        m.put(216, "CAIlic");
        m.put(217, "dbase");
        m.put(218, "mpp");
        m.put(219, "uarps");
        m.put(220, "imap3");
        m.put(221, "fln-spx");
        m.put(222, "rsh-spx");
        m.put(223, "cdc");
        m.put(224, "masqdialer");
        m.put(242, "direct");
        m.put(243, "sur-meas");
        m.put(244, "inbusiness");
        m.put(245, "link");
        m.put(246, "dsp3270");
        m.put(247, "subntbcst-tftp");
        m.put(248, "bhfhs");
        m.put(256, "rap");
        m.put(257, "set");
        m.put(259, "esro-gen");
        m.put(260, "openport");
        m.put(261, "nsiiops");
        m.put(262, "arcisdms");
        m.put(263, "hdap");
        m.put(264, "bgmp");
        m.put(265, "x-bone-ctl");
        m.put(266, "sst");
        m.put(267, "td-service");
        m.put(268, "td-replica");
        m.put(269, "manet");
        m.put(270, "gist");
        m.put(271, "pt-tls");
        m.put(280, "http-mgmt");
        m.put(281, "personal-link");
        m.put(282, "cableport-ax");
        m.put(283, "rescap");
        m.put(284, "corerjd");
        m.put(286, "fxp");
        m.put(287, "k-block");
        m.put(308, "novastorbakcup");
        m.put(309, "entrusttime");
        m.put(310, "bhmds");
        m.put(311, "asip-webadmin");
        m.put(312, "vslmp");
        m.put(313, "magenta-logic");
        m.put(314, "opalis-robot");
        m.put(315, "dpsi");
        m.put(316, "decauth");
        m.put(317, "zannet");
        m.put(318, "pkix-timestamp");
        m.put(319, "ptp-event");
        m.put(320, "ptp-general");
        m.put(321, "pip");
        m.put(322, "rtsps");
        m.put(323, "rpki-rtr");
        m.put(324, "rpki-rtr-tls");
        m.put(333, "texar");
        m.put(344, "pdap");
        m.put(345, "pawserv");
        m.put(346, "zserv");
        m.put(347, "fatserv");
        m.put(348, "csi-sgwp");
        m.put(349, "mftp");
        m.put(350, "matip-type-a");
        m.put(351, "matip-type-b");
        m.put(352, "dtag-ste-sb");
        m.put(353, "ndsauth");
        m.put(354, "bh611");
        m.put(355, "datex-asn");
        m.put(356, "cloanto-net-1");
        m.put(357, "bhevent");
        m.put(358, "shrinkwrap");
        m.put(359, "nsrmp");
        m.put(360, "scoi2odialog");
        m.put(361, "semantix");
        m.put(362, "srssend");
        m.put(363, "rsvp-tunnel");
        m.put(364, "aurora-cmgr");
        m.put(365, "dtk");
        m.put(366, "odmr");
        m.put(367, "mortgageware");
        m.put(368, "qbikgdp");
        m.put(369, "rpc2portmap");
        m.put(370, "codaauth2");
        m.put(371, "clearcase");
        m.put(372, "ulistproc");
        m.put(373, "legent-1");
        m.put(374, "legent-2");
        m.put(375, "hassle");
        m.put(376, "nip");
        m.put(377, "tnETOS");
        m.put(378, "dsETOS");
        m.put(379, "is99c");
        m.put(380, "is99s");
        m.put(381, "hp-collector");
        m.put(382, "hp-managed-node");
        m.put(383, "hp-alarm-mgr");
        m.put(384, "arns");
        m.put(385, "ibm-app");
        m.put(386, "asa");
        m.put(387, "aurp");
        m.put(388, "unidata-ldm");
        m.put(389, "ldap");
        m.put(390, "uis");
        m.put(391, "synotics-relay");
        m.put(392, "synotics-broker");
        m.put(393, "meta5");
        m.put(394, "embl-ndt");
        m.put(395, "netcp");
        m.put(396, "netware-ip");
        m.put(397, "mptn");
        m.put(398, "kryptolan");
        m.put(399, "iso-tsap-c2");
        m.put(400, "osb-sd");
        m.put(401, "ups");
        m.put(402, "genie");
        m.put(403, "decap");
        m.put(404, "nced");
        m.put(405, "ncld");
        m.put(406, "imsp");
        m.put(407, "timbuktu");
        m.put(408, "prm-sm");
        m.put(409, "prm-nm");
        m.put(410, "decladebug");
        m.put(411, "rmt");
        m.put(412, "synoptics-trap");
        m.put(413, "smsp");
        m.put(414, "infoseek");
        m.put(415, "bnet");
        m.put(416, "silverplatter");
        m.put(417, "onmux");
        m.put(418, "hyper-g");
        m.put(419, "ariel1");
        m.put(420, "smpte");
        m.put(421, "ariel2");
        m.put(422, "ariel3");
        m.put(423, "opc-job-start");
        m.put(424, "opc-job-track");
        m.put(425, "icad-el");
        m.put(426, "smartsdp");
        m.put(427, "svrloc");
        m.put(428, "ocs-cmu");
        m.put(429, "ocs-amu");
        m.put(430, "utmpsd");
        m.put(431, "utmpcd");
        m.put(432, "iasd");
        m.put(433, "nnsp");
        m.put(434, "mobileip-agent");
        m.put(435, "mobilip-mn");
        m.put(436, "dna-cml");
        m.put(437, "comscm");
        m.put(438, "dsfgw");
        m.put(439, "dasp");
        m.put(440, "sgcp");
        m.put(441, "decvms-sysmgt");
        m.put(442, "cvc-hostd");
        m.put(443, "https");
        m.put(444, "snpp");
        m.put(445, "microsoft-ds");
        m.put(446, "ddm-rdb");
        m.put(447, "ddm-dfm");
        m.put(448, "ddm-ssl");
        m.put(449, "as-servermap");
        m.put(450, "tserver");
        m.put(451, "sfs-smp-net");
        m.put(452, "sfs-config");
        m.put(453, "creativeserver");
        m.put(454, "contentserver");
        m.put(455, "creativepartnr");
        m.put(456, "macon-tcp");
        m.put(457, "scohelp");
        m.put(458, "appleqtc");
        m.put(459, "ampr-rcmd");
        m.put(460, "skronk");
        m.put(461, "datasurfsrv");
        m.put(462, "datasurfsrvsec");
        m.put(463, "alpes");
        m.put(464, "kpasswd");
        m.put(465, "urd");
        m.put(466, "digital-vrc");
        m.put(467, "mylex-mapd");
        m.put(468, "photuris");
        m.put(469, "rcp");
        m.put(470, "scx-proxy");
        m.put(471, "mondex");
        m.put(472, "ljk-login");
        m.put(473, "hybrid-pop");
        m.put(474, "tn-tl-w1");
        m.put(475, "tcpnethaspsrv");
        m.put(476, "tn-tl-fd1");
        m.put(477, "ss7ns");
        m.put(478, "spsc");
        m.put(479, "iafserver");
        m.put(480, "iafdbase");
        m.put(481, "ph");
        m.put(482, "bgs-nsi");
        m.put(483, "ulpnet");
        m.put(484, "integra-sme");
        m.put(485, "powerburst");
        m.put(486, "avian");
        m.put(487, "saft");
        m.put(488, "gss-http");
        m.put(489, "nest-protocol");
        m.put(490, "micom-pfs");
        m.put(491, "go-login");
        m.put(492, "ticf-1");
        m.put(493, "ticf-2");
        m.put(494, "pov-ray");
        m.put(495, "intecourier");
        m.put(496, "pim-rp-disc");
        m.put(497, "retrospect");
        m.put(498, "siam");
        m.put(499, "iso-ill");
        m.put(500, "isakmp");
        m.put(501, "stmf");
        m.put(502, "mbap");
        m.put(503, "intrinsa");
        m.put(504, "citadel");
        m.put(505, "mailbox-lm");
        m.put(506, "ohimsrv");
        m.put(507, "crs");
        m.put(508, "xvttp");
        m.put(509, "snare");
        m.put(510, "fcp");
        m.put(511, "passgo");
        m.put(512, "exec");
        m.put(513, "login");
        m.put(514, "shell");
        m.put(515, "printer");
        m.put(516, "videotex");
        m.put(517, "talk");
        m.put(518, "ntalk");
        m.put(519, "utime");
        m.put(520, "efs");
        m.put(521, "ripng");
        m.put(522, "ulp");
        m.put(523, "ibm-db2");
        m.put(524, "ncp");
        m.put(525, "timed");
        m.put(526, "tempo");
        m.put(527, "stx");
        m.put(528, "custix");
        m.put(529, "irc-serv");
        m.put(530, "courier");
        m.put(531, "conference");
        m.put(532, "netnews");
        m.put(533, "netwall");
        m.put(534, "windream");
        m.put(535, "iiop");
        m.put(536, "opalis-rdv");
        m.put(537, "nmsp");
        m.put(538, "gdomap");
        m.put(539, "apertus-ldp");
        m.put(540, "uucp");
        m.put(541, "uucp-rlogin");
        m.put(542, "commerce");
        m.put(543, "klogin");
        m.put(544, "kshell");
        m.put(545, "appleqtcsrvr");
        m.put(546, "dhcpv6-client");
        m.put(547, "dhcpv6-server");
        m.put(548, "afpovertcp");
        m.put(549, "idfp");
        m.put(550, "new-rwho");
        m.put(551, "cybercash");
        m.put(552, "devshr-nts");
        m.put(553, "pirp");
        m.put(554, "rtsp");
        m.put(555, "dsf");
        m.put(556, "remotefs");
        m.put(557, "openvms-sysipc");
        m.put(558, "sdnskmp");
        m.put(559, "teedtap");
        m.put(560, "rmonitor");
        m.put(561, "monitor");
        m.put(562, "chshell");
        m.put(563, "nntps");
        m.put(564, "9pfs");
        m.put(565, "whoami");
        m.put(566, "streettalk");
        m.put(567, "banyan-rpc");
        m.put(568, "ms-shuttle");
        m.put(569, "ms-rome");
        m.put(570, "meter");
        m.put(571, "meter");
        m.put(572, "sonar");
        m.put(573, "banyan-vip");
        m.put(574, "ftp-agent");
        m.put(575, "vemmi");
        m.put(576, "ipcd");
        m.put(577, "vnas");
        m.put(578, "ipdd");
        m.put(579, "decbsrv");
        m.put(580, "sntp-heartbeat");
        m.put(581, "bdp");
        m.put(582, "scc-security");
        m.put(583, "philips-vc");
        m.put(584, "keyserver");
        m.put(586, "password-chg");
        m.put(587, "submission");
        m.put(588, "cal");
        m.put(589, "eyelink");
        m.put(590, "tns-cml");
        m.put(591, "http-alt");
        m.put(592, "eudora-set");
        m.put(593, "http-rpc-epmap");
        m.put(594, "tpip");
        m.put(595, "cab-protocol");
        m.put(596, "smsd");
        m.put(597, "ptcnameservice");
        m.put(598, "sco-websrvrmg3");
        m.put(599, "acp");
        m.put(600, "ipcserver");
        m.put(601, "syslog-conn");
        m.put(602, "xmlrpc-beep");
        m.put(603, "idxp");
        m.put(604, "tunnel");
        m.put(605, "soap-beep");
        m.put(606, "urm");
        m.put(607, "nqs");
        m.put(608, "sift-uft");
        m.put(609, "npmp-trap");
        m.put(610, "npmp-local");
        m.put(611, "npmp-gui");
        m.put(612, "hmmp-ind");
        m.put(613, "hmmp-op");
        m.put(614, "sshell");
        m.put(615, "sco-inetmgr");
        m.put(616, "sco-sysmgr");
        m.put(617, "sco-dtmgr");
        m.put(618, "dei-icda");
        m.put(619, "compaq-evm");
        m.put(620, "sco-websrvrmgr");
        m.put(621, "escp-ip");
        m.put(622, "collaborator");
        m.put(623, "oob-ws-http");
        m.put(624, "cryptoadmin");
        m.put(625, "dec-dlm");
        m.put(626, "asia");
        m.put(627, "passgo-tivoli");
        m.put(628, "qmqp");
        m.put(629, "3com-amp3");
        m.put(630, "rda");
        m.put(631, "ipp");
        m.put(632, "bmpp");
        m.put(633, "servstat");
        m.put(634, "ginad");
        m.put(635, "rlzdbase");
        m.put(636, "ldaps");
        m.put(637, "lanserver");
        m.put(638, "mcns-sec");
        m.put(639, "msdp");
        m.put(640, "entrust-sps");
        m.put(641, "repcmd");
        m.put(642, "esro-emsdp");
        m.put(643, "sanity");
        m.put(644, "dwr");
        m.put(645, "pssc");
        m.put(646, "ldp");
        m.put(647, "dhcp-failover");
        m.put(648, "rrp");
        m.put(649, "cadview-3d");
        m.put(650, "obex");
        m.put(651, "ieee-mms");
        m.put(652, "hello-port");
        m.put(653, "repscmd");
        m.put(654, "aodv");
        m.put(655, "tinc");
        m.put(656, "spmp");
        m.put(657, "rmc");
        m.put(658, "tenfold");
        m.put(660, "mac-srvr-admin");
        m.put(661, "hap");
        m.put(662, "pftp");
        m.put(663, "purenoise");
        m.put(664, "oob-ws-https");
        m.put(665, "sun-dr");
        m.put(666, "mdqs");
        m.put(667, "disclose");
        m.put(668, "mecomm");
        m.put(669, "meregister");
        m.put(670, "vacdsm-sws");
        m.put(671, "vacdsm-app");
        m.put(672, "vpps-qua");
        m.put(673, "cimplex");
        m.put(674, "acap");
        m.put(675, "dctp");
        m.put(676, "vpps-via");
        m.put(677, "vpp");
        m.put(678, "ggf-ncp");
        m.put(679, "mrm");
        m.put(680, "entrust-aaas");
        m.put(681, "entrust-aams");
        m.put(682, "xfr");
        m.put(683, "corba-iiop");
        m.put(684, "corba-iiop-ssl");
        m.put(685, "mdc-portmapper");
        m.put(686, "hcp-wismar");
        m.put(687, "asipregistry");
        m.put(688, "realm-rusd");
        m.put(689, "nmap");
        m.put(690, "vatp");
        m.put(691, "msexch-routing");
        m.put(692, "hyperwave-isp");
        m.put(693, "connendp");
        m.put(694, "ha-cluster");
        m.put(695, "ieee-mms-ssl");
        m.put(696, "rushd");
        m.put(697, "uuidgen");
        m.put(698, "olsr");
        m.put(699, "accessnetwork");
        m.put(700, "epp");
        m.put(701, "lmp");
        m.put(702, "iris-beep");
        m.put(704, "elcsd");
        m.put(705, "agentx");
        m.put(706, "silc");
        m.put(707, "borland-dsj");
        m.put(709, "entrust-kmsh");
        m.put(710, "entrust-ash");
        m.put(711, "cisco-tdp");
        m.put(712, "tbrpf");
        m.put(713, "iris-xpc");
        m.put(714, "iris-xpcs");
        m.put(715, "iris-lwz");
        m.put(716, "pana");
        m.put(729, "netviewdm1");
        m.put(730, "netviewdm2");
        m.put(731, "netviewdm3");
        m.put(741, "netgw");
        m.put(742, "netrcs");
        m.put(744, "flexlm");
        m.put(747, "fujitsu-dev");
        m.put(748, "ris-cm");
        m.put(749, "kerberos-adm");
        m.put(750, "rfile");
        m.put(751, "pump");
        m.put(752, "qrh");
        m.put(753, "rrh");
        m.put(754, "tell");
        m.put(758, "nlogin");
        m.put(759, "con");
        m.put(760, "ns");
        m.put(761, "rxe");
        m.put(762, "quotad");
        m.put(763, "cycleserv");
        m.put(764, "omserv");
        m.put(765, "webster");
        m.put(767, "phonebook");
        m.put(769, "vid");
        m.put(770, "cadlock");
        m.put(771, "rtip");
        m.put(772, "cycleserv2");
        m.put(773, "submit");
        m.put(774, "rpasswd");
        m.put(775, "entomb");
        m.put(776, "wpages");
        m.put(777, "multiling-http");
        m.put(780, "wpgs");
        m.put(800, "mdbs-daemon");
        m.put(801, "device");
        m.put(802, "mbap-s");
        m.put(810, "fcp-udp");
        m.put(828, "itm-mcell-s");
        m.put(829, "pkix-3-ca-ra");
        m.put(830, "netconf-ssh");
        m.put(831, "netconf-beep");
        m.put(832, "netconfsoaphttp");
        m.put(833, "netconfsoapbeep");
        m.put(847, "dhcp-failover2");
        m.put(848, "gdoi");
        m.put(853, "domain-s");
        m.put(860, "iscsi");
        m.put(861, "owamp-control");
        m.put(862, "twamp-control");
        m.put(873, "rsync");
        m.put(886, "iclcnet-locate");
        m.put(887, "iclcnet-svinfo");
        m.put(888, "accessbuilder");
        m.put(900, "omginitialrefs");
        m.put(901, "smpnameres");
        m.put(902, "ideafarm-door");
        m.put(903, "ideafarm-panic");
        m.put(910, "kink");
        m.put(911, "xact-backup");
        m.put(912, "apex-mesh");
        m.put(913, "apex-edge");
        m.put(989, "ftps-data");
        m.put(990, "ftps");
        m.put(991, "nas");
        m.put(992, "telnets");
        m.put(993, "imaps");
        m.put(995, "pop3s");
        m.put(996, "vsinet");
        m.put(997, "maitrd");
        m.put(998, "busboy");
        m.put(999, "garcon");
        m.put(1000, "cadlock2");
        m.put(1001, "webpush");
        m.put(1010, "surf");
        m.put(1021, "exp1");
        m.put(1022, "exp2");
    }


}