package com.example.bakeryapp;

public class Feedback {
    private String customerId;
    private String customerEmail;
    private int rating;
    private String comment;
    private String timestamp;

    // Default constructor required for Firebase
    public Feedback() {
    }

    public Feedback(String customerId, String customerEmail, int rating, String comment, String timestamp) {
        this.customerId = customerId;
        this.customerEmail = customerEmail;
        this.rating = rating;
        this.comment = comment;
        this.timestamp = timestamp;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
