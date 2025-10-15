package com.example.androidapp.views.activities;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.androidapp.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;

public class LoginActivity extends AppCompatActivity {
    private EditText etEmail;
    private EditText etPassword;
    private TextView tvForgotPassword;
    private TextView tvSignUp;
    private MaterialButton btnLogin;
    private FirebaseAuth mAuth;
    private ImageView imgPasswordToggle;
    private boolean isPasswordVisible = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        initViews();
        setupListeners();
        setupPasswordToggle();
    }
    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvSignUp = findViewById(R.id.tvSignUp);
        btnLogin = findViewById(R.id.btnLogin);
        imgPasswordToggle = findViewById(R.id.ivPasswordToggle);

    }
    //kiem tra du lieu
    private boolean validateInput(String email, String password) {
        if (email.isEmpty()) {
            etEmail.setError("Vui lòng nhập email");
            etEmail.requestFocus();
            return false;
        }
        if (password.isEmpty()) {
            etPassword.setError("Vui lòng nhập mật khẩu");
            etPassword.requestFocus();
            return false;
        }
        return true;
    }
    private void setupListeners() {
        btnLogin.setOnClickListener(v -> loginUser());
        tvSignUp.setOnClickListener(v -> {
            startActivity(new Intent(this, SignUpActivity.class));
        });
        tvForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(this, ForgotPasswordActivity.class));
        });
    }
    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!validateInput(email, password)) {
            return;
        }

        // Gọi Firebase Auth để đăng nhập
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithEmail:success");

                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user != null) {
                            if (user.isEmailVerified()) {
                                // Email đã xác minh → Cho vào app
                                Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                                // Chuyển sang màn hình chính (MainActivity)
                                Intent intent = new Intent(this, MainActivity.class);
                                startActivity(intent);
                                finish(); // Đóng LoginActivity
                            } else {
                                // Email chưa xác minh
                                Toast.makeText(this,
                                        "Vui lòng xác minh email trước khi đăng nhập.",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(this, "Email hoặc mật khẩu không đúng!",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
    //hiện pass
    private void setupPasswordToggle() {
        imgPasswordToggle.setOnClickListener(v -> {
            if (isPasswordVisible) {
                // Đang hiện → Chuyển sang ẩn
                etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                imgPasswordToggle.setImageResource(R.drawable.ic_visibility_off);
                isPasswordVisible = false;
            } else {
                // Đang ẩn → Chuyển sang hiện
                etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                imgPasswordToggle.setImageResource(R.drawable.ic_visibility);
                isPasswordVisible = true;
            }

            // Đưa con trỏ về cuối text
            etPassword.setSelection(etPassword.getText().length());
        });
    }

}