package com.example.livelive;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements Helper.InteractWithActivity {

    private static final String TAG = "okay_MainActivity";
    private ProgressDialog progressDialog;
//    boolean isUpStreamRequested = false;
    Helper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate: called");

        showProgressBarDialog();
        new getStreamDetails().execute();


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

    void StartSFUDownStream(String sharedSecret, String applicationId){
        helper = new Helper(this,this,findViewById(R.id.preivew_container_inMain));
        String channel_id = getIntent().getExtras().getString("channelId");

            helper.SetChannelId(channel_id);
            helper.SetUserId("this_is_a_Down_stream_user");
            helper.GetClientToken(sharedSecret, applicationId);
            try {
                helper.RegisterTheClient();
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
        helper.CreateSFU_DownStreamConnection();
    }

    @Override
    public void ConnectionClosed() {
        Toast.makeText(this, "Video is Failing or remote connection is closed", Toast.LENGTH_SHORT).show();
        finish();
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
                        StartSFUDownStream(sharedSecret, applicationId);
                    }else{
                        Toast.makeText(MainActivity.this, "Error occured in fetching streaming details. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
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
}