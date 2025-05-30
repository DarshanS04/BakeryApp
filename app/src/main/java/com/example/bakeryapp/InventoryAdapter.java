package com.example.bakeryapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.ViewHolder> {
    private List<InventoryItem> items;
    private OnEditClickListener editListener;
    private OnDeleteClickListener deleteListener;
    private Context context;

    public interface OnEditClickListener {
        void onEditClick(InventoryItem item);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(InventoryItem item);
    }

    public InventoryAdapter(Context context, List<InventoryItem> items, OnEditClickListener editListener, OnDeleteClickListener deleteListener) {
        this.context = context;
        this.items = items;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inventory, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InventoryItem item = items.get(position);
        holder.serialNumberTextView.setText(String.valueOf(position + 1));
        holder.nameTextView.setText(item.getName());
        holder.quantityTextView.setText(String.valueOf(item.getQuantityAsInt()));
        holder.manufactureDateTextView.setText(item.getManufactureDate());
        holder.batchNumberTextView.setText(item.getBatchNumber());
        holder.expiryDateTextView.setText(item.getExpiryDate());
        holder.statusTextView.setText(item.getStatus());
        holder.statusTextView.setBackgroundColor(getStatusColor(item.getStatusClass()));
        holder.editButton.setOnClickListener(v -> editListener.onEditClick(item));
        holder.deleteButton.setOnClickListener(v -> deleteListener.onDeleteClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private int getStatusColor(String statusClass) {
        switch (statusClass) {
            case "status-expired":
                return context.getResources().getColor(android.R.color.holo_red_light);
            case "status-expiring":
                return context.getResources().getColor(android.R.color.holo_orange_light);
            default:
                return context.getResources().getColor(android.R.color.holo_green_light);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView serialNumberTextView, nameTextView, quantityTextView, manufactureDateTextView, batchNumberTextView, expiryDateTextView, statusTextView;
        MaterialButton editButton, deleteButton;

        ViewHolder(View itemView) {
            super(itemView);
            serialNumberTextView = itemView.findViewById(R.id.serial_number_value);
            nameTextView = itemView.findViewById(R.id.name);
            quantityTextView = itemView.findViewById(R.id.quantity);
            manufactureDateTextView = itemView.findViewById(R.id.manufacture_date);
            batchNumberTextView = itemView.findViewById(R.id.batch_number);
            expiryDateTextView = itemView.findViewById(R.id.expiry_date);
            statusTextView = itemView.findViewById(R.id.status);
            editButton = itemView.findViewById(R.id.edit_button);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}