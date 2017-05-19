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

public class RingBufferFillCount
{
    public Object[] elements = null;

    private int capacity = 0;
    private int writePos = 0;
    private int available = 0;

    public RingBufferFillCount(int capacity)
    {
        this.capacity = capacity;
        this.elements = new Object[capacity];
    }

    public void reset()
    {
        this.writePos = 0;
        this.available = 0;
    }

    public int capacity()
    {
        return this.capacity;
    }

    public int available()
    {
        return this.available;
    }

    public int remainingCapacity()
    {
        return this.capacity - this.available;
    }

    public boolean put(Object element)
    {

        if (available < capacity)
        {
            if (writePos >= capacity)
            {
                writePos = 0;
            }
            elements[writePos] = element;
            writePos++;
            available++;
            return true;
        }

        return false;
    }

    public Object take()
    {
        if (available == 0)
        {
            return null;
        }
        int nextSlot = writePos - available;
        if (nextSlot < 0)
        {
            nextSlot += capacity;
        }
        Object nextObj = elements[nextSlot];
        available--;
        return nextObj;
    }
}