package com.example.livelive;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ChannelListActivity extends AppCompatActivity {

    TabLayout tabLayout;
    view_pager2_adapter adapter;
    ViewPager2 viewPager2;
    private static final String TAG = "okay";
    /// update approach one
    List<Streams> streamsList = new ArrayList<>();
    private SharedPreferences preferences;
    Gson gson = new Gson();

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
                AlertDialog.Builder builder = new AlertDialog.Builder(ChannelListActivity.this);
                builder.setMessage("Are you sure you want to logout?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Log.d(TAG, "onClick: user clicked ok for alert");
                                getApplicationContext().getSharedPreferences("TokeyKey",0)
                                        .edit().clear().commit();
                                Intent i = new Intent(ChannelListActivity.this, LoginActivity.class);
                                startActivity(i);
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
        preferences = getApplicationContext().getSharedPreferences("TokeyKey",0);
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

    void GetUsersStreamChannels(String end_url)
    {
        new getUsersStreamChannels(getResources().getString(R.string.endPointUrl)+end_url).execute();
    }
    public class getUsersStreamChannels extends AsyncTask<String, Void, String> {
        boolean isStatus = true;
        String getChannelUrl;

        public getUsersStreamChannels(String getChannelUrl) {
            this.getChannelUrl = getChannelUrl;
        }

        @Override
        protected String doInBackground(String... strings) {

            final OkHttpClient client = new OkHttpClient();

//            String getChannelUrl =  getResources().getString(R.string.endPointUrl)+"api/v1/user/channels";

            Log.d("demo", "entering do in Background");
            Request request = new Request.Builder()
                    .url(getChannelUrl)
                    .header("Authorization", "Bearer "+ preferences.getString("TOKEN_KEY", null))
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
            streamsList.clear();
            Log.d("streaming broadcasting : ", s);
            if(s!=null){
                JSONArray root = null;
                try {
                    if(isStatus){
                        root = new JSONArray(s);
                        String pro =  preferences.getString("USER",null);
                        User user = gson.fromJson(pro, User.class);
                        for(int i=0; i<root.length(); i++){
                            JSONObject channelDetails = root.getJSONObject(i);
                            Streams streams = new Streams();
                            streams._id = channelDetails.getString("_id");
                            streams.channelId = channelDetails.getString("channelId");
                            streams.channelName = channelDetails.getString("channelName");
                            streams.user_id = user._id;
                            streamsList.add(streams);
                        }
                    }else{
                        Toast.makeText(ChannelListActivity.this, new JSONObject(s).getString("error"), Toast.LENGTH_SHORT).show();
                    }

                    Log.d("demo",streamsList.toString());
//                    mAdapter.notifyDataSetChanged();
                    //////////// approach one
                    adapter.getAll_stream_fragment().UpdateChannelList(streamsList);
                    adapter.getFavorite_streams_fragment().UpdateChannelList(streamsList);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}