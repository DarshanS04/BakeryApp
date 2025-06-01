package com.example.bakeryapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
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
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    private List<Map<String, Object>> rawMaterials;
    private List<Map<String, Object>> orderHistory;
    private ArrayAdapter<Map<String, Object>> ordersAdapter;

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
                for (DataSnapshot data : snapshot.getChildren()) {
                    Map<String, Object> material = new HashMap<>();
                    material.put("index", data.getKey());
                    material.put("name", data.child("name").getValue(String.class));
                    material.put("price", data.child("price").getValue(String.class));
                    rawMaterials.add(material);
                }
                // Update all spinners
                updateAllSpinners();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
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

        // Use user's email or UID as identifier
        String managerEmail = user.getEmail() != null ? user.getEmail() : user.getUid();
        String managerName = user.getDisplayName() != null ? user.getDisplayName() : managerEmail;

        // Collect order items
        List<Map<String, Object>> orderItems = new ArrayList<>();
        for (int i = 0; i < retailOrderItems.getChildCount(); i++) {
            View itemView = retailOrderItems.getChildAt(i);
            Spinner materialSpinner = itemView.findViewById(R.id.material_spinner);
            EditText quantityInput = itemView.findViewById(R.id.material_quantity);

            int selectedPosition = materialSpinner.getSelectedItemPosition();
            if (selectedPosition > 0 && !quantityInput.getText().toString().isEmpty()) {
                Map<String, Object> item = new HashMap<>();
                Map<String, Object> material = rawMaterials.get(selectedPosition - 1);
                item.put("material", material.get("name"));
                item.put("price", material.get("price"));
                item.put("quantity", Integer.parseInt(quantityInput.getText().toString()));
                orderItems.add(item);
            }
        }

        if (orderItems.isEmpty()) {
            Toast.makeText(RetailOrderActivity.this, "Please add at least one item", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create order object
        Map<String, Object> newOrder = new HashMap<>();
        newOrder.put("items", orderItems);
        newOrder.put("managerId", user.getUid());
        newOrder.put("managerName", managerName);
        newOrder.put("managerEmail", managerEmail);
        newOrder.put("status", "pending");
        newOrder.put("timestamp", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(new Date()));

        // Save to Firebase
        DatabaseReference retailOrdersRef = databaseReference.child("retailOrders");
        retailOrdersRef.push().setValue(newOrder, (error, ref) -> {
            if (error == null) {
                Toast.makeText(RetailOrderActivity.this, "Order placed successfully!", Toast.LENGTH_SHORT).show();
                resetForm();
                loadOrderHistory();
            } else {
                Toast.makeText(RetailOrderActivity.this, "Failed to place order: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
        ordersAdapter = new ArrayAdapter<Map<String, Object>>(this, R.layout.item_retail_order, orderHistory) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_retail_order, parent, false);
                }

                Map<String, Object> order = orderHistory.get(position);

                TextView orderIdView = convertView.findViewById(R.id.order_id);
                TextView orderDateView = convertView.findViewById(R.id.order_date);
                TextView orderMaterialsView = convertView.findViewById(R.id.order_materials);
                TextView orderTotalView = convertView.findViewById(R.id.order_total);
                TextView orderStatusView = convertView.findViewById(R.id.order_status);
                Button cancelButton = convertView.findViewById(R.id.cancel_button);
                Button reorderButton = convertView.findViewById(R.id.reorder_button);
                TextView rejectionReasonView = convertView.findViewById(R.id.rejection_reason);

                String orderId = (String) order.get("id");
                orderIdView.setText("Order ID: " + (orderId != null ? orderId.substring(Math.max(0, orderId.length() - 6)) : "N/A"));

                String timestamp = (String) order.get("timestamp");
                String orderDate = timestamp != null ? timestamp.substring(0, 10) : "Unknown"; // Extract YYYY-MM-DD
                orderDateView.setText("Date: " + orderDate);

                List<Map<String, Object>> items = (List<Map<String, Object>>) order.get("items");
                StringBuilder materials = new StringBuilder();
                double totalCost = 0.0;
                if (items != null) {
                    for (Map<String, Object> item : items) {
                        String material = (String) item.get("material");
                        Number quantity = (Number) item.get("quantity");
                        String priceStr = (String) item.get("price");
                        double price = priceStr != null ? Double.parseDouble(priceStr.replaceAll("[^0-9.]", "")) : 0;
                        materials.append(material).append(" (").append(quantity).append(")\n");
                        totalCost += price * (quantity != null ? quantity.intValue() : 0);
                    }
                }
                orderMaterialsView.setText("Materials:\n" + (materials.length() > 0 ? materials.toString() : "N/A"));
                orderTotalView.setText("Total: " + String.format(Locale.US, "%.2f", totalCost) + " rupees");

                String status = (String) order.get("status");
                orderStatusView.setText("Status: " + (status != null ? status : "N/A"));
                orderStatusView.setTextColor(getStatusColor(status));

                cancelButton.setVisibility(status != null && status.equalsIgnoreCase("pending") ? View.VISIBLE : View.GONE);
                reorderButton.setVisibility(status != null && status.equalsIgnoreCase("completed") ? View.VISIBLE : View.GONE);
                rejectionReasonView.setVisibility(status != null && status.equalsIgnoreCase("rejected") && order.get("rejectionReason") != null ? View.VISIBLE : View.GONE);
                if (rejectionReasonView.getVisibility() == View.VISIBLE) {
                    rejectionReasonView.setText("Reason: " + order.get("rejectionReason"));
                }

                cancelButton.setOnClickListener(v -> cancelOrder(orderId));
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
            case "completed": return getResources().getColor(android.R.color.holo_green_light);
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
            Toast.makeText(this, "Please log in to view order history", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseReference.child("retailOrders").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderHistory.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Map<String, Object> order = (Map<String, Object>) data.getValue();
                    if (order != null && user.getUid().equals(order.get("managerId"))) {
                        order.put("id", data.getKey());
                        orderHistory.add(order);
                    }
                }
                ordersAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RetailOrderActivity.this, "Error loading orders: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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
                Map<String, Object> order = (Map<String, Object>) snapshot.getValue();
                if (order != null && !"pending".equalsIgnoreCase((String) order.get("status"))) {
                    Toast.makeText(RetailOrderActivity.this, "Only pending orders can be cancelled", Toast.LENGTH_SHORT).show();
                    return;
                }
                Map<String, Object> updates = new HashMap<>();
                updates.put("status", "cancelled");
                updates.put("statusUpdatedAt", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(new Date()));
                orderRef.updateChildren(updates, (error, ref) -> {
                    if (error == null) {
                        Toast.makeText(RetailOrderActivity.this, "Order cancelled", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(RetailOrderActivity.this, "Failed to cancel order: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RetailOrderActivity.this, "Error fetching order: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void reorderOrder(Map<String, Object> order) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please log in to reorder", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> newOrder = new HashMap<>();
        newOrder.put("items", order.get("items"));
        newOrder.put("managerId", user.getUid());
        newOrder.put("managerName", order.get("managerName"));
        newOrder.put("managerEmail", order.get("managerEmail"));
        newOrder.put("status", "pending");
        newOrder.put("timestamp", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(new Date()));
        newOrder.put("reorderedFrom", order.get("id"));

        DatabaseReference retailOrdersRef = databaseReference.child("retailOrders");
        retailOrdersRef.push().setValue(newOrder, (error, ref) -> {
            if (error == null) {
                Toast.makeText(RetailOrderActivity.this, "Reorder placed successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(RetailOrderActivity.this, "Failed to place reorder: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}