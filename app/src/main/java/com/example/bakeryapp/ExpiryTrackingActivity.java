package com.example.bakeryapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ExpiryTrackingActivity extends AppCompatActivity {

    private ListView expiryListView;
    private DatabaseReference inventoryRef;
    private List<ProductGroup> productGroups;
    private ExpiryAdapter adapter;
    private FirebaseAuth mAuth;
    private TextView loadingText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expiry_tracking);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        inventoryRef = FirebaseDatabase.getInstance().getReference("inventory");

        // Initialize UI
        expiryListView = findViewById(R.id.expiryListView);
        loadingText = findViewById(R.id.loadingText);
        productGroups = new ArrayList<>();
        adapter = new ExpiryAdapter();
        expiryListView.setAdapter(adapter);

        // Load data if user is authenticated
        if (mAuth.getCurrentUser() != null) {
            loadProductExpiry();
        } else {
            Toast.makeText(this, "Please log in to view expiry data", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    private void loadProductExpiry() {
        productGroups.clear();
        expiryListView.setAdapter(null); // Clear list
        adapter.notifyDataSetChanged();

        loadingText.setVisibility(View.VISIBLE);
        loadingText.setText("Loading expiry data...");

        inventoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Map<String, ProductGroup> productMap = new HashMap<>();
                    Date today = new Date();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

                    for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                        InventoryItem item = itemSnapshot.getValue(InventoryItem.class);
                        if (item == null) continue;
                        item.setKey(itemSnapshot.getKey());

                        String productName = item.getName();
                        if (!productMap.containsKey(productName)) {
                            productMap.put(productName, new ProductGroup(productName));
                        }

                        ProductGroup group = productMap.get(productName);
                        int quantity = Integer.parseInt(item.getQuantity());
                        group.setQuantity(group.getQuantity() + quantity);

                        try {
                            Date expiryDate = sdf.parse(item.getExpiryDate());
                            long diffDays = (expiryDate.getTime() - today.getTime()) / (1000 * 60 * 60 * 24);

                            if (diffDays < 0) {
                                group.setExpired(group.getExpired() + quantity);
                                group.getExpiredBatches().add(item.getBatchNumber());
                            } else if (diffDays <= 1) {
                                group.setExpirySoon(group.getExpirySoon() + quantity);
                                group.getExpiringSoonBatches().add(item.getBatchNumber());
                            }

                            if (group.getNearestExpiry() == null || expiryDate.before(group.getNearestExpiry())) {
                                group.setNearestExpiry(expiryDate);
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }

                    productGroups.addAll(productMap.values());
                    Collections.sort(productGroups, new Comparator<ProductGroup>() {
                        @Override
                        public int compare(ProductGroup a, ProductGroup b) {
                            if (a.getExpired() > 0 && b.getExpired() == 0) return -1;
                            if (a.getExpired() == 0 && b.getExpired() > 0) return 1;
                            if (a.getExpirySoon() > 0 && b.getExpirySoon() == 0) return -1;
                            if (a.getExpirySoon() == 0 && b.getExpirySoon() > 0) return 1;
                            return a.getNearestExpiry().compareTo(b.getNearestExpiry());
                        }
                    });

                    adapter.notifyDataSetChanged();
                    expiryListView.setAdapter(adapter);
                    loadingText.setVisibility(View.GONE);

                    if (productGroups.isEmpty()) {
                        loadingText.setVisibility(View.VISIBLE);
                        loadingText.setText("No inventory items found.");
                    }
                } else {
                    loadingText.setVisibility(View.VISIBLE);
                    loadingText.setText("No inventory items found.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadingText.setVisibility(View.VISIBLE);
                loadingText.setText("Error loading expiry data: " + error.getMessage());
                Toast.makeText(ExpiryTrackingActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void discardExpiredItem(String name, String expiryDate) {
        inventoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    List<String> keysToDelete = new ArrayList<>();
                    for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                        InventoryItem item = itemSnapshot.getValue(InventoryItem.class);
                        if (item != null && item.getName().equals(name) && item.getExpiryDate().equals(expiryDate)) {
                            keysToDelete.add(itemSnapshot.getKey());
                        }
                    }

                    if (keysToDelete.isEmpty()) {
                        Toast.makeText(ExpiryTrackingActivity.this, "No matching expired item found.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (String key : keysToDelete) {
                        inventoryRef.child(key).removeValue((error, ref) -> {
                            if (error == null) {
                                Toast.makeText(ExpiryTrackingActivity.this, "Expired item(s) discarded!", Toast.LENGTH_SHORT).show();
                                loadProductExpiry();
                            } else {
                                Toast.makeText(ExpiryTrackingActivity.this, "Failed to discard item: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ExpiryTrackingActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class ExpiryAdapter extends ArrayAdapter<ProductGroup> {
        ExpiryAdapter() {
            super(ExpiryTrackingActivity.this, R.layout.expiry_item, productGroups);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.expiry_item, parent, false);
            }

            ProductGroup product = getItem(position);
            if (product == null) return convertView;

            TextView productNameText = convertView.findViewById(R.id.productName);
            TextView quantityText = convertView.findViewById(R.id.quantity);
            TextView batchNumbersText = convertView.findViewById(R.id.batchNumbers);
            TextView expiryDateText = convertView.findViewById(R.id.expiryDate);
            TextView statusText = convertView.findViewById(R.id.status);
            Button discardButton = convertView.findViewById(R.id.discardButton);

            productNameText.setText(product.getName());
            quantityText.setText(String.valueOf(product.getQuantity()));

            String batchNumbers = product.getExpiredBatches().isEmpty() && product.getExpiringSoonBatches().isEmpty()
                    ? "-"
                    : (!product.getExpiredBatches().isEmpty() ? product.getExpiredBatches() : product.getExpiringSoonBatches()).toString().replaceAll("[\\[\\]]", "");
            batchNumbersText.setText(batchNumbers);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            expiryDateText.setText(product.getNearestExpiry() != null ? sdf.format(product.getNearestExpiry()) : "-");

            String status;
            int statusColor;
            if (product.getExpired() > 0) {
                status = "Expired";
                statusColor = getResources().getColor(android.R.color.holo_red_dark);
                discardButton.setVisibility(View.VISIBLE);
                discardButton.setOnClickListener(v -> discardExpiredItem(product.getName(), sdf.format(product.getNearestExpiry())));
            } else if (product.getExpirySoon() > 0) {
                status = "Expiring Soon";
                statusColor = getResources().getColor(android.R.color.holo_orange_dark);
                discardButton.setVisibility(View.GONE);
            } else {
                status = "Good";
                statusColor = getResources().getColor(android.R.color.holo_green_dark);
                discardButton.setVisibility(View.GONE);
            }
            statusText.setText(status);
            statusText.setTextColor(statusColor);

            return convertView;
        }
    }

    // Model classes
    public static class InventoryItem {
        private String key;
        private String name;
        private String quantity;
        private String expiryDate;
        private String batchNumber;

        public InventoryItem() {}

        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }
        public String getName() { return name; }
        public String getQuantity() { return quantity; }
        public String getExpiryDate() { return expiryDate; }
        public String getBatchNumber() { return batchNumber; }
    }

    public static class ProductGroup {
        private String name;
        private int quantity;
        private int expirySoon;
        private int expired;
        private Date nearestExpiry;
        private List<String> expiredBatches;
        private List<String> expiringSoonBatches;

        public ProductGroup(String name) {
            this.name = name;
            this.quantity = 0;
            this.expirySoon = 0;
            this.expired = 0;
            this.expiredBatches = new ArrayList<>();
            this.expiringSoonBatches = new ArrayList<>();
        }

        public String getName() { return name; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public int getExpirySoon() { return expirySoon; }
        public void setExpirySoon(int expirySoon) { this.expirySoon = expirySoon; }
        public int getExpired() { return expired; }
        public void setExpired(int expired) { this.expired = expired; }
        public Date getNearestExpiry() { return nearestExpiry; }
        public void setNearestExpiry(Date nearestExpiry) { this.nearestExpiry = nearestExpiry; }
        public List<String> getExpiredBatches() { return expiredBatches; }
        public List<String> getExpiringSoonBatches() { return expiringSoonBatches; }
    }
}