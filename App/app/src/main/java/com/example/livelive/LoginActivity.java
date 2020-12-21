package com.example.livelive;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_CODE_PERMISSIONS = 1111;
    TextInputLayout email_TIL,password_TIL;
    TextInputEditText email_TIET,password_TIET;
    private ProgressDialog progressDialog;
    private FirebaseStorage storage;
    private FirebaseFirestore db;
    private StorageReference storageRef;
    private boolean isTakenPhoto = false;

    Gson gson =  new Gson();
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    private final String[] REQUIRED_PERMISSIONS = new String[]
            {
                    "android.permission.CAMERA",
                    "android.permission.BLUETOOTH",
                    "android.permission.INTERNET",
            };

    private static final String TAG = "demo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        email_TIET = findViewById(R.id.email_TIET);
        password_TIET = findViewById(R.id.password_TIET);
        email_TIL = findViewById(R.id.email_TIL);
        password_TIL = findViewById(R.id.password_TIL);

        storage = FirebaseStorage.getInstance();
        // Create a storage reference from our app
        storageRef = storage.getReference();
        db = FirebaseFirestore.getInstance();

        //checking for all the permissions
        CheckForALLPermissions();

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

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent("android.media.action.IMAGE_CAPTURE");
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            isTakenPhoto = true;
            uploadToFirebase(imageBitmap);
        }
    }

    private void uploadToFirebase(Bitmap imageBitmap) {
        String imagePath = "image_"+preferences.getString("ID", null)+"_copy.jpg";
        final StorageReference imageRepo = storageRef.child(imagePath);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        UploadTask uploadTask = imageRepo.putBytes(data);
        final String finalImagePath = imagePath;
        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                // Continue with the task to get the download URL
                return imageRepo.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    Log.d("demo",downloadUri+"");
                    //calling the face verification api. Upload of the face is successful
                    showProgressBarDialog();
                    new getFaceValidationAsync(downloadUri.toString()).execute();
                }else{
                    hideProgressBarDialog();
                    Toast.makeText(LoginActivity.this, "Upload Failure", Toast.LENGTH_SHORT).show();
                }
            }
        });
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
                        .url(getResources().getString(R.string.endPointUrl)+"api/v1/login/users")
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
                    User user = new User();
                    user._id = root.getString("_id");
                    user.name = root.getString("name");
                    user.age = root.getString("age");
                    user.email = root.getString("email");
                    user.token = root.getString("token");
                    Log.d("demo",user._id);
                    preferences = getApplicationContext().getSharedPreferences("TokeyKey",0);
                    editor = preferences.edit();
                    editor.putString("TOKEN_KEY",user.token);
                    editor.putString("ID",user._id);
                    editor.putString("USER",gson.toJson(user));
                    editor.commit();
                    hideProgressBarDialog();
                    Toast.makeText(LoginActivity.this, "Procceding with the face verification", Toast.LENGTH_SHORT).show();

                    //Just commenting to pass the face verification.. remove the next line comment if you want face verification
                    dispatchTakePictureIntent();


                   //Since thfe face verification is passed, calling the next actvity. When the above line is uncommented, comment the below two lines
//                    Intent intent = new Intent(LoginActivity.this, ChannelListActivity.class);
//                    startActivity(intent);
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


    public class getFaceValidationAsync extends AsyncTask<String, Void, String> {

        String downloadURI;
        boolean isStatus =true;

        public getFaceValidationAsync(String downloadURI) {
            this.downloadURI = downloadURI;
        }

        @Override
        protected String doInBackground(String... strings) {
            final OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(getResources().getString(R.string.endPointUrl)+"api/v1/user/verifyFace")
                    .header("Authorization", "Bearer "+ preferences.getString("TOKEN_KEY", null))
                    .build();

            String responseValue = null;
            try (Response response = client.newCall(request).execute()) {
                if(response.isSuccessful()){
                    isStatus = true;
                }else{
                    isStatus = false;
                }
                responseValue = response.body().string();
            } catch (IOException e) {
                Log.d("demo","Face verification exception");
                e.printStackTrace();
            }

            return responseValue;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            JSONObject root = null;
            try {
                Log.d("demo",result);
                root = new JSONObject(result);
                if(isStatus){
                    if(root.getString("isFaceSame").equals("true")){
                        Log.d("demo","The token for the user is : " +root.getString("token"));
                        preferences = getApplicationContext().getSharedPreferences("TokeyKey",0);
                        editor = preferences.edit();
                        editor.putString("TOKEN_KEY",root.getString("token"));
                        Toast.makeText(LoginActivity.this, "Face Verified", Toast.LENGTH_SHORT).show();
                        editor.commit();
                        deleteTheFaceImage();
                    }else{
                        hideProgressBarDialog();
                        Toast.makeText(LoginActivity.this, "Face not authorized. Please try again", Toast.LENGTH_SHORT).show();
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

    private void deleteTheFaceImage() {
        String imagePath = "image_"+preferences.getString("ID", null)+"_copy.jpg";
        final StorageReference imageRepo = storageRef.child(imagePath);
        imageRepo.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // File deleted successfully
                hideProgressBarDialog();
                Intent intent = new Intent(LoginActivity.this, ChannelListActivity.class);
                startActivity(intent);
                finish();
                Log.d(TAG, "onSuccess: deleted file");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Uh-oh, an error occurred!
                hideProgressBarDialog();
                Toast.makeText(LoginActivity.this, "Error occured in Face Verification. Please try again", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onFailure: did not delete file");
            }
        });
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