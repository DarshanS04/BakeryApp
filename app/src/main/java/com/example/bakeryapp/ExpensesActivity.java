package com.example.bakeryapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.bakeryapp.databinding.ActivityExpensesBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class ExpensesActivity extends AppCompatActivity {
    private ActivityExpensesBinding binding;
    private ExpenseAdapter adapter;
    private List<ExpenseItem> expenseItems = new ArrayList<>();
    private DatabaseReference expensesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExpensesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        expensesRef = FirebaseDatabase.getInstance().getReference("expenses");

        // Set up RecyclerView
        binding.expensesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ExpenseAdapter(this, expenseItems, this::editItem, this::deleteItem);
        binding.expensesRecyclerView.setAdapter(adapter);

        // Add expense button
        binding.addExpenseButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddExpenseActivity.class);
            startActivity(intent);
        });

        // Load expenses
        loadExpenses();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadExpenses(); // Refresh data
    }

    private void loadExpenses() {
        binding.progressBar.setVisibility(View.VISIBLE);
        expensesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                expenseItems.clear();
                for (DataSnapshot item : snapshot.getChildren()) {
                    try {
                        ExpenseItem expenseItem = item.getValue(ExpenseItem.class);
                        if (expenseItem != null && expenseItem.getType() != null && expenseItem.getAmount() != 0) {
                            expenseItem.setKey(item.getKey());
                            expenseItems.add(expenseItem);
                        } else {
                            Log.w("ExpensesActivity", "Invalid item skipped: " + item.getKey());
                        }
                    } catch (Exception e) {
                        Log.e("ExpensesActivity", "Error parsing item " + item.getKey() + ": " + e.getMessage());
                    }
                }
                adapter.notifyDataSetChanged();
                binding.progressBar.setVisibility(View.GONE);
                binding.expensesRecyclerView.setVisibility(View.VISIBLE);
                if (expenseItems.isEmpty()) {
                    Toast.makeText(ExpensesActivity.this, "No expenses found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ExpensesActivity", "Error: " + error.getMessage());
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(ExpensesActivity.this, "Failed to load expenses", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void editItem(ExpenseItem item) {
        Intent intent = new Intent(this, AddExpenseActivity.class);
        intent.putExtra("item", item);
        startActivity(intent);
    }

    private void deleteItem(ExpenseItem item) {
        if (item.getKey() == null) {
            Toast.makeText(this, "Invalid item", Toast.LENGTH_SHORT).show();
            return;
        }
        expensesRef.child(item.getKey()).removeValue((error, ref) -> {
            if (error == null) {
                Toast.makeText(ExpensesActivity.this, "Expense deleted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ExpensesActivity.this, "Failed to delete: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
