package com.example.androidapp.views.fragments;

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
import com.example.androidapp.views.adapters.CartAdapter;

import java.util.ArrayList;
import java.util.List;

public class CartFragment extends Fragment implements CartAdapter.OnCartChangeListener {

    private RecyclerView recyclerCart;
    private TextView tvTotal;
    private Button btnCheckout;
    private List<CartItem> cartList;
    private CartAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Nối fragment với layout tương ứng
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        recyclerCart = view.findViewById(R.id.recyclerCart);
        tvTotal = view.findViewById(R.id.tvTotal);
        btnCheckout = view.findViewById(R.id.btnCheckout);

        cartList = new ArrayList<>();
        cartList.add(new CartItem("Iphone 17 Pro Max", 12000000, 1, R.drawable.iphone_17));
        cartList.add(new CartItem("Iphone 17 Pro Orange", 15000000, 1, R.drawable.iphone17_orange));
        cartList.add(new CartItem("Huawei Pro Max", 20000000, 1, R.drawable.huawei));

        adapter = new CartAdapter(cartList, this);
        recyclerCart.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerCart.setAdapter(adapter);

        updateTotal();

        btnCheckout.setOnClickListener(v ->
                Toast.makeText(getContext(), "Thanh toán " + tvTotal.getText(), Toast.LENGTH_SHORT).show()
        );

        return view;
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
