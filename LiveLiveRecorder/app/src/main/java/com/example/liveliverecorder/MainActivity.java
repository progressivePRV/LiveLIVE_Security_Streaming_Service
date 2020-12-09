package com.example.liveliverecorder;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements Helper.InteractWithActivity {

    private static final String TAG = "okay_MainActivity";
    Helper helper;
    Admin admin;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ///////////////// getting intent data
        Intent i = getIntent();
        if (i.hasExtra("Admin_Obj")){
            admin = (Admin) i.getSerializableExtra("Admin_Obj");
            Log.d(TAG, "onCreate: got the data from Intent");
        }else{
            Log.d(TAG, "onCreate: didn't got any intent data");
        }


        Log.d(TAG, "onCreate: calling start SFU Up Stream");
        startSFUUpstream();



//        findViewById(R.id.btn_up_stream).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startSFUUpstream();
//            }
//        });
    }

    void startSFUUpstream()  {
        showProgressBarDialog();
        helper = new Helper(this,this,findViewById(R.id.preview_container));
        helper.SetChannelId(admin.channelId);
        helper.SetUserId(admin._id);
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
            hideProgressBarDialog();
            finish();
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
            hideProgressBarDialog();
            finish();
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
        hideProgressBarDialog();
//        finish();
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
            showProgressBarDialog();
            helper.CloseSFUConnections();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "onStop: exception msg=>"+e.getMessage());
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            hideProgressBarDialog();
            finish();
        }
    }

    //for showing the progress dialog
    public void showProgressBarDialog()
    {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    //for hiding the progress dialog
    public void hideProgressBarDialog()
    {
        progressDialog.dismiss();
    }

//    @Override
//    protected void onStop() {
//        super.onStop();
//
//    }
}