package com.example.liveliverecorder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

public class ChannelInfo extends AppCompatActivity implements UserListAdapter.InteractWithRecyclerView {

    private static final String TAG = "okay_ChannelInfo";
    TextView channelName;
    ListView listView;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    Admin admin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_info);

        ///////////////// get ui components
        channelName =  findViewById(R.id.channel_name_inChannelInfo);
//        listView = findViewById(R.id.listview_inChannelInfo);

        ///////////////// getting intent data
        Intent i = getIntent();
        if (i.hasExtra("Admin_Obj")){
            admin = (Admin) i.getExtras().getSerializable("Admin_Obj");
            Log.d(TAG, "onCreate: got the data from Intent");
        }else{
            Log.d(TAG, "onCreate: didn't got any intent data");
        }

        Log.d("demo","The values of admin are : "+admin.toString());
        ///////////////// set values in ui
        channelName.setText(admin.channelName);
        // listView set here

        recyclerView = (RecyclerView) findViewById(R.id.listview_inChannelInfo);

        layoutManager = new LinearLayoutManager(ChannelInfo.this);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        Log.d("demo"," List of the users : " +admin.users.toString());
        mAdapter = new UserListAdapter(admin.users, ChannelInfo.this, false);
        recyclerView.setAdapter(mAdapter);

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
                startActivity(i);
            }
        });

        findViewById(R.id.delete_img_inChannelInfo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent i = new Intent(ChannelInfo.this,);
                startActivity(i);
            }
        });
    }


    @Override
    public void deleteItem(int position) {

    }
}