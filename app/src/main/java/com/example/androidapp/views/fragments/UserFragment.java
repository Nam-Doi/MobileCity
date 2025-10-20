package com.example.androidapp.views.fragments;

import android.content.Intent; // <-- Thêm import này
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView; // <-- Thêm import này

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.androidapp.R;
import com.example.androidapp.views.activities.Order.MyOrdersActivity; // <-- Thêm import này

public class UserFragment extends Fragment {

    private TextView tvMyOrders; // Biến cho nút "Đơn hàng"

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // 1. Gắn layout
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        // 2. Ánh xạ nút "Đơn hàng" bằng ID
        tvMyOrders = view.findViewById(R.id.tv_my_orders);

        // 3. Gán sự kiện click
        tvMyOrders.setOnClickListener(v -> {
            // 4. Tạo Intent để mở MyOrdersActivity
            Intent intent = new Intent(getActivity(), MyOrdersActivity.class);
            startActivity(intent);
        });

        // 5. Trả về view
        return view;
    }
}