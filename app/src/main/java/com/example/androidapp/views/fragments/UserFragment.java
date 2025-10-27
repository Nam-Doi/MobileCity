package com.example.androidapp.views.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.androidapp.R;
import com.example.androidapp.views.activities.Auths.ProfileActivity;
import com.example.androidapp.views.activities.Order.MyOrdersActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserFragment extends Fragment {

    private TextView tvUserName; // TextView để hiển thị tên người dùng
    private ImageView avatar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView tv_myorders;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Gắn layout cho fragment
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        // Ánh xạ TextView từ layout
        tvUserName = view.findViewById(R.id.tvNameUser);
        avatar = view.findViewById(R.id.avatarMedium);
        tv_myorders = view.findViewById(R.id.tv_my_orders);

        // Khởi tạo Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        View.OnClickListener profileClickListener = v -> {
            Intent intent = new Intent(getActivity(), ProfileActivity.class);
            startActivity(intent);
        };

        // Gán sự kiện click cho cả avatar và tên
        avatar.setOnClickListener(profileClickListener);
        tvUserName.setOnClickListener(profileClickListener);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Gọi hàm để tải và hiển thị thông tin người dùng
        loadUserInfo();
        tv_myorders.setOnClickListener(v ->{
            Intent intent = new Intent(getActivity(), MyOrdersActivity.class);
            startActivity(intent);
        });
    }

    private void loadUserInfo() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();

            // Truy vấn vào Firestore để lấy thông tin user
            db.collection("users").document(uid).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                // Lấy trường fullName từ document
                                String fullName = document.getString("fullName");

                                // Hiển thị tên lên TextView
                                if (fullName != null && !fullName.isEmpty()) {
                                    tvUserName.setText(fullName);
                                } else {
                                    tvUserName.setText("Xin chào!");
                                }
                            } else {
                                Log.d("HomeFragment", "Không tìm thấy document");
                                tvUserName.setText("Xin chào!");
                            }
                        } else {
                            Log.e("HomeFragment", "Lỗi khi lấy dữ liệu: ", task.getException());
                            tvUserName.setText("Xin chào!");
                        }
                    });
        } else {
            // Trường hợp người dùng chưa đăng nhập
            tvUserName.setText("Khách");
        }
    }
}