package com.judickaelle.pelletier.journeytracking;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
    private RecyclerView homeRecyclerView;

    SwipeRefreshLayout swipeRefreshLayout;

    private JourneyItemAdapter adapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_home, container, false);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        journeybookRef = db.collection("JourneyBook");

        //swipe to refresh the view
        swipeRefreshLayout = view.findViewById(R.id.swipeToRefresh_homeRecycleView);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        try {
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            ownerEmail = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getEmail();
            Log.d("tag", "ownerEmail recuperer : " + ownerEmail);
        }catch (Exception ignored){}

        //start the activity to create a new JourneyItem
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

    private void refresh(){
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (Build.VERSION.SDK_INT >= 26) {
            ft.setReorderingAllowed(false);
        }
        ft.detach(this).attach(this).commit();
    }


    private void setUpJourneyRecyclerView(){
        Query query = journeybookRef
                .whereEqualTo("owner", ownerEmail)
                .orderBy("title", Query.Direction.ASCENDING);

        //how we get our query to the adapter
        FirestoreRecyclerOptions<JourneyItem> options = new FirestoreRecyclerOptions.Builder<JourneyItem>()
                .setQuery(query, JourneyItem.class)
                .build();

        adapter = new JourneyItemAdapter(options);

        homeRecyclerView = view.findViewById(R.id.home_recycler_view);
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
                //show an alert dialog to confirm the suppression
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setCancelable(true);
                builder.setTitle(R.string.suppression_item_title);
                builder.setMessage(R.string.suppression_item_message);
                builder.setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        adapter.deleteItem(viewHolder.getAdapterPosition());
                        refresh();
                    }
                });
                builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        }).attachToRecyclerView(homeRecyclerView);

        //This section handles what happens when you click on an item in the recyclerView list
        adapter.setOnItemClickListener(new JourneyItemAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                JourneyItem journeyItem = documentSnapshot.toObject(JourneyItem.class);
                String id = documentSnapshot.getId();
                documentSnapshot.getReference();
                Toast.makeText(getContext(), "title: "+ journeyItem.getTitle() + " owner : " + journeyItem.getOwner() + " ID: " + id, Toast.LENGTH_SHORT).show();
                Intent i = new Intent(getContext(), AddStepJourneyActivity.class);
                i.putExtra("journeyId", id);
                i.putExtra("journeyTitle", journeyItem.getTitle());
                i.putExtra("journeyOwner", journeyItem.getOwner());
                startActivity(i);
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
        adapter.stopListening();
    }


}
