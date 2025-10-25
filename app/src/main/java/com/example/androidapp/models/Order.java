package com.example.androidapp.models;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import com.google.firebase.Timestamp;
import java.util.List;
import com.google.firebase.firestore.IgnoreExtraProperties;//Dùng để tránh lưu các biến không mong muốn
@IgnoreExtraProperties
public class Order implements Parcelable {

    // --- Fields ---
    private String orderId;
    private String userId;
    private String customerName;
    private String phone;
    private String address;
    private double total;
    private String status;
    private Timestamp createdAt;
    private List<OrderItem> items;
    private boolean cancellationRequested = false;

    // Constructor rỗng
    public Order() {}

    // --- Getters and Setters ---
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    public boolean isCancellationRequested() { return cancellationRequested; }
    public void setCancellationRequested(boolean cancellationRequested) { this.cancellationRequested = cancellationRequested; }

    // --- Parcelable Implementation ---
    protected Order(Parcel in) {
        orderId = in.readString();
        userId = in.readString();
        customerName = in.readString();
        phone = in.readString();
        address = in.readString();
        total = in.readDouble();
        status = in.readString();
        createdAt = in.readParcelable(Timestamp.class.getClassLoader());
        items = in.createTypedArrayList(OrderItem.CREATOR);
        cancellationRequested = in.readByte() != 0;
    }

    public static final Creator<Order> CREATOR = new Creator<Order>() {
        @Override
        public Order createFromParcel(Parcel in) {
            return new Order(in);
        }
        @Override
        public Order[] newArray(int size) {
            return new Order[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(orderId);
        dest.writeString(userId);
        dest.writeString(customerName);
        dest.writeString(phone);
        dest.writeString(address);
        dest.writeDouble(total);
        dest.writeString(status);
        dest.writeParcelable(createdAt, flags);
        dest.writeTypedList(items);
        dest.writeByte((byte) (cancellationRequested ? 1 : 0));
    }
}