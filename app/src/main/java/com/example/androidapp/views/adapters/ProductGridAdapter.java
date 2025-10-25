package com.example.androidapp.views.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.androidapp.R;
import com.example.androidapp.models.Product;
import com.example.androidapp.models.ProductVariant;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProductGridAdapter extends BaseAdapter {
    private Context context;
    private List<Product> productList; // Danh sách sản phẩm adapter đang dùng

    // CONSTRUCTOR ĐƠN GIẢN HƠN: Chỉ cần Context và List
    public ProductGridAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList; // Lưu tham chiếu đến list từ HomeFragment
    }

    @Override
    public int getCount() {
        return productList == null ? 0 : productList.size();
    }

    @Override
    public Object getItem(int position) {
        // Sửa lại kiểm tra một chút cho an toàn
        if (productList == null || position < 0 || position >= productList.size()) {
            return null;
        }
        return productList.get(position);
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_product_grid, parent, false);
            holder = new ViewHolder();
            holder.imgProduct = convertView.findViewById(R.id.imgProduct);
            holder.tvName = convertView.findViewById(R.id.tvProductName);
            holder.tvBrand = convertView.findViewById(R.id.tvProductBrand);
            holder.tvPrice = convertView.findViewById(R.id.tvProductPrice);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Lấy sản phẩm một cách an toàn hơn
        Product product = (Product) getItem(position);

        if (product == null) {
            Log.w("ProductGridAdapter", "Product is null at position " + position);
            // Có thể đặt ảnh placeholder hoặc ẩn view nếu cần
            holder.tvName.setText("N/A");
            holder.tvBrand.setText("");
            holder.tvPrice.setText("");
            holder.imgProduct.setImageResource(R.drawable.ic_launcher_background); // Placeholder
            return convertView;
        }

        // --- Code hiển thị thông tin sản phẩm (Giữ nguyên như cũ) ---
        holder.tvName.setText(product.getName() != null ? product.getName() : "--");
        holder.tvBrand.setText(product.getBrand() != null ? product.getBrand() : "--");

        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        // ... (Code xử lý giá và ảnh từ variant hoặc product giữ nguyên) ...
        // Ví dụ đơn giản hóa:
        if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
            Glide.with(context)
                    .load(product.getImageUrls().get(0))
                    .placeholder(R.drawable.ic_launcher_background) // Thêm placeholder
                    .error(R.drawable.ic_launcher_background) // Thêm ảnh lỗi
                    .into(holder.imgProduct);
        } else {
            holder.imgProduct.setImageResource(R.drawable.ic_launcher_background);
        }
        try {
            holder.tvPrice.setText(currencyFormatter.format(product.getPrice()));
        } catch (Exception e) {
            holder.tvPrice.setText("N/A");
        }
        // -----------------------------------------------------------

        // == XÓA BỎ setOnClickListener ở đây ==
        // Việc xử lý click sẽ do GridView trong HomeFragment đảm nhiệm

        return convertView;
    }

    static class ViewHolder {
        ImageView imgProduct;
        TextView tvName, tvBrand, tvPrice;
    }

    /**
     * Hàm cập nhật dữ liệu (Giữ lại vì nó vẫn hữu ích)
     * HomeFragment sẽ KHÔNG cần gọi hàm này nếu nó trực tiếp sửa list và gọi notifyDataSetChanged()
     */
    public void updateData(List<Product> newProductList) {
        // Chỉ cần gán lại tham chiếu list nếu HomeFragment quản lý list này
        this.productList = newProductList;
        notifyDataSetChanged();

        // Hoặc nếu muốn adapter tự quản lý bản copy:
        // if (newProductList != null) {
        //    this.productList.clear();
        //    this.productList.addAll(newProductList);
        //    notifyDataSetChanged();
        // }
    }
}