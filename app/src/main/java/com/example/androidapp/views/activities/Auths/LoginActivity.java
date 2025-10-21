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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.androidapp.R;
import com.example.androidapp.views.activities.MainActivity;
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
        progressBar = findViewById(R.id.progressBar); // ✅ KHỞI TẠO MỚI
    }

    // kiem tra du lieu
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

    // ✅ HÀM QUẢN LÝ TRẠNG THÁI LOADING
    private void setLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnLogin.setText(""); // Ẩn chữ "Login"
            btnLogin.setEnabled(false); // Vô hiệu hóa nút
            // Tùy chọn: Vô hiệu hóa EditTexts để ngăn chỉnh sửa
            etEmail.setEnabled(false);
            etPassword.setEnabled(false);
            imgPasswordToggle.setEnabled(false);

        } else {
            progressBar.setVisibility(View.GONE);
            btnLogin.setText("Login"); // Hiện lại chữ "Login"
            btnLogin.setEnabled(true); // Kích hoạt lại nút
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
    // private void loginUser() {
    // String email = etEmail.getText().toString().trim();
    // String password = etPassword.getText().toString().trim();
    //
    // if (!validateInput(email, password)) {
    // return;
    // }
    //
    // // Gọi Firebase Auth để đăng nhập
    // mAuth.signInWithEmailAndPassword(email, password)
    // .addOnCompleteListener(this, task -> {
    // if (task.isSuccessful()) {
    // Log.d(TAG, "signInWithEmail:success");
    //
    // FirebaseUser user = mAuth.getCurrentUser();
    //
    // if (user != null) {
    // if (user.isEmailVerified()) {
    // // Email đã xác minh → Cho vào app
    // Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
    //
    // // Chuyển sang màn hình chính (MainActivity)
    // Intent intent = new Intent(this, MainActivity.class);
    // startActivity(intent);
    // finish(); // Đóng LoginActivity
    // } else {
    // // Email chưa xác minh
    // Toast.makeText(this,
    // "Vui lòng xác minh email trước khi đăng nhập.",
    // Toast.LENGTH_LONG).show();
    // }
    // }
    // } else {
    // Log.w(TAG, "signInWithEmail:failure", task.getException());
    // Toast.makeText(this, "Email hoặc mật khẩu không đúng!",
    // Toast.LENGTH_SHORT).show();
    // }
    // });
    // }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!validateInput(email, password)) {
            return;
        }

        setLoading(true); // ✅ BẮT ĐẦU TẢI (SAU KHI VALIDATE)

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    try {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                if (user.isEmailVerified()) {
                                    try {
                                        CheckUserRoleActivity(user.getUid());
                                    } catch (Exception e) {
                                        // Guard against unexpected crashes inside role check
                                        setLoading(false);
                                        Log.e(TAG, "Error during role check", e);
                                        Toast.makeText(this, "Lỗi khi xác thực quyền người dùng.", Toast.LENGTH_LONG)
                                                .show();
                                    }
                                } else {
                                    // Lỗi: Chưa xác minh email
                                    setLoading(false); // ✅ KẾT THÚC TẢI

                                    Toast.makeText(this,
                                            "Vui lòng xác minh email trước khi đăng nhập.",
                                            Toast.LENGTH_LONG).show();
                                }
                            } else {
                                // Lỗi không xác định
                                setLoading(false); // ✅ KẾT THÚC TẢI
                                Log.w(TAG, "signInWithEmail: success but user is null");
                            }
                        } else {
                            // Lỗi: Sai Email/Password
                            setLoading(false); // ✅ KẾT THÚC TẢI

                            Toast.makeText(this, "Email hoặc mật khẩu không đúng!",
                                    Toast.LENGTH_SHORT).show();
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                        }
                    } catch (Exception e) {
                        setLoading(false);
                        Log.e(TAG, "Unexpected error in sign-in completion", e);
                        Toast.makeText(this, "Lỗi không xác định khi đăng nhập", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Ensure any async failure is logged and UI reset
                    setLoading(false);
                    Log.e(TAG, "signInWithEmailAndPassword failed", e);
                    Toast.makeText(this, "Lỗi khi đăng nhập: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // check role
    private void CheckUserRoleActivity(String userId) {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        if (role != null) {
                            if (role.equals("admin")) {
                                Toast.makeText(this, "Xin chào Admin!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, AdminActivity.class));
                            } else {
                                Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, HomeActivity.class));
                            }
                            finish();
                        } else {
                            // Lỗi: Không tìm thấy role
                            setLoading(false); // ✅ KẾT THÚC TẢI

                            Toast.makeText(this, "Không tìm thấy quyền người dùng!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Lỗi: Không tìm thấy role
                        setLoading(false); // ✅ KẾT THÚC TẢI

                        Toast.makeText(this, "Người dùng không tồn tại trong Firestore!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Lỗi: Không tìm thấy role
                    setLoading(false); // ✅ KẾT THÚC TẢI

                    Toast.makeText(this, "Lỗi khi đọc dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error getting role", e);
                });
    }

    // hiện pass
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