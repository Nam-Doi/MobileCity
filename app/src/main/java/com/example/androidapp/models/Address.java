package com.example.androidapp.models; // Đảm bảo đúng package

public class Address {
    private String receiverName;
    private String phoneNumber;
    private String street;
    private String ward;
    private String district;
    private String city;
    private boolean isDefault;

    // Constructor rỗng bắt buộc cho Firestore
    public Address() {}

    // Constructor đầy đủ để tiện sử dụng
    public Address(String receiverName, String phoneNumber, String street, String ward, String district, String city, boolean isDefault) {
        // ... (code gán giá trị)
    }

    // Đầy đủ các phương thức Getter và Setter...
    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
    // ...
}