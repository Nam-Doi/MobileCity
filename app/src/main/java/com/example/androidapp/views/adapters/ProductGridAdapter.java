package com.example.androidapp.views.adapters;

import android.content.Context;
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
    private List<Product> productList;
    private SearchSuggestionAdapter.OnItemClickListener listener; // ✅ Khai báo Listener

    // ✅ CONSTRUCTOR MỚI: Nhận thêm OnItemClickListener
    public ProductGridAdapter(Context context, List<Product> productList,
            SearchSuggestionAdapter.OnItemClickListener listener) {
        this.context = context;
        this.productList = productList;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return productList == null ? 0 : productList.size();
    }

    @Override
    public Object getItem(int position) {
        if (productList == null || position < 0 || position >= productList.size())
            return null;
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

        Product product = null;
        if (productList != null && position >= 0 && position < productList.size()) {
            product = productList.get(position);
        }

        if (product == null) {
            android.util.Log.w("ProductGridAdapter", "product is null at position " + position);
            // set placeholders and return
            holder.tvName.setText("--");
            holder.tvBrand.setText("--");
            holder.tvPrice.setText("N/A");
            holder.imgProduct.setImageResource(R.drawable.ic_launcher_background);
            return convertView;
        }
        // Name and brand (null-safe)
        holder.tvName.setText(product.getName() != null ? product.getName() : "--");
        holder.tvBrand.setText(product.getBrand() != null ? product.getBrand() : "--");

        // Use variant-level price and images when available (product may store
        // variants)
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        List<ProductVariant> variants = product.getVariants();
        if (variants != null && !variants.isEmpty()) {
            ProductVariant defaultVariant = variants.get(0);
            try {
                holder.tvPrice.setText(currencyFormatter.format(defaultVariant.getPrice()));
            } catch (Exception e) {
                android.util.Log.w("ProductGridAdapter", "Invalid variant price", e);
                holder.tvPrice.setText("N/A");
            }

            if (defaultVariant.getImageUrls() != null && !defaultVariant.getImageUrls().isEmpty()) {
                Glide.with(context)
                        .load(defaultVariant.getImageUrls().get(0))
                        .into(holder.imgProduct);
            } else if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
                Glide.with(context)
                        .load(product.getImageUrls().get(0))
                        .into(holder.imgProduct);
            } else {
                holder.imgProduct.setImageResource(R.drawable.ic_launcher_background);
            }
        } else {
            // Fallback to product-level fields
            try {
                holder.tvPrice.setText(currencyFormatter.format(product.getPrice()));
            } catch (Exception e) {
                android.util.Log.w("ProductGridAdapter", "Invalid product price", e);
                holder.tvPrice.setText("N/A");
            }

            if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
                Glide.with(context)
                        .load(product.getImageUrls().get(0))
                        .into(holder.imgProduct);
            } else {
                holder.imgProduct.setImageResource(R.drawable.ic_launcher_background);
            }
        }
        // ✅ THAY THẾ LOGIC INTENT CŨ bằng việc gọi Listener
        final Product clickedProduct = product; // make effectively final for lambda
        convertView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(clickedProduct);
            }
        });

        return convertView;
    }

    static class ViewHolder {
        ImageView imgProduct;
        TextView tvName, tvBrand, tvPrice;
    }
}