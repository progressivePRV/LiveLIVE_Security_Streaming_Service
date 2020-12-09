package com.example.liveliverecorder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CreateNewUser extends AppCompatActivity {

    TextInputLayout firstName_TIL,age_TIL,email_TIL,password_TIL;
    TextInputEditText firstName_TIET,age_TIET,email_TIET,password_TIET;
    SharedPreferences preferences;
    private Button sigin_button;
    private ImageButton imageButton;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private boolean isTakenPhoto = false;
    private ProgressDialog progressDialog;
    private FirebaseStorage storage;
    private FirebaseFirestore db;
    private StorageReference storageRef;
    private String user_email = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_user);

        firstName_TIET = findViewById(R.id.firstName_TIET);
        age_TIET = findViewById(R.id.age_TIET);
        email_TIET = findViewById(R.id.email_TIET);
        password_TIET = findViewById(R.id.password_TIET);
        firstName_TIL = findViewById(R.id.firstName_TIL);
        age_TIL = findViewById(R.id.age_TIL);
        email_TIL = findViewById(R.id.email_TIL);
        password_TIL = findViewById(R.id.password_TIL);
        imageButton = findViewById(R.id.imageButtonUser);
        sigin_button = findViewById(R.id.sigin_button);

        storage = FirebaseStorage.getInstance();
        // Create a storage reference from our app
        storageRef = storage.getReference();
        db = FirebaseFirestore.getInstance();
        sigin_button.setEnabled(false);

        preferences = getApplicationContext().getSharedPreferences("AdminTokenKey",0);

        sigin_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(CheckIfFieldAreEmpty()){
                    //call async task
                    String fnameValue = firstName_TIET.getText().toString().trim();
                    String passwordValue = password_TIET.getText().toString().trim();
                    //String repeatPasswordValue = repeatPassword.getText().toString().trim();
                    String emailValue = email_TIET.getText().toString().trim();
                    String ageValue = age_TIET.getText().toString().trim();
                    if(isTakenPhoto){
                        showProgressBarDialog();
                        new createNewUser(emailValue,passwordValue,fnameValue,ageValue).execute("");
                    }else{
                        Toast.makeText(CreateNewUser.this, "Please upload a photo of the user image for face verification", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        findViewById(R.id.imageButtonUser).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        findViewById(R.id.buttonCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //delete the user

            }
        });
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
            imageButton.setImageBitmap(imageBitmap);
            isTakenPhoto = true;
            showProgressBarDialog();
            uploadUserImageForVerification(true);
        }
    }

    public boolean CheckIfFieldAreEmpty(){
        if(firstName_TIET.getText().toString().equals("")){
            firstName_TIL.setError("Cannot be empty");
            return false;
        }else{
            firstName_TIL.setError("");
        }
        if(age_TIET.getText().toString().equals("")){
            age_TIL.setError("Cannot be empty");
            return false;
        }else{
            age_TIL.setError("");
        }
        if(email_TIET.getText().toString().equals("")){
            email_TIL.setError("Cannot be empty");
            return false;
        }else{
            email_TIL.setError("");
        }
        String x = password_TIET.getText().toString();
        if(x.equals("")){
            password_TIL.setError("Cannot be empty");
            return false;
        }
        else{
            password_TIL.setError("");
        }
        return true;
    }

    public class createNewUser extends AsyncTask<String, Void, String> {
        boolean isStatus = true;

        String emailValue, passwordValue, fnameValue;
        String age;
        public createNewUser(String emailValue, String passwordValue, String fnameValue, String age) {
            this.emailValue = emailValue;
            this.passwordValue = passwordValue;
            this.fnameValue = fnameValue;
            this.age = age;
        }

        @Override
        protected String doInBackground(String... strings) {
            Log.d("demo", getResources().getString(R.string.endPointUrl)+"api/v1/admin/users");
            final OkHttpClient client = new OkHttpClient();
            RequestBody formBody = new FormBody.Builder()
                    .add("name",fnameValue)
                    .add("age",age)
                    .add("email", emailValue)
                    .add("password",passwordValue)
                    .build();
            Request request = new Request.Builder()
                    .url(getResources().getString(R.string.endPointUrl)+"api/v1/admin/users")
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
            //Log.d("demo",s);
            if(s!=null){
                JSONObject root = null;
                try {
                    root = new JSONObject(s);
                    if(isStatus){
                        Log.d("test","hello it is entering is staus for the user add");
                        Log.d("test","trying to print the s : "+s);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("USER_ID",root.getString("_id"));
                        editor.commit();
                        //upload the Face
                        user_email = root.getString("email");
                        Log.d("demo","printing user_email : "+user_email);
                        uploadUserImageForVerification(false);
                        Log.d("demo", "onPostExecute: User Successfully created");
                    }else{
                        hideProgressBarDialog();
                        //Handling the error scenario here
                        JSONObject error = root.getJSONObject("error");
                        if(error.length() > 1){
                            //It means duplicate email issue
                            JSONObject keyValue = error.getJSONObject("keyValue");
                            if(keyValue.getString("email") != null){
                                Toast.makeText(CreateNewUser.this, "Email already exist. Please use another email!", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(CreateNewUser.this, "Some error occured. Please try again!", Toast.LENGTH_SHORT).show();
                            }
                        }else if(error.length() == 1){
                            JSONArray message = error.getJSONArray("errors");
                            JSONObject arrayObject = message.getJSONObject(0);
                            Toast.makeText(CreateNewUser.this, arrayObject.getString("msg"), Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(CreateNewUser.this, "Some error occured. Please try again!", Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void uploadUserImageForVerification(boolean isVerify) {
//        showProgressBarDialog();
        String imagePath = "";
        if(isVerify){
            imagePath = "image_"+preferences.getString("ID", null)+"_original.jpg";
        }else{
            imagePath = "image_"+preferences.getString("USER_ID", null)+"_original.jpg";
        }
        final StorageReference imageRepo = storageRef.child(imagePath);
        Bitmap bitmap = ((BitmapDrawable) imageButton.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
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
                    if(isVerify){
                        new verifyFaceForAdminAsync(downloadUri.toString()).execute();
                    }else{
                        hideProgressBarDialog();
                        Toast.makeText(CreateNewUser.this, "user Successfully created", Toast.LENGTH_SHORT).show();
                        Log.d("demo","it should actually come here for the face verification : "+user_email);
                        Intent intent = new Intent();
                        intent.putExtra("user_email",user_email);
                        setResult(200, intent);
                        finish();
                    }
                }else{
                    Toast.makeText(CreateNewUser.this, "Upload Failure", Toast.LENGTH_SHORT).show();
                    hideProgressBarDialog();
                }
            }
        });
    }

    public class verifyFaceForAdminAsync extends AsyncTask<String, Void, String> {

        String downloadURI;
        boolean isStatus =true;

        public verifyFaceForAdminAsync(String downloadURI) {
            this.downloadURI = downloadURI;
            Log.d("demo","the download url is : "+downloadURI);
        }

        @Override
        protected String doInBackground(String... strings) {
            final OkHttpClient client = new OkHttpClient();
            RequestBody formBody = new FormBody.Builder()
                    .add("url",downloadURI)
                    .build();

            Request request = new Request.Builder()
                    .url(getResources().getString(R.string.endPointUrl)+"api/v1/user/verifyFace")
                    .header("Authorization", "Bearer "+ preferences.getString("TOKEN_KEY", null))
                    .post(formBody)
                    .build();

            Log.d("demo","authorization is : " +preferences.getString("TOKEN_KEY", null));
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
//                Log.d("demo",root.getString("result"));
                if(!isStatus){
                    //It means that the face is valid
                    //going on to the next step
                    Toast.makeText(CreateNewUser.this, "Face is verified.", Toast.LENGTH_SHORT).show();
                    sigin_button.setEnabled(true);
                    delteTheUserImage();
                }else{
                    //It means that the face is not valid. So asking the user to upload the image again.
                    delteTheUserImage();
//                    Toast.makeText(CreateNewUser.this, "Face uploaded is not proper. Please upload again correctly or the user will be deleted", Toast.LENGTH_SHORT).show();
                    Toast.makeText(CreateNewUser.this, root.getString("error"), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                hideProgressBarDialog();
                e.printStackTrace();
            }
        }

    }

    private void delteTheUserImage() {
        //Have td delete the userImage from the ImageButton
        String imagePath = "image_"+preferences.getString("ID", null)+"_original.jpg";
        final StorageReference imageRepo = storageRef.child(imagePath);
        imageRepo.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // File deleted successfully
                hideProgressBarDialog();
                Log.d("demo", "onSuccess: deleted file");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Uh-oh, an error occurred!
                hideProgressBarDialog();
                Toast.makeText(CreateNewUser.this, "Error in Face Verification. Please try again", Toast.LENGTH_SHORT).show();
                Log.d("demo", "onFailure: did not delete file");
            }
        });
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