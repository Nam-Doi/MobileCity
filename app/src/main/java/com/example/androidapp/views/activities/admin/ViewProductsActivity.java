package com.example.androidapp.views.activities.admin;

import android.os.Bundle;
import android.util.Log; // <-- THÊM DÒNG NÀY
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidapp.R;
import com.example.androidapp.views.adapters.ProductAdapter;
import com.example.androidapp.models.Product;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ViewProductsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private List<Product> productList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // SỬA LỖI TIỀM NĂNG:
        // Đảm bảo tên layout là "activity_view_products" chứ không phải "activity_views_product"
        setContentView(R.layout.activity_views_product); // <-- Kiểm tra lại tên file XML này

        recyclerView = findViewById(R.id.rvViewProducts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        productList = new ArrayList<>();
        adapter = new ProductAdapter(this, productList);
        recyclerView.setAdapter(adapter);

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

                            // Gán ID cho sản phẩm (quan trọng cho các chức năng sau)
                            product.setId(doc.getId());

                            // Nếu không lỗi, thêm vào list
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
                        Toast.makeText(ViewProductsActivity.this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}