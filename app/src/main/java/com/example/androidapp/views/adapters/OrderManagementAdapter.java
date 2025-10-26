package com.example.androidapp.views.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.example.androidapp.R;
import com.example.androidapp.models.Notification;
import com.example.androidapp.models.Order;
import com.example.androidapp.models.OrderItem;
import com.example.androidapp.repositories.NotificationRepository;
import com.example.androidapp.views.activities.Order.OrderDetailActivity;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OrderManagementAdapter extends RecyclerView.Adapter<OrderManagementAdapter.ViewHolder> {

    private List<Order> orders;
    private Context context;
    private FirebaseFirestore db;
    private final String[] statusTitles; // Tên Tiếng Việt
    private final String[] statusValues; // Giá trị Tiếng Anh

    public OrderManagementAdapter(List<Order> orders, Context context) {
        this.orders = orders;
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        this.statusTitles = context.getResources().getStringArray(R.array.order_status_titles);
        this.statusValues = context.getResources().getStringArray(R.array.order_status_values);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orders.get(position);
        if (order == null) return;

        holder.tvCustomerName.setText(order.getCustomerName());

        if (order.getOrderId() != null && order.getOrderId().length() >= 8) {
            holder.tvOrderId.setText("Mã đơn: #" + order.getOrderId().substring(0, 8));
        } else if (order.getOrderId() != null) {
            holder.tvOrderId.setText("Mã đơn: #" + order.getOrderId());
        } else {
            holder.tvOrderId.setText("Mã đơn: #LỖI_ID");
        }
        holder.tvTotal.setText("Tổng: " + formatCurrency(order.getTotal()));

        String statusText = getStatusTitle(order.getStatus());
        if (order.isCancellationRequested() && !"cancelled".equals(order.getStatus())) {
            holder.itemView.setBackgroundColor(Color.parseColor("#FFF9C4")); // Vàng nhạt
            holder.tvStatus.setText(statusText + " (Y/C HỦY)");
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE);
            holder.tvStatus.setText(statusText);
        }
        setStatusColor(holder.tvStatus, order.getStatus());

        holder.btnDetail.setOnClickListener(v -> {
            Intent intent = new Intent(context, OrderDetailActivity.class);
            intent.putExtra("order", order);
            context.startActivity(intent);
        });

        if ("pending_confirmation".equals(order.getStatus()) || "delivered".equals(order.getStatus()) || "cancelled".equals(order.getStatus())) {
            holder.btnUpdate.setEnabled(false);
            if("pending_confirmation".equals(order.getStatus())) {
                holder.btnUpdate.setText("Xem chi tiết");
            } else {
                holder.btnUpdate.setText("Cập nhật");
            }
        } else {
            holder.btnUpdate.setEnabled(true);
            holder.btnUpdate.setText("Cập nhật");
            holder.btnUpdate.setOnClickListener(v -> {
                showStatusDialog(order);
            });
        }
    }


    private void showStatusDialog(Order order) {
        String currentStatus = order.getStatus();
        if (currentStatus == null) return;

        final List<String> availableActionsList = new ArrayList<>();
        final List<String> nextStatusValuesList = new ArrayList<>();

        switch (currentStatus) {
            case "confirmed":
                availableActionsList.add(getStatusTitle("shipping"));
                nextStatusValuesList.add("shipping");
                availableActionsList.add(getStatusTitle("cancelled"));
                nextStatusValuesList.add("cancelled");
                break;
            case "shipping":
                availableActionsList.add(getStatusTitle("delivered"));
                nextStatusValuesList.add("delivered");
                availableActionsList.add(getStatusTitle("cancelled"));
                nextStatusValuesList.add("cancelled");
                break;
            case "delivered":
            case "cancelled":
            default:
                Log.w("OrderManagementAdapter", "showStatusDialog called for final status: " + currentStatus);
                return;
        }

        final String[] availableActions = availableActionsList.toArray(new String[0]);
        final String[] nextStatusValues = nextStatusValuesList.toArray(new String[0]);


        new AlertDialog.Builder(context)
                .setTitle("Cập nhật trạng thái")
                .setItems(availableActions, (dialog, which) -> {
                    // 1. Lấy trạng thái được chọn
                    String selectedStatusValue = nextStatusValues[which];
                    String selectedStatusText = availableActions[which]; // Tên Tiếng Việt

                    // 2. Gọi dialog xác nhận THỨ HAI
                    showAdminConfirmationDialog(order, selectedStatusValue, selectedStatusText);
                })
                .setNegativeButton("Hủy", null) // Nút Hủy cho dialog chọn trạng thái
                .show();
    }


    private void showAdminConfirmationDialog(Order order, String newStatus, String newStatusText) {
        String title;
        String message;
        String positiveText;

        // Tùy chỉnh thông báo cho hành động Hủy đơn
        if ("cancelled".equals(newStatus)) {
            title = "Xác nhận hủy đơn?";
            message = "Bạn có chắc chắn muốn hủy đơn hàng #" + order.getOrderId().substring(0, 8) +
                    "? Hàng sẽ được hoàn kho (nếu có).";
            positiveText = "Xác nhận hủy";
        } else {
            // Thông báo chung cho các hành động khác (vd: Giao hàng)
            title = "Xác nhận cập nhật?";
            message = "Bạn có muốn chuyển đơn hàng sang trạng thái \"" + newStatusText + "\"?";
            positiveText = "Đồng ý";
        }

        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveText, (dialog, which) -> {
                    // 3. Chỉ khi nhấn "Đồng ý" ở đây thì mới THỰC SỰ cập nhật
                    updateOrderStatus(order, newStatus);
                })
                .setNegativeButton("Không", null) // Nút "Không" cho dialog xác nhận
                .show();
    }


    private void updateOrderStatus(Order order, String newStatus) {
        if (order.getOrderId() == null) {
            Toast.makeText(context, "Lỗi: Không tìm thấy ID đơn hàng.", Toast.LENGTH_SHORT).show();
            return;
        }

        final String oldStatus = order.getStatus();

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);
        updates.put("cancellationRequested", false);

        db.collection("orders").document(order.getOrderId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d("StockUpdate", "Cập nhật status (từ Adapter) thành công. Old: " + oldStatus + ", New: " + newStatus);

                    if (newStatus.equals("cancelled") && (oldStatus.equals("confirmed") || oldStatus.equals("shipping"))) {
                        Log.i("StockUpdate", "LOGIC HOÀN KHO (TỪ ADAPTER) ĐƯỢC KÍCH HOẠT.");
                        updateStockForOrder(order, "increase");
                    }
                    else {
                        Log.w("StockUpdate", "Không thực hiện hành động kho (từ Adapter).");
                    }

                    Toast.makeText(context, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreUpdate", "Lỗi khi cập nhật order ID: " + order.getOrderId(), e);
                    Toast.makeText(context, "Cập nhật thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setStatusColor(TextView tv, String status) {
        int color;
        if (status == null) status = "";
        switch (status) {
            case "pending_confirmation": color = Color.parseColor("#FFA726"); break; // Cam
            case "confirmed":            color = Color.parseColor("#66BB6A"); break; // Xanh lá
            case "shipping":             color = Color.parseColor("#42A5F5"); break; // Xanh dương
            case "delivered":            color = Color.parseColor("#26A69A"); break; // Xanh mòng két
            case "cancelled":            color = Color.parseColor("#EF5350"); break; // Đỏ
            default:                     color = Color.parseColor("#BDBDBD"); break; // Xám
        }
        tv.setBackgroundColor(color);
        tv.setTextColor(Color.WHITE);
    }

    private String getStatusTitle(String statusValue) {
        if (statusValue == null || statusValue.isEmpty()) {
            return "N/A";
        }
        for (int i = 0; i < statusValues.length; i++) {
            if (statusValue.equals(statusValues[i])) {
                if (i < statusTitles.length) {
                    return statusTitles[i];
                } else {
                    Log.w("OrderManagementAdapter", "Mismatch between statusValues and statusTitles arrays for value: " + statusValue);
                    break;
                }
            }
        }
        Log.w("OrderManagementAdapter", "Status value not found in arrays: " + statusValue);
        return statusValue.toUpperCase();
    }


    @Override
    public int getItemCount() {
        return orders != null ? orders.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCustomerName, tvOrderId, tvTotal, tvStatus;
        Button btnDetail, btnUpdate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCustomerName = itemView.findViewById(R.id.tv_item_customer_name);
            tvOrderId = itemView.findViewById(R.id.tv_item_order_id);
            tvTotal = itemView.findViewById(R.id.tv_item_total);
            tvStatus = itemView.findViewById(R.id.tv_item_status);
            btnDetail = itemView.findViewById(R.id.btn_item_detail);
            btnUpdate = itemView.findViewById(R.id.btn_item_update);
        }
    }

    private String formatCurrency(double amount) {
        return NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(amount);
    }

    private void updateStockForOrder(Order order, String operation) {
        if (order == null || order.getItems() == null) {
            Log.e("StockUpdate", "BỎ QUA: Order hoặc items là null, không thể cập nhật kho.");
            return;
        }

        Log.i("StockUpdate", "--- BẮT ĐẦU TÁC VỤ KHO (TỪ ADAPTER): " + operation + " ---");

        for (com.example.androidapp.models.OrderItem item : order.getItems()) {
            if (item.getProductId() == null || item.getProductId().isEmpty() ||
                    item.getQty() <= 0) {

                Log.e("StockUpdate", "BỎ QUA ITEM: Dữ liệu cơ bản không hợp lệ. " +
                        "ProductID: " + item.getProductId() + ", " +
                        "Qty: " + item.getQty());
                continue;
            }

            String logicalProductId = item.getProductId();
            String variantId = item.getVariantId();
            int quantity = item.getQty();

            Log.d("StockUpdate", "Đang xử lý item: " + item.getName() +
                    " | LogicalPID: " + logicalProductId +
                    " | VariantID: " + variantId);

            db.collection("phones")
                    .whereEqualTo("id", logicalProductId)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (queryDocumentSnapshots.isEmpty()) {
                            Log.e("StockUpdate", "LỖI QUERY: Không tìm thấy sản phẩm nào có TRƯỜNG 'id' == " + logicalProductId);
                            Toast.makeText(context, "Lỗi kho: Không tìm thấy SP " + item.getName(), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        DocumentSnapshot productDoc = queryDocumentSnapshots.getDocuments().get(0);
                        DocumentReference productRef = productDoc.getReference();

                        Log.d("StockUpdate", "Query thành công: Tìm thấy Document ID thật: " + productRef.getId() + ". Bắt đầu Transaction...");

                        db.runTransaction((Transaction.Function<Void>) transaction -> {
                            DocumentSnapshot snapshot = transaction.get(productRef);
                            Log.d("StockUpdate", "Transaction: Đã get() snapshot cho " + snapshot.getId());

                            if (variantId != null && !variantId.isEmpty()) {

                                Log.d("StockUpdate", "Transaction: Đang xử lý sản phẩm có biến thể. VariantID: " + variantId);

                                List<Map<String, Object>> variants = (List<Map<String, Object>>) snapshot.get("variants");
                                if (variants == null || variants.isEmpty()) {
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
                                            throw new FirebaseFirestoreException("Lỗi dữ liệu kho của biến thể " + item.getName(),
                                                    FirebaseFirestoreException.Code.ABORTED);
                                        }

                                        long newStock;
                                        if ("decrease".equals(operation)) {
                                            newStock = currentStock - quantity;
                                        } else { // "increase"
                                            newStock = currentStock + quantity;
                                        }
                                        Log.d("StockUpdate", "Transaction: Cập nhật kho biến thể = " + newStock);
                                        variant.put("stock", newStock);
                                        break;
                                    }
                                }

                                if (variantFound) {
                                    Log.i("StockUpdate", "Transaction: Sắp cập nhật 'variants' lên Firestore.");
                                    transaction.update(productRef, "variants", variants);
                                } else {
                                    throw new FirebaseFirestoreException("Không tìm thấy biến thể " + item.getVariantName(),
                                            FirebaseFirestoreException.Code.ABORTED);
                                }

                            } else {

                                Log.d("StockUpdate", "Transaction: Đang xử lý sản phẩm đơn.");

                                Object stockObj = snapshot.get("stock");
                                long currentStock = 0;
                                if (stockObj instanceof Number) {
                                    currentStock = ((Number) stockObj).longValue();
                                } else {
                                    throw new FirebaseFirestoreException("Lỗi dữ liệu kho của sản phẩm " + item.getName(),
                                            FirebaseFirestoreException.Code.ABORTED);
                                }
                                Log.d("StockUpdate", "Transaction: Kho gốc hiện tại: " + currentStock);

                                long newStock;
                                if ("decrease".equals(operation)) {
                                    newStock = currentStock - quantity;
                                } else { // "increase"
                                    newStock = currentStock + quantity;
                                }
                                Log.d("StockUpdate", "Transaction: Cập nhật kho gốc = " + newStock);

                                Log.i("StockUpdate", "Transaction: Sắp cập nhật 'stock' (gốc) lên Firestore.");
                                transaction.update(productRef, "stock", newStock);
                            }

                            return null;
                        }).addOnSuccessListener(aVoid -> {
                            Log.i("StockUpdate", "--- TRANSACTION THÀNH CÔNG (TỪ ADAPTER) cho LogicalPID: " + logicalProductId + " ---");
                        }).addOnFailureListener(e -> {
                            Log.e("StockUpdate", "--- TRANSACTION THẤT BẠI (TỪ ADAPTER) cho LogicalPID: " + logicalProductId + " ---", e);
                            Toast.makeText(context, "Lỗi cập nhật kho: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });

                    })
                    .addOnFailureListener(e -> {
                        Log.e("StockUpdate", "LỖI QUERY: Thất bại khi query 'id' == " + logicalProductId, e);
                        Toast.makeText(context, "Lỗi mạng khi tìm SP: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}