package com.example.androidapp.views.activities.carts;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.androidapp.R;
import com.example.androidapp.models.CartItem;
import com.example.androidapp.models.Notification;
import com.example.androidapp.models.Order;
import com.example.androidapp.models.OrderItem;
import com.example.androidapp.repositories.CartRepository;
import com.example.androidapp.repositories.NotificationRepository;
import com.example.androidapp.views.adapters.cartAdt.CheckoutAdapter;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckoutActivity extends AppCompatActivity {
    private ImageView imgBack;
    private TextView tvTotal;
    private Button btnCheckout;
    private RecyclerView rvCheckoutItems;
    private LinearLayout layoutAddress;
    private LinearLayout layoutPaymentMethod;
    private TextView tvTotalPayment;
    private TextView tvPaymentMethod;
    private ImageView ivPaymentIcon;
    private ArrayList<CartItem> selectedItems;
    private CheckoutAdapter checkoutAdapter;
    private TextView tvReceiverName, tvReceiverPhone, tvReceiverAddress;
    private com.example.androidapp.models.PaymentMethod selectedPaymentMethod;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private NotificationRepository notificationRepository;

    // Repository
    private CartRepository cartRepository;
    private static final String PREFS_NAME = "CheckoutPrefs";
    private static final String KEY_RECEIVER_NAME = "receiverName";
    private static final String KEY_RECEIVER_PHONE = "receiverPhone";
    private static final String KEY_RECEIVER_ADDRESS = "receiverAddress";
    private static final String KEY_PAYMENT_NAME = "name";
    private static final String KEY_PAYMENT_ICON = "iconUrl";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        cartRepository = new CartRepository();
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        notificationRepository = new NotificationRepository();


        initViews();
        receiveData();
        setupRecyclerView();
        calculateTotal();
        setupEventListeners();
        setDefaultPlaceholderText();
        if (!receiveAddressFromIntent()) {
            loadSavedAddress();
        }
        if (!receivePaymentMethodFromIntent()) {
            loadSavedPaymentMethod();
        }


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
        ivPaymentIcon = findViewById(R.id.ivPaymentIcon);
        tvReceiverName = findViewById(R.id.tvReceiverName);
        tvReceiverPhone = findViewById(R.id.tvReceiverPhone);
        tvReceiverAddress = findViewById(R.id.tvAddress);
    }
    private void setDefaultPlaceholderText() {
        tvReceiverName.setText("Tên người nhận");
        tvReceiverPhone.setText("Số điện thoại");
        tvReceiverAddress.setText("Vui lòng chọn địa chỉ giao hàng");
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
        //it xay ra vì đã có logic kiển tra không select bên giỏ rồi
    }
    // Receiver address from sharedreferences
    private boolean receiveAddressFromIntent(){
        String receiverName = getIntent().getStringExtra("receiverName");
        String receiverPhone = getIntent().getStringExtra("receiverPhone");
        String address = getIntent().getStringExtra("address");
        if(receiverName != null && receiverPhone != null && address != null){
            tvReceiverName.setText(receiverName);
            tvReceiverPhone.setText(receiverPhone);
            tvReceiverAddress.setText(address);
            saveAddressToPrefs(receiverName, receiverPhone, address);
            return true;
        }
        return false;

    }
    private void loadSavedAddress(){
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedName = prefs.getString(KEY_RECEIVER_NAME, "");
        String savedPhone = prefs.getString(KEY_RECEIVER_PHONE, "");
        String savedAddress = prefs.getString(KEY_RECEIVER_ADDRESS, "");
        if(savedName != null && !savedName.isEmpty() && savedPhone != null &&
                !savedPhone.isEmpty() && savedAddress != null && !savedAddress.isEmpty()){
            tvReceiverName.setText(savedName);
            tvReceiverPhone.setText(savedPhone);
            tvReceiverAddress.setText(savedAddress);
        }else{
            loadDefaultAddressFromFirestore();
        }

    }
    private void loadDefaultAddressFromFirestore(){
        if(auth.getCurrentUser() == null){
            Log.d("CheckoutDebug", "User not logged in. Cannot load default address.");
            return;
        }
        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId).collection("addresses")
                .orderBy("isDefault", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots ->{
                    if(!queryDocumentSnapshots.isEmpty()){
                        QueryDocumentSnapshot document = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                        String name = document.getString("receiverName");
                        String phone = document.getString("receiverPhone");
                        String address = document.getString("address");
                        if(name != null && phone != null && address != null){
                            tvReceiverName.setText(name);
                            tvReceiverPhone.setText(phone);
                            tvReceiverAddress.setText(address);
                            saveAddressToPrefs(name, phone, address);
                        }
                    }else{
                        Log.d("CheckoutDebug", "No default address found.");
                        tvReceiverAddress.setText("Vui lòng chọn địa chỉ giao hàng");
                        tvReceiverName.setText("Tên người nhận");
                        tvReceiverPhone.setText("Số điện thoại");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("CheckoutDebug", "Error loading default address", e);
                    Toast.makeText(this, "Lỗi khi tải địa chỉ mặc định", Toast.LENGTH_SHORT).show();
                });
    }
    //luu dia chi vao sharedPreferences
    private void saveAddressToPrefs(String name, String phone, String address){
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_RECEIVER_NAME, name);
        editor.putString(KEY_RECEIVER_PHONE, phone);
        editor.putString(KEY_RECEIVER_ADDRESS, address);
        editor.apply();


    }
    //luu phuong thuc thanh toan
    private boolean receivePaymentMethodFromIntent() {
        String paymentName = getIntent().getStringExtra("paymentMethodName");
        String paymentIconUrl = getIntent().getStringExtra("paymentMethodIcon"); // URL từ intent trước

        if (paymentName != null) {
            tvPaymentMethod.setText(paymentName);
            if (paymentIconUrl != null && !paymentIconUrl.isEmpty()) {
                Glide.with(this)
                        .load(paymentIconUrl)
                        .placeholder(R.drawable.ic_cod)
                        .error(R.drawable.ic_cod)
                        .into(ivPaymentIcon);
            } else {
                ivPaymentIcon.setImageResource(R.drawable.ic_cod);
            }
            savePaymentMethodToPrefs(paymentName, paymentIconUrl != null ? paymentIconUrl : "");
            return true;
        }
        return false;
    }
    private void savePaymentMethodToPrefs(String name, String icon) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_PAYMENT_NAME, name);
        editor.putString(KEY_PAYMENT_ICON, icon);
        editor.apply();
    }
    private void loadSavedPaymentMethod() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedName = prefs.getString(KEY_PAYMENT_NAME, null);
        String savedIconUrl = prefs.getString(KEY_PAYMENT_ICON, null);

        if (savedName != null) {
            tvPaymentMethod.setText(savedName);
            if (savedIconUrl != null && !savedIconUrl.isEmpty()) {
                Glide.with(this)
                        .load(savedIconUrl)
                        .placeholder(R.drawable.ic_cod)
                        .error(R.drawable.ic_cod)
                        .into(ivPaymentIcon);
            } else {

                ivPaymentIcon.setImageResource(R.drawable.ic_cod);
            }
        } else {
            tvPaymentMethod.setText("Chọn phương thức thanh toán");
            ivPaymentIcon.setImageResource(R.drawable.ic_cod);
        }
    }
    //------------------------------------// ok

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

    //Thực hiện thanh toán và tạo hóa đơn
    private void processCheckout() {
        // Kiểm tra xem selectedItems có null hoặc rỗng không
        if (selectedItems == null || selectedItems.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy thông tin người nhận từ TextViews
        String receiverName = tvReceiverName.getText().toString().trim();
        String receiverPhone = tvReceiverPhone.getText().toString().trim();
        String receiverAddress = tvReceiverAddress.getText().toString().trim();

        // 1. Kiểm tra thông tin giao hàng đã được chọn/nhập chưa
        // So sánh với text mặc định hoặc kiểm tra rỗng
        if (receiverName.equals("Tên người nhận") || receiverName.isEmpty() ||
                receiverPhone.equals("Số điện thoại") || receiverPhone.isEmpty() ||
                receiverAddress.equals("Vui lòng chọn địa chỉ giao hàng") || receiverAddress.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn đầy đủ thông tin giao hàng", Toast.LENGTH_SHORT).show();
            return;
        }


        // Kiểm tra người dùng đã đăng nhập chưa (sử dụng biến auth đã khởi tạo)
        if (auth == null || auth.getCurrentUser() == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để đặt hàng", Toast.LENGTH_SHORT).show();
            // Cân nhắc chuyển người dùng đến màn hình đăng nhập
            return;
        }
        // Lấy User ID
        String currentUid = auth.getCurrentUser().getUid();

        // Vô hiệu hóa nút thanh toán để tránh nhấn nhiều lần
        btnCheckout.setEnabled(false);

        // 2. Chuyển đổi CartItems -> OrderItems (trong bộ nhớ)
        List<OrderItem> orderItems = new ArrayList<>();
        double total = 0;
        for (CartItem cartItem : selectedItems) {
            OrderItem item = new OrderItem();
            item.setProductId(cartItem.getProductId());
            item.setName(cartItem.getCachedName());
            item.setPrice(cartItem.getCachedPrice());
            item.setQty(cartItem.getQuantity());
            item.setCachedImageUrl(cartItem.getCachedImageUrl());
            orderItems.add(item);
            total += cartItem.getTotalPrice();
        }
        // Tạo biến final để dùng trong lambda
        final double finalTotal = total;

        // 3. Tạo ID duy nhất cho đơn hàng mới (sử dụng biến db đã khởi tạo)
        String newOrderId = db.collection("orders").document().getId();

        // 4. Tạo đối tượng Order (trong bộ nhớ)
        Order newOrder = new Order();
        newOrder.setOrderId(newOrderId);
        newOrder.setUserId(currentUid);
        newOrder.setCustomerName(receiverName);
        newOrder.setPhone(receiverPhone);
        newOrder.setAddress(receiverAddress);
        newOrder.setTotal(finalTotal);
        newOrder.setStatus("pending_confirmation"); // Trạng thái ban đầu
        newOrder.setCreatedAt(Timestamp.now());
        newOrder.setItems(orderItems); // Gán danh sách OrderItem đã tạo
        newOrder.setCancellationRequested(false);

        // 5. Chuẩn bị dữ liệu dạng Map để lưu lên Firestore
        Map<String, Object> orderMap = new HashMap<>();
        orderMap.put("orderId", newOrder.getOrderId());
        orderMap.put("userId", newOrder.getUserId());
        orderMap.put("customerName", newOrder.getCustomerName());
        orderMap.put("phone", newOrder.getPhone());
        orderMap.put("address", newOrder.getAddress());
        orderMap.put("total", newOrder.getTotal());
        orderMap.put("status", newOrder.getStatus());
        orderMap.put("createdAt", newOrder.getCreatedAt());
        orderMap.put("cancellationRequested", newOrder.isCancellationRequested());

        // Chuyển đổi List<OrderItem> thành List<Map<String, Object>>
        List<Map<String, Object>> itemsMapList = new ArrayList<>();
        // Kiểm tra selectedItems và orderItems khớp nhau về số lượng
        if (newOrder.getItems() != null && selectedItems != null && newOrder.getItems().size() == selectedItems.size()) {
            for (int i = 0; i < newOrder.getItems().size(); i++) {
                OrderItem orderItem = newOrder.getItems().get(i); // Lấy OrderItem
                CartItem cartItem = selectedItems.get(i); // Lấy CartItem tương ứng

                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("productId", orderItem.getProductId());
                itemMap.put("name", orderItem.getName());
                itemMap.put("price", orderItem.getPrice());
                itemMap.put("qty", orderItem.getQty());
                itemMap.put("cachedImageUrl", orderItem.getCachedImageUrl());

                // Lấy thông tin variant từ CartItem tương ứng
                itemMap.put("variantId", cartItem.getVariantId());
                itemMap.put("variantName", cartItem.getVariantName());

                itemsMapList.add(itemMap);
            }
        } else {
            // Xử lý lỗi nếu danh sách không khớp hoặc null
            Log.e("CheckoutDebug", "Lỗi: Danh sách OrderItem (" + (newOrder.getItems() != null ? newOrder.getItems().size() : "null") +
                    ") và SelectedItems (" + (selectedItems != null ? selectedItems.size() : "null") + ") không khớp!");
            Toast.makeText(this, "Lỗi xử lý giỏ hàng, vui lòng thử lại.", Toast.LENGTH_SHORT).show();
            btnCheckout.setEnabled(true); // Kích hoạt lại nút
            return; // Thoát khỏi hàm
        }
        orderMap.put("items", itemsMapList); // Thêm danh sách item (dạng Map) vào orderMap

        Log.d("CheckoutDebug", "Chuẩn bị lưu Map (với variants): " + orderMap.toString());

        // 6. Lưu dữ liệu Map lên Firestore
        db.collection("orders").document(newOrderId).set(orderMap)
                .addOnSuccessListener(aVoid -> {
                    Log.d("CheckoutDebug", "Lưu bằng Map thành công! Kiểm tra Firestore.");
                    // Gọi hàm tạo thông báo (nếu có)
                    createOrderNotification(currentUid, newOrderId, finalTotal);
                    // Gọi hàm xóa các item đã mua khỏi giỏ hàng
                    removeItemsFromCart(currentUid);
                })
                .addOnFailureListener(e -> {
                    // Xử lý khi lưu Firestore thất bại
                    Log.e("CheckoutDebug", "Lỗi lưu bằng Map", e);
                    Toast.makeText(this, "Lỗi khi đặt hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnCheckout.setEnabled(true); // Kích hoạt lại nút thanh toán
                });
    }
    //Xóa các item đã mua khỏi giỏ hàng
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
                saveAddressToPrefs(receiverName, receiverPhone, address);
                handled = true;
            }

            // Payment method result
            String paymentId = data.getStringExtra("paymentMethodId");
            String paymentName = data.getStringExtra("paymentMethodName");
            String paymentIconUrl = data.getStringExtra("paymentMethodIconUrl");

            if (paymentId != null && paymentName != null) {
                tvPaymentMethod.setText(paymentName);
                if (paymentIconUrl != null && !paymentIconUrl.isEmpty()) {
                    Glide.with(this)
                            .load(paymentIconUrl)
                            .placeholder(R.drawable.ic_cod)
                            .error(R.drawable.ic_cod)
                            .into(ivPaymentIcon);
                }

                savePaymentMethodToPrefs(paymentName, paymentIconUrl != null ? paymentIconUrl : "");
                handled = true;
            }

            // fallback
            if (!handled && data.hasExtra("selected_payment_method")) {
                Object obj = data.getSerializableExtra("selected_payment_method");
                if (obj instanceof com.example.androidapp.models.PaymentMethod) {
                    com.example.androidapp.models.PaymentMethod pm = (com.example.androidapp.models.PaymentMethod) obj;
                    tvPaymentMethod.setText(pm.getName());
                    savePaymentMethodToPrefs(pm.getName(), "");
                }
            }
        }
    }

    private String formatCurrency(double amount) {
        return String.format("%,.0fđ", amount);
    }

    //tao thong bao dat hang thanh cong
    private void createOrderNotification(String userId, String orderId, double totalAmount) {
        Notification notification = new Notification(
                userId,
                "Đặt hàng thành công! ⚡",
                "Đơn hàng #" + orderId.substring(0, 8) + " trị giá " +
                        formatCurrency(totalAmount) + " đang được xử lý",
                "order"
        );

        // Set actionUrl để khi click vào notification sẽ mở OrderDetailActivity
        notification.setActionUrl("order/" + orderId);

        // Lưu thông báo
        notificationRepository.createNotification(userId, notification,
                new NotificationRepository.OnOperationListener() {
                    @Override
                    public void onSuccess(String message) {
                        Log.d("CheckoutDebug", "Notification created successfully");
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e("CheckoutDebug", "Failed to create notification", e);
                        // Silent fail - không hiển thị lỗi cho user
                    }
                });
    }
}