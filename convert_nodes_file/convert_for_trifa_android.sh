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

wget 'https://nodes.tox.chat/json' -O "$nodes_file"

nodes_count=$(jq .nodes[] "$nodes_file" |grep ipv4|wc -l)
echo "nodes:"$nodes_count

#  "last_ping": 1494344722,
#  "motd": "tox-bootstrapd",
#  "version": "2016010100",
#  "status_tcp": false,
#  "ipv4": "77.37.142.179",
#  "ipv6": "-",
#  "port": 33445,
#  "tcp_ports": [],
#  "public_key": "98F5830A426C6BF165F895F04B897AFC4F57331B4BE0561F583C9F323194227B",
#  "maintainer": "ps",
#  "location": "RU",
#  "status_udp": false


i=0
good_nodes_count=0
while [ $i -lt $nodes_count ]; do
        status_udp=$(jq .nodes["$i"].status_udp "$nodes_file")
        if [ "$status_udp""x" == "true""x" ]; then
                good_nodes_count=$[ $good_nodes_count + 1 ]
        fi
        i=$[ $i + 1 ]
done

echo "good nodes:"$good_nodes_count

rm -f "$outfile"

i=0
while [ $i -lt $nodes_count ]; do
        ipv4=$(jq .nodes["$i"].ipv4 "$nodes_file"|tr -d '"')
        port=$(jq .nodes["$i"].port "$nodes_file"|tr -d '"')
        pubkey=$(jq .nodes["$i"].public_key "$nodes_file"|tr -d '"')
        status_udp=$(jq .nodes["$i"].status_udp "$nodes_file"|tr -d '"')

        if [ "$status_udp""x" == "true""x" ]; then
              if [ "$ipv4""x" != "NONEx" ]; then
              
                echo -n 'n = BootstrapNodeEntryDB_(true, num_, "'"$ipv4"'",'"$port"',"'"$pubkey"'");' >> "$outfile"
                echo -n 'insert_node_into_db_real(n);' >> "$outfile"
                echo -n 'num_++;' >> "$outfile"
                echo "node #""$i:"
                if [ $[ $i + 1 ] != $good_nodes_count ]; then
                        echo -n '' >> "$outfile"
                fi
                echo '' >> "$outfile"
              fi
        else
                :
                # echo "node #""$i:"
                # echo "node not OK"
        fi

        i=$[ $i + 1 ]
done


### TCP ###


i=0
good_nodes_count=0
while [ $i -lt $nodes_count ]; do
        status_tcp=$(jq .nodes["$i"].status_tcp "$nodes_file")
        if [ "$status_tcp""x" == "true""x" ]; then
                good_nodes_count=$[ $good_nodes_count + 1 ]
        fi
        i=$[ $i + 1 ]
done

echo "good nodes:"$good_nodes_count

echo "" >> "$outfile"
echo "" >> "$outfile"

i=0
while [ $i -lt $nodes_count ]; do
        ipv4=$(jq .nodes["$i"].ipv4 "$nodes_file"|tr -d '"')

        ports_count=$(jq '.nodes['"$i"'].tcp_ports|length' "$nodes_file"|tr -d '"')
        # echo $ports_count

        if [ "$ports_count""x" != "0x" ]; then
            for tcp_port_index in $(seq 0 $(("$ports_count"-1)) ) ; do
                port=$(jq .nodes["$i"].tcp_ports["$tcp_port_index"] "$nodes_file"|tr -d '"')
                # echo "$tcp_port_index"" -> ""$port"
                if [ "$port""x" == "443x" ]; then
                    break
                fi

                if [ "$port""x" == "80x" ]; then
                    break
                fi

                if [ "$port""x" == "53x" ]; then
                    break
                fi

                if [ "$port""x" == "25x" ]; then
                    break
                fi
            done

            pubkey=$(jq .nodes["$i"].public_key "$nodes_file"|tr -d '"')
            status_tcp=$(jq .nodes["$i"].status_tcp "$nodes_file"|tr -d '"')

            if [ "$status_tcp""x" == "true""x" ]; then
                if [ "$port""x" != "x" ]; then
                  if [ "$ipv4""x" != "NONEx" ]; then
                    echo -n 'n = BootstrapNodeEntryDB_(false, num_, "'"$ipv4"'",'"$port"',"'"$pubkey"'");' >> "$outfile"
                    echo -n 'insert_node_into_db_real(n);' >> "$outfile"
                    echo -n 'num_++;' >> "$outfile"
                    echo "node #""$i:"" -> ""$port"
                    if [ $[ $i + 1 ] != $good_nodes_count ]; then
                            echo -n '' >> "$outfile"
                    fi
                    echo '' >> "$outfile"
                  fi
                fi
            else
                    :
                    # echo "node #""$i:"
                    # echo "node not OK"
            fi
        fi

        i=$[ $i + 1 ]
done
