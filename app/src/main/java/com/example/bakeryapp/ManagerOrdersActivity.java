package com.example.bakeryapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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

public class ManagerOrdersActivity extends AppCompatActivity {

    private RecyclerView ordersRecyclerView;
    private ProgressBar progressBar;
    private TextView noOrdersText;
    private OrderAdapter orderAdapter;
    private List<Order> orderList;
    private DatabaseReference ordersRef, billsRef, inventoryRef;
    private static final int STORAGE_PERMISSION_CODE = 100;
    private String pendingBillId; // Store billId for retry after permission

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_orders);

        // Initialize Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        ordersRef = database.getReference("orders");
        billsRef = database.getReference("bills");
        inventoryRef = database.getReference("inventory");

        // Initialize UI
        ordersRecyclerView = findViewById(R.id.ordersRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        noOrdersText = findViewById(R.id.noOrdersText);
        orderList = new ArrayList<>();
        orderAdapter = new OrderAdapter(orderList, this::onExecuteOrder, this::onViewBill);
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ordersRecyclerView.setAdapter(orderAdapter);

        // Load orders
        loadCustomerOrders();
    }

    private void loadCustomerOrders() {
        progressBar.setVisibility(View.VISIBLE);
        noOrdersText.setVisibility(View.GONE);
        ordersRecyclerView.setVisibility(View.GONE);

        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderList.clear();
                for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                    Order order = orderSnapshot.getValue(Order.class);
                    if (order != null) {
                        order.setId(orderSnapshot.getKey());
                        orderList.add(order);
                    }
                }
                Collections.sort(orderList, (o1, o2) -> o2.getTimestamp().compareTo(o1.getTimestamp()));
                progressBar.setVisibility(View.GONE);
                if (orderList.isEmpty()) {
                    noOrdersText.setVisibility(View.VISIBLE);
                    ordersRecyclerView.setVisibility(View.GONE);
                } else {
                    noOrdersText.setVisibility(View.GONE);
                    ordersRecyclerView.setVisibility(View.VISIBLE);
                }
                orderAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                noOrdersText.setText("Error loading orders: " + error.getMessage());
                noOrdersText.setVisibility(View.VISIBLE);
                Toast.makeText(ManagerOrdersActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onExecuteOrder(String orderId) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Execution")
                .setMessage("Are you sure you want to execute this order?")
                .setPositiveButton("Yes", (dialog, which) -> executeOrder(orderId))
                .setNegativeButton("No", null)
                .show();
    }

    private void executeOrder(String orderId) {
        progressBar.setVisibility(View.VISIBLE);
        DatabaseReference orderRef = ordersRef.child(orderId);
        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Order order = snapshot.getValue(Order.class);
                if (order == null) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ManagerOrdersActivity.this, "Order not found", Toast.LENGTH_SHORT).show();
                    return;
                }
                if ("completed".equals(order.getStatus())) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ManagerOrdersActivity.this, "Order is already completed", Toast.LENGTH_SHORT).show();
                    return;
                }

                checkAndUpdateInventory(order.getItems(), () -> {
                    order.setStatus("completed");
                    if (order.getBillId() == null) {
                        Bill bill = new Bill();
                        bill.setOrderId(orderId);
                        bill.setCustomerEmail(order.getCustomerEmail() != null ? order.getCustomerEmail() : "");
                        bill.setItems(order.getItems());
                        bill.setTotalAmount(order.getTotalAmount());
                        bill.setDeliveryAddress(order.getDeliveryAddress() != null ? order.getDeliveryAddress() : "");
                        bill.setPhoneNumber(order.getPhoneNumber() != null ? order.getPhoneNumber() : "");
                        bill.setPaymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod() : "Cash");
                        bill.setStatus("completed");
                        bill.setTimestamp(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(new Date()));
                        bill.setCompletionDate(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()));
                        bill.setBillNumber(generateBillNumber());

                        DatabaseReference newBillRef = billsRef.push();
                        newBillRef.setValue(bill, (error, ref) -> {
                            if (error != null) {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(ManagerOrdersActivity.this, "Error generating bill: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            } else {
                                order.setBillId(newBillRef.getKey());
                                orderRef.setValue(order, (error2, ref2) -> {
                                    progressBar.setVisibility(View.GONE);
                                    if (error2 != null) {
                                        Toast.makeText(ManagerOrdersActivity.this, "Error updating order: " + error2.getMessage(), Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(ManagerOrdersActivity.this, "Order executed and bill generated", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                    } else {
                        orderRef.setValue(order, (error, ref) -> {
                            progressBar.setVisibility(View.GONE);
                            if (error != null) {
                                Toast.makeText(ManagerOrdersActivity.this, "Error updating order: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ManagerOrdersActivity.this, "Order executed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }, errorMessage -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ManagerOrdersActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ManagerOrdersActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkAndUpdateInventory(List<Item> items, Runnable onSuccess, OnErrorListener onError) {
        inventoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean sufficientStock = true;
                List<InventoryUpdate> updates = new ArrayList<>();
                for (Item item : items) {
                    boolean found = false;
                    for (DataSnapshot invSnapshot : snapshot.getChildren()) {
                        ManagerInventoryItem invItem = invSnapshot.getValue(ManagerInventoryItem.class);
                        if (invItem != null && item.getProduct().equals(invItem.getName())) {
                            found = true;
                            int available = invItem.getQuantityAsInt();
                            if (available < item.getQuantity()) {
                                sufficientStock = false;
                                break;
                            } else {
                                updates.add(new InventoryUpdate(invSnapshot.getKey(), available - item.getQuantity()));
                            }
                        }
                    }
                    if (!found) {
                        sufficientStock = false;
                        break;
                    }
                    if (!sufficientStock) break;
                }
                if (!sufficientStock) {
                    onError.onError("Insufficient stock for one or more items");
                    return;
                }
                for (InventoryUpdate update : updates) {
                    inventoryRef.child(update.key).child("quantity").setValue(String.valueOf(update.newQuantity));
                }
                onSuccess.run();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                onError.onError("Error checking inventory: " + error.getMessage());
            }
        });
    }

    private String generateBillNumber() {
        String dateStr = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        int random = (int) (1000 + Math.random() * 9000);
        return "BILL-" + dateStr + "-" + random;
    }

    private void onViewBill(String billId) {
        pendingBillId = billId; // Store billId for retry
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                new AlertDialog.Builder(this)
                        .setTitle("Storage Permission Needed")
                        .setMessage("This app needs storage permission to save PDF bills. Please grant the permission.")
                        .setPositiveButton("OK", (dialog, which) -> ActivityCompat.requestPermissions(
                                ManagerOrdersActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                STORAGE_PERMISSION_CODE))
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            Toast.makeText(this, "Permission denied, PDF generation disabled", Toast.LENGTH_SHORT).show();
                            BillDialog.showBillDialog(this, billId, false); // Show dialog without PDF
                        })
                        .show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
            }
            return;
        }
        BillDialog.showBillDialog(this, billId, true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Storage permission granted", Toast.LENGTH_SHORT).show();
                if (pendingBillId != null) {
                    BillDialog.showBillDialog(this, pendingBillId, true);
                    pendingBillId = null;
                }
            } else {
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    new AlertDialog.Builder(this)
                            .setTitle("Permission Denied")
                            .setMessage("Storage permission was permanently denied. Please enable it in app settings to generate PDFs.")
                            .setPositiveButton("Go to Settings", (dialog, which) -> {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.fromParts("package", getPackageName(), null));
                                startActivity(intent);
                            })
                            .setNegativeButton("Cancel", (dialog, which) -> {
                                if (pendingBillId != null) {
                                    BillDialog.showBillDialog(this, pendingBillId, false);
                                    pendingBillId = null;
                                }
                            })
                            .show();
                } else if (pendingBillId != null) {
                    BillDialog.showBillDialog(this, pendingBillId, false);
                    pendingBillId = null;
                }
            }
        }
    }

    private interface OnErrorListener {
        void onError(String message);
    }

    private static class InventoryUpdate {
        String key;
        int newQuantity;

        InventoryUpdate(String key, int newQuantity) {
            this.key = key;
            this.newQuantity = newQuantity;
        }
    }
}