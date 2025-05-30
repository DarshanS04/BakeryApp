package com.example.bakeryapp;

public class RawMaterial {
    private String name;
    private String price;

    // Required empty constructor for Firebase
    public RawMaterial() {}

    public RawMaterial(String name, String price) {
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}