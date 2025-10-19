package com.example.androidapp.views.activities.Auths;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.androidapp.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {
    private static final String TAG = "ForgotPasswordActivity";
    private EditText etEmail;
    private MaterialButton btnResetPassword;
    private TextView tvBackToLogin;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_fogot_password);
        mAuth = FirebaseAuth.getInstance();
        initViews();
        setListeners();
    }
    private void initViews(){
        etEmail = findViewById(R.id.etEmail);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

    }
    private void sendResetPasswordEmail() {
        String email = etEmail.getText().toString().trim();
        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }
    }
    private void setListeners(){
        btnResetPassword.setOnClickListener(v -> sendPasswordResetEmail());
        tvBackToLogin.setOnClickListener(v -> {
            startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void sendPasswordResetEmail() {
        String email = etEmail.getText().toString();
        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Log.d(TAG, "Email sent to" + email);
                        Toast.makeText(ForgotPasswordActivity.this, "Email sent to" + email, Toast.LENGTH_LONG).show();
                    }else{
                        Log.w(TAG, "sendPasswordResetEmail:failure", task.getException());
                        Toast.makeText(ForgotPasswordActivity.this, "Email sent to" + email, Toast.LENGTH_LONG).show();
                    }
                });
    }
}