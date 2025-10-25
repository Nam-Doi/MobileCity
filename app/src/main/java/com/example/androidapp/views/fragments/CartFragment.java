package com.example.androidapp.views.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidapp.R;
import com.example.androidapp.models.CartItem;
import com.example.androidapp.models.CartItemDisplay;
import com.example.androidapp.repositories.CartRepository;
import com.example.androidapp.views.activities.carts.CheckoutActivity;
import com.example.androidapp.views.adapters.cartAdt.CartAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class CartFragment extends Fragment implements CartAdapter.OnCartChangeListener {

    private RecyclerView recyclerCart;
    private CheckBox cbSelectItem;
    private TextView tvTotal,tvTitle;
    private Button btnCheckout;
    private List<CartItemDisplay> cartList;
    private CartAdapter cartAdapter;
    private ImageView imBack;
    private ProgressBar progressBar;
    private TextView tvEmptyCart;
    private CartRepository cartRepository;
    private FirebaseAuth auth;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        cartRepository = new CartRepository();
        auth = FirebaseAuth.getInstance();

        imBack = view.findViewById(R.id.btnBack);
        recyclerCart = view.findViewById(R.id.recyclerCart);
        tvTotal = view.findViewById(R.id.tvTotal);
        btnCheckout = view.findViewById(R.id.btnCheckout);
        cbSelectItem = view.findViewById(R.id.cbSelectItem);
        tvTitle = view.findViewById(R.id.tvTitle);
        // tvEmptyCart = view.findViewById(R.id.tvEmptyCart); // Nếu có

        cartList = new ArrayList<>();

        setupRecyclerView(); // Gọi trước load data để adapter sẵn sàng
        setupListeners();
        // loadCartDataFromRepository(); // Không cần gọi ở đây nữa, gọi trong onResume

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Load data khi Fragment hiển thị
        loadCartDataFromRepository();
    }

    private void loadCartDataFromRepository() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Log.d("CartFragment", "User not logged in");
            // Kiểm tra isAdded trước khi gọi hàm showEmptyCart
            if (isAdded()) {
                showEmptyCart("Vui lòng đăng nhập để xem giỏ hàng");
            }
            return;
        }

        String userId = currentUser.getUid();

        cartRepository.getCartItems(userId, new CartRepository.OnCartItemsLoadedListener() {
            @Override
            public void onCartItemsLoaded(List<CartItemDisplay> items) {
                // *** THÊM KIỂM TRA isAdded() NGAY ĐẦU CALLBACK ***
                if (!isAdded() || getContext() == null) {
                    Log.w("CartFragment", "onCartItemsLoaded callback received but Fragment not attached.");
                    return; // Thoát nếu Fragment không còn gắn
                }

                Log.d("CartFragment", "Cart items loaded: " + items.size());
                cartList.clear();
                cartList.addAll(items);

                if (cartList.isEmpty()) {
                    showEmptyCart("Giỏ hàng trống");
                } else {
                    showCartData();
                    if (cartAdapter != null) { // Kiểm tra adapter null
                        cartAdapter.notifyDataSetChanged();
                    }
                    updateTotalPrice();
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (!isAdded() || getContext() == null) {
                    Log.e("CartFragment", "Error loading cart but Fragment not attached.", e);
                    return;
                }
                Toast.makeText(getContext(), // Dùng getContext()
                        "Lỗi tải giỏ hàng: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
                Log.e("CartFragment", "Error loading cart", e);
            }
        });
    }

    private void setupRecyclerView() {
        if (!isAdded() || getContext() == null) return;

        cartAdapter = new CartAdapter(cartList, this);
        recyclerCart.setLayoutManager(new LinearLayoutManager(getContext())); // Dùng getContext()
        recyclerCart.setAdapter(cartAdapter);
    }

    private void setupListeners() {
        if (!isAdded() || getContext() == null) return;

        imBack.setOnClickListener(v -> {
            // Kiểm tra isAdded bên trong listener nếu cần
            if (!isAdded()) return;
            try {
                // Code chuyển Fragment giữ nguyên
                requireActivity()
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.main_container, new HomeFragment())
                        .commit();
                // ... (cập nhật BottomNav) ...
            } catch (Exception ex) {
                if (isAdded()) { // Kiểm tra lại trước khi pop
                    requireActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });

        btnCheckout.setOnClickListener(v -> {
            // Kiểm tra isAdded bên trong listener
            if (!isAdded() || getContext() == null || cartAdapter == null) return;

            List<CartItem> selectedItems = cartAdapter.getSelectedItems();
            if (selectedItems.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng chọn sản phẩm", Toast.LENGTH_SHORT).show();
            } else {
                proceedToCheckout(selectedItems);
            }
        });

        cbSelectItem.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Kiểm tra isAdded bên trong listener
            if (!isAdded() || cartAdapter == null) return;
            cartAdapter.selectAll(isChecked);
        });
    }

    @Override
    public void onCartUpdated() {
        if (!isAdded() || getContext() == null || cartAdapter == null) return;

        updateTotalPrice();

        boolean allSelected = cartAdapter.getSelectedItemCount() == cartList.size() && !cartList.isEmpty();
        // Tạm thời tắt listener để tránh vòng lặp vô hạn khi setChecked
        cbSelectItem.setOnCheckedChangeListener(null);
        cbSelectItem.setChecked(allSelected);
        // Bật lại listener
        cbSelectItem.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isAdded() || cartAdapter == null) return;
            cartAdapter.selectAll(isChecked);
        });
    }
    private void updatetotalcart(){
        if (!isAdded() || getContext() == null || cartAdapter == null) return;



    }

    private void updateTotalPrice() {
        if (!isAdded() || getContext() == null || cartAdapter == null) return;

        double total = cartAdapter.getTotalPrice();
        if (tvTotal != null) { // Kiểm tra view null
            tvTotal.setText(String.format("%,.0f ₫", total));
        }


        int selectedItemCount = cartAdapter.getSelectedItemCount();
        if (btnCheckout != null) { // Kiểm tra view null
            btnCheckout.setText("Mua hàng (" + selectedItemCount + ")");
        }
        int carttotal = cartAdapter.getTotalQuantity();
        if(tvTitle != null){
            tvTitle.setText("Giỏ hàng (" + carttotal + ")");

        }
    }

    private void proceedToCheckout(List<CartItem> selectedItems) {
        if (!isAdded() || getContext() == null) return;

        try {
            Log.d("CartFragment", "Proceeding to checkout with " + selectedItems.size() + " items");
            // ... (log items) ...
        } catch (Exception e) {
            Log.e("CartFragment", "Error logging selectedItems", e);
        }

        Intent intent = new Intent(getContext(), CheckoutActivity.class); // Dùng getContext()
        intent.putParcelableArrayListExtra("selectedItems", new ArrayList<>(selectedItems));
        startActivity(intent);
    }

    private void showEmptyCart(String message) {
        if (!isAdded() || getContext() == null) return; // Kiểm tra isAdded()

        // 1. Ẩn RecyclerView và CheckBox All
        if (recyclerCart != null) recyclerCart.setVisibility(View.GONE);
        if (cbSelectItem != null) cbSelectItem.setVisibility(View.GONE); // Ẩn checkbox "All"

        // 2. Vô hiệu hóa nút Checkout và Reset Text
        if (btnCheckout != null) {
            btnCheckout.setEnabled(false);
            btnCheckout.setText("Mua hàng (0)"); // Reset text nút
        }

        // 3. Reset Tổng tiền
        if (tvTotal != null) {
            tvTotal.setText(String.format("%,.0f ₫", 0.0)); // Reset tổng tiền về 0
        }

        // 4. Hiện thông báo trống (Nếu có tvEmptyCart)
        if (tvEmptyCart != null) {
            tvEmptyCart.setVisibility(View.VISIBLE);
            tvEmptyCart.setText(message);
        } else {
            // Nếu không có TextView riêng, hiển thị Toast
            //Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show(); // Dùng getContext()
        }
    }

    private void showCartData() {
        if (!isAdded()) return;

        if (recyclerCart != null) recyclerCart.setVisibility(View.VISIBLE);
        if (btnCheckout != null) btnCheckout.setEnabled(true);
        if (cbSelectItem != null) cbSelectItem.setVisibility(View.VISIBLE);
        if (tvEmptyCart != null) tvEmptyCart.setVisibility(View.GONE);
    }
}