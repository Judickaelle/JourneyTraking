package com.judickaelle.pelletier.journeytracking;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class HomeFragment extends Fragment {

    private CollectionReference journeybookRef;
    private View view;

    private JourneyItemAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_home, container, false);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        journeybookRef = db.collection("JourneyBook");


        //connect adapter to the recycler view
        setUpJourneyRecyclerView();
        return view;
    }

    private void setUpJourneyRecyclerView(){

        Query query = journeybookRef.orderBy("title", Query.Direction.ASCENDING);

        //how we get our query to the adapter
        FirestoreRecyclerOptions<JourneyItem> options = new FirestoreRecyclerOptions.Builder<JourneyItem>()
                .setQuery(query, JourneyItem.class)
                .build();

        adapter = new JourneyItemAdapter(options);

        RecyclerView homeRecyclerView = view.findViewById(R.id.home_recycler_view);
        homeRecyclerView.setHasFixedSize(true);
        homeRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        homeRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.startListening();
    }
}
