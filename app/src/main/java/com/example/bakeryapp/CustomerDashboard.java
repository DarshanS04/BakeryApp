package com.example.bakeryapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bakeryapp.databinding.ActivityCustomerDashboardBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class CustomerDashboard extends AppCompatActivity {

    private ActivityCustomerDashboardBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize View Binding
        binding = ActivityCustomerDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase Authentication and Firestore
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Verify user role
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            firestore.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists() && "Customer".equals(document.getString("role"))) {
                            // User is a Customer, proceed with dashboard setup
                            setupDashboard();
                        } else {
                            Toast.makeText(CustomerDashboard.this,
                                    "Access denied: Not a Customer",
                                    Toast.LENGTH_SHORT).show();
                            navigateToSignIn();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(CustomerDashboard.this,
                                "Error verifying role: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        navigateToSignIn();
                    });
        } else {
            Toast.makeText(CustomerDashboard.this,
                    "User not authenticated",
                    Toast.LENGTH_SHORT).show();
            navigateToSignIn();
        }
    }

    private void setupDashboard() {
        // Menu Button Listener
        binding.customerMenu.setOnClickListener(v -> {
            //intent
        });

        // Orders Button Listener
        binding.customerOrders.setOnClickListener(v -> {
           //intent
        });
    }

    private void navigateToSignIn() {
        Intent intent = new Intent(CustomerDashboard.this, SignInActivity.class);
        startActivity(intent);
        finish();
    }
}