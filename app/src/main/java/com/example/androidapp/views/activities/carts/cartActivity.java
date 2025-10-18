package com.example.androidapp.views.activities.carts;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidapp.R;
import com.example.androidapp.models.CartItem;
import com.example.androidapp.views.adapters.CartAdapter;

import java.util.ArrayList;
import java.util.List;
//inplement method moi co the su dung
public class cartActivity extends AppCompatActivity implements CartAdapter.OnCartChangeListener {

    private RecyclerView recyclerCart;
    private TextView tvTotal;
    private Button btnCheckout;
    private CartAdapter cartAdapter;
    private List<CartItem> cartList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        initViews();
        loadCartData();
        setupRecyclerView();
        setupListeners();
    }

    private void initViews() {
        recyclerCart = findViewById(R.id.recyclerCart);
        tvTotal = findViewById(R.id.tvTotal);
        btnCheckout = findViewById(R.id.btnCheckout);
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
        recyclerCart.setLayoutManager(new LinearLayoutManager(this));
        recyclerCart.setAdapter(cartAdapter);

        updateTotalPrice();
    }

    private void setupListeners() {
        btnCheckout.setOnClickListener(v -> {
            List<CartItem> selectedItems = cartAdapter.getSelectedItems();
            if (selectedItems.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn sản phẩm", Toast.LENGTH_SHORT).show();
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
        //cap nhat so luong hang mua oke
        int selectedItemCount = cartAdapter.getSelectedItemCount();
        btnCheckout.setText("Mua hàng(" + selectedItemCount + ")");
    }

    private void proceedToCheckout(List<CartItem> selectedItems) {
        // TODO: Chuyển sang màn hình thanh toán
        Toast.makeText(this,
                "Đã chọn " + selectedItems.size() + " sản phẩm - Tổng: "
                        + String.format("%,.0f ₫", cartAdapter.getTotalPrice()),
                Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, CheckoutActivity.class);
        intent.putExtra("selectedItems", new ArrayList<>(selectedItems));
        startActivity(intent);
    }
}