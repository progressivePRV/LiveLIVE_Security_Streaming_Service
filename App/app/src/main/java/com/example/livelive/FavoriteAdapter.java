package com.example.livelive;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fm.liveswitch.vp8.Fragment;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.MyViewHolder> {
    private List<Streams> mDataset;
    public static FavoriteAdapter.InteractWithRecyclerView interact;
//    Fragment frag;
    Activity act;

    // Provide a suitable constructor (depends on the kind of dataset)
    public FavoriteAdapter(List<Streams> myDataset, InteractWithRecyclerView frag, Activity activity) {
        mDataset = myDataset;
        interact = frag;
        this.act = activity;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public FavoriteAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                           int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.streams_list, parent, false);
        FavoriteAdapter.MyViewHolder vh = new FavoriteAdapter.MyViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final FavoriteAdapter.MyViewHolder holder, final int position) {
        Streams streams = mDataset.get(position);
        Log.d("demo" ,streams.toString());
        holder.streamListConstraintLayout.setBackgroundColor(act.getResources().getColor(R.color.colorAccent));
//        holder.imageFavButton.setBackgroundColor(act.getResources().getColor(R.color.colorAccent));
//        holder.imageFavButtonFav.setBackgroundColor(act.getResources().getColor(R.color.colorAccent));

        holder.channelName.setText(streams.channelName);
        holder.imageFavButton.setVisibility(ImageButton.INVISIBLE);

        holder.imageFavButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.imageFavButton.setVisibility(ImageButton.INVISIBLE);
                holder.imageFavButtonFav.setVisibility(ImageButton.VISIBLE);
                interact.getDetails(mDataset.get(position), "add");
            }
        });

        holder.imageFavButtonFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.imageFavButtonFav.setVisibility(ImageButton.INVISIBLE);
                holder.imageFavButton.setVisibility(ImageButton.VISIBLE);
                interact.getDetails(mDataset.get(position), "delete");
            }
        });

        holder.streamListConstraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                interact.getChannelId(streams.channelId);
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder{
        // each data item is just a string in this case
        TextView channelName;
        ImageButton imageFavButton, imageFavButtonFav;
        ConstraintLayout streamListConstraintLayout;

        public MyViewHolder(View view) {
            super(view);
            channelName = view.findViewById(R.id.channelName);
            imageFavButton = view.findViewById(R.id.imageFavButton);
            imageFavButtonFav = view.findViewById(R.id.imageFavButtonFav);
            streamListConstraintLayout = view.findViewById(R.id.streamListConstraintLayout);
        }
    }

    public interface InteractWithRecyclerView{
        public void getDetails(Streams order, String Operation);
        public void getChannelId(String channelId);
    }
}


