package com.example.liveliverecorder;

import java.util.ArrayList;

public class Channel {
   String channelName;
   String channelId;
   ArrayList<String> users;

    public Channel() {
    }

    public String getChannelName() {
        return channelName;
    }

    public String getChannelId() {
        return channelId;
    }

    public ArrayList<String> getUsers() {
        return users;
    }
}
