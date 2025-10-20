package com.example.androidapp.views.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.androidapp.R;
import com.example.androidapp.models.Product;
import com.example.androidapp.views.adapters.ProductGridAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private GridView gridViewProducts;
    private ProductGridAdapter productAdapter;
    private List<Product> productList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Gắn nút mở menu
        ImageButton menuButton = view.findViewById(R.id.menuButton);
        menuButton.setOnClickListener(this::showPopupMenu);

        // Khởi tạo GridView
        gridViewProducts = view.findViewById(R.id.gridViewProducts);
        productList = new ArrayList<>();
        productAdapter = new ProductGridAdapter(requireContext(), productList);
        gridViewProducts.setAdapter(productAdapter);

        // Tải sản phẩm từ Firebase
        loadProductsFromFirebase();

        return view;
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
