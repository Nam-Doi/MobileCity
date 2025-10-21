package com.example.androidapp.views.adapters.cartAdt;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.androidapp.R;
import com.example.androidapp.models.CartItem;

import java.util.List;

public class CheckoutAdapter extends RecyclerView.Adapter<CheckoutAdapter.CheckoutViewHolder> {
    private List<CartItem> itemList;
    public CheckoutAdapter(List<CartItem> itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public CheckoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.checkout_items, parent, false);
        return new CheckoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CheckoutAdapter.CheckoutViewHolder holder, int position) {
        CartItem item = itemList.get(position);
        
        // Load ảnh sản phẩm
        if (item.getCachedImageUrl() != null && !item.getCachedImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(item.getCachedImageUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(holder.imgProduct);
        }
        
        holder.tvName.setText(item.getCachedName());
        
        // Giá hiện tại (không có giá gốc trong CartItem)
        holder.tvPrice.setText(String.format("%,.0fđ", item.getCachedPrice()));
        
        // Ẩn giá gốc vì CartItem không có thông tin này
        holder.tvPriceOriginal.setVisibility(View.GONE);

        // Số lượng
        holder.tvQuantity.setText("x" + item.getQuantity());

        // Tổng tiền cho item này
        double itemTotal = item.getTotalPrice();
        holder.tvItemCount.setText(String.format("Tổng số tiền (%d sản phẩm):", item.getQuantity()));
        holder.tvItemTotal.setText(String.format("%,.0fđ", itemTotal));
    }

    @Override
    public int getItemCount() {
        return  itemList != null ? itemList.size(): 0;
    }
    public static class CheckoutViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvName, tvPriceOriginal, tvPrice, tvQuantity,tvItemCount, tvItemTotal;
        public CheckoutViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvName= itemView.findViewById(R.id.tvName);
            tvPriceOriginal = itemView.findViewById(R.id.tvPriceOriginal);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvItemCount = itemView.findViewById(R.id.tvItemCount);
            tvItemTotal = itemView.findViewById(R.id.tvItemTotal);
        }



    }

}
