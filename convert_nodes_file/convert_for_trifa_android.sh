#! /bin/bash
#
# [TRIfA], shellscript part of Tox Reference Implementation for Android
# Copyright (C) 2021 Zoff <zoff@zoff.cc>
#
# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License
# version 2 as published by the Free Software Foundation.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the
# Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
# Boston, MA  02110-1301, USA.
# 

nodes_file="./nodes.json"
outfile="./nodes2.java"

# wget 'https://nodes.tox.chat/json' -O "$nodes_file"

nodes_count=$(jq .nodes[] "$nodes_file" | grep ipv4 |wc -l)
echo "nodes:"$nodes_count


nodes_count4=$(jq .nodes[] "$nodes_file" |grep ipv4|grep -v '"NONE",'|wc -l)
echo "nodes4:"$nodes_count4

nodes_count6=$(jq .nodes[] "$nodes_file" |grep ipv6|grep -v '"-",'|wc -l)
echo "nodes6:"$nodes_count6


rm -f "$outfile"



### UDP ###
echo ""
echo ""
echo "UDP:"
echo ""
echo ""

i=0
good_nodes_count=0
while [ $i -lt $nodes_count ]; do
        status_udp=$(jq .nodes["$i"].status_udp "$nodes_file")
        if [ "$status_udp""x" == "true""x" ]; then
                echo "DD:000:node:$i"
                good_nodes_count=$[ $good_nodes_count + 1 ]

                ipv4=$(jq .nodes["$i"].ipv4 "$nodes_file"|tr -d '"')
                ipv6=$(jq .nodes["$i"].ipv6 "$nodes_file"|tr -d '"')
                port=$(jq .nodes["$i"].port "$nodes_file"|tr -d '"')
                pubkey=$(jq .nodes["$i"].public_key "$nodes_file"|tr -d '"')

                echo "DD:001:$ipv6 $ipv4 $port $pubkey"

                echo -n 'n = BootstrapNodeEntryDB_(true, num_, "'"$ipv4"'",'"$port"',"'"$pubkey"'");' >> "$outfile"
                echo -n 'insert_node_into_db_real(n);' >> "$outfile"
                echo -n 'num_++;' >> "$outfile"
                echo '' >> "$outfile"

                if [ "$ipv6""x" != "-""x" ]; then
                    if [[ $ipv6 = 2* ]] ; then
                        echo "DD:002:ipv6"
                        echo -n 'n = BootstrapNodeEntryDB_(true, num_, "'"$ipv6"'",'"$port"',"'"$pubkey"'");' >> "$outfile"
                        echo -n 'insert_node_into_db_real(n);' >> "$outfile"
                        echo -n 'num_++;' >> "$outfile"
                    else
                        # lookup ipv6 address
                        ipv6_lookup=$(dig AAAA +short "$ipv6" 2>/dev/null|tail -1 2>/dev/null)
                        echo "DD:003:ipv6"
                        if [[ $ipv6_lookup = 2* ]] ; then
                            echo "DD:004:ipv6 is $ipv6_lookup"
                            echo -n 'n = BootstrapNodeEntryDB_(true, num_, "'"$ipv6_lookup"'",'"$port"',"'"$pubkey"'");' >> "$outfile"
                            echo -n 'insert_node_into_db_real(n);' >> "$outfile"
                            echo -n 'num_++;' >> "$outfile"
                        else
                            echo "DD:005:ipv6 address seems broken $ipv6_lookup"
                        fi
                    fi
                fi

                echo '' >> "$outfile"
        fi
        i=$[ $i + 1 ]
done




echo "// ================" >> "$outfile"
echo "// ================" >> "$outfile"





### TCP ###
echo ""
echo ""
echo "TCP:"
echo ""
echo ""


i=0
good_nodes_count=0
while [ $i -lt $nodes_count4 ]; do
        status_tcp=$(jq .nodes["$i"].status_tcp "$nodes_file")
        if [ "$status_tcp""x" == "true""x" ]; then
                echo "DD:101:node:$i"
                good_nodes_count4=$[ $good_nodes_count + 1 ]


                ipv4=$(jq .nodes["$i"].ipv4 "$nodes_file"|tr -d '"')
                ipv6=$(jq .nodes["$i"].ipv6 "$nodes_file"|tr -d '"')
                ports=$(jq .nodes["$i"].tcp_ports "$nodes_file"|tr -d '"')
                pubkey=$(jq .nodes["$i"].public_key "$nodes_file"|tr -d '"')
                ports_count=$(jq '.nodes['"$i"'].tcp_ports|length' "$nodes_file"|tr -d '"')

                echo "DD:102:$ports len=$ports_count"

                if [ "$ports_count""x" != "0x" ]; then
                    for tcp_port_index in $(seq 0 $(("$ports_count"-1)) ) ; do
                        port=$(jq .nodes["$i"].tcp_ports["$tcp_port_index"] "$nodes_file"|tr -d '"')
                        echo "DD:103:port=$port"


                        if [ "$port""x" != "x" ]; then

                          if [ "$ipv4""x" != "NONEx" ]; then
                            echo -n 'n = BootstrapNodeEntryDB_(false, num_, "'"$ipv4"'",'"$port"',"'"$pubkey"'");' >> "$outfile"
                            echo -n 'insert_node_into_db_real(n);' >> "$outfile"
                            echo -n 'num_++;' >> "$outfile"
                            echo "DD:104:node #""$i:"" ""$ipv4"" "" -> ""$port"
                            if [ $[ $i + 1 ] != $good_nodes_count ]; then
                                    echo -n '' >> "$outfile"
                            fi
                            echo '' >> "$outfile"
                          fi


                            if [ "$ipv6""x" != "-""x" ]; then
                                echo "DD:105:ipv6"
                                if [[ $ipv6 = 2* ]] ; then
                                    echo -n 'n = BootstrapNodeEntryDB_(false, num_, "'"$ipv6"'",'"$port"',"'"$pubkey"'");' >> "$outfile"
                                    echo -n 'insert_node_into_db_real(n);' >> "$outfile"
                                    echo -n 'num_++;' >> "$outfile"
                                    echo "DD:106:ipv6"
                                else
                                    # lookup ipv6 address
                                    ipv6_lookup=$(dig AAAA +short "$ipv6" 2>/dev/null|tail -1 2>/dev/null)
                                    if [[ $ipv6_lookup = 2* ]] ; then
                                        echo "DD:107:ipv6 is $ipv6_lookup"
                                        echo -n 'n = BootstrapNodeEntryDB_(false, num_, "'"$ipv6_lookup"'",'"$port"',"'"$pubkey"'");' >> "$outfile"
                                        echo -n 'insert_node_into_db_real(n);' >> "$outfile"
                                        echo -n 'num_++;' >> "$outfile"
                                    else
                                        echo "DD:108:ipv6 address seems broken $ipv6_lookup"
                                    fi
                                fi
                                echo '' >> "$outfile"
                            fi


                        fi



                    done
                fi

        fi
        i=$[ $i + 1 ]
done

