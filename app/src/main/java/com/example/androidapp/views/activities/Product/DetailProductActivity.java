package com.example.androidapp.views.activities.Product;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.androidapp.R;
import com.example.androidapp.models.Product;
import com.example.androidapp.views.adapters.SearchSuggestionAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DetailProductActivity extends AppCompatActivity {
    ImageView productImage;
    TextView productName, productPrice;
    TableLayout tableLayout;
    Button btnCart, btnBuy, btnAddToCart;
    RecyclerView suggestItem;
    List<Product> suggestionList = new ArrayList<>();
    SearchSuggestionAdapter suggestionAdapter;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ScrollView scr_detail;
    MenuItem searchItem;
    SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_product);

        Toolbar toolbar = findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Thông tin chi tiết");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.getNavigationIcon().setTint(Color.WHITE);
        }
        productImage = findViewById(R.id.product_image);
        productName = findViewById(R.id.product_name);
        productPrice = findViewById(R.id.product_price);
        tableLayout = findViewById(R.id.tablelayout);
        scr_detail=findViewById(R.id.scr_detail);
        btnCart = findViewById(R.id.btnCart);
        btnAddToCart = findViewById(R.id.btnAddtoCart);
        btnBuy = findViewById(R.id.btnBuy);
        suggestItem = findViewById(R.id.searchSuggestionsRecycler);
        //Sự kiện click các nút
        btnCart.setOnClickListener(v -> Toast.makeText(this, "Xem giỏ hàng", Toast.LENGTH_SHORT).show());
        btnAddToCart.setOnClickListener(v -> Toast.makeText(this, "Đã thêm vào giỏ", Toast.LENGTH_SHORT).show());
        btnBuy.setOnClickListener(v -> Toast.makeText(this, "Mua ngay", Toast.LENGTH_SHORT).show());
        //Load thông tin theo id máy của sự kiện click
        String docId = getIntent().getStringExtra("DOC_ID");
        if (docId != null) {
            loadProductFromFirestore(docId);
        }

        suggestItem.setLayoutManager(new LinearLayoutManager(this));
        suggestionAdapter = new SearchSuggestionAdapter(suggestionList, product -> /*khi click vào 1 sản phẩm gợi ý thì tực hiện 3 hành động dưới*/{
            // 1. Tải lại dữ liệu của sản phẩm mới ngay trên trang hiện tại
            loadProductFromFirestore(product.getId());

            // 2. Ẩn danh sách gợi ý
            suggestItem.setVisibility(View.GONE);

            // 3. Đóng thanh tìm kiếm
            if (searchItem != null) {
                searchItem.collapseActionView();
            }
        });
        suggestItem.setAdapter(suggestionAdapter);
        //Tạo một GestureDetector để nhận diện cử chỉ "chạm một lần"
        GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                // Khi người dùng chạm vào ScrollView

                // Ẩn bàn phím ⌨️
                hideKeyboard();

                // Ẩn danh sách gợi ý 👇
                suggestItem.setVisibility(View.GONE);

                // Bỏ focus khỏi thanh tìm kiếm
                if (searchView != null) {
                    searchView.clearFocus();
                }

                return true;
            }
        });

        //Gắn Listener vào ScrollView
        scr_detail.setOnTouchListener((v, event) -> {
            // Chuyển sự kiện chạm cho GestureDetector xử lý
            gestureDetector.onTouchEvent(event);
            // Trả về false để không làm ảnh hưởng đến sự kiện cuộn
            return false;
        });
    }
    //Hàm lấy dữ liệu và loadform
    private void loadProductFromFirestore(String docId) {
        db.collection("phones")
                .document(docId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        Long price = documentSnapshot.getLong("price");
                        List<String> imageUrls = (List<String>) documentSnapshot.get("imageUrls");
                        Map<String, Object> specifications = (Map<String, Object>) documentSnapshot.get("specifications");

                        productName.setText(name);

                        if (price != null) {

                            NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
                            productPrice.setText(currencyFormatter.format(price));
                        }

                        if (imageUrls != null && !imageUrls.isEmpty()) {
                            Glide.with(this)
                                    .load(imageUrls.get(0))
                                    .placeholder(R.drawable.ic_launcher_background)
                                    .into(productImage);
                        }

                        if (specifications != null) {
                            displaySpecs(specifications);//gọi hàm tạo bảng
                        }
                    } else {
                        Toast.makeText(this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lấy dữ liệu thất bại", Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error", e);
                });
    }
    //Hàm này tạo bảng thông số chi tiết
    private void displaySpecs(Map<String, Object> specs) {
        tableLayout.removeAllViews();
        if (specs == null) return;
        LayoutInflater inflater = LayoutInflater.from(this);
        Map<String, String> labelMapping = Map.of(
                "display", "Màn hình", "os", "Hệ điều hành", "mainCamera", "Camera sau",
                "frontCamera", "Camera trước", "cpu", "CPU", "ram", "RAM",
                "storage", "Bộ nhớ trong", "battery", "Dung lượng pin"
        );
        for (Map.Entry<String, Object> entry : specs.entrySet()) {
            TableRow row = (TableRow) inflater.inflate(R.layout.row_specification, tableLayout, false);
            TextView tvLabel = row.findViewById(R.id.tvLabel);
            TextView tvValue = row.findViewById(R.id.tvValue);
            String label = labelMapping.getOrDefault(entry.getKey(), entry.getKey());
            tvLabel.setText(label);
            tvValue.setText(String.valueOf(entry.getValue()));
            tableLayout.addView(row);
        }
    }
    //Tìm kiếm
    @Override
    public boolean onCreateOptionsMenu(Menu menu)/*Khởi tạo, hiển thị thanh menu*/ {
        getMenuInflater().inflate(R.menu.find, menu);//Đưa layout thiết kế menu vào đối tượng menu
        // Gán vào biến toàn cục
        searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Tìm kiếm sản phẩm...");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {//Khi gõ
                if (newText.isEmpty()) {suggestItem.setVisibility(View.GONE);
                    return false;
                }
                String keyword = newText.toLowerCase();
                db.collection("phones").orderBy("name_lowercase").startAt(keyword).endAt(keyword + "\uf8ff").limit(10)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            suggestionList.clear();
                            if (!queryDocumentSnapshots.isEmpty()) {
                                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                    suggestionList.add(doc.toObject(Product.class));
                                }
                            }
                            suggestionAdapter.notifyDataSetChanged();
                            suggestItem.setVisibility(!suggestionList.isEmpty() ? View.VISIBLE : View.GONE);
                        })
                        .addOnFailureListener(e -> Log.e("DEBUG_SEARCH", "Lỗi khi tìm kiếm", e));
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        // Tìm view đang có focus để ẩn bàn phím từ nó
        View view = getCurrentFocus();
        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}