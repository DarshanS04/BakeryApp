package com.example.bakeryapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bakeryapp.databinding.ActivitySignInBinding;
import com.google.firebase.auth.FirebaseAuth;

public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize View Binding
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase Authentication
        firebaseAuth = FirebaseAuth.getInstance();

        // Set up Spinner with roles
        String[] roles = {"Customer", "Manager", "Distributor"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.roleSpinner.setAdapter(adapter);

        // Navigate to SignUpActivity
        binding.textView.setOnClickListener(v -> {
            Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
            startActivity(intent);
            finish(); // Close SignInActivity when going to SignUpActivity
        });

        // Navigate to ForgotPasswordActivity
        binding.forgotPass.setOnClickListener(v -> {
            Intent intent = new Intent(SignInActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        // Sign-in button functionality
        binding.button.setOnClickListener(v -> {
            Log.d("SignInActivity", "Sign-in button clicked");
            String email = binding.emailEt.getText().toString().trim();
            String pass = binding.passET.getText().toString().trim();
            String selectedRole = binding.roleSpinner.getSelectedItem().toString();

            if (!email.isEmpty() && !pass.isEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(email, pass)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.d("SignInActivity", "Sign-in successful");
                                Intent intent;
                                // Navigate based on selected role
                                switch (selectedRole) {
                                    case "Customer":
                                        intent = new Intent(SignInActivity.this, CustomerDashboard.class);
                                        break;
                                    case "Manager":
                                        intent = new Intent(SignInActivity.this, ManagerDashboard.class);
                                        break;
                                    case "Distributor":
                                        intent = new Intent(SignInActivity.this, DistributorDashboard.class);
                                        break;
                                    default:
                                        intent = new Intent(SignInActivity.this, CustomerDashboard.class); // Fallback
                                        break;
                                }
                                startActivity(intent);
                                finish(); // Close SignInActivity on successful sign-in
                            } else {
                                Log.e("SignInActivity", "Sign-in failed: " + task.getException().getMessage());
                                Toast.makeText(SignInActivity.this,
                                        "Sign-in failed: " + task.getException().getLocalizedMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(SignInActivity.this,
                        "Empty fields are not allowed!",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Check if user is already signed in
        if (firebaseAuth.getCurrentUser() != null) {
            // Optionally, you could retrieve the user's role from Firebase or elsewhere
            // For simplicity, navigate to CustomerDashboard as a default
            Intent intent = new Intent(SignInActivity.this, CustomerDashboard.class);
            startActivity(intent);
            finish(); // Close SignInActivity if already signed in
        }
    }
}