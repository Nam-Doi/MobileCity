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
import com.example.androidapp.models.Order;
import com.example.androidapp.models.OrderItem;
import com.example.androidapp.repositories.CartRepository;
import com.example.androidapp.views.adapters.cartAdt.CheckoutAdapter;
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
    private TextView tvPaymentMethod;
    private ArrayList<CartItem> selectedItems;
    private CheckoutAdapter checkoutAdapter;
    private TextView tvReceiverName, tvReceiverPhone, tvReceiverAddress;
    private com.example.androidapp.models.PaymentMethod selectedPaymentMethod;

    // Repository
    private CartRepository cartRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        cartRepository = new CartRepository();

        initViews();
        receiveData();
        setupRecyclerView();
        calculateTotal();
        setupEventListeners();
        receiveAddress();
    }

    private void initViews() {
        imgBack = findViewById(R.id.btnBack);
        tvTotal = findViewById(R.id.tvTotal);
        btnCheckout = findViewById(R.id.btnCheckout);
        rvCheckoutItems = findViewById(R.id.rvCheckoutItems);
        layoutAddress = findViewById(R.id.layoutAddress);
        layoutPaymentMethod = findViewById(R.id.layoutPaymentMethod);
        tvTotalPayment = findViewById(R.id.tvTotalPayment);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
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
                selectedItems = intent.getParcelableArrayListExtra("selectedItems");
            }
        }

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

        tvTotal.setText(formatCurrency(subtotal));
        tvTotalPayment.setText(formatCurrency(subtotal));
    }

    private void setupEventListeners() {
        imgBack.setOnClickListener(v -> finish());
        btnCheckout.setOnClickListener(v -> processCheckout());

        layoutAddress.setOnClickListener(v -> {
            Intent intent = new Intent(this, SelectAddressActivity.class);
            intent.putParcelableArrayListExtra("selectedItems", selectedItems);
            startActivityForResult(intent, 1001);
        });

        layoutPaymentMethod.setOnClickListener(v -> {
            Intent intent = new Intent(this, PaymentMethodActivity.class);
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

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để đặt hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUid = auth.getCurrentUser().getUid();

        // Disable button để tránh click nhiều lần
        btnCheckout.setEnabled(false);

        // 2. Chuyển đổi CartItems -> OrderItems
        List<OrderItem> orderItems = new ArrayList<>();
        double total = 0;

        for (CartItem cartItem : selectedItems) {
            OrderItem item = new OrderItem();
            item.setProductId(cartItem.getProductId());
            item.setName(cartItem.getCachedName());
            item.setPrice(cartItem.getCachedPrice());
            item.setQty(cartItem.getQuantity());

            orderItems.add(item);
            total += cartItem.getTotalPrice();
        }

        // 3. Tạo ID cho đơn hàng mới
        String newOrderId = db.collection("orders").document().getId();

        // 4. Tạo đối tượng Order
        Order newOrder = new Order();
        newOrder.setOrderId(newOrderId);
        newOrder.setUserId(currentUid);
        newOrder.setCustomerName(receiverName);
        newOrder.setPhone(receiverPhone);
        newOrder.setAddress(receiverAddress);
        newOrder.setTotal(total);
        newOrder.setStatus("pending");
        newOrder.setCreatedAt(Timestamp.now());
        newOrder.setItems(orderItems);

        // 5. Lưu đơn hàng
        db.collection("orders").document(newOrderId).set(newOrder)
                .addOnSuccessListener(aVoid -> {
                    // ✅ SAU KHI ĐẶT HÀNG THÀNH CÔNG -> XÓA KHỎI GIỎ HÀNG
                    removeItemsFromCart(currentUid);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi đặt hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnCheckout.setEnabled(true);
                });
    }

    /**
     * Xóa các sản phẩm đã mua khỏi giỏ hàng
     */
    private void removeItemsFromCart(String userId) {
        // Tạo danh sách cartItemId cần xóa
        List<String> cartItemIds = new ArrayList<>();

        for (CartItem item : selectedItems) {
            String cartItemId = item.getVariantId() != null ? item.getProductId() + "_" + item.getVariantId()
                    : item.getProductId();
            cartItemIds.add(cartItemId);
        }

        // Sử dụng CartRepository để xóa
        cartRepository.removeMultipleItems(userId, cartItemIds,
                new CartRepository.OnCartOperationListener() {
                    @Override
                    public void onSuccess(String message) {
                        runOnUiThread(() -> {
                            Toast.makeText(CheckoutActivity.this,
                                    "Đặt hàng thành công!",
                                    Toast.LENGTH_SHORT).show();

                            setResult(RESULT_OK);
                            finish();
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        runOnUiThread(() -> {
                            Toast.makeText(CheckoutActivity.this,
                                    "Đặt hàng thành công nhưng không xóa được giỏ hàng",
                                    Toast.LENGTH_SHORT).show();

                            setResult(RESULT_OK);
                            finish();
                        });
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            // Address result
            String receiverName = data.getStringExtra("receiverName");
            String receiverPhone = data.getStringExtra("receiverPhone");
            String address = data.getStringExtra("address");

            boolean handled = false;
            if (receiverName != null && receiverPhone != null && address != null) {
                tvReceiverName.setText(receiverName);
                tvReceiverPhone.setText(receiverPhone);
                tvReceiverAddress.setText(address);
                handled = true;
            }

            // Payment method result (PaymentMethodActivity / adapter returns id + name)
            String paymentId = data.getStringExtra("paymentMethodId");
            String paymentName = data.getStringExtra("paymentMethodName");
            if (paymentId != null) {
                com.example.androidapp.models.PaymentMethod pm = new com.example.androidapp.models.PaymentMethod();
                pm.setId(paymentId);
                pm.setName(paymentName != null ? paymentName : "");
                // store selected payment method
                try {
                    // selectedPaymentMethod field may not exist previously; add if present
                    // using reflection would be heavy; but selectedPaymentMethod is not declared in
                    // this file
                    // We'll declare a local assignment to update UI and keep method selection
                    // across process
                    // If there's a field, set it; otherwise just update UI
                    tvPaymentMethod.setText(pm.getName());
                } catch (Exception ignored) {
                }
                handled = true;
            }

            // fallback: if nothing handled, try previous behavior for serialized object
            if (!handled && data.hasExtra("selected_payment_method")) {
                Object obj = data.getSerializableExtra("selected_payment_method");
                if (obj instanceof com.example.androidapp.models.PaymentMethod) {
                    com.example.androidapp.models.PaymentMethod pm = (com.example.androidapp.models.PaymentMethod) obj;
                    tvPaymentMethod.setText(pm.getName());
                }
            }
        }
    }

    private String formatCurrency(double amount) {
        return String.format("%,.0fđ", amount);
    }
}