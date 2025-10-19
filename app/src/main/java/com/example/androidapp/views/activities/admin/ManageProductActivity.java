package com.example.androidapp.views.activities.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidapp.R;
import com.example.androidapp.models.Product;
import com.example.androidapp.views.adapters.ManageProductAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ManageProductActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ManageProductAdapter adapter;
    private List<Product> productList;
    private FirebaseFirestore db;
    private Button btnAddProduct;

    @Override
    protected void onResume() {
        super.onResume();
        fetchProducts(); // refresh khi trở về từ Add/Edit
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_product);

        recyclerView = findViewById(R.id.rvManageProducts);
        btnAddProduct = findViewById(R.id.btnAddProduct);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        productList = new ArrayList<>();
        adapter = new ManageProductAdapter(this, productList, new ManageProductAdapter.OnProductActionListener() {
            @Override
            public void onEdit(Product product) {
                Toast.makeText(ManageProductActivity.this, "Sửa: " + product.getName(), Toast.LENGTH_SHORT).show();
                // Mở EditProductActivity nếu có
            }

            @Override
            public void onDelete(Product product) {
                if(product.getId() != null){
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

        btnAddProduct.setOnClickListener(v -> {
            Intent intent = new Intent(ManageProductActivity.this, AddProductActivity.class);
            startActivity(intent);
        });

        db = FirebaseFirestore.getInstance();
        fetchProducts();
    }

    private void fetchProducts() {
        db.collection("phones").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    productList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Product product = doc.toObject(Product.class);
                        product.setId(doc.getId()); // gán ID từ Firestore
                        productList.add(product);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(ManageProductActivity.this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
