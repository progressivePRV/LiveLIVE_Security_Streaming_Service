package com.example.livelive;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements Helper.InteractWithActivity {

    private static final String TAG = "okay_MainActivity";
    boolean isUpStreamRequested = false;
    Helper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate: called");
        findViewById(R.id.btn_downStream).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: starting SFU Down stream");
                isUpStreamRequested = false;
                StartSFUDownStream();
            }
        });

        findViewById(R.id.btn_upstream).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: start SFU Up Stream");
                isUpStreamRequested = true;
                startSFUUpstream();
            }
        });


    }

    void startSFUUpstream()  {
        helper = new Helper(this,this,findViewById(R.id.preivew_container_inMain));
        helper.SetChannelId("Test001");
        helper.SetUserId("this_is_a_up_stream_user");
        helper.GetClientToken();
        try {
            helper.RegisterTheClient();
//            new RegisterTheClient().execute();
//            helper.JoinChannel();
//            helper.InitializeLocalMedia();
//            helper.StartLocalMediaCapture();
//            // setting up the preview is done in helper.StartLocalMediaCapture();
//            helper.CreateSFU_UpStreamConnection();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "startSFUUpstream: exception msg=>"+e.getMessage());
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    void StartSFUDownStream(){
        helper = new Helper(this,this,findViewById(R.id.preivew_container_inMain));
        helper.SetChannelId("Test001");
        helper.SetUserId("this_is_a_Down_stream_user");
        helper.GetClientToken();
        try {
            helper.RegisterTheClient();
//            new RegisterTheClient().execute();
//            helper.JoinChannel();
//            helper.InitializeLocalMedia();
//            helper.StartLocalMediaCapture();
//            // setting up the preview is done in helper.StartLocalMediaCapture();
//            helper.CreateSFU_DownStreamConnection();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "StartSFUDownStream: exception msg=>"+e.getMessage());
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void ClientRegistered() {
        // after registering you need to join the channel
        try {
            helper.JoinChannel();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "ClientRegistered: exception msg=>" + e.getMessage());
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void ChannelJoined() {
        if (isUpStreamRequested){
            // sending data from this device
            try {
                helper.InitializeLocalMedia();
                helper.StartLocalMediaCapture();
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "ChannelJoined: exception msg=>" + e.getMessage());
//                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }else{
            helper.CreateSFU_DownStreamConnection();
        }
    }

    @Override
    public void StartedLocalMediaCapture() {
        // send media to up stream
        helper.CreateSFU_UpStreamConnection();
    }

    @Override
    public void CreatedSFU_UpStreamConnection() {

    }

    @Override
    public void CreatedSFU_DownStreamConnection() {

    }

//    class RegisterTheClient extends AsyncTask<Void,Void,Void>{
//        boolean isSuccessful = false;
//        @Override
//        protected Void doInBackground(Void... voids) {
//            try {
//                helper.RegisterTheClient();
//                isSuccessful = true;
//            } catch (Exception e) {
//                Log.d(TAG, "doInBackground: RegisterTheClient exception msg=>"+e.getMessage());
//                e.printStackTrace();
//            }
//            return null;
//        }
//        @Override
//        protected void onPostExecute(Void aVoid) {
//            super.onPostExecute(aVoid);
//            if (isSuccessful){
////                call join channel
//                new JoinChannel().execute();
//            }
//        }
//    }
//
//    class JoinChannel extends AsyncTask<Void,Void,Void> {
//        boolean isSuccessful = false;
//        @Override
//        protected Void doInBackground(Void... voids) {
//            try {
//                helper.JoinChannel();
//            } catch (Exception e) {
//                e.printStackTrace();
//                Log.d(TAG, "doInBackground: JoinChannel exception msg=>"+e.getMessage());
//            }
//            return null;
//        }
//        @Override
//        protected void onPostExecute(Void aVoid) {
//            super.onPostExecute(aVoid);
//            if (isSuccessful){
//                if (isUpStreamRequested){
////                    call start local media
////                    call satrt up stream
//                    new StartLocalMedia().execute();
//                }else{
////                    call down stream
//                    new StartSFUDownStream().execute();
//                }
//            }
//        }
//    }
//
//    class StartSFUDownStream extends AsyncTask<Void,Void,Void>{
//        boolean isSuccessful = false;
//        @Override
//        protected Void doInBackground(Void... voids) {
//            helper.CreateSFU_DownStreamConnection();
//            return null;
//        }
//    }
//
//    class StartLocalMedia extends AsyncTask<Void,Void,Void>{
//        boolean isSuccessful = false;
//        @Override
//        protected Void doInBackground(Void... voids) {
//            try {
//                helper.InitializeLocalMedia();
//                helper.StartLocalMediaCapture();
//            } catch (Exception e) {
//                e.printStackTrace();
//                Log.d(TAG, "doInBackground: StartLocalMedia exception msg=>"+e.getMessage());
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void aVoid) {
//            super.onPostExecute(aVoid);
//            if (isSuccessful){
////                call up stream
//                new StartSFUUpStream().execute();
//            }
//        }
//    }
//
//    class StartSFUUpStream extends AsyncTask<Void,Void,Void>{
//        boolean isSuccessful = false;
//        @Override
//        protected Void doInBackground(Void... voids) {
//            helper.CreateSFU_UpStreamConnection();
//            return null;
//        }
//    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            helper.CloseSFUConnections();
            helper.stopLocalMediaCapture();
            helper.LeaveAChannel();
            helper.UnRegisterTheClient();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "onStop: exception msg=>"+e.getMessage());
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}