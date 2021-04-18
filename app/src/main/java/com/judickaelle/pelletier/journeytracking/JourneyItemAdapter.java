package com.judickaelle.pelletier.journeytracking;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class JourneyItemAdapter extends FirestoreRecyclerAdapter<JourneyItem, JourneyItemAdapter.JourneyItemHolder>{

    public JourneyItemAdapter(@NonNull FirestoreRecyclerOptions<JourneyItem> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull JourneyItemHolder holder, int position, @NonNull JourneyItem model) {
        holder.textViewTitle.setText(model.getTitle());
        holder.textViewSecretKey.setText(model.getSecretKey());
    }

    @NonNull
    @Override
    public JourneyItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.journey_item, parent, false);
        return new JourneyItemHolder(v);
    }

    class JourneyItemHolder extends RecyclerView.ViewHolder{
        TextView textViewTitle;
        TextView textViewSecretKey;

        public JourneyItemHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.journey_item_title);
            textViewSecretKey = itemView.findViewById(R.id.journey_item_reference);

        }
    }
}
