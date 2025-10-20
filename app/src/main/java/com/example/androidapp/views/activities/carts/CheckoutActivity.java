package com.example.androidapp.views.activities.carts;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidapp.R;
import com.example.androidapp.models.CartItem;
import com.example.androidapp.views.adapters.cartAdt.CheckoutAdapter;

import com.example.androidapp.models.Order;
import com.example.androidapp.models.OrderItem;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;


public class CheckoutActivity extends AppCompatActivity {
    private ImageView imgBack;
    private TextView tvTotal;
    private Button btnCheckout;
    private RecyclerView rvCheckoutItems;
    private LinearLayout layoutAddress;
    private LinearLayout layoutPaymentMethod;
    private TextView tvTotalPayment;
    private ArrayList<CartItem> selectedItems;
    private CheckoutAdapter checkoutAdapter;
    private TextView tvReceiverName, tvReceiverPhone, tvReceiverAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        initViews();           // 1. Khởi tạo views
        receiveData();         // 2. Nhận dữ liệu
        setupRecyclerView();   // 3. Setup RecyclerView với dữ liệu
        calculateTotal();      // 4. Tính tổng tiền
        setupEventListeners(); // 5. Gắn sự kiện
        receiveAddress();      // 6. Nhận địa chỉ nếu có
    }

    private void initViews() {
        imgBack = findViewById(R.id.btnBack);
        tvTotal = findViewById(R.id.tvTotal);
        btnCheckout = findViewById(R.id.btnCheckout);
        rvCheckoutItems = findViewById(R.id.rvCheckoutItems);
        layoutAddress = findViewById(R.id.layoutAddress);
        layoutPaymentMethod = findViewById(R.id.layoutPaymentMethod);
        tvTotalPayment = findViewById(R.id.tvTotalPayment);
        tvReceiverName = findViewById(R.id.tvReceiverName);
        tvReceiverPhone = findViewById(R.id.tvReceiverPhone);
        tvReceiverAddress = findViewById(R.id.tvAddress);
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

    private void receiveAddress() {
        String receiverName = getIntent().getStringExtra("receiverName");
        String receiverPhone = getIntent().getStringExtra("receiverPhone");
        String address = getIntent().getStringExtra("address");

        if (receiverName != null && receiverPhone != null && address != null) {
            tvReceiverName.setText(receiverName);
            tvReceiverPhone.setText(receiverPhone);
            tvReceiverAddress.setText(address);
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
            // Truyền selectedItems để giữ dữ liệu khi quay lại
            intent.putParcelableArrayListExtra("selectedItems", selectedItems);
            startActivityForResult(intent, 1001);
        });

        layoutPaymentMethod.setOnClickListener(v -> {
            Intent intent = new Intent (this, PaymentMethodActivity.class);
            intent.putParcelableArrayListExtra("selectedItems", selectedItems);
            startActivityForResult(intent, 1001);
        });
    }

    private void processCheckout() {
        if (selectedItems == null || selectedItems.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
            return;
        }

        String receiverName = tvReceiverName.getText().toString().trim();
        String receiverPhone = tvReceiverPhone.getText().toString().trim();
        String receiverAddress = tvReceiverAddress.getText().toString().trim();

        // 1. Kiểm tra thông tin giao hàng
        if (receiverName.isEmpty() || receiverPhone.isEmpty() || receiverAddress.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn thông tin giao hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String currentUid = "guest"; // Giá trị mặc định nếu user chưa đăng nhập
        if (auth.getCurrentUser() != null) {
            currentUid = auth.getCurrentUser().getUid();
        }

        // 2. Chuyển đổi CartItems -> OrderItems
        List<OrderItem> orderItems = new ArrayList<>();
        double total = 0;

        for (CartItem cartItem : selectedItems) {
            OrderItem item = new OrderItem();
            item.setProductId(cartItem.getId());
            item.setName(cartItem.getName());
            item.setPrice(cartItem.getPrice());
            item.setQty(cartItem.getQuantity());

            orderItems.add(item);
            total += cartItem.getTotalPrice(); // Dùng hàm tính tổng có sẵn
        }

        // 3. Tạo ID cho đơn hàng MỚI (Cách làm chuẩn)
        String newOrderId = db.collection("orders").document().getId();

        // 4. Tạo đối tượng Order (Dùng model của Giai đoạn 1)
        Order newOrder = new Order();
        newOrder.setOrderId(newOrderId); // Gán ID mới
        newOrder.setUserId(currentUid);
        newOrder.setCustomerName(receiverName);
        newOrder.setPhone(receiverPhone);
        newOrder.setAddress(receiverAddress);
        newOrder.setTotal(total);
        newOrder.setStatus("pending"); // Trạng thái mặc định
        newOrder.setCreatedAt(Timestamp.now());
        newOrder.setItems(orderItems);

        // 5. Lưu đối tượng Order lên Firestore
        db.collection("orders").document(newOrderId).set(newOrder)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();

                    // TODO: Báo cho team Giỏ hàng rằng họ cần code logic
                    // để xóa các sản phẩm đã mua khỏi giỏ hàng ở đây.

                    // Đóng màn hình Checkout và quay về.
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi đặt hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            // Nhận dữ liệu địa chỉ từ SelectAddressActivity
            String receiverName = data.getStringExtra("receiverName");
            String receiverPhone = data.getStringExtra("receiverPhone");
            String address = data.getStringExtra("address");

            if (receiverName != null && receiverPhone != null && address != null) {
                tvReceiverName.setText(receiverName);
                tvReceiverPhone.setText(receiverPhone);
                tvReceiverAddress.setText(address);
            }
        }
    }

    // Helper method để format tiền tệ
    private String formatCurrency(double amount) {
        return String.format("%,.0fđ", amount);
    }
}