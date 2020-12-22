package com.example.listtestapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String a[] = new String[] { "aditi", "anirban", "monesa", "prabhav","shweta","nidhi","venu","anisha" };

        List<String> list = Arrays.asList(a);

        ListView namesList = findViewById(R.id.listView);
        TextView tv = findViewById(R.id.textView);

        ArrayAdapter<String> namesAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,android.R.id.text1,list);
        namesList.setAdapter(namesAdapter);

        namesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                tv.setText(list.get(i));
            }
        });

    }
}