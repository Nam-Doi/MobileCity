package com.example.androidapp.views.fragments;

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
    private List<CartItem> cartList;
    private CartAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart);
        recyclerCart = findViewById(R.id.recyclerCart);
        tvTotal = findViewById(R.id.tvTotal);
        btnCheckout = findViewById(R.id.btnCheckout);

        cartList = new ArrayList<>();
//        cartList.add(new CartItem("Iphone 17 Pro Max", 12000000, 1, R.drawable.iphone_17));
//        cartList.add(new CartItem("Iphone 17 Pro Orange", 15000000, 1, R.drawable.iphone17_orange));
//        cartList.add(new CartItem("Huawei Pro Max", 20000000, 1, R.drawable.huawei));

        adapter = new CartAdapter(cartList,this);

        recyclerCart.setLayoutManager(new LinearLayoutManager(this));
        recyclerCart.setAdapter(adapter);

        updateTotal();

        btnCheckout.setOnClickListener(v -> {
            Toast.makeText(this, "Thanh toán " + tvTotal.getText(), Toast.LENGTH_SHORT).show();
        });
    }
    @Override
    public void onCartUpdated() {
        updateTotal();
    }

    private void updateTotal() {
        int total = 0;
        for (CartItem item : cartList) {
            total += item.getTotalPrice();
        }
        tvTotal.setText("TOTAL: " + total + " $");
    }
}