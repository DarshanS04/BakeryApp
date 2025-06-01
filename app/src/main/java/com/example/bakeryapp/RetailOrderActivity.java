package com.example.bakeryapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

public class RetailOrderActivity extends AppCompatActivity {

    private LinearLayout retailOrderItems;
    private Button addItemButton, placeOrderButton;
    private ListView retailOrdersList;
    private TextView emptyOrdersText;
    private ProgressBar ordersLoading;
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    private List<Map<String, Object>> rawMaterials;
    private List<RetailOrder> orderHistory;
    private ArrayAdapter<RetailOrder> ordersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retail_order);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Initialize UI components
        retailOrderItems = findViewById(R.id.retail_order_items);
        addItemButton = findViewById(R.id.add_item_button);
        placeOrderButton = findViewById(R.id.place_order_button);
        retailOrdersList = findViewById(R.id.retail_orders_list);
        emptyOrdersText = findViewById(R.id.empty_orders_text);
        ordersLoading = findViewById(R.id.orders_loading);

        // Initialize data lists
        rawMaterials = new ArrayList<>();
        orderHistory = new ArrayList<>();

        // Hide remove button for the first item row
        View firstItemRow = retailOrderItems.getChildAt(0);
        if (firstItemRow != null) {
            firstItemRow.findViewById(R.id.remove_item_button).setVisibility(View.GONE);
        }

        // Load raw materials and order history
        loadRawMaterials();
        setupOrderHistory();

        // Add item button listener
        addItemButton.setOnClickListener(v -> addNewItemRow());

        // Place order button listener
        placeOrderButton.setOnClickListener(v -> placeRetailOrder());
    }

    private void loadRawMaterials() {
        databaseReference.child("raw").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                rawMaterials.clear();
                int index = 0;
                for (DataSnapshot data : snapshot.getChildren()) {
                    Map<String, Object> material = new HashMap<>();
                    material.put("index", index++);
                    material.put("name", data.child("name").getValue(String.class));
                    material.put("price", data.child("price").getValue(String.class));
                    rawMaterials.add(material);
                }
                Log.d("RetailOrderActivity", "Loaded " + rawMaterials.size() + " raw materials");
                updateAllSpinners();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("RetailOrderActivity", "Error loading raw materials: " + error.getMessage());
                Toast.makeText(RetailOrderActivity.this, "Error loading raw materials: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateAllSpinners() {
        for (int i = 0; i < retailOrderItems.getChildCount(); i++) {
            View itemView = retailOrderItems.getChildAt(i);
            Spinner materialSpinner = itemView.findViewById(R.id.material_spinner);
            List<String> materialNames = new ArrayList<>();
            materialNames.add("Select Material");
            for (Map<String, Object> material : rawMaterials) {
                materialNames.add(material.get("name") + " - " + material.get("price"));
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(RetailOrderActivity.this, android.R.layout.simple_spinner_item, materialNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            materialSpinner.setAdapter(adapter);
        }
    }

    private void addNewItemRow() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View itemRow = inflater.inflate(R.layout.item_retail_order_row, retailOrderItems, false);

        // Set up remove button
        Button removeButton = itemRow.findViewById(R.id.remove_item_button);
        removeButton.setOnClickListener(v -> retailOrderItems.removeView(itemRow));

        // Set up spinner with raw materials
        Spinner materialSpinner = itemRow.findViewById(R.id.material_spinner);
        List<String> materialNames = new ArrayList<>();
        materialNames.add("Select Material");
        for (Map<String, Object> material : rawMaterials) {
            materialNames.add(material.get("name") + " - " + material.get("price"));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, materialNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        materialSpinner.setAdapter(adapter);

        retailOrderItems.addView(itemRow);
    }

    private void placeRetailOrder() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please log in to place an order", Toast.LENGTH_SHORT).show();
            return;
        }

        String managerEmail = user.getEmail() != null ? user.getEmail() : user.getUid();
        List<RetailOrder.OrderItem> orderItems = new ArrayList<>();
        double orderTotal = 0.0;

        // Collect order items
        for (int i = 0; i < retailOrderItems.getChildCount(); i++) {
            View itemView = retailOrderItems.getChildAt(i);
            Spinner materialSpinner = itemView.findViewById(R.id.material_spinner);
            EditText quantityInput = itemView.findViewById(R.id.material_quantity);

            int selectedPosition = materialSpinner.getSelectedItemPosition();
            if (selectedPosition > 0 && !quantityInput.getText().toString().isEmpty()) {
                Map<String, Object> material = rawMaterials.get(selectedPosition - 1);
                int quantity;
                try {
                    quantity = Integer.parseInt(quantityInput.getText().toString());
                    if (quantity <= 0) {
                        Toast.makeText(this, "Quantity must be positive", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid quantity", Toast.LENGTH_SHORT).show();
                    return;
                }

                String priceStr = (String) material.get("price");
                double price = parsePrice(priceStr);
                double itemTotal = price * quantity;

                RetailOrder.OrderItem item = new RetailOrder.OrderItem(
                        ((Number) material.get("index")).intValue(),
                        (String) material.get("name"),
                        priceStr,
                        quantity,
                        itemTotal
                );
                orderItems.add(item);
                orderTotal += itemTotal;
            }
        }

        if (orderItems.isEmpty()) {
            Toast.makeText(this, "Please add at least one item", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create order object
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        String timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(new Date());
        RetailOrder newOrder = new RetailOrder(
                date,
                orderItems,
                managerEmail,
                user.getUid(),
                "Pending",
                timestamp,
                orderTotal
        );

        // Save to Firebase
        DatabaseReference retailOrdersRef = databaseReference.child("retailOrders");
        retailOrdersRef.push().setValue(newOrder, (error, ref) -> {
            if (error == null) {
                Toast.makeText(RetailOrderActivity.this, "Order placed successfully!", Toast.LENGTH_SHORT).show();
                resetForm();
            } else {
                Log.e("RetailOrderActivity", "Failed to place order: " + error.getMessage());
                Toast.makeText(RetailOrderActivity.this, "Failed to place order: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private double parsePrice(String priceStr) {
        if (priceStr == null) return 0.0;
        try {
            return Double.parseDouble(priceStr.replaceAll("[^0-9.]", ""));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private void resetForm() {
        retailOrderItems.removeAllViews();
        // Add one empty item row
        LayoutInflater inflater = LayoutInflater.from(this);
        View itemRow = inflater.inflate(R.layout.item_retail_order_row, retailOrderItems, false);
        itemRow.findViewById(R.id.remove_item_button).setVisibility(View.GONE);
        retailOrderItems.addView(itemRow);
        updateAllSpinners();
    }

    private void setupOrderHistory() {
        ordersAdapter = new ArrayAdapter<RetailOrder>(this, R.layout.item_retail_order, orderHistory) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_retail_order, parent, false);
                }

                RetailOrder order = orderHistory.get(position);

                TextView orderIdView = convertView.findViewById(R.id.order_id);
                TextView orderDateView = convertView.findViewById(R.id.order_date);
                TextView orderMaterialsView = convertView.findViewById(R.id.order_materials);
                TextView orderTotalView = convertView.findViewById(R.id.order_total);
                TextView orderStatusView = convertView.findViewById(R.id.order_status);
                Button cancelButton = convertView.findViewById(R.id.cancel_button);
                Button reorderButton = convertView.findViewById(R.id.reorder_button);
                TextView rejectionReasonView = convertView.findViewById(R.id.rejection_reason);

                orderIdView.setText("Order ID: " + (order.getId() != null ? order.getId().substring(Math.max(0, order.getId().length() - 6)) : "N/A"));
                orderDateView.setText("Date: " + (order.getDate() != null ? order.getDate() : "Unknown"));

                StringBuilder materials = new StringBuilder();
                if (order.getItems() != null) {
                    for (RetailOrder.OrderItem item : order.getItems()) {
                        materials.append(item.getMaterialName()).append(" (").append(item.getQuantity()).append(")\n");
                    }
                }
                orderMaterialsView.setText("Materials:\n" + (materials.length() > 0 ? materials.toString() : "N/A"));
                orderTotalView.setText("Total: " + String.format(Locale.US, "%.2f", order.getTotal()) + " rupees");

                orderStatusView.setText("Status: " + (order.getStatus() != null ? order.getStatus() : "N/A"));
                orderStatusView.setTextColor(getStatusColor(order.getStatus()));

                cancelButton.setVisibility(order.getStatus() != null && order.getStatus().equalsIgnoreCase("Pending") ? View.VISIBLE : View.GONE);
                reorderButton.setVisibility(order.getStatus() != null && order.getStatus().equalsIgnoreCase("Accepted") ? View.VISIBLE : View.GONE);
                rejectionReasonView.setVisibility(order.getStatus() != null && order.getStatus().equalsIgnoreCase("Rejected") && order.getRejectionReason() != null ? View.VISIBLE : View.GONE);
                if (rejectionReasonView.getVisibility() == View.VISIBLE) {
                    rejectionReasonView.setText("Reason: " + order.getRejectionReason());
                }

                cancelButton.setOnClickListener(v -> cancelOrder(order.getId()));
                reorderButton.setOnClickListener(v -> reorderOrder(order));

                return convertView;
            }
        };
        retailOrdersList.setAdapter(ordersAdapter);
        loadOrderHistory();
    }

    private int getStatusColor(String status) {
        if (status == null) return getResources().getColor(android.R.color.black);
        switch (status.toLowerCase()) {
            case "pending": return getResources().getColor(android.R.color.holo_orange_light);
            case "accepted": return getResources().getColor(android.R.color.holo_green_light);
            case "rejected": return getResources().getColor(android.R.color.holo_red_light);
            case "cancelled": return getResources().getColor(android.R.color.darker_gray);
            default: return getResources().getColor(android.R.color.black);
        }
    }

    private void loadOrderHistory() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            orderHistory.clear();
            ordersAdapter.notifyDataSetChanged();
            emptyOrdersText.setVisibility(View.VISIBLE);
            ordersLoading.setVisibility(View.GONE);
            Log.e("RetailOrderActivity", "User not authenticated");
            Toast.makeText(this, "Please log in to view order history", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("RetailOrderActivity", "Loading all orders for user: " + user.getUid());
        ordersLoading.setVisibility(View.VISIBLE);
        emptyOrdersText.setVisibility(View.GONE);
        databaseReference.child("retailOrders").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderHistory.clear();
                int orderCount = 0;
                Log.d("RetailOrderActivity", "Raw snapshot: " + snapshot.getValue());
                for (DataSnapshot data : snapshot.getChildren()) {
                    try {
                        RetailOrder order = data.getValue(RetailOrder.class);
                        if (order != null) {
                            order.setId(data.getKey());
                            orderHistory.add(order);
                            orderCount++;
                            Log.d("RetailOrderActivity", "Added order: " + order.getId() + ", Status: " + order.getStatus() + ", ManagerId: " + order.getManagerId());
                        } else {
                            Log.w("RetailOrderActivity", "Null order for key: " + data.getKey());
                        }
                    } catch (Exception e) {
                        Log.e("RetailOrderActivity", "Error deserializing order " + data.getKey() + ": " + e.getMessage());
                    }
                }
                ordersAdapter.notifyDataSetChanged();
                ordersLoading.setVisibility(View.GONE);
                emptyOrdersText.setVisibility(orderHistory.isEmpty() ? View.VISIBLE : View.GONE);
                Log.d("RetailOrderActivity", "Loaded " + orderCount + " orders");
                if (orderHistory.isEmpty()) {
                    Toast.makeText(RetailOrderActivity.this, "No orders found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("RetailOrderActivity", "Error loading orders: " + error.getMessage());
                Toast.makeText(RetailOrderActivity.this, "Failed to load orders: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                ordersLoading.setVisibility(View.GONE);
                emptyOrdersText.setVisibility(View.VISIBLE);
            }
        });
    }

    private void cancelOrder(String orderId) {
        if (orderId == null) return;
        DatabaseReference orderRef = databaseReference.child("retailOrders").child(orderId);
        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(RetailOrderActivity.this, "Order not found", Toast.LENGTH_SHORT).show();
                    return;
                }
                RetailOrder order = snapshot.getValue(RetailOrder.class);
                if (order != null && !"Pending".equalsIgnoreCase(order.getStatus())) {
                    Toast.makeText(RetailOrderActivity.this, "Only pending orders can be cancelled", Toast.LENGTH_SHORT).show();
                    return;
                }
                Map<String, Object> updates = new HashMap<>();
                updates.put("status", "Cancelled");
                updates.put("statusUpdatedAt", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(new Date()));
                orderRef.updateChildren(updates, (error, ref) -> {
                    if (error == null) {
                        Toast.makeText(RetailOrderActivity.this, "Order cancelled", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("RetailOrderActivity", "Failed to cancel order: " + error.getMessage());
                        Toast.makeText(RetailOrderActivity.this, "Failed to cancel order: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("RetailOrderActivity", "Error fetching order: " + error.getMessage());
                Toast.makeText(RetailOrderActivity.this, "Failed to retrieve order: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void reorderOrder(RetailOrder order) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please log in to reorder", Toast.LENGTH_SHORT).show();
            return;
        }

        RetailOrder newOrder = new RetailOrder(
                new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date()),
                order.getItems(),
                order.getManagerEmail(),
                user.getUid(),
                "Pending",
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(new Date()),
                order.getTotal()
        );

        DatabaseReference retailOrdersRef = databaseReference.child("retailOrders");
        retailOrdersRef.push().setValue(newOrder, (error, ref) -> {
            if (error == null) {
                Toast.makeText(RetailOrderActivity.this, "Reorder placed successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Log.e("RetailOrderActivity", "Failed to place reorder: " + error.getMessage());
                Toast.makeText(RetailOrderActivity.this, "Failed to place reorder: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
