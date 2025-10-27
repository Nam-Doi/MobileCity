package com.example.androidapp.views.activities.Auths;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
// Import này không được dùng, nhưng tôi giữ lại
// import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog; // Import thiếu cho AlertDialog

import com.example.androidapp.R;
import com.example.androidapp.models.users;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import com.example.androidapp.views.activities.Auths.AdminActivity;
import com.example.androidapp.views.activities.Auths.HomeActivity;
import com.example.androidapp.views.activities.Auths.DisabledUserActivity;
import com.example.androidapp.views.activities.Auths.SignUpActivity; // Đã có
import com.example.androidapp.views.activities.Auths.ForgotPasswordActivity; // Đã có


public class LoginActivity extends AppCompatActivity {
    private EditText etEmail;
    private EditText etPassword;
    private TextView tvForgotPassword;
    // btnSignup đã được đổi thành MaterialButton trong file XML của bạn
    private MaterialButton btnLogin, btnSignup;
    private FirebaseAuth mAuth;
    private ImageView imgPasswordToggle;
    private boolean isPasswordVisible = false;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ProgressBar progressBar;
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        initViews();
        setupListeners();
        setupPasswordToggle();
        loadSavedCredentials();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        btnSignup = findViewById(R.id.btnSignUp);
        btnLogin = findViewById(R.id.btnLogin);
        imgPasswordToggle = findViewById(R.id.ivPasswordToggle);
        progressBar = findViewById(R.id.progressBar);
    }
    //kiem tra email va password hop le
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

    // ham ghi nho dang nhap
    private void saveLoginCredentials(String email, String password){
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putString(KEY_EMAIL, email)
                .putString(KEY_PASSWORD, password)
                .apply();
    }

    private void loadSavedCredentials(){
        var prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedEmail = prefs.getString(KEY_EMAIL, "");
        String savedPassword = prefs.getString(KEY_PASSWORD, "");
        if(!savedEmail.isEmpty() && !savedPassword.isEmpty()){
            etEmail.setText(savedEmail);
            etPassword.setText(savedPassword);
        }
    }

    private void showSaveCredentialsDialog(String email, String password, Runnable onContinue) {
        // Sử dụng AlertDialog đã import
        new AlertDialog.Builder(this)
                .setTitle("Lưu đăng nhập?")
                .setMessage("Bạn có muốn lưu tài khoản này khum?")
                .setPositiveButton("Lưu", (dialog, which) -> {
                    saveLoginCredentials(email, password);
                    onContinue.run();
                })
                .setNegativeButton("Không", (dialog, which) -> {
                    saveLoginCredentials("", "");
                    onContinue.run();
                })
                .setCancelable(false)
                .show();
    }


    private void setLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnLogin.setText("");
            btnLogin.setEnabled(false);
            etEmail.setEnabled(false);
            etPassword.setEnabled(false);
            imgPasswordToggle.setEnabled(false);
            // Bạn nên vô hiệu hóa cả các nút khác
            btnSignup.setEnabled(false);
            tvForgotPassword.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnLogin.setText("Login");
            btnLogin.setEnabled(true);
            etEmail.setEnabled(true);
            etPassword.setEnabled(true);
            imgPasswordToggle.setEnabled(true);
            // Kích hoạt lại
            btnSignup.setEnabled(true);
            tvForgotPassword.setEnabled(true);
        }
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> loginUser());
        btnSignup.setOnClickListener(v -> {
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
    }

    private void CheckUserStatusAndRole(String userId) {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Chuyển document sang model 'users' (Logic từ nhánh remote)
                        users user = documentSnapshot.toObject(users.class);

                        if (user != null) {
                            String role = user.getRole();
                            boolean isActive = user.isActive(); // Lấy trạng thái active (Logic từ nhánh remote)

                            // Lấy thông tin đăng nhập (Logic từ nhánh HEAD)
                            String email = etEmail.getText().toString();
                            String password = etPassword.getText().toString();
                            var prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                            String savedEmail = prefs.getString(KEY_EMAIL, "");

                            // Tạo một hành động (Runnable) để thực hiện sau khi dialog (nếu có)
                            Runnable navigateAction = () -> {
                                setLoading(false); // Dừng loading TẠI ĐÂY

                                // Logic điều hướng (Từ nhánh remote)
                                if ("admin".equals(role)) {
                                    Toast.makeText(this, "Xin chào Admin!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(this, AdminActivity.class));
                                    finish();
                                } else {
                                    if (isActive) {
                                        Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(this, HomeActivity.class));
                                        finish();
                                    } else {
                                        Toast.makeText(this, "Tài khoản của bạn đã bị vô hiệu hóa.", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(this, DisabledUserActivity.class));
                                        finish();
                                    }
                                }
                            };

                            // Logic kiểm tra và hiển thị dialog (Từ nhánh HEAD)
                            if(savedEmail.isEmpty() || !savedEmail.equals(email)){
                                showSaveCredentialsDialog(email, password, navigateAction);
                            } else {
                                navigateAction.run(); // Nếu đã lưu thì chạy luôn
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


    // hiện pass
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