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
import com.example.androidapp.repositories.CartRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private List<CartItem> cartList;
    private OnCartChangeListener listener;

    public interface OnCartChangeListener {
        void onCartUpdated();
    }

    public CartAdapter(List<CartItem> cartList, OnCartChangeListener listener) {
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
        CartItem item = cartList.get(position);

        // Load ảnh sản phẩm
        if (item.getCachedImageUrl() != null && !item.getCachedImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(item.getCachedImageUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(holder.imgProduct);
        }

        holder.tvName.setText(item.getCachedName());
        holder.tvVariant.setText(item.getVariantName() != null ? item.getVariantName() : "Mặc định");

        // Ẩn giá gốc vì CartItem không có thông tin này (view có thể không tồn tại
        // trong layout)
        if (holder.tvPriceOriginal != null) {
            holder.tvPriceOriginal.setVisibility(View.GONE);
        }
        holder.tvPrice.setText(String.format("%,.0fđ", item.getCachedPrice()));
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));

        // QUAN TRỌNG: Remove listener cũ trước khi set checked để tránh trigger không
        // cần thiết
        holder.cbSelectItem.setOnCheckedChangeListener(null);
        holder.cbSelectItem.setChecked(item.isSelected());

        // Checkbox change listener - Cập nhật total khi check/uncheck
        holder.cbSelectItem.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setSelected(isChecked);
            if (listener != null) {
                listener.onCartUpdated(); // Cập nhật total ngay khi check/uncheck
            }
        });

        // Decrease quantity - Cập nhật total khi giảm số lượng
        holder.tvMinus.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                item.setQuantity(item.getQuantity() - 1);
                holder.tvQuantity.setText(String.valueOf(item.getQuantity()));

                // Chỉ cập nhật total nếu item đang được chọn
                if (item.isSelected() && listener != null) {
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
            holder.tvQuantity.setText(String.valueOf(item.getQuantity()));

            // Chỉ cập nhật total nếu item đang được chọn
            if (item.isSelected() && listener != null) {
                listener.onCartUpdated();
            }
        });

        // Click variant để chọn biến thể
        holder.tvVariant.setOnClickListener(v -> {
            Toast.makeText(v.getContext(),
                    "Chọn biến thể: " + item.getVariantName(),
                    Toast.LENGTH_SHORT).show();
            // TODO: Show variant selection dialog
        });
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    // Tính tổng tiền các item được chọn
    public double getTotalPrice() {
        double total = 0;
        for (CartItem item : cartList) {
            if (item.isSelected()) {
                total += item.getTotalPrice(); // item.getPrice() * item.getQuantity()
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
        for (CartItem item : cartList) {
            if (item.isSelected()) {
                count++;
            }
        }
        return count;
    }

    // Lấy danh sách item được chọn
    public List<CartItem> getSelectedItems() {
        List<CartItem> selectedItems = new ArrayList<>();
        for (CartItem item : cartList) {
            if (item.isSelected()) {
                selectedItems.add(item);
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
        for (CartItem item : cartList) {
            item.setSelected(isSelected);
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