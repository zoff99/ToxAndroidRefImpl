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

public class NodeListJS
{

    @SerializedName("last_scan")
    @Expose
    private Integer lastScan;
    @SerializedName("last_refresh")
    @Expose
    private Integer lastRefresh;
    @SerializedName("nodes")
    @Expose
    private List<NodeJS> nodes = null;

    public Integer getLastScan()
    {
        return lastScan;
    }

    public void setLastScan(Integer lastScan)
    {
        this.lastScan = lastScan;
    }

    public Integer getLastRefresh()
    {
        return lastRefresh;
    }

    public void setLastRefresh(Integer lastRefresh)
    {
        this.lastRefresh = lastRefresh;
    }

    public List<NodeJS> getNodes()
    {
        return nodes;
    }

    public void setNodes(List<NodeJS> nodes)
    {
        this.nodes = nodes;
    }

}

