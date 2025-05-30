package com.example.bakeryapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder> {

    private List<OrderItem> orderItems = new ArrayList<>();
    private List<String> availableProducts;
    private Map<String, Integer> productPrices;
    private Map<String, Integer> productImageResIds;
    private Context context;
    private OnOrderItemChangeListener listener;

    public interface OnOrderItemChangeListener {
        void onItemChanged();
        void onItemRemoved(int position);
    }

    public OrderItemAdapter(Context context, List<String> availableProducts, Map<String, Integer> productPrices,
                            Map<String, Integer> productImageResIds, OnOrderItemChangeListener listener) {
        this.context = context;
        this.availableProducts = availableProducts;
        this.productPrices = productPrices;
        this.productImageResIds = productImageResIds;
        this.listener = listener;
        // Add initial empty item
        orderItems.add(new OrderItem("", 0, 0, 0));
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void addOrderItem() {
        orderItems.add(new OrderItem("", 0, 0, 0));
        notifyItemInserted(orderItems.size() - 1);
    }

    @NonNull
    @Override
    public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.order_item, parent, false);
        return new OrderItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderItemViewHolder holder, int position) {
        OrderItem item = orderItems.get(position);

        // Set up product spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, availableProducts);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.productSpinner.setAdapter(adapter);

        // Select current product
        if (item.getProduct() != null && !item.getProduct().isEmpty()) {
            int spinnerPosition = availableProducts.indexOf(item.getProduct());
            holder.productSpinner.setSelection(spinnerPosition);
        }

        // Set quantity
        holder.quantityTextView.setText(item.getQuantity() > 0 ? String.valueOf(item.getQuantity()) : "");

        // Set image
        if (item.getProduct() != null && productImageResIds.containsKey(item.getProduct())) {
            holder.productImage.setImageResource(productImageResIds.get(item.getProduct()));
            holder.productImage.setVisibility(View.VISIBLE);
        } else {
            holder.productImage.setVisibility(View.GONE);
        }

        // Product selection listener
        holder.productSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String selectedProduct = availableProducts.get(pos);
                item.setProduct(selectedProduct);
                item.setUnitPrice(productPrices.getOrDefault(selectedProduct, 0));
                if (!selectedProduct.isEmpty()) {
                    holder.productImage.setImageResource(productImageResIds.getOrDefault(selectedProduct, R.drawable.default_image));
                    holder.productImage.setVisibility(View.VISIBLE);
                } else {
                    holder.productImage.setVisibility(View.GONE);
                }
                updateTotal(item, holder.quantityTextView.getText().toString());
                listener.onItemChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Quantity input listener
        holder.quantityTextView.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                try {
                    int quantity = s.toString().isEmpty() ? 0 : Integer.parseInt(s.toString());
                    item.setQuantity(quantity);
                    updateTotal(item, s.toString());
                    listener.onItemChanged();
                } catch (NumberFormatException e) {
                    item.setQuantity(0);
                    item.setTotal(0);
                    listener.onItemChanged();
                }
            }
        });

        // Remove button listener (using anonymous class with getAdapterPosition)
        holder.removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    orderItems.remove(currentPosition);
                    notifyItemRemoved(currentPosition);
                    notifyItemRangeChanged(currentPosition, orderItems.size());
                    listener.onItemRemoved(currentPosition);
                }
            }
        });

        // Hide remove button for first item
        holder.removeButton.setVisibility(position == 0 ? View.GONE : View.VISIBLE);
    }

    private void updateTotal(OrderItem item, String quantityStr) {
        try {
            int quantity = quantityStr.isEmpty() ? 0 : Integer.parseInt(quantityStr);
            item.setQuantity(quantity);
            item.setTotal(item.getUnitPrice() * quantity);
        } catch (NumberFormatException e) {
            item.setTotal(0);
        }
    }

    @Override
    public int getItemCount() {
        return orderItems.size();
    }

    static class OrderItemViewHolder extends RecyclerView.ViewHolder {
        Spinner productSpinner;
        TextView quantityTextView;
        ImageView productImage;
        Button removeButton;

        public OrderItemViewHolder(@NonNull View itemView) {
            super(itemView);
            productSpinner = itemView.findViewById(R.id.product_spinner);
            quantityTextView = itemView.findViewById(R.id.quantity_input);
            productImage = itemView.findViewById(R.id.product_image);
            removeButton = itemView.findViewById(R.id.remove_button);
        }
    }
}