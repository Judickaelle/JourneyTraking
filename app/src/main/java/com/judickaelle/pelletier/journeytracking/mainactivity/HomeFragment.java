package com.judickaelle.pelletier.journeytracking.mainactivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.judickaelle.pelletier.journeytracking.R;
import com.judickaelle.pelletier.journeytracking.journey.JourneyItem;
import com.judickaelle.pelletier.journeytracking.journey.JourneyItemAdapter;
import com.judickaelle.pelletier.journeytracking.journey.NewJourneyItemActivity;

import java.util.Objects;

public class HomeFragment extends Fragment{

    private CollectionReference journeybookRef, stepbookRef;
    private View view;
    private String ownerEmail;
    private RecyclerView homeRecyclerView;

    private SwipeRefreshLayout swipeRefreshLayout;

    private JourneyItemAdapter adapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_home, container, false);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        journeybookRef = db.collection("JourneyBook");
        stepbookRef = db.collection("StepBook");

        //swipe to refresh the view
        swipeRefreshLayout = view.findViewById(R.id.swipeToRefresh_homeRecycleView);
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
        onStop();
        onStart();
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

            @SuppressLint("ResourceType")
            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                final int DIRECTION_RIGHT = 1;
                final int DIRECTION_LEFT = 0;
                if(actionState == ItemTouchHelper.ACTION_STATE_SWIPE && isCurrentlyActive){
                    int direction = dX > 0 ? DIRECTION_RIGHT : DIRECTION_LEFT;
                    int absoluteDisplacement = (int) Math.abs(dX);

                    if(direction == DIRECTION_LEFT){

                        //draw background
                        View itemView = viewHolder.itemView;
                        ColorDrawable bg = new ColorDrawable();
                        bg.setColor(Color.RED);
                        bg.setBounds(itemView.getLeft(), itemView.getTop(), itemView.getRight(), itemView.getBottom());
                        bg.draw(c);
                    }


                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
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
                        //get the documentId that user wants to delete
                        DocumentSnapshot documentSnapshot = adapter.getSnapshots().getSnapshot(viewHolder.getAdapterPosition());
                        String idDoc = documentSnapshot.getId();
                        //delete all step link to the journey
                        //first get the document step link to the particular journey
                        Query query1 = stepbookRef.whereEqualTo("id_journey", idDoc);
                        query1.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if(task.isSuccessful()){
                                    for(DocumentSnapshot document : task.getResult()){
                                        document.getReference().delete();
                                    }
                                    //then we delete the journey document
                                    adapter.deleteItem(viewHolder.getAdapterPosition());
                                }else {
                                    Log.d("tag", "error when trying to suppress the document " +
                                            "step link to the journey : ", task.getException());
                                }
                            }
                        });
                    }
                });
                builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        refresh();
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
                //creation of an journeyItem to be able to retrieve information from the
                //different parameters
                JourneyItem journeyItem = documentSnapshot.toObject(JourneyItem.class);
                String id = documentSnapshot.getId();
                documentSnapshot.getReference();
                Intent i = new Intent(getContext(), AddStepJourneyActivity.class);
                i.putExtra("journeyId", id);
                i.putExtra("journeyTitle", journeyItem.getTitle());
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