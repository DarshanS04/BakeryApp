package com.example.bakeryapp;

public class RetailOrderItem {
    private String material;
    private String materialName; // For some orders using materialName
    private String price;
    private int quantity;
    private int total;

    // Required empty constructor for Firebase
    public RetailOrderItem() {}

    // Getters and setters
    public String getMaterial() { return material != null ? material : materialName; }
    public void setMaterial(String material) { this.material = material; }

    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
}