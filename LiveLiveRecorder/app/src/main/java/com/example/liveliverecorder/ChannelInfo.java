package com.example.liveliverecorder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

public class ChannelInfo extends AppCompatActivity {

    private static final String TAG = "okay_ChannelInfo";
    TextView channelName;
    ListView listView;
    Admin admin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_info);

        ///////////////// get ui components
        channelName =  findViewById(R.id.channel_name_inChannelInfo);
        listView = findViewById(R.id.listview_inChannelInfo);

        ///////////////// getting intent data
        Intent i = getIntent();
        if (i.hasExtra("Admin_Obj")){
            admin = (Admin) i.getSerializableExtra("Admin_Obj");
            Log.d(TAG, "onCreate: got the data from Intent");
        }else{
            Log.d(TAG, "onCreate: didn't got any intent data");
        }

        ///////////////// set values in ui
        channelName.setText(admin.channelName);
        // listView set here



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
//                Intent i = new Intent(ChannelInfo.this,);
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




}