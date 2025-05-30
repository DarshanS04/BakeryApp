package com.example.bakeryapp;

public class Feedback {
    private String userId;
    private String userEmail;
    private float rating;
    private String comments;
    private String timestamp;

    // Default constructor for Firebase
    public Feedback() {}

    public Feedback(String userId, String userEmail, float rating, String comments, String timestamp) {
        this.userId = userId;
        this.userEmail = userEmail;
        this.rating = rating;
        this.comments = comments;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}