package com.example.liveliverecorder;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import fm.liveswitch.android.Camera2Source;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements Helper.InteractWithActivity {

    private static final String TAG = "okay_MainActivity";
    Helper helper;
    Admin admin;
    private ProgressDialog progressDialog;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ///////////////// getting intent data
        Intent i = getIntent();
        if (i.hasExtra("Admin_Obj")){
            admin = (Admin) i.getSerializableExtra("Admin_Obj");
            Log.d(TAG, "onCreate: got the data from Intent : "+admin.toString());
        }else{
            Log.d(TAG, "onCreate: didn't got any intent data");
        }

        preferences = getApplicationContext().getSharedPreferences("AdminTokenKey",0);
        Log.d(TAG, "onCreate: calling start SFU Up Stream");

        //Getting all the application ID, Shared Secret and the gateway URL using the API key then starting the upstream

        showProgressBarDialog();
        new getStreamDetails().execute();


        findViewById(R.id.Flip_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helper.FlipTheCamera();
            }
        });

        findViewById(R.id.stop_broadcast_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startSFUUpstream();
//                onDestroy();

                //Updating the broadcast status and then will finish the screen
                showProgressBarDialog();
                new setBroadcastingStatus().execute();
            }
        });
    }

    void startSFUUpstream(String sharedSecret, String applicationId)  {
        showProgressBarDialog();
        helper = new Helper(this,this,findViewById(R.id.preview_container));
//        helper.SetChannelId("test001");
//        helper.SetUserId("user_test");
        helper.SetChannelId(admin.channelId);
        helper.SetUserId(admin._id);
        helper.GetClientToken(sharedSecret, applicationId);
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
        Log.d(TAG, "onDestroy: called");
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


    //Async task for getting the stream details from Liveswitch
    public class getStreamDetails extends AsyncTask<String, Void, String> {
        boolean isStatus = true;


        @Override
        protected String doInBackground(String... strings) {
            final OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("https://api.liveswitch.io/ApplicationConfigs")
                    .header("X-API-Key", "83-42-34-66-19-9c-d9-e7-5c-d1-a2-88-8c-25-eb-6b")
                    .build();
            String responseValue = null;
            try (Response response = client.newCall(request).execute()) {
                if(response.isSuccessful()){
                    isStatus = true;
                }else{
                    isStatus = false;
                }
                Log.d("demo"," "+response.isSuccessful());
                responseValue = response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return responseValue;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //Log.d("demo",s);
            if(s!=null){
                JSONObject root = null;
                try {
                    root = new JSONObject(s);
                    hideProgressBarDialog();
                    if(isStatus){
                        JSONArray streamingDetails = root.getJSONArray("value");
                        JSONObject details = (JSONObject) streamingDetails.get(0);
                        Log.d("details", details.toString());
                        String sharedSecret = details.getString("sharedSecret");
                        String applicationId = details.getString("applicationId");

                        //starting the upstream here
                        startSFUUpstream(sharedSecret, applicationId);
                    }else{
                        Toast.makeText(MainActivity.this, "Error occured in fetching streaming details. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class setBroadcastingStatus extends AsyncTask<String, Void, String> {
        boolean isStatus = true;

        @Override
        protected String doInBackground(String... strings) {
            final OkHttpClient client = new OkHttpClient();

            RequestBody formBody = new FormBody.Builder()
                    .add("isBroadcasting","false")
                    .build();

            Request request = new Request.Builder()
                    .url(getResources().getString(R.string.endPointUrl)+"api/v1/admin/broadcasting")
                    .header("Authorization", "Bearer "+ preferences.getString("TOKEN_KEY", null))
                    .put(formBody)
                    .build();

            String responseValue = null;
            try (Response response = client.newCall(request).execute()) {
                if(response.isSuccessful()){
                    isStatus = true;
                }else{
                    isStatus = false;
                }
                Log.d("demo"," "+response.isSuccessful());
                responseValue = response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return responseValue;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d("streaming broadcasting : ", s);
            if(s!=null){
                JSONObject root = null;
                try {
                    root = new JSONObject(s);
                    hideProgressBarDialog();
                    if(isStatus){
                        if(root.getString("result").equals("broadcasting updated")){
                            //It means broadcasting status has been updated. So the live stream can be ended now.
                            finish();
                        }
                    }else{
                        Toast.makeText(MainActivity.this, "Error occurred in starting the live stream. Please click on the start stream again.", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        //Updating the broadcast status and then will finish the screen
        showProgressBarDialog();
        new setBroadcastingStatus().execute();
    }
}