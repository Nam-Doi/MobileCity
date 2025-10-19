package com.example.androidapp.models;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class Product implements Serializable {
    private String id;
    private String name;
    private String brand;
    private double price;
    private List<String> imageUrls;
    private int stock;
    private Map<String, String> specifications;

    public Product() {}

    public Product(String id, String name, String brand, double price,
                   List<String> imageUrls, int stock, Map<String, String> specifications) {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.price = price;
        this.imageUrls = imageUrls;
        this.stock = stock;
        this.specifications = specifications;
    }

    // Getter và Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public Map<String, String> getSpecifications() { return specifications; }
    public void setSpecifications(Map<String, String> specifications) { this.specifications = specifications; }
}
