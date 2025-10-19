package com.example.androidapp.views.activities.admin;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androidapp.R;

public class ManageUsersActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);
        setTitle("Quản lý người dùng");
    }
}
