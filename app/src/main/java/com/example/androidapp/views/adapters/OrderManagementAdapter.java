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
import com.example.androidapp.repositories.NotificationRepository;
import com.example.androidapp.views.activities.Order.OrderDetailActivity;
import com.google.firebase.firestore.FirebaseFirestore;
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
    private final String[] statusValues; // Giá trị Tiếng Anh chat gpt han hanh tai tro chuong trinh :>


    public OrderManagementAdapter(List<Order> orders, Context context) {
        this.orders = orders;
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        // Lấy mảng từ resources
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
        if (order == null) return; // Thêm kiểm tra null cho order

        holder.tvCustomerName.setText(order.getCustomerName());

        // Kiểm tra null và độ dài orderId
        if (order.getOrderId() != null && order.getOrderId().length() >= 8) {
            holder.tvOrderId.setText("Mã đơn: #" + order.getOrderId().substring(0, 8));
        } else if (order.getOrderId() != null) {
            holder.tvOrderId.setText("Mã đơn: #" + order.getOrderId());
        } else {
            holder.tvOrderId.setText("Mã đơn: #LỖI_ID");
        }
        holder.tvTotal.setText("Tổng: " + formatCurrency(order.getTotal()));

        // --- Hiển thị dấu hiệu Yêu cầu hủy và Status Text ---
        String statusText = getStatusTitle(order.getStatus()); // Lấy tên Tiếng Việt
        if (order.isCancellationRequested() && !"cancelled".equals(order.getStatus())) {
            holder.itemView.setBackgroundColor(Color.parseColor("#FFF9C4")); // Vàng nhạt
            holder.tvStatus.setText(statusText + " (Y/C HỦY)");
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE); // Màu mặc định
            holder.tvStatus.setText(statusText); // Hiển thị tên Tiếng Việt
        }
        setStatusColor(holder.tvStatus, order.getStatus()); // Đặt màu nền chữ status

        // --- Sự kiện click ---
        holder.btnDetail.setOnClickListener(v -> {
            Intent intent = new Intent(context, OrderDetailActivity.class);
            intent.putExtra("order", order); // Gửi Parcelable
            context.startActivity(intent);
        });

        // Vô hiệu hóa nút nếu là pending_confirmation hoặc trạng thái cuối
        if ("pending_confirmation".equals(order.getStatus()) || "delivered".equals(order.getStatus()) || "cancelled".equals(order.getStatus())) {
            holder.btnUpdate.setEnabled(false);
            // Thay đổi text nút tùy ý
            if("pending_confirmation".equals(order.getStatus())) {
                holder.btnUpdate.setText("Xem chi tiết");
            } else {
                holder.btnUpdate.setText("Cập nhật"); // Giữ nguyên cho delivered/cancelled
            }
        } else {
            holder.btnUpdate.setEnabled(true);
            holder.btnUpdate.setText("Cập nhật");
            // Chỉ gán listener khi nút được kích hoạt
            holder.btnUpdate.setOnClickListener(v -> {
                showStatusDialog(order);
            });
        }
    }

    private void showStatusDialog(Order order) {
        String currentStatus = order.getStatus();
        if (currentStatus == null) return;

        final List<String> availableActionsList = new ArrayList<>(); // Dùng List để linh hoạt
        final List<String> nextStatusValuesList = new ArrayList<>();

        // Xác định các hành động tiếp theo hợp lệ
        switch (currentStatus) {
            // Trường hợp pending_confirmation đã bị chặn ở onBindViewHolder
            case "confirmed":
                availableActionsList.add(getStatusTitle("shipping")); // Bắt đầu giao hàng
                nextStatusValuesList.add("shipping");
                availableActionsList.add(getStatusTitle("cancelled")); // Hủy đơn
                nextStatusValuesList.add("cancelled");
                break;
            case "shipping":
                availableActionsList.add(getStatusTitle("delivered")); // Xác nhận đã giao
                nextStatusValuesList.add("delivered");
                availableActionsList.add(getStatusTitle("cancelled")); // Hủy đơn (Tùy chọn)
                nextStatusValuesList.add("cancelled");
                break;
            // Không có hành động cho delivered và cancelled từ danh sách
            case "delivered":
            case "cancelled":
            default:
                Log.w("OrderManagementAdapter", "showStatusDialog called for final status: " + currentStatus);
                return;
        }

        // Chuyển List thành Array để dùng trong setItem
        final String[] availableActions = availableActionsList.toArray(new String[0]);
        final String[] nextStatusValues = nextStatusValuesList.toArray(new String[0]);

        // Hiển thị dialog với các hành động hợp lệ
        new AlertDialog.Builder(context)
                .setTitle("Cập nhật trạng thái")
                .setItems(availableActions, (dialog, which) -> {
                    String nextStatus = nextStatusValues[which];
                    updateOrderStatus(order, nextStatus);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void updateOrderStatus(Order order, String newStatus) {
        if (order.getOrderId() == null) {
            Toast.makeText(context, "Lỗi: Không tìm thấy ID đơn hàng.", Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);
        updates.put("cancellationRequested", false); // Reset cờ yêu cầu

        db.collection("orders").document(order.getOrderId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    // Listener trong Fragment sẽ tự cập nhật UI
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
        // TODO: Nên dùng background drawable để bo góc thay vì màu nền trực tiếp
        tv.setBackgroundColor(color);
        tv.setTextColor(Color.WHITE); // chữ trắng
    }

    // Hàm tiện ích để lấy tên trạng thái Tiếng Việt (Thêm kiểm tra null/rỗng)
    private String getStatusTitle(String statusValue) {
        if (statusValue == null || statusValue.isEmpty()) {
            return "N/A";
        }
        for (int i = 0; i < statusValues.length; i++) {
            // So sánh an toàn với null
            if (statusValue.equals(statusValues[i])) {
                // Đảm bảo index không vượt quá mảng titles
                if (i < statusTitles.length) {
                    return statusTitles[i];
                } else {
                    Log.w("OrderManagementAdapter", "Mismatch between statusValues and statusTitles arrays for value: " + statusValue);
                    break; // Thoát vòng lặp nếu có lỗi
                }
            }
        }
        // Trả về giá trị gốc nếu không tìm thấy
        Log.w("OrderManagementAdapter", "Status value not found in arrays: " + statusValue);
        return statusValue.toUpperCase();
    }


    @Override
    public int getItemCount() {
        return orders != null ? orders.size() : 0; // Thêm kiểm tra null
    }

    // ViewHolder
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


}