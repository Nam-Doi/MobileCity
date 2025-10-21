package com.example.androidapp.views.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.androidapp.R;
import com.example.androidapp.models.Product;
import com.example.androidapp.models.ProductVariant; // SỬA: Import thêm
import com.example.androidapp.views.activities.admin.DetailProductActivity;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ManageProductAdapter extends RecyclerView.Adapter<ManageProductAdapter.ProductViewHolder> {

    private Context context;
    private List<Product> productList;
    private OnProductActionListener listener;

    public interface OnProductActionListener {
        void onEdit(Product product);
        void onDelete(Product product);
    }

    public ManageProductAdapter(Context context, List<Product> productList, OnProductActionListener listener) {
        this.context = context;
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_manage_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.tvName.setText(product.getName());
        holder.tvBrand.setText(product.getBrand());

        // --- SỬA TỪ ĐÂY ---
        List<ProductVariant> variants = product.getVariants();

        if (variants != null && !variants.isEmpty()) {
            // Lấy phiên bản đầu tiên để hiển thị
            ProductVariant defaultVariant = variants.get(0);

            // Sửa: Lấy giá từ variant
            holder.tvPrice.setText(NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(defaultVariant.getPrice()));

            // Sửa: Lấy ảnh từ variant
            if (defaultVariant.getImageUrls() != null && !defaultVariant.getImageUrls().isEmpty()) {
                Glide.with(context).load(defaultVariant.getImageUrls().get(0)).into(holder.imgProduct);
            } else {
                // Xử lý nếu variant không có ảnh
                holder.imgProduct.setImageResource(R.drawable.ic_launcher_background); // (Bạn cần có 1 ảnh placeholder)
            }

        } else {
            // Xử lý nếu sản phẩm chưa có phiên bản nào
            holder.tvPrice.setText("Chưa có giá");
            holder.imgProduct.setImageResource(R.drawable.ic_launcher_background); // (Bạn cần có 1 ảnh placeholder)
        }
        // --- SỬA ĐẾN ĐÂY ---


        // Click vào ảnh để xem chi tiết
        holder.imgProduct.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailProductActivity.class);
            intent.putExtra("product", product); // product implements Serializable
            context.startActivity(intent);
        });

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(product));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(product));
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvName, tvBrand, tvPrice;
        Button btnEdit, btnDelete;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvBrand = itemView.findViewById(R.id.tvProductBrand);
            tvPrice = itemView.findViewById(R.id.tvProductPrice);
            btnEdit = itemView.findViewById(R.id.btnEditProduct);
            btnDelete = itemView.findViewById(R.id.btnDeleteProduct);
        }
    }
}