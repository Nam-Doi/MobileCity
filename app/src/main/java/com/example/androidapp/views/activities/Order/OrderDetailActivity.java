package com.example.androidapp.views.activities.Order;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.androidapp.R;
import com.example.androidapp.models.Order;
import com.example.androidapp.views.adapters.OrderItemDetailAdapter;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class OrderDetailActivity extends AppCompatActivity {

    private Order order;

    // Views cơ bản
    private Toolbar toolbarDetail;
    private TextView tvOrderId, tvCustomerName, tvPhone, tvAddress, tvTotal, tvCreatedAt;
    private RecyclerView rvOrderItems;
    private OrderItemDetailAdapter itemAdapter;

    // Views của Tracker
    private TextView iconPending, textPending, iconShipped, textShipped, iconCompleted, textCompleted;
    private View line1, line2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail); // Gắn layout mới

        // 1. Nhận dữ liệu Order
        if (getIntent().hasExtra("order")) {
            order = getIntent().getParcelableExtra("order");
        } else {
            // Xử lý lỗi nếu không nhận được order
            finish();
            return;
        }

        // 2. Ánh xạ toàn bộ Views
        initViews();

        // 3. Cài đặt Toolbar
        setupToolbar();

        // 4. Hiển thị dữ liệu
        displayOrderInfo();

        // 5. Cập nhật Tracker
        updateStatusTracker(order.getStatus());

        // 6. Cài đặt RecyclerView
        setupItemsRecyclerView();
    }

    private void initViews() {
        toolbarDetail = findViewById(R.id.toolbarDetail);
        tvOrderId = findViewById(R.id.tvOrderId);
        tvCustomerName = findViewById(R.id.tvCustomerName);
        tvPhone = findViewById(R.id.tvPhone);
        tvAddress = findViewById(R.id.tvAddress);
        tvTotal = findViewById(R.id.tvTotal);
        tvCreatedAt = findViewById(R.id.tvCreatedAt);
        rvOrderItems = findViewById(R.id.rvOrderItems);

        // Ánh xạ Tracker
        iconPending = findViewById(R.id.tv_tracker_icon_pending);
        textPending = findViewById(R.id.tv_tracker_text_pending);
        line1 = findViewById(R.id.line1);
        iconShipped = findViewById(R.id.tv_tracker_icon_shipped);
        textShipped = findViewById(R.id.tv_tracker_text_shipped);
        line2 = findViewById(R.id.line2);
        iconCompleted = findViewById(R.id.tv_tracker_icon_completed);
        textCompleted = findViewById(R.id.tv_tracker_text_completed);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbarDetail);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbarDetail.setNavigationOnClickListener(v -> finish());
    }

    private void displayOrderInfo() {
        if (order == null) return;

        tvOrderId.setText("Mã đơn: #" + order.getOrderId());
        tvCustomerName.setText("Tên: " + order.getCustomerName());
        tvPhone.setText("SĐT: " + order.getPhone());
        tvAddress.setText("Địa chỉ: " + order.getAddress());
        tvTotal.setText("Tổng tiền: " + formatCurrency(order.getTotal()));

        if (order.getCreatedAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            tvCreatedAt.setText("Ngày đặt: " + sdf.format(order.getCreatedAt().toDate()));
        }
    }

    private void setupItemsRecyclerView() {
        rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
        // Tái sử dụng Adapter và Layout Item cũ của bạn
        itemAdapter = new OrderItemDetailAdapter(order.getItems());
        rvOrderItems.setAdapter(itemAdapter);
    }

    // --- Logic quan trọng nhất của Giai đoạn 3 ---
    private void updateStatusTracker(String status) {
        int activeColor = ContextCompat.getColor(this, R.color.primary_blue);
        int inactiveColor = Color.parseColor("#BDBDBD"); // Màu xám

        // Đặt tất cả về mặc định (xám)
        setTrackerStep(iconPending, textPending, line1, inactiveColor, Typeface.NORMAL);
        setTrackerStep(iconShipped, textShipped, line2, inactiveColor, Typeface.NORMAL);
        setTrackerStep(iconCompleted, textCompleted, null, inactiveColor, Typeface.NORMAL);

        // Cập nhật dựa trên trạng thái
        switch (status) {
            case "completed":
                setTrackerStep(iconPending, textPending, line1, activeColor, Typeface.BOLD);
                setTrackerStep(iconShipped, textShipped, line2, activeColor, Typeface.BOLD);
                setTrackerStep(iconCompleted, textCompleted, null, activeColor, Typeface.BOLD);
                break;
            case "shipped":
                setTrackerStep(iconPending, textPending, line1, activeColor, Typeface.BOLD);
                setTrackerStep(iconShipped, textShipped, line2, activeColor, Typeface.BOLD);
                break;
            case "pending":
                setTrackerStep(iconPending, textPending, line1, activeColor, Typeface.BOLD);
                break;
            case "cancelled":
                // Nếu bị hủy, bạn có thể đổi màu tất cả thành đỏ
                int cancelledColor = Color.parseColor("#EF5350"); // Đỏ
                setTrackerStep(iconPending, textPending, line1, cancelledColor, Typeface.NORMAL);
                setTrackerStep(iconShipped, textShipped, line2, cancelledColor, Typeface.NORMAL);
                setTrackerStep(iconCompleted, textCompleted, null, cancelledColor, Typeface.NORMAL);
                textShipped.setText("Đã hủy"); // Ví dụ
                break;
        }
    }

    private void setTrackerStep(TextView icon, TextView text, View line, int color, int typeface) {
        icon.setBackgroundColor(color); // Bạn nên dùng Drawable xịn hơn, tạm thời dùng màu
        text.setTextColor(color);
        text.setTypeface(null, typeface);
        if (line != null) {
            line.setBackgroundColor(color);
        }
    }

    private String formatCurrency(double amount) {
        return NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(amount);
    }
}