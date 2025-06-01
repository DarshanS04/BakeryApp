package com.example.bakeryapp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.bakeryapp.databinding.ActivityDistributorRetailOrdersBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DistributorRetailOrdersActivity extends AppCompatActivity {

    private ActivityDistributorRetailOrdersBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference retailOrdersRef;
    private RetailOrderAdapter adapter;
    private List<RetailOrder> orders = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("DistributorRetailOrders", "onCreate called");

        // Initialize View Binding
        binding = ActivityDistributorRetailOrdersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        retailOrdersRef = FirebaseDatabase.getInstance().getReference("retailOrders");

        // Set up RecyclerView
        adapter = new RetailOrderAdapter(new RetailOrderAdapter.OnOrderActionListener() {
            @Override
            public void onAccept(String orderId) {
                updateOrderStatus(orderId, "Accepted", null);
            }

            @Override
            public void onReject(String orderId) {
                showRejectReasonDialog(orderId);
            }
        });
        binding.retailOrdersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.retailOrdersRecyclerView.setAdapter(adapter);

        // Check authentication and load orders
        if (mAuth.getCurrentUser() != null) {
            loadRetailOrders();
        } else {
            Log.e("DistributorRetailOrders", "User not authenticated");
            Toast.makeText(this, "Please sign in to view retail orders", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadRetailOrders() {
        retailOrdersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                orders.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    RetailOrder order = snapshot.getValue(RetailOrder.class);
                    if (order != null) {
                        order.setId(snapshot.getKey());
                        orders.add(order);
                    }
                }

                // Sort by timestamp (newest first)
                Collections.sort(orders, (a, b) -> {
                    try {
                        String aTimestamp = a.getTimestamp() != null ? a.getTimestamp() : "";
                        String bTimestamp = b.getTimestamp() != null ? b.getTimestamp() : "";
                        return bTimestamp.compareTo(aTimestamp);
                    } catch (Exception e) {
                        return 0;
                    }
                });

                adapter.setOrders(orders);
                if (orders.isEmpty()) {
                    Log.d("DistributorRetailOrders", "No retail orders found");
                    Toast.makeText(DistributorRetailOrdersActivity.this, "No retail orders available", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("DistributorRetailOrders", "Fetched " + orders.size() + " retail orders");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("DistributorRetailOrders", "Database error: " + databaseError.getMessage());
                Toast.makeText(DistributorRetailOrdersActivity.this, "Failed to load orders: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showRejectReasonDialog(String orderId) {
        EditText input = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle("Reject Order")
                .setMessage("Please provide a reason for rejecting this order:")
                .setView(input)
                .setPositiveButton("Reject", (dialog, which) -> {
                    String reason = input.getText().toString().trim();
                    if (!reason.isEmpty()) {
                        updateOrderStatus(orderId, "Rejected", reason);
                    } else {
                        Toast.makeText(this, "Please provide a reason", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateOrderStatus(String orderId, String newStatus, String rejectionReason) {
        if (orderId == null) return;

        DatabaseReference orderRef = retailOrdersRef.child(orderId);
        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    RetailOrder order = snapshot.getValue(RetailOrder.class);
                    if (order != null) {
                        order.setStatus(newStatus);
                        order.setStatusUpdatedAt(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(new Date()));
                        order.setDistributorId(mAuth.getCurrentUser().getUid());
                        if ("Rejected".equals(newStatus) && rejectionReason != null) {
                            order.setRejectionReason(rejectionReason);
                        }

                        orderRef.setValue(order, (error, ref) -> {
                            if (error == null) {
                                Toast.makeText(DistributorRetailOrdersActivity.this, "Order " + orderId.substring(Math.max(0, orderId.length() - 6)) + " " + newStatus.toLowerCase(), Toast.LENGTH_SHORT).show();
                            } else {
                                Log.e("DistributorRetailOrders", "Error updating order: " + error.getMessage());
                                Toast.makeText(DistributorRetailOrdersActivity.this, "Failed to update order: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    Toast.makeText(DistributorRetailOrdersActivity.this, "Order not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("DistributorRetailOrders", "Error retrieving order: " + databaseError.getMessage());
                Toast.makeText(DistributorRetailOrdersActivity.this, "Failed to retrieve order: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
