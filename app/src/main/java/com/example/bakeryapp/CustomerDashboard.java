package com.example.bakeryapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bakeryapp.databinding.ActivityCustomerDashboardBinding;
import com.google.firebase.auth.FirebaseAuth;

public class CustomerDashboard extends AppCompatActivity {

    private ActivityCustomerDashboardBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("CustomerDashboard", "onCreate called");

        // Initialize View Binding
        binding = ActivityCustomerDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Check authentication (no role check)
        if (mAuth.getCurrentUser() == null) {
            Log.e("CustomerDashboard", "User not authenticated");
            Toast.makeText(this, "Please sign in to access the dashboard", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        }

        // Set up button listeners
        binding.orderProductButton.setOnClickListener(v -> {
            Log.d("CustomerDashboard", "Navigating to OrderProductActivity");
            Intent intent = new Intent(this, OrderProductActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        binding.menuButton.setOnClickListener(v -> {
            Log.d("CustomerDashboard", "Navigating to MenuActivity");
            Intent intent = new Intent(this, MenuActivity.class);
            startActivity(intent);
        });

        binding.orderHistoryButton.setOnClickListener(v -> {
            Log.d("CustomerDashboard", "Navigating to OrderHistoryActivity");
            Intent intent = new Intent(this, OrderHistoryActivity.class);
            startActivity(intent);
        });

        binding.giveFeedbackButton.setOnClickListener(v -> {
            Log.d("CustomerDashboard", "Give Feedback button clicked");
            Toast.makeText(this, "Give Feedback feature not implemented yet", Toast.LENGTH_SHORT).show();
            // Intent intent = new Intent(this, GiveFeedbackActivity.class);
            // intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            // startActivity(intent);
        });

        binding.recommendationButton.setOnClickListener(v -> {
            Log.d("CustomerDashboard", "Recommendation button clicked");
            Toast.makeText(this, "Recommendation feature not implemented yet", Toast.LENGTH_SHORT).show();
            // Intent intent = new Intent(this, RecommendationActivity.class);
            // intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            // startActivity(intent);
        });

        binding.logoutButton.setOnClickListener(v -> {
            Log.d("CustomerDashboard", "Logout clicked");
            mAuth.signOut();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}