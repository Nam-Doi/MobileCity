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
import com.example.androidapp.models.ProductVariant;
import com.example.androidapp.views.activities.Product.DetailProductActivity;
import com.example.androidapp.views.activities.admin.DetailProductActivitys;

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
        if (product == null) return;
        holder.tvName.setText(product.getName());
        holder.tvBrand.setText(product.getBrand());
        List<ProductVariant> variants = product.getVariants();
        if (variants != null && !variants.isEmpty()) {
            // Lấy variant đầu tiên làm mặc định
            ProductVariant defaultVariant = variants.get(0);

            // Lấy giá
            holder.tvPrice.setText(NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(defaultVariant.getPrice()));

            // Lấy ảnh
            if (defaultVariant.getImageUrls() != null && !defaultVariant.getImageUrls().isEmpty()) {
                Glide.with(context).load(defaultVariant.getImageUrls().get(0)).into(holder.imgProduct);
            } else {
                holder.imgProduct.setImageResource(R.drawable.ic_launcher_background); // Cần có ảnh placeholder
            }
        } else {
            // Xử lý nếu sản phẩm không có variant
            holder.tvPrice.setText("Chưa có giá");
            holder.imgProduct.setImageResource(R.drawable.ic_launcher_background); // Cần có ảnh placeholder
        }



        // Click vào ảnh để xem chi tiết
        // --- SỬA: THÊM SỰ KIỆN CLICK ĐỂ MỞ CHI TIẾT SẢN PHẨM ---
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailProductActivitys.class); // Mở màn hình chi tiết người dùng
            intent.putExtra("product", product); // Gửi product (đã implements Serializable)
            context.startActivity(intent);
        });

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(product));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(product));
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder{
        ImageView imgProduct;
        TextView tvName, tvBrand, tvPrice;
        Button btnEdit, btnDelete;

        public ProductViewHolder(@NonNull View itemView){
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
