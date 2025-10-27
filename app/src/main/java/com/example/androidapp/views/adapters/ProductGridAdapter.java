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

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProductGridAdapter extends BaseAdapter {
    private Context context;
    private List<Product> productList;

    public ProductGridAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
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

        // Hiển thị thông tin sản phẩm---
        holder.tvName.setText(product.getName() != null ? product.getName() : "--");
        holder.tvBrand.setText(product.getBrand() != null ? product.getBrand() : "--");

        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        // Xử lý giá và ảnh sản phẩm
        if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
            Glide.with(context)
                    .load(product.getImageUrls().get(0))
                    .placeholder(R.drawable.ic_launcher_background)
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

        return convertView;
    }

    static class ViewHolder {
        ImageView imgProduct;
        TextView tvName, tvBrand, tvPrice;
    }

}