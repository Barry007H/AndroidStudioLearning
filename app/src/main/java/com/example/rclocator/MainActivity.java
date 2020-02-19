package com.example.rclocator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthSettings;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        progressBar = (ProgressBar) findViewById(R.id.progressBar_UsrReg);
        progressBar.setVisibility(View.INVISIBLE);
    }

    public void goLogin(View view) {
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
    }

    public void registerUser(View view) {
        // Getting email and password from edit texts
        String email = ((EditText) findViewById(R.id.editText2)).getText().toString();
        String password = ((EditText) findViewById(R.id.editText3)).getText().toString();

        // Checking if email and password text boxes are empty
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter email", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter password", Toast.LENGTH_LONG).show();
            return;
        }
        // If the email and password are not empty, displaying a progress bar
        progressBar.setVisibility(View.VISIBLE);
        // Creating a new user
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // Checking if success
                        if(task.isSuccessful()) {
                            progressBar.setVisibility(View.GONE);
                            // Display some message here
                            Toast.makeText(MainActivity.this, "Successfully registered",
                                    Toast.LENGTH_LONG).show();
                            startActivity(new Intent(MainActivity.this, LoginActivity.class));
                            finish();
                        } else {
                            // Display some message here
                            FirebaseAuthException e = (FirebaseAuthException) task.getException();
                            Toast.makeText(MainActivity.this, "Failed Registration: "
                                    + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
