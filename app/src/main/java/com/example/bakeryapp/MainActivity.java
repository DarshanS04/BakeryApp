package com.example.bakeryapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bakeryapp.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FirebaseAuth mAuth;
    private static final String MANAGER_PASSWORD = "mgr123";
    private static final String DISTRIBUTOR_PASSWORD = "dist123";

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
            Log.d("MainActivity", "Manager button clicked, showing password prompt");
            showPasswordDialog("Manager", MANAGER_PASSWORD, ManagerDashboardActivity.class);
        });

        // Distributor Dashboard Button
        binding.distributorDashboard.setOnClickListener(v -> {
            Log.d("MainActivity", "Distributor button clicked, showing password prompt");
            showPasswordDialog("Distributor", DISTRIBUTOR_PASSWORD, DistributorDashboard.class);
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

    private void showPasswordDialog(String role, String correctPassword, Class<?> targetActivity) {
        // Create an EditText for password input
        final EditText passwordInput = new EditText(this);
        passwordInput.setHint("Enter " + role + " Password");

        // Create AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(role + " Password");
        builder.setView(passwordInput);
        builder.setPositiveButton("OK", (dialog, which) -> {
            String enteredPassword = passwordInput.getText().toString().trim();
            if (enteredPassword.equals(correctPassword)) {
                Log.d("MainActivity", "Password correct, navigating to " + targetActivity.getSimpleName());
                Intent intent = new Intent(MainActivity.this, targetActivity);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            } else {
                Log.w("MainActivity", "Incorrect password for " + role);
                new AlertDialog.Builder(this)
                        .setTitle("Error")
                        .setMessage("Incorrect password. Please try again.")
                        .setPositiveButton("OK", null)
                        .show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            Log.d("MainActivity", role + " password dialog cancelled");
            dialog.dismiss();
        });
        builder.setCancelable(false);
        builder.show();
    }
}