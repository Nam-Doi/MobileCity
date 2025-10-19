package com.example.androidapp.views.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
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
import com.example.androidapp.views.activities.admin.DetailProductActivity;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private Context context;
    private List<Product> productList;

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.admin_item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.tvName.setText(product.getName());
        holder.tvBrand.setText(product.getBrand());
        holder.tvPrice.setText(NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(product.getPrice()));

        if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
            Glide.with(context).load(product.getImageUrls().get(0)).into(holder.imgProduct);
        }

        // Khi click vào ảnh mở DetailActivity
        holder.imgProduct.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailProductActivity.class);
            intent.putExtra("phones", (Parcelable) product); // Product cần implements Serializable hoặc Parcelable
            context.startActivity(intent);
        });
    }


    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvName, tvBrand, tvPrice;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvBrand = itemView.findViewById(R.id.tvProductBrand);
            tvPrice = itemView.findViewById(R.id.tvProductPrice);
        }
    }
}


