package com.example.liveliverecorder;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.transition.Explode;
import android.transition.Fade;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ChannelInfo extends AppCompatActivity implements UserListAdapter.InteractWithRecyclerView {

    private static final String TAG = "okay_ChannelInfo";
    private static final int EDIT_CHANNEL_REQUEST_CODE = 1111;
    TextView channelName;
    ChipGroup chipGroup;
    ListView listView;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    Admin admin;

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

        ///////////////// get ui components
        channelName =  findViewById(R.id.channel_name_inChannelInfo);
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
                Intent i = new Intent(ChannelInfo.this,MainActivity.class);
                i.putExtra("Admin_Obj",admin);
                startActivity(i);
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
        recyclerView = (RecyclerView) findViewById(R.id.listview_inChannelInfo);
        layoutManager = new LinearLayoutManager(ChannelInfo.this);
        recyclerView.setLayoutManager(layoutManager);
        // specify an adapter (see also next example)
        Log.d("demo"," List of the users : " +admin.users.toString());
        mAdapter = new UserListAdapter(admin.users, ChannelInfo.this, false);
        recyclerView.setAdapter(mAdapter);
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
}