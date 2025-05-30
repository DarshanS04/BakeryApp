package com.example.bakeryapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.bakeryapp.databinding.ActivityInventoryBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class InventoryActivity extends AppCompatActivity {
    private ActivityInventoryBinding binding;
    private InventoryAdapter adapter;
    private List<InventoryItem> inventoryItems = new ArrayList<>();
    private DatabaseReference inventoryRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInventoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        inventoryRef = FirebaseDatabase.getInstance().getReference("inventory");

        // Set up RecyclerView
        binding.inventoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InventoryAdapter(this, inventoryItems, this::editItem, this::deleteItem);
        binding.inventoryRecyclerView.setAdapter(adapter);

        // Add inventory button
        binding.addInventoryButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddInventoryActivity.class);
            startActivity(intent);
        });

        // Load inventory
        loadInventory();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadInventory(); // Refresh data
    }

    private void loadInventory() {
        binding.progressBar.setVisibility(View.VISIBLE);
        inventoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                inventoryItems.clear();
                for (DataSnapshot item : snapshot.getChildren()) {
                    try {
                        InventoryItem inventoryItem = item.getValue(InventoryItem.class);
                        if (inventoryItem != null && inventoryItem.getName() != null && inventoryItem.getQuantity() != null) {
                            inventoryItem.setKey(item.getKey());
                            inventoryItems.add(inventoryItem);
                        } else {
                            Log.w("InventoryActivity", "Invalid item skipped: " + item.getKey());
                        }
                    } catch (Exception e) {
                        Log.e("InventoryActivity", "Error parsing item " + item.getKey() + ": " + e.getMessage());
                    }
                }
                adapter.notifyDataSetChanged();
                binding.progressBar.setVisibility(View.GONE);
                binding.inventoryRecyclerView.setVisibility(View.VISIBLE);
                if (inventoryItems.isEmpty()) {
                    Toast.makeText(InventoryActivity.this, "No inventory items found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("InventoryActivity", "Error: " + error.getMessage());
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(InventoryActivity.this, "Failed to load inventory", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void editItem(InventoryItem item) {
        Intent intent = new Intent(this, AddInventoryActivity.class);
        intent.putExtra("item", item);
        startActivity(intent);
    }

    private void deleteItem(InventoryItem item) {
        if (item.getKey() == null) {
            Toast.makeText(this, "Invalid item", Toast.LENGTH_SHORT).show();
            return;
        }
        inventoryRef.child(item.getKey()).removeValue((error, ref) -> {
            if (error == null) {
                Toast.makeText(InventoryActivity.this, "Item deleted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(InventoryActivity.this, "Failed to delete: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}