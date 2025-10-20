package com.example.androidapp.views.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color; // <-- Import
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
import com.example.androidapp.models.Order;
import com.example.androidapp.views.activities.Order.OrderDetailActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.NumberFormat;
import java.util.HashMap; // <-- Import
import java.util.List;
import java.util.Locale;
import java.util.Map; // <-- Import

public class OrderManagementAdapter extends RecyclerView.Adapter<OrderManagementAdapter.ViewHolder> {

    private List<Order> orders;
    private Context context;
    private FirebaseFirestore db;
    private final String[] statusTitles;
    private final String[] statusValues;

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

        holder.tvCustomerName.setText(order.getCustomerName());
        // Kiểm tra null trước khi substring
        if (order.getOrderId() != null && order.getOrderId().length() >= 8) {
            holder.tvOrderId.setText("Mã đơn: #" + order.getOrderId().substring(0, 8));
        } else if (order.getOrderId() != null) {
            holder.tvOrderId.setText("Mã đơn: #" + order.getOrderId());
        } else {
            holder.tvOrderId.setText("Mã đơn: #LỖI_ID");
        }
        holder.tvTotal.setText("Tổng: " + formatCurrency(order.getTotal()));

        // --- Hiển thị dấu hiệu Yêu cầu hủy ---
        String statusText = order.getStatus() != null ? order.getStatus().toUpperCase() : "N/A";
        if (order.isCancellationRequested() && !"cancelled".equals(order.getStatus())) {
            holder.itemView.setBackgroundColor(Color.parseColor("#FFF9C4")); // Vàng nhạt
            holder.tvStatus.setText(statusText + " (Y/C HỦY)");
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE); // Màu mặc định
            holder.tvStatus.setText(statusText);
        }
        setStatusColor(holder.tvStatus, order.getStatus());

        // --- Sự kiện click ---
        holder.btnDetail.setOnClickListener(v -> {
            Intent intent = new Intent(context, OrderDetailActivity.class);
            intent.putExtra("order", order);
            context.startActivity(intent);
        });

        holder.btnUpdate.setOnClickListener(v -> {
            showStatusDialog(order);
        });
    }

    private void showStatusDialog(Order order) {
        new AlertDialog.Builder(context)
                .setTitle("Cập nhật trạng thái")
                .setItems(statusTitles, (dialog, which) -> {
                    String newStatusValue = statusValues[which];
                    updateOrderStatus(order, newStatusValue);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // --- Cập nhật logic update ---
    private void updateOrderStatus(Order order, String newStatus) {
        if (order.getOrderId() == null) {
            Toast.makeText(context, "Lỗi: Không tìm thấy ID đơn hàng.", Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);
        updates.put("cancellationRequested", false); // <-- RESET CỜ YÊU CẦU

        db.collection("orders").document(order.getOrderId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Log.e("FirestoreUpdate", "Lỗi khi cập nhật order ID: " + order.getOrderId(), e); // Đã thêm ở lần sửa trước
                    Toast.makeText(context, "Cập nhật thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setStatusColor(TextView tv, String status) {
        int color;
        if (status == null) status = ""; // Tránh null
        switch (status) {
            case "pending": color = Color.parseColor("#FFA726"); break; // Cam
            case "shipped": color = Color.parseColor("#42A5F5"); break; // Xanh dương
            case "completed": color = Color.parseColor("#66BB6A"); break; // Xanh lá
            case "cancelled": color = Color.parseColor("#EF5350"); break; // Đỏ
            default: color = Color.parseColor("#BDBDBD"); break; // Xám
        }
        tv.setBackgroundColor(color);
        // Đặt màu chữ là trắng để dễ đọc trên nền màu
        tv.setTextColor(Color.WHITE);
    }


    @Override
    public int getItemCount() {
        return orders.size();
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
}