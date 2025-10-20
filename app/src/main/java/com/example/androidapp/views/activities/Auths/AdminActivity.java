package com.example.androidapp.views.activities.Auths;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.androidapp.R;
import com.example.androidapp.views.activities.Order.OrdersManagementActivity;
import com.example.androidapp.views.activities.admin.AdminSettingsActivity;
import com.example.androidapp.views.activities.admin.ManageProductActivity;
import com.example.androidapp.views.activities.admin.ManageUsersActivity;
import com.example.androidapp.views.activities.admin.ViewProductsActivity;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

public class AdminActivity extends AppCompatActivity {

    private TextView tvAdminName;
    private ImageView imgAdminAvatar;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    // Các card trong dashboard
    private MaterialCardView cardManageProducts, cardViewProducts, cardManageOrders, cardManageUsers, cardSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_admin);

        // Ánh xạ view
        tvAdminName = findViewById(R.id.tvAdminName);
        imgAdminAvatar = findViewById(R.id.imgAdminAvatar);

        cardManageProducts = findViewById(R.id.cardManageProducts);
        cardViewProducts = findViewById(R.id.cardViewProducts);
        cardManageOrders = findViewById(R.id.cardManageOrders);
        cardManageUsers = findViewById(R.id.cardManageUsers);
        cardSettings = findViewById(R.id.cardSettings);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Load thông tin admin
        loadAdminInfo();

        // Áp dụng animation scale cho các card
        applyCardAnimation(cardManageProducts);
        applyCardAnimation(cardViewProducts);
        applyCardAnimation(cardManageOrders);
        applyCardAnimation(cardManageUsers);
        applyCardAnimation(cardSettings);
        //Mở manage product
        cardManageProducts.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, ManageProductActivity.class);
            startActivity(intent);
        });

        //Mơ view product̉
        cardViewProducts.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, ViewProductsActivity.class);
            startActivity(intent);
        });

        //Mở manage order
        cardManageOrders.setOnClickListener(v ->{
            Intent intent = new Intent(AdminActivity.this, OrdersManagementActivity.class);
            startActivity(intent);
        });
        //Mở manage account
        cardManageUsers.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, ManageUsersActivity.class);
            startActivity(intent);
        });

        //Mở settings
        cardSettings.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, AdminSettingsActivity.class);
            startActivity(intent);
        });

    }

    //  load thông tin admin
    private void loadAdminInfo() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = currentUser.getUid();

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("fullName");
                        if (name == null || name.isEmpty()) {
                            name = "Admin";
                        }

                        tvAdminName.setText("Xin chào, " + name);
                        tvAdminName.setAlpha(0f);
                        tvAdminName.animate().alpha(1f).setDuration(800).start();

                        String avatarUrl = documentSnapshot.getString("avatarUrl");
                        if (avatarUrl != null && !avatarUrl.isEmpty()) {
                            Picasso.get()
                                    .load(avatarUrl)
                                    .placeholder(R.drawable.admin_avt)
                                    .error(R.drawable.admin_avt)
                                    .into(imgAdminAvatar);
                        } else {
                            imgAdminAvatar.setImageResource(R.drawable.admin_avt);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Không thể tải thông tin admin", Toast.LENGTH_SHORT).show();
                });
    }

    //gán hiệu ứng scale
    private void applyCardAnimation(MaterialCardView cardView) {
        Animation scaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up);
        Animation scaleDown = AnimationUtils.loadAnimation(this, R.anim.scale_down);

        cardView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.startAnimation(scaleUp);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.startAnimation(scaleDown);
                    break;
            }
            return false;
        });
    }
}