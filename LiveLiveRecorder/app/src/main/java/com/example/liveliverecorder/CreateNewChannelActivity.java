package com.example.liveliverecorder;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

public class CreateNewChannelActivity extends AppCompatActivity implements UserListAdapter.InteractWithRecyclerView {

    private EditText editTextUserName;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    ArrayList<String> userArrayList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_channel);

        recyclerView = (RecyclerView) findViewById(R.id.usersRecyclerView);

        layoutManager = new LinearLayoutManager(CreateNewChannelActivity.this);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        mAdapter = new UserListAdapter(userArrayList, CreateNewChannelActivity.this);
        recyclerView.setAdapter(mAdapter);

        editTextUserName = findViewById(R.id.editTextUserName);
        findViewById(R.id.buttonAddUser).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userEmail = editTextUserName.getText().toString();

                if(userEmail.equals("")){
                    Toast.makeText(CreateNewChannelActivity.this, "User email cannot be empty", Toast.LENGTH_SHORT).show();
                    editTextUserName.setError("Cannot be empty");
                }else{

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
}