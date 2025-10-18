package com.example.androidapp.views.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.androidapp.R;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Gắn nút mở menu
        ImageButton menuButton = view.findViewById(R.id.menuButton);
        menuButton.setOnClickListener(this::showPopupMenu);

        return view;
    }

    private void showPopupMenu(View v) {
        PopupMenu popup = new PopupMenu(requireContext(), v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.top_menu, popup.getMenu());

        // ⚙️ Bắt buộc hiển thị icon trong PopupMenu bằng Reflection
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

        // 🎯 Xử lý khi chọn từng item
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
