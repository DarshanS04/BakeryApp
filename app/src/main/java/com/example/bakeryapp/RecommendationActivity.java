package com.example.bakeryapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import com.example.bakeryapp.databinding.ActivityRecommendationBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecommendationActivity extends AppCompatActivity {

    private ActivityRecommendationBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference ordersRef;
    private MenuAdapter adapter;
    private List<Product> products = new ArrayList<>();
    private Map<String, Integer> productPrices = new HashMap<>();
    private Map<String, Integer> productImageResIds = new HashMap<>();
    private Map<String, Long> productSales = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("RecommendationActivity", "onCreate called");

        // Initialize View Binding
        binding = ActivityRecommendationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        ordersRef = FirebaseDatabase.getInstance().getReference("orders");

        // Check authentication
        if (mAuth.getCurrentUser() == null) {
            Log.e("RecommendationActivity", "User not authenticated");
            Toast.makeText(this, "Please sign in to view recommendations", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize product data
        initializeProductData();

        // Set up RecyclerView
        binding.recommendationRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new MenuAdapter(this, products, productPrices, productImageResIds, true); // showSales = true
        binding.recommendationRecyclerView.setAdapter(adapter);

        // Show progress bar
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.recommendationRecyclerView.setVisibility(View.GONE);

        // Load recommendations
        loadRecommendations();
    }

    private void initializeProductData() {
        // Same as MenuActivity
        productPrices.put("Bread", 35);
        productPrices.put("Cake", 350);
        productPrices.put("Pastry", 90);
        productPrices.put("Cookies", 75);
        productPrices.put("Brownie", 25);
        productPrices.put("Croissant", 70);
        productPrices.put("Cup Cake", 25);
        productPrices.put("Dispasand", 10);
        productPrices.put("Egg Puff", 23);
        productPrices.put("Garlic Loaf", 50);
        productPrices.put("Masala Bun", 18);
        productPrices.put("Pav Bread", 42);
        productPrices.put("Rusk", 135);
        productPrices.put("Samosa", 20);
        productPrices.put("Veg Puff", 18);
        productPrices.put("Cheese Sandwich", 70);
        productPrices.put("Bread Pakora", 40);

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

    private void loadRecommendations() {
        ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                productSales.clear();
                products.clear();
                Log.d("RecommendationActivity", "Orders snapshot: " + dataSnapshot.toString());

                // Aggregate sales from orders
                for (DataSnapshot orderSnapshot : dataSnapshot.getChildren()) {
                    try {
                        DataSnapshot itemsSnapshot = orderSnapshot.child("items");
                        for (DataSnapshot itemSnapshot : itemsSnapshot.getChildren()) {
                            String productName = itemSnapshot.child("product").getValue(String.class);
                            Long quantity = itemSnapshot.child("quantity").getValue(Long.class);
                            if (productName != null && quantity != null) {
                                productSales.put(productName, productSales.getOrDefault(productName, 0L) + quantity);
                                Log.d("RecommendationActivity", "Product: " + productName + ", Sold: " + quantity);
                            }
                        }
                    } catch (Exception e) {
                        Log.e("RecommendationActivity", "Error processing order: " + orderSnapshot.getKey(), e);
                    }
                }

                // Convert sales to product list
                for (Map.Entry<String, Long> entry : productSales.entrySet()) {
                    String productName = entry.getKey();
                    if (productPrices.containsKey(productName)) {
                        products.add(new Product(productName, entry.getValue()));
                    }
                }

                // Sort by sales (descending) and limit to 10
                Collections.sort(products, (p1, p2) -> Long.compare(p2.getQuantity(), p1.getQuantity()));
                if (products.size() > 10) {
                    products = products.subList(0, 10);
                }

                adapter.notifyDataSetChanged();
                binding.progressBar.setVisibility(View.GONE);
                binding.recommendationRecyclerView.setVisibility(View.VISIBLE);

                if (products.isEmpty()) {
                    Toast.makeText(RecommendationActivity.this, "No recommendations available", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("RecommendationActivity", "Loaded products: " + products.toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("RecommendationActivity", "Error: " + databaseError.getMessage());
                Toast.makeText(RecommendationActivity.this, "Failed to load recommendations", Toast.LENGTH_SHORT).show();
                binding.progressBar.setVisibility(View.GONE);
                binding.recommendationRecyclerView.setVisibility(View.VISIBLE);
            }
        });
    }
}