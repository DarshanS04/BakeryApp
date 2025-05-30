package com.example.bakeryapp;

public class OrderItem {
    private String product;
    private int quantity;
    private int unitPrice;
    private int total;

    // Required empty constructor for Firebase
    public OrderItem() {}

    public OrderItem(String product, int quantity, int unitPrice, int total) {
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.total = total;
    }

    // Getters and setters
    public String getProduct() { return product; }
    public void setProduct(String product) { this.product = product; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getUnitPrice() { return unitPrice; }
    public void setUnitPrice(int unitPrice) { this.unitPrice = unitPrice; }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
}