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

    private List<Order> orderList;
    private OnExecuteOrderListener executeOrderListener;
    private OnViewBillListener viewBillListener;

    public interface OnExecuteOrderListener {
        void onExecuteOrder(String orderId);
    }

    public interface OnViewBillListener {
        void onViewBill(String billId);
    }

    public OrderAdapter(List<Order> orderList, OnExecuteOrderListener executeOrderListener, OnViewBillListener viewBillListener) {
        this.orderList = orderList;
        this.executeOrderListener = executeOrderListener;
        this.viewBillListener = viewBillListener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.orderIdText.setText(order.getId().substring(order.getId().length() - 6));
        holder.customerText.setText(order.getCustomerEmail() != null ? order.getCustomerEmail() : "N/A");
        StringBuilder itemsStr = new StringBuilder();
        for (Item item : order.getItems()) {
            itemsStr.append(item.getProduct()).append(" x").append(item.getQuantity()).append("\n");
        }
        holder.itemsText.setText(itemsStr.toString().trim());
        holder.totalText.setText("₹" + order.getTotalAmount());
        holder.statusText.setText(order.getStatus() != null ? order.getStatus() : "pending");

        if (order.getBillId() != null) {
            holder.actionButton.setText("View Bill");
            holder.actionButton.setOnClickListener(v -> viewBillListener.onViewBill(order.getBillId()));
        } else if (!"completed".equals(order.getStatus()) && !"cancelled".equals(order.getStatus())) {
            holder.actionButton.setText("Execute Order");
            holder.actionButton.setOnClickListener(v -> executeOrderListener.onExecuteOrder(order.getId()));
        } else {
            holder.actionButton.setText("N/A");
            holder.actionButton.setEnabled(false);
        }
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderIdText, customerText, itemsText, totalText, statusText;
        Button actionButton;

        OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderIdText = itemView.findViewById(R.id.orderIdText);
            customerText = itemView.findViewById(R.id.customerText);
            itemsText = itemView.findViewById(R.id.itemsText);
            totalText = itemView.findViewById(R.id.totalText);
            statusText = itemView.findViewById(R.id.statusText);
            actionButton = itemView.findViewById(R.id.actionButton);
        }
    }
}