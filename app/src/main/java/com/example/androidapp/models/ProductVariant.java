package com.example.androidapp.models;

import java.io.Serializable;
import java.util.List;

public class ProductVariant implements Serializable {
    private String id; // ID của biến thể (hoặc SKU)
    private String color;       // MỚI: "Black", "Titanium", "Red"...
    private String ram;         // MỚI: "8GB", "12GB"...
    private String storage;     // MỚI: "128GB", "256GB"...
    private double price;       // Giá của biến thể này
    private int stock;          // Tồn kho của biến thể này
    private List<String> imageUrls; // Hình ảnh của biến thể này

    // Constructor rỗng RẤT QUAN TRỌNG cho Firebase/Firestore
    public ProductVariant() {}

    // Constructor đầy đủ (có thể dùng hoặc không)
    public ProductVariant(String id, String color, String ram, String storage,
                          double price, int stock, List<String> imageUrls) {
        this.id = id;
        this.color = color;
        this.ram = ram;
        this.storage = storage;
        this.price = price;
        this.stock = stock;
        this.imageUrls = imageUrls;
    }

    // Toàn bộ Getter và Setter

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getRam() { return ram; }
    public void setRam(String ram) { this.ram = ram; }

    public String getStorage() { return storage; }
    public void setStorage(String storage) { this.storage = storage; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
}