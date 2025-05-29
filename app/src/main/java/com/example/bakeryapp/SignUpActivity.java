package com.example.bakeryapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bakeryapp.databinding.ActivitySignUpBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize View Binding
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase Authentication and Firestore
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Set up Spinner with roles
        String[] roles = {"Customer", "Manager", "Distributor"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.roleSpinner.setAdapter(adapter);

        // Navigate to SignInActivity
        binding.textView.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
            startActivity(intent);
            finish(); // Close SignUpActivity when going to SignInActivity
        });

        // Register button functionality
        binding.button.setOnClickListener(v -> {
            Log.d("SignUpActivity", "Sign-up button clicked");
            String email = binding.emailEt.getText().toString().trim();
            String pass = binding.passET.getText().toString().trim();
            String confirmPass = binding.confirmPassEt.getText().toString().trim();
            String selectedRole = binding.roleSpinner.getSelectedItem().toString();

            if (!email.isEmpty() && !pass.isEmpty() && !confirmPass.isEmpty()) {
                if (pass.equals(confirmPass)) {
                    // Validate password: at least 8 characters, alphanumeric, and includes a special character
                    if (pass.length() >= 8 && pass.matches(".*[a-zA-Z0-9].*") && pass.matches(".*[^a-zA-Z0-9].*")) {
                        firebaseAuth.createUserWithEmailAndPassword(email, pass)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Log.d("SignUpActivity", "Sign-up successful");
                                        // Store user role in Firestore
                                        String userId = firebaseAuth.getCurrentUser().getUid();
                                        Map<String, Object> userData = new HashMap<>();
                                        userData.put("role", selectedRole);
                                        firestore.collection("users").document(userId)
                                                .set(userData)
                                                .addOnSuccessListener(aVoid -> {
                                                    Log.d("SignUpActivity", "User role saved to Firestore");
                                                    Toast.makeText(SignUpActivity.this,
                                                            "Account created successfully!",
                                                            Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                                                    startActivity(intent);
                                                    finish(); // Close SignUpActivity after successful registration
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e("SignUpActivity", "Failed to save user role: " + e.getMessage());
                                                    Toast.makeText(SignUpActivity.this,
                                                            "Failed to save user role: " + e.getLocalizedMessage(),
                                                            Toast.LENGTH_SHORT).show();
                                                });
                                    } else {
                                        Log.e("SignUpActivity", "Sign-up failed: " + task.getException().getMessage());
                                        Toast.makeText(SignUpActivity.this,
                                                "Sign-up failed: " + task.getException().getLocalizedMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(SignUpActivity.this,
                                "Password must be at least 8 characters long, alphanumeric, and include a special character.",
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(SignUpActivity.this,
                            "Passwords do not match",
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(SignUpActivity.this,
                        "All fields are required!",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}