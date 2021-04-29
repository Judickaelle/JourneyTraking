package com.judickaelle.pelletier.journeytracking.mainactivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.judickaelle.pelletier.journeytracking.R;
import com.judickaelle.pelletier.journeytracking.step.NewStepActivity;
import com.judickaelle.pelletier.journeytracking.step.Step;
import com.judickaelle.pelletier.journeytracking.step.StepAdapter;

public class AddStepJourneyActivity extends AppCompatActivity {
    private TextView textViewJourneyAccesKey;
    private int nombreEtape;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference stepbookRef = db.collection("StepBook");
    private Query query;

    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton buttonAddStep, buttonPreviewJourney;

    private StepAdapter stepAdapter;

    @SuppressLint("ResourceType")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_step_journey_item);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        setTitle(getString(R.string.title_add_step_journey_activity));
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(getResources().getString(R.color.colorPrimary))));

        TextView textViewJourneyTitle = findViewById(R.id.JourneyItem_title);
        textViewJourneyAccesKey = findViewById(R.id.JourneyItem_owner);

        textViewJourneyAccesKey.setText(getIntent().getExtras().getString("journeyId"));
        textViewJourneyTitle.setText(getIntent().getExtras().getString("journeyTitle"));

        swipeRefreshLayout = findViewById(R.id.swipeToRefresh_stepRecycleView);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        buttonAddStep = findViewById(R.id.btn_JourneyItem_add_step);
        buttonPreviewJourney = findViewById(R.id.btn_Journey_preview);

        buttonListener();
        setUpRecyclerView();
        copyToClipboard();

    }

    private void buttonListener() {
        //will lead to the activity to add a new step to the journey
        buttonAddStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int nvNombreEtape;
                if(stepAdapter.getSnapshots().isEmpty()){
                    nvNombreEtape = 1;
                }else{
                    DocumentSnapshot lastDocument = stepAdapter.getSnapshots().getSnapshot(nombreEtape-1);
                    //get the stepNumber from le last documentSnapshot find
                    Step step = lastDocument.toObject(Step.class);
                    nvNombreEtape = step.getStepNumber()+1;
                }
                if(nombreEtape < 10){
                    try {
                        Intent i = new Intent(getApplicationContext(), NewStepActivity.class);
                        i.putExtra("idJourney", textViewJourneyAccesKey.getText().toString());
                        i.putExtra("stepNumber", String.valueOf(nvNombreEtape));
                        startActivity(i);
                    }catch (Exception e){
                        Log.e("tag", "error : " + e);
                    }

                }else{
                    Toast.makeText(getApplicationContext(), "you can't add more than 10 step", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //will lead to the activity to preview the journey with the different point on the map
        buttonPreviewJourney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    Intent i = new Intent(AddStepJourneyActivity.this, MapsActivity.class);
                    i.putExtra("accessKey", textViewJourneyAccesKey.getText().toString());
                    startActivity(i);
                }catch (Exception e){
                    Log.e("tag", "error : " + e);
                }

            }
        });
    }

    private void setUpRecyclerView() {
        query = stepbookRef
                .whereEqualTo("id_journey", textViewJourneyAccesKey.getText().toString())
                .orderBy("stepNumber" , Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Step> options = new FirestoreRecyclerOptions.Builder<Step>()
                .setQuery(query, Step.class)
                .build();

        stepAdapter = new StepAdapter(options);

        RecyclerView recyclerView = findViewById(R.id.step_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(stepAdapter);

        //delete an item on swiped left
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                //show an alert dialog to confirm the suppression
                AlertDialog.Builder builder = new AlertDialog.Builder(AddStepJourneyActivity.this);
                builder.setCancelable(true);
                builder.setTitle(R.string.suppression_item_title);
                builder.setMessage(R.string.suppression_item_message);
                builder.setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //When an item is deleted then all the other stepNumber are updated
                        int position = viewHolder.getAdapterPosition();
                        int endList = nombreEtape-2;
                        try {
                            for(int i=position; i<=endList; i++){
                                String nextDoc = stepAdapter.getSnapshots().getSnapshot(i+1).getId();
                                stepbookRef.document(nextDoc).update("stepNumber", i+1);
                            }
                        }catch (Exception e){
                            Log.e("tag", "error : " + e);
                        }

                        stepAdapter.deleteItem(position);
                        refresh();
                        itemCount(); //count the number of step in the selected journey
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
        }).attachToRecyclerView(recyclerView);
    }

    private void itemCount() {
        nombreEtape = 0;
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(DocumentSnapshot document : task.getResult()){
                        nombreEtape++;
                    }
                    Log.d("tag", "nombre d'item : " + nombreEtape);
                }else {
                    Log.d("tag", "error getting number document : ", task.getException());
                }
            }
        });
    }

    private void copyToClipboard(){
        textViewJourneyAccesKey.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //initialize variable
                ClipData myClip;
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                //copy the text inside the textViewJourneyId
                myClip = ClipData.newPlainText("journeyAccessKey", textViewJourneyAccesKey
                        .getText().toString());
                clipboard.setPrimaryClip(myClip);
                //return a Toast message
                Toast.makeText(getApplicationContext(), R.string.access_key_copy, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    private void refresh(){
        onStop();
        onStart();
    }

    @Override
    protected void onStart() {
        super.onStart();
        stepAdapter.startListening();
        itemCount(); //count the number of step in the selected journey
    }

    @Override
    protected void onStop() {
        super.onStop();
        stepAdapter.stopListening();
    }
}
