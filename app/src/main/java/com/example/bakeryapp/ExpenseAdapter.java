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
import java.util.Locale;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ViewHolder> {
    private List<ExpenseItem> items;
    private OnEditClickListener editListener;
    private OnDeleteClickListener deleteListener;
    private Context context;

    public interface OnEditClickListener {
        void onEditClick(ExpenseItem item);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(ExpenseItem item);
    }

    public ExpenseAdapter(Context context, List<ExpenseItem> items, OnEditClickListener editListener, OnDeleteClickListener deleteListener) {
        this.context = context;
        this.items = items;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expense, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExpenseItem item = items.get(position);
        holder.serialNumberTextView.setText(String.valueOf(position + 1));
        holder.categoryTextView.setText(item.getCategory());
        holder.amountTextView.setText(String.format(Locale.US, "%.2f", item.getAmountAsDouble()));
        holder.dateTextView.setText(item.getDate());
        holder.descriptionTextView.setText(item.getDescription() != null ? item.getDescription() : "-");
        holder.editButton.setOnClickListener(v -> editListener.onEditClick(item));
        holder.deleteButton.setOnClickListener(v -> deleteListener.onDeleteClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView serialNumberTextView, categoryTextView, amountTextView, dateTextView, descriptionTextView;
        MaterialButton editButton, deleteButton;

        ViewHolder(View itemView) {
            super(itemView);
            serialNumberTextView = itemView.findViewById(R.id.serial_number);
            categoryTextView = itemView.findViewById(R.id.category);
            amountTextView = itemView.findViewById(R.id.amount);
            dateTextView = itemView.findViewById(R.id.date);
            descriptionTextView = itemView.findViewById(R.id.description);
            editButton = itemView.findViewById(R.id.edit_button);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}