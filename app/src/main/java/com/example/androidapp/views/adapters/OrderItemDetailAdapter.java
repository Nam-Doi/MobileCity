package com.example.androidapp.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.androidapp.R;
import com.example.androidapp.models.OrderItem;
import com.bumptech.glide.Glide; // Bạn có thể cần thư viện này để tải ảnh sản phẩm
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class OrderItemDetailAdapter extends RecyclerView.Adapter<OrderItemDetailAdapter.ViewHolder> {

    private List<OrderItem> items;

    public OrderItemDetailAdapter(List<OrderItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Gắn layout "item_order_detail_item.xml"
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

        // TODO: Tải ảnh sản phẩm thật
        // Hiện tại, chúng ta chưa có link ảnh trong OrderItem
        // holder.imgProduct.setImageResource(R.drawable.iphone_17); // Ảnh mẫu

        /* // Khi bạn nâng cấp OrderItem để có ảnh:
        Glide.with(holder.itemView.getContext())
                .load(item.getImageUrl()) // Giả sử item có getImageUrl()
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.imgProduct);
        */
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