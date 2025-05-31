package com.example.bakeryapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bakeryapp.databinding.ActivityManagerDashboardBinding;
import com.google.firebase.auth.FirebaseAuth;

public class ManagerDashboardActivity extends AppCompatActivity {

    private ActivityManagerDashboardBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("ManagerDashboard", "onCreate called");

        // Initialize View Binding
        binding = ActivityManagerDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Check authentication
        if (mAuth.getCurrentUser() == null) {
            Log.e("ManagerDashboard", "User not authenticated");
            Toast.makeText(this, "Please sign in", Toast.LENGTH_SHORT).show();
            navigateToSignIn();
            return;
        }

        // Set up button click listeners
       // binding.overviewButton.setOnClickListener(v -> startActivity(new Intent(this, OverviewActivity.class)));
        binding.inventoryButton.setOnClickListener(v -> startActivity(new Intent(this, InventoryActivity.class)));
        binding.expensesButton.setOnClickListener(v -> startActivity(new Intent(this, ExpensesActivity.class)));
        binding.ordersButton.setOnClickListener(v -> startActivity(new Intent(this, ManagerOrdersActivity.class)));
       // binding.retailOrdersButton.setOnClickListener(v -> startActivity(new Intent(this, RetailOrdersActivity.class)));
       // binding.expiryButton.setOnClickListener(v -> startActivity(new Intent(this, ExpiryActivity.class)));
       // binding.feedbackButton.setOnClickListener(v -> startActivity(new Intent(this, FeedbackActivity.class)));
       // binding.billsButton.setOnClickListener(v -> startActivity(new Intent(this, BillsActivity.class)));

        // Logout button
        binding.logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(this, "Signed out", Toast.LENGTH_SHORT).show();
            navigateToSignIn();
        });
    }

    private void navigateToSignIn() {
        Intent intent = new Intent(this, SignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}