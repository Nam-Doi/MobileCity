package com.example.androidapp.views.activities.admin;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.androidapp.R;
import com.example.androidapp.models.users;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class CreateUserActivity extends AppCompatActivity {

    private static final String TAG = "CreateUserActivity";

    private EditText editTextFullName, editTextEmail, editTextPassword;
    private RadioGroup radioGroupRole;
    private Button buttonCreateAccount;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        editTextFullName = findViewById(R.id.editTextFullName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        radioGroupRole = findViewById(R.id.radioGroupRole);
        buttonCreateAccount = findViewById(R.id.buttonCreateAccount);

        buttonCreateAccount.setOnClickListener(v -> {
            createNewAccount();
        });
    }

    private void createNewAccount() {
        String fullName = editTextFullName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Kiểm tra thông tin đầu vào
        if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo user trong Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Tạo tài khoản Auth thành công
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        String uid = firebaseUser.getUid();

                        // Lấy vai trò được chọn
                        int selectedRoleId = radioGroupRole.getCheckedRadioButtonId();
                        String role = (selectedRoleId == R.id.radioButtonAdmin) ? "admin" : "user";

                        //Gửi email xác thực
                        sendVerificationEmail(firebaseUser);

                        // Tạo document trong Firestore
                        createUserDocumentInFirestore(uid, fullName, email, role);

                    } else {
                        // Nếu tạo tài khoản Auth thất bại, kiểm tra nguyên nhân cụ thể
                        String errorMessage = "Tạo tài khoản thất bại."; // Thông báo mặc định
                        try {
                            throw task.getException();
                        } catch (FirebaseAuthWeakPasswordException e) {
                            errorMessage = "Mật khẩu quá yếu, vui lòng nhập ít nhất 6 ký tự.";
                        } catch (FirebaseAuthInvalidCredentialsException e) {
                            errorMessage = "Địa chỉ email không hợp lệ.";
                        } catch (FirebaseAuthUserCollisionException e) {
                            errorMessage = "Email này đã tồn tại, vui lòng sử dụng email khác.";
                        } catch (Exception e) {
                            // Các lỗi khác (ví dụ: không có mạng)
                            if (e.getMessage() != null) {
                                errorMessage = e.getMessage();
                            }
                        }
                        Toast.makeText(CreateUserActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void createUserDocumentInFirestore(String uid, String fullName, String email, String role) {
        // Tạo một đối tượng user mới (không cần timestamp, Firestore sẽ tự thêm)
        users newUser = new users(fullName, email, role, uid, null);

        db.collection("users").document(uid)
                .set(newUser)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Tạo document người dùng thành công trên Firestore!");
                    Toast.makeText(CreateUserActivity.this, "Tạo tài khoản thành công!", Toast.LENGTH_SHORT).show();
                    finish(); // Đóng màn hình hiện tại và quay về danh sách
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Lỗi khi tạo document trên Firestore", e);
                });
    }

    private void sendVerificationEmail(FirebaseUser user) {
        // Gọi hàm gửi email xác minh của Firebase
        user.sendEmailVerification()
                // addOnCompleteListener: Lắng nghe kết quả
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Gửi thành công
                        Log.d(TAG, "Verification email sent to " + user.getEmail());
                    } else {
                        // Gửi thất bại
                        Log.e(TAG, "Failed to send verification email", task.getException());
                        Toast.makeText(CreateUserActivity.this,
                                "Không thể gửi email xác minh.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}