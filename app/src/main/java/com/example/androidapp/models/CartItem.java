package com.example.androidapp.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class CartItem implements Parcelable {
    private String productId;
    private String userId;
    private String variantId;
    private String variantName;
    private int quantity;
    private boolean isSelected;
    private String cachedName;
    private String cachedImageUrl;
    private double cachedPrice;
    private long addedAt;
    private long updatedAt;

    public CartItem() {}
    public CartItem(String productId, String userId, String variantId,
                    String variantName, int quantity, boolean isSelected,
                    String cachedName, String cachedImageUrl, double cachedPrice,
                    long addedAt, long updatedAt) {
        this.productId = productId;
        this.userId = userId;
        this.variantId = variantId;
        this.variantName = variantName;
        this.quantity = quantity;
        this.isSelected = isSelected;
        this.cachedName = cachedName;
        this.cachedImageUrl = cachedImageUrl;
        this.cachedPrice = cachedPrice;
        this.addedAt = addedAt;
        this.updatedAt = updatedAt;
    }

    public CartItem(String productId, String userId, int quantity) {
        this.productId = productId;
        this.userId = userId;
        this.quantity = quantity;
        this.isSelected = true;
        this.addedAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }
    // Parcelable implementation
    protected CartItem(Parcel in) {
        productId = in.readString();
        userId = in.readString();
        variantId = in.readString();
        variantName = in.readString();
        quantity = in.readInt();
        isSelected = in.readByte() != 0;
        cachedName = in.readString();
        cachedImageUrl = in.readString();
        cachedPrice = in.readDouble();
        addedAt = in.readLong();
        updatedAt = in.readLong();
    }

    public static final Creator<CartItem> CREATOR = new Creator<CartItem>() {
        @Override
        public CartItem createFromParcel(Parcel in) {
            return new CartItem(in);
        }

        @Override
        public CartItem[] newArray(int size) {
            return new CartItem[size];
        }
    };

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getVariantId() {
        return variantId;
    }

    public void setVariantId(String variantId) {
        this.variantId = variantId;
    }

    public String getVariantName() {
        return variantName;
    }

    public void setVariantName(String variantName) {
        this.variantName = variantName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getCachedName() {
        return cachedName;
    }

    public void setCachedName(String cachedName) {
        this.cachedName = cachedName;
    }

    public String getCachedImageUrl() {
        return cachedImageUrl;
    }

    public void setCachedImageUrl(String cachedImageUrl) {
        this.cachedImageUrl = cachedImageUrl;
    }

    public double getCachedPrice() {
        return cachedPrice;
    }

    public void setCachedPrice(double cachedPrice) {
        this.cachedPrice = cachedPrice;
    }

    public long getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(long addedAt) {
        this.addedAt = addedAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(productId);
        dest.writeString(userId);
        dest.writeString(variantId);
        dest.writeString(variantName);
        dest.writeInt(quantity);
        dest.writeByte((byte) (isSelected ? 1 : 0));
        dest.writeString(cachedName);
        dest.writeString(cachedImageUrl);
        dest.writeDouble(cachedPrice);
        dest.writeLong(addedAt);
        dest.writeLong(updatedAt);
    }
    // Helper methods
    public double getTotalPrice() {
        return cachedPrice * quantity;
    }

    public void incrementQuantity() {
        this.quantity++;
        this.updatedAt = System.currentTimeMillis();
    }

    public void decrementQuantity() {
        if (this.quantity > 1) {
            this.quantity--;
            this.updatedAt = System.currentTimeMillis();
        }
    }

}
