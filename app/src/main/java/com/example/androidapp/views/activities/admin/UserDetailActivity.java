package com.example.androidapp.views.activities.admin; // Thay đổi package cho đúng với dự án của bạn

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.androidapp.R;
import com.example.androidapp.models.users;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserDetailActivity extends AppCompatActivity {

    private static final String TAG = "UserDetailActivity";

    private EditText editUserFullName, editUserEmail, editUserRole;
    private Button buttonToggleActive, buttonSaveChanges, buttonEdit;
    private boolean isEditMode = false; // Biến để theo dõi trạng thái sửa

    private FirebaseFirestore db;
    private DocumentReference userDocRef;
    private users currentUser; // Biến để lưu thông tin user hiện tại

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);

        // 1. Lấy UID của user được gửi từ màn hình trước
        String userId = getIntent().getStringExtra("USER_ID");
        if (userId == null || userId.isEmpty()) {
            // Xử lý lỗi nếu không có userId
            finish();
            return;
        }

        // 2. Khởi tạo Firestore và tham chiếu đến document của user
        db = FirebaseFirestore.getInstance();
        userDocRef = db.collection("users").document(userId);

        // 3. Ánh xạ các view từ layout
        editUserFullName = findViewById(R.id.editUserFullName);
        editUserEmail = findViewById(R.id.editUserEmail);
        editUserRole = findViewById(R.id.editUserRole);
        buttonToggleActive = findViewById(R.id.buttonToggleActive);
        buttonSaveChanges = findViewById(R.id.buttonSaveChanges);
        buttonEdit = findViewById(R.id.buttonEdit);

        // 4. Tải dữ liệu của user và hiển thị
        loadUserData();

        // 5. Thiết lập Listener cho nút Vô hiệu hóa/Kích hoạt
        buttonToggleActive.setOnClickListener(v -> {
            if (currentUser != null) {
                toggleUserStatus();
            }
        });

        // Thiết lập Listener cho nút Sửa
        buttonEdit.setOnClickListener(v -> {
            toggleEditMode(true);
        });

// Thiết lập Listener cho nút Lưu
        buttonSaveChanges.setOnClickListener(v -> {
            saveChanges();
        });

    }
    private void toggleEditMode(boolean enable) {
        isEditMode = enable;

        // Cho phép/không cho phép sửa EditText
        editUserFullName.setEnabled(enable);
        editUserRole.setEnabled(enable);
        // Email thường không nên cho sửa
        // editUserEmail.setEnabled(enable);

        if (enable) {
            // Chuyển sang chế độ SỬA
            buttonEdit.setVisibility(View.GONE);
            buttonToggleActive.setVisibility(View.GONE);
            buttonSaveChanges.setVisibility(View.VISIBLE);
            // Đặt con trỏ vào ô fullName để tiện sửa
            editUserFullName.requestFocus();
        } else {
            // Chuyển về chế độ XEM
            buttonEdit.setVisibility(View.VISIBLE);
            buttonToggleActive.setVisibility(View.VISIBLE);
            buttonSaveChanges.setVisibility(View.GONE);
        }
    }

    private void saveChanges() {
        String newFullName = editUserFullName.getText().toString().trim();
        String newRole = editUserRole.getText().toString().trim();

        if (newFullName.isEmpty() || newRole.isEmpty()) {
            Toast.makeText(this, "Tên và vai trò không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }

        // Cập nhật dữ liệu lên Firestore
        userDocRef.update("fullName", newFullName, "role", newRole)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    // Cập nhật lại object currentUser
                    currentUser.setFullName(newFullName);
                    currentUser.setRole(newRole);
                    // Chuyển về chế độ xem
                    toggleEditMode(false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Cập nhật thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    private void loadUserData() {
        userDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                currentUser = documentSnapshot.toObject(users.class);
                if (currentUser != null) {
                    // Hiển thị thông tin lên UI (giữ nguyên)
                    editUserFullName.setText(currentUser.getFullName());
                    editUserEmail.setText(currentUser.getEmail());
                    editUserRole.setText(currentUser.getRole());

                    // ===============================================
                    // THÊM ĐOẠN CODE KIỂM TRA ROLE VÀO ĐÂY
                    // ===============================================
                    if ("admin".equals(currentUser.getRole())) {
                        // Nếu là admin, ẩn nút Vô hiệu hóa/Kích hoạt
                        buttonToggleActive.setVisibility(View.GONE);
                    } else {
                        // Nếu không phải admin, hiển thị nút và cập nhật trạng thái
                        buttonToggleActive.setVisibility(View.VISIBLE);
                        updateButtonStatus(); // Chỉ gọi hàm này cho user thường
                    }
                    // ===============================================

                }
            } else {
                Log.d(TAG, "Không tìm thấy user");
                // Có thể thêm Toast báo lỗi ở đây
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Lỗi khi tải dữ liệu user", e);
            // Có thể thêm Toast báo lỗi ở đây
        });
    }

    private void updateButtonStatus() {
        if (currentUser.isActive()) {
            buttonToggleActive.setText("Vô hiệu hóa tài khoản");
            buttonToggleActive.setBackgroundColor(Color.parseColor("FF0000"));
        } else {
            buttonToggleActive.setText("Kích hoạt tài khoản");
            buttonToggleActive.setBackgroundColor(Color.parseColor("#00FF00"));
        }
    }

    private void toggleUserStatus() {
        boolean newStatus = !currentUser.isActive();
        userDocRef.update("active", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Cập nhật trạng thái thành công");
                    // Cập nhật lại trạng thái trong object currentUser và giao diện
                    currentUser.setActive(newStatus);
                    updateButtonStatus();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Lỗi khi cập nhật trạng thái", e);
                });
    }
}