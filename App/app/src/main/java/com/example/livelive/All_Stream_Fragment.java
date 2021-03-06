package com.example.livelive;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link All_Stream_Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class All_Stream_Fragment extends Fragment implements AllStreamsAdapter.InteractWithRecyclerView {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "okay_AllStreams";
    private ProgressDialog progressDialog;
    private RecyclerView recyclerView;
    private AppViewModel viewModel;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    List<Streams> streamsList = new ArrayList<>();
    Gson gson = new Gson();
    private ImageButton channelSearchButton;
    private ImageButton cancelSearchButton;
    private EditText searchChannelsByName;
    ChannelListActivity parent_act;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private SharedPreferences preferences;

    public All_Stream_Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment All_Stream_Fragment.
     */
    // TODO: Rename and change types and number of parameters
    public static All_Stream_Fragment newInstance(String param1, String param2) {
        All_Stream_Fragment fragment = new All_Stream_Fragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        viewModel = new ViewModelProvider(this).get(AppViewModel.class);
        return inflater.inflate(R.layout.fragment_all__stream_, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        preferences = getActivity().getApplicationContext().getSharedPreferences("TokeyKey",0);
        recyclerView = getView().findViewById(R.id.allStreamsRecyclerViewList);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        //////////// approach one
        parent_act = (ChannelListActivity) getActivity();
        /////////////
        // specify an adapter (see also next example)
        mAdapter = new AllStreamsAdapter(streamsList, All_Stream_Fragment.this);

        recyclerView.setAdapter(mAdapter);
        recyclerView.requestDisallowInterceptTouchEvent(true);
        searchChannelsByName = getView().findViewById(R.id.searchChannelsByName);
        channelSearchButton = getView().findViewById(R.id.channelSearchButton);
        cancelSearchButton = getView().findViewById(R.id.cancelSearchButton);
        Log.d("demo", "Entering all the stream fragment");

        channelSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchChannel = searchChannelsByName.getText().toString().trim();
                if(searchChannel.equals("")){
                    Toast.makeText(getActivity(), "Search By Channel Name cannot be Empty", Toast.LENGTH_SHORT).show();
                }else{
                    showProgressBarDialog();
                    cancelSearchButton.setVisibility(ImageButton.VISIBLE);
                    channelSearchButton.setVisibility(ImageButton.INVISIBLE);
//                    new getUsersStreamChannels(getResources().getString(R.string.endPointUrl)+"api/v1/user/channels?channelName="+searchChannel).execute();
                    String url = "api/v1/user/channels?channelName="+searchChannel;
                    parent_act.GetUsersStreamChannels(url);
                }
            }
        });

        cancelSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressBarDialog();
                searchChannelsByName.setText("");
                cancelSearchButton.setVisibility(ImageButton.INVISIBLE);
                channelSearchButton.setVisibility(ImageButton.VISIBLE);
//                new getUsersStreamChannels(getResources().getString(R.string.endPointUrl)+"api/v1/user/channels").execute();
                String url = "api/v1/user/channels";
                parent_act.GetUsersStreamChannels(url);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        showProgressBarDialog();
//        new getUsersStreamChannels(getResources().getString(R.string.endPointUrl)+"api/v1/user/channels").execute();
        String url = "api/v1/user/channels";
        parent_act.GetUsersStreamChannels(url);
    }

    @Override
    public void getDetails(Streams stream, String Operation) {
        if(Operation.equals("add")){
            viewModel.InsertStream(stream);
        }else if(Operation.equals("delete")){
            viewModel.DeleteStream(stream);
        }
    }
////////////////// approach one
    void UpdateChannelList(List<Streams> streams){
        Log.d(TAG, "UpdateChannelList: called");
        streamsList = streams;
//        mAdapter.notifyDataSetChanged();
        mAdapter = new AllStreamsAdapter(streamsList, All_Stream_Fragment.this);
        recyclerView.setAdapter(mAdapter);
        Log.d(TAG, "UpdateChannelList: calling hide progress bar");
        hideProgressBarDialog();
    }
///////////////////////////
    @Override
    public void getChannelId(String channelId) {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.putExtra("channelId", channelId);
        startActivity(intent);
    }

//    public class getUsersStreamChannels extends AsyncTask<String, Void, String> {
//        boolean isStatus = true;
//        String getChannelUrl;
//
//        public getUsersStreamChannels(String getChannelUrl) {
//            this.getChannelUrl = getChannelUrl;
//        }
//
//        @Override
//        protected String doInBackground(String... strings) {
//
//            final OkHttpClient client = new OkHttpClient();
//
////            String getChannelUrl =  getResources().getString(R.string.endPointUrl)+"api/v1/user/channels";
//
//            Log.d("demo", "entering do in Background");
//            Request request = new Request.Builder()
//                    .url(getChannelUrl)
//                    .header("Authorization", "Bearer "+ preferences.getString("TOKEN_KEY", null))
//                    .build();
//
//            String responseValue = null;
//            try (Response response = client.newCall(request).execute()) {
//                if(response.isSuccessful()){
//                    isStatus = true;
//                }else{
//                    isStatus = false;
//                }
//                Log.d("demo"," "+response.isSuccessful());
//                responseValue = response.body().string();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            return responseValue;
//        }
//
//        @Override
//        protected void onPostExecute(String s) {
//            super.onPostExecute(s);
//            streamsList.clear();
//            Log.d("streaming broadcasting : ", s);
//            if(s!=null){
//                JSONArray root = null;
//                try {
//                    hideProgressBarDialog();
//                    if(isStatus){
//                        root = new JSONArray(s);
//                        String pro =  preferences.getString("USER",null);
//                        User user = gson.fromJson(pro, User.class);
//                      for(int i=0; i<root.length(); i++){
//                          JSONObject channelDetails = root.getJSONObject(i);
//                          Streams streams = new Streams();
//                          streams._id = channelDetails.getString("_id");
//                          streams.channelId = channelDetails.getString("channelId");
//                          streams.channelName = channelDetails.getString("channelName");
//                          streams.user_id = user._id;
//                          streamsList.add(streams);
//                      }
//                    }else{
//                        Toast.makeText(getActivity(), new JSONObject(s).getString("error"), Toast.LENGTH_SHORT).show();
//                    }
//
//                    Log.d("demo",streamsList.toString());
//                    mAdapter.notifyDataSetChanged();
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }

    public void showProgressBarDialog()
    {
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Loading");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public void hideProgressBarDialog()
    {
        progressDialog.dismiss();
    }
}