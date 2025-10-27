package com.example.androidapp.views.activities.admin;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.androidapp.R;
import com.example.androidapp.views.activities.Auths.LoginActivity;

public class AdminSettingsActivity extends AppCompatActivity {

    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_settings);

        btnLogout = findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(v -> showLogoutConfirmDialog());
    }

    private void showLogoutConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận đăng xuất")
                .setMessage("Bạn có chắc muốn đăng xuất không?")
                .setIcon(R.drawable.ic_logout) // nếu có icon logout thì thêm cho đẹp, không thì bỏ dòng này
                .setPositiveButton("Có", (dialog, which) -> logout())
                .setNegativeButton("Không", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void logout() {
        // Xóa dữ liệu đăng nhập
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        // Quay lại màn hình đăng nhập
        Intent intent = new Intent(AdminSettingsActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
