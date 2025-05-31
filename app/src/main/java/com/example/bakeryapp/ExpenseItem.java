package com.example.bakeryapp;

import com.google.firebase.database.IgnoreExtraProperties;
import java.io.Serializable;

@IgnoreExtraProperties
public class ExpenseItem implements Serializable {
    private String key;
    private String category;
    private String amount;
    private String date;
    private String description;
    private String timestamp;

    public ExpenseItem() {}

    public ExpenseItem(String category, String amount, String date, String description, String timestamp) {
        this.category = category;
        this.amount = amount;
        this.date = date;
        this.description = description;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getAmount() { return amount; }
    public void setAmount(String amount) { this.amount = amount; }
    public double getAmountAsDouble() {
        try {
            return amount != null ? Double.parseDouble(amount) : 0.0;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}