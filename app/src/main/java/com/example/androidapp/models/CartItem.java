package com.example.androidapp.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class CartItem implements Parcelable {
    private String id;
    private String name;
    private String variant;
    private int imageResId;
    private double priceOriginal;
    private double price;
    private int quantity;
    private boolean isSelected;

    public CartItem(String id, String name, String variant, int imageResId,
                    double priceOriginal, double price, int quantity, boolean isSelected) {
        this.id = id;
        this.name = name;
        this.variant = variant;
        this.imageResId = imageResId;
        this.priceOriginal = priceOriginal;
        this.price = price;
        this.quantity = quantity;
        this.isSelected = isSelected;
    }
    // Constructor từ Parcel
    protected CartItem(Parcel in) {
        id = in.readString();
        name = in.readString();
        variant = in.readString();
        imageResId = in.readInt();
        priceOriginal = in.readDouble();
        price = in.readDouble();
        quantity = in.readInt();
        isSelected = in.readByte() != 0;
    }

    // CREATOR để tạo đối tượng từ Parcel
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(variant);
        dest.writeInt(imageResId);
        dest.writeDouble(priceOriginal);
        dest.writeDouble(price);
        dest.writeInt(quantity);
        dest.writeByte((byte) (isSelected ? 1 : 0));
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getVariant() { return variant; }
    public int getImageResId() { return imageResId; }
    public double getPriceOriginal() { return priceOriginal; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public boolean isSelected() { return isSelected; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setVariant(String variant) { this.variant = variant; }
    public void setImageResId(int imageResId) { this.imageResId = imageResId; }
    public void setPriceOriginal(double priceOriginal) { this.priceOriginal = priceOriginal; }
    public void setPrice(double price) { this.price = price; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setSelected(boolean selected) { isSelected = selected; }

    // Tính tổng tiền cho item này
    public double getTotalPrice() {
        return price * quantity;
    }
}