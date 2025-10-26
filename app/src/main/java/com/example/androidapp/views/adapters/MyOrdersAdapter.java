package com.example.androidapp.views.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.androidapp.R;
import com.example.androidapp.models.Order;
import com.example.androidapp.models.OrderItem;
import com.example.androidapp.views.activities.Order.OrderDetailActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class MyOrdersAdapter extends RecyclerView.Adapter<MyOrdersAdapter.ViewHolder> {

    private List<Order> orders;
    private Context context;
    // Thêm mảng trạng thái để lấy tên tiếng Việt
    private final String[] statusTitles;
    private final String[] statusValues;

    //constructor để lấy mảng trạng thái
    public MyOrdersAdapter(List<Order> orders, Context context) {
        this.orders = orders;
        this.context = context;
        // Lấy mảng từ resources
        this.statusTitles = context.getResources().getStringArray(R.array.order_status_titles);
        this.statusValues = context.getResources().getStringArray(R.array.order_status_values);
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
        if (order == null) return; // Kiểm tra null

        // --- Gán dữ liệu ---
        holder.tvTotal.setText(formatCurrency(order.getTotal()));
        // Hiển thị Status Text tiếng Việt
        holder.tvStatus.setText(getStatusTitle(order.getStatus()));
        // Đặt màu chữ cho status (Tùy chọn)
        setStatusTextColor(holder.tvStatus, order.getStatus());


        // Logic hiển thị sản phẩm và ảnh
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            OrderItem firstItem = order.getItems().get(0);
            holder.tvProductName.setText(firstItem.getName());


            if (firstItem.getVariantName() != null && !firstItem.getVariantName().isEmpty()) {
                holder.tvVariantName.setText("Phân loại: " + firstItem.getVariantName());
                holder.tvVariantName.setVisibility(View.VISIBLE);
            } else {
                holder.tvVariantName.setVisibility(View.GONE);
            }


            if (firstItem.getCachedImageUrl() != null && !firstItem.getCachedImageUrl().isEmpty()) {
                Glide.with(context)
                        .load(firstItem.getCachedImageUrl())
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .into(holder.ivProduct);
            } else {
                holder.ivProduct.setImageResource(R.drawable.ic_launcher_background);
            }
            int extraItems = order.getItems().size() - 1;
            if (extraItems > 0) {
                holder.tvExtraCount.setText("và " + extraItems + " sản phẩm khác");
                holder.tvExtraCount.setVisibility(View.VISIBLE);
            } else {
                holder.tvExtraCount.setVisibility(View.GONE);
            }
        } else {
            holder.tvProductName.setText("Không có sản phẩm");
            holder.ivProduct.setImageResource(R.drawable.ic_launcher_background);
            holder.tvExtraCount.setVisibility(View.GONE);
            holder.tvVariantName.setVisibility(View.GONE); // --- THÊM DÒNG NÀY ---
        }

        // --- HIỂN THỊ NÚT HỦY/CHỈ BÁO CHO USER ---
        String currentStatus = order.getStatus();
        boolean isCancelRequested = order.isCancellationRequested();
        boolean isFinalState = "delivered".equals(currentStatus) || "cancelled".equals(currentStatus);

        // Xác định xem user có thể hủy/yêu cầu hủy không
        boolean canCancelDirectly = "pending_confirmation".equals(currentStatus);
        boolean canRequestCancel = "confirmed".equals(currentStatus) || "shipping".equals(currentStatus);

        // Xử lý hiển thị
        if (isCancelRequested && !isFinalState) {
            // Trường hợp 1: ĐÃ yêu cầu hủy -> Hiện chỉ báo, ẩn nút
            holder.btnCancel.setVisibility(View.GONE);
            if (holder.tvCancelRequestedIndicator != null) { // Kiểm tra null
                holder.tvCancelRequestedIndicator.setVisibility(View.VISIBLE);
            }
        } else if ((canCancelDirectly || canRequestCancel) && !isFinalState) {
            // Trường hợp 2: Có thể hủy/yêu cầu VÀ CHƯA yêu cầu -> Hiện nút, ẩn chỉ báo
            holder.btnCancel.setVisibility(View.VISIBLE);
            holder.btnCancel.setText(canCancelDirectly ? "Hủy đơn" : "Yêu cầu hủy");
            if (holder.tvCancelRequestedIndicator != null) {
                holder.tvCancelRequestedIndicator.setVisibility(View.GONE);
            }

            // Gán listener cho nút Hủy/Yêu cầu
            holder.btnCancel.setOnClickListener(v -> {
                showUserCancelDialog(order, canCancelDirectly); //Truyền order vào
            });
        } else {
            // Trường hợp 3: Trạng thái cuối -> Ẩn cả hai
            holder.btnCancel.setVisibility(View.GONE);
            if (holder.tvCancelRequestedIndicator != null) {
                holder.tvCancelRequestedIndicator.setVisibility(View.GONE);
            }
        }



        // Sự kiện click vào item để xem chi tiết
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, OrderDetailActivity.class);
            intent.putExtra("order", order); // Gửi Parcelable
            context.startActivity(intent);
        });
    }

    // Dialog xác nhận cho User
    private void showUserCancelDialog(Order order, boolean isDirectCancel) {
        String title = isDirectCancel ? "Xác nhận hủy đơn" : "Yêu cầu hủy đơn";
        String message = isDirectCancel ? "Bạn có chắc chắn muốn hủy đơn hàng này?" : "Bạn muốn yêu cầu hủy đơn hàng này? Yêu cầu sẽ được gửi đến quản trị viên.";
        String positiveButtonText = isDirectCancel ? "Đồng ý" : "Gửi yêu cầu";

        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveButtonText, (dialog, which) -> {
                    if (isDirectCancel) {
                        cancelOrderDirectly(order); // Truyền order vào
                    } else {
                        requestCancellation(order); // Truyền order vào
                    }
                })
                .setNegativeButton("Không", null)
                .show();
    }

    // Hàm Hủy trực tiếp
    private void cancelOrderDirectly(Order order) {
        if (order == null || order.getOrderId() == null) return; // Thêm kiểm tra null
        FirebaseFirestore.getInstance().collection("orders").document(order.getOrderId())
                .update("status", "cancelled")
                .addOnSuccessListener(aVoid -> Toast.makeText(context, "Đã hủy đơn hàng.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(context, "Hủy đơn thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // Hàm Gửi yêu cầu hủy
    private void requestCancellation(Order order) {
        if (order == null || order.getOrderId() == null) return; // Thêm kiểm tra null
        FirebaseFirestore.getInstance().collection("orders").document(order.getOrderId())
                .update("cancellationRequested", true)
                .addOnSuccessListener(aVoid -> Toast.makeText(context, "Đã gửi yêu cầu hủy đơn.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(context, "Gửi yêu cầu thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return orders != null ? orders.size() : 0; // Thêm kiểm tra null
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct;
        TextView tvProductName, tvExtraCount, tvStatus, tvTotal;
        TextView tvVariantName;
        Button btnCancel;
        TextView tvCancelRequestedIndicator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.iv_item_product_image);
            tvProductName = itemView.findViewById(R.id.tv_item_product_name);
            tvVariantName = itemView.findViewById(R.id.tv_item_variant_name);
            tvExtraCount = itemView.findViewById(R.id.tv_item_extra_count);
            tvStatus = itemView.findViewById(R.id.tv_item_status_user);
            tvTotal = itemView.findViewById(R.id.tv_item_total_user);
            btnCancel = itemView.findViewById(R.id.btn_item_cancel_user);
            tvCancelRequestedIndicator = itemView.findViewById(R.id.tv_item_cancel_requested_indicator);
        }
    }


    // Hàm định dạng tiền tệ
    private String formatCurrency(double amount) {
        return NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(amount);
    }

    // Hàm tiện ích để lấy tên trạng thái Tiếng Việt
    private String getStatusTitle(String statusValue) {
        if (statusValue == null || statusValue.isEmpty()) return "N/A";
        for (int i = 0; i < statusValues.length; i++) {
            if (statusValue.equals(statusValues[i])) {
                if (i < statusTitles.length) return statusTitles[i];
                else break;
            }
        }
        Log.w("MyOrdersAdapter", "Status value not found: " + statusValue);
        return statusValue.toUpperCase();
    }

    // Hàm tiện ích để đặt màu chữ cho trạng thái
    private void setStatusTextColor(TextView tvStatus, String statusValue) {
        int colorRes;
        if (statusValue == null) statusValue = "";
        switch (statusValue) {
            case "pending_confirmation":
            case "confirmed":
                colorRes = R.color.purple_700; // Màu tím
                break;
            case "shipping":
                colorRes = R.color.primary_blue; // Màu xanh dương
                break;
            case "delivered":
                colorRes = android.R.color.holo_green_dark; // Màu xanh lá
                break;
            case "cancelled":
                colorRes = android.R.color.holo_red_dark; // Màu đỏ
                break;
            default:
                colorRes = android.R.color.darker_gray; // Màu xám
                break;
        }
        tvStatus.setTextColor(ContextCompat.getColor(context, colorRes));
    }
}