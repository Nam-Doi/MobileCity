package com.example.androidapp.views.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button; // <-- Import
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast; // <-- Import
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog; // <-- Import
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.androidapp.R;
import com.example.androidapp.models.Order;
import com.example.androidapp.views.activities.Order.OrderDetailActivity;
import com.google.firebase.firestore.FirebaseFirestore; // <-- Import
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class MyOrdersAdapter extends RecyclerView.Adapter<MyOrdersAdapter.ViewHolder> {

    private List<Order> orders;
    private Context context;

    public MyOrdersAdapter(List<Order> orders, Context context) {
        this.orders = orders;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_my_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orders.get(position);

        // --- Gán dữ liệu ---
        holder.tvTotal.setText(formatCurrency(order.getTotal()));
        holder.tvStatus.setText(order.getStatus().toUpperCase());

        // Logic hiển thị sản phẩm
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            String firstItemName = order.getItems().get(0).getName();
            holder.tvProductName.setText(firstItemName);
            // TODO: Tải ảnh
            // Glide.with(context).load(order.getItems().get(0).getImageUrl()).into(holder.ivProduct);
            holder.ivProduct.setImageResource(R.drawable.ic_launcher_background); // Ảnh mẫu

            int extraItems = order.getItems().size() - 1;
            if (extraItems > 0) {
                holder.tvExtraCount.setText("và " + extraItems + " sản phẩm khác");
                holder.tvExtraCount.setVisibility(View.VISIBLE);
            } else {
                holder.tvExtraCount.setVisibility(View.GONE);
            }
        }

        // --- Logic Nút Hủy MỚI ---
        holder.btnCancel.setOnClickListener(v -> {
            if ("pending".equals(order.getStatus())) {
                showConfirmCancelDialog(order, true); // true = hủy trực tiếp
            } else {
                // Chỉ cho phép yêu cầu hủy nếu chưa bị hủy hoặc chưa hoàn thành
                if (!"cancelled".equals(order.getStatus()) && !"completed".equals(order.getStatus())) {
                    showConfirmCancelDialog(order, false); // false = gửi yêu cầu
                } else {
                    Toast.makeText(context, "Không thể yêu cầu hủy đơn hàng này.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Ẩn nút hủy nếu đơn đã hoàn thành hoặc đã hủy
        if ("completed".equals(order.getStatus()) || "cancelled".equals(order.getStatus())) {
            holder.btnCancel.setVisibility(View.GONE);
        } else {
            holder.btnCancel.setVisibility(View.VISIBLE);
        }

        // Sự kiện click vào item để xem chi tiết
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, OrderDetailActivity.class);
            intent.putExtra("order", order);
            context.startActivity(intent);
        });
    }

    // Hàm hiển thị Dialog xác nhận
    private void showConfirmCancelDialog(Order order, boolean isDirectCancel) {
        String message = isDirectCancel ? "Bạn có chắc chắn muốn hủy đơn hàng này?" : "Bạn muốn yêu cầu hủy đơn hàng này? Yêu cầu sẽ được gửi đến quản trị viên.";
        String positiveButtonText = isDirectCancel ? "Đồng ý" : "Gửi yêu cầu";

        new AlertDialog.Builder(context)
                .setTitle(isDirectCancel ? "Xác nhận hủy đơn" : "Yêu cầu hủy đơn")
                .setMessage(message)
                .setPositiveButton(positiveButtonText, (dialog, which) -> {
                    if (isDirectCancel) {
                        cancelOrderDirectly(order);
                    } else {
                        requestCancellation(order);
                    }
                })
                .setNegativeButton("Không", null)
                .show();
    }

    // Hàm Hủy trực tiếp
    private void cancelOrderDirectly(Order order) {
        FirebaseFirestore.getInstance().collection("orders").document(order.getOrderId())
                .update("status", "cancelled")
                .addOnSuccessListener(aVoid -> Toast.makeText(context, "Đã hủy đơn hàng.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(context, "Hủy đơn thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // Hàm Gửi yêu cầu hủy
    private void requestCancellation(Order order) {
        FirebaseFirestore.getInstance().collection("orders").document(order.getOrderId())
                .update("cancellationRequested", true)
                .addOnSuccessListener(aVoid -> Toast.makeText(context, "Đã gửi yêu cầu hủy đơn.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(context, "Gửi yêu cầu thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct;
        TextView tvProductName, tvExtraCount, tvStatus, tvTotal;
        Button btnCancel; // <-- Thêm nút Hủy

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.iv_item_product_image);
            tvProductName = itemView.findViewById(R.id.tv_item_product_name);
            tvExtraCount = itemView.findViewById(R.id.tv_item_extra_count);
            tvStatus = itemView.findViewById(R.id.tv_item_status_user);
            tvTotal = itemView.findViewById(R.id.tv_item_total_user);
            btnCancel = itemView.findViewById(R.id.btn_item_cancel_user); // <-- Ánh xạ nút Hủy
        }
    }

    private String formatCurrency(double amount) {
        return NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(amount);
    }
}