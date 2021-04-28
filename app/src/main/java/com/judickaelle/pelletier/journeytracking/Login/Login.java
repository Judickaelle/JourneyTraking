package com.judickaelle.pelletier.journeytracking.Login;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.judickaelle.pelletier.journeytracking.mainactivity.MapsActivity;
import com.judickaelle.pelletier.journeytracking.mainactivity.NavigationDrawerActivity;
import com.judickaelle.pelletier.journeytracking.R;

public class  Login extends AppCompatActivity {
    private TextView loginToRegiter;
    private EditText txtLoginWithEmail, txtLoginWithPwd, txtLoginGuest;

    private SignInButton googleSignIn;
    private Button memberSignIn, secretCodeSignIn;

    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth firebaseAuth;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        txtLoginWithEmail = findViewById(R.id.loginWithEmail);
        txtLoginWithPwd = findViewById(R.id.loginEnterPassword);
        txtLoginGuest = findViewById(R.id.loginSecretCode);

        googleSignIn = findViewById(R.id.googleSignInButton);
        memberSignIn = findViewById(R.id.btnSignInMember);
        secretCodeSignIn = findViewById(R.id.btnSignInGuest);
        loginToRegiter = findViewById(R.id.txtLoginToRegister);

        firebaseAuth = FirebaseAuth.getInstance();

        //connection of user
        UserConnection();

        //go to the register activity
        OpenRegisterActivity();

        //go to the guest space
        OpenMapActivity();

        }

    private void OpenMapActivity() {
        secretCodeSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO vérifier que la clé existe
                Intent i = new Intent(Login.this, MapsActivity.class);
                i.putExtra("accessKey", txtLoginGuest.getText().toString().trim());
                startActivity(i);
            }
        });
    }

    public void UserConnection(){
        ConnectionWithGoogle();
        ConnectionWithEmailAndPwd();
    }

    public void ConnectionWithEmailAndPwd(){
        //validate the user input and if the infomations are correct we let them go to the main activity
        memberSignIn.setOnClickListener(v -> {
            String emailAdress = txtLoginWithEmail.getText().toString().trim();
            String password = txtLoginWithPwd.getText().toString().trim();

            //we are checking if all the information are correct to allow the use's registering
            if(TextUtils.isEmpty(emailAdress)||!Patterns.EMAIL_ADDRESS.matcher(emailAdress).matches()){
                txtLoginWithEmail.setError(getString(R.string.register_email_error));
                return;
            }
            if(TextUtils.isEmpty(password)){
                txtLoginWithPwd.setError(getString(R.string.register_pwd_error));
                return;
            }

            //Authenticate the user
            firebaseAuth.signInWithEmailAndPassword(emailAdress, password).addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            Toast.makeText(Login.this, getString(R.string.authentication), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), NavigationDrawerActivity.class));
                        }else {
                            Toast.makeText(Login.this, getString(R.string.error) + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
            );
        });
    }

    public void ConnectionWithGoogle(){
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.IdTocken))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        //if the user is still login then we automatically go to the navigation activity
        if(signInAccount != null || firebaseAuth.getCurrentUser() != null){
            Toast.makeText(this, "user is Logged in Already ", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(), NavigationDrawerActivity.class));
        }

        //When google sign in button is clicked
        googleSignIn.setOnClickListener(v -> {
            Intent googlesign = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(googlesign, RC_SIGN_IN);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> signInTask = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount signInAccount = signInTask.getResult(ApiException.class);

                AuthCredential authCredential = GoogleAuthProvider.getCredential(signInAccount.getIdToken(), null);

                //register the user using firebaseAuth
                firebaseAuth.signInWithCredential(authCredential).addOnCompleteListener(task -> {
                    Toast.makeText(getApplicationContext(), R.string.authentication, Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), NavigationDrawerActivity.class));
                }).addOnFailureListener(e -> {

                });
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("tag", "Google sign in failed", e);
            }
        }
    }

    public void OpenRegisterActivity(){
        loginToRegiter.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), Register.class));
            finish();
        });
    }
}