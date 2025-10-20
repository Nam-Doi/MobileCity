package com.example.androidapp.views.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.androidapp.R;
import com.example.androidapp.models.Product;
import com.example.androidapp.views.activities.admin.DetailProductActivity;

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
        return productList.size();
    }

    @Override
    public Object getItem(int position) {
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

        Product product = productList.get(position);
        holder.tvName.setText(product.getName());
        holder.tvBrand.setText(product.getBrand());
        holder.tvPrice.setText(NumberFormat.getCurrencyInstance(new Locale("vi", "VN"))
                .format(product.getPrice()));

        if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
            Glide.with(context)
                    .load(product.getImageUrls().get(0))
                    .into(holder.imgProduct);
        }

        convertView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailProductActivity.class);
            intent.putExtra("phones", product);
            context.startActivity(intent);
        });

        return convertView;
    }

    static class ViewHolder {
        ImageView imgProduct;
        TextView tvName, tvBrand, tvPrice;
    }
}
