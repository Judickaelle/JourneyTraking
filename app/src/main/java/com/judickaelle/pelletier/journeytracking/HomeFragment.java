package com.judickaelle.pelletier.journeytracking;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Objects;

public class HomeFragment extends Fragment{

    private CollectionReference journeybookRef;
    private View view;
    private String ownerEmail;

    private JourneyItemAdapter adapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_home, container, false);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        journeybookRef = db.collection("JourneyBook");

        try {
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            ownerEmail = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getEmail();
            Log.d("tag", "ownerEmail recuperer : " + ownerEmail);
        }catch (Exception ignored){}

        //start th activity to create a new JourneyItem
        FloatingActionButton buttonAddJourney = view.findViewById(R.id.btn_home_add_journey);
        buttonAddJourney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), NewJourneyItemActivity.class).putExtra("ownerEmail", ownerEmail));
            }
        });

        //connect adapter to the recycler view
        setUpJourneyRecyclerView();
        return view;
    }

    private void setUpJourneyRecyclerView(){
        Query query = journeybookRef
                .orderBy("title", Query.Direction.ASCENDING)
                .whereEqualTo("owner", ownerEmail);

        //how we get our query to the adapter
        FirestoreRecyclerOptions<JourneyItem> options = new FirestoreRecyclerOptions.Builder<JourneyItem>()
                .setQuery(query, JourneyItem.class)
                .build();

        adapter = new JourneyItemAdapter(options);

        RecyclerView homeRecyclerView = view.findViewById(R.id.home_recycler_view);
        homeRecyclerView.setHasFixedSize(true);
        homeRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        homeRecyclerView.setAdapter(adapter);

        //delete an item on swiped
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                adapter.deleteItem(viewHolder.getAdapterPosition());
            }
        }).attachToRecyclerView(homeRecyclerView);

        //get some result when a card is clicked
        adapter.setOnItemClickListener(new JourneyItemAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                JourneyItem journeyItem = documentSnapshot.toObject(JourneyItem.class);
                String id = documentSnapshot.getId();
                documentSnapshot.getReference();
                Toast.makeText(getContext(), "Position: "+ position + " ID: " + id, Toast.LENGTH_SHORT).show();
            }
        });
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
