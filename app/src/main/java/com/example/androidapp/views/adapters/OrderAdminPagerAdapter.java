package com.example.androidapp.views.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.example.androidapp.views.fragments.OrderListFragment;

public class OrderAdminPagerAdapter extends FragmentStateAdapter {

    // Lấy các giá trị từ mảng "order_status_values"
    // (pending, shipped, completed, cancelled)
    private final String[] statusValues;

    public OrderAdminPagerAdapter(@NonNull FragmentActivity fragmentActivity, String[] statusValues) {
        super(fragmentActivity);
        this.statusValues = statusValues;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Khi vuốt đến tab vị trí [position],
        // tạo một Fragment mới với trạng thái tương ứng
        String status = statusValues[position];
        return OrderListFragment.newInstance(status);
    }

    @Override
    public int getItemCount() {
        // Tổng số Tab
        return statusValues.length;
    }
}