package com.example.bakeryapp;

import com.google.firebase.database.IgnoreExtraProperties;
import java.io.Serializable;

@IgnoreExtraProperties
public class InventoryItem implements Serializable {
    private String key;
    private String name;
    private String quantity; // Changed to String
    private String manufactureDate;
    private String batchNumber;
    private String expiryDate;
    private String status;
    private String statusClass;
    private String timestamp;

    public InventoryItem() {}

    public InventoryItem(String name, String quantity, String manufactureDate, String batchNumber, String expiryDate, String status, String statusClass, String timestamp) {
        this.name = name;
        this.quantity = quantity;
        this.manufactureDate = manufactureDate;
        this.batchNumber = batchNumber;
        this.expiryDate = expiryDate;
        this.status = status;
        this.statusClass = statusClass;
        this.timestamp = timestamp;
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
    public String getManufactureDate() { return manufactureDate; }
    public void setManufactureDate(String manufactureDate) { this.manufactureDate = manufactureDate; }
    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }
    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getStatusClass() { return statusClass; }
    public void setStatusClass(String statusClass) { this.statusClass = statusClass; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}