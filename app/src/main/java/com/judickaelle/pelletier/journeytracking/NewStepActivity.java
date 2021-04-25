package com.judickaelle.pelletier.journeytracking;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class NewStepActivity extends AppCompatActivity {
    private EditText newStepTitle, newStepLatitude, newStepLongitude;
    private NumberPicker newStepNumber;
    private Button getGPSposition;
    private String idJourney;

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_step);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_close_24);
        setTitle(getString(R.string.add_new_step_title));
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(getResources().getString(R.color.colorPrimary))));

        newStepTitle = findViewById(R.id.newStepItem_title);
        newStepLatitude = findViewById(R.id.newStepItem_latitude);
        newStepLongitude = findViewById(R.id.newStepItem_longitude);
        newStepNumber = findViewById(R.id.newStepItem_number);
        getGPSposition = findViewById(R.id.btn_getGPSposition);

        idJourney = getIntent().getExtras().getString("idJourney");

        //it is possible to create only 11 steps for one journey
        newStepNumber.setMinValue(1);
        newStepNumber.setMaxValue(11);

        getGPSposition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO implement to obtain the gsp coodonate
                newStepLatitude.setText("latitude obtenue");
                newStepLongitude.setText("longitude obtenue");
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
        int stepNumber = newStepNumber.getValue();

        if(stepTitle.trim().isEmpty()||latitude.trim().isEmpty() || longitude.trim().isEmpty()){
            Toast.makeText(this, R.string.error_creation_new_step, Toast.LENGTH_SHORT).show();
            return;
        }

        CollectionReference stepbookRef = FirebaseFirestore.getInstance().collection("StepBook");
        stepbookRef.add(new Step(stepTitle, id_journey, latitude, longitude, stepNumber));
        Toast.makeText(this, R.string.step_added, Toast.LENGTH_SHORT).show();
        finish();

    }
}