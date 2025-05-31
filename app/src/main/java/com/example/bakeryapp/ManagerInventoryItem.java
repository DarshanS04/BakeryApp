package com.example.bakeryapp;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class ManagerInventoryItem {
    private String key;
    private String name;
    private String quantity;

    public ManagerInventoryItem() {}

    public ManagerInventoryItem(String name, String quantity) {
        this.name = name;
        this.quantity = quantity;
    }

    // Getters and setters
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }
    public int getQuantityAsInt() {
        try {
            return quantity != null ? Integer.parseInt(quantity) : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}