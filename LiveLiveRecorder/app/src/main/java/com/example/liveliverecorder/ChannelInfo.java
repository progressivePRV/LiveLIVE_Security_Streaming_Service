package com.example.liveliverecorder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.transition.Explode;
import android.transition.Fade;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChannelInfo extends AppCompatActivity implements UserListAdapter.InteractWithRecyclerView {

    private static final String TAG = "okay_ChannelInfo";
    private static final int EDIT_CHANNEL_REQUEST_CODE = 1111;
    TextView channelName;
    ChipGroup chipGroup;
    ListView listView;
//    private RecyclerView recyclerView;
//    private RecyclerView.Adapter mAdapter;
    private ProgressDialog progressDialog;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
//    private RecyclerView.LayoutManager layoutManager;
    Admin admin;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.live_recorder_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout:
                AlertDialog.Builder builder = new AlertDialog.Builder(ChannelInfo.this);
                builder.setMessage("Are you sure you want to logout?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Log.d(TAG, "onClick: user clicked ok for alert");
                                preferences = getApplicationContext().getSharedPreferences("AdminTokenKey",0);
                                editor = preferences.edit();
                                editor.clear();
                                editor.commit();
                                Intent intent = new Intent(ChannelInfo.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onClick: user canceled the alert from notification");
                    }
                });
                builder.create();
                builder.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ///////// setting up the animation
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        getWindow().setEnterTransition(new Fade());
        getWindow().setExitTransition(new Explode());
//        getWindow().setSharedElementEnterTransition();
//        getWindow().setSharedElementExitTransition();
        /////////

        setContentView(R.layout.activity_channel_info);
        Log.d(TAG, "onCreate: called");
        preferences = getApplicationContext().getSharedPreferences("AdminTokenKey",0);


        ///////////////// get ui components
        channelName =  findViewById(R.id.channel_name_inChannelInfo);
//        channelName.setBackgroundResource(R.color.colorPrimary);
        chipGroup = findViewById(R.id.chip_group_in_channelInfo);
//        listView = findViewById(R.id.listview_inChannelInfo);

        ///////////////// getting intent data
        Intent i = getIntent();
        if (i.hasExtra("Admin_Obj")){
            admin = (Admin) i.getExtras().getSerializable("Admin_Obj");
            Log.d(TAG, "onCreate: got the data from Intent");
            //////////// adding user chips
            UpdateChips();
            ///////////
        }else{
            Log.d(TAG, "onCreate: didn't got any intent data");
        }

        Log.d("demo","The values of admin are : "+admin.toString());
        ///////////////// set values in ui
        SetValuesInUI();


        findViewById(R.id.btn_start_stream_inChannelInfo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //setting the broadcasting status to true here
                    showProgressBarDialog();
                    new setBroadcastingStatus().execute();
            }
        });

        findViewById(R.id.edit_img_inChannelInfo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ChannelInfo.this,CreateNewChannelActivity.class);
                i.putExtra("admin",admin);
                startActivityForResult(i,EDIT_CHANNEL_REQUEST_CODE);
            }
        });

        findViewById(R.id.delete_img_inChannelInfo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DeleteChannel().execute();
//                Intent i = new Intent(ChannelInfo.this,);
//                startActivity(i);
            }
        });


    }

    private void UpdateChips() {
//        View.OnClickListener onChipClick =  new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Log.d(TAG, "onClick: "+((Chip)view).getText()+" chip");
//                int index = (int) view.getTag();
//                admin.users.remove(index);
//                chipGroup.removeViewAt(index);
//            }
//        };
        chipGroup.removeAllViews();
        for(int i=0;i<admin.users.size();i++){
            Chip c1 =  new Chip(this);
            c1.setText(admin.users.get(i));
//            c1.setBackgroundColor();
            c1.setChipBackgroundColorResource(R.color.colorAccent);
//            c1.setChipBackgroundColor(ColorStateList.valueOf(R.color.colorAccent));
            c1.setTag(i);
//            c1.setCloseIconVisible(true);
//            c1.setOnCloseIconClickListener(onChipClick);
            chipGroup.addView(c1,i);
        }
    }

    private void SetValuesInUI() {
        Log.d(TAG, "SetValuesInUI: called");
        channelName.setText(admin.channelName);
//        recyclerView = (RecyclerView) findViewById(R.id.listview_inChannelInfo);
//        layoutManager = new LinearLayoutManager(ChannelInfo.this);
//        recyclerView.setLayoutManager(layoutManager);
//        // specify an adapter (see also next example)
//        Log.d("demo"," List of the users : " +admin.users.toString());
//        mAdapter = new UserListAdapter(admin.users, ChannelInfo.this, false);
//        recyclerView.setAdapter(mAdapter);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d(TAG, "onActivityResult:  called");
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==RESULT_OK && requestCode == EDIT_CHANNEL_REQUEST_CODE){
            admin  = (Admin) data.getSerializableExtra("admin");
            //////////// adding user chips
            UpdateChips();
            ///////////
            SetValuesInUI();
        }
    }

    class DeleteChannel extends AsyncTask<Void,Void,Void> {
        boolean isSuccessful = false;
        String result1 = "";
        @Override
        protected Void doInBackground(Void... voids) {
            String token = getApplicationContext().getSharedPreferences("AdminTokenKey",0).getString("TOKEN_KEY",null);
            // if needed check for token null
            final OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(getResources().getString(R.string.endPointUrl)+"api/v1/admin/channels")//206.81.0.65:3000/api/v1/admin/channels
                    .header("Authorization", "Bearer " + token)
                    .delete()
                    .build();
            try (Response response = client.newCall(request).execute()) {
                result1 = response.body().string();
                Log.d(TAG, "doInBackground: login response=>"+result1);
                if (response.isSuccessful()){
                    isSuccessful = true;
                }else{
                    isSuccessful = false;
                }
//                result1 = response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            JSONObject root = null;
            Log.d("demo",result1);
            try {
                root = new JSONObject(result1);
                if (isSuccessful) {
                    Toast.makeText(ChannelInfo.this, root.getString("result"), Toast.LENGTH_SHORT).show();
                    Intent o =  new Intent(ChannelInfo.this,CreateNewChannelActivity.class);
                    startActivity(o);
                    finish();
                }else{
                    Toast.makeText(ChannelInfo.this, root.getString("error"), Toast.LENGTH_SHORT).show();
                }

            }catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void deleteItem(int position) {

    }

    public class setBroadcastingStatus extends AsyncTask<String, Void, String> {
        boolean isStatus = true;

        @Override
        protected String doInBackground(String... strings) {
            Log.d(TAG, "streaming broadcasting: called for setBroadcastingStatus");
            final OkHttpClient client = new OkHttpClient();

//            RequestBody formBody = new FormBody.Builder()
//                    .add("isBroadcasting","true")
//                    .build();
            MediaType MEDIA_TYPE_JSON
                    = MediaType.parse("application/json");

            String post = "{\"isBroadcasting\":true}";

            Request request = new Request.Builder()
                    .url(getResources().getString(R.string.endPointUrl)+"api/v1/admin/broadcasting")
                    .header("Authorization", "Bearer "+ preferences.getString("TOKEN_KEY", null))
                    .put(RequestBody.create(post,MEDIA_TYPE_JSON))
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
                            //It means broadcasting status has been updated. So the user can actually go and broadcast the stream
                            Intent i = new Intent(ChannelInfo.this,MainActivity.class);
                            i.putExtra("Admin_Obj",admin);
                            startActivity(i);
                        }
                    }else{
                        Toast.makeText(ChannelInfo.this, root.getString("error"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void showProgressBarDialog()
    {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public void hideProgressBarDialog()
    {
        progressDialog.dismiss();
    }
}