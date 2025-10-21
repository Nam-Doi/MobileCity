package com.example.androidapp.views.adapters;

import android.content.Context;
import android.content.Intent;
// SỬA: Không cần Parcelable
// import android.os.Parcelable;
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
import com.example.androidapp.models.ProductVariant; // SỬA: Import ProductVariant

// SỬA: Import Activity chi tiết của NGƯỜI DÙNG
import com.example.androidapp.views.activities.admin.DetailProductActivity;
// SỬA: Xóa import Activity chi tiết của ADMIN (nếu có)
// import com.example.androidapp.views.activities.admin.DetailProductActivity;

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
        // SỬA: Đảm bảo bạn dùng đúng layout item cho người dùng
        // (Bạn đang dùng 'admin_item_product', nếu sai hãy đổi thành 'item_product.xml' v.v..)
        View view = LayoutInflater.from(context).inflate(R.layout.admin_item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        if (product == null) return;

        holder.tvName.setText(product.getName());
        holder.tvBrand.setText(product.getBrand());

        // --- SỬA: LẤY GIÁ VÀ ẢNH TỪ VARIANT ---
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
        // --- KẾT THÚC SỬA GIÁ/ẢNH ---


        // --- SỬA: THÊM SỰ KIỆN CLICK ĐỂ MỞ CHI TIẾT SẢN PHẨM ---
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailProductActivity.class); // Mở màn hình chi tiết người dùng
            intent.putExtra("product", product); // Gửi product (đã implements Serializable)
            context.startActivity(intent);
        });
        // --- KẾT THÚC SỬA CLICK ---
    }


    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvName, tvBrand, tvPrice;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            // Các ID này phải khớp với file layout item của bạn (ví dụ: admin_item_product.xml)
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvBrand = itemView.findViewById(R.id.tvProductBrand);
            tvPrice = itemView.findViewById(R.id.tvProductPrice);
        }
    }
}