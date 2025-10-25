package com.example.androidapp.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Model hiển thị Cart - JOIN giữa CartItem và Product
 * Sử dụng khi hiển thị trong RecyclerView
 */
public class CartItemDisplay implements Parcelable {
    // Thông tin từ CartItem
    private CartItem cartItem;

    // Thông tin từ Product (JOIN)
    private Product product;

    public CartItemDisplay(CartItem cartItem, Product product) {
        this.cartItem = cartItem;
        this.product = product;
    }

    protected CartItemDisplay(Parcel in) {
        cartItem = in.readParcelable(CartItem.class.getClassLoader());
        // Product implements Serializable, nên cần custom đọc
    }

    public static final Creator<CartItemDisplay> CREATOR = new Creator<CartItemDisplay>() {
        @Override
        public CartItemDisplay createFromParcel(Parcel in) {
            return new CartItemDisplay(in);
        }

        @Override
        public CartItemDisplay[] newArray(int size) {
            return new CartItemDisplay[size];
        }
    };

    // Getters
    public CartItem getCartItem() { return cartItem; }
    public Product getProduct() { return product; }

    // Setters
    public void setCartItem(CartItem cartItem) { this.cartItem = cartItem; }
    public void setProduct(Product product) { this.product = product; }

    // Helper methods để truy cập nhanh
    public String getProductId() {
        return cartItem.getProductId();
    }

    public String getProductName() {
        return product != null ? product.getName() : cartItem.getCachedName();
    }

    public String getImageUrl() {
        // Ưu tiên lấy ảnh từ cachedImageUrl (ảnh của variant đã chọn)
        if (cartItem.getCachedImageUrl() != null && !cartItem.getCachedImageUrl().isEmpty()) {
            return cartItem.getCachedImageUrl();
        }
        
        // Nếu không có cachedImageUrl, tìm ảnh từ variant tương ứng
        if (product != null && product.getVariants() != null && cartItem.getVariantId() != null) {
            for (ProductVariant variant : product.getVariants()) {
                if (variant.getId().equals(cartItem.getVariantId()) && 
                    variant.getImageUrls() != null && !variant.getImageUrls().isEmpty()) {
                    return variant.getImageUrls().get(0);
                }
            }
        }
        
        // Fallback: lấy ảnh chung của sản phẩm
        if (product != null && product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
            return product.getImageUrls().get(0);
        }
        
        return null;
    }

    public double getCurrentPrice() {
        // Ưu tiên lấy giá từ cachedPrice (giá của variant đã chọn)
        if (cartItem.getCachedPrice() > 0) {
            return cartItem.getCachedPrice();
        }
        
        // Nếu không có cachedPrice, tìm giá từ variant tương ứng
        if (product != null && product.getVariants() != null && cartItem.getVariantId() != null) {
            for (ProductVariant variant : product.getVariants()) {
                if (variant.getId().equals(cartItem.getVariantId())) {
                    return variant.getPrice();
                }
            }
        }
        
        // Fallback: lấy giá chung của sản phẩm
        return product != null ? product.getPrice() : 0.0;
    }

    public int getQuantity() {
        return cartItem.getQuantity();
    }

    public boolean isSelected() {
        return cartItem.isSelected();
    }

    public int getAvailableStock() {
        return product != null ? product.getStock() : 0;
    }

    public double getTotalPrice() {
        return getCurrentPrice() * getQuantity();
    }

    public String getBrand() {
        return product != null ? product.getBrand() : "";
    }

    public String getVariantName() {
        return cartItem.getVariantName();
    }

    // Kiểm tra sản phẩm có còn hàng không
    public boolean isInStock() {
        return product != null && product.getStock() >= cartItem.getQuantity();
    }

    // Kiểm tra giá có thay đổi không
    public boolean isPriceChanged() {
        if (product == null) return false;
        return product.getPrice() != cartItem.getCachedPrice();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(cartItem, flags);
        // Xử lý Product serialization nếu cần
    }
}