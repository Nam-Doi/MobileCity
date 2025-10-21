package com.example.androidapp.views.activities.admin;

// Xóa import liên quan đến ảnh và storage
// import android.app.ProgressDialog;
// import android.net.Uri;
// import androidx.activity.result.ActivityResultLauncher;
// import androidx.activity.result.PickVisualMediaRequest;
// import androidx.activity.result.contract.ActivityResultContracts;
// import com.google.firebase.storage.FirebaseStorage;
// import com.google.firebase.storage.StorageReference;
import android.os.Bundle;
import android.util.Log; // Giữ lại Log nếu cần
import android.webkit.URLUtil; // Thêm để kiểm tra URL
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
import com.example.androidapp.views.adapters.ProductVariantAdapter; // Đảm bảo adapter này đã được sửa
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// XÓA implements ProductVariantAdapter.OnImageSelectListener
public class AddProductActivity extends AppCompatActivity /* implements ProductVariantAdapter.OnImageSelectListener */ {

    // Views cho thông tin chung
    private TextInputEditText editName, editBrand, editDescription;
    private Spinner spinnerCategory;
    private TextInputEditText editSpecManHinh, editSpecCPU, editSpecRamBoNho,
            editSpecCameraSau, editSpecCameraTruoc, editSpecPin, editSpecOS, editSpecKichThuoc;
    private RecyclerView recyclerVariants;
    private Button btnAddVariant;
    private Button btnSaveProduct;

    private ProductVariantAdapter variantAdapter;
    private List<ProductVariant> variantsList;

    private FirebaseFirestore db;

    // XÓA CÁC BIẾN XỬ LÝ ẢNH
    // private StorageReference storageReference;
    // private ActivityResultLauncher<PickVisualMediaRequest> pickImagesLauncher;
    // private int currentVariantPosition = -1;
    // private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_product_activity);

        db = FirebaseFirestore.getInstance();
        // storageReference = FirebaseStorage.getInstance().getReference("product_images"); // Xóa

        initViews();
        setupCategorySpinner();
        setupRecyclerView();
        // setupImagePicker(); // Xóa

        btnAddVariant.setOnClickListener(v -> addVariant());
        btnSaveProduct.setOnClickListener(v -> saveProduct());
    }

    // XÓA CÁC HÀM setupImagePicker(), uploadImages(), onImageSelect()

    // SỬA HÀM setupRecyclerView()
    private void setupRecyclerView() {
        variantsList = new ArrayList<>();
        // Xóa listener "this" khi tạo adapter
        // Đảm bảo ProductVariantAdapter đã được sửa để không cần listener
        variantAdapter = new ProductVariantAdapter(this, variantsList /*, this*/);
        recyclerVariants.setLayoutManager(new LinearLayoutManager(this));
        recyclerVariants.setAdapter(variantAdapter);

        addVariant();
    }

    // Hàm saveProduct (Đã sửa Validate ảnh URL)
    private void saveProduct() {
        // --- 1. LẤY THÔNG TIN CHUNG ---
        String name = editName.getText().toString().trim();
        String brand = editBrand.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();
        String description = editDescription.getText().toString().trim();

        Map<String, String> specifications = new HashMap<>();
        specifications.put("Màn hình", editSpecManHinh.getText().toString().trim());
        specifications.put("CPU/GPU", editSpecCPU.getText().toString().trim());
        specifications.put("RAM/Bộ nhớ", editSpecRamBoNho.getText().toString().trim());
        specifications.put("Camera sau", editSpecCameraSau.getText().toString().trim());
        specifications.put("Camera trước", editSpecCameraTruoc.getText().toString().trim());
        specifications.put("Pin/Sạc", editSpecPin.getText().toString().trim());
        specifications.put("Hệ điều hành", editSpecOS.getText().toString().trim());
        specifications.put("Kích thước", editSpecKichThuoc.getText().toString().trim());

        // --- 2. VALIDATE THÔNG TIN CHUNG ---
        if (name.isEmpty() || brand.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập Tên và Thương hiệu", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- 4. VALIDATE THÔNG TIN PHIÊN BẢN ---
        if (variantsList.isEmpty()) {
            Toast.makeText(this, "Phải có ít nhất 1 phiên bản sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean allVariantsValid = true;
        for (ProductVariant variant : variantsList) {
            // Validate thông tin
            if (variant.getColor() == null || variant.getColor().isEmpty() || variant.getPrice() <= 0 || variant.getStock() < 0) {
                Toast.makeText(this, "Vui lòng điền đủ thông tin (Màu, Giá, Tồn kho)", Toast.LENGTH_SHORT).show();
                allVariantsValid = false;
                break;
            }
            // SỬA: Validate URL ảnh
            if (variant.getImageUrls() == null || variant.getImageUrls().isEmpty() || variant.getImageUrls().get(0).isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập URL hình ảnh cho tất cả phiên bản", Toast.LENGTH_SHORT).show();
                allVariantsValid = false;
                break;
            }
            // (Tùy chọn) Kiểm tra định dạng URL
            if (!URLUtil.isValidUrl(variant.getImageUrls().get(0))) {
                Toast.makeText(this, "URL hình ảnh không hợp lệ: " + variant.getImageUrls().get(0), Toast.LENGTH_SHORT).show();
                allVariantsValid = false;
                break;
            }

            // Tạo ID
            if (variant.getId() == null || variant.getId().isEmpty()) {
                variant.setId(UUID.randomUUID().toString());
            }
        }

        if (!allVariantsValid) {
            return; // Dừng nếu lỗi
        }

        // --- 5. TẠO OBJECT PRODUCT HOÀN CHỈNH ---
        String productId = UUID.randomUUID().toString();
        Product product = new Product(
                productId,
                name,
                brand,
                category,
                description,
                specifications,
                variantsList
        );

        // --- 6. LƯU LÊN DATABASE (Firestore) ---
        Toast.makeText(this, "Đang lưu sản phẩm...", Toast.LENGTH_SHORT).show();

        db.collection("phones").document(productId)
                .set(product)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AddProductActivity.this, "Thêm sản phẩm thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddProductActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // (Hàm initViews giữ nguyên)
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

    // (Hàm addVariant giữ nguyên)
    private void addVariant() {
        variantsList.add(new ProductVariant());
        variantAdapter.notifyItemInserted(variantsList.size() - 1);
    }

    // (Hàm setupCategorySpinner giữ nguyên)
    private void setupCategorySpinner() {
        String[] categories = new String[]{"Điện thoại", "Phụ kiện", "Máy tính bảng"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);
    }
}