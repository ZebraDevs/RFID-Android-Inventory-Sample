package com.zebra.rfid.demo.reflexions;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.Viewholder> {

    private final Context context;
    private final ArrayList<ItemData> itemDataArrayList;
    private String TAG = "_RECYCLERVIEW_";




    // Constructor
    public ItemAdapter(Context context, ArrayList<ItemData> itemDataArrayList) {
        this.context = context;
        this.itemDataArrayList = itemDataArrayList;
    }

    @NonNull
    @Override
    public ItemAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // to inflate the layout for each item of recycler view.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_layout, parent, false);
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemAdapter.Viewholder holder, @SuppressLint("RecyclerView") final int position) {
        // to set data to textview and imageview of each card layout
        final ItemData model = itemDataArrayList.get(position);
        holder.itemName.setText(model.getItemname());
        holder.itemSKU.setText("Tote No.: "+model.getSku());
        holder.itemSize.setText("Size: "+model.getSize());
        holder.itemCount.setText("Seen count: "+model.getSeencount() +"/"+model.getExpectedCount());

        if(((double) model.getSeencount()/model.getExpectedCount()) < .6) { //changed number to a percent of how much of the expected stock in is the store -- instead of difference
            holder.view.setBackgroundColor(Color.RED);
            holder.view.setOnClickListener(new View.OnClickListener() {  // <--- here
                @Override
                public void onClick(View v) {
                    Log.i("ItemAdapter","Click-"+itemDataArrayList.get(position).getSku());  // <--- here
                }
            });
        }else{
            holder.view.setBackgroundColor(Color.WHITE);
            holder.view.setOnClickListener(null);
        }
        //get image
        String pathName = context.getFilesDir().getAbsolutePath() + "/" + model.getImage();
        Log.i(TAG, "onBindViewHolder: " + pathName);
        Drawable d = Drawable.createFromPath(pathName);
        holder.itemImage.setImageDrawable(d);


    }

    @Override
    public int getItemCount() {
        // this method is used for showing number
        // of card items in recycler view.
        return itemDataArrayList.size();
    }

    // View holder class for initializing of 
    // your views such as TextView and Imageview.
    public class Viewholder extends RecyclerView.ViewHolder {
        private final ImageView itemImage;
        private final TextView itemName;
        private final TextView itemCount;
        private final TextView itemSize;
        private final TextView itemSKU;
        public View view;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            this.view = itemView.findViewById(R.id.cardViewConstraintLayout);
            itemImage = itemView.findViewById(R.id.imageViewItemImage);
            itemName = itemView.findViewById(R.id.textViewItemName);
            itemSKU = itemView.findViewById(R.id.textViewItemSKU);
            itemCount = itemView.findViewById(R.id.textViewItemCount);
            itemSize = itemView.findViewById(R.id.textViewItemSize);
        }
    }

}