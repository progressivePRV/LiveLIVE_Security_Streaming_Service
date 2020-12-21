package com.example.livelive;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Streams.class}, version = 2, exportSchema = false)
public abstract class RoomDataBase extends RoomDatabase {

    private static final String TAG = "okay";

    public abstract StreamsDAO streamsDAO();

    private static RoomDataBase instance;

    static RoomDataBase getDatabase(final Context ctx){
        if (instance == null){
            synchronized (RoomDataBase.class){
                if (instance == null){
                    instance = Room.databaseBuilder(ctx.getApplicationContext(), RoomDataBase.class,"room_database")
                            // wipes and rebuilds instead of migrating
                            // if no migration object
                            .fallbackToDestructiveMigration()
                            .addCallback(roomCallback)
                            .build();
                }
            }
        }
        return instance;
    }

    private static RoomDatabase.Callback roomCallback =  new RoomDatabase.Callback(){
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            Log.d(TAG, "onOpen: Room database is opened");
        }
    };

}