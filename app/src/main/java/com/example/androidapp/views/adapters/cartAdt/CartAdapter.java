package com.example.androidapp.views.adapters.cartAdt;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.androidapp.R;
import com.example.androidapp.models.CartItem;
import com.example.androidapp.models.CartItemDisplay;
import com.example.androidapp.models.Product;
import com.example.androidapp.models.ProductVariant;
import com.example.androidapp.repositories.CartRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private List<CartItemDisplay> cartList;
    private OnCartChangeListener listener;

    public interface OnCartChangeListener {
        void onCartUpdated();
    }

    public CartAdapter(List<CartItemDisplay> cartList, OnCartChangeListener listener) {
        this.cartList = cartList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cart_items, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartAdapter.CartViewHolder holder, int position) {
        CartItemDisplay display = cartList.get(position);
        CartItem item = display.getCartItem();

        // Load ảnh sản phẩm
        String imageUrl = display.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(holder.imgProduct);
        }

        holder.tvName.setText(display.getProductName());
        holder.tvVariant.setText(display.getVariantName() != null ? display.getVariantName() : "Mặc định");

        holder.tvPrice.setText(String.format("%,.0fđ", display.getCurrentPrice()));
        holder.tvQuantity.setText(String.valueOf(display.getQuantity()));

        // cần thiết
        holder.cbSelectItem.setOnCheckedChangeListener(null);
        holder.cbSelectItem.setChecked(display.isSelected());

        // Checkbox change listener - Cập nhật total khi check/uncheck
        holder.cbSelectItem.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setSelected(isChecked);
            if (listener != null) {
                listener.onCartUpdated(); // Cập nhật total ngay khi check/uncheck
            }
        });

        // Decrease quantity - Cập nhật total khi giảm số lượng
        holder.tvMinus.setOnClickListener(v -> {
            if (display.getQuantity() > 1) {
                // Cập nhật quantity trong CartItem
                item.setQuantity(item.getQuantity() - 1);
                holder.tvQuantity.setText(String.valueOf(display.getQuantity()));

                // Chỉ cập nhật total nếu item đang được chọn
                if (display.isSelected() && listener != null) {
                    listener.onCartUpdated();
                }
            } else {
                // Hiển thị dialog xác nhận xóa
                showDeleteConfirmDialog(v, item, holder.getAdapterPosition());
            }
        });

        // Increase quantity - Cập nhật total khi tăng số lượng
        holder.tvPlus.setOnClickListener(v -> {
            item.setQuantity(item.getQuantity() + 1);
            holder.tvQuantity.setText(String.valueOf(display.getQuantity()));

            // Chỉ cập nhật total nếu item đang được chọn
            if (display.isSelected() && listener != null) {
                listener.onCartUpdated();
            }
        });

        // Click variant để chọn biến thể
        holder.tvVariant.setOnClickListener(v -> {
            android.util.Log.d("CartAdapter", "Variant clicked for position: " + holder.getAdapterPosition());
            android.util.Log.d("CartAdapter", "Variant text: " + holder.tvVariant.getText());
            showVariantSelectionDialog(v.getContext(), item, holder.getAdapterPosition());
        });

        // Đảm bảo TextView có thể click
        holder.tvVariant.setClickable(true);
        holder.tvVariant.setFocusable(true);
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    // Tính tổng tiền các item được chọn
    public double getTotalPrice() {
        double total = 0;
        for (CartItemDisplay display : cartList) {
            if (display.isSelected()) {
                total += display.getTotalPrice();
            }
        }
        return total;
    }

    public int getTotalQuantity() {
        int count = cartList.size();
        return count;

    }

    // Đếm số lượng item được chọn
    public int getSelectedItemCount() {
        int count = 0;
        for (CartItemDisplay display : cartList) {
            if (display.isSelected()) {
                count++;
            }
        }
        return count;
    }

    // Lấy danh sách item được chọn
    public List<CartItem> getSelectedItems() {
        List<CartItem> selectedItems = new ArrayList<>();
        for (CartItemDisplay display : cartList) {
            if (display.isSelected()) {
                selectedItems.add(display.getCartItem());
            }
        }
        return selectedItems;
    }

    // Xóa item
    public void removeItem(int position) {
        cartList.remove(position);
        notifyItemRemoved(position);
        if (listener != null) {
            listener.onCartUpdated();
        }
    }

    // Select tất cả items
    public void selectAll(boolean isSelected) {
        for (CartItemDisplay display : cartList) {
            display.getCartItem().setSelected(isSelected);
        }
        notifyDataSetChanged();
        if (listener != null) {
            listener.onCartUpdated();
        }
    }

    // Hiển thị dialog xác nhận xóa
    private void showDeleteConfirmDialog(View view, CartItem item, int position) {
        new android.app.AlertDialog.Builder(view.getContext())
                .setTitle("Xóa sản phẩm")
                .setMessage("Bạn có muốn xóa bỏ sản phẩm này?")
                .setPositiveButton("Đồng ý", (dialog, which) -> {
                    // Xóa trên Firestore trước, sau đó xóa local nếu thành công
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user == null) {
                        Toast.makeText(view.getContext(), "Vui lòng đăng nhập để xóa sản phẩm", Toast.LENGTH_SHORT)
                                .show();
                        return;
                    }

                    CartRepository repo = new CartRepository();
                    String userId = user.getUid();
                    String productId = item.getProductId();
                    String variantId = item.getVariantId();

                    repo.removeItem(userId, productId, variantId, new CartRepository.OnCartOperationListener() {
                        @Override
                        public void onSuccess(String message) {
                            // Tìm lại vị trí item trong danh sách (position có thể đã thay đổi)
                            int idx = cartList.indexOf(item);
                            if (idx >= 0) {
                                removeItem(idx);
                            }
                            Toast.makeText(view.getContext(), "Đã xóa sản phẩm khỏi giỏ hàng", Toast.LENGTH_SHORT)
                                    .show();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(view.getContext(), "Không thể xóa sản phẩm: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Không", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    // Hiển thị dialog chọn variant
    private void showVariantSelectionDialog(android.content.Context context, CartItem item, int position) {
        android.util.Log.d("CartAdapter", "showVariantSelectionDialog called for position: " + position);
        CartItemDisplay display = cartList.get(position);
        Product product = display.getProduct();

        android.util.Log.d("CartAdapter", "Product: " + (product != null ? "not null" : "null"));
        if (product != null) {
            android.util.Log.d("CartAdapter",
                    "Variants: " + (product.getVariants() != null ? product.getVariants().size() : "null"));
        }

        if (product != null && product.getVariants() != null && !product.getVariants().isEmpty()) {
            android.util.Log.d("CartAdapter", "Showing variant dialog directly");
            showVariantDialog(context, product, item, position);
        } else {
            // Nếu không có product info, load từ Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("phones")
                    .document(item.getProductId())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Product loadedProduct = documentSnapshot.toObject(Product.class);
                            if (loadedProduct != null && loadedProduct.getVariants() != null
                                    && !loadedProduct.getVariants().isEmpty()) {
                                showVariantDialog(context, loadedProduct, item, position);
                            } else {
                                Toast.makeText(context, "Không có biến thể nào", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Lỗi tải thông tin sản phẩm", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void showVariantDialog(android.content.Context context, Product product, CartItem item, int position) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        builder.setTitle("Chọn biến thể");

        // Tạo danh sách các variant
        List<ProductVariant> variants = product.getVariants();
        String[] variantNames = new String[variants.size()];
        for (int i = 0; i < variants.size(); i++) {
            ProductVariant variant = variants.get(i);
            variantNames[i] = variant.getColor() + " - " + variant.getRam() + " - " + variant.getStorage() +
                    " (" + String.format("%,.0fđ", variant.getPrice()) + ")";
        }

        // Tìm variant hiện tại
        int currentSelection = 0;
        for (int i = 0; i < variants.size(); i++) {
            if (variants.get(i).getId().equals(item.getVariantId())) {
                currentSelection = i;
                break;
            }
        }

        builder.setSingleChoiceItems(variantNames, currentSelection, (dialog, which) -> {
            ProductVariant selectedVariant = variants.get(which);

            // Lưu oldVariantId trước khi thay đổi item
            String oldVariantId = item.getVariantId();

            // Cập nhật thông tin variant trong CartItem (local)
            item.setVariantId(selectedVariant.getId());
            item.setVariantName(selectedVariant.getColor() + " - " + selectedVariant.getRam() + " - "
                    + selectedVariant.getStorage());
            item.setCachedPrice(selectedVariant.getPrice());

            // Cập nhật ảnh nếu có
            if (selectedVariant.getImageUrls() != null && !selectedVariant.getImageUrls().isEmpty()) {
                item.setCachedImageUrl(selectedVariant.getImageUrls().get(0));
            }

            // Cập nhật CartItemDisplay
            CartItemDisplay display = cartList.get(position);
            display.setProduct(product); // Cập nhật product với variant mới
            // Cập nhật CartItem trong display
            display.getCartItem().setVariantId(selectedVariant.getId());
            display.getCartItem().setVariantName(selectedVariant.getColor() + " - " + selectedVariant.getRam() + " - "
                    + selectedVariant.getStorage());
            display.getCartItem().setCachedPrice(selectedVariant.getPrice());
            if (selectedVariant.getImageUrls() != null && !selectedVariant.getImageUrls().isEmpty()) {
                display.getCartItem().setCachedImageUrl(selectedVariant.getImageUrls().get(0));
            }

            // Cập nhật trên Firestore
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                CartRepository repo = new CartRepository();
                String newVariantId = selectedVariant.getId();
                repo.updateVariant(user.getUid(), item.getProductId(), oldVariantId, newVariantId,
                        item.getVariantName(), selectedVariant.getPrice(),
                        selectedVariant.getImageUrls() != null && !selectedVariant.getImageUrls().isEmpty()
                                ? selectedVariant.getImageUrls().get(0)
                                : null,
                        new CartRepository.OnCartOperationListener() {
                            @Override
                            public void onSuccess(String message) {
                                // Cập nhật UI
                                notifyItemChanged(position);
                                if (listener != null) {
                                    listener.onCartUpdated();
                                }
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(context, "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }

            dialog.dismiss();
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbSelectItem;
        ImageView imgProduct;
        TextView tvName, tvVariant, tvPriceOriginal, tvPrice, tvQuantity, tvMinus, tvPlus;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            cbSelectItem = itemView.findViewById(R.id.cbSelectItem);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvName = itemView.findViewById(R.id.tvName);
            tvVariant = itemView.findViewById(R.id.tvVariant);
            tvPriceOriginal = itemView.findViewById(R.id.tvPriceOriginal);
            if (tvPriceOriginal != null) {
                tvPriceOriginal.setPaintFlags(tvPriceOriginal.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvMinus = itemView.findViewById(R.id.tvMinus);
            tvPlus = itemView.findViewById(R.id.tvPlus);
        }
    }
}