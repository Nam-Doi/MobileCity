package com.example.androidapp.views.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText; // Thêm import
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView; // Thêm import

import com.example.androidapp.R;
import com.example.androidapp.models.Product;
import com.example.androidapp.views.activities.Product.DetailProductActivity;
import com.example.androidapp.views.adapters.ProductGridAdapter;
import com.example.androidapp.views.adapters.SearchSuggestionAdapter; // ✅ IMPORT MỚI
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// HomeFragment triển khai Interface click của SearchSuggestionAdapter
public class HomeFragment extends Fragment implements SearchSuggestionAdapter.OnItemClickListener {

    private GridView gridViewProducts;
    private ProductGridAdapter productAdapter; // Adapter cho GridView

    // ✅ THÊM VIEWS VÀ ADAPTER CHO TÌM KIẾM
    private EditText edtSearch;
    private RecyclerView recyclerViewSearchResults;
    private SearchSuggestionAdapter searchAdapter; // Adapter cho RecyclerView thả xuống

    private List<Product> productList;
    private List<Product> searchList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Ánh xạ Views
        ImageButton menuButton = view.findViewById(R.id.menuButton);
        menuButton.setOnClickListener(this::showPopupMenu);

        edtSearch = view.findViewById(R.id.edtSearch); // ✅ Ánh xạ EditText
        gridViewProducts = view.findViewById(R.id.gridViewProducts);
        recyclerViewSearchResults = view.findViewById(R.id.recyclerViewSearchResults); // ✅ Ánh xạ RecyclerView

        // Khởi tạo List
        productList = new ArrayList<>();
        searchList = new ArrayList<>();

        // 1. Khởi tạo Adapter cho GridView chính
        // LƯU Ý: productAdapter PHẢI CÓ constructor nhận thêm 'this' (OnItemClickListener)
        productAdapter = new ProductGridAdapter(requireContext(), productList, this);
        gridViewProducts.setAdapter(productAdapter);
        //Thành thêm
        gridViewProducts.setOnItemClickListener((parent, itemView, position, id) -> {
            Product selectedProduct = productList.get(position);

            if (selectedProduct != null && selectedProduct.getId() != null) {
                Intent intent = new Intent(getActivity(), DetailProductActivity.class);

                // Gửi đi ID chính xác của sản phẩm với key là "DOC_ID"
                intent.putExtra("DOC_ID", selectedProduct.getId());

                startActivity(intent);
            }
        });//End

        // 2. Khởi tạo Adapter cho RecyclerView tìm kiếm
        // Dùng SearchSuggestionAdapter và truyền 'this' (OnItemClickListener)
        searchAdapter = new SearchSuggestionAdapter(searchList, this);
        recyclerViewSearchResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewSearchResults.setAdapter(searchAdapter);

        // Thiết lập Listener tìm kiếm
        setupSearchListener();

        // Tải sản phẩm từ Firebase
        loadProductsFromFirebase();

        return view;
    }

    // ✅ TRIỂN KHAI HÀNH ĐỘNG CLICK (Hàm xử lý chuyển màn hình chi tiết)
    @Override
    public void onItemClick(Product product) {
        // Tùy chọn: Ẩn khung tìm kiếm và bàn phím sau khi click
        recyclerViewSearchResults.setVisibility(View.GONE);
        edtSearch.setText(""); // Xóa nội dung tìm kiếm

        Intent intent = new Intent(requireContext(), DetailProductActivity.class);
        intent.putExtra("phones", product);
        startActivity(intent);
    }

    private void setupSearchListener() {
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();

                if (query.isEmpty()) {
                    // Nếu rỗng: Ẩn RecyclerView tìm kiếm
                    recyclerViewSearchResults.setVisibility(View.GONE);
                } else {
                    // Nếu có chữ: Hiện RecyclerView (hiệu ứng thả xuống)
                    recyclerViewSearchResults.setVisibility(View.VISIBLE);
                    filterProducts(query);
                }
            }

            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void filterProducts(String query) {
        String lowerCaseQuery = query.toLowerCase(Locale.getDefault());
        searchList.clear();

        if (productList != null) {
            for (Product product : productList) {
                // Lọc theo tên sản phẩm, không phân biệt hoa/thường
                if (product.getName().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery)) {
                    searchList.add(product);
                }
            }
        }

        // Cập nhật kết quả trên RecyclerView tìm kiếm
        searchAdapter.notifyDataSetChanged();
    }

    private void loadProductsFromFirebase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("phones")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    productList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Product product = doc.toObject(Product.class);
                        productList.add(product);
                    }
                    productAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Không thể tải sản phẩm", Toast.LENGTH_SHORT).show());
    }

    private void showPopupMenu(View v) {
        PopupMenu popup = new PopupMenu(requireContext(), v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.top_menu, popup.getMenu());

        try {
            Field mPopupField = popup.getClass().getDeclaredField("mPopup");
            mPopupField.setAccessible(true);
            Object mPopup = mPopupField.get(popup);
            Class<?> popupClass = Class.forName(mPopup.getClass().getName());
            Method setForceShowIcon = popupClass.getMethod("setForceShowIcon", boolean.class);
            setForceShowIcon.invoke(mPopup, true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_home) {
                Toast.makeText(getContext(), "Trang chủ", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.menu_phone) {
                Toast.makeText(getContext(), "Điện thoại", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.menu_accessory) {
                Toast.makeText(getContext(), "Phụ kiện", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.menu_other) {
                Toast.makeText(getContext(), "Khác", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });

        popup.show();
    }

}
