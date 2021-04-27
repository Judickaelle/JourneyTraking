package com.judickaelle.pelletier.journeytracking.mainactivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.judickaelle.pelletier.journeytracking.R;
import com.judickaelle.pelletier.journeytracking.journey.JourneyItem;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference stepbookRef;
    private String accesKey;

    private JourneyItem journeyItem;

    private GoogleMap mMap;

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //get the accessKey
        accesKey = getIntent().getExtras().getString("accessKey");

        //initialisation of the firestore variables
        DocumentReference myJourney = db.collection("JourneyBook").document(accesKey);
        stepbookRef = db.collection("StepBook");

        //display the action bar for the map activity
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_back);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(getResources().getString(R.color.colorPrimary))));

        myJourney.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()){
                        Log.d("tag", "Journey exist : " + document.getData());
                        journeyItem = document.toObject(JourneyItem.class);
                        Log.d("tag", "Journey title : "+ journeyItem.getTitle());

                        //set title and subtitle on the action bar
                        actionBar.setTitle(getString(R.string.map_title) + " " + journeyItem.getTitle());
                        actionBar.setSubtitle(accesKey);
                    }else {
                        Log.d("tag", "no such document");
                    }
                }else {
                    Log.d("tag", "get document failed with : "+ task.getException());
                }
            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        //TODO get step of the journey thanks to the idJourney
        Query query = stepbookRef
                .whereEqualTo("id_journey", accesKey)
                .orderBy("stepNumber" , Query.Direction.ASCENDING);

        //TODO for each step display marker on the map
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    //finish the activity on button back press
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}