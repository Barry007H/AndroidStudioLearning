package com.example.rclocator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();
    }

    public void goRegister(View view) {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
    }

    public void loginUser(View view) {
        // Getting email and password from edit texts
        String email = ((EditText) findViewById(R.id.editText_Log_email)).getText().toString();
        String password = ((EditText) findViewById(R.id.editText_Log_password)).getText().toString();

        // Checking if email and password text boxes are empty
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter email", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter password", Toast.LENGTH_LONG).show();
            return;
        }
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // Checking if success
                        if (task.isSuccessful()) {
                            // Display some message here
                            Toast.makeText(LoginActivity.this, "Successfully Logged in",
                                    Toast.LENGTH_LONG).show();
                            startActivity(new Intent(getApplicationContext(), Map_MainPage.class));
                            finish();
                        } else {
                            // Display some message here
                            FirebaseAuthException e = (FirebaseAuthException) task.getException();
                            Toast.makeText(LoginActivity.this, "Login Failed: "
                                    + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
