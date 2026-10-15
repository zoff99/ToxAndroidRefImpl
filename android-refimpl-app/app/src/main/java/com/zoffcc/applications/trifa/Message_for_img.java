package com.zoffcc.applications.trifa;

import com.zoffcc.applications.sorm.Message;

public class Message_for_img extends Message
{
    private String file_uri;

    public Message_for_img() {
        super();
    }

    public String getfile_uri() {
        return file_uri;
    }

    public void setfile_uri(String file_uri) {
        this.file_uri = file_uri;
    }
}
