package com.example.bakeryapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.bakeryapp.databinding.ActivityOrderHistoryBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OrderHistoryActivity extends AppCompatActivity {

    private ActivityOrderHistoryBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference ordersRef;
    private OrderHistoryAdapter adapter;
    private List<OrderHistoryItem> orders = new ArrayList<>();
    private Map<String, Integer> productImageResIds = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("OrderHistory", "onCreate called");

        // Initialize View Binding
        binding = ActivityOrderHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        ordersRef = FirebaseDatabase.getInstance().getReference("orders");

        // Check authentication
        if (mAuth.getCurrentUser() == null) {
            Log.e("OrderHistory", "User not authenticated");
            Toast.makeText(this, "Please sign in to view order history", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize image resources
        initializeImageResIds();

        // Set up RecyclerView
        binding.orderHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderHistoryAdapter(this, orders, productImageResIds);
        binding.orderHistoryRecyclerView.setAdapter(adapter);

        // Show progress bar
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.orderHistoryRecyclerView.setVisibility(View.GONE);

        // Load orders
        loadOrders();
    }

    private void initializeImageResIds() {
        // Same as OrderProductActivity and MenuActivity
        productImageResIds.put("Bread", R.drawable.bread);
        productImageResIds.put("Cake", R.drawable.cake);
        productImageResIds.put("Pastry", R.drawable.pastry);
        productImageResIds.put("Cookies", R.drawable.cookies);
        productImageResIds.put("Brownie", R.drawable.brownie);
        productImageResIds.put("Croissant", R.drawable.croissant);
        productImageResIds.put("Cup Cake", R.drawable.cup_cake);
        productImageResIds.put("Dispasand", R.drawable.dilpasand);
        productImageResIds.put("Egg Puff", R.drawable.egg_puff);
        productImageResIds.put("Garlic Loaf", R.drawable.garlic_loaf);
        productImageResIds.put("Masala Bun", R.drawable.masala_bun);
        productImageResIds.put("Pav Bread", R.drawable.pav_bread);
        productImageResIds.put("Rusk", R.drawable.rusk);
        productImageResIds.put("Samosa", R.drawable.samosa);
        productImageResIds.put("Veg Puff", R.drawable.veg_puff);
        productImageResIds.put("Cheese Sandwich", R.drawable.cheese_sandwich);
        productImageResIds.put("Bread Pakora", R.drawable.bread_pakora);
    }

    private void loadOrders() {
        String userId = mAuth.getCurrentUser().getUid();
        Query userOrdersQuery = ordersRef.orderByChild("customerId").equalTo(userId);

        userOrdersQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                orders.clear();
                Log.d("OrderHistory", "Orders snapshot: " + dataSnapshot.toString());

                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US);

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        CustomerOrder order = snapshot.getValue(CustomerOrder.class);
                        if (order != null) {
                            String orderId = snapshot.getKey();
                            String formattedDate = order.getTimestamp();
                            try {
                                Date date = inputFormat.parse(order.getTimestamp());
                                formattedDate = outputFormat.format(date);
                            } catch (ParseException e) {
                                Log.e("OrderHistory", "Error parsing date: " + order.getTimestamp(), e);
                            }
                            orders.add(new OrderHistoryItem(orderId, order, formattedDate));
                            Log.d("OrderHistory", "Loaded order: " + orderId);
                        }
                    } catch (Exception e) {
                        Log.e("OrderHistory", "Error processing order: " + snapshot.getKey(), e);
                    }
                }

                adapter.notifyDataSetChanged();
                binding.progressBar.setVisibility(View.GONE);
                binding.orderHistoryRecyclerView.setVisibility(View.VISIBLE);

                if (orders.isEmpty()) {
                    Toast.makeText(OrderHistoryActivity.this, "No orders found", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("OrderHistory", "Loaded orders: " + orders.size());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("OrderHistory", "Error: " + databaseError.getMessage());
                Toast.makeText(OrderHistoryActivity.this, "Failed to load orders", Toast.LENGTH_SHORT).show();
                binding.progressBar.setVisibility(View.GONE);
                binding.orderHistoryRecyclerView.setVisibility(View.VISIBLE);
            }
        });
    }
}