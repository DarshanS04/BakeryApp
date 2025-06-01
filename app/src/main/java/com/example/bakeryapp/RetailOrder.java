package com.example.bakeryapp;

import java.io.Serializable;
import java.util.List;

public class RetailOrder implements Serializable {
    private String id;
    private String date;
    private List<OrderItem> items;
    private String managerEmail;
    private String managerId;
    private String status;
    private String timestamp;
    private double total;
    private String distributorId;
    private String rejectionReason;
    private String statusUpdatedAt;

    // Default constructor for Firebase
    public RetailOrder() {
    }

    public RetailOrder(String date, List<OrderItem> items, String managerEmail, String managerId,
                       String status, String timestamp, double total) {
        this.date = date;
        this.items = items;
        this.managerEmail = managerEmail;
        this.managerId = managerId;
        this.status = status;
        this.timestamp = timestamp;
        this.total = total;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public String getManagerEmail() {
        return managerEmail;
    }

    public void setManagerEmail(String managerEmail) {
        this.managerEmail = managerEmail;
    }

    public String getManagerId() {
        return managerId;
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getDistributorId() {
        return distributorId;
    }

    public void setDistributorId(String distributorId) {
        this.distributorId = distributorId;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getStatusUpdatedAt() {
        return statusUpdatedAt;
    }

    public void setStatusUpdatedAt(String statusUpdatedAt) {
        this.statusUpdatedAt = statusUpdatedAt;
    }

    public static class OrderItem implements Serializable {
        private int materialIndex;
        private String materialName;
        private String materialPrice;
        private int quantity;
        private double total;

        public OrderItem() {
        }

        public OrderItem(int materialIndex, String materialName, String materialPrice, int quantity, double total) {
            this.materialIndex = materialIndex;
            this.materialName = materialName;
            this.materialPrice = materialPrice;
            this.quantity = quantity;
            this.total = total;
        }

        public int getMaterialIndex() {
            return materialIndex;
        }

        public void setMaterialIndex(int materialIndex) {
            this.materialIndex = materialIndex;
        }

        public String getMaterialName() {
            return materialName;
        }

        public void setMaterialName(String materialName) {
            this.materialName = materialName;
        }

        public String getMaterialPrice() {
            return materialPrice;
        }

        public void setMaterialPrice(String materialPrice) {
            this.materialPrice = materialPrice;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public double getTotal() {
            return total;
        }

        public void setTotal(double total) {
            this.total = total;
        }
    }
}
