package com.example.androidapp.models;

import com.google.firebase.Timestamp;
import java.util.List;
import java.util.ArrayList;

public class users {
    private String fullName;
    private String email;
    private String role;
    private String uid;
    private Timestamp createAt;
    private List<Address> addresses;
    private boolean isActive;

    // Constructors
    public users(String fullName, String email, String role, String uid, Timestamp createAt) {
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.uid = uid;
        this.createAt = createAt;
        this.addresses = new ArrayList<>();
        this.isActive = true;
    }
    public users() {
        this.isActive = true;
        this.addresses = new ArrayList<>();
    }

    // GETTERS & SETTERS ĐẦY ĐỦ

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; } // <-- Thêm cái này
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; } // <-- Thêm cái này
    public void setRole(String role) { this.role = role; }

    public String getUid() { return uid; } // <-- Đảm bảo có cái này
    public void setUid(String uid) { this.uid = uid; }

    public Timestamp getCreateAt() { return createAt; }
    public void setCreateAt(Timestamp createAt) { this.createAt = createAt; }

    public List<Address> getAddresses() { return addresses; }
    public void setAddresses(List<Address> addresses) { this.addresses = addresses; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}