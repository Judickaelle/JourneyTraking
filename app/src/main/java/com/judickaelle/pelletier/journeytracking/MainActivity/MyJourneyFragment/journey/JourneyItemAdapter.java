package com.judickaelle.pelletier.journeytracking.MainActivity.MyJourneyFragment.journey;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.judickaelle.pelletier.journeytracking.R;

public class JourneyItemAdapter extends FirestoreRecyclerAdapter<JourneyItem, JourneyItemAdapter.JourneyItemHolder>{

    private OnItemClickListener listener;

    public JourneyItemAdapter(@NonNull FirestoreRecyclerOptions<JourneyItem> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull JourneyItemHolder holder, int position, @NonNull JourneyItem model) {
        holder.textViewTitle.setText(model.getTitle());
        holder.textViewSecretKey.setText(getSnapshots().getSnapshot(position).getReference().getId());
    }

    @NonNull
    @Override
    public JourneyItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.journey_item, parent, false);
        return new JourneyItemHolder(v);
    }

    public void deleteItem(int position){
        getSnapshots().getSnapshot(position).getReference().delete()
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("tag", "DocumentSnapshot successfully deleted!");
                }
            })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("tag", "Error deleting document", e);
                    }
                });
    }


    class JourneyItemHolder extends RecyclerView.ViewHolder{
        TextView textViewTitle;
        TextView textViewSecretKey;

        public JourneyItemHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.journey_item_title);
            textViewSecretKey = itemView.findViewById(R.id.journey_item_reference);

            //catch a click wherever on a journey item card
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    //get a document snapshot at the position and send the data to another activity
                    //if we want to pass a context to our adapter we could call a startActivity
                    //better approach to send the click to the loan activity to send the click from
                    //the adapter to an activity we need an interface
                    if(position != RecyclerView.NO_POSITION && listener != null){
                        listener.onItemClick(getSnapshots().getSnapshot(position), position);
                    }
                }
            });

        }
    }
    //with this listener and this method, we can sent data from the adapter to the underland activity
    //that implements the interface below
    public interface OnItemClickListener{
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }
}
