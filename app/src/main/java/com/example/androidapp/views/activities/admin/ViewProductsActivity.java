package com.example.androidapp.views.activities.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidapp.R;
import com.example.androidapp.models.Product;
import com.example.androidapp.views.adapters.ViewProductAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ViewProductsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Spinner spinnerCategory, spinnerBrand;
    private TextView tvBrandLabel;
    private Button btnResetFilter;

    private ViewProductAdapter adapter;
    private final List<Product> allProducts = new ArrayList<>();
    private final List<Product> filteredProducts = new ArrayList<>();

    private FirebaseFirestore db;
    private ArrayAdapter<String> categoryAdapter, brandAdapter;

    private String selectedCategory = null;
    private String selectedBrand = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_views_product);

        // Ánh xạ
        recyclerView = findViewById(R.id.rvViewProducts);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerBrand = findViewById(R.id.spinnerBrand);
        tvBrandLabel = findViewById(R.id.tvBrandLabel);
        btnResetFilter = findViewById(R.id.btnResetFilter);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new ViewProductAdapter(this, filteredProducts);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        fetchProducts();

        // Xử lý chọn Category
        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String category = spinnerCategory.getSelectedItem().toString();
                if (!category.equals("Tất cả")) {
                    selectedCategory = category;
                    showBrandFilter(category);
                } else {
                    selectedCategory = null;
                    tvBrandLabel.setVisibility(View.GONE);
                    spinnerBrand.setVisibility(View.GONE);
                }
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Xử lý chọn Brand
        spinnerBrand.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String brand = spinnerBrand.getSelectedItem().toString();
                selectedBrand = brand.equals("Tất cả") ? null : brand;
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Nút reset lọc
        btnResetFilter.setOnClickListener(v -> {
            selectedCategory = null;
            selectedBrand = null;
            spinnerCategory.setSelection(0);
            tvBrandLabel.setVisibility(View.GONE);
            spinnerBrand.setVisibility(View.GONE);
            applyFilters();
        });
    }

    private void fetchProducts() {
        db.collection("phones").get()
                .addOnSuccessListener(querySnapshot -> {
                    allProducts.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        try {
                            Product product = doc.toObject(Product.class);
                            product.setId(doc.getId());

                            // Chống null dữ liệu
                            if (product.getName() == null) product.setName("Không tên");
                            if (product.getBrand() == null) product.setBrand("Không xác định");
                            if (product.getCategory() == null) product.setCategory("Khác");

                            allProducts.add(product);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    setupCategorySpinner();
                    applyFilters();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void setupCategorySpinner() {
        Set<String> categories = new HashSet<>();
        for (Product p : allProducts) {
            if (p.getCategory() != null && !p.getCategory().isEmpty()) {
                categories.add(p.getCategory());
            }
        }

        List<String> sortedCategories = new ArrayList<>(categories);
        Collections.sort(sortedCategories); // Sắp xếp theo alphabet
        sortedCategories.add(0, "Tất cả"); // Thêm "Tất cả" lên đầu

        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, sortedCategories);
        spinnerCategory.setAdapter(categoryAdapter);
    }

    private void showBrandFilter(String category) {
        Set<String> brands = new HashSet<>();
        for (Product p : allProducts) {
            if (category.equals(p.getCategory())) {
                brands.add(p.getBrand());
            }
        }

        List<String> sortedBrands = new ArrayList<>(brands);
        Collections.sort(sortedBrands);
        sortedBrands.add(0, "Tất cả"); // "Tất cả" luôn ở đầu

        brandAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, sortedBrands);
        spinnerBrand.setAdapter(brandAdapter);

        tvBrandLabel.setVisibility(View.VISIBLE);
        spinnerBrand.setVisibility(View.VISIBLE);
    }

    private void applyFilters() {
        filteredProducts.clear();

        for (Product p : allProducts) {
            boolean matchCategory = (selectedCategory == null) || selectedCategory.equals(p.getCategory());
            boolean matchBrand = (selectedBrand == null) || selectedBrand.equals(p.getBrand());

            if (matchCategory && matchBrand) {
                filteredProducts.add(p);
            }
        }

        adapter.notifyDataSetChanged();
    }
}
