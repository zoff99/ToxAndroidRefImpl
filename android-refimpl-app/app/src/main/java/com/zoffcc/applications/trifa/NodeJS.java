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

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class NodeJS
{

    @SerializedName("ipv4")
    @Expose
    private String ipv4;
    @SerializedName("ipv6")
    @Expose
    private String ipv6;
    @SerializedName("port")
    @Expose
    private Integer port;
    @SerializedName("tcp_ports")
    @Expose
    private List<Integer> tcpPorts = null;
    @SerializedName("public_key")
    @Expose
    private String publicKey;
    @SerializedName("maintainer")
    @Expose
    private String maintainer;
    @SerializedName("location")
    @Expose
    private String location;
    @SerializedName("status_udp")
    @Expose
    private Boolean statusUdp;
    @SerializedName("status_tcp")
    @Expose
    private Boolean statusTcp;
    @SerializedName("version")
    @Expose
    private String version;
    @SerializedName("motd")
    @Expose
    private String motd;
    @SerializedName("last_ping")
    @Expose
    private Integer lastPing;

    public String getIpv4()
    {
        return ipv4;
    }

    public void setIpv4(String ipv4)
    {
        this.ipv4 = ipv4;
    }

    public String getIpv6()
    {
        return ipv6;
    }

    public void setIpv6(String ipv6)
    {
        this.ipv6 = ipv6;
    }

    public Integer getPort()
    {
        return port;
    }

    public void setPort(Integer port)
    {
        this.port = port;
    }

    public List<Integer> getTcpPorts()
    {
        return tcpPorts;
    }

    public void setTcpPorts(List<Integer> tcpPorts)
    {
        this.tcpPorts = tcpPorts;
    }

    public String getPublicKey()
    {
        return publicKey;
    }

    public void setPublicKey(String publicKey)
    {
        this.publicKey = publicKey;
    }

    public String getMaintainer()
    {
        return maintainer;
    }

    public void setMaintainer(String maintainer)
    {
        this.maintainer = maintainer;
    }

    public String getLocation()
    {
        return location;
    }

    public void setLocation(String location)
    {
        this.location = location;
    }

    public Boolean getStatusUdp()
    {
        return statusUdp;
    }

    public void setStatusUdp(Boolean statusUdp)
    {
        this.statusUdp = statusUdp;
    }

    public Boolean getStatusTcp()
    {
        return statusTcp;
    }

    public void setStatusTcp(Boolean statusTcp)
    {
        this.statusTcp = statusTcp;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    public String getMotd()
    {
        return motd;
    }

    public void setMotd(String motd)
    {
        this.motd = motd;
    }

    public Integer getLastPing()
    {
        return lastPing;
    }

    public void setLastPing(Integer lastPing)
    {
        this.lastPing = lastPing;
    }

}
