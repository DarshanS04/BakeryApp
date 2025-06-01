package com.example.bakeryapp;

public class SpecialCake {
    private String code;
    private int price;
    private int imageResId;

    public SpecialCake(String code, int price, int imageResId) {
        this.code = code;
        this.price = price;
        this.imageResId = imageResId;
    }

    public String getCode() {
        return code;
    }

    public int getPrice() {
        return price;
    }

    public int getImageResId() {
        return imageResId;
    }
}