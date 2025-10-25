package com.example.androidapp.views.adapters;

import android.content.Context;
import android.content.Intent;
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
import com.example.androidapp.views.activities.Product.DetailProductActivity;
import com.example.androidapp.views.activities.admin.DetailProductActivitys;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ViewProductAdapter extends RecyclerView.Adapter<ViewProductAdapter.ProductViewHolder> {

    private final Context context;
    private final List<Product> productList;

    public ViewProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        if (product == null) return;

        holder.tvName.setText(product.getName());
        holder.tvBrand.setText(product.getBrand());

        // Định dạng giá tiền: 300.000.000₫
        double priceValue = product.getPrice();
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        holder.tvPrice.setText(nf.format(priceValue) + "₫");

        // Load ảnh sản phẩm
        Object imageData = product.getImageUrls();
        if (imageData instanceof List) {
            List<?> list = (List<?>) imageData;
            if (!list.isEmpty()) {
                Glide.with(context)
                        .load(list.get(0))
                        .placeholder(R.drawable.ic_launcher_background)
                        .into(holder.ivProduct);
            } else {
                holder.ivProduct.setImageResource(R.drawable.ic_launcher_background);
            }
        } else if (imageData instanceof String) {
            Glide.with(context)
                    .load((String) imageData)
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(holder.ivProduct);
        } else {
            holder.ivProduct.setImageResource(R.drawable.ic_launcher_background);
        }

        // 🟢 SỰ KIỆN CLICK - MỞ CHI TIẾT SẢN PHẨM
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailProductActivitys.class);
            intent.putExtra("product", product);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct;
        TextView tvName, tvBrand, tvPrice;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.iv_product);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvBrand = itemView.findViewById(R.id.tvProductBrand);
            tvPrice = itemView.findViewById(R.id.tvProductPrice);
        }
    }
}
