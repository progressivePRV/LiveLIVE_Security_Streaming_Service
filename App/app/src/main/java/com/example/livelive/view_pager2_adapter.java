package com.example.livelive;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class view_pager2_adapter extends FragmentStateAdapter {

    private static final String TAG = "okay";
    All_Stream_Fragment all_stream_fragment;
    Favorite_Streams_Fragment favorite_streams_fragment;

    public view_pager2_adapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        all_stream_fragment =  new All_Stream_Fragment();
        favorite_streams_fragment = new Favorite_Streams_Fragment();
    }

    public All_Stream_Fragment getAll_stream_fragment() {
        return all_stream_fragment;
    }

    public Favorite_Streams_Fragment getFavorite_streams_fragment() {
        return favorite_streams_fragment;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0:
                return all_stream_fragment;
                //break;
            case 1:
                return favorite_streams_fragment;
        }
        Log.d(TAG, "createFragment: position is not right in tab selection");
        return null;
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
