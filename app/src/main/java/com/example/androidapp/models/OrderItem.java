package com.example.androidapp.models;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import com.google.firebase.firestore.IgnoreExtraProperties;
@IgnoreExtraProperties
public class OrderItem implements Parcelable {

    private String productId;
    private String name;
    private double price;
    private int qty;

    // Bắt buộc phải có constructor rỗng để Firestore đọc dữ liệu
    public OrderItem() {}

    public OrderItem(String productId, String name, double price, int qty) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.qty = qty;
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

    // --- Parcelable Implementation (Code đóng gói) ---

    protected OrderItem(Parcel in) {
        productId = in.readString();
        name = in.readString();
        price = in.readDouble();
        qty = in.readInt();
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
    }
}