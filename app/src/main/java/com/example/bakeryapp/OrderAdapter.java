package com.example.bakeryapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private List<Order> orders;
    private OnExecuteClickListener executeClickListener;
    private OnViewBillClickListener viewBillClickListener;

    public interface OnExecuteClickListener {
        void onExecuteClick(String orderId);
    }

    public interface OnViewBillClickListener {
        void onViewBillClick(String billId);
    }

    public OrderAdapter(List<Order> orders, OnExecuteClickListener executeClickListener, OnViewBillClickListener viewBillClickListener) {
        this.orders = orders;
        this.executeClickListener = executeClickListener;
        this.viewBillClickListener = viewBillClickListener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.orderIdText.setText("Order ID: " + (order.getId() != null ? order.getId() : "N/A"));
        holder.customerEmailText.setText("Customer: " + (order.getCustomerEmail() != null ? order.getCustomerEmail() : "N/A"));
        holder.totalAmountText.setText("Total: ₹" + order.getTotalAmount());
        holder.statusText.setText("Status: " + (order.getStatus() != null ? order.getStatus() : "N/A"));
        holder.descriptionText.setText("Description: " + (order.getDescription() != null && !order.getDescription().isEmpty() ? order.getDescription() : "None"));

        // Enable/disable execute button based on status
        holder.executeButton.setEnabled(!"completed".equals(order.getStatus()));
        holder.executeButton.setOnClickListener(v -> executeClickListener.onExecuteClick(order.getId()));

        // Show/hide view bill button based on billId
        holder.viewBillButton.setVisibility(order.getBillId() != null ? View.VISIBLE : View.GONE);
        holder.viewBillButton.setOnClickListener(v -> {
            if (order.getBillId() != null) {
                viewBillClickListener.onViewBillClick(order.getBillId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderIdText, customerEmailText, totalAmountText, statusText, descriptionText;
        Button executeButton, viewBillButton;

        OrderViewHolder(View itemView) {
            super(itemView);
            orderIdText = itemView.findViewById(R.id.orderIdText);
            customerEmailText = itemView.findViewById(R.id.customerEmailText);
            totalAmountText = itemView.findViewById(R.id.totalAmountText);
            statusText = itemView.findViewById(R.id.statusText);
            descriptionText = itemView.findViewById(R.id.descriptionText);
            executeButton = itemView.findViewById(R.id.executeButton);
            viewBillButton = itemView.findViewById(R.id.viewBillButton);
        }
    }
}