package com.example.androidapp.models;

public class AddressItems {
    private String receiverName;
    private String receiverPhone;
    private String address;
    private boolean isSelected;

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
    public AddressItems(String receiverName, String receiverPhone, String address, boolean isSelected){
        this.receiverName = receiverName;
        this.address = address;
        this.receiverPhone = receiverPhone;
        this.isSelected = isSelected;
    }
}
