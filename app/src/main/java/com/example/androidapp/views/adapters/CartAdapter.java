package com.example.androidapp.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidapp.R;
import com.example.androidapp.models.CartItem;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private List<CartItem> cartList;
    private OnCartChangeListener listener;

    public interface OnCartChangeListener { // implement ben cartActivity
        void onCartUpdated();
    }

    public CartAdapter(List<CartItem> cartList, OnCartChangeListener listener) {
        this.cartList = cartList;
        this.listener = listener;
    }
    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cart_items, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartAdapter.CartViewHolder holder, int position) {
        CartItem item = cartList.get(position);
        holder.imgProduct.setImageResource(item.getImageResId());
        holder.tvName.setText(item.getName());
        holder.tvPrice.setText(item.getPrice() + " $");
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));
        holder.tvTotalItem.setText(item.getTotalPrice() + " $");

        holder.tvMinus.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                item.setQuantity(item.getQuantity() - 1);
                notifyItemChanged(position);
                listener.onCartUpdated();
            }
        });

        holder.tvPlus.setOnClickListener(v -> {
            item.setQuantity(item.getQuantity() + 1);
            notifyItemChanged(position);
            listener.onCartUpdated();
        });
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }
    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvName, tvPrice, tvQuantity, tvTotalItem, tvMinus, tvPlus;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvTotalItem = itemView.findViewById(R.id.tvTotalItem);
            tvMinus = itemView.findViewById(R.id.tvMinus);
            tvPlus = itemView.findViewById(R.id.tvPlus);
        }
    }
}
