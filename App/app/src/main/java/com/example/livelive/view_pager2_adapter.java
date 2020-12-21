package com.example.livelive;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class view_pager2_adapter extends FragmentStateAdapter {

    private static final String TAG = "okay";

    public view_pager2_adapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0:
                return new Favorite_Streams_Fragment();
                //break;
            case 1:
                return new All_Stream_Fragment();
        }
        Log.d(TAG, "createFragment: position is not right in tab selection");
        return null;
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
