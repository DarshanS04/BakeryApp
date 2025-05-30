package com.example.bakeryapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import com.example.bakeryapp.databinding.ActivityMenuBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuActivity extends AppCompatActivity {

    private ActivityMenuBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference inventoryRef;
    private MenuAdapter adapter;
    private List<Product> products = new ArrayList<>();
    private Map<String, Integer> productPrices = new HashMap<>();
    private Map<String, Integer> productImageResIds = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("MenuActivity", "onCreate called");

        // Initialize View Binding
        binding = ActivityMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        inventoryRef = FirebaseDatabase.getInstance().getReference("inventory");

        // Check authentication
        if (mAuth.getCurrentUser() == null) {
            Log.e("MenuActivity", "User not authenticated");
            Toast.makeText(this, "Please sign in to view menu", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize product data
        initializeProductData();

        // Set up RecyclerView
        binding.menuRecyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columns
        adapter = new MenuAdapter(this, products, productPrices, productImageResIds);
        binding.menuRecyclerView.setAdapter(adapter);

        // Show progress bar
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.menuRecyclerView.setVisibility(View.GONE);

        // Load products
        loadProducts();
    }

    private void initializeProductData() {
        // Same as OrderProductActivity
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

    private void loadProducts() {
        inventoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                products.clear();
                Log.d("MenuActivity", "Inventory snapshot: " + dataSnapshot.toString());

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        String productName = snapshot.child("name").getValue(String.class);
                        String quantityStr = snapshot.child("quantity").getValue(String.class);
                        Long quantity = null;
                        if (quantityStr != null) {
                            try {
                                quantity = Long.parseLong(quantityStr);
                            } catch (NumberFormatException e) {
                                Log.e("MenuActivity", "Invalid quantity format for " + productName + ": " + quantityStr);
                            }
                        }
                        Log.d("MenuActivity", "Product: " + productName + ", Quantity: " + quantity);
                        if (productName != null && quantity != null && quantity > 0 && productPrices.containsKey(productName)) {
                            products.add(new Product(productName, quantity));
                        }
                    } catch (Exception e) {
                        Log.e("MenuActivity", "Error processing product: " + snapshot.getKey(), e);
                    }
                }

                adapter.notifyDataSetChanged();
                binding.progressBar.setVisibility(View.GONE);
                binding.menuRecyclerView.setVisibility(View.VISIBLE);

                if (products.isEmpty()) {
                    Toast.makeText(MenuActivity.this, "No products available", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("MenuActivity", "Loaded products: " + products.toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("MenuActivity", "Error: " + databaseError.getMessage());
                Toast.makeText(MenuActivity.this, "Failed to load menu", Toast.LENGTH_SHORT).show();
                binding.progressBar.setVisibility(View.GONE);
                binding.menuRecyclerView.setVisibility(View.VISIBLE);
            }
        });
    }
}