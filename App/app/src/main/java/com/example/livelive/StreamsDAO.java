package com.example.livelive;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface StreamsDAO {

    @Insert
    void insert(Streams streams);

    @Delete
    int delete(Streams deleteStream);

    @Query("Select * from `Streams` where user_id=:user_id and channelId=:channel_id")
    Streams findStreamWhereIdAndUserId(String user_id, String channel_id);

    @Query("Select * from `Streams` where user_id=:user_id")
    LiveData<List<Streams>> FindAllStreamsForUser(String user_id);

}
