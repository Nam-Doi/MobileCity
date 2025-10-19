package com.example.androidapp.views.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.androidapp.R;

public class detail_product_activity extends AppCompatActivity {
    private ImageView productImage;
    private TextView productName, productPrice, productDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_activity);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // hiển thị nút back
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Ánh xạ view
        productImage = findViewById(R.id.product_image);
        productName = findViewById(R.id.product_name);
        productPrice = findViewById(R.id.product_price);
        productDescription = findViewById(R.id.product_description);

        // Lấy dữ liệu từ Intent
        String name = getIntent().getStringExtra("name");
        String price = getIntent().getStringExtra("price");
        String description = getIntent().getStringExtra("description");
        int imageResId = getIntent().getIntExtra("image", R.drawable.ic_launcher_background);

        // Set dữ liệu
        productName.setText(name);
        productPrice.setText("Giá: " + price);
        productDescription.setText(description);
        productImage.setImageResource(imageResId);
    }

    // Xử lý nút back trên toolbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // quay về Activity trước
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
