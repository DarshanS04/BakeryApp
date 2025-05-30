package com.example.bakeryapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bakeryapp.databinding.ActivitySignUpBinding;
import com.google.firebase.auth.FirebaseAuth;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize View Binding
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase Authentication
        firebaseAuth = FirebaseAuth.getInstance();

        // Navigate to SignInActivity
        binding.textView.setOnClickListener(v -> {
            Log.d("SignUpActivity", "Navigating to SignInActivity");
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

            if (!email.isEmpty() && !pass.isEmpty() && !confirmPass.isEmpty()) {
                if (pass.equals(confirmPass)) {
                    // Validate password: at least 8 characters, alphanumeric, and includes a special character
                    boolean hasAlphanumeric = false;
                    boolean hasSpecialChar = false;
                    for (char c : pass.toCharArray()) {
                        if (Character.isLetterOrDigit(c)) {
                            hasAlphanumeric = true;
                        } else {
                            hasSpecialChar = true;
                        }
                    }
                    if (pass.length() >= 8 && hasAlphanumeric && hasSpecialChar) {
                        firebaseAuth.createUserWithEmailAndPassword(email, pass)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Log.d("SignUpActivity", "Sign-up successful");
                                        Toast.makeText(SignUpActivity.this,
                                                "Account created successfully!",
                                                Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                                        startActivity(intent);
                                        finish(); // Close SignUpActivity after successful registration
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