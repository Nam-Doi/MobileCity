package com.example.androidapp.models;

import com.google.firebase.firestore.DocumentId;

import java.util.List;
import java.util.Map;

public class Product {

    @DocumentId
     String id; // Lấy ID của document để điều hướng

     String name;
     String name_lowercase; // Dùng cho tìm kiếm
     Long price; // Kiểu Long cho giá tiền
     List<String> imageUrls; // Mảng String cho nhiều hình ảnh
     String brand;
     Long stock;
     Map<String, Object> specifications; // Map chứa thông số kỹ thuật

    public Product() {
    }

    public Product(String id, String name, String name_lowercase, Long price, List<String> imageUrls, String brand, Long stock, Map<String, Object> specifications) {
        this.id = id;
        this.name = name;
        this.name_lowercase = name_lowercase;
        this.price = price;
        this.imageUrls = imageUrls;
        this.brand = brand;
        this.stock = stock;
        this.specifications = specifications;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName_lowercase() {
        return name_lowercase;
    }

    public void setName_lowercase(String name_lowercase) {
        this.name_lowercase = name_lowercase;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public Long getStock() {
        return stock;
    }

    public void setStock(Long stock) {
        this.stock = stock;
    }

    public Map<String, Object> getSpecifications() {
        return specifications;
    }

    public void setSpecifications(Map<String, Object> specifications) {
        this.specifications = specifications;
    }
}
