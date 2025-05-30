package com.example.bakeryapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bakeryapp.databinding.ActivityDistributorDashboardBinding;
import com.google.firebase.auth.FirebaseAuth;

public class DistributorDashboard extends AppCompatActivity {

    private ActivityDistributorDashboardBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("DistributorDashboard", "onCreate called");

        // Initialize View Binding
        binding = ActivityDistributorDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // Check if user is authenticated
        if (mAuth.getCurrentUser() != null) {
            // User is authenticated, proceed with dashboard setup
            setupDashboard();
        } else {
            Log.e("DistributorDashboard", "User not authenticated");
            Toast.makeText(DistributorDashboard.this,
                    "User not authenticated",
                    Toast.LENGTH_SHORT).show();
            navigateToSignIn();
        }
    }

    private void setupDashboard() {
        // Menu Button Listener
        binding.distributorMenu.setOnClickListener(v -> {
            Log.d("DistributorDashboard", "Navigating to DistributorMenu");
            Intent intent = new Intent(DistributorDashboard.this, DistributorMenu.class);
            startActivity(intent);
        });

        // Retail Orders Button Listener
        binding.distributorRetailOrder.setOnClickListener(v -> {
            Log.d("DistributorDashboard", "Navigating to DistributorRetailOrders");
            Intent intent = new Intent(DistributorDashboard.this, DistributorRetailOrders.class);
            startActivity(intent);
        });

        // Logout Button Listener
        binding.distributorLogout.setOnClickListener(v -> {
            Log.d("DistributorDashboard", "Logout clicked");
            mAuth.signOut();
            Intent intent = new Intent(DistributorDashboard.this, SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        binding.distributorRetailOrder.setOnClickListener(v -> {
            Log.d("DistributorDashboard", "Navigating to DistributorRetailOrders");
            Intent intent = new Intent(DistributorDashboard.this, DistributorRetailOrdersActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

    }

    private void navigateToSignIn() {
        Log.d("DistributorDashboard", "Navigating to SignInActivity");
        Intent intent = new Intent(DistributorDashboard.this, SignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}