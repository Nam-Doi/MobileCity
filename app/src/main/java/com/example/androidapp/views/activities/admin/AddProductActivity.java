package com.example.androidapp.views.activities.admin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.androidapp.R;
import com.example.androidapp.models.Product;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddProductActivity extends AppCompatActivity {

    private EditText etName, etBrand, etPrice, etStock, etImageUrl;
    private Button btnChooseImage, btnSave;
    private Uri selectedImageUri;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_product_activity);

        etName = findViewById(R.id.etName);
        etBrand = findViewById(R.id.etBrand);
        etPrice = findViewById(R.id.etPrice);
        etStock = findViewById(R.id.etStock);
        etImageUrl = findViewById(R.id.etImageUrl);

        btnChooseImage = findViewById(R.id.btnChooseImage);
        btnSave = findViewById(R.id.btnSaveProduct);

        db = FirebaseFirestore.getInstance();

        // Chọn ảnh từ thư viện
        ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        etImageUrl.setText(uri.toString()); // Hiển thị URI ảnh
                    }
                });

        btnChooseImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        btnSave.setOnClickListener(v -> saveProduct());
    }

    private void saveProduct() {
        String name = etName.getText().toString().trim();
        String brand = etBrand.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String stockStr = etStock.getText().toString().trim();
        String imageUrl = etImageUrl.getText().toString().trim();

        if(name.isEmpty() || brand.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty() || imageUrl.isEmpty()){
            Toast.makeText(this,"Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        double price = Double.parseDouble(priceStr);
        int stock = Integer.parseInt(stockStr);

        List<String> images = new ArrayList<>();
        images.add(imageUrl);

        Map<String,String> specs = new HashMap<>();
        specs.put("CPU/GPU",""); // Bạn có thể add EditText cho specs nếu muốn
        specs.put("Camera sau","");
        specs.put("Camera trước","");
        specs.put("Hệ điều hành","");
        specs.put("Kích thước","");
        specs.put("Màn hình","");
        specs.put("Pin/Sạc","");
        specs.put("RAM/Bộ nhớ","");

        Product product = new Product(null, name, brand, price, images, stock, specs);

        db.collection("phones").add(product)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this,"Thêm sản phẩm thành công",Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this,"Lỗi thêm sản phẩm: "+e.getMessage(),Toast.LENGTH_SHORT).show());
    }
}
