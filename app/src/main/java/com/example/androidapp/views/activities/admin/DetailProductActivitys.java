package com.example.androidapp.views.activities.admin;

import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.androidapp.R;
import com.example.androidapp.models.Product;

import java.util.Map;

public class DetailProductActivitys extends AppCompatActivity {

    private ImageView imgProduct;
    private TextView tvName, tvBrand, tvPrice, tvStock;
    private LinearLayout llSpecifications;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_product);

        Product product = (Product) getIntent().getSerializableExtra("product");

        imgProduct = findViewById(R.id.imgProductDetail);
        tvName = findViewById(R.id.tvProductNameDetail);
        tvBrand = findViewById(R.id.tvProductBrandDetail);
        tvPrice = findViewById(R.id.tvProductPriceDetail);
        tvStock = findViewById(R.id.tvProductStockDetail);
        llSpecifications = findViewById(R.id.llSpecifications);

        if (product != null) {
            tvName.setText(product.getName());
            tvBrand.setText(product.getBrand());
            tvPrice.setText(String.format("%,.0f ₫", product.getPrice()));
            tvStock.setText("Còn: " + product.getStock());

            if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
                Glide.with(this).load(product.getImageUrls().get(0)).into(imgProduct);
            }

            // Hiển thị thông số
            Map<String, String> specs = product.getSpecifications();
            if (specs != null) {
                for (Map.Entry<String, String> entry : specs.entrySet()) {
                    LinearLayout row = new LinearLayout(this);
                    row.setOrientation(LinearLayout.HORIZONTAL);

                    TextView key = new TextView(this);
                    key.setText(entry.getKey() + ": ");
                    key.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
                    key.setTextSize(Typeface.BOLD);

                    TextView value = new TextView(this);
                    value.setText(entry.getValue());
                    value.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2));

                    row.addView(key);
                    row.addView(value);
                    llSpecifications.addView(row);
                }
            }
        }
    }
}
