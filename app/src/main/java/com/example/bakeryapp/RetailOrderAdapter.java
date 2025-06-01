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
        holder.orderIdTextView.setText(order.getId() != null ? order.getId().substring(Math.max(0, order.getId().length() - 6)) : "N/A");

        // Date (use getDate directly)
        holder.dateTextView.setText(order.getDate() != null ? order.getDate() : "N/A");

        // Products (materialName, quantity)
        StringBuilder products = new StringBuilder();
        if (order.getItems() != null) {
            for (RetailOrder.OrderItem item : order.getItems()) {
                products.append(item.getMaterialName()).append(" (").append(item.getQuantity()).append(")\n");
            }
        }
        holder.productsTextView.setText(products.length() > 0 ? products.toString().trim() : "None");

        // Manager
        holder.managerTextView.setText(order.getManagerEmail() != null ? order.getManagerEmail() : "Unknown");

        // Total
        double total = order.getTotal();
        holder.totalTextView.setText(String.format(Locale.US, "%.2f rupees", total));

        // Status (with styling)
        String status = order.getStatus() != null ? order.getStatus() : "Unknown";
        holder.statusTextView.setText(status);
        setStatusStyle(holder.statusTextView, status);

        // Actions
        holder.acceptButton.setVisibility(View.GONE);
        holder.rejectButton.setVisibility(View.GONE);

        if ("Pending".equalsIgnoreCase(status)) {
            holder.acceptButton.setVisibility(View.VISIBLE);
            holder.rejectButton.setVisibility(View.VISIBLE);
            holder.acceptButton.setOnClickListener(v -> actionListener.onAccept(order.getId()));
            holder.rejectButton.setOnClickListener(v -> actionListener.onReject(order.getId()));
        }
    }

    private double calculateTotal(List<RetailOrder.OrderItem> items) {
        if (items == null) return 0.0;
        double total = 0.0;
        for (RetailOrder.OrderItem item : items) {
            total += item.getTotal();
        }
        return total;
    }

    private void setStatusStyle(TextView statusTextView, String status) {
        int colorRes;
        switch (status != null ? status.toLowerCase() : "") {
            case "pending":
                colorRes = android.R.color.holo_orange_light;
                break;
            case "rejected":
            case "cancelled":
                colorRes = android.R.color.holo_red_light;
                break;
            default:
                colorRes = android.R.color.black;
        }
        statusTextView.setTextColor(statusTextView.getContext().getResources().getColor(colorRes));
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderIdTextView, dateTextView, productsTextView, managerTextView, totalTextView, statusTextView;
        Button acceptButton, rejectButton;

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
        }
    }
}
