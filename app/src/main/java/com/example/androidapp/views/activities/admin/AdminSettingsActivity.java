package com.example.androidapp.views.activities.admin;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androidapp.R;

public class AdminSettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_settings);
        setTitle("Cài đặt");
    }
}
