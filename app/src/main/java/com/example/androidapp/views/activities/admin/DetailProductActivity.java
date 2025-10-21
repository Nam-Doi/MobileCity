package com.example.androidapp.views.activities.admin; // Gói của Admin

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.example.androidapp.R;
import com.example.androidapp.models.Product;
import com.example.androidapp.models.ProductVariant;
// SỬA: Import Adapter ảnh mới
import com.example.androidapp.views.adapters.ImageSliderAdapter;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.NumberFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class DetailProductActivity extends AppCompatActivity {

    // Views
    private ViewPager2 viewPagerImages;
    private TextView tvProductName, tvProductBrand, tvProductPrice, tvProductStock;
    private ChipGroup chipGroupColor, chipGroupRam, chipGroupStorage;
    private LinearLayout llSpecifications;
    private Toolbar toolbar;

    // Data
    private Product product;
    private List<ProductVariant> variants;
    private ProductVariant selectedVariant;

    // SỬA: Khai báo biến adapter ảnh
    private ImageSliderAdapter imageAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_product_admin); // Dùng layout admin đã sửa

        product = (Product) getIntent().getSerializableExtra("product");
        if (product == null || product.getVariants() == null || product.getVariants().isEmpty()) {
            Toast.makeText(this, "Lỗi: Không thể tải sản phẩm", Toast.LENGTH_SHORT).show();
            finish(); return;
        }
        variants = product.getVariants();

        initViews();
        populateStaticInfo();
        populateChips();
        setDefaultVariant();
    }

    private void initViews() {
        viewPagerImages = findViewById(R.id.viewPagerImages);
        tvProductName = findViewById(R.id.tvProductName);
        tvProductBrand = findViewById(R.id.tvProductBrand);
        tvProductPrice = findViewById(R.id.tvProductPrice);
        tvProductStock = findViewById(R.id.tvProductStock);
        chipGroupColor = findViewById(R.id.chipGroupColor);
        chipGroupRam = findViewById(R.id.chipGroupRam);
        chipGroupStorage = findViewById(R.id.chipGroupStorage);
        llSpecifications = findViewById(R.id.llSpecifications);

        // SỬA: Khởi tạo và gán adapter cho ViewPager2
        imageAdapter = new ImageSliderAdapter(this);
        viewPagerImages.setAdapter(imageAdapter);
    }

    // SỬA: Hàm updateUiForSelectedVariant (bỏ comment phần ảnh)
    private void updateUiForSelectedVariant() {
        if (selectedVariant == null) return;

        Locale locale = new Locale("vi", "VN"); NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(locale);
        tvProductPrice.setText(currencyFormatter.format(selectedVariant.getPrice()));
        if (selectedVariant.getStock() > 0) { tvProductStock.setText("Còn: " + selectedVariant.getStock()); } else { tvProductStock.setText("Hết hàng"); }

        // SỬA: Bỏ comment và gọi hàm setImages của adapter
        if (selectedVariant.getImageUrls() != null) {
            imageAdapter.setImages(selectedVariant.getImageUrls()); // Cập nhật ảnh cho ViewPager
        } else {
            imageAdapter.setImages(null); // Xóa ảnh nếu variant không có ảnh
        }
    }

    // (Các hàm khác giữ nguyên y hệt)
    private void populateStaticInfo() {
        tvProductName.setText(product.getName());
        tvProductBrand.setText(product.getBrand());
        Map<String, String> specs = product.getSpecifications();
        if (specs != null) {
            llSpecifications.removeAllViews();
            for (Map.Entry<String, String> entry : specs.entrySet()) {
                if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                    LinearLayout row = new LinearLayout(this);
                    row.setPadding(0, 8, 0, 8);
                    TextView key = new TextView(this);
                    key.setText(entry.getKey() + ": ");
                    key.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                    key.setTypeface(null, Typeface.BOLD);
                    TextView value = new TextView(this);
                    value.setText(entry.getValue());
                    value.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f));
                    row.addView(key); row.addView(value);
                    llSpecifications.addView(row);
                }
            }
        }
    }
    private void populateChips() {
        Set<String> colors = new HashSet<>(); Set<String> rams = new HashSet<>(); Set<String> storages = new HashSet<>();
        for (ProductVariant variant : variants) {
            if (variant.getColor() != null) colors.add(variant.getColor());
            if (variant.getRam() != null) rams.add(variant.getRam());
            if (variant.getStorage() != null) storages.add(variant.getStorage());
        }
        for (String color : colors) {
            Chip chip = createChip(color);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> { if (isChecked) findAndUpdateVariant(); });
            chipGroupColor.addView(chip);
        }
        for (String ram : rams) {
            Chip chip = createChip(ram);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> { if (isChecked) findAndUpdateVariant(); });
            chipGroupRam.addView(chip);
        }
        for (String storage : storages) {
            Chip chip = createChip(storage);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> { if (isChecked) findAndUpdateVariant(); });
            chipGroupStorage.addView(chip);
        }
    }
    private Chip createChip(String text) {
        Chip chip = new Chip(this); chip.setText(text); chip.setCheckable(true); chip.setClickable(true); return chip;
    }
    private void setDefaultVariant() {
        selectedVariant = variants.get(0);
        checkChip(chipGroupColor, selectedVariant.getColor());
        checkChip(chipGroupRam, selectedVariant.getRam());
        checkChip(chipGroupStorage, selectedVariant.getStorage());
        updateUiForSelectedVariant();
    }
    private void checkChip(ChipGroup group, String text) {
        if (text == null) return;
        for (int i = 0; i < group.getChildCount(); i++) {
            Chip chip = (Chip) group.getChildAt(i);
            if (chip.getText().toString().equals(text)) { chip.setChecked(true); return; }
        }
    }
    private void findAndUpdateVariant() {
        String color = getSelectedChipText(chipGroupColor); String ram = getSelectedChipText(chipGroupRam); String storage = getSelectedChipText(chipGroupStorage);
        for (ProductVariant variant : variants) {
            boolean colorMatch = (color == null || color.isEmpty()) || color.equals(variant.getColor());
            boolean ramMatch = (ram == null || ram.isEmpty()) || ram.equals(variant.getRam());
            boolean storageMatch = (storage == null || storage.isEmpty()) || storage.equals(variant.getStorage());
            if (colorMatch && ramMatch && storageMatch) { selectedVariant = variant; updateUiForSelectedVariant(); return; }
        }
        selectedVariant = null; tvProductPrice.setText("Không có phiên bản này"); tvProductStock.setText("Hết hàng");
    }
    private String getSelectedChipText(ChipGroup group) {
        int checkedId = group.getCheckedChipId();
        if (checkedId != View.NO_ID) { Chip chip = group.findViewById(checkedId); return chip.getText().toString(); }
        return null;
    }
}