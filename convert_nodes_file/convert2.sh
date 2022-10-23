#! /bin/bash
#
# [TRIfA], shellscript part of Tox Reference Implementation for Android
# Copyright (C) 2017 - 2019 Zoff <zoff@zoff.cc>
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
outfile="./nodes.c"

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
                echo -n '{"'"$ipv4"'","'"$pubkey"'",'"$port"','"$port"'}' >> "$outfile"
                if [ $[ $i + 1 ] != $good_nodes_count ]; then
                        echo -n ',' >> "$outfile"
                fi
                echo '' >> "$outfile"
        else
                echo "node #""$i:"
                echo "node not OK"
        fi

        i=$[ $i + 1 ]
done
