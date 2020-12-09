package com.example.liveliverecorder;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements Helper.InteractWithActivity {

    private static final String TAG = "okay_MainActivity";
    Helper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_up_stream).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSFUUpstream();
            }
        });
    }

    void startSFUUpstream()  {
        helper = new Helper(this,this,findViewById(R.id.preview_container));
        helper.SetChannelId("Test002");
        helper.SetUserId("this_is_a_up_stream_user");
        helper.GetClientToken();
        try {
            helper.RegisterTheClient();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "startSFUUpstream: exception msg=>"+e.getMessage());
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
        try {
            helper.InitializeLocalMedia();
            helper.StartLocalMediaCapture();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "ChannelJoined: exception msg=>" + e.getMessage());
//                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (helper==null){
            return;
        }
        try {
            helper.CloseSFUConnections();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "onStop: exception msg=>"+e.getMessage());
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

//    @Override
//    protected void onStop() {
//        super.onStop();
//
//    }
}