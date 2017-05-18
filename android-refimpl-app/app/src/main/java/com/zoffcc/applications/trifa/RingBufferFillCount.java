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