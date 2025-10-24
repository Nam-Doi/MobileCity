package com.example.androidapp.views.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.androidapp.R;
import com.example.androidapp.models.OrderItem;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class OrderItemDetailAdapter extends RecyclerView.Adapter<OrderItemDetailAdapter.ViewHolder> {

    private List<OrderItem> items;
    private Context context;


    public OrderItemDetailAdapter(List<OrderItem> items, Context context) {
        this.items = items;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_detail_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderItem item = items.get(position);

        holder.tvName.setText(item.getName());
        holder.tvPrice.setText(formatCurrency(item.getPrice()));
        holder.tvQuantity.setText("x" + item.getQty());

        // --- TẢI ẢNH BẰNG GLIDE ---
        if (item.getCachedImageUrl() != null && !item.getCachedImageUrl().isEmpty()) {
            Glide.with(context) // Sử dụng context đã lưu
                    .load(item.getCachedImageUrl()) // Dùng đúng tên hàm
                    .placeholder(R.drawable.ic_launcher_background) // Ảnh chờ
                    .error(R.drawable.ic_launcher_background) // Ảnh lỗi
                    .into(holder.imgProduct); // ImageView đích
        } else {
            holder.imgProduct.setImageResource(R.drawable.ic_launcher_background); // Ảnh mặc định
        }

    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvName, tvPrice, tvQuantity;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
        }
    }

    private String formatCurrency(double amount) {
        return NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(amount);
    }
}