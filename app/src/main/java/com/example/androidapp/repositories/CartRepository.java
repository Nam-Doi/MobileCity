package com.example.androidapp.repositories;

import android.util.Log;

import androidx.annotation.NonNull;
import com.example.androidapp.models.CartItem;
import com.example.androidapp.models.CartItemDisplay;
import com.example.androidapp.models.Product;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartRepository {
    private final FirebaseFirestore db;
    private static final String COLLECTION_USERS = "users";
    private static final String SUBCOLLECTION_CARTS = "carts";
    private static final String COLLECTION_PRODUCTS = "phones";

    public CartRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Lấy reference đến cart subcollection của user
     */
    private DocumentReference getCartItemRef(@NonNull String userId, @NonNull String cartItemId) {
        return db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(SUBCOLLECTION_CARTS)
                .document(cartItemId);
    }

    /**
     * Thêm sản phẩm vào giỏ hàng
     * Cấu trúc: users/{userId}/carts/{productId} hoặc
     * users/{userId}/carts/{productId_variantId}
     */
    public void addToCart(@NonNull String userId, @NonNull Product product,
            int quantity, String variantId, String variantName,
            OnCartOperationListener listener) {

        Log.d("CartRepository", "addToCart called for user: " + userId + ", product: " + product.getName());

        // Tạo cartItemId: nếu có variant thì thêm vào
        String cartItemId = variantId != null ? product.getId() + "_" + variantId : product.getId();

        Log.d("CartRepository", "Cart item ID: " + cartItemId);
        DocumentReference cartRef = getCartItemRef(userId, cartItemId);

        // Kiểm tra sản phẩm đã tồn tại chưa
        cartRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Sản phẩm đã có -> Tăng số lượng
                Long currentQuantity = documentSnapshot.getLong("quantity");
                int newQuantity = (currentQuantity != null ? currentQuantity.intValue() : 0) + quantity;

                Map<String, Object> updates = new HashMap<>();
                updates.put("quantity", newQuantity);
                updates.put("updatedAt", System.currentTimeMillis());

                cartRef.update(updates)
                        .addOnSuccessListener(aVoid -> listener.onSuccess("Đã cập nhật số lượng trong giỏ hàng"))
                        .addOnFailureListener(listener::onFailure);
            } else {
                // Sản phẩm chưa có -> Thêm mới
                Map<String, Object> cartData = new HashMap<>();
                cartData.put("productId", product.getId());
                cartData.put("userId", userId);
                cartData.put("variantId", variantId);
                cartData.put("variantName", variantName);
                cartData.put("quantity", quantity);
                cartData.put("isSelected", true);

                // Cache thông tin từ Product/Variant để hiển thị nhanh
                cartData.put("cachedName", product.getName());
                cartData.put("cachedBrand", product.getBrand());

                // Nếu variant được truyền lên, ưu tiên lấy giá và ảnh từ variant đó
                double cachedPrice = 0.0;
                String cachedImageUrl = null;
                if (variantId != null && product.getVariants() != null) {
                    for (var v : product.getVariants()) {
                        if (variantId.equals(v.getId())) {
                            try {
                                cachedPrice = v.getPrice();
                            } catch (Exception ignored) {
                            }
                            if (v.getImageUrls() != null && !v.getImageUrls().isEmpty()) {
                                cachedImageUrl = v.getImageUrls().get(0);
                            }
                            break;
                        }
                    }
                }

                // Fallback: nếu không lấy được từ variant thì dùng product-level
                if (cachedPrice <= 0) {
                    try {
                        cachedPrice = product.getPrice();
                    } catch (Exception ignored) {
                        cachedPrice = 0.0;
                    }
                }
                if (cachedImageUrl == null) {
                    if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
                        cachedImageUrl = product.getImageUrls().get(0);
                    }
                }

                cartData.put("cachedPrice", cachedPrice);
                if (cachedImageUrl != null)
                    cartData.put("cachedImageUrl", cachedImageUrl);

                cartData.put("addedAt", System.currentTimeMillis());
                cartData.put("updatedAt", System.currentTimeMillis());
                cartRef.set(cartData)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("CartRepository", "Added cart item for user=" + userId + " id=" + cartItemId);
                            listener.onSuccess("Đã thêm sản phẩm vào giỏ hàng");
                        })
                        .addOnFailureListener(e -> {
                            Log.e("CartRepository", "Failed to add cart item user=" + userId + " id=" + cartItemId, e);
                            listener.onFailure(e);
                        });
            }
        }).addOnFailureListener(listener::onFailure);
    }

    /**
     * Lấy tất cả items trong giỏ hàng (JOIN với Product để lấy thông tin mới nhất)
     */
    public void getCartItems(@NonNull String userId, OnCartItemsLoadedListener listener) {
        db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(SUBCOLLECTION_CARTS)
                .orderBy("addedAt", Query.Direction.DESCENDING)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        listener.onFailure(error);
                        return;
                    }

                    if (querySnapshot == null || querySnapshot.isEmpty()) {
                        Log.d("CartRepository", "getCartItems: no cart documents for user=" + userId);
                        listener.onCartItemsLoaded(new ArrayList<>());
                        return;
                    }

                    List<CartItem> cartItems = querySnapshot.toObjects(CartItem.class);
                    Log.d("CartRepository", "getCartItems: loaded " + cartItems.size() + " items for user=" + userId);

                    // JOIN với Product để lấy thông tin đầy đủ và mới nhất
                    List<CartItemDisplay> displayItems = new ArrayList<>();
                    int[] counter = { 0 };
                    final int totalItems = cartItems.size();

                    for (CartItem cartItem : cartItems) {
                        db.collection(COLLECTION_PRODUCTS)
                                .document(cartItem.getProductId())
                                .get()
                                .addOnSuccessListener(productDoc -> {
                                    Product product = productDoc.exists() ? productDoc.toObject(Product.class) : null;
                                    if (product != null) {
                                        product.setId(productDoc.getId()); // QUAN TRỌNG: Set ID cho Product
                                    }

                                    displayItems.add(new CartItemDisplay(cartItem, product));

                                    counter[0]++;
                                    if (counter[0] == totalItems) {
                                        listener.onCartItemsLoaded(displayItems);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    // Nếu không load được Product, dùng cached data
                                    displayItems.add(new CartItemDisplay(cartItem, null));

                                    counter[0]++;
                                    if (counter[0] == totalItems) {
                                        listener.onCartItemsLoaded(displayItems);
                                    }
                                });
                    }
                });
    }

    /**
     * Lấy số lượng items trong giỏ hàng
     */
    public void getCartItemCount(@NonNull String userId, OnCartCountListener listener) {
        db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(SUBCOLLECTION_CARTS)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        listener.onCountLoaded(0);
                        return;
                    }

                    int count = querySnapshot != null ? querySnapshot.size() : 0;
                    listener.onCountLoaded(count);
                });
    }

    /**
     * Cập nhật số lượng sản phẩm
     */
    public void updateQuantity(@NonNull String userId, @NonNull String productId,
            String variantId, int newQuantity,
            OnCartOperationListener listener) {
        String cartItemId = variantId != null ? productId + "_" + variantId : productId;

        Map<String, Object> updates = new HashMap<>();
        updates.put("quantity", newQuantity);
        updates.put("updatedAt", System.currentTimeMillis());

        getCartItemRef(userId, cartItemId)
                .update(updates)
                .addOnSuccessListener(aVoid -> listener.onSuccess("Đã cập nhật số lượng"))
                .addOnFailureListener(listener::onFailure);
    }

    /**
     * Xóa một item khỏi giỏ hàng
     */
    public void removeItem(@NonNull String userId, @NonNull String productId,
            String variantId, OnCartOperationListener listener) {
        String cartItemId = variantId != null ? productId + "_" + variantId : productId;

        getCartItemRef(userId, cartItemId)
                .delete()
                .addOnSuccessListener(aVoid -> listener.onSuccess("Đã xóa khỏi giỏ hàng"))
                .addOnFailureListener(listener::onFailure);
    }

    /**
     * Xóa nhiều items cùng lúc
     */
    public void removeMultipleItems(@NonNull String userId, @NonNull List<String> cartItemIds,
            OnCartOperationListener listener) {
        int[] counter = { 0 };
        int total = cartItemIds.size();

        for (String cartItemId : cartItemIds) {
            getCartItemRef(userId, cartItemId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        counter[0]++;
                        if (counter[0] == total) {
                            listener.onSuccess("Đã xóa " + total + " sản phẩm");
                        }
                    })
                    .addOnFailureListener(listener::onFailure);
        }
    }

    /**
     * Cập nhật trạng thái checkbox (selected)
     */
    public void updateSelection(@NonNull String userId, @NonNull String productId,
            String variantId, boolean isSelected,
            OnCartOperationListener listener) {
        String cartItemId = variantId != null ? productId + "_" + variantId : productId;

        getCartItemRef(userId, cartItemId)
                .update("isSelected", isSelected)
                .addOnSuccessListener(aVoid -> listener.onSuccess(""))
                .addOnFailureListener(listener::onFailure);
    }

    /**
     * Chọn/bỏ chọn tất cả items
     */
    public void selectAll(@NonNull String userId, boolean isSelected,
            OnCartOperationListener listener) {
        db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(SUBCOLLECTION_CARTS)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        listener.onSuccess("Giỏ hàng trống");
                        return;
                    }

                    int[] counter = { 0 };
                    int total = querySnapshot.size();

                    for (var doc : querySnapshot.getDocuments()) {
                        doc.getReference().update("isSelected", isSelected)
                                .addOnSuccessListener(aVoid -> {
                                    counter[0]++;
                                    if (counter[0] == total) {
                                        listener.onSuccess("Đã cập nhật");
                                    }
                                })
                                .addOnFailureListener(listener::onFailure);
                    }
                })
                .addOnFailureListener(listener::onFailure);
    }

    /**
     * Xóa toàn bộ giỏ hàng
     */
    public void clearCart(@NonNull String userId, OnCartOperationListener listener) {
        db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(SUBCOLLECTION_CARTS)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        listener.onSuccess("Giỏ hàng đã trống");
                        return;
                    }

                    int[] counter = { 0 };
                    int total = querySnapshot.size();

                    for (var doc : querySnapshot.getDocuments()) {
                        doc.getReference().delete()
                                .addOnSuccessListener(aVoid -> {
                                    counter[0]++;
                                    if (counter[0] == total) {
                                        listener.onSuccess("Đã xóa toàn bộ giỏ hàng");
                                    }
                                })
                                .addOnFailureListener(listener::onFailure);
                    }
                })
                .addOnFailureListener(listener::onFailure);
    }

    /**
     * Xóa các items đã chọn (sau khi đặt hàng thành công)
     */
    public void removeSelectedItems(@NonNull String userId, OnCartOperationListener listener) {
        db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(SUBCOLLECTION_CARTS)
                .whereEqualTo("isSelected", true)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        listener.onSuccess("Không có sản phẩm được chọn");
                        return;
                    }

                    int[] counter = { 0 };
                    int total = querySnapshot.size();

                    for (var doc : querySnapshot.getDocuments()) {
                        doc.getReference().delete()
                                .addOnSuccessListener(aVoid -> {
                                    counter[0]++;
                                    if (counter[0] == total) {
                                        listener.onSuccess("Đã xóa " + total + " sản phẩm");
                                    }
                                })
                                .addOnFailureListener(listener::onFailure);
                    }
                })
                .addOnFailureListener(listener::onFailure);
    }

    /**
     * Cập nhật variant của một item trong giỏ hàng
     */
    public void updateVariant(@NonNull String userId, @NonNull String productId,
            @NonNull String oldVariantId, @NonNull String newVariantId,
            @NonNull String variantName, double price, String imageUrl,
            OnCartOperationListener listener) {
        String oldCartItemId = productId + "_" + oldVariantId;
        String newCartItemId = productId + "_" + newVariantId;

        // Nếu variant không thay đổi, chỉ cập nhật thông tin
        if (oldVariantId.equals(newVariantId)) {
            Map<String, Object> updates = new HashMap<>();
            updates.put("variantName", variantName);
            updates.put("cachedPrice", price);
            if (imageUrl != null) {
                updates.put("cachedImageUrl", imageUrl);
            }
            updates.put("updatedAt", System.currentTimeMillis());

            getCartItemRef(userId, oldCartItemId)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> listener.onSuccess("Đã cập nhật biến thể"))
                    .addOnFailureListener(listener::onFailure);
            return;
        }

        // Nếu variant thay đổi, cần xóa item cũ và tạo item mới
        DocumentReference oldRef = getCartItemRef(userId, oldCartItemId);
        DocumentReference newRef = getCartItemRef(userId, newCartItemId);

        // Lấy thông tin item cũ
        oldRef.get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists()) {
                listener.onFailure(new Exception("Không tìm thấy item cũ"));
                return;
            }

            // Lấy thông tin từ item cũ
            int quantity = documentSnapshot.getLong("quantity").intValue();
            boolean isSelected = Boolean.TRUE.equals(documentSnapshot.getBoolean("isSelected"));
            String cachedName = documentSnapshot.getString("cachedName");
            long addedAt = documentSnapshot.getLong("addedAt");

            // Tạo dữ liệu cho item mới
            Map<String, Object> newItemData = new HashMap<>();
            newItemData.put("productId", productId);
            newItemData.put("userId", userId);
            newItemData.put("variantId", newVariantId);
            newItemData.put("variantName", variantName);
            newItemData.put("quantity", quantity);
            newItemData.put("isSelected", isSelected);
            newItemData.put("cachedName", cachedName);
            newItemData.put("cachedPrice", price);
            if (imageUrl != null) {
                newItemData.put("cachedImageUrl", imageUrl);
            }
            newItemData.put("addedAt", addedAt);
            newItemData.put("updatedAt", System.currentTimeMillis());

            // Tạo item mới
            newRef.set(newItemData)
                    .addOnSuccessListener(aVoid -> {
                        // Xóa item cũ
                        oldRef.delete()
                                .addOnSuccessListener(aVoid1 -> listener.onSuccess("Đã cập nhật biến thể"))
                                .addOnFailureListener(listener::onFailure);
                    })
                    .addOnFailureListener(listener::onFailure);
        }).addOnFailureListener(listener::onFailure);
    }

    // Callback Interfaces
    public interface OnCartOperationListener {
        void onSuccess(String message);

        void onFailure(Exception e);
    }

    public interface OnCartItemsLoadedListener {
        void onCartItemsLoaded(List<CartItemDisplay> items);

        void onFailure(Exception e);
    }

    public interface OnCartCountListener {
        void onCountLoaded(int count);
    }
}