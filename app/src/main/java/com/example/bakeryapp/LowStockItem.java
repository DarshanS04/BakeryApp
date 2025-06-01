package com.example.bakeryapp;

public class LowStockItem {
    private String productName;
    private int quantity;

    public LowStockItem(String productName, int quantity) {
        this.productName = productName;
        this.quantity = quantity;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }
}