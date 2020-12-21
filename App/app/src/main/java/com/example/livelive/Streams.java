package com.example.livelive;

import androidx.annotation.NonNull;
import androidx.room.Entity;

@Entity(primaryKeys = {"user_id","channelId"})
public class Streams {

    @NonNull
    String _id;

    @NonNull
    String user_id;

    @NonNull
    String channelId;

    @NonNull
    String channelName;

    public Streams(@NonNull String _id, @NonNull String user_id, @NonNull String channelId, @NonNull String channelName) {
        this._id = _id;
        this.user_id = user_id;
        this.channelId = channelId;
        this.channelName = channelName;
    }

    public Streams() {

    }

    @Override
    public String toString() {
        return "Streams{" +
                "_id='" + _id + '\'' +
                ", channelId='" + channelId + '\'' +
                ", channelName='" + channelName + '\'' +
                '}';
    }
}
