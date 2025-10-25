package com.example.androidapp.views.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log; // Import Log
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidapp.R;
import com.example.androidapp.models.Product;
import com.example.androidapp.views.activities.Product.DetailProductActivity;
// import com.example.androidapp.views.activities.admin.DetailProductActivitys; // Có thể không cần import này nữa
import com.example.androidapp.views.adapters.ProductGridAdapter;
import com.example.androidapp.views.adapters.SearchSuggestionAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// HomeFragment triển khai Interface click của SearchSuggestionAdapter (cho thanh tìm kiếm)
public class HomeFragment extends Fragment implements SearchSuggestionAdapter.OnItemClickListener {

    // Views
    private GridView gridViewProducts;
    private EditText edtSearch;
    private RecyclerView recyclerViewSearchResults;

    // Adapters
    private ProductGridAdapter productAdapter; // Adapter cho GridView chính
    private SearchSuggestionAdapter searchAdapter; // Adapter cho RecyclerView thả xuống

    // Data Lists
    private List<Product> productList; // Danh sách hiển thị trên GridView (có thể thay đổi)
    private List<Product> originalProductList; // Danh sách gốc chứa TẤT CẢ sản phẩm
    private List<Product> searchList; // Danh sách cho kết quả tìm kiếm

    private static final String TAG = "HomeFragment"; // Thêm TAG để Log

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = null;
        try {
            view = inflater.inflate(R.layout.fragment_home, container, false);

            // Ánh xạ Views
            ImageButton menuButton = view.findViewById(R.id.menuButton);
            if (menuButton != null)
                menuButton.setOnClickListener(this::showPopupMenu);

            edtSearch = view.findViewById(R.id.edtSearch);
            gridViewProducts = view.findViewById(R.id.gridViewProducts);
            recyclerViewSearchResults = view.findViewById(R.id.recyclerViewSearchResults);

            // Khởi tạo List
            productList = new ArrayList<>(); // List cho adapter GridView
            originalProductList = new ArrayList<>(); // List gốc
            searchList = new ArrayList<>(); // List cho adapter Search

            // 1. Khởi tạo Adapter cho GridView chính (SỬ DỤNG CONSTRUCTOR MỚI)
            productAdapter = new ProductGridAdapter(requireContext(), productList); // Chỉ cần context và list
            if (gridViewProducts != null)
                gridViewProducts.setAdapter(productAdapter);

            // Click handler cho GridView (ĐÃ CÓ SẴN VÀ ĐÚNG)
            if (gridViewProducts != null) {
                gridViewProducts.setOnItemClickListener((parent, itemView, position, id) -> {
                    try {
                        // Lấy product từ productList (list đang hiển thị)
                        Product selectedProduct = (productList != null && position >= 0
                                && position < productList.size()) ? productList.get(position) : null;
                        if (selectedProduct != null && selectedProduct.getId() != null) {
                            Intent intent = new Intent(getActivity(), DetailProductActivity.class);
                            intent.putExtra("DOC_ID", selectedProduct.getId());
                            startActivity(intent);
                        } else {
                            Log.w(TAG, "Clicked item has null product or null ID at position: " + position);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error on grid item click", e);
                    }
                });
            }

            // 2. Khởi tạo Adapter cho RecyclerView tìm kiếm (Giữ nguyên)
            searchAdapter = new SearchSuggestionAdapter(searchList, this);
            if (recyclerViewSearchResults != null) {
                recyclerViewSearchResults.setLayoutManager(new LinearLayoutManager(requireContext()));
                recyclerViewSearchResults.setAdapter(searchAdapter);
            }

            // Thiết lập Listener tìm kiếm (Giữ nguyên)
            setupSearchListener();

            // Tải sản phẩm từ Firebase
            loadProductsFromFirebase();

        } catch (Exception ex) {
            Log.e(TAG, "Exception in onCreateView", ex);
            if (getContext() != null) {
                Toast.makeText(getContext(), "Lỗi khi khởi tạo trang chủ", Toast.LENGTH_LONG).show();
            }
        }

        return view;
    }

    // ========== HÀM LỌC MỚI THEO CATEGORY ==========
    private void filterByCategory(String categoryToFilter) {
        Log.d(TAG, "Filtering by category: " + categoryToFilter);
        productList.clear(); // Xóa danh sách hiện tại trong adapter GridView

        if (categoryToFilter == null || categoryToFilter.equalsIgnoreCase("Trang chủ")) {
            // Nếu chọn "Trang chủ" hoặc null, hiển thị tất cả sản phẩm từ danh sách gốc
            productList.addAll(originalProductList);
            Log.d(TAG, "Showing all products: " + productList.size());
        } else {
            // Lọc sản phẩm từ danh sách gốc dựa trên category
            for (Product product : originalProductList) {
                if (product.getCategory() != null && product.getCategory().equalsIgnoreCase(categoryToFilter)) {
                    productList.add(product);
                }
            }
            Log.d(TAG, "Filtered products count: " + productList.size());
        }

        // Cập nhật GridView để hiển thị danh sách đã lọc
        if (productAdapter != null) {
            productAdapter.notifyDataSetChanged();
        }

        // (Tùy chọn) Cuộn lên đầu GridView
        if (gridViewProducts != null) {
            gridViewProducts.smoothScrollToPosition(0);
        }
    }
    // ===============================================

    // ========== HÀM LOAD ĐÃ ĐƯỢC CẬP NHẬT ==========
    private void loadProductsFromFirebase() {
        Log.d(TAG, "Loading products from Firebase...");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("phones") // Đảm bảo tên collection là đúng
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    try {
                        originalProductList.clear(); // Xóa list gốc trước khi thêm mới
                        productList.clear();       // Xóa cả list của adapter
                        int count = 0;
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            try {
                                Product product = doc.toObject(Product.class);
                                if (product != null) {
                                    product.setId(doc.getId()); // Gán ID từ document
                                    originalProductList.add(product); // Thêm vào list gốc
                                    count++;
                                }
                            } catch (Exception e) {
                                Log.w(TAG, "Failed to parse product doc: " + doc.getId(), e);
                            }
                        }
                        // Sao chép toàn bộ list gốc vào list hiển thị ban đầu
                        productList.addAll(originalProductList);
                        Log.d(TAG, "Loaded products successfully: " + count);
                        // Cập nhật adapter GridView
                        if (productAdapter != null)
                            productAdapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing products response", e);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load products", e);
                    if (getContext() != null)
                        Toast.makeText(getContext(), "Không thể tải sản phẩm", Toast.LENGTH_SHORT).show();
                });
    }
    // ===============================================

    // ========== HÀM HIỂN THỊ MENU ĐÃ ĐƯỢC CẬP NHẬT ==========
    private void showPopupMenu(View v) {
        PopupMenu popup = new PopupMenu(requireContext(), v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.top_menu, popup.getMenu());

        // Code hiển thị icon (giữ nguyên)
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

        // Xử lý sự kiện click item menu -> Gọi hàm lọc
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_home) {
                filterByCategory("Trang chủ"); // Hoặc filterByCategory(null);
                return true;
            } else if (id == R.id.menu_phone) {
                filterByCategory("Điện thoại"); // Lọc theo tên Category trong Firebase
                return true;
            } else if (id == R.id.menu_accessory) {
                filterByCategory("Phụ kiện"); // Lọc theo tên Category trong Firebase
                return true;
            } else if (id == R.id.menu_tablet) {
                filterByCategory("Máy tính bảng"); // Lọc theo tên Category trong Firebase
                return true;
            }
            return false;
        });

        popup.show();
    }
    // ===============================================

    // Hàm onItemClick cho SearchSuggestionAdapter (giữ nguyên)
    @Override
    public void onItemClick(Product product) {
        if (product == null || product.getId() == null) {
            Log.w(TAG, "Clicked search item has null product or null ID");
            return;
        }
        recyclerViewSearchResults.setVisibility(View.GONE);
        edtSearch.setText("");
        Intent intent = new Intent(requireContext(), DetailProductActivity.class);
        intent.putExtra("DOC_ID", product.getId());
        startActivity(intent);
    }

    // Hàm setupSearchListener (giữ nguyên)
    private void setupSearchListener() {
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    recyclerViewSearchResults.setVisibility(View.GONE);
                } else {
                    recyclerViewSearchResults.setVisibility(View.VISIBLE);
                    filterProductsForSearch(query); // Đổi tên hàm này cho rõ
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // Hàm filterProducts (đổi tên thành filterProductsForSearch để phân biệt)
    private void filterProductsForSearch(String query) {
        String lowerCaseQuery = query.toLowerCase(Locale.getDefault());
        searchList.clear(); // Clear danh sách KẾT QUẢ TÌM KIẾM

        // Lọc từ danh sách GỐC để tìm kiếm
        if (originalProductList != null) {
            for (Product product : originalProductList) {
                if (product.getName() != null && product.getName().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery)) {
                    searchList.add(product);
                }
            }
        }

        // Cập nhật adapter của RecyclerView TÌM KIẾM
        if (searchAdapter != null) {
            searchAdapter.notifyDataSetChanged();
        }
    }
}