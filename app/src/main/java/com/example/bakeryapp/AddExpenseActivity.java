package com.example.bakeryapp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bakeryapp.databinding.ActivityAddExpenseBinding;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddExpenseActivity extends AppCompatActivity {
    private ActivityAddExpenseBinding binding;
    private DatabaseReference expensesRef;
    private ExpenseItem editItem;
    private Calendar selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddExpenseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        expensesRef = FirebaseDatabase.getInstance().getReference("expenses");
        selectedDate = Calendar.getInstance();

        // Populate type dropdown
        String[] types = {"ingredients", "utilities", "labor", "equipment", "rent", "marketing", "other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.typeSpinner.setAdapter(adapter);

        // Set up date picker
        binding.dateEditText.setOnClickListener(v -> showDatePickerDialog());

        // Check for edit
        editItem = (ExpenseItem) getIntent().getSerializableExtra("item");
        if (editItem != null) {
            binding.typeSpinner.setSelection(adapter.getPosition(editItem.getType()));
            binding.amountEditText.setText(String.valueOf(editItem.getAmount()));
            binding.dateEditText.setText(editItem.getDate());
            binding.submitButton.setText("Update Expense");
            // Set selected date for edit
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                Date date = sdf.parse(editItem.getDate());
                if (date != null) {
                    selectedDate.setTime(date);
                }
            } catch (Exception e) {
                Log.e("AddExpense", "Error parsing edit date: " + e.getMessage());
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
                    binding.dateEditText.setText(sdf.format(selectedDate.getTime()));
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
        String type = binding.typeSpinner.getSelectedItem() != null ? binding.typeSpinner.getSelectedItem().toString() : "";
        String amountStr = binding.amountEditText.getText().toString().trim();
        String date = binding.dateEditText.getText().toString().trim();

        if (type.isEmpty() || amountStr.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                Toast.makeText(this, "Amount must be positive", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate date format
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        sdf.setLenient(false);
        try {
            sdf.parse(date);
        } catch (Exception e) {
            Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show();
            return;
        }

        String timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(new Date());

        ExpenseItem item = new ExpenseItem(amount, date, timestamp, type);
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.submitButton.setEnabled(false);

        DatabaseReference ref = editItem != null ? expensesRef.child(editItem.getKey()) : expensesRef.push();
        ref.setValue(item, (error, ref1) -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.submitButton.setEnabled(true);
            if (error == null) {
                Toast.makeText(AddExpenseActivity.this, editItem != null ? "Expense updated" : "Expense added", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Log.e("AddExpense", "Error saving expense: " + error.getMessage());
                Toast.makeText(AddExpenseActivity.this, "Failed to save: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
