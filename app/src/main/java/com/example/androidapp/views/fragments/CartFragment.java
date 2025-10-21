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
    private TextView tvTotal;
    private Button btnCheckout;
    private List<CartItem> cartList;
    private CartAdapter cartAdapter;
    private ImageView imBack;
    private ProgressBar progressBar;
    private TextView tvEmptyCart;

    // Repository
    private CartRepository cartRepository;
    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        // Khởi tạo
        cartRepository = new CartRepository();
        auth = FirebaseAuth.getInstance();

        // Ánh xạ views
        imBack = view.findViewById(R.id.btnBack);
        recyclerCart = view.findViewById(R.id.recyclerCart);
        tvTotal = view.findViewById(R.id.tvTotal);
        btnCheckout = view.findViewById(R.id.btnCheckout);
        cbSelectItem = view.findViewById(R.id.cbSelectItem);

        // Optional - nếu có trong layout
        // progressBar = view.findViewById(R.id.progressBar);
        // tvEmptyCart = view.findViewById(R.id.tvEmptyCart);

        cartList = new ArrayList<>();

        setupRecyclerView();
        setupListeners();
        loadCartDataFromRepository();

        return view;
    }

    /**
     * Load dữ liệu từ CartRepository (có JOIN với Product)
     */
    private void loadCartDataFromRepository() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Log.d("CartFragment", "User not logged in");
            showEmptyCart("Vui lòng đăng nhập để xem giỏ hàng");
            return;
        }

        String userId = currentUser.getUid();

        // Sử dụng CartRepository để load dữ liệu
        cartRepository.getCartItems(userId, new CartRepository.OnCartItemsLoadedListener() {
            @Override
            public void onCartItemsLoaded(List<CartItemDisplay> items) {
                Log.d("CartFragment", "Cart items loaded: " + items.size());

                cartList.clear();

                // Chuyển đổi CartItemDisplay -> CartItem để hiển thị
                for (CartItemDisplay display : items) {
                    CartItem cartItem = display.getCartItem();

                    // Cập nhật cache với dữ liệu mới nhất từ Product
                    if (display.getProduct() != null) {
                        cartItem.setCachedName(display.getProductName());
                        cartItem.setCachedPrice(display.getCurrentPrice());
                        cartItem.setCachedImageUrl(display.getImageUrl());
                    }

                    cartList.add(cartItem);
                }

                if (cartList.isEmpty()) {
                    showEmptyCart("Giỏ hàng trống");
                } else {
                    showCartData();
                    cartAdapter.notifyDataSetChanged();
                    updateTotalPrice();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(requireContext(),
                        "Lỗi tải giỏ hàng: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
                Log.e("CartFragment", "Error loading cart", e);
            }
        });
    }

    private void setupRecyclerView() {
        cartAdapter = new CartAdapter(cartList, this);
        recyclerCart.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerCart.setAdapter(cartAdapter);
    }

    private void setupListeners() {
        imBack.setOnClickListener(v -> {
            // Thay vì popBackStack (có thể không có backstack), thay bằng chuyển về
            // HomeFragment
            try {
                requireActivity()
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.main_container, new HomeFragment())
                        .commit();

                // Cập nhật BottomNavigationView nếu có để highlight tab Home
                android.view.View navView = requireActivity().findViewById(R.id.bottom_navigation);
                if (navView instanceof com.google.android.material.bottomnavigation.BottomNavigationView) {
                    ((com.google.android.material.bottomnavigation.BottomNavigationView) navView)
                            .setSelectedItemId(R.id.nav_home);
                }
            } catch (Exception ex) {
                // Fallback: nếu có lỗi thì thử popBackStack
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });

        btnCheckout.setOnClickListener(v -> {
            List<CartItem> selectedItems = cartAdapter.getSelectedItems();
            if (selectedItems.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng chọn sản phẩm", Toast.LENGTH_SHORT).show();
            } else {
                proceedToCheckout(selectedItems);
            }
        });

        // Check all
        cbSelectItem.setOnCheckedChangeListener((buttonView, isChecked) -> {
            cartAdapter.selectAll(isChecked);
        });
    }

    @Override
    public void onCartUpdated() {
        updateTotalPrice();

        // Kiểm tra xem tất cả item đã được chọn chưa
        boolean allSelected = cartAdapter.getSelectedItemCount() == cartList.size() && !cartList.isEmpty();
        cbSelectItem.setOnCheckedChangeListener(null);
        cbSelectItem.setChecked(allSelected);
        cbSelectItem.setOnCheckedChangeListener((buttonView, isChecked) -> {
            cartAdapter.selectAll(isChecked);
        });
    }

    private void updateTotalPrice() {
        double total = cartAdapter.getTotalPrice();
        tvTotal.setText(String.format("%,.0f ₫", total));

        int selectedItemCount = cartAdapter.getSelectedItemCount();
        btnCheckout.setText("Mua hàng (" + selectedItemCount + ")");
    }

    private void proceedToCheckout(List<CartItem> selectedItems) {
        // Log selected items for debugging — helps diagnose parcelable / data issues
        try {
            android.util.Log.d("CartFragment", "Proceeding to checkout with " + selectedItems.size() + " items");
            for (int i = 0; i < selectedItems.size(); i++) {
                CartItem it = selectedItems.get(i);
                android.util.Log.d("CartFragment", "item[" + i + "] pid=" + it.getProductId()
                        + ", name=" + it.getCachedName() + ", price=" + it.getCachedPrice()
                        + ", qty=" + it.getQuantity());
            }
        } catch (Exception e) {
            android.util.Log.e("CartFragment", "Error logging selectedItems", e);
        }

        Intent intent = new Intent(requireContext(), CheckoutActivity.class);
        intent.putParcelableArrayListExtra("selectedItems", new ArrayList<>(selectedItems));
        startActivity(intent);
    }

    private void showEmptyCart(String message) {
        recyclerCart.setVisibility(View.GONE);
        btnCheckout.setEnabled(false);
        cbSelectItem.setVisibility(View.GONE);

        // Nếu có TextView empty cart
        // tvEmptyCart.setVisibility(View.VISIBLE);
        // tvEmptyCart.setText(message);

        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void showCartData() {
        recyclerCart.setVisibility(View.VISIBLE);
        btnCheckout.setEnabled(true);
        cbSelectItem.setVisibility(View.VISIBLE);

        // Nếu có TextView empty cart
        // tvEmptyCart.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCartDataFromRepository();
    }
}