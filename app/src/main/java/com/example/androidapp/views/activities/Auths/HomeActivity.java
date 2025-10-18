package com.example.androidapp.views.activities.Auths;

import android.os.Bundle;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.androidapp.R;
import com.example.androidapp.views.fragments.CartFragment;
import com.example.androidapp.views.fragments.HomeFragment;
import com.example.androidapp.views.fragments.NotifyFragment;
import com.example.androidapp.views.fragments.UserFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    ImageButton menuButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_user);

        // Ánh xạ
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // --- Hiển thị Fragment mặc định khi vào app ---
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_container, new HomeFragment())
                .commit();

        // --- Xử lý khi bấm item ở Bottom Navigation ---
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_cart) {
                selectedFragment = new CartFragment();
            } else if (itemId == R.id.nav_notify) {
                selectedFragment = new NotifyFragment();
            } else if (itemId == R.id.nav_user) {
                selectedFragment = new UserFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.main_container, selectedFragment)
                        .commit();
                return true;
            }
            return false;
        });
    }

    private void showPopupMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.top_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_home) {
                Toast.makeText(this, "Trang chủ", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.menu_phone) {
                Toast.makeText(this, "Điện thoại", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.menu_accessory) {
                Toast.makeText(this, "Phụ kiện", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.menu_other) {
                Toast.makeText(this, "Khác", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });

        popup.show();
    }
}
