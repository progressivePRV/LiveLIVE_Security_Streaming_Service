package com.example.livelive;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class ChannelListActivity extends AppCompatActivity {

    TabLayout tabLayout;
    view_pager2_adapter adapter;
    ViewPager2 viewPager2;
    private static final String TAG = "okay";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.live_recorder_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                getApplicationContext().getSharedPreferences("TokeyKey",0)
                        .edit().clear().commit();
                Intent i = new Intent(this, MainActivity.class);
                startActivity(i);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_list);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tabLayout = findViewById(R.id.tab_layout_in_tabActivity);
        adapter = new view_pager2_adapter(this);
        viewPager2 = findViewById(R.id.view_pager2_in_tabLayout);
        viewPager2.setAdapter(adapter);
        new TabLayoutMediator(tabLayout,viewPager2, (tab, position) -> {

            switch (position){
                case 0:
                    tab.setText(R.string.all_streams);
                    break;
                case 1:
                    tab.setText(R.string.favorite_streams);
                    break;
            }
        }).attach();
    }

    @Override
    public boolean onSupportNavigateUp() {
        super.onSupportNavigateUp();
        return true;
    }
}