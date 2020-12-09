package com.example.liveliverecorder;

import java.io.Serializable;
import java.util.ArrayList;

public class Admin implements Serializable {

//    "_id": "5fcf15b50499937785ed29c6",
//            "username": "new_admin",
//            "channelId": "789011",
//            "channelName": "new channel",
//            "users": [
//            "aditi@gmail.com",
//            "anirban@gmail.com"
//            ],
//            "isBroadcasting": false,
//            "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJfaWQiOiI1ZmNmMTViNTA0OTk5Mzc3ODVlZDI5YzYiLCJleHAiOjE2MDc1ODkwNjAsImlhdCI6MTYwNzUwMjY2MH0.oot0F9pBwtFKhaVVnHd3lXkGT2_B4YiPHlVLUfWqQS8"

    String _id, username, channelId, channelName, token;
    ArrayList<String> userList = new ArrayList<>();
    boolean isBroadcasting;



    @Override
    public String toString() {
        return "Admin{" +
                "_id='" + _id + '\'' +
                ", username='" + username + '\'' +
                ", channelId='" + channelId + '\'' +
                ", channelName='" + channelName + '\'' +
                ", token='" + token + '\'' +
                ", userList=" + userList +
                ", isBroadcasting=" + isBroadcasting +
                '}';
    }
}
