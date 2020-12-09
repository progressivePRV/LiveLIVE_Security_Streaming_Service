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
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CreateNewChannelActivity extends AppCompatActivity implements UserListAdapter.InteractWithRecyclerView {

    private EditText editTextUserName;
    private RecyclerView recyclerView;
    private ProgressDialog progressDialog;
    private RecyclerView.Adapter mAdapter;
    SharedPreferences preferences;
    Gson gson = new Gson();
    private RecyclerView.LayoutManager layoutManager;
    private Button buttonEditStream, buttonCreateStream;
    private EditText editTextStreamName;
    ArrayList<String> userArrayList = new ArrayList<>();
    Admin admin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_channel);

        if(getIntent().hasExtra("admin")){
            admin  = (Admin) getIntent().getExtras().getSerializable("admin");
        }

        editTextUserName = findViewById(R.id.editTextUserName);
        editTextStreamName = findViewById(R.id.editTextStreamName);
        buttonEditStream = findViewById(R.id.buttonEditStream);
        buttonCreateStream = findViewById(R.id.buttonCreateStream);

        if(admin != null){
            userArrayList = admin.users;
            editTextStreamName.setText(admin.channelName);
            buttonCreateStream.setVisibility(Button.INVISIBLE);
            buttonEditStream.setVisibility(Button.VISIBLE);
        }else{
            editTextStreamName.setText("");
            buttonCreateStream.setVisibility(Button.VISIBLE);
            buttonEditStream.setVisibility(Button.INVISIBLE);
        }

        recyclerView = (RecyclerView) findViewById(R.id.usersRecyclerView);

        layoutManager = new LinearLayoutManager(CreateNewChannelActivity.this);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        mAdapter = new UserListAdapter(userArrayList, CreateNewChannelActivity.this, true);
        recyclerView.setAdapter(mAdapter);
        preferences = getApplicationContext().getSharedPreferences("AdminTokenKey",0);

        findViewById(R.id.buttonAddUser).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userEmail = editTextUserName.getText().toString();

                if(userEmail.equals("")){
                    Toast.makeText(CreateNewChannelActivity.this, "User email cannot be empty", Toast.LENGTH_SHORT).show();
                    editTextUserName.setError("Cannot be empty");
                }else{
                    //hit the endpoint to check if he is a valid user
                    showProgressBarDialog();
                    new checkUser(userEmail).execute();
                }
            }
        });

        findViewById(R.id.buttonCreateNewUser).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CreateNewChannelActivity.this, CreateNewUser.class);
                startActivityForResult(intent,100);
            }
        });

        buttonCreateStream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String channelName = editTextStreamName.getText().toString();
                if(channelName.equals("")){
                    Toast.makeText(CreateNewChannelActivity.this, "Channel name cannot be empty", Toast.LENGTH_SHORT).show();
                    editTextStreamName.setError("cannot be empty");
                }else{
                    //have to hit the channel creating endpoint
                    new createChannel(channelName,gson.toJson(userArrayList)).execute();
                }
            }
        });

        buttonEditStream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String channelName = editTextStreamName.getText().toString();
                admin.channelName = channelName;
                admin.users = userArrayList;
                if(channelName.equals("")){
                    Toast.makeText(CreateNewChannelActivity.this, "Channel name cannot be empty", Toast.LENGTH_SHORT).show();
                    editTextStreamName.setError("cannot be empty");
                }else{
                    //have to hit the channel creating endpoint
                    new createChannel(channelName,gson.toJson(userArrayList)).execute();
                }
            }
        });
    }

    public class createChannel extends AsyncTask<String, Void, String> {
        boolean isStatus = true;
        String userList;

        String channelName;
        public createChannel(String channelName, String userList) {
            this.channelName = channelName;
            this.userList = userList;
        }

        @Override
        protected String doInBackground(String... strings) {

            Log.d("demo", getResources().getString(R.string.endPointUrl)+"api/v1/admin/channels");

            MediaType MEDIA_TYPE_JSON
                    = MediaType.parse("application/json");
            Log.d("demo", "this is the userlist json thing : "+userList);

            final OkHttpClient client = new OkHttpClient();
            RequestBody formBody = new FormBody.Builder()
                    .add("channelName",channelName)
                    .add("channelId",preferences.getString("ID", null))
                    .add("users",userList)
                    .build();
            Request request = new Request.Builder()
                    .url(getResources().getString(R.string.endPointUrl)+"api/v1/admin/channels")
                    .header("Authorization", "Bearer "+ preferences.getString("TOKEN_KEY", null))
                    .post(formBody)
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
            if(s!=null){
                JSONObject root = null;
                try {
                    root = new JSONObject(s);
                    if(isStatus){
                        Toast.makeText(CreateNewChannelActivity.this, root.getString("result"), Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(CreateNewChannelActivity.this, root.getString("error"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 100 && resultCode == 200 && data!=null){
            //yay finally user is created.
            Log.d("demo", "The new user email is : "+data.getExtras().getString("user_email"));
            userArrayList.add(data.getExtras().getString("user_email"));
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void deleteItem(int position) {
        userArrayList.remove(position);
        mAdapter.notifyDataSetChanged();
    }

    public class checkUser extends AsyncTask<String, Void, String> {
        String email;

        public checkUser(String email) {
            this.email = email;
        }

        @Override
        protected String doInBackground(String... strings) {
            final OkHttpClient client = new OkHttpClient();

            Log.d("demo", "doInBackground: async called for User");

                Request request = new Request.Builder()
                        .url(getResources().getString(R.string.endPointUrl)+"api/v1/admin/verifyUser?email="+email)
                        .header("Authorization", "Bearer "+ preferences.getString("TOKEN_KEY", null))
                        .build();
                try (Response response = client.newCall(request).execute()) {
                    String result = response.body().string();
                    Log.d("demo", "doInBackground: login response=>"+result);
                    return result;
                } catch (IOException e) {
                    e.printStackTrace();
                }


            return "";
        }

        @Override
        protected void onPostExecute(String result1) {
            super.onPostExecute(result1);
            JSONObject root = null;
            Log.d("demo",result1);
            try {
                root = new JSONObject(result1);
                hideProgressBarDialog();
                if(root.getBoolean("userFound")){
                    userArrayList.add(email);
                    mAdapter.notifyDataSetChanged();
                }else{
                    Toast.makeText(CreateNewChannelActivity.this, "Please enter a valid user email address", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                hideProgressBarDialog();
                e.printStackTrace();
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