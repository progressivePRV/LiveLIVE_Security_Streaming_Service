package com.example.livelive;

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
    private ProgressDialog progressDialog;
    private RecyclerView recyclerView;
    private AppViewModel viewModel;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    List<Streams> streamsList = new ArrayList<>();
    Gson gson = new Gson();

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

        // specify an adapter (see also next example)
        mAdapter = new AllStreamsAdapter(streamsList, All_Stream_Fragment.this);

        recyclerView.setAdapter(mAdapter);
        recyclerView.requestDisallowInterceptTouchEvent(true);

        Log.d("demo", "Entering all the stream fragment");
    }

    @Override
    public void onResume() {
        super.onResume();
        showProgressBarDialog();
        streamsList.clear();
        new getUsersStreamChannels().execute();
    }

    @Override
    public void getDetails(Streams order, String Operation) {
        if(Operation.equals("add")){
            viewModel.InsertOrder(order);
        }else if(Operation.equals("delete")){
            viewModel.DeleteOrder(order);
        }
    }

    @Override
    public void getChannelId(String channelId) {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.putExtra("channelId", channelId);
        startActivity(intent);
    }

    public class getUsersStreamChannels extends AsyncTask<String, Void, String> {
        boolean isStatus = true;

        @Override
        protected String doInBackground(String... strings) {
            final OkHttpClient client = new OkHttpClient();

            Log.d("demo", "entering do in Background");
            Request request = new Request.Builder()
                    .url(getResources().getString(R.string.endPointUrl)+"api/v1/user/channels")
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
            Log.d("streaming broadcasting : ", s);
            if(s!=null){
                JSONArray root = null;
                try {
                    hideProgressBarDialog();
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
                        Toast.makeText(getActivity(), new JSONObject(s).getString("error"), Toast.LENGTH_SHORT).show();
                    }

                    Log.d("demo",streamsList.toString());
                    if(streamsList.size() <= 0){
                        Toast.makeText(getActivity(), "Sorry no channels found", Toast.LENGTH_SHORT).show();
                    }else{
                        mAdapter.notifyDataSetChanged();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

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