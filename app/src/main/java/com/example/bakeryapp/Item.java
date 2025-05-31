package com.example.bakeryapp;

public class Item {
    private String product;
    private int quantity;
    private double total;
    private double unitPrice;

    // Default constructor for Firebase
    public Item() {}

    // Getters and setters
    public String getProduct() { return product; }
    public void setProduct(String product) { this.product = product; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
}