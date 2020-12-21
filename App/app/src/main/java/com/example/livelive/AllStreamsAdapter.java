package com.example.livelive;

import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AllStreamsAdapter extends RecyclerView.Adapter<AllStreamsAdapter.MyViewHolder> {
    private List<Streams> mDataset;
    public static InteractWithRecyclerView interact;
    private AppViewModel viewModel;
    String adapter_user_id;


    // Provide a suitable constructor (depends on the kind of dataset)
    public AllStreamsAdapter(List<Streams> myDataset, All_Stream_Fragment ctx) {
        mDataset = myDataset;
        interact = (InteractWithRecyclerView) ctx;
        viewModel = new ViewModelProvider(ctx).get(AppViewModel.class);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public AllStreamsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                           int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.streams_list, parent, false);
        AllStreamsAdapter.MyViewHolder vh = new AllStreamsAdapter.MyViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final AllStreamsAdapter.MyViewHolder holder, final int position) {
        Streams streams = mDataset.get(position);
        Log.d("demo", streams.toString());

        holder.channelName.setText(streams.channelName);
        holder.imageFavButton.setVisibility(ImageButton.INVISIBLE);


        new FindSpecificStream(streams, holder.imageFavButtonFav, holder.imageFavButton).execute();

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
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        TextView channelName;
        ImageButton imageFavButton, imageFavButtonFav;
        ConstraintLayout streamListConstraintLayout;

        //        ConstraintLayout constraintLayout;
        public MyViewHolder(View view) {
            super(view);
            channelName = view.findViewById(R.id.channelName);
            imageFavButton = view.findViewById(R.id.imageFavButton);
            imageFavButtonFav = view.findViewById(R.id.imageFavButtonFav);
            streamListConstraintLayout = view.findViewById(R.id.streamListConstraintLayout);
        }
    }

    public interface InteractWithRecyclerView {
        public void getDetails(Streams order, String Operation);
        public void getChannelId(String channelId);
    }

    class FindSpecificStream extends AsyncTask<String,Void,Streams>
    {
        Streams streamsCheck;
        ImageButton imgfav, img;


        FindSpecificStream(Streams streams, ImageButton imageButtonFav, ImageButton imageButton){
            this.streamsCheck = streams;
            this.imgfav = imageButtonFav;
            this.img = imageButton;

        }
        @Override
        protected Streams doInBackground(String... strs) {
            return viewModel.findStreamWhereIdAndUserId(streamsCheck.user_id,streamsCheck.channelId);
        }
        @Override
        protected void onPostExecute(Streams streams) {
            super.onPostExecute(streams);
            if (streams==null){
                imgfav.setVisibility(ImageButton.INVISIBLE);
                img.setVisibility(ImageButton.VISIBLE);
            }else{
                imgfav.setVisibility(ImageButton.VISIBLE);
                img.setVisibility(ImageButton.INVISIBLE);
            }
        }
    }

}