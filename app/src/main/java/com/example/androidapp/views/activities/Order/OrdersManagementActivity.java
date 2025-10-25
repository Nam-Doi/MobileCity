package com.example.androidapp.views.activities.Order;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;
import com.example.androidapp.R;
import com.example.androidapp.views.adapters.OrderAdminPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class OrdersManagementActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private OrderAdminPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders_management);

        // 1. Ánh xạ các view
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tab_layout_admin);
        viewPager = findViewById(R.id.view_pager_admin);

        setupToolbar();

        // 2. Lấy mảng trạng thái từ arrays.xml
        String[] statusTitles = getResources().getStringArray(R.array.order_status_titles);
        String[] statusValues = getResources().getStringArray(R.array.order_status_values);

        // 3. Khởi tạo Adapter cho ViewPager
        pagerAdapter = new OrderAdminPagerAdapter(this, statusValues);
        viewPager.setAdapter(pagerAdapter);

        // 4. Kết nối TabLayout với ViewPager2
        new TabLayoutMediator(tabLayout, viewPager,
                (@NonNull TabLayout.Tab tab, int position) -> {
                    tab.setText(statusTitles[position]); // Gán tên cho từng Tab
                }
        ).attach();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }
}