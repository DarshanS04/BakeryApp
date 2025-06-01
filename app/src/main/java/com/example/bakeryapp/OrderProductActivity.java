package com.example.bakeryapp;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.bakeryapp.databinding.ActivityOrderProductBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OrderProductActivity extends AppCompatActivity {

    private ActivityOrderProductBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference inventoryRef, ordersRef, historyRef;
    private OrderItemAdapter adapter;
    private List<String> availableProducts = new ArrayList<>();
    private Map<String, Integer> productPrices = new HashMap<>();
    private Map<String, Integer> productImageResIds = new HashMap<>();
    private ProgressBar progressBar;
    private ArrayAdapter<String> paymentMethodAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("OrderProduct", "onCreate called");

        // Initialize View Binding
        binding = ActivityOrderProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize ProgressBar
        progressBar = new ProgressBar(this);
        progressBar.setVisibility(View.VISIBLE);
        binding.orderItemsRecyclerView.setVisibility(View.GONE);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        inventoryRef = FirebaseDatabase.getInstance().getReference("inventory");
        ordersRef = FirebaseDatabase.getInstance().getReference("orders");
        historyRef = FirebaseDatabase.getInstance().getReference("customerProductHistory");

        // Check authentication
        if (mAuth.getCurrentUser() == null) {
            Log.e("OrderProduct", "User not authenticated");
            Toast.makeText(this, "Please sign in to place orders", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize product data
        initializeProductData();

        // Set up RecyclerView (adapter will be set after products load)
        binding.orderItemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Set up payment method spinner
        setupPaymentMethodSpinner();

        // Load available products
        loadAvailableProducts();

        // Button listeners
        binding.addItemButton.setOnClickListener(v -> {
            adapter.addOrderItem();
            updateTotalCost();
        });

        binding.clearOrderButton.setOnClickListener(v -> {
            adapter = new OrderItemAdapter(this, availableProducts, productPrices, productImageResIds,
                    new OrderItemAdapter.OnOrderItemChangeListener() {
                        @Override
                        public void onItemChanged() {
                            updateTotalCost();
                        }

                        @Override
                        public void onItemRemoved(int position) {
                            updateTotalCost();
                        }
                    });
            binding.orderItemsRecyclerView.setAdapter(adapter);
            binding.descriptionInput.setText(""); // Clear description
            binding.paymentMethodSpinner.setSelection(0); // Reset to "Cash"
            binding.qrCodeImage.setVisibility(View.GONE); // Hide QR code
            updateTotalCost();
        });

        binding.placeOrderButton.setOnClickListener(v -> placeOrder());
    }

    private void setupPaymentMethodSpinner() {
        String[] paymentMethods = {"Cash", "Net Banking"};
        paymentMethodAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, paymentMethods);
        paymentMethodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.paymentMethodSpinner.setAdapter(paymentMethodAdapter);

        binding.paymentMethodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedMethod = paymentMethods[position];
                binding.qrCodeImage.setVisibility(selectedMethod.equals("Net Banking") ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                binding.qrCodeImage.setVisibility(View.GONE);
            }
        });
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
        productImageResIds.put("Bread Pakora", R.drawable.bread);
    }

    private void loadAvailableProducts() {
        inventoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                availableProducts.clear();
                availableProducts.add(""); // For "Select Product"
                Log.d("OrderProduct", "Inventory snapshot: " + dataSnapshot.toString());

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        String productName = snapshot.child("name").getValue(String.class);
                        String quantityStr = snapshot.child("quantity").getValue(String.class);
                        Long quantity = null;
                        if (quantityStr != null) {
                            try {
                                quantity = Long.parseLong(quantityStr);
                            } catch (NumberFormatException e) {
                                Log.e("OrderProduct", "Invalid quantity format for " + productName + ": " + quantityStr);
                            }
                        }
                        Log.d("OrderProduct", "Product: " + productName + ", Quantity: " + quantity);
                        if (productName != null && quantity != null && quantity > 0 && productPrices.containsKey(productName)) {
                            availableProducts.add(productName);
                        }
                    } catch (Exception e) {
                        Log.e("OrderProduct", "Error processing product: " + snapshot.getKey(), e);
                    }
                }

                // Set up adapter after data is loaded
                adapter = new OrderItemAdapter(OrderProductActivity.this, availableProducts, productPrices, productImageResIds,
                        new OrderItemAdapter.OnOrderItemChangeListener() {
                            @Override
                            public void onItemChanged() {
                                updateTotalCost();
                            }

                            @Override
                            public void onItemRemoved(int position) {
                                updateTotalCost();
                            }
                        });
                binding.orderItemsRecyclerView.setAdapter(adapter);
                binding.orderItemsRecyclerView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);

                if (availableProducts.size() <= 1) {
                    Toast.makeText(OrderProductActivity.this, "No products available", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("OrderProduct", "Loaded products: " + availableProducts.toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("OrderProduct", "Error: " + databaseError.getMessage());
                Toast.makeText(OrderProductActivity.this, "Failed to load products", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                binding.orderItemsRecyclerView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void updateTotalCost() {
        int total = 0;
        for (OrderItem item : adapter.getOrderItems()) {
            total += item.getTotal();
        }
        binding.totalCost.setText("Total Cost: " + total + " rupees");
    }

    private void placeOrder() {
        List<OrderItem> validItems = new ArrayList<>();
        for (OrderItem item : adapter.getOrderItems()) {
            if (item.getProduct() != null && !item.getProduct().isEmpty() && item.getQuantity() > 0) {
                validItems.add(item);
            }
        }

        if (validItems.isEmpty()) {
            Toast.makeText(this, "Please add at least one product", Toast.LENGTH_SHORT).show();
            return;
        }

        String description = binding.descriptionInput.getText().toString().trim();
        if (description.isEmpty()) {
            Toast.makeText(this, "Please enter an order description", Toast.LENGTH_SHORT).show();
            return;
        }

        String paymentMethod = binding.paymentMethodSpinner.getSelectedItem().toString().toLowerCase();
        String userId = mAuth.getCurrentUser().getUid();
        String userEmail = mAuth.getCurrentUser().getEmail();

        // Prompt for phone number
        AlertDialog.Builder phoneDialog = new AlertDialog.Builder(this);
        phoneDialog.setTitle("Enter Phone Number");
        final EditText phoneInput = new EditText(this);
        phoneInput.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        phoneDialog.setView(phoneInput);
        phoneDialog.setPositiveButton("Submit", (dialog, which) -> {
                    String phoneNumber = phoneInput.getText().toString().trim();
                    if (phoneNumber.isEmpty()) {
                        Toast.makeText(this, "Phone number required", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!phoneNumber.matches("\\d{10}")) {
                        Toast.makeText(this, "Please enter a valid 10-digit phone number", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Prompt for delivery address
                    AlertDialog.Builder addressDialog = new AlertDialog.Builder(this);
                    addressDialog.setTitle("Enter Delivery Address");
                    final EditText addressInput = new EditText(this);
                    addressDialog.setView(addressInput);
                    addressDialog.setPositiveButton("Submit", (dialog2, which2) -> {
                                String deliveryAddress = addressInput.getText().toString().trim();
                                if (deliveryAddress.isEmpty()) {
                                    Toast.makeText(this, "Delivery address required", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                placeOrderFinal(userId, userEmail, validItems, phoneNumber, deliveryAddress, paymentMethod, description);
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void placeOrderFinal(String customerId, String customerEmail, List<OrderItem> items, String phoneNumber, String deliveryAddress, String paymentMethod, String description) {
        CustomerOrder order = new CustomerOrder();
        order.setCustomerId(customerId);
        order.setCustomerEmail(customerEmail);
        order.setItems(items);
        order.setTotalAmount(items.stream().mapToInt(OrderItem::getTotal).sum());
        order.setStatus("pending");
        order.setTimestamp(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(new Date()));
        order.setDeliveryAddress(deliveryAddress);
        order.setPhoneNumber(phoneNumber);
        order.setPaymentMethod(paymentMethod);
        order.setDescription(description);

        ordersRef.push().setValue(order, (error, ref) -> {
            if (error == null) {
                Toast.makeText(this, "Order placed successfully", Toast.LENGTH_LONG).show();

                // Update inventory
                updateInventory(items);

                // Update product history
                updateProductHistory(customerId, items);

                // Clear order, description, payment method, and QR code
                adapter = new OrderItemAdapter(this, availableProducts, productPrices, productImageResIds,
                        new OrderItemAdapter.OnOrderItemChangeListener() {
                            @Override
                            public void onItemChanged() {
                                updateTotalCost();
                            }

                            @Override
                            public void onItemRemoved(int position) {
                                updateTotalCost();
                            }
                        });
                binding.orderItemsRecyclerView.setAdapter(adapter);
                binding.descriptionInput.setText("");
                binding.paymentMethodSpinner.setSelection(0); // Reset to "Cash"
                binding.qrCodeImage.setVisibility(View.GONE); // Hide QR code
                updateTotalCost();
            } else {
                Log.e("OrderProduct", "Error placing order: " + error.getMessage());
                Toast.makeText(this, "Failed to place order", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateInventory(List<OrderItem> items) {
        inventoryRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                Map<String, Object> inventory = (Map<String, Object>) snapshot.getValue();
                for (OrderItem item : items) {
                    String product = item.getProduct();
                    if (inventory.containsKey(product)) {
                        Map<String, Object> productData = (Map<String, Object>) inventory.get(product);
                        Object quantityObj = productData.get("quantity");
                        long currentQuantity = 0;
                        if (quantityObj instanceof Long) {
                            currentQuantity = (Long) quantityObj;
                        } else if (quantityObj instanceof String) {
                            try {
                                currentQuantity = Long.parseLong((String) quantityObj);
                            } catch (NumberFormatException e) {
                                Log.e("OrderProduct", "Invalid quantity format in inventory for " + product);
                            }
                        }
                        productData.put("quantity", String.valueOf(currentQuantity - item.getQuantity()));
                        inventory.put(product, productData);
                    }
                }
                inventoryRef.setValue(inventory).addOnFailureListener(e -> {
                    Log.e("OrderProduct", "Error updating inventory: " + e.toString());
                });
            }
        }).addOnFailureListener(e -> {
            Log.e("OrderProduct", "Error retrieving inventory: " + e.toString());
        });
    }

    private void updateProductHistory(String customerId, List<OrderItem> items) {
        DatabaseReference userHistoryRef = historyRef.child(customerId);
        userHistoryRef.get().addOnSuccessListener(snapshot -> {
            Map<String, Long> history = snapshot.exists() ? (Map<String, Long>) snapshot.getValue() : new HashMap<>();
            for (OrderItem item : items) {
                String product = item.getProduct();
                history.compute(product, (k, v) -> v == null ? 1L : v + 1);
            }
            userHistoryRef.setValue(history).addOnFailureListener(e -> {
                Log.e("OrderProduct", "Error updating history: " + e.toString());
            });
        }).addOnFailureListener(e -> {
            Log.e("OrderProduct", "Error retrieving history: " + e.toString());
        });
    }
}