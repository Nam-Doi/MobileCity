package com.example.androidapp.views.adapters;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
// Xóa Button nếu không dùng
// import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
// Xóa import RecyclerView preview ảnh
// import androidx.recyclerview.widget.LinearLayoutManager;
// import androidx.recyclerview.widget.RecyclerView;

import com.example.androidapp.R;
import com.example.androidapp.models.ProductVariant;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList; // Thêm
import java.util.Collections; // Thêm
import java.util.List;

public class ProductVariantAdapter extends RecyclerView.Adapter<ProductVariantAdapter.VariantViewHolder> {

    private Context context;
    private List<ProductVariant> variantList;

    public ProductVariantAdapter(Context context, List<ProductVariant> variantList) {
        this.context = context;
        this.variantList = variantList;
    }

    @NonNull
    @Override
    public VariantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Đảm bảo item_variant.xml đã được sửa
        View view = LayoutInflater.from(context).inflate(R.layout.item_variant, parent, false);
        return new VariantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VariantViewHolder holder, int position) {
        ProductVariant variant = variantList.get(position);

        // Set text (giữ nguyên)
        holder.editColor.setText(variant.getColor());
        holder.editRam.setText(variant.getRam());
        holder.editStorage.setText(variant.getStorage());
        if (variant.getPrice() > 0) holder.editPrice.setText(String.valueOf(variant.getPrice())); else holder.editPrice.setText("");
        if (variant.getStock() > 0) holder.editStock.setText(String.valueOf(variant.getStock())); else holder.editStock.setText("");

        // Set URL ảnh (nếu có)
        if (variant.getImageUrls() != null && !variant.getImageUrls().isEmpty()) {
            // Hiển thị URL đầu tiên vào EditText
            holder.editImageUrl.setText(variant.getImageUrls().get(0));
        } else {
            holder.editImageUrl.setText("");
        }

        // Nút xóa (giữ nguyên)
        holder.btnDelete.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                variantList.remove(currentPosition);
                notifyItemRemoved(currentPosition);
                notifyItemRangeChanged(currentPosition, variantList.size());
            }
        });
    }

    @Override
    public int getItemCount() {
        return variantList != null ? variantList.size() : 0;
    }

    // Lớp ViewHolder (Đã sửa)
    class VariantViewHolder extends RecyclerView.ViewHolder {

        TextInputEditText editColor, editRam, editStorage, editPrice, editStock;
        ImageButton btnDelete;
        TextInputEditText editImageUrl;

        public VariantViewHolder(@NonNull View itemView) {
            super(itemView);

            editColor = itemView.findViewById(R.id.editVariantColor);
            editRam = itemView.findViewById(R.id.editVariantRam);
            editStorage = itemView.findViewById(R.id.editVariantStorage);
            editPrice = itemView.findViewById(R.id.editVariantPrice);
            editStock = itemView.findViewById(R.id.editVariantStock);
            btnDelete = itemView.findViewById(R.id.btnDeleteVariant);
            editImageUrl = itemView.findViewById(R.id.editVariantImageUrl); // THÊM: Ánh xạ EditText URL

            // TextWatcher (Thêm cho editImageUrl)
            editColor.addTextChangedListener(new CustomTextWatcher(getAdapterPosition(), "color"));
            editRam.addTextChangedListener(new CustomTextWatcher(getAdapterPosition(), "ram"));
            editStorage.addTextChangedListener(new CustomTextWatcher(getAdapterPosition(), "storage"));
            editPrice.addTextChangedListener(new CustomTextWatcher(getAdapterPosition(), "price"));
            editStock.addTextChangedListener(new CustomTextWatcher(getAdapterPosition(), "stock"));
            editImageUrl.addTextChangedListener(new CustomTextWatcher(getAdapterPosition(), "imageUrl")); // THÊM
        }

        // Class CustomTextWatcher (Đã thêm case "imageUrl")
        private class CustomTextWatcher implements TextWatcher {
            private int position;
            private String field;

            // Constructor
            public CustomTextWatcher(int position, String field) {
                this.position = position;
                this.field = field;
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                position = getAdapterPosition();
                if (position == RecyclerView.NO_POSITION || s == null) return;

                // Lấy đúng variant tại vị trí hiện tại
                if (position >= variantList.size()) return; // Kiểm tra an toàn
                ProductVariant variant = variantList.get(position);

                String value = s.toString().trim(); // Trim URL
                try {
                    switch (field) {
                        case "color": variant.setColor(value); break;
                        case "ram": variant.setRam(value); break;
                        case "storage": variant.setStorage(value); break;
                        case "price": variant.setPrice(value.isEmpty() ? 0 : Double.parseDouble(value)); break;
                        case "stock": variant.setStock(value.isEmpty() ? 0 : Integer.parseInt(value)); break;
                        case "imageUrl": // THÊM CASE NÀY
                            // Khởi tạo list nếu cần
                            if (variant.getImageUrls() == null) {
                                variant.setImageUrls(new ArrayList<>());
                            }
                            if (value.isEmpty()) {
                                // Nếu xóa URL, xóa khỏi list
                                if (!variant.getImageUrls().isEmpty()) {
                                    variant.getImageUrls().clear();
                                }
                            } else {
                                // Nếu có URL, ghi đè URL đầu tiên (hoặc thêm nếu list rỗng)
                                // Vì ta chỉ có 1 EditText nên chỉ xử lý ảnh đầu tiên
                                if (variant.getImageUrls().isEmpty()) {
                                    variant.getImageUrls().add(value);
                                } else {
                                    variant.getImageUrls().set(0, value);
                                }
                            }
                            break;
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace(); // In lỗi nếu nhập chữ vào ô số
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace(); // In lỗi nếu có vấn đề với list imageUrls
                }
            }
        }
    }
}