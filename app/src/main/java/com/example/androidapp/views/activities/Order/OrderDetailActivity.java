package com.example.androidapp.views.activities.Order;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.androidapp.R;
import com.example.androidapp.models.Order;
import com.example.androidapp.repositories.NotificationRepository;
import com.example.androidapp.views.adapters.OrderItemDetailAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import java.util.List;


public class OrderDetailActivity extends AppCompatActivity {

    private Order order;

    // Views
    private Toolbar toolbarDetail;
    private TextView tvOrderId, tvCustomerName, tvPhone, tvAddress, tvTotal, tvCreatedAt;
    private RecyclerView rvOrderItems;
    private OrderItemDetailAdapter itemAdapter;
    private TextView iconPending, textPending, iconConfirmed, textConfirmed, iconShipping, textShipping, iconDelivered, textDelivered;
    private View line1, line2, line3;
    private LinearLayout layoutActionButtons;
    private Button btnNextStatusAdmin, btnCancelOrderDetail;
    private TextView tvCancelRequestedIndicator;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String currentUserRole = "user";
    private NotificationRepository notificationRepository; // thong bao

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        notificationRepository = new NotificationRepository();

        if (getIntent().hasExtra("order")) {
            order = getIntent().getParcelableExtra("order");
        } else {
            Log.e("OrderDetailActivity", "Không nhận được dữ liệu Order.");
            Toast.makeText(this, "Lỗi: Không thể tải chi tiết đơn hàng.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        displayOrderInfo();
        fetchCurrentUserRoleAndSetupUI();
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

        iconPending = findViewById(R.id.tv_tracker_icon_pending);
        textPending = findViewById(R.id.tv_tracker_text_pending);
        line1 = findViewById(R.id.line1);
        iconConfirmed = findViewById(R.id.tv_tracker_icon_confirmed);
        textConfirmed = findViewById(R.id.tv_tracker_text_confirmed);
        line2 = findViewById(R.id.line2);
        iconShipping = findViewById(R.id.tv_tracker_icon_shipping);
        textShipping = findViewById(R.id.tv_tracker_text_shipping);
        line3 = findViewById(R.id.line3);
        iconDelivered = findViewById(R.id.tv_tracker_icon_delivered);
        textDelivered = findViewById(R.id.tv_tracker_text_delivered);

        layoutActionButtons = findViewById(R.id.layout_action_buttons);
        btnCancelOrderDetail = findViewById(R.id.btn_cancel_order_detail);
        btnNextStatusAdmin = findViewById(R.id.btn_next_status_admin);
        tvCancelRequestedIndicator = findViewById(R.id.tv_cancel_requested_indicator_detail);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbarDetail);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbarDetail.setNavigationOnClickListener(v -> finish());
    }

    private void displayOrderInfo() {
        if (order == null) return;
        tvOrderId.setText("Mã đơn: #" + (order.getOrderId() != null ? order.getOrderId() : "N/A"));
        tvCustomerName.setText("Tên: " + (order.getCustomerName() != null ? order.getCustomerName() : "N/A"));
        tvPhone.setText("SĐT: " + (order.getPhone() != null ? order.getPhone() : "N/A"));
        tvAddress.setText("Địa chỉ: " + (order.getAddress() != null ? order.getAddress() : "N/A"));
        tvTotal.setText("Tổng tiền: " + formatCurrency(order.getTotal()));
        if (order.getCreatedAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm, dd/MM/yyyy", Locale.getDefault());
            tvCreatedAt.setText("Ngày đặt: " + sdf.format(order.getCreatedAt().toDate()));
        } else {
            tvCreatedAt.setText("Ngày đặt: N/A");
        }
    }

    private void setupItemsRecyclerView() {
        if (order != null && order.getItems() != null) {
            rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
            itemAdapter = new OrderItemDetailAdapter(order.getItems(), this);
            rvOrderItems.setAdapter(itemAdapter);
        } else {
            Log.e("OrderDetailActivity", "Order items list is null when setting up RecyclerView.");
        }
    }

    private void fetchCurrentUserRoleAndSetupUI() {
        if (auth.getCurrentUser() == null) {
            Log.e("OrderDetailActivity", "User not logged in, cannot fetch role. Assuming 'user' role.");
            currentUserRole = "user";
            if (order != null && order.getStatus() != null) {
                updateStatusTracker(order.getStatus());
                setupActionButtons(order.getStatus());
            }
            return;
        }
        String uid = auth.getCurrentUser().getUid();
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        currentUserRole = (role != null) ? role : "user";
                    } else {
                        currentUserRole = "user";
                    }
                    Log.d("OrderDetailActivity", "Current user role fetched: " + currentUserRole);
                    if (order != null && order.getStatus() != null) {
                        updateStatusTracker(order.getStatus());
                        setupActionButtons(order.getStatus());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("OrderDetailActivity", "Error fetching user role", e);
                    currentUserRole = "user";
                    if (order != null && order.getStatus() != null) {
                        updateStatusTracker(order.getStatus());
                        setupActionButtons(order.getStatus());
                    }
                });
    }

    //Thiết lập (ẩn/hiện, bật/tắt, đặt nhãn, và gán hành động) cho các nút thao tác của đơn hàng, dựa trên vai trò người dùng (admin hay user) và trạng thái hiện tại của đơn hàng (status)
    private void setupActionButtons(String status) {
        boolean isAdmin = "admin".equals(currentUserRole);
        boolean isPendingConfirmation = "pending_confirmation".equals(status);
        boolean isConfirmed = "confirmed".equals(status);
        boolean isShipping = "shipping".equals(status);
        boolean isFinalState = "delivered".equals(status) || "cancelled".equals(status);
        boolean isCancelRequested = order != null && order.isCancellationRequested();

        // Mặc định ẩn và disable
        layoutActionButtons.setVisibility(View.GONE);
        btnNextStatusAdmin.setVisibility(View.GONE);
        btnCancelOrderDetail.setVisibility(View.GONE);
        btnNextStatusAdmin.setEnabled(false); // Disable mặc định
        btnCancelOrderDetail.setEnabled(false); // Disable mặc định
        if(tvCancelRequestedIndicator != null) tvCancelRequestedIndicator.setVisibility(View.GONE);

        if (isFinalState) return; // Không hiển thị nút nếu đã xong

        if (isAdmin) {
            layoutActionButtons.setVisibility(View.VISIBLE);

            // Nút trạng thái tiếp theo
            String nextStatusText = null;
            String nextStatusValue = null;
            if (isPendingConfirmation) {
                nextStatusText = "Xác nhận đơn";
                nextStatusValue = "confirmed";
            } else if (isConfirmed) {
                nextStatusText = "Bắt đầu giao";
                nextStatusValue = "shipping";
            } else if (isShipping) {
                nextStatusText = "Đã giao";
                nextStatusValue = "delivered";
            }

            if (nextStatusText != null) {
                btnNextStatusAdmin.setText(nextStatusText);
                btnNextStatusAdmin.setVisibility(View.VISIBLE);
                btnNextStatusAdmin.setEnabled(true); // <-- KÍCH HOẠT NÚT

                final String finalNextStatusText = nextStatusText;
                final String finalNextStatusValue = nextStatusValue;
                btnNextStatusAdmin.setOnClickListener(v -> {
                    showAdminConfirmationDialog("Cập nhật trạng thái?",
                            "Chuyển đơn hàng sang trạng thái \"" + finalNextStatusText + "\"?",
                            "Đồng ý", finalNextStatusValue);
                });
            }

            // Nút Hủy (Admin luôn thấy trừ khi đã final)
            btnCancelOrderDetail.setVisibility(View.VISIBLE);
            btnCancelOrderDetail.setEnabled(true); // <-- KÍCH HOẠT NÚT
            btnCancelOrderDetail.setText("Hủy đơn");
            btnCancelOrderDetail.setOnClickListener(v -> {
                showAdminConfirmationDialog("Hủy đơn hàng?",
                        "Bạn có chắc chắn muốn hủy đơn hàng này không?",
                        "Hủy đơn", "cancelled");
            });

            // Hiển thị chỉ báo nếu User yêu cầu hủy
            if(isCancelRequested && tvCancelRequestedIndicator != null){
                tvCancelRequestedIndicator.setVisibility(View.VISIBLE);
            }

        } else { // User
            boolean canCancelDirectly = isPendingConfirmation;
            boolean canRequestCancel = isConfirmed || isShipping;

            if (isCancelRequested) {
                layoutActionButtons.setVisibility(View.VISIBLE);
                if(tvCancelRequestedIndicator != null) tvCancelRequestedIndicator.setVisibility(View.VISIBLE);
            }
            else if (canCancelDirectly || canRequestCancel) {
                layoutActionButtons.setVisibility(View.VISIBLE);
                btnCancelOrderDetail.setVisibility(View.VISIBLE);
                btnCancelOrderDetail.setEnabled(true); // <-- KÍCH HOẠT NÚT
                btnCancelOrderDetail.setText(canCancelDirectly ? "Hủy đơn" : "Yêu cầu hủy");
                btnCancelOrderDetail.setOnClickListener(v -> {
                    showUserCancelDialog(canCancelDirectly);
                });
            }
        }
    }

    private void showAdminConfirmationDialog(String title, String message, String positiveText, String newStatus) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveText, (dialog, which) -> updateOrderStatusByAdmin(newStatus))
                .setNegativeButton("Không", null)
                .show();
    }

    private void showUserCancelDialog(boolean isDirectCancel) {
        String title = isDirectCancel ? "Xác nhận hủy đơn" : "Yêu cầu hủy đơn";
        String message = isDirectCancel ? "Bạn có chắc chắn muốn hủy đơn hàng này?" : "Bạn muốn yêu cầu hủy đơn hàng này? Yêu cầu sẽ được gửi đến quản trị viên.";
        String positiveButtonText = isDirectCancel ? "Đồng ý" : "Gửi yêu cầu";

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveButtonText, (dialog, which) -> {
                    if (isDirectCancel) {
                        updateOrderStatusByUser("cancelled");
                    } else {
                        requestCancellationByUser();
                    }
                })
                .setNegativeButton("Không", null)
                .show();
    }

    // ---CẬP NHẬT KHO HÀNG---
    /**
     * Cập nhật kho hàng cho các sản phẩm trong đơn hàng bằng Transaction.
     * @param order Đơn hàng chứa các item
     * @param operation "decrease" (giảm kho) hoặc "increase" (hoàn kho)
     */
    private void updateStockForOrder(Order order, String operation) {
        if (order == null || order.getItems() == null) {
            Log.e("StockUpdate", "BỎ QUA: Order hoặc items là null, không thể cập nhật kho.");
            return;
        }

        Log.i("StockUpdate", "--- BẮT ĐẦU TÁC VỤ KHO: " + operation + " ---");

        for (com.example.androidapp.models.OrderItem item : order.getItems()) {
            // KIỂM TRA DỮ LIỆU ITEM
            if (item.getProductId() == null || item.getProductId().isEmpty() ||
                    item.getVariantId() == null || item.getVariantId().isEmpty() ||
                    item.getQty() <= 0) {

                Log.e("StockUpdate", "BỎ QUA ITEM: Dữ liệu không hợp lệ. " +
                        "ProductID: " + item.getProductId() + ", " +
                        "VariantID: " + item.getVariantId() + ", " +
                        "Qty: " + item.getQty());
                continue; // Bỏ qua item này nếu thiếu thông tin
            }


            String logicalProductId = item.getProductId();
            String variantId = item.getVariantId();
            int quantity = item.getQty();

            Log.d("StockUpdate", "Đang xử lý item: " + item.getName() +
                    " | LogicalPID: " + logicalProductId +
                    " | VariantID: " + variantId);



            db.collection("phones")
                    .whereEqualTo("id", logicalProductId) // Query vào trường 'id'
                    .limit(1)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (queryDocumentSnapshots.isEmpty()) {
                            // Không tìm thấy sản phẩm nào có TRƯỜNG 'id' khớp
                            Log.e("StockUpdate", "LỖI QUERY: Không tìm thấy sản phẩm nào có TRƯỜNG 'id' == " + logicalProductId);
                            Toast.makeText(OrderDetailActivity.this, "Lỗi kho: Không tìm thấy SP " + item.getName(), Toast.LENGTH_SHORT).show();
                            return;
                        }


                        DocumentSnapshot productDoc = queryDocumentSnapshots.getDocuments().get(0);
                        DocumentReference productRef = productDoc.getReference();

                        Log.d("StockUpdate", "Query thành công: Tìm thấy Document ID thật: " + productRef.getId() + ". Bắt đầu Transaction...");

                        db.runTransaction((Transaction.Function<Void>) transaction -> {
                            DocumentSnapshot snapshot = transaction.get(productRef);

                            Log.d("StockUpdate", "Transaction: Đã get() snapshot cho " + snapshot.getId());

                            List<Map<String, Object>> variants = (List<Map<String, Object>>) snapshot.get("variants");
                            if (variants == null || variants.isEmpty()) {
                                Log.e("StockUpdate", "Transaction Thất bại: Sản phẩm " + snapshot.getId() + " không có mảng 'variants'");
                                throw new FirebaseFirestoreException("Sản phẩm " + item.getName() + " không có biến thể.",
                                        FirebaseFirestoreException.Code.ABORTED);
                            }

                            boolean variantFound = false;
                            for (Map<String, Object> variant : variants) {
                                String id = (String) variant.get("id");

                                if (variantId.equals(id)) {
                                    variantFound = true;
                                    Log.d("StockUpdate", "Transaction: Đã tìm thấy VariantID khớp: " + variantId);

                                    Object stockObj = variant.get("stock");
                                    long currentStock = 0;
                                    if (stockObj instanceof Number) {
                                        currentStock = ((Number) stockObj).longValue();
                                    } else {
                                        Log.e("StockUpdate", "Transaction Thất bại: Trường 'stock' ("+stockObj+") không phải là số hoặc bị null: " + variantId);
                                        throw new FirebaseFirestoreException("Lỗi dữ liệu kho của sản phẩm " + item.getName(),
                                                FirebaseFirestoreException.Code.ABORTED);
                                    }
                                    Log.d("StockUpdate", "Transaction: Kho hiện tại: " + currentStock);

                                    long newStock;
                                    if ("decrease".equals(operation)) {
                                        newStock = currentStock - quantity;
                                        Log.d("StockUpdate", "Transaction: Giảm kho: " + currentStock + " - " + quantity + " = " + newStock);
                                        if (newStock < 0) {
                                            Log.w("StockUpdate", "Cảnh báo: Kho bị âm cho " + variantId + ": " + newStock);
                                        }
                                    } else { // "increase"
                                        newStock = currentStock + quantity;
                                        Log.d("StockUpdate", "Transaction: Tăng kho: " + currentStock + " + " + quantity + " = " + newStock);
                                    }

                                    variant.put("stock", newStock);
                                    break; // Thoát vòng lặp for
                                }
                            }

                            if (variantFound) {
                                Log.i("StockUpdate", "Transaction: Sắp cập nhật 'variants' lên Firestore.");
                                transaction.update(productRef, "variants", variants);
                            } else {
                                Log.e("StockUpdate", "Transaction Thất bại: Không tìm thấy Variant ID " + variantId + " trong mảng 'variants'");
                                throw new FirebaseFirestoreException("Không tìm thấy biến thể " + item.getVariantName() + " của sản phẩm " + item.getName(),
                                        FirebaseFirestoreException.Code.ABORTED);
                            }
                            return null;
                        }).addOnSuccessListener(aVoid -> {
                            Log.i("StockUpdate", "--- TRANSACTION THÀNH CÔNG cho LogicalPID: " + logicalProductId + " (DocID: " + productRef.getId() + ") ---");
                        }).addOnFailureListener(e -> {
                            Log.e("StockUpdate", "--- TRANSACTION THẤT BẠI cho LogicalPID: " + logicalProductId + " ---", e);
                            Toast.makeText(OrderDetailActivity.this, "Lỗi cập nhật kho: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });

                    })
                    .addOnFailureListener(e -> {
                        // Lỗi khi Query (mất mạng, không có quyền, index...)
                        Log.e("StockUpdate", "LỖI QUERY: Thất bại khi query 'id' == " + logicalProductId, e);
                        Toast.makeText(OrderDetailActivity.this, "Lỗi mạng khi tìm SP: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }


    private void updateOrderStatusByAdmin(String newStatus) {
        if (order == null || order.getOrderId() == null) return;
        // Vô hiệu hóa nút tạm thời
        if (btnNextStatusAdmin != null) btnNextStatusAdmin.setEnabled(false);
        if (btnCancelOrderDetail != null) btnCancelOrderDetail.setEnabled(false);

        String oldStatus = order.getStatus(); // Lấy trạng thái CŨ

        Log.d("StockUpdate", "Attempting status update. OldStatus: '" + oldStatus + "', NewStatus: '" + newStatus + "'");

        // Chuẩn bị cập nhật status cho Order
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);
        updates.put("cancellationRequested", false);

        // BƯỚC 1: Cập nhật trạng thái của Order LÊN FIRESTORE TRƯỚC
        db.collection("orders").document(order.getOrderId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    // BƯỚC 2: CHỈ KHI BƯỚC 1 THÀNH CÔNG, MỚI CẬP NHẬT KHO
                    Log.d("StockUpdate", "Cập nhật status Order thành công. Bắt đầu cập nhật kho.");



                    // 1. Logic giảm kho khi Admin XÁC NHẬN
                    if (newStatus.equals("confirmed") && oldStatus.equals("pending_confirmation")) {
                        Log.i("StockUpdate", "LOGIC TRỪ KHO ĐƯỢC KÍCH HOẠT.");
                        updateStockForOrder(order, "decrease");
                    }
                    // 2. Logic hoàn kho khi Admin HỦY
                    else if (newStatus.equals("cancelled") && (oldStatus.equals("confirmed") || oldStatus.equals("shipping"))) {
                        Log.i("StockUpdate", "LOGIC HOÀN KHO ĐƯỢC KÍCH HOẠT.");
                        updateStockForOrder(order, "increase");
                    }
                    else {
                        Log.w("StockUpdate", "Không thực hiện hành động kho. (Điều kiện if/else if không được đáp ứng)");
                    }


                    // BƯỚC 3: Cập nhật UI và biến local
                    Toast.makeText(this, "Cập nhật trạng thái thành công!", Toast.LENGTH_SHORT).show();
                    sendOrderStatusNotification(order.getUserId(), order.getOrderId(), newStatus, order.getTotal());

                    order.setStatus(newStatus); // Cập nhật biến local
                    order.setCancellationRequested(false);
                    updateStatusTracker(newStatus);
                    setupActionButtons(newStatus);
                })
                .addOnFailureListener(e -> {

                    Log.e("StockUpdate", "Cập nhật status Order thất bại. KHÔNG cập nhật kho.", e);
                    Toast.makeText(this, "Cập nhật thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    setupActionButtons(order.getStatus());
                });
    }

    private void updateOrderStatusByUser(String newStatus) {
        if (!"cancelled".equals(newStatus)) return;
        if (order == null || order.getOrderId() == null) return;
        if (btnCancelOrderDetail != null) btnCancelOrderDetail.setEnabled(false);



        db.collection("orders").document(order.getOrderId())
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã hủy đơn hàng.", Toast.LENGTH_SHORT).show();
                    order.setStatus(newStatus);
                    updateStatusTracker(newStatus);
                    setupActionButtons(newStatus);
                    sendOrderStatusNotification(order.getUserId(), order.getOrderId(),newStatus, order.getTotal());

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Hủy đơn thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    if (btnCancelOrderDetail != null) btnCancelOrderDetail.setEnabled(true);
                });
    }

    private void requestCancellationByUser() {
        if (order == null || order.getOrderId() == null) return;
        if (btnCancelOrderDetail != null) btnCancelOrderDetail.setEnabled(false);

        db.collection("orders").document(order.getOrderId())
                .update("cancellationRequested", true)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã gửi yêu cầu hủy đơn.", Toast.LENGTH_SHORT).show();
                    order.setCancellationRequested(true);
                    setupActionButtons(order.getStatus());

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gửi yêu cầu thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    if (btnCancelOrderDetail != null) btnCancelOrderDetail.setEnabled(true);
                });
    }

    private void updateStatusTracker(String status) {
        if (status == null) status = "";
        int activeColor = ContextCompat.getColor(this, R.color.primary_blue);
        int inactiveColor = Color.parseColor("#BDBDBD");
        int cancelledColor = Color.parseColor("#EF5350");

        setTrackerStep(iconPending, textPending, line1, inactiveColor, Typeface.NORMAL);
        setTrackerStep(iconConfirmed, textConfirmed, line2, inactiveColor, Typeface.NORMAL);
        setTrackerStep(iconShipping, textShipping, line3, inactiveColor, Typeface.NORMAL);
        setTrackerStep(iconDelivered, textDelivered, null, inactiveColor, Typeface.NORMAL);
        textPending.setText("Chờ xác nhận");
        textConfirmed.setText("Đã xác nhận");
        textShipping.setText("Đang giao");
        textDelivered.setText("Đã giao");

        switch (status) {
            case "delivered":
                setTrackerStep(iconPending, textPending, line1, activeColor, Typeface.BOLD);
                setTrackerStep(iconConfirmed, textConfirmed, line2, activeColor, Typeface.BOLD);
                setTrackerStep(iconShipping, textShipping, line3, activeColor, Typeface.BOLD);
                setTrackerStep(iconDelivered, textDelivered, null, activeColor, Typeface.BOLD);
                break;
            case "shipping":
                setTrackerStep(iconPending, textPending, line1, activeColor, Typeface.BOLD);
                setTrackerStep(iconConfirmed, textConfirmed, line2, activeColor, Typeface.BOLD);
                setTrackerStep(iconShipping, textShipping, line3, activeColor, Typeface.BOLD);
                break;
            case "confirmed":
                setTrackerStep(iconPending, textPending, line1, activeColor, Typeface.BOLD);
                setTrackerStep(iconConfirmed, textConfirmed, line2, activeColor, Typeface.BOLD);
                break;
            case "pending_confirmation":
                setTrackerStep(iconPending, textPending, line1, activeColor, Typeface.BOLD);
                break;
            case "cancelled":
                setTrackerStep(iconPending, textPending, line1, cancelledColor, Typeface.NORMAL);
                setTrackerStep(iconConfirmed, textConfirmed, line2, cancelledColor, Typeface.NORMAL);
                setTrackerStep(iconShipping, textShipping, line3, cancelledColor, Typeface.NORMAL);
                setTrackerStep(iconDelivered, textDelivered, null, cancelledColor, Typeface.NORMAL);
                textPending.setText("Đã hủy");
                break;
            default:
                Log.w("OrderDetailActivity", "Unknown status: " + status);
                break;
        }
    }

    private void setTrackerStep(TextView icon, TextView text, @Nullable View line, int color, int typefaceStyle) {
        icon.setBackgroundColor(color);
        icon.setTextColor(Color.WHITE);
        text.setTextColor(color);
        text.setTypeface(null, typefaceStyle);
        if (line != null) {
            line.setBackgroundColor(color);
        }
    }

    private String formatCurrency(double amount) {
        return NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(amount);
    }
    private void sendOrderStatusNotification(String userId, String orderId, String newStatus, double total) {
        if (userId == null || orderId == null) {
            Log.w("OrderDetailActivity", "Cannot send notification: userId or orderId is null");
            return;
        }

        Log.d("OrderDetailActivity", "Attempting to send notification - userId: " + userId +
                ", orderId: " + orderId + ", status: " + newStatus);

        String shortOrderId = orderId.length() >= 8 ? orderId.substring(0, 8) : orderId;

        // Lấy tên sản phẩm đầu tiên (hoặc nhiều sản phẩm)
        String productInfo = getProductInfoForNotification();

        String title = "";
        String message = "";

        switch (newStatus) {
            case "confirmed":
                title = "Đơn hàng đã được xác nhận ✅";
                message = "Đơn hàng #" + shortOrderId + " (" + productInfo + ") đã được xác nhận và đang chuẩn bị";
                break;

            case "shipping":
                title = "Đơn hàng đang được giao 🚚";
                message = "Đơn hàng #" + shortOrderId + " (" + productInfo + ") đang trên đường giao đến bạn";
                break;

            case "delivered":
                title = "Đơn hàng đã giao thành công 🎉";
                message = "Đơn hàng #" + shortOrderId + " (" + productInfo + ") đã được giao thành công. Cảm ơn bạn đã mua hàng!";
                break;

            case "cancelled":
                title = "Đơn hàng đã bị hủy ❌";
                message = "Đơn hàng #" + shortOrderId + " (" + productInfo + ") đã bị hủy. Số tiền " +
                        formatCurrency(total) + " sẽ được hoàn lại (nếu đã thanh toán)";
                break;

            default:
                title = "Cập nhật đơn hàng";
                message = "Đơn hàng #" + shortOrderId + " (" + productInfo + ") đã được cập nhật trạng thái";
                break;
        }

        Log.d("OrderDetailActivity", "Creating notification - title: " + title + ", message: " + message);

        // Tạo notification
        com.example.androidapp.models.Notification notification =
                new com.example.androidapp.models.Notification(userId, title, message, "order");

        notification.setActionUrl("order/" + orderId);

        Log.d("OrderDetailActivity", "Calling createNotification...");

        // Lưu thông báo
        notificationRepository.createNotification(userId, notification,
                new com.example.androidapp.repositories.NotificationRepository.OnOperationListener() {
                    @Override
                    public void onSuccess(String msg) {
                        Log.d("OrderDetailActivity", "Notification sent successfully for order: " + orderId);
                        Toast.makeText(OrderDetailActivity.this, "Đã gửi thông báo đến khách hàng", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e("OrderDetailActivity", "Failed to send notification for order: " + orderId, e);
                        Toast.makeText(OrderDetailActivity.this, "Lỗi gửi thông báo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    // if có 1 sản phẩm thì hiển thị tên của sản phâm
    // if > 1 sản phẩm sẽ gọi hàm này để lấy tên sản phẩm đầu tiên
    private String getProductInfoForNotification() {
        if (order == null || order.getItems() == null || order.getItems().isEmpty()) {
            return "sản phẩm";
        }

        int itemCount = order.getItems().size();
        String firstName = order.getItems().get(0).getName();

        // Rút gọn tên nếu quá dài
        if (firstName != null && firstName.length() > 30) {
            firstName = firstName.substring(0, 27) + "...";
        }

        if (itemCount == 1) {
            return firstName != null ? firstName : "sản phẩm";
        } else {
            return firstName + " và " + (itemCount - 1) + " sản phẩm khác";
        }
    }

}