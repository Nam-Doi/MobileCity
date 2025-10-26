package com.example.androidapp.models; // Hoặc package của bạn

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class Product implements Serializable {
    private String id;
    private String name;
    private String brand; // thương hiệu
    private String category; // Thêm
    private String description; // Thêm
    private Map<String, String> specifications;
    private List<ProductVariant> variants; // Thay đổi

    public Product() {
        // Constructor rỗng cho Firebase
    }

    // Constructor 7 tham số mà AddProductActivity đang gọi
    public Product(String id, String name, String brand, String category,
            String description, Map<String, String> specifications,
            List<ProductVariant> variants) {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.category = category;
        this.description = description;
        this.specifications = specifications;
        this.variants = variants;
    }

    // Getter và Setter
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

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, String> getSpecifications() {
        return specifications;
    }

    public void setSpecifications(Map<String, String> specifications) {
        this.specifications = specifications;
    }

    public List<ProductVariant> getVariants() {
        return variants;
    }

    public void setVariants(List<ProductVariant> variants) {
        this.variants = variants;
    }

    // Backwards-compatibility helpers: many parts of the app expect product-level
    // getters for price, stock and imageUrls. Return the first variant's values
    // when variants are present, else sensible defaults.
    public java.util.List<String> getImageUrls() {
        if (variants != null && !variants.isEmpty() && variants.get(0).getImageUrls() != null) {
            return variants.get(0).getImageUrls();
        }
        return null;
    }

    public double getPrice() {
        if (variants != null && !variants.isEmpty()) {
            return variants.get(0).getPrice();
        }
        return 0.0;
    }

    public int getStock() {
        if (variants != null && !variants.isEmpty()) {
            return variants.get(0).getStock();
        }
        return 0;
    }
}