package com.example.androidapp.views.activities.Auths;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.androidapp.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class ChangePasswordActivity extends AppCompatActivity {

    private static final String TAG = "ChangePasswordActivity";

    private TextInputEditText editTextCurrentPassword, editTextNewPassword, editTextConfirmPassword;
    private Button buttonConfirmChangePassword;
    private ProgressBar progressBarChangePass;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        editTextCurrentPassword = findViewById(R.id.editTextCurrentPassword);
        editTextNewPassword = findViewById(R.id.editTextNewPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonConfirmChangePassword = findViewById(R.id.buttonConfirmChangePassword);
        progressBarChangePass = findViewById(R.id.progressBarChangePass);

        // Kiểm tra xem user có đang đăng nhập không
        if (currentUser == null || currentUser.getEmail() == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy người dùng hoặc email.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        buttonConfirmChangePassword.setOnClickListener(v -> handleChangePassword());
    }

    private void handleChangePassword() {
        String currentPassword = editTextCurrentPassword.getText().toString();
        String newPassword = editTextNewPassword.getText().toString();
        String confirmPassword = editTextConfirmPassword.getText().toString();

        if (TextUtils.isEmpty(currentPassword)) {
            editTextCurrentPassword.setError("Vui lòng nhập mật khẩu hiện tại"); return;
        }
        if (TextUtils.isEmpty(newPassword)) {
            editTextNewPassword.setError("Vui lòng nhập mật khẩu mới"); return;
        }
        if (newPassword.length() < 6) {
            editTextNewPassword.setError("Mật khẩu mới phải có ít nhất 6 ký tự"); return;
        }
        if (TextUtils.isEmpty(confirmPassword)) {
            editTextConfirmPassword.setError("Vui lòng xác nhận mật khẩu mới"); return;
        }
        if (!newPassword.equals(confirmPassword)) {
            editTextConfirmPassword.setError("Mật khẩu xác nhận không khớp"); return;
        }

        setLoading(true);

        // Hành động đổi mật khẩu yêu cầu người dùng phải xác thực lại gần đây
        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), currentPassword);

        currentUser.reauthenticate(credential)
                .addOnCompleteListener(reauthTask -> {
                    if (reauthTask.isSuccessful()) {
                        // Xác thực lại thành công, TIẾN HÀNH ĐỔI MẬT KHẨU
                        Log.d(TAG, "User re-authenticated successfully.");
                        updatePassword(newPassword);
                    } else {
                        // Xác thực lại thất bại (sai mật khẩu hiện tại)
                        setLoading(false);
                        Log.w(TAG, "User re-authentication failed.", reauthTask.getException());
                        Toast.makeText(ChangePasswordActivity.this, "Mật khẩu hiện tại không đúng.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Cập nhật mật khẩu
    private void updatePassword(String newPassword) {
        currentUser.updatePassword(newPassword)
                .addOnCompleteListener(updateTask -> {
                    setLoading(false); // Kết thúc loading dù thành công hay thất bại
                    if (updateTask.isSuccessful()) {
                        Log.d(TAG, "User password updated successfully.");
                        Toast.makeText(ChangePasswordActivity.this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                        finish(); // Đóng màn hình đổi mật khẩu
                    } else {
                        Log.w(TAG, "User password update failed.", updateTask.getException());
                        // Xử lý các lỗi phổ biến
                        try {
                            throw updateTask.getException();
                        } catch(FirebaseAuthWeakPasswordException e) {
                            editTextNewPassword.setError("Mật khẩu mới quá yếu.");
                            editTextNewPassword.requestFocus();
                        } catch(Exception e) {
                            Toast.makeText(ChangePasswordActivity.this, "Đổi mật khẩu thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void setLoading(boolean isLoading) {
        progressBarChangePass.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        buttonConfirmChangePassword.setEnabled(!isLoading);
        editTextCurrentPassword.setEnabled(!isLoading);
        editTextNewPassword.setEnabled(!isLoading);
        editTextConfirmPassword.setEnabled(!isLoading);
    }
}