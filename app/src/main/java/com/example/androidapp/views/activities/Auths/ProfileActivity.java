package com.example.androidapp.views.activities.Auths;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.androidapp.R;
import com.example.androidapp.models.users;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    // Các Views
    private TextInputEditText editTextFullName, editTextEmail;
    private Button buttonEditSave, buttonLogout, buttonDeleteAccount;
    private ProgressBar progressBarProfile;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DocumentReference userDocRef;
    private FirebaseUser currentUser;
    private users userProfile;
    private Button buttonChangePassword;

    // Trạng thái
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Khởi tạo Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Ánh xạ Views
        editTextFullName = findViewById(R.id.editTextFullName);
        editTextEmail = findViewById(R.id.editTextEmail);
        buttonEditSave = findViewById(R.id.buttonEditSave);
        buttonChangePassword = findViewById(R.id.buttonChangePassword);
        buttonLogout = findViewById(R.id.buttonLogout);
        buttonDeleteAccount = findViewById(R.id.buttonDeleteAccount);
        progressBarProfile = findViewById(R.id.progressBarProfile); // Nếu layout có

        // Kiểm tra user đăng nhập
        if (currentUser == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy người dùng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userDocRef = db.collection("users").document(currentUser.getUid());

        loadUserProfile();
        setupClickListeners();
    }

    private void loadUserProfile() {
        setLoading(true);
        userDocRef.get().addOnSuccessListener(documentSnapshot -> {
            setLoading(false);
            if (documentSnapshot.exists()) {
                userProfile = documentSnapshot.toObject(users.class);
                if (userProfile != null) {
                    editTextFullName.setText(userProfile.getFullName());
                    editTextEmail.setText(userProfile.getEmail());
                }
            } else {
                Log.w(TAG, "Không tìm thấy document của user");
            }
        }).addOnFailureListener(e -> {
            setLoading(false);
            Log.e(TAG, "Lỗi khi tải thông tin user", e);
            Toast.makeText(ProfileActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupClickListeners() {

        buttonEditSave.setOnClickListener(v -> {
            if (isEditMode) {
                saveUserProfile();
            } else {
                toggleEditMode(true);
            }
        });

        buttonChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        });

        buttonLogout.setOnClickListener(v -> logoutUser());
        buttonDeleteAccount.setOnClickListener(v -> showDeleteAccountConfirmation());
    }

    private void toggleEditMode(boolean enable) {
        isEditMode = enable;
        editTextFullName.setEnabled(enable);
        editTextEmail.setEnabled(enable);

        if (enable) {
            buttonEditSave.setText("Lưu thay đổi");
            editTextFullName.requestFocus();
        } else {
            buttonEditSave.setText("Sửa thông tin");
        }
    }

    private void saveUserProfile() {
        String newFullName = editTextFullName.getText().toString().trim();
        String newEmail = editTextEmail.getText().toString().trim();

        if (newFullName.isEmpty()) { editTextFullName.setError("Tên không được để trống"); return; }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) { editTextEmail.setError("Email không hợp lệ"); return; }

        boolean nameChanged = !newFullName.equals(userProfile.getFullName());
        boolean emailChanged = !newEmail.equals(userProfile.getEmail());

        if (!nameChanged && !emailChanged) {
            toggleEditMode(false);
            return;
        }

        setLoading(true);

        if (emailChanged) {
            // Nếu email thay đổi, gọi hàm xác minh
            currentUser.verifyBeforeUpdateEmail(newEmail).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Cập nhật Firestore sau khi gửi email xác minh thành công
                    updateUserDocument(newFullName, newEmail, true);
                } else {
                    setLoading(false);
                    handleEmailUpdateError(task.getException());
                }
            });
        } else {
            // Nếu chỉ tên thay đổi
            updateUserDocument(newFullName, newEmail, false);
        }
    }

    // Hàm xử lý lỗi đổi email
    private void handleEmailUpdateError(Exception exception) {
        if (exception instanceof FirebaseAuthUserCollisionException) {
            Toast.makeText(this, "Email mới này đã được sử dụng.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Không thể gửi email xác minh: " + exception.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(TAG, "Lỗi verifyBeforeUpdateEmail", exception);
        }
    }

    private void updateUserDocument(String fullName, String email, boolean emailWasChanged) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", fullName);
        updates.put("email", email);

        userDocRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    setLoading(false);
                    if (userProfile != null) {
                        userProfile.setFullName(fullName);
                        userProfile.setEmail(email);
                        // Không cần cập nhật avatarUrl
                    }

                    String successMessage = "Cập nhật thành công!";
                    if (emailWasChanged) {
                        successMessage = "Thông tin đã cập nhật. Vui lòng kiểm tra email mới để xác minh!";
                    }
                    Toast.makeText(ProfileActivity.this, successMessage, Toast.LENGTH_LONG).show();
                    toggleEditMode(false);
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(ProfileActivity.this, "Cập nhật thất bại: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Lỗi update Firestore", e);
                });
    }

    // Hàm đăng xuất
    private void logoutUser() {
        mAuth.signOut();
        Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // Hàm hiển thị xác nhận xóa
    private void showDeleteAccountConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Xóa tài khoản")
                .setMessage("Bạn có chắc chắn muốn xóa tài khoản này vĩnh viễn không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    setLoading(true);
                    deleteAccount();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // Hàm xóa tài khoản
    private void deleteAccount() {
        userDocRef.delete()
                .addOnSuccessListener(aVoid -> currentUser.delete()
                        .addOnSuccessListener(aVoid2 -> {
                            setLoading(false);
                            Toast.makeText(ProfileActivity.this, "Tài khoản đã được xóa", Toast.LENGTH_SHORT).show();
                            logoutUser();
                        })
                        .addOnFailureListener(e -> {
                            setLoading(false);
                            Log.e(TAG, "Lỗi khi xóa tài khoản Auth", e);
                            Toast.makeText(ProfileActivity.this, "Lỗi xóa tài khoản Auth: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            // Xử lý re-authentication nếu cần
                        }))
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Log.e(TAG, "Lỗi khi xóa document Firestore", e);
                    Toast.makeText(ProfileActivity.this, "Lỗi xóa dữ liệu Firestore: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // Hàm bật/tắt ProgressBar
    private void setLoading(boolean isLoading) {
        if (progressBarProfile != null) {
            progressBarProfile.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            buttonEditSave.setEnabled(!isLoading);
            buttonLogout.setEnabled(!isLoading);
            buttonDeleteAccount.setEnabled(!isLoading);
        }
    }
}