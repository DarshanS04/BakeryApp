package com.example.bakeryapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class LowStockAdapter extends RecyclerView.Adapter<LowStockAdapter.ViewHolder> {
    private List<LowStockItem> lowStockItems;

    public LowStockAdapter(List<LowStockItem> lowStockItems) {
        this.lowStockItems = lowStockItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_low_stock, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LowStockItem item = lowStockItems.get(position);
        holder.productName.setText(item.getProductName());
        holder.quantity.setText("Quantity: " + item.getQuantity());
    }

    @Override
    public int getItemCount() {
        return lowStockItems.size();
    }

    public void updateData(List<LowStockItem> newItems) {
        lowStockItems.clear();
        lowStockItems.addAll(newItems);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView productName, quantity;

        ViewHolder(View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.lowStockProductName);
            quantity = itemView.findViewById(R.id.lowStockQuantity);
        }
    }
}