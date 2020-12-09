package com.example.livelive;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements Helper.InteractWithActivity {

    private EditText editTextChannelId;
    private static final String TAG = "okay_MainActivity";
//    boolean isUpStreamRequested = false;
    Helper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate: called");
        editTextChannelId = findViewById(R.id.editTextChannelId);

        findViewById(R.id.btn_downStream).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: starting SFU Down stream");
//                isUpStreamRequested = false;
                StartSFUDownStream();
            }
        });

//        findViewById(R.id.btn_upstream).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d(TAG, "onClick: start SFU Up Stream");
//                isUpStreamRequested = true;
//                startSFUUpstream();
//            }
//        });


    }

//    void startSFUUpstream()  {
//        helper = new Helper(this,this,findViewById(R.id.preivew_container_inMain));
//        helper.SetChannelId("Test001");
//        helper.SetUserId("this_is_a_up_stream_user");
//        helper.GetClientToken();
//        try {
//            helper.RegisterTheClient();
//        } catch (Exception e) {
//            e.printStackTrace();
//            Log.d(TAG, "startSFUUpstream: exception msg=>"+e.getMessage());
//            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
//        }
//    }

    void StartSFUDownStream(){
        helper = new Helper(this,this,findViewById(R.id.preivew_container_inMain));
        String channel_id = editTextChannelId.getText().toString();
        if(channel_id.equals("")){
            Toast.makeText(this, "Please enter a channel name to downstream", Toast.LENGTH_SHORT).show();
        }else{
            helper.SetChannelId(channel_id);
            helper.SetUserId("this_is_a_Down_stream_user");
            helper.GetClientToken();
            try {
                helper.RegisterTheClient();
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "StartSFUDownStream: exception msg=>"+e.getMessage());
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
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
        helper.CreateSFU_DownStreamConnection();
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
    protected void onStop() {
        super.onStop();
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
}