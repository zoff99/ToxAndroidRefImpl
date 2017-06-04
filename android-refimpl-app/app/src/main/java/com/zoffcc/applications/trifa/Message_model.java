package com.zoffcc.applications.trifa;


public class Message_model
{
    public static final int TEXT_INCOMING_NOT_READ = 1001;
    public static final int TEXT_INCOMING_HAVE_READ = 1002;

    public static final int TEXT_OUTGOING_NOT_READ = 2001;
    public static final int TEXT_OUTGOING_HAVE_READ = 2002;

    public static final int FILE_INCOMING_STATE_CANCEL = 3001;
    public static final int FILE_INCOMING_STATE_PAUSE_NOT_YET_ACCEPTED = 3002;
    public static final int FILE_INCOMING_STATE_PAUSE_HAS_ACCEPTED = 3003;
    public static final int FILE_INCOMING_STATE_RESUME = 3004;

    public static final int FILE_OUTGOING = 4001;

    public static final int ERROR_UNKNOWN = 9999;
}
