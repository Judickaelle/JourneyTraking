package com.judickaelle.pelletier.journeytracking.mainactivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.judickaelle.pelletier.journeytracking.Login.Login;
import com.judickaelle.pelletier.journeytracking.R;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import static com.judickaelle.pelletier.journeytracking.R.*;

public class NavigationDrawerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    private View header;
    private Bundle emailBundle = new Bundle();

    private FirebaseAuth firebaseAuth;

    public TextView nav_header_subtitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_main);

        Toolbar toolbar = findViewById(id.toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(id.drawer_layout);

        //create view for the header
        NavigationView navigationView = findViewById(id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        header = navigationView.getHeaderView(0);
        nav_header_subtitle = header.findViewById(id.nav_header_subtitle);

        //to have a toogle that enable on a much easier way the navigationView's opening
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                string.navigation_drawer_open, string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //when we start the activity, the home fragment is showing up
        if(savedInstanceState == null){
            getSupportFragmentManager().beginTransaction().replace(id.fragment_container,
                    new HomeFragment()).commit();
            navigationView.setCheckedItem(id.nav_home);
            setTitle(getString(string.menu_home));
        }

        //extract information from user
        firebaseAuth = FirebaseAuth.getInstance();
        nav_header_subtitle.setText(firebaseAuth.getCurrentUser().getEmail());
        //emailBundle.putString("ownerEmail", firebaseAuth.getCurrentUser().getEmail());

    }

    //the method that enable to pass fom one fragment to another one
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_home:
                //HomeFragment homeFragment = new HomeFragment();
                //homeFragment.setArguments(emailBundle);
                getSupportFragmentManager().beginTransaction().replace(id.fragment_container, new HomeFragment()).commit();
                break;
            case R.id.nav_sign_out:
                logout();
                Toast.makeText(this, string.press_log_out, Toast.LENGTH_SHORT).show();
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }else{
            super.onBackPressed();
        }
    }

    //method to logout from the application and return to the login page
    private void logout(){
        FirebaseAuth.getInstance().signOut();
        GoogleSignIn.getClient(this, new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build())
                .signOut().addOnSuccessListener(aVoid -> {
                    startActivity(new Intent(getApplicationContext(), Login.class));
                    finish();
                }).addOnFailureListener(e -> Toast.makeText(NavigationDrawerActivity.this,
                getString(R.string.signout_failed), Toast.LENGTH_SHORT).show());
    }
}