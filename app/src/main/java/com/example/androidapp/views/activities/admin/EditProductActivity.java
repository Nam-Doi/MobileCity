package com.example.androidapp.views.activities.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidapp.R;
import com.example.androidapp.models.Product;
import com.example.androidapp.models.ProductVariant;
import com.example.androidapp.views.adapters.ProductVariantAdapter;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EditProductActivity extends AppCompatActivity {

    private TextInputEditText editName, editBrand, editDescription;
    private Spinner spinnerCategory;
    private TextInputEditText editSpecManHinh, editSpecCPU, editSpecRamBoNho,
            editSpecCameraSau, editSpecCameraTruoc, editSpecPin, editSpecOS, editSpecKichThuoc;
    private RecyclerView recyclerVariants;
    private Button btnAddVariant, btnSaveProduct;

    private ProductVariantAdapter variantAdapter;
    private List<ProductVariant> variantsList;

    private FirebaseFirestore db;
    private Product editingProduct; // sản phẩm đang được sửa

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_product_activity);

        db = FirebaseFirestore.getInstance();
        initViews();
        setupCategorySpinner();
        setupRecyclerView();

        // 🔹 Nhận dữ liệu sản phẩm từ Intent (nếu đang sửa)
        Intent intent = getIntent();
        editingProduct = (Product) intent.getSerializableExtra("product");

        if (editingProduct != null) {
            loadOldData(editingProduct);
        } else {
            addVariant(); // Nếu thêm mới
        }

        btnAddVariant.setOnClickListener(v -> addVariant());
        btnSaveProduct.setOnClickListener(v -> saveProduct());
    }

    private void initViews() {
        editName = findViewById(R.id.editProductName);
        editBrand = findViewById(R.id.editProductBrand);
        editDescription = findViewById(R.id.editProductDescription);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        editSpecManHinh = findViewById(R.id.editSpecManHinh);
        editSpecCPU = findViewById(R.id.editSpecCPU);
        editSpecRamBoNho = findViewById(R.id.editSpecRamBoNho);
        editSpecCameraSau = findViewById(R.id.editSpecCameraSau);
        editSpecCameraTruoc = findViewById(R.id.editSpecCameraTruoc);
        editSpecPin = findViewById(R.id.editSpecPin);
        editSpecOS = findViewById(R.id.editSpecOS);
        editSpecKichThuoc = findViewById(R.id.editSpecKichThuoc);
        recyclerVariants = findViewById(R.id.recyclerVariants);
        btnAddVariant = findViewById(R.id.btnAddVariant);
        btnSaveProduct = findViewById(R.id.btnSaveProduct);
    }

    private void setupCategorySpinner() {
        String[] categories = new String[]{"Điện thoại", "Phụ kiện", "Máy tính bảng"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void setupRecyclerView() {
        variantsList = new ArrayList<>();
        variantAdapter = new ProductVariantAdapter(this, variantsList);
        recyclerVariants.setLayoutManager(new LinearLayoutManager(this));
        recyclerVariants.setAdapter(variantAdapter);
    }

    // 🔹 Nạp dữ liệu cũ khi chỉnh sửa
    private void loadOldData(Product product) {
        editName.setText(product.getName());
        editBrand.setText(product.getBrand());
        editDescription.setText(product.getDescription());

        // Gán category
        String[] categories = new String[]{"Điện thoại", "Phụ kiện", "Máy tính bảng"};
        for (int i = 0; i < categories.length; i++) {
            if (categories[i].equalsIgnoreCase(product.getCategory())) {
                spinnerCategory.setSelection(i);
                break;
            }
        }

        // Thông số kỹ thuật
        if (product.getSpecifications() != null) {
            Map<String, String> spec = product.getSpecifications();
            editSpecManHinh.setText(spec.getOrDefault("Màn hình", ""));
            editSpecCPU.setText(spec.getOrDefault("CPU/GPU", ""));
            editSpecRamBoNho.setText(spec.getOrDefault("RAM/Bộ nhớ", ""));
            editSpecCameraSau.setText(spec.getOrDefault("Camera sau", ""));
            editSpecCameraTruoc.setText(spec.getOrDefault("Camera trước", ""));
            editSpecPin.setText(spec.getOrDefault("Pin/Sạc", ""));
            editSpecOS.setText(spec.getOrDefault("Hệ điều hành", ""));
            editSpecKichThuoc.setText(spec.getOrDefault("Kích thước", ""));
        }

        // Danh sách biến thể
        if (product.getVariants() != null && !product.getVariants().isEmpty()) {
            variantsList.clear();
            variantsList.addAll(product.getVariants());
            variantAdapter.notifyDataSetChanged();
        }
    }

    private void addVariant() {
        variantsList.add(new ProductVariant());
        variantAdapter.notifyItemInserted(variantsList.size() - 1);
    }

    private void saveProduct() {
        String name = editName.getText().toString().trim();
        String brand = editBrand.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();
        String description = editDescription.getText().toString().trim();

        Map<String, String> specs = new HashMap<>();
        specs.put("Màn hình", editSpecManHinh.getText().toString().trim());
        specs.put("CPU/GPU", editSpecCPU.getText().toString().trim());
        specs.put("RAM/Bộ nhớ", editSpecRamBoNho.getText().toString().trim());
        specs.put("Camera sau", editSpecCameraSau.getText().toString().trim());
        specs.put("Camera trước", editSpecCameraTruoc.getText().toString().trim());
        specs.put("Pin/Sạc", editSpecPin.getText().toString().trim());
        specs.put("Hệ điều hành", editSpecOS.getText().toString().trim());
        specs.put("Kích thước", editSpecKichThuoc.getText().toString().trim());

        if (name.isEmpty() || brand.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên và thương hiệu", Toast.LENGTH_SHORT).show();
            return;
        }

        if (variantsList.isEmpty()) {
            Toast.makeText(this, "Phải có ít nhất 1 biến thể", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔹 Nếu đang sửa, dùng ID cũ. Nếu thêm mới, tạo ID mới.
        String productId = (editingProduct != null) ? editingProduct.getId() : UUID.randomUUID().toString();

        Product product = new Product(
                productId,
                name,
                brand,
                category,
                description,
                specs,
                variantsList
        );

        // 🔹 Lưu vào đúng collection "phones"
        db.collection("phones").document(productId)
                .set(product)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditProductActivity.this, "Đã lưu sản phẩm!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditProductActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
