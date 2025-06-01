package com.example.bakeryapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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

    // Array to store special cake prices
    private final String[] specialCakePrices = {
            "cake01:300",
            "cake02:900",
            "cake03:400",
            "cake04:700",
            "cake05:500",
            "cake06:1000",
            "cake07:200",
            "cake08:800",
            "cake09:600"
    };

    // Array to store corresponding drawable resource IDs
    private final int[] specialCakeImages = {
            R.drawable.cake01,
            R.drawable.cake02,
            R.drawable.cake03,
            R.drawable.cake04,
            R.drawable.cake05,
            R.drawable.cake06,
            R.drawable.cake07,
            R.drawable.cake08,
            R.drawable.cake09
    };

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

        // Set up special cake button
        binding.specialCakeButton.setOnClickListener(v -> showSpecialCakes());

        // Set up order product button
//        binding.orderProductButton.setOnClickListener(v -> {
//            Intent intent = new Intent(MenuActivity.this, OrderProductActivity.class);
//            startActivity(intent);
//        });

        // Load products
        loadProducts();
    }

    private void initializeProductData() {
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

    private void showSpecialCakes() {
        List<SpecialCake> cakeList = new ArrayList<>();
        // Populate the list of special cakes
        for (int i = 0; i < specialCakePrices.length; i++) {
            String cakeEntry = specialCakePrices[i];
            if (cakeEntry.trim().isEmpty()) continue;
            String[] parts = cakeEntry.split(":");
            if (parts.length == 2) {
                String cakeCode = parts[0].trim();
                int price = Integer.parseInt(parts[1].trim());
                int imageResId = specialCakeImages[i];
                cakeList.add(new SpecialCake(cakeCode, price, imageResId));
            }
        }

        if (cakeList.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Special Cakes")
                    .setMessage("No special cakes available.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        // Create a custom dialog
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_special_cakes);

        // Adjust dialog width to 90% of screen width
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog.getWindow().getAttributes());
        layoutParams.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(layoutParams);

        // Set up the RecyclerView
        RecyclerView recyclerView = dialog.findViewById(R.id.specialCakesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        SpecialCakeAdapter adapter = new SpecialCakeAdapter(this, cakeList);
        recyclerView.setAdapter(adapter);

        // Set up the close button
        Button closeButton = dialog.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}