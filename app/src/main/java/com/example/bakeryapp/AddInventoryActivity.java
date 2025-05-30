package com.example.bakeryapp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bakeryapp.databinding.ActivityAddInventoryBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class AddInventoryActivity extends AppCompatActivity {
    private ActivityAddInventoryBinding binding;
    private DatabaseReference inventoryRef;
    private InventoryItem editItem;
    private Calendar selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddInventoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        inventoryRef = FirebaseDatabase.getInstance().getReference("inventory");
        selectedDate = Calendar.getInstance();

        // Populate product dropdown
        String[] products = {"Bread", "Cake", "Pastry", "Cookies", "Brownie", "Croissant", "Cup Cake", "Dispasand",
                "Egg Puff", "Garlic Loaf", "Masala Bun", "Pav Bread", "Rusk", "Samosa", "Veg Puff", "Cheese Sandwich", "Bread Pakora"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, products);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.productSpinner.setAdapter(adapter);

        // Set up date picker
        binding.manufactureDateEditText.setOnClickListener(v -> showDatePickerDialog());

        // Check for edit
        editItem = (InventoryItem) getIntent().getSerializableExtra("item");
        if (editItem != null) {
            binding.productSpinner.setSelection(adapter.getPosition(editItem.getName()));
            binding.quantityEditText.setText(editItem.getQuantity());
            binding.manufactureDateEditText.setText(editItem.getManufactureDate());
            binding.submitButton.setText("Update Item");
            // Set selected date for edit
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                Date date = sdf.parse(editItem.getManufactureDate());
                if (date != null) {
                    selectedDate.setTime(date);
                }
            } catch (Exception e) {
                Log.e("AddInventory", "Error parsing edit date: " + e.getMessage());
            }
        }

        // Submit button
        binding.submitButton.setOnClickListener(v -> submitItem());
    }

    private void showDatePickerDialog() {
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(year, month, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    binding.manufactureDateEditText.setText(sdf.format(selectedDate.getTime()));
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        // Optional: Prevent future dates
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.show();
    }

    private void submitItem() {
        String product = binding.productSpinner.getSelectedItem() != null ? binding.productSpinner.getSelectedItem().toString() : "";
        String quantity = binding.quantityEditText.getText().toString().trim();
        String manufactureDate = binding.manufactureDateEditText.getText().toString().trim();

        if (product.isEmpty() || quantity.isEmpty() || manufactureDate.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int qty = Integer.parseInt(quantity);
            if (qty <= 0) {
                Toast.makeText(this, "Quantity must be positive", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid quantity", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate manufacture date format
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        sdf.setLenient(false);
        Date manufDate;
        try {
            manufDate = sdf.parse(manufactureDate);
        } catch (Exception e) {
            Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show();
            return;
        }

        // Calculate expiry date (3 days after manufacture)
        Date expiry = new Date(manufDate.getTime() + 3 * 24 * 60 * 60 * 1000);
        String expiryDate = sdf.format(expiry);
        String status = checkStatus(expiryDate);
        String statusClass = getStatusClass(status);
        String batchNumber = editItem != null ? editItem.getBatchNumber() : generateBatchNumber();
        String timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(new Date());

        // Check for duplicate batch number (if adding new item)
        if (editItem == null) {
            inventoryRef.orderByChild("batchNumber").equalTo(batchNumber).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Toast.makeText(AddInventoryActivity.this, "Batch number already exists", Toast.LENGTH_SHORT).show();
                        binding.progressBar.setVisibility(View.GONE);
                        binding.submitButton.setEnabled(true);
                    } else {
                        saveItem(product, quantity, manufactureDate, batchNumber, expiryDate, status, statusClass, timestamp);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("AddInventory", "Error checking batch number: " + error.getMessage());
                    Toast.makeText(AddInventoryActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    binding.progressBar.setVisibility(View.GONE);
                    binding.submitButton.setEnabled(true);
                }
            });
        } else {
            saveItem(product, quantity, manufactureDate, batchNumber, expiryDate, status, statusClass, timestamp);
        }
    }

    private void saveItem(String product, String quantity, String manufactureDate, String batchNumber, String expiryDate, String status, String statusClass, String timestamp) {
        InventoryItem item = new InventoryItem(product, quantity, manufactureDate, batchNumber, expiryDate, status, statusClass, timestamp);
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.submitButton.setEnabled(false);

        DatabaseReference ref = editItem != null ? inventoryRef.child(editItem.getKey()) : inventoryRef.push();
        ref.setValue(item, (error, ref1) -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.submitButton.setEnabled(true);
            if (error == null) {
                Toast.makeText(AddInventoryActivity.this, editItem != null ? "Item updated" : "Item added", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Log.e("AddInventory", "Error saving item: " + error.getMessage());
                Toast.makeText(AddInventoryActivity.this, "Failed to save: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String generateBatchNumber() {
        return String.valueOf(10000000 + new Random().nextInt(90000000));
    }

    private String checkStatus(String expiryDate) {
        try {
            Date today = new Date();
            Date expiry = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(expiryDate);
            long diff = expiry.getTime() - today.getTime();
            long days = diff / (1000 * 60 * 60 * 24);
            if (days < 0) return "Expired";
            if (days <= 1) return "ExpiringSoon";
            return "Good";
        } catch (Exception e) {
            Log.e("AddInventory", "Error checking status: " + e.getMessage());
            return "Good";
        }
    }

    private String getStatusClass(String status) {
        switch (status) {
            case "Expired":
                return "status-expired";
            case "ExpiringSoon":
                return "status-expiring";
            default:
                return "status-good";
        }
    }
}