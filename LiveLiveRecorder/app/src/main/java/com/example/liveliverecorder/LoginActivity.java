package com.example.liveliverecorder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.transition.Explode;
import android.transition.Fade;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "demo_LoginActivity";
    private static final int REQUEST_CODE_PERMISSIONS = 1111;
    Gson gson =  new Gson();
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    private ProgressDialog progressDialog;
    TextInputLayout email_TIL,password_TIL;
    TextInputEditText email_TIET,password_TIET;
    private final String[] REQUIRED_PERMISSIONS = new String[]
            {
                    "android.permission.CAMERA",
                    "android.permission.RECORD_AUDIO",
                    "android.permission.MODIFY_AUDIO_SETTINGS",
                    "android.permission.BLUETOOTH",
                    "android.permission.INTERNET",
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ///////// setting up the animation
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        getWindow().setEnterTransition(new Explode());
        getWindow().setExitTransition(new Explode());
//        getWindow().setSharedElementEnterTransition();
//        getWindow().setSharedElementExitTransition();
        /////////

        setContentView(R.layout.activity_login);

        email_TIET = findViewById(R.id.email_TIET);
        password_TIET = findViewById(R.id.password_TIET);
        email_TIL = findViewById(R.id.email_TIL);
        password_TIL = findViewById(R.id.password_TIL);

        preferences = getApplicationContext().getSharedPreferences("AdminTokenKey",0);
        Log.d("demo",preferences.toString());

        if(preferences != null && preferences.getString("TOKEN_KEY", null) != null && !preferences.getString("TOKEN_KEY", null).equals("")){
            //it means that the token is there so go ahead and login
            Toast.makeText(this, "Logging in", Toast.LENGTH_SHORT).show();
            showProgressBarDialog();
            new getAdminChannel().execute();
        }

        findViewById(R.id.sigin_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(CheckIfEmailAndPasswordAreEmpty()){
                    String loginText = email_TIET.getText().toString().trim();
                    String passwordText = password_TIET.getText().toString().trim();
                    Log.d("demo",loginText+" "+passwordText);
                    Log.d(TAG, "onClick: calling async");
                    showProgressBarDialog();
                    new getValidatedAsync(loginText, passwordText).execute();
                }
            }
        });

        CheckForALLPermissions();

    }

    private void CheckForALLPermissions() {
        boolean allGranted = true;
        for (String permission : REQUIRED_PERMISSIONS){
            Log.d(TAG, "CheckForALLPermissions: checking permission for=>"+permission);
            if(ContextCompat.checkSelfPermission(this,permission)
                    != PackageManager.PERMISSION_GRANTED){
                allGranted = false;
                break;
            }
        }
        if (!allGranted)
            ActivityCompat.requestPermissions(this,REQUIRED_PERMISSIONS,REQUEST_CODE_PERMISSIONS);
        else
            Log.d(TAG, "CheckForALLPermissions: all permission granted");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_CODE_PERMISSIONS){

            for (int i : grantResults){
                Log.d(TAG, "onRequestPermissionsResult: grantResult "+i);
                if(i != 0){
                    Log.d(TAG, "onRequestPermissionsResult: permission not granted");
                    Toast.makeText(this, "without Permissions App will not work Properly", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            Log.d(TAG, "onRequestPermissionsResult: permission granted");
            //use camera
//            StartCamera();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private boolean CheckIfEmailAndPasswordAreEmpty() {
        if(email_TIET.getText().toString().equals("")){
            email_TIL.setError("Cannot be empty");
            return false;
        }else{
            email_TIL.setError("");
        }
        if(password_TIET.getText().toString().equals("")){
            password_TIL.setError("Cannot be empty");
            return false;
        }else{
            password_TIL.setError("");
        }
        return true;
    }

    public class getValidatedAsync extends AsyncTask<String, Void, String> {

        String username, password;
        boolean isStatus =true;

        public getValidatedAsync(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        protected String doInBackground(String... strings) {
            final OkHttpClient client = new OkHttpClient();
            String decodedValue = username+":"+password;

            Log.d(TAG, "doInBackground: async called for login");

            byte[] encodedValue = new byte[0];
            try {
                encodedValue = decodedValue.getBytes("UTF-8");
                String encodedString = Base64.encodeToString(encodedValue, Base64.NO_WRAP);

                Request request = new Request.Builder()
                        .url(getResources().getString(R.string.endPointUrl)+"api/v1/login/admins")
                        .header("Authorization", "Basic " + encodedString)
                        .build();
                try (Response response = client.newCall(request).execute()) {
                    String result = response.body().string();
                    Log.d(TAG, "doInBackground: login response=>"+result);
                    if (response.isSuccessful()){
                        isStatus = true;
                    }else{
                        isStatus = false;
                    }
                    return result;
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } catch (UnsupportedEncodingException e) {
                Toast.makeText(LoginActivity.this, "Some problem occured with the password", Toast.LENGTH_SHORT).show();
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
                if(isStatus){
                    Log.d("demo",root.toString());
                    Admin admin = gson.fromJson(result1,Admin.class);
                    Log.d(TAG, "This is admin "+admin.toString());


                        editor = preferences.edit();
                        editor.putString("TOKEN_KEY",admin.token);
                        editor.putString("ID",admin._id);
                        editor.putString("ADMIN",gson.toJson(admin));
                        editor.commit();
                        hideProgressBarDialog();
                        if(admin.channelId == null){
                            Intent intent = new Intent(LoginActivity.this, CreateNewChannelActivity.class);
                            ActivityOptions activityOptions =  ActivityOptions.makeSceneTransitionAnimation(LoginActivity.this,findViewById(R.id.app_icon_iv),"icon");
                            startActivity(intent, activityOptions.toBundle());
//                        startActivity(intent);
                        }else{
                            Intent i = new Intent(LoginActivity.this,ChannelInfo.class);
                            i.putExtra("Admin_Obj",admin);
                            ActivityOptions activityOptions =  ActivityOptions.makeSceneTransitionAnimation(LoginActivity.this,findViewById(R.id.app_icon_iv),"icon");
                            startActivity(i, activityOptions.toBundle());
//                        startActivity(i);
                            Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                            finish();
                        }

                }else{
                    //It means that they are some error while signing up.
                    hideProgressBarDialog();
                    Toast.makeText(LoginActivity.this, root.getString("error"), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                hideProgressBarDialog();
                e.printStackTrace();
            }
        }
    }


    public class getAdminChannel extends AsyncTask<String, Void, String> {

        boolean isStatus =true;

        @Override
        protected String doInBackground(String... strings) {
            final OkHttpClient client = new OkHttpClient();
            Log.d(TAG, "doInBackground: async called for login");

            try {

                Request request = new Request.Builder()
                        .url(getResources().getString(R.string.endPointUrl)+"api/v1/admin/channels")
                        .header("Authorization", "Bearer "+ preferences.getString("TOKEN_KEY", null))
                        .build();
                try (Response response = client.newCall(request).execute()) {
                    String result = response.body().string();
                    Log.d(TAG, "doInBackground: login response=>"+result);
                    if (response.isSuccessful()){
                        isStatus = true;
                    }else{
                        isStatus = false;
                    }
                    return result;
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } catch (Exception e) {
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
                if(isStatus){
                    Log.d("demo",root.toString());
                    String adminChannel = root.getString("adminChannel");
                    Admin admin = gson.fromJson(adminChannel,Admin.class);
                    admin.token = preferences.getString("TOKEN_KEY", null);
                    Log.d(TAG, "This is admin "+admin.toString());
                    editor = preferences.edit();
                    editor.putString("TOKEN_KEY",admin.token);
                    editor.putString("ID",admin._id);
                    editor.putString("ADMIN",gson.toJson(admin));
                    editor.commit();
                    hideProgressBarDialog();
                    if(admin.channelId == null){
                        Intent intent = new Intent(LoginActivity.this, CreateNewChannelActivity.class);
                        ActivityOptions activityOptions =  ActivityOptions.makeSceneTransitionAnimation(LoginActivity.this,findViewById(R.id.app_icon_iv),"icon");
                        startActivity(intent, activityOptions.toBundle());
//                        startActivity(intent);
                    }else{
                        Intent i = new Intent(LoginActivity.this,ChannelInfo.class);
                        i.putExtra("Admin_Obj",admin);
                        ActivityOptions activityOptions =  ActivityOptions.makeSceneTransitionAnimation(LoginActivity.this,findViewById(R.id.app_icon_iv),"icon");
                        startActivity(i, activityOptions.toBundle());
//                        startActivity(i);
                        Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                }else{
                    //It means that they are some error while signing up.
                    hideProgressBarDialog();
                    Toast.makeText(LoginActivity.this, root.getString("error"), Toast.LENGTH_SHORT).show();
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