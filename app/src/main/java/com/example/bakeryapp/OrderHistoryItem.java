package com.example.bakeryapp;

public class OrderHistoryItem {
    private String orderId;
    private CustomerOrder order;
    private String formattedDate;
    private boolean isExpanded;

    public OrderHistoryItem(String orderId, CustomerOrder order, String formattedDate) {
        this.orderId = orderId;
        this.order = order;
        this.formattedDate = formattedDate;
        this.isExpanded = false;
    }

    public String getOrderId() { return orderId; }
    public CustomerOrder getOrder() { return order; }
    public String getFormattedDate() { return formattedDate; }
    public boolean isExpanded() { return isExpanded; }
    public void setExpanded(boolean expanded) { isExpanded = expanded; }
}