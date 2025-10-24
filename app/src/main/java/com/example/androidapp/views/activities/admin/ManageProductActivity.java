package com.example.androidapp.views.activities.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidapp.R;
import com.example.androidapp.models.Product;
import com.example.androidapp.views.adapters.ManageProductAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ManageProductActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ManageProductAdapter adapter;
    private List<Product> allProducts;
    private List<Product> filteredProducts;
    private FirebaseFirestore db;

    // 🔹 Đổi tên để rõ ràng hơn
    private FloatingActionButton fabAddProduct;
    private TextInputEditText etSearchProduct;
    private Spinner spinnerCategory; // 🔹 thay cho spinnerBrand
    private List<String> categoryList;

    @Override
    protected void onResume() {
        super.onResume();
        fetchProducts();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_product);

        recyclerView = findViewById(R.id.rvManageProducts);
        fabAddProduct = findViewById(R.id.fabAddProduct);
        etSearchProduct = findViewById(R.id.etSearchProduct);
        spinnerCategory = findViewById(R.id.spinnerCategory); // 🔹

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        allProducts = new ArrayList<>();
        filteredProducts = new ArrayList<>();

        adapter = new ManageProductAdapter(this, filteredProducts, new ManageProductAdapter.OnProductActionListener() {
            @Override
            public void onEdit(Product product) {
                Intent intent = new Intent(ManageProductActivity.this, EditProductActivity.class);
                intent.putExtra("product", product);
                startActivity(intent);
            }

            @Override
            public void onDelete(Product product) {
                if (product.getId() != null) {
                    db.collection("phones").document(product.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(ManageProductActivity.this, "Xóa thành công", Toast.LENGTH_SHORT).show();
                                fetchProducts();
                            })
                            .addOnFailureListener(e -> Toast.makeText(ManageProductActivity.this, "Lỗi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                } else {
                    Toast.makeText(ManageProductActivity.this, "Lỗi: sản phẩm chưa có ID", Toast.LENGTH_SHORT).show();
                }
            }
        });

        recyclerView.setAdapter(adapter);
        db = FirebaseFirestore.getInstance();

        fabAddProduct.setOnClickListener(v -> {
            startActivity(new Intent(ManageProductActivity.this, AddProductActivity.class));
        });

        etSearchProduct.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilter();
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                applyFilter();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                applyFilter();
            }
        });
    }

    // --- Lấy sản phẩm từ Firestore ---
    private void fetchProducts() {
        db.collection("phones").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allProducts.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            Product product = doc.toObject(Product.class);
                            if (product.getVariants() == null) continue;
                            product.setId(doc.getId());
                            allProducts.add(product);
                        } catch (Exception e) {
                            Log.e("FetchProductError", "Lỗi map dữ liệu: " + doc.getId(), e);
                        }
                    }

                    // --- 🔹 Setup spinner category ---
                    categoryList = new ArrayList<>();
                    categoryList.add("All"); // hiển thị tất cả

                    // 🔹 Nếu bạn muốn cố định 3 category cụ thể
                    categoryList.add("Điện thoại");
                    categoryList.add("Phụ kiện");
                    categoryList.add("Máy tính bảng");

                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryList);
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerCategory.setAdapter(spinnerAdapter);

                    applyFilter();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // --- 🔹 Filter theo tên + category ---
    private void applyFilter() {
        String query = etSearchProduct.getText() != null ? etSearchProduct.getText().toString().trim().toLowerCase() : "";
        String selectedCategory = spinnerCategory.getSelectedItem() != null ? spinnerCategory.getSelectedItem().toString() : "All";

        filteredProducts.clear();
        for (Product p : allProducts) {
            boolean matchesName = p.getName() != null && p.getName().toLowerCase().contains(query);
            boolean matchesCategory = selectedCategory.equals("All") ||
                    (p.getCategory() != null && p.getCategory().equalsIgnoreCase(selectedCategory));

            if (matchesName && matchesCategory) {
                filteredProducts.add(p);
            }
        }
        adapter.notifyDataSetChanged();
    }
}
