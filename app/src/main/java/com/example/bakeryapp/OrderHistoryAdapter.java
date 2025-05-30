package com.example.bakeryapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Map;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder> {

    private Context context;
    private List<OrderHistoryItem> orders;
    private Map<String, Integer> productImageResIds;

    public OrderHistoryAdapter(Context context, List<OrderHistoryItem> orders, Map<String, Integer> productImageResIds) {
        this.context = context;
        this.orders = orders;
        this.productImageResIds = productImageResIds;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_history_item, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderHistoryItem orderItem = orders.get(position);
        CustomerOrder order = orderItem.getOrder();

        // Set summary
        holder.orderId.setText("Order ID: " + orderItem.getOrderId());
        holder.orderDate.setText("Date: " + orderItem.getFormattedDate());
        holder.orderTotal.setText("Total: ₹" + order.getTotalAmount());
        holder.orderStatus.setText("Status: " + order.getStatus());

        // Set expandable content
        boolean isExpanded = orderItem.isExpanded();
        holder.expandableLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        // Set up nested RecyclerView for items
        holder.itemsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        OrderItemDetailsAdapter itemsAdapter = new OrderItemDetailsAdapter(order.getItems());
        holder.itemsRecyclerView.setAdapter(itemsAdapter);

        // Set additional details
        holder.deliveryAddress.setText("Address: " + order.getDeliveryAddress());
        holder.phoneNumber.setText("Phone: " + order.getPhoneNumber());
        holder.paymentMethod.setText("Payment: " + order.getPaymentMethod());

        // Toggle expansion
        holder.itemView.setOnClickListener(v -> {
            orderItem.setExpanded(!isExpanded);
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderId;
        TextView orderDate;
        TextView orderTotal;
        TextView orderStatus;
        LinearLayout expandableLayout;
        RecyclerView itemsRecyclerView;
        TextView deliveryAddress;
        TextView phoneNumber;
        TextView paymentMethod;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderId = itemView.findViewById(R.id.order_id);
            orderDate = itemView.findViewById(R.id.order_date);
            orderTotal = itemView.findViewById(R.id.order_total);
            orderStatus = itemView.findViewById(R.id.order_status);
            expandableLayout = itemView.findViewById(R.id.expandable_layout);
            itemsRecyclerView = itemView.findViewById(R.id.items_recycler_view);
            deliveryAddress = itemView.findViewById(R.id.delivery_address);
            phoneNumber = itemView.findViewById(R.id.phone_number);
            paymentMethod = itemView.findViewById(R.id.payment_method);
        }
    }

    class OrderItemDetailsAdapter extends RecyclerView.Adapter<OrderItemDetailsAdapter.ItemDetailViewHolder> {
        private List<OrderItem> items;

        public OrderItemDetailsAdapter(List<OrderItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ItemDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_item_detail, parent, false);
            return new ItemDetailViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ItemDetailViewHolder holder, int position) {
            OrderItem item = items.get(position);
            holder.itemName.setText(item.getProduct());
            holder.itemQuantity.setText("Qty: " + item.getQuantity());
            holder.itemPrice.setText("₹" + item.getUnitPrice());
            holder.itemTotal.setText("Total: ₹" + item.getTotal());
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ItemDetailViewHolder extends RecyclerView.ViewHolder {
            TextView itemName;
            TextView itemQuantity;
            TextView itemPrice;
            TextView itemTotal;

            public ItemDetailViewHolder(@NonNull View itemView) {
                super(itemView);
                itemName = itemView.findViewById(R.id.item_name);
                itemQuantity = itemView.findViewById(R.id.item_quantity);
                itemPrice = itemView.findViewById(R.id.item_price);
                itemTotal = itemView.findViewById(R.id.item_total);
            }
        }
    }
}