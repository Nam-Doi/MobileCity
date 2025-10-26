package com.example.androidapp.views.activities.Auths;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.androidapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DisabledUserActivity extends AppCompatActivity {

    private Button buttonRequestReactivation;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disabled_user);

        // Khởi tạo Firebase Auth để lấy thông tin user
        mAuth = FirebaseAuth.getInstance();

        // Ánh xạ nút bấm từ layout
        buttonRequestReactivation = findViewById(R.id.buttonRequestReactivation);

        // Thiết lập sự kiện khi nhấn nút
        buttonRequestReactivation.setOnClickListener(v -> {
            sendReactivationEmail();
        });
    }

    private void sendReactivationEmail() {
        FirebaseUser user = mAuth.getCurrentUser();

        // Lấy thông tin người dùng hiện tại (dù bị khóa nhưng vẫn đăng nhập)
        if (user == null) {
            Toast.makeText(this, "Không thể lấy thông tin người dùng.", Toast.LENGTH_SHORT).show();
            return;
        }


        // QUAN TRỌNG: Thay đổi email này thành email của Admin
        String adminEmail = "phamthanhtung108049@gmail.com";

        String subject = "Yêu cầu kích hoạt lại tài khoản - " + user.getEmail();
        String body = "Kính gửi quản trị viên,\n\n"
                + "Tôi muốn yêu cầu kích hoạt lại tài khoản của mình.\n\n"
                + "Email: " + user.getEmail() + "\n"
                + "UID: " + user.getUid() + "\n\n"
                + "Lí do:\n\n"
                + "Cảm ơn.";

        // Tạo Intent để gửi email
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // Chỉ mở ứng dụng email
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{ adminEmail });
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, body);

        try {
            // Thử mở ứng dụng email
            startActivity(Intent.createChooser(intent, "Chọn ứng dụng Email..."));
        } catch (ActivityNotFoundException ex) {
            // Xử lý nếu người dùng không cài đặt ứng dụng email nào
            Toast.makeText(this, "Không có ứng dụng email nào được cài đặt.", Toast.LENGTH_SHORT).show();
        }
    }
}