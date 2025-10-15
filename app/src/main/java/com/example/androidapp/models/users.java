package com.example.androidapp.models;

import com.google.firebase.Timestamp;

public class users {
    String fullName;
    String email;
    String password;
    String role;
//    String address;
    String uid;
    Timestamp createAt;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

//    public String getAddress() {
//        return address;
//    }
//
//    public void setAddress(String address) {
//        this.address = address;
//    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Timestamp getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Timestamp createAt) {
        this.createAt = createAt;
    }

    public users(String fullName, String email, String role, String uid, Timestamp createAt){
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.uid = uid;
        this.createAt = createAt;
    }
    public users(){}



}
