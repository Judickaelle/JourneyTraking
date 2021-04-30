package com.judickaelle.pelletier.journeytracking.MainActivity.MyJourneyFragment.journey;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.judickaelle.pelletier.journeytracking.R;

public class NewJourneyItemActivity extends AppCompatActivity {
    private EditText txtJourneyTitle;
    private TextView txtJourneyOwner;
    private String ownerEmail;

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_journey_item);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_close_24);
        setTitle(getString(R.string.add_journey));
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(getResources().getString(R.color.colorPrimary))));

        txtJourneyTitle = findViewById(R.id.newJourneyItem_title);
        txtJourneyOwner = findViewById(R.id.newJourneyItem_owner);

        ownerEmail = getIntent().getExtras().getString("ownerEmail");
        txtJourneyOwner.setText(ownerEmail);

    }

    //we will use our option menu in this activity
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.new_journey_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.save_journey:
                saveJourney();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveJourney() {
        String title = txtJourneyTitle.getText().toString();
        String owner = txtJourneyOwner.getText().toString();

        //if no title are enter then no Journey item is created
        if(title.trim().isEmpty()){
            txtJourneyTitle.setError(getString(R.string.error_newJourneyItem_titleOrSecretKeyEmpty));
            return;
        }

        CollectionReference journeybookRef = FirebaseFirestore.getInstance()
                .collection(("JourneyBook"));
        journeybookRef.add(new JourneyItem(title, owner));
        Toast.makeText(this, R.string.message_journey_added, Toast.LENGTH_SHORT).show();
        finish();
    }
}