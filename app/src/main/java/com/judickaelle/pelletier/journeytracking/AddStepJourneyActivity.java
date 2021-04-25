package com.judickaelle.pelletier.journeytracking;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class AddStepJourneyActivity extends AppCompatActivity {
    private TextView textViewJourneyTitle;
    private TextView textViewJourneyOwner;
    private String idJourney;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference stepbookRef = db.collection("StepBook");

    private StepAdapter stepAdapter;

    @SuppressLint("ResourceType")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_step_journey_item);

        textViewJourneyTitle = findViewById(R.id.JourneyItem_title);
        textViewJourneyOwner = findViewById(R.id.JourneyItem_owner);

        idJourney = getIntent().getExtras().getString("journeyId");
        textViewJourneyOwner.setText(getIntent().getExtras().getString("journeyOwner"));
        textViewJourneyTitle.setText(getIntent().getExtras().getString("journeyTitle"));

        setUpRecyclerView();

    }

    private void setUpRecyclerView() {
        Query query = stepbookRef.orderBy("stepNumber" , Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Step> options = new FirestoreRecyclerOptions.Builder<Step>()
                .setQuery(query, Step.class)
                .build();

        stepAdapter = new StepAdapter(options);

        RecyclerView recyclerView = findViewById(R.id.step_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(stepAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        stepAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stepAdapter.stopListening();
    }
}
