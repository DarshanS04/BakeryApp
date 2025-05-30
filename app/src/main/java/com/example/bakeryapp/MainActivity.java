package com.example.bakeryapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bakeryapp.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("MainActivity", "onCreate called");

        // Initialize View Binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // Set up button listeners
        setupButtonListeners();
    }

    private void setupButtonListeners() {
        // Manager Dashboard Button
        binding.managerDashboard.setOnClickListener(v -> {
            Log.d("MainActivity", "Navigating to ManagerDashboard");
            Intent intent = new Intent(MainActivity.this, ManagerDashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        // Distributor Dashboard Button
        binding.distributorDashboard.setOnClickListener(v -> {
            Log.d("MainActivity", "Navigating to DistributorDashboard");
            Intent intent = new Intent(MainActivity.this, DistributorDashboard.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        // Customer Dashboard Button
        binding.customerDashboard.setOnClickListener(v -> {
            Log.d("MainActivity", "Navigating to CustomerDashboard");
            Intent intent = new Intent(MainActivity.this, CustomerDashboard.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        // Logout Button
        binding.logoutButton.setOnClickListener(v -> {
            Log.d("MainActivity", "Logout clicked");
            mAuth.signOut();
            Intent intent = new Intent(MainActivity.this, SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }
}