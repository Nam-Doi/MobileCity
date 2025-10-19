package com.example.androidapp.views.activities.carts;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidapp.R;
import com.example.androidapp.models.CartItem;
import com.example.androidapp.views.adapters.CheckoutAdapter;

import java.util.ArrayList;

public class CheckoutActivity extends AppCompatActivity {
    private ImageView imgBack;
    private TextView tvTittle;
    private TextView tvTotal;
    private Button btnCheckout;
    private RecyclerView rvCheckoutItems;
    private LinearLayout layoutAddress;
    private LinearLayout layoutPaymentMethod;
    private TextView tvTotalPayment;
    private ArrayList<CartItem> selectedItems;
    private CheckoutAdapter checkoutAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        initViews();           // 1. Khởi tạo views
        receiveData();         // 2. Nhận dữ liệu
        setupRecyclerView();   // 3. Setup RecyclerView với dữ liệu
        calculateTotal();      // 4. Tính tổng tiền
        setupEventListeners(); // 5. Gắn sự kiện
    }

    private void initViews() {
        imgBack = findViewById(R.id.btnBack);
        tvTotal = findViewById(R.id.tvTotal);
        btnCheckout = findViewById(R.id.btnCheckout);
        rvCheckoutItems = findViewById(R.id.rvCheckoutItems);
        layoutAddress = findViewById(R.id.layoutAddress);
        layoutPaymentMethod = findViewById(R.id.layoutPaymentMethod);
        tvTotalPayment = findViewById(R.id.tvTotalPayment);
        tvTittle = findViewById(R.id.tvTitle);
    }

    private void receiveData() {
        Intent intent = getIntent();
        if (intent != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                selectedItems = intent.getParcelableArrayListExtra("selectedItems", CartItem.class);
            } else {
                //noinspection deprecation
                selectedItems = intent.getParcelableArrayListExtra("selectedItems");
            }
        }

        // Kiểm tra dữ liệu
        if (selectedItems == null || selectedItems.isEmpty()) {
            Toast.makeText(this, "Không có sản phẩm được chọn", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    private void setupRecyclerView() {
        // Khởi tạo adapter SAU KHI đã có dữ liệu
        checkoutAdapter = new CheckoutAdapter(selectedItems);
        rvCheckoutItems.setLayoutManager(new LinearLayoutManager(this));
        rvCheckoutItems.setAdapter(checkoutAdapter);
    }

    private void calculateTotal() {
        if (selectedItems == null || selectedItems.isEmpty()) {
            return;
        }

        double subtotal = 0;
        for (CartItem item : selectedItems) {
            subtotal += item.getTotalPrice();
        }

        // Format tiền tệ
        tvTotal.setText(formatCurrency(subtotal));
        tvTotalPayment.setText(formatCurrency(subtotal));
    }

    private void setupEventListeners() {
        imgBack.setOnClickListener(v -> finish());
        btnCheckout.setOnClickListener(v -> processCheckout());

        layoutAddress.setOnClickListener(v -> {
            Intent intent = new Intent(this, SelectAddressActivity.class);
            startActivity(intent);
        });

        layoutPaymentMethod.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng chọn phương thức thanh toán đang phát triển", Toast.LENGTH_SHORT).show();
            // TODO: Mở Dialog/Activity chọn phương thức thanh toán
        });
    }

    private void processCheckout() {
        if (selectedItems == null || selectedItems.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Xử lý thanh toán
        // 1. Validate địa chỉ và phương thức thanh toán
        // 2. Gọi API đặt hàng
        // 3. Lưu đơn hàng vào database
        // 4. Xóa items khỏi giỏ hàng
        // 5. Chuyển đến màn hình xác nhận

        Toast.makeText(this, "Đang xử lý thanh toán...", Toast.LENGTH_SHORT).show();

        // Intent intent = new Intent(this, OrderSuccessActivity.class);
        // startActivity(intent);
        // finish();
    }

    // Helper method để format tiền tệ
    private String formatCurrency(double amount) {
        return String.format("%,.0fđ", amount);
    }
}