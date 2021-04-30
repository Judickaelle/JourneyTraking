package com.judickaelle.pelletier.journeytracking.MainActivity.MyJourneyFragment.step;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.judickaelle.pelletier.journeytracking.R;

public class StepAdapter extends FirestoreRecyclerAdapter<Step, StepAdapter.StepHolder> {

    public StepAdapter(@NonNull FirestoreRecyclerOptions<Step> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull StepHolder holder, int position, @NonNull Step model) {
        holder.textViewStepTitle.setText(model.getStepTitle());
        holder.textViewStepLatitude.setText(model.getLatitude());
        holder.textViewStepLongitude.setText(model.getLongitude());
        holder.textViewStepNumber.setText(String.valueOf(model.getStepNumber()));
    }

    @NonNull
    @Override
    public StepHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.step_item,
                parent, false);
        return new StepHolder(v);
    }

    public void deleteItem(int position){
        getSnapshots().getSnapshot(position).getReference().delete();
    }

    class  StepHolder extends RecyclerView.ViewHolder{
        TextView textViewStepTitle;
        TextView textViewStepLatitude;
        TextView textViewStepLongitude;
        TextView textViewStepNumber;

        public StepHolder(@NonNull View itemView) {
            super(itemView);
            textViewStepTitle = itemView.findViewById(R.id.step_item_title);
            textViewStepLatitude = itemView.findViewById(R.id.step_item_latitude);
            textViewStepLongitude = itemView.findViewById(R.id.step_item_longitude);
            textViewStepNumber = itemView.findViewById(R.id.step_item_number);
        }
    }
}
