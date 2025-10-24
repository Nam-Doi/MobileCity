package com.example.androidapp.views.activities.Auths;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.androidapp.R;
// BẠN CÓ THỂ CẦN THÊM CÁC IMPORT NÀY
import com.example.androidapp.models.users;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {
    private EditText etEmail;
    private EditText etPassword;
    private TextView tvForgotPassword;
    private TextView tvSignUp;
    private MaterialButton btnLogin;
    private FirebaseAuth mAuth;
    private ImageView imgPasswordToggle;
    private boolean isPasswordVisible = false;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ProgressBar progressBar;

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
        progressBar = findViewById(R.id.progressBar);
    }

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

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnLogin.setText("");
            btnLogin.setEnabled(false);
            etEmail.setEnabled(false);
            etPassword.setEnabled(false);
            imgPasswordToggle.setEnabled(false);

        } else {
            progressBar.setVisibility(View.GONE);
            btnLogin.setText("Login");
            btnLogin.setEnabled(true);
            etEmail.setEnabled(true);
            etPassword.setEnabled(true);
            imgPasswordToggle.setEnabled(true);
        }
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

        setLoading(true);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    // Cần một khối try-catch lớn ở đây để đảm bảo setLoading(false) luôn được gọi
                    try {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                if (user.isEmailVerified()) {
                                    // Đã xác minh email, kiểm tra vai trò và trạng thái
                                    CheckUserStatusAndRole(user.getUid());
                                } else {
                                    setLoading(false);
                                    Toast.makeText(this,
                                            "Vui lòng xác minh email trước khi đăng nhập.",
                                            Toast.LENGTH_LONG).show();
                                }
                            } else {
                                setLoading(false);
                                Log.w(TAG, "signInWithEmail: success but user is null");
                            }
                        } else {
                            setLoading(false);
                            Toast.makeText(this, "Email hoặc mật khẩu không đúng!",
                                    Toast.LENGTH_SHORT).show();
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                        }
                    } catch (Exception e) {
                        setLoading(false);
                        Log.e(TAG, "Unexpected error in sign-in completion", e);
                        Toast.makeText(this, "Lỗi không xác định khi đăng nhập", Toast.LENGTH_SHORT).show();
                    }
                });
        // Không cần .addOnFailureListener vì .addOnCompleteListener đã bao gồm cả trường hợp thất bại
    }

    // =================================================================
    // HÀM NÀY ĐÃ ĐƯỢC CẬP NHẬT HOÀN TOÀN
    // (Tên hàm cũ của bạn là CheckUserRoleActivity)
    // =================================================================
    private void CheckUserStatusAndRole(String userId) {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Chuyển document sang model 'users' để lấy dữ liệu an toàn
                        users user = documentSnapshot.toObject(users.class);

                        if (user != null) {
                            String role = user.getRole();
                            boolean isActive = user.isActive(); // Lấy trạng thái active

                            if ("admin".equals(role)) {
                                // 1. ADMIN: Luôn cho vào
                                setLoading(false);
                                Toast.makeText(this, "Xin chào Admin!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, AdminActivity.class));
                                finish();
                            } else {
                                // 2. USER: Kiểm tra trạng thái active
                                if (isActive) {
                                    // 2a. USER HOẠT ĐỘNG: vào màn hình chính
                                    setLoading(false);
                                    Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(this, HomeActivity.class));
                                    finish();
                                } else {
                                    // 2b. USER BỊ KHÓA: vào màn hình bị khóa
                                    setLoading(false);
                                    Toast.makeText(this, "Tài khoản của bạn đã bị vô hiệu hóa.", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(this, DisabledUserActivity.class));
                                    finish();
                                }
                            }
                        } else {
                            // Lỗi: Không thể map dữ liệu
                            setLoading(false);
                            Toast.makeText(this, "Lỗi: Không thể đọc dữ liệu người dùng.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Lỗi: Không tìm thấy document
                        setLoading(false);
                        Toast.makeText(this, "Người dùng không tồn tại trong Firestore!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Lỗi: Mất mạng, không có quyền, ...
                    setLoading(false);
                    Toast.makeText(this, "Lỗi khi đọc dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error getting user details", e);
                });
    }

    private void setupPasswordToggle() {
        imgPasswordToggle.setOnClickListener(v -> {
            if (isPasswordVisible) {
                etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                imgPasswordToggle.setImageResource(R.drawable.ic_visibility_off);
                isPasswordVisible = false;
            } else {
                etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                imgPasswordToggle.setImageResource(R.drawable.ic_visibility);
                isPasswordVisible = true;
            }
            etPassword.setSelection(etPassword.getText().length());
        });
    }
}