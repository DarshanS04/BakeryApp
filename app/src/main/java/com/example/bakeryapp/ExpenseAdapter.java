package com.example.bakeryapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {
    private Context context;
    private List<ExpenseItem> expenseItems;
    private OnItemActionListener editListener;
    private OnItemActionListener deleteListener;

    public interface OnItemActionListener {
        void onAction(ExpenseItem item);
    }

    public ExpenseAdapter(Context context, List<ExpenseItem> expenseItems,
                          OnItemActionListener editListener, OnItemActionListener deleteListener) {
        this.context = context;
        this.expenseItems = expenseItems;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        ExpenseItem item = expenseItems.get(position);
        holder.typeTextView.setText("Type: " + item.getType());
        holder.amountTextView.setText("Amount: ₹" + item.getAmount());
        holder.dateTextView.setText("Date: " + item.getDate());
        holder.editButton.setOnClickListener(v -> editListener.onAction(item));
        holder.deleteButton.setOnClickListener(v -> deleteListener.onAction(item));
    }

    @Override
    public int getItemCount() {
        return expenseItems.size();
    }

    static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView typeTextView, amountTextView, dateTextView;
        View editButton, deleteButton;

        ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            typeTextView = itemView.findViewById(R.id.typeTextView);
            amountTextView = itemView.findViewById(R.id.amountTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
