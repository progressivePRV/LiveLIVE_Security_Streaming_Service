package com.example.livelive;

import android.app.Application;
import android.database.sqlite.SQLiteConstraintException;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class AppViewModel extends AndroidViewModel {

    private static final String TAG = "okay";
    StreamsDAO streamsDAO;


    public AppViewModel(@NonNull Application application) {
        super(application);
        RoomDataBase db = RoomDataBase.getDatabase(application);
        streamsDAO = db.streamsDAO();
    }

    public void InsertStream(Streams streams){
        new InsertStream(streamsDAO).execute(streams);
    }

    public LiveData<List<Streams>> GetStreamsForUser(String uid){
        return streamsDAO.FindAllStreamsForUser(uid);
    }
    public void DeleteStream(Streams streams){
        new DeleteStream(streamsDAO).execute(streams);
    }

    public Streams findStreamWhereIdAndUserId(String id,String uid){
        return streamsDAO.findStreamWhereIdAndUserId(id,uid);
    }

    class InsertStream extends AsyncTask<Streams,Void,Void> {
        StreamsDAO dao;
        String result="",error="";
        public InsertStream(StreamsDAO dao) {
            this.dao = dao;
        }
        @Override
        protected Void doInBackground(Streams... streams) {
            Log.d(TAG, "doInBackground: calling for insert orders in App View Model");
            try{
                dao.insert(streams[0]);
                result = "Insert query successful";
            }catch (SQLiteConstraintException e){
                error = "Id should be unique for insert query";
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.d(TAG, "onPostExecute: InsertStreams called called");
            if (error.isEmpty()){
                Toast.makeText(getApplication(), result, Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getApplication(), error, Toast.LENGTH_SHORT).show();
            }
        }
    }


    class DeleteStream extends AsyncTask<Streams,Void,Void> {
        StreamsDAO dao;
        public DeleteStream(StreamsDAO dao) {
            this.dao = dao;
        }
        @Override
        protected Void doInBackground(Streams... streams) {
            int i = dao.delete(streams[0]);
            Log.d(TAG, "doInBackground: "+i+" rows deleted");
            return null;
        }
    }

}
