package com.example.livelive;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.support.v4.app.INotificationSideChannel;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Favorite_Streams_Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Favorite_Streams_Fragment extends Fragment implements FavoriteAdapter.InteractWithRecyclerView{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private SharedPreferences preferences;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    Gson gson = new Gson();
    User user;
    List<Streams> favArrayList = new ArrayList<>();

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private AppViewModel viewModel;
    private static final String TAG = "okay_Favorite_streamFragment";

    //////// approach one
    List<Streams> streamsList = new ArrayList<>();
    List<Streams> currentFavFromRoom = new ArrayList<>();

    public Favorite_Streams_Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Favorite_Streams_Fragment.
     */
    // TODO: Rename and change types and number of parameters
    public static Favorite_Streams_Fragment newInstance(String param1, String param2) {
        Favorite_Streams_Fragment fragment = new Favorite_Streams_Fragment();
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
        View view = inflater.inflate(R.layout.fragment_favorite__streams_, container, false);
        // Inflate the layout for this fragment

        viewModel = new ViewModelProvider(this).get(AppViewModel.class);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        preferences = getActivity().getSharedPreferences("TokeyKey", 0);
        String token_key = preferences.getString("TOKEN_KEY", null);
        String pro =  preferences.getString("USER",null);
        user = gson.fromJson(pro, User.class);

        viewModel.GetStreamsForUser(user._id).observe(getActivity(), new Observer<List<Streams>>() {
            @Override
            public void onChanged(List<Streams> streams) {
                favArrayList.clear();
                Log.d(TAG, "onChanged: got "+streams.size()+" orders for user1");
                if (!streams.isEmpty()){
                    Log.d("demo","The streamList are : "+streamsList.toString() +"Done ... ");
//                    Log.d(TAG, "onChanged: first order for user1=>"+orders.get(0));
                    favArrayList = new ArrayList<>();
                    for(Streams streams1 : streams){
                        int size = favArrayList.size();
                        for(Streams auth : streamsList){
                            if(streams1.channelId.equals(auth.channelId)){
                                favArrayList.add(streams1);
                                break;
                            }
                        }
                        if(favArrayList.size() != size+1){
                            //It means that the stream is no more autorized for the user. So we have to delete the stream.
                            viewModel.DeleteStream(streams1);
                        }
                    }
//                    favArrayList = streams;
                    Log.d("demo","Fav array list : " +favArrayList.toString());
                    recyclerView = getView().findViewById(R.id.favRecyclerView);

                    layoutManager = new LinearLayoutManager(getActivity());
                    recyclerView.setLayoutManager(layoutManager);

                    // specify an adapter (see also next example)
                    mAdapter = new FavoriteAdapter(favArrayList, Favorite_Streams_Fragment.this,getActivity());
                    recyclerView.setAdapter(mAdapter);
                }else{
                    favArrayList = new ArrayList<>();
                    recyclerView = getView().findViewById(R.id.favRecyclerView);
                    layoutManager = new LinearLayoutManager(getActivity());
                    recyclerView.setLayoutManager(layoutManager);
                    mAdapter = new FavoriteAdapter(favArrayList, Favorite_Streams_Fragment.this,getActivity());
                    recyclerView.setAdapter(mAdapter);
                }
                currentFavFromRoom = favArrayList;
            }
        });
    }

    @Override
    public void getDetails(Streams stream, String Operation) {
        if(Operation.equals("add")){
            viewModel.InsertStream(stream);
        }else if(Operation.equals("delete")){
            viewModel.DeleteStream(stream);
        }
    }

    @Override
    public void getChannelId(String channelId) {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.putExtra("channelId", channelId);
        startActivity(intent);
    }
    ////////////////// approach one
    void UpdateChannelList(List<Streams> streams){
        Log.d(TAG, "UpdateChannelList: getting stream in favorite");
        streamsList = streams;
        for (Streams s_from_room: currentFavFromRoom ){
            boolean isPresent = false;
            for (Streams new_stream : streams){
                if (s_from_room.channelId.equals(new_stream.channelId)) isPresent = true;
            }
            if (!isPresent){
                viewModel.DeleteStream(s_from_room);
            }
        }
    }
///////////////////////////

}