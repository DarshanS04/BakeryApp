package com.example.bakeryapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RetailOrderAdapter extends RecyclerView.Adapter<RetailOrderAdapter.OrderViewHolder> {

    private List<RetailOrder> orders = new ArrayList<>();
    private OnOrderActionListener actionListener;

    public interface OnOrderActionListener {
        void onAccept(String orderId);
        void onReject(String orderId);
        void onComplete(String orderId);
    }

    public RetailOrderAdapter(OnOrderActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public void setOrders(List<RetailOrder> orders) {
        this.orders = orders;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.retail_order_item, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        RetailOrder order = orders.get(position);

        // Order ID (last 6 chars)
        holder.orderIdTextView.setText(order.getId().substring(Math.max(0, order.getId().length() - 6)));

        // Date (formatted)
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).parse(order.getTimestamp());
            holder.dateTextView.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.US).format(date));
        } catch (Exception e) {
            holder.dateTextView.setText(order.getDate() != null ? order.getDate() : "N/A");
        }

        // Products (material, quantity)
        StringBuilder products = new StringBuilder();
        for (RetailOrderItem item : order.getItems()) {
            products.append(item.getMaterial()).append(" (").append(item.getQuantity()).append(")\n");
        }
        holder.productsTextView.setText(products.toString().trim());

        // Manager
        holder.managerTextView.setText(order.getManagerName() != null ? order.getManagerName() : "Unknown");

        // Total
        int total = order.getTotal() > 0 ? order.getTotal() : calculateTotal(order.getItems());
        holder.totalTextView.setText(total + " rupees");

        // Status (with styling)
        holder.statusTextView.setText(order.getStatus());
        setStatusStyle(holder.statusTextView, order.getStatus());

        // Actions
        holder.acceptButton.setVisibility(View.GONE);
        holder.rejectButton.setVisibility(View.GONE);
        holder.completeButton.setVisibility(View.GONE);

        if ("pending".equalsIgnoreCase(order.getStatus())) {
            holder.acceptButton.setVisibility(View.VISIBLE);
            holder.rejectButton.setVisibility(View.VISIBLE);
            holder.acceptButton.setOnClickListener(v -> actionListener.onAccept(order.getId()));
            holder.rejectButton.setOnClickListener(v -> actionListener.onReject(order.getId()));
        } else if ("accepted".equalsIgnoreCase(order.getStatus())) {
            holder.completeButton.setVisibility(View.VISIBLE);
            holder.completeButton.setOnClickListener(v -> actionListener.onComplete(order.getId()));
        }
    }

    private int calculateTotal(List<RetailOrderItem> items) {
        if (items == null) return 0;
        int total = 0;
        for (RetailOrderItem item : items) {
            if (item.getTotal() > 0) {
                total += item.getTotal();
            } else {
                String priceStr = item.getPrice() != null ? item.getPrice().replaceAll("[^0-9]", "") : "0";
                int price = Integer.parseInt(priceStr.isEmpty() ? "0" : priceStr);
                total += price * item.getQuantity();
            }
        }
        return total;
    }

    private void setStatusStyle(TextView statusTextView, String status) {
        int color;
        switch (status != null ? status.toLowerCase() : "") {
            case "pending":
                color = 0xFFFFA500; // Orange
                break;
            case "accepted":
                color = 0xFF4CAF50; // Green
                break;
            case "completed":
                color = 0xFF2196F3; // Blue
                break;
            case "rejected":
            case "cancelled":
                color = 0xFFF44336; // Red
                break;
            default:
                color = 0xFF000000; // Black
        }
        statusTextView.setTextColor(color);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderIdTextView, dateTextView, productsTextView, managerTextView, totalTextView, statusTextView;
        Button acceptButton, rejectButton, completeButton;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderIdTextView = itemView.findViewById(R.id.order_id);
            dateTextView = itemView.findViewById(R.id.order_date);
            productsTextView = itemView.findViewById(R.id.order_products);
            managerTextView = itemView.findViewById(R.id.order_manager);
            totalTextView = itemView.findViewById(R.id.order_total);
            statusTextView = itemView.findViewById(R.id.order_status);
            acceptButton = itemView.findViewById(R.id.accept_button);
            rejectButton = itemView.findViewById(R.id.reject_button);
            completeButton = itemView.findViewById(R.id.complete_button);
        }
    }
}