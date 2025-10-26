package com.example.androidapp.models;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import com.google.firebase.firestore.IgnoreExtraProperties;//Dùng để tránh lưu các biến không mong muốn
@IgnoreExtraProperties
public class OrderItem implements Parcelable {

    private String productId;
    private String name;
    private double price;
    private String cachedImageUrl;
    private int qty;
    private String variantId;
    private String variantName;

    // Bắt buộc phải có constructor rỗng để Firestore đọc dữ liệu
    public OrderItem() {}

    public OrderItem(String productId, String name, double price, String cachedImageUrl, int qty, String variantId, String variantName) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.cachedImageUrl = cachedImageUrl;
        this.qty = qty;
        this.variantId = variantId;
        this.variantName = variantName;
    }


    // --- Getters and Setters ---

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }

    public String getCachedImageUrl() {return cachedImageUrl;}

    public String getVariantId() {return variantId;}

    public void setVariantId(String variantId) {this.variantId = variantId;}

    public String getVariantName() {return variantName;}

    public void setVariantName(String variantName) {this.variantName = variantName;}

    public void setCachedImageUrl(String cachedImageUrl) {this.cachedImageUrl = cachedImageUrl;}
    // --- Parcelable Implementation (Code đóng gói) ---

    protected OrderItem(Parcel in) {
        productId = in.readString();
        name = in.readString();
        price = in.readDouble();
        qty = in.readInt();
        cachedImageUrl = in.readString();
        variantId = in.readString();
        variantName = in.readString();
    }

    public static final Creator<OrderItem> CREATOR = new Creator<OrderItem>() {
        @Override
        public OrderItem createFromParcel(Parcel in) {
            return new OrderItem(in);
        }

        @Override
        public OrderItem[] newArray(int size) {
            return new OrderItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(productId);
        dest.writeString(name);
        dest.writeDouble(price);
        dest.writeInt(qty);
        dest.writeString(cachedImageUrl);
        dest.writeString(variantId);
        dest.writeString(variantName);
    }
}