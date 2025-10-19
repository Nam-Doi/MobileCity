package com.example.androidapp.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.androidapp.R;
import com.example.androidapp.models.Product;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class SearchSuggestionAdapter extends RecyclerView.Adapter<SearchSuggestionAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Product product);
    }

    private List<Product> suggestionList;
    private OnItemClickListener listener;

    public SearchSuggestionAdapter(List<Product> suggestionList, OnItemClickListener listener) {
        this.suggestionList = suggestionList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.suggest_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = suggestionList.get(position);
        if (product == null) return;

        // --- CÁC THAY ĐỔI NẰM Ở ĐÂY ---

        // 1. Hiển thị tên sản phẩm
        holder.tvName.setText(product.getName());

        // 2. Xử lý Giá tiền (từ Long sang String tiền tệ)
        if (product.getPrice() != null) {
            NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            holder.tvPrice.setText(currencyFormatter.format(product.getPrice()));
        } else {
            holder.tvPrice.setText("N/A"); // Hoặc giá trị mặc định
        }

        // 3. Xử lý Hình ảnh (lấy ảnh đầu tiên từ mảng imageUrls)
        List<String> imageUrls = product.getImageUrls();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrls.get(0)) // Lấy ảnh đầu tiên
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(holder.imgProduct);
        } else {
            // Nếu không có ảnh, hiển thị ảnh mặc định
            holder.imgProduct.setImageResource(R.drawable.ic_launcher_background);
        }

        // Gán sự kiện click
        holder.itemView.setOnClickListener(v -> listener.onItemClick(product));
    }

    @Override
    public int getItemCount() {
        return suggestionList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvName, tvPrice;

        ViewHolder(View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }
    }
}
