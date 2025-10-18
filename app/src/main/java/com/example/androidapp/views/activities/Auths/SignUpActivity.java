package com.example.androidapp.views.activities.Auths;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.androidapp.R;
import com.example.androidapp.models.users;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignUpActivity extends AppCompatActivity {

    // TAG dùng để debug và log, giúp dễ dàng tìm log trong Logcat
    private static final String TAG = "SignUpActivity";
    private EditText etFullName;
    private EditText etEmail;
    private EditText etPassword;
    private MaterialButton btnSignUp;
    private boolean isPasswordVisible = false;
    private ImageView imgPasswordToggle;
    private TextView tvLogin;

    // Các biến Firebase
    private FirebaseAuth mAuth;  // xác thực người dùng
    private FirebaseFirestore db;// database

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Khởi tạo Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        // Khởi tạo Firestore - dùng để lưu thông tin chi tiết của user
        db = FirebaseFirestore.getInstance();

        // Kết nối các View từ XML với biến Java
        initViews();

        // Thiết lập các sự kiện click cho button
        setupListeners();
        setupPasswordToggle();
        setupLogin(); // quay lai trang login
    }

    //find view tái sử dungj oke
    private void initViews() {
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        imgPasswordToggle = findViewById(R.id.ivPasswordToggle);
        tvLogin = findViewById(R.id.tvLogin);

    }
    //bat su kien click vao login
    private void setupLogin() {
        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
        });
    }

    //hàm click oke
    private void setupListeners() {
        btnSignUp.setOnClickListener(v -> signUp());
    }

    //hàm đăng ký oke
    private void signUp() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        //kiem tra tinh hop le của dữ liệu
        if (!validateInput(fullName, email, password)) {
            return;
        }

        // hợp lệ thì lưu vào firestore
        createAccountAndSaveToFirestore(fullName, email, password);
    }

    //hàm kiểm tra dữ liệu hợp lệ
    private boolean validateInput(String fullName, String email, String password) {
        if (fullName.isEmpty()) {
            etFullName.setError("Vui lòng nhập họ tên");
            etFullName.requestFocus();
            return false;
        }

        if (email.isEmpty()) {
            etEmail.setError("Vui lòng nhập email");
            etEmail.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email không hợp lệ");
            etEmail.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            etPassword.setError("Vui lòng nhập mật khẩu");
            etPassword.requestFocus();
            return false;
        }
        // tối thiểu 6 ký tự quy ước chung oke
        if (password.length() < 6) {
            etPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            etPassword.requestFocus();
            return false;
        }

        return true;
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

    private void createAccountAndSaveToFirestore(String fullName, String email,
                                                 String password) {
        // Gọi Firebase Auth để tạo tài khoản mới
        mAuth.createUserWithEmailAndPassword(email, password)
                // addOnCompleteListener: Lắng nghe kết quả khi hoàn thành
                .addOnCompleteListener(this, task -> {
                    // Kiểm tra xem việc tạo tài khoản có thành công không
                    if (task.isSuccessful()) {
                        // Thành công - Log ra để debug
                        Log.d(TAG, "createUserWithEmail:success");

                        // Lấy thông tin user vừa tạo từ Firebase Auth
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();

                        // Kiểm tra user có tồn tại không
                        if (firebaseUser != null) {
                            // Lưu thông tin chi tiết vào Firestore
                            // firebaseUser.getUid(): ID duy nhất của user do Firebase tạo oke
                            saveUserToFirestore(firebaseUser.getUid(), fullName, email);

                            // Gửi email xác minh đến địa chỉ email user đã đăng ký
                            sendVerificationEmail(firebaseUser);
                        } else {
                            Log.e(TAG, "User is null after successful registration.");
                        }
                    } else {
                        Exception e = task.getException();
                        Log.w(TAG, "createUserWithEmail:failure", e);

                        // Kiểm tra loại lỗi: Email đã tồn tại hay lỗi khác
                        if (e instanceof FirebaseAuthUserCollisionException) {
                            // Email đã được đăng ký trước đó
                            Toast.makeText(SignUpActivity.this,
                                    "Email đã được đăng ký!",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            // Lỗi khác (mạng, server, ...)
                            Toast.makeText(SignUpActivity.this,
                                    "Đăng ký thất bại: " + (e != null ? e.getMessage() : "Unknown error"),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    //lưu thông tin user
    private void saveUserToFirestore(String uid, String fullName, String email) {
        // Tạo object User với các thông tin cần lưu
        users user = new users(
                fullName,
                email,
                "user",
                uid,
                Timestamp.now()

        );

        // Lưu vào Firestore
        // db.collection("users"): Truy cập collection tên "users"
        // .document(uid): Tạo/truy cập document có ID = uid
        // .set(user): Lưu object user vào document này
        db.collection("users")
                .document(uid)
                .set(user)
                // addOnSuccessListener: Được gọi khi lưu thành công
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User profile created successfully in Firestore");
                    // Thông báo cho user biết đăng ký thành công
                    Toast.makeText(SignUpActivity.this,
                            "Đăng ký thành công! Vui lòng kiểm tra email để xác minh.",
                            Toast.LENGTH_LONG).show();

                    // Có thể chuyển sang màn hình khác hoặc đóng Activity
                    // finish();
                    // startActivity(new Intent(this, LoginActivity.class));
                })
                // addOnFailureListener: Được gọi khi lưu thất bại
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating user profile in Firestore", e);
                    // Thông báo lỗi cho user
                    Toast.makeText(SignUpActivity.this,
                            "Lỗi khi lưu thông tin: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    //gui email xac thuc
    private void sendVerificationEmail(FirebaseUser user) {
        // Gọi hàm gửi email xác minh của Firebase
        user.sendEmailVerification()
                // addOnCompleteListener: Lắng nghe kết quả
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Gửi thành công
                        Log.d(TAG, "Verification email sent to " + user.getEmail());
                    } else {
                        // Gửi thất bại (có thể do lỗi mạng, server Firebase, ...)
                        Log.e(TAG, "Failed to send verification email", task.getException());
                        Toast.makeText(SignUpActivity.this,
                                "Không thể gửi email xác minh. Bạn có thể gửi lại sau.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
//    private void getUserInfo(String uid) {
//        // Truy cập collection "users", document có ID = uid
//        db.collection("users")
//                .document(uid)
//                .get()  // Lấy dữ liệu
//                // addOnSuccessListener: Khi lấy dữ liệu thành công
//                .addOnSuccessListener(documentSnapshot -> {
//                    // Kiểm tra document có tồn tại không
//                    if (documentSnapshot.exists()) {
//                        // Chuyển đổi document thành object User
//                        users user = documentSnapshot.toObject(users.class);
//
//                        // Kiểm tra user không null
//                        if (user != null) {
//                            // Sử dụng thông tin user
//                            Log.d(TAG, "User name: " + user.getFullName());
//                            Log.d(TAG, "Email: " + user.getEmail());
//                            Log.d(TAG, "Role: " + user.getRole());
//
//                            // Có thể hiển thị lên UI
//                            // tvUserName.setText(user.getFullName());
//                            // tvEmail.setText(user.getEmail());
//                        }
//                    } else {
//                        // Document không tồn tại
//                        Log.d(TAG, "No such document");
//                    }
//                })
//                // addOnFailureListener: Khi có lỗi
//                .addOnFailureListener(e -> {
//                    Log.e(TAG, "Error getting user info", e);
//                    Toast.makeText(SignUpActivity.this,
//                            "Không thể tải thông tin người dùng",
//                            Toast.LENGTH_SHORT).show();
//                });
//    }
//    //update users
//    private void updateUserAddress(String uid, String newAddress) {
//        // Cập nhật chỉ 1 field cụ thể
//        db.collection("users")
//                .document(uid)
//                .update("address", newAddress)  // Chỉ cập nhật field "address"
//                .addOnSuccessListener(aVoid -> {
//                    Log.d(TAG, "Address updated successfully");
//                    Toast.makeText(SignUpActivity.this,
//                            "Cập nhật địa chỉ thành công",
//                            Toast.LENGTH_SHORT).show();
//                })
//                .addOnFailureListener(e -> {
//                    Log.e(TAG, "Error updating address", e);
//                });
//    }
//}
}