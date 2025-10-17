package com.example.androidapp.views.activities;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.androidapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;


public class AdminActivity extends AppCompatActivity {

    private TextView tvAdminName;
    private ImageView imgAdminAvatar;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_admin);

        tvAdminName = findViewById(R.id.tvAdminName);
        imgAdminAvatar = findViewById(R.id.imgAdminAvatar);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loadAdminInfo();
    }

    private void loadAdminInfo() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();

            db.collection("users").document(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Lấy từ field fullName
                            String name = documentSnapshot.getString("fullName");

                            tvAdminName.setText("Xin chào, " + (name != null ? name : "Admin"));

                            // Nếu muốn load avatar từ field avatarUrl, vẫn dùng Picasso/Glide
                            String avatarUrl = documentSnapshot.getString("avatarUrl");
                            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                                Picasso.get()
                                        .load(avatarUrl)
                                        .placeholder(R.drawable.admin_avt)
                                        .into(imgAdminAvatar);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Không thể tải thông tin admin", Toast.LENGTH_SHORT).show();
                    });
        }
    }
}

