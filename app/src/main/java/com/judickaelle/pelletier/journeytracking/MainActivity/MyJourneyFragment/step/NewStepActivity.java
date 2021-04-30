package com.judickaelle.pelletier.journeytracking.MainActivity.MyJourneyFragment.step;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.judickaelle.pelletier.journeytracking.R;

public class NewStepActivity extends AppCompatActivity {
    //initialize variable
    private EditText newStepTitle, newStepLatitude, newStepLongitude;
    private TextView newStepNumber;
    private Button getGPSlocation;
    private String idJourney;
    FusedLocationProviderClient fusedLocationProviderClient;

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_step);

        //define action bar of this activity
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_close_24);
        setTitle(getString(R.string.add_new_step_title));
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(getResources().getString(R.color.colorPrimary))));

        //assign variable
        newStepTitle = findViewById(R.id.newStepItem_title);
        newStepLatitude = findViewById(R.id.newStepItem_latitude);
        newStepLongitude = findViewById(R.id.newStepItem_longitude);
        newStepNumber = findViewById(R.id.newStepItem_number);
        getGPSlocation = findViewById(R.id.btn_getGPSposition);
        idJourney = getIntent().getExtras().getString("idJourney");
        newStepNumber.setText(getIntent().getExtras().getString("stepNumber"));

        //initialize fusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //it is possible to create only 8 waypoints for one journey with the free google API

        //method to get the user location
        getGPSlocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Check permission
                if (ActivityCompat.checkSelfPermission(NewStepActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    //when permission granded
                    getLocation();
                } else {
                    ActivityCompat.requestPermissions(NewStepActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void getLocation() {
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                //initialize location
                Location location = task.getResult();
                if (location != null){
                    //initialize variable with the location
                    newStepLatitude.setText(String.valueOf(location.getLatitude()));
                    newStepLongitude.setText(String.valueOf(location.getLongitude()));
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.new_journey_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.save_journey:
                saveStep();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveStep() {
        String stepTitle = newStepTitle.getText().toString();
        String latitude = newStepLatitude.getText().toString();
        String longitude = newStepLongitude.getText().toString();
        String id_journey = idJourney;
        int stepNumber = Integer.parseInt(newStepNumber.getText().toString());

        //verification that all steps are completed
        if(stepTitle.trim().isEmpty()){
            newStepTitle.setError(getString(R.string.error_creation_new_step));
        }
        if(latitude.trim().isEmpty()){
            newStepLatitude.setError(getString(R.string.error_creation_new_step));
        }
        if(longitude.trim().isEmpty()){
            newStepLongitude.setError(getString(R.string.error_creation_new_step));
        }

        CollectionReference stepbookRef = FirebaseFirestore.getInstance().collection("StepBook");
        stepbookRef.add(new Step(stepTitle, id_journey, latitude, longitude, stepNumber));
        Toast.makeText(this, R.string.step_added, Toast.LENGTH_SHORT).show();
        finish();

    }
}