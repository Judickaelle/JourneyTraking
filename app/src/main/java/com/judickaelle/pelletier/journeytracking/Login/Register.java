package com.judickaelle.pelletier.journeytracking.Login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.judickaelle.pelletier.journeytracking.MainActivity.NavigationDrawerActivity;
import com.judickaelle.pelletier.journeytracking.R;

public class Register extends AppCompatActivity {

    private EditText registerEmail, registerEnterPwd, registerRepeatPwd;
    private FirebaseAuth fAuth;
    private TextView registerToLogin;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        registerEmail = findViewById(R.id.registerEmailAddress);
        registerEnterPwd = findViewById(R.id.registerEnterPassword);
        registerRepeatPwd = findViewById(R.id.registerRepeatPassword);
        Button btnCreateAccount = findViewById(R.id.btnCreateAccount);
        registerToLogin = findViewById(R.id.txtViewRegisterLogin);

        fAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.registerProgressBar);

        //check if the user have already created an account
        if(fAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(), NavigationDrawerActivity.class));
        }

        //handle the register bouton
        btnCreateAccount.setOnClickListener(v -> {
            String emailAdress = registerEmail.getText().toString().trim();
            String password = registerEnterPwd.getText().toString().trim();
            String repeatPassword = registerRepeatPwd.getText().toString().trim();

            //we are checking if all the information are correct to allow the use's registering
            if(TextUtils.isEmpty(emailAdress)||!Patterns.EMAIL_ADDRESS.matcher(emailAdress).matches()){
                registerEmail.setError(getString(R.string.register_email_error));
                return;
            }
            if(TextUtils.isEmpty(password)){
                registerEnterPwd.setError(getString(R.string.register_pwd_error));
                return;
            }else if (password.length() <= 5){
                registerEnterPwd.setError(getString(R.string.register_pwd_length_error));
                return;
            }
            if(password.equals(repeatPassword)){
                Log.d("tag", "pwd = " + password + " repeat pwd = " + repeatPassword);
            }else{
                registerRepeatPwd.setError(getString(R.string.register_pwd_not_equals));
                return;
            }

            //all conditions are right. we can start register the user in Firebase
            fAuth.createUserWithEmailAndPassword(emailAdress,password).addOnCompleteListener(task -> {
               if(task.isSuccessful()){
                   progressBar.setVisibility(View.VISIBLE);
                   Toast.makeText(Register.this, R.string.register_user_created, Toast.LENGTH_SHORT).show();
                   startActivity(new Intent(getApplicationContext(), NavigationDrawerActivity.class));
                   finish();
               }else {
                   Log.e("tag", "erreur : " + task.getException());
                   try{
                       throw task.getException();
                   }catch (FirebaseAuthWeakPasswordException e){
                       registerEnterPwd.getText().clear();
                       registerRepeatPwd.getText().clear();
                       registerEnterPwd.setError(getString(R.string.register_pwd_length_error));
                   }catch (FirebaseAuthUserCollisionException e){
                       registerEmail.getText().clear();
                       registerEnterPwd.getText().clear();
                       registerRepeatPwd.getText().clear();
                       registerEmail.setError(getString(R.string.firebase_collision_error));
                   }catch (Exception e){
                       Toast.makeText(Register.this, getString(R.string.error) + e, Toast.LENGTH_LONG).show();
                   }

               }
            });
        });

        //go to the login activity
        OpenLoginactivity();
    }

    public void OpenLoginactivity(){
        registerToLogin.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), Login.class));
            finish();
        });
    }
}