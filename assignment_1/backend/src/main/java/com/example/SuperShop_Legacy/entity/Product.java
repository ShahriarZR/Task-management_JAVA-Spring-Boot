package com.example.SuperShop_Legacy.entity;
import com.example.SuperShop_Legacy.enums.ProductCategory;
import java.time.LocalDate;

public class Product {

    private int id;

    private String name;

    private double price;

    private int quantity;

    private ProductCategory category;

    private LocalDate expireDate;

    private Double discountedPrice;

    private boolean availability;

    public Product() {}

    public Product(int id, String name, double price, int quantity, ProductCategory category, LocalDate expireDate) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.category = category;
        this.expireDate = expireDate;
        this.discountedPrice = null;
        this.availability = expireDate != null && expireDate.isAfter(LocalDate.now());
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public ProductCategory getCategory() {
        return category;
    }

    public LocalDate getExpireDate() {
        return expireDate;
    }

    public Double getDiscountedPrice() {
        return discountedPrice;
    }

    public boolean isAvailability() {
        return availability;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setCategory(ProductCategory category) {
        this.category = category;
    }

    public void setExpireDate(LocalDate expireDate) {
        this.expireDate = expireDate;
        this.availability = expireDate != null && expireDate.isAfter(LocalDate.now());
    }

    public void setDiscountedPrice(Double discountedPrice) {
        this.discountedPrice = discountedPrice;
    }

    public void setAvailability(boolean availability) {
        this.availability = availability;
    }
}
