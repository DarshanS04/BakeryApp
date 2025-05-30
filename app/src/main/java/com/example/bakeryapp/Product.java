package com.example.bakeryapp;

public class Product {
    private String name;
    private long quantity;

    public Product(String name, long quantity) {
        this.name = name;
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public long getQuantity() {
        return quantity;
    }

    @Override
    public String toString() {
        return "Product{name='" + name + "', quantity=" + quantity + "}";
    }
}