package com.example.androidapp.views.activities.Product;

import android.app.Activity;
import android.content.Intent;
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
import com.example.androidapp.models.CartItem;
import com.example.androidapp.models.Product;
import com.example.androidapp.repositories.CartRepository;
import com.example.androidapp.views.activities.carts.CheckoutActivity;
import com.example.androidapp.views.adapters.SearchSuggestionAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DetailProductActivity extends AppCompatActivity {
    ImageView iv_product;
    TextView tv_name_product, tv_product_price, tv_stock;
    TableLayout tableLayout;
    Button btn_buy, btn_add_to_cart;
    RecyclerView rv_suggestItem;
    List<Product> suggestionList = new ArrayList<>();
    SearchSuggestionAdapter suggestionAdapter;
    // luu san pham hien tai
    Product currentProduct;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth auth = FirebaseAuth.getInstance();
    CartRepository cartRepository;
    ScrollView sv_detail;
    MenuItem searchItem;
    SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_product);
        cartRepository = new CartRepository();

        Toolbar toolbar = findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Thông tin chi tiết");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.getNavigationIcon().setTint(Color.WHITE);
        }
        iv_product = findViewById(R.id.iv_product);
        tv_name_product = findViewById(R.id.tv_name_product);
        tv_product_price = findViewById(R.id.tv_prooduct_price);
        tableLayout = findViewById(R.id.tablelayout);
        sv_detail = findViewById(R.id.sv_detail);
        tv_stock = findViewById(R.id.tv_stock);
        btn_add_to_cart = findViewById(R.id.btn_add_to_cart);
        btn_buy = findViewById(R.id.btn_buy);
        rv_suggestItem = findViewById(R.id.rv_suggestItem);
        // Sự kiện click các nút
        btn_add_to_cart.setOnClickListener(v -> addToCart());
        btn_buy.setOnClickListener(v -> buyNow());
        // Load thông tin theo id máy của sự kiện click
        String docId = getIntent().getStringExtra("DOC_ID");
        if (docId != null) {
            loadProductFromFirestore(docId);
        }

        rv_suggestItem.setLayoutManager(new LinearLayoutManager(this));
        suggestionAdapter = new SearchSuggestionAdapter(suggestionList, product -> /*
                                                                                    * khi click vào 1 sản phẩm gợi ý thì
                                                                                    * tực hiện 3 hành động dưới
                                                                                    */ {
            // 1. Tải lại dữ liệu của sản phẩm mới ngay trên trang hiện tại
            loadProductFromFirestore(product.getId());

            // 2. Ẩn danh sách gợi ý
            rv_suggestItem.setVisibility(View.GONE);

            // 3. Đóng thanh tìm kiếm
            if (searchItem != null) {
                searchItem.collapseActionView();
            }
        });
        rv_suggestItem.setAdapter(suggestionAdapter);
        // Tạo một GestureDetector để nhận diện cử chỉ "chạm một lần"
        GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                // Khi người dùng chạm vào ScrollView

                // Ẩn bàn phím ⌨️
                hideKeyboard();

                // Ẩn danh sách gợi ý 👇
                rv_suggestItem.setVisibility(View.GONE);

                // Bỏ focus khỏi thanh tìm kiếm
                if (searchView != null) {
                    searchView.clearFocus();
                }

                return true;
            }
        });

        // Gắn Listener vào ScrollView
        sv_detail.setOnTouchListener((v, event) -> {
            // Chuyển sự kiện chạm cho GestureDetector xử lý
            gestureDetector.onTouchEvent(event);
            // Trả về false để không làm ảnh hưởng đến sự kiện cuộn
            return false;
        });
    }

    // them san pham vao gio hang
    private void addToCart() {
        Log.d("DetailProduct", "addToCart called");

        // Kiểm tra đăng nhập
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Log.d("DetailProduct", "User not logged in");
            Toast.makeText(this, "Vui lòng đăng nhập để thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            // TODO: Chuyển đến màn hình đăng nhập
            return;
        }

        // Kiểm tra sản phẩm hiện tại
        if (currentProduct == null) {
            Log.d("DetailProduct", "currentProduct is null");
            Toast.makeText(this, "Không tìm thấy thông tin sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("DetailProduct", "Adding product: " + currentProduct.getName() + " ID: " + currentProduct.getId());

        // Kiểm tra còn hàng
        if (currentProduct.getStock() <= 0) {
            Toast.makeText(this, "Sản phẩm đã hết hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable button để tránh click nhiều lần
        btn_add_to_cart.setEnabled(false);

        // Thêm vào giỏ hàng (mặc định số lượng = 1, không có variant)
        cartRepository.addToCart(
                user.getUid(),
                currentProduct,
                1, // Số lượng mặc định
                null, // variantId (nếu có màu/size thì cần dialog chọn)
                null, // variantName
                new CartRepository.OnCartOperationListener() {
                    @Override
                    public void onSuccess(String message) {
                        runOnUiThread(() -> {
                            Toast.makeText(DetailProductActivity.this,
                                    message, Toast.LENGTH_SHORT).show();
                            btn_add_to_cart.setEnabled(true);

                            // TODO: Cập nhật badge số lượng giỏ hàng trên toolbar
                            updateCartBadge();
                            // VERIFY: đọc nhanh lại giỏ hàng để đảm bảo item đã được lưu
                            try {
                                cartRepository.getCartItems(user.getUid(),
                                        new com.example.androidapp.repositories.CartRepository.OnCartItemsLoadedListener() {
                                            @Override
                                            public void onCartItemsLoaded(
                                                    java.util.List<com.example.androidapp.models.CartItemDisplay> items) {
                                                android.util.Log.d("DetailProduct", "Post-add verification: cart has "
                                                        + items.size() + " items for user=" + user.getUid());
                                                for (int i = 0; i < items.size(); i++) {
                                                    com.example.androidapp.models.CartItem ci = items.get(i)
                                                            .getCartItem();
                                                    android.util.Log.d("DetailProduct",
                                                            "cart[" + i + "] pid=" + ci.getProductId() + ", cachedName="
                                                                    + ci.getCachedName() + ", qty=" + ci.getQuantity());
                                                }
                                            }

                                            @Override
                                            public void onFailure(Exception e) {
                                                android.util.Log.e("DetailProduct", "Post-add verification failed", e);
                                            }
                                        });
                            } catch (Exception e) {
                                android.util.Log.e("DetailProduct", "Error requesting cart items", e);
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        runOnUiThread(() -> {
                            Toast.makeText(DetailProductActivity.this,
                                    "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            btn_add_to_cart.setEnabled(true);
                            Log.e("ADD_TO_CART", "Error adding to cart", e);
                        });
                    }
                }

        );
    }

    private void buyNow() {
        // Kiểm tra đăng nhập
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để mua hàng", Toast.LENGTH_SHORT).show();
            // TODO: Chuyển đến màn hình đăng nhập
            return;
        }

        // Kiểm tra sản phẩm
        if (currentProduct == null) {
            Toast.makeText(this, "Không tìm thấy thông tin sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra còn hàng
        if (currentProduct.getStock() <= 0) {
            Toast.makeText(this, "Sản phẩm đã hết hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        // tao cart item tu sp hien tai
        CartItem cartItem = new CartItem(
                currentProduct.getId(),
                user.getUid(),
                1 // Số lượng mặc định = 1
        );

        // Set cached data
        cartItem.setCachedName(currentProduct.getName());
        cartItem.setCachedPrice(currentProduct.getPrice());
        cartItem.setSelected(true); // Đánh dấu là đã chọn

        if (currentProduct.getImageUrls() != null && !currentProduct.getImageUrls().isEmpty()) {
            cartItem.setCachedImageUrl(currentProduct.getImageUrls().get(0));
        }

        // Tạo ArrayList với 1 item
        ArrayList<CartItem> selectedItems = new ArrayList<>();
        selectedItems.add(cartItem);
        // Chuyển sang màn hình Checkout
        Intent intent = new Intent(this, CheckoutActivity.class);
        intent.putParcelableArrayListExtra("selectedItems", selectedItems);
        startActivity(intent);
    }

    // CAP NHAT SO LUONG GIO HANG
    private void updateCartBadge() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null)
            return;

        cartRepository.getCartItemCount(user.getUid(), count -> {
            // TODO: Cập nhật badge trên icon giỏ hàng
            Log.d("CART_BADGE", "Cart items count: " + count);
        });
    }

    // Hàm lấy dữ liệu và loadform
    private void loadProductFromFirestore(String docId) {
        Log.d("DetailProduct", "Loading product with ID: " + docId);
        db.collection("phones")
                .document(docId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d("DetailProduct", "Product document exists");
                        // Tạo đối tượng Product từ Firestore document
                        currentProduct = documentSnapshot.toObject(Product.class);
                        if (currentProduct != null) {
                            currentProduct.setId(documentSnapshot.getId()); // Đảm bảo có ID
                            Log.d("DetailProduct",
                                    "Product loaded: " + currentProduct.getName() + " ID: " + currentProduct.getId());
                        } else {
                            Log.e("DetailProduct", "Failed to parse Product from document");
                        }

                        String name = documentSnapshot.getString("name");
                        Long price = documentSnapshot.getLong("price");
                        List<String> imageUrls = (List<String>) documentSnapshot.get("imageUrls");
                        Map<String, Object> specifications = (Map<String, Object>) documentSnapshot
                                .get("specifications");
                        // Lấy s lượng
                        Long stock = documentSnapshot.getLong("stock");
                        if (stock != null && stock > 0) {
                            tv_stock.setText("Còn: " + stock);
                            tv_stock.setTextColor(Color.parseColor("#2E7D32"));
                        } else {
                            tv_stock.setText("Hết hàng");
                            tv_stock.setTextColor(Color.RED);
                        }
                        tv_name_product.setText(name);
                        if (price != null) {

                            NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
                            tv_product_price.setText(currencyFormatter.format(price));
                        }

                        if (imageUrls != null && !imageUrls.isEmpty()) {
                            Glide.with(this)
                                    .load(imageUrls.get(0))
                                    .placeholder(R.drawable.ic_launcher_background)
                                    .into(iv_product);
                        }

                        if (specifications != null) {
                            displaySpecs(specifications);// gọi hàm tạo bảng
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

    // Hàm này tạo bảng thông số chi tiết
    private void displaySpecs(Map<String, Object> specs) {
        tableLayout.removeAllViews();
        if (specs == null)
            return;
        LayoutInflater inflater = LayoutInflater.from(this);
        Map<String, String> labelMapping = Map.of(
                "display", "Màn hình", "os", "Hệ điều hành", "mainCamera", "Camera sau",
                "frontCamera", "Camera trước", "cpu", "CPU", "ram", "RAM",
                "storage", "Bộ nhớ trong", "battery", "Dung lượng pin");
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

    // Tìm kiếm
    @Override
    public boolean onCreateOptionsMenu(Menu menu)/* Khởi tạo, hiển thị thanh menu */ {
        getMenuInflater().inflate(R.menu.find, menu);// Đưa layout thiết kế menu vào đối tượng menu
        // Gán vào biến toàn cục
        searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Tìm kiếm sản phẩm...");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {// Khi gõ
                if (newText.isEmpty()) {
                    rv_suggestItem.setVisibility(View.GONE);
                    return false;
                }
                String keyword = newText.toLowerCase();
                db.collection("phones").orderBy("name_lowercase").startAt(keyword).endAt(keyword + "\uf8ff").limit(10)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            suggestionList.clear();
                            if (!queryDocumentSnapshots.isEmpty()) {
                                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                    Product p = doc.toObject(Product.class);
                                    if (p != null) {
                                        p.setId(doc.getId());
                                        suggestionList.add(p);
                                    }
                                }
                            }
                            suggestionAdapter.notifyDataSetChanged();
                            rv_suggestItem.setVisibility(!suggestionList.isEmpty() ? View.VISIBLE : View.GONE);
                        })
                        .addOnFailureListener(e -> Log.e("DEBUG_SEARCH", "Lỗi khi tìm kiếm", e));
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.menu_cart) {
            // Mở HomeActivity và yêu cầu hiển thị CartFragment
            android.content.Intent intent = new android.content.Intent(this,
                    com.example.androidapp.views.activities.Auths.HomeActivity.class);
            intent.putExtra("open_cart", true);
            // Đặt flag để tránh tạo nhiều activity đằng sau
            intent.addFlags(
                    android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
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