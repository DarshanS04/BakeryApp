package com.example.bakeryapp;

import java.util.List;

public class RetailOrder {
    private String id; // Added for order key
    private String date;
    private List<RetailOrderItem> items;
    private String managerEmail;
    private String managerId;
    private String managerName;
    private String distributorId;
    private String status;
    private String statusUpdatedAt;
    private String timestamp;
    private String rejectionReason;
    private int total;

    // Required empty constructor for Firebase
    public RetailOrder() {}

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public List<RetailOrderItem> getItems() { return items; }
    public void setItems(List<RetailOrderItem> items) { this.items = items; }

    public String getManagerEmail() { return managerEmail; }
    public void setManagerEmail(String managerEmail) { this.managerEmail = managerEmail; }

    public String getManagerId() { return managerId; }
    public void setManagerId(String managerId) { this.managerId = managerId; }

    public String getManagerName() { return managerName; }
    public void setManagerName(String managerName) { this.managerName = managerName; }

    public String getDistributorId() { return distributorId; }
    public void setDistributorId(String distributorId) { this.distributorId = distributorId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStatusUpdatedAt() { return statusUpdatedAt; }
    public void setStatusUpdatedAt(String statusUpdatedAt) { this.statusUpdatedAt = statusUpdatedAt; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
}