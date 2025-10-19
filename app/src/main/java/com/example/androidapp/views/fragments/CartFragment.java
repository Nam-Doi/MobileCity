package com.example.androidapp.views.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidapp.R;
import com.example.androidapp.models.CartItem;
import com.example.androidapp.views.activities.carts.CheckoutActivity;
import com.example.androidapp.views.adapters.CartAdapter;

import java.util.ArrayList;
import java.util.List;

public class CartFragment extends Fragment implements CartAdapter.OnCartChangeListener {

    private RecyclerView recyclerCart;
    private TextView tvTotal;
    private Button btnCheckout;
    private List<CartItem> cartList;
    private CartAdapter cartAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Nối fragment với layout tương ứng
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        recyclerCart = view.findViewById(R.id.recyclerCart);
        tvTotal = view.findViewById(R.id.tvTotal);
        btnCheckout = view.findViewById(R.id.btnCheckout);
        loadCartData();
        setupRecyclerView();
        setupListeners();
        return view;
    }
    private void loadCartData() {
        cartList = new ArrayList<>();

        // Dữ liệu mẫu
        cartList.add(new CartItem(
                "1",
                "Sữa rửa mặt hoa cúc Kiehl's Calendula Deep Cleansing Foam...",
                "Màu xám, L",
                R.drawable.iphone_17,
                630720.0,
                378432.0,
                1,
                false
        ));

        cartList.add(new CartItem(
                "2",
                "iPhone 15 Pro Max",
                "Titan Xanh, 256GB",
                R.drawable.iphone_17,
                3500000.0,
                3200000.0,
                2,
                false
        ));
        cartList.add(new CartItem(
                "3",
                "iPhone 15 Pro Max",
                "Titan Xanh, 256GB",
                R.drawable.iphone_17,
                3500000.0,
                3200000.0,
                2,
                false
        ));
        cartList.add(new CartItem(
                "4",
                "iPhone 17 Pro Max",
                "Đen, 512GB",
                R.drawable.iphone_17,
                500000000.0,
                350000000.0,
                3,
                false
        ));
        cartList.add(new CartItem(
                "1",
                "Sữa rửa mặt hoa cúc Kiehl's Calendula Deep Cleansing Foam...",
                "Màu xám, L",
                R.drawable.iphone_17,
                630720.0,
                378432.0,
                1,
                false
        ));
        cartList.add(new CartItem(
                "1",
                "Sữa rửa mặt hoa cúc Kiehl's Calendula Deep Cleansing Foam...",
                "Màu xám, L",
                R.drawable.iphone_17,
                630720.0,
                378432.0,
                1,
                false
        ));
        cartList.add(new CartItem(
                "1",
                "Sữa rửa mặt hoa cúc Kiehl's Calendula Deep Cleansing Foam...",
                "Màu xám, L",
                R.drawable.iphone_17,
                630720.0,
                378432.0,
                1,
                false
        ));
    }

    private void setupRecyclerView() {
        cartAdapter = new CartAdapter(cartList, this);
        recyclerCart.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerCart.setAdapter(cartAdapter);

        updateTotalPrice();
    }

    private void setupListeners() {
        btnCheckout.setOnClickListener(v -> {
            List<CartItem> selectedItems = cartAdapter.getSelectedItems();
            if (selectedItems.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng chọn sản phẩm", Toast.LENGTH_SHORT).show();
            } else {
                proceedToCheckout(selectedItems);
            }
        });
    }

    @Override
    public void onCartUpdated() {
        updateTotalPrice();
    }

    private void updateTotalPrice() {
        double total = cartAdapter.getTotalPrice();
        tvTotal.setText(String.format("%,.0f ₫", total));

        int selectedItemCount = cartAdapter.getSelectedItemCount();
        btnCheckout.setText("Mua hàng (" + selectedItemCount + ")");
    }

    private void proceedToCheckout(List<CartItem> selectedItems) {
        Toast.makeText(requireContext(),
                "Đã chọn " + selectedItems.size() + " sản phẩm - Tổng: " +
                        String.format("%,.0f ₫", cartAdapter.getTotalPrice()),
                Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(requireContext(), CheckoutActivity.class);
        intent.putExtra("selectedItems", new ArrayList<>(selectedItems));
        startActivity(intent);
    }
}
