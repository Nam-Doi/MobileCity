package com.example.androidapp.views.activities.admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log; // <-- THÊM DÒNG NÀY
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
                Intent intent = new Intent(ManageProductActivity.this, EditProductActivity.class);
                intent.putExtra("product", product); // Truyền object product
                startActivity(intent);
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

    // --- HÀM ĐÃ SỬA LỖI NẰM Ở ĐÂY ---
    private void fetchProducts() {
        db.collection("phones").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    productList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {

                        // Bọc trong try-catch để bắt lỗi
                        try {
                            // Lệnh có thể gây crash
                            Product product = doc.toObject(Product.class);

                            // Kiểm tra an toàn: Nếu 'variants' là null (dữ liệu cũ)
                            // hoặc cấu trúc sai, ta bỏ qua
                            if (product.getVariants() == null) {
                                Log.w("FetchProduct", "Bỏ qua sản phẩm có cấu trúc cũ/lỗi: " + doc.getId());
                                continue; // Bỏ qua, đi đến sản phẩm tiếp theo
                            }

                            // Nếu không lỗi, thêm vào list
                            product.setId(doc.getId());
                            productList.add(product);

                        } catch (Exception e) {
                            // Bắt lỗi (bao gồm cả lỗi "Expected List, got HashMap")
                            Log.e("FetchProductError", "Lỗi map dữ liệu cho sản phẩm: " + doc.getId(), e);
                            // Bỏ qua sản phẩm lỗi này và tiếp tục
                        }
                    }
                    // Cập nhật adapter sau khi đã lọc
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(ManageProductActivity.this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}