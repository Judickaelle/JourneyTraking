package com.judickaelle.pelletier.journeytracking.mainactivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.judickaelle.pelletier.journeytracking.R;
import com.judickaelle.pelletier.journeytracking.journey.JourneyItem;
import com.judickaelle.pelletier.journeytracking.step.Step;

import java.util.ArrayList;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentSnapshot document;
    private CollectionReference stepbookRef;
    private String accesKey;
    private Double latOne, longOne, latitude, longitude;

    private ArrayList<Step> listStep = new ArrayList<Step>();
    private ArrayList<Marker> listMarker = new ArrayList<Marker>();

    private JourneyItem journeyItem;
    private Step myStep, stepOne;
    private Query query;

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
                if (task.isSuccessful()) {
                    document = task.getResult();
                    if (document.exists()) {
                        journeyItem = document.toObject(JourneyItem.class);

                        //set title and subtitle on the action bar
                        actionBar.setTitle(getString(R.string.map_title) + " " + journeyItem.getTitle());
                        actionBar.setSubtitle(accesKey);
                    } else {
                        Log.e("tag", "no such document");
                    }
                } else {
                    Log.e("tag", "get document failed with : " + task.getException());
                }
            }
        });

        getStep();
    }


    private void getStep() {
        //get step of the journey thanks to the idJourney
        query = stepbookRef
                .whereEqualTo("id_journey", accesKey)
                .orderBy("stepNumber", Query.Direction.ASCENDING);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    //Log.d("tag", "taille document récupérée  : " + task.getResult().size());
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        Step step = documentSnapshot.toObject(Step.class);
                        //Log.d("tag", "numéro step  : " + step.getStepNumber() + "  titre : " + step.getStepTitle());
                        listStep.add(documentSnapshot.toObject(Step.class));
                    }
                    //Log.d("tag", "list size : " + listStep.size());
                } else {
                    Log.e("tag", "Error getting documents: ", task.getException());
                }
            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Log.d("tag", "list size : " + listStep.size());

        setMap();
    }

    private void setMap() {
        if(listStep.size()>0){
            //define a polyline
            PolylineOptions polylineOptions = new PolylineOptions();

            //initialization of the map and focus on the first marker
            stepOne = listStep.get(0);
            latOne = Double.parseDouble(stepOne.getLatitude());
            longOne = Double.parseDouble(stepOne.getLongitude());
            listMarker.add(createMarker(mMap, latOne, longOne, stepOne.getStepTitle()));

            //move the camera
            LatLng initial = new LatLng(latOne, longOne);
            polylineOptions.add(initial);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(initial));

            //zoom en 10x
            CameraPosition cameraPos = new CameraPosition.Builder().target(initial).zoom(10).build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPos));

            //add the other step on the map
            for (int i = 1; i < listStep.size(); i++) {
                myStep = listStep.get(i);
                Log.d("tag", "step " + myStep.getStepTitle());
                latitude = Double.parseDouble(myStep.getLatitude());
                longitude = Double.parseDouble(myStep.getLongitude());
                polylineOptions.add(new LatLng(latitude, longitude));
                Marker marker = createMarker(mMap, latitude, longitude, myStep.getStepTitle());
                listMarker.add(marker);
            }
            mMap.addPolyline(polylineOptions);
        }
    }


    protected Marker createMarker(GoogleMap googleMap, double lat, double lng, String title) {
        Log.d("tag", "step " + title + " added to the map");
        return googleMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(title).visible(true));
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