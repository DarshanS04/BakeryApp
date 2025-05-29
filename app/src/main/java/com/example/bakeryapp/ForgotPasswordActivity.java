package com.example.bakeryapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bakeryapp.databinding.ActivityForgotPasswordBinding;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ActivityForgotPasswordBinding binding;
    private FirebaseAuth mAuth;
    private String strEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize View Binding
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // Reset Button Listener
        binding.btnReset.setOnClickListener(v -> {
            strEmail = binding.edtForgotPasswordEmail.getText().toString().trim();
            if (!strEmail.isEmpty()) {
                resetPassword();
            } else {
                binding.edtForgotPasswordEmail.setError("Email field can't be empty");
            }
        });

        // Back Button Listener
        binding.btnForgotPasswordBack.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });
    }

    private void resetPassword() {
        binding.forgetPasswordProgressbar.setVisibility(View.VISIBLE);
        binding.btnReset.setVisibility(View.INVISIBLE);

        mAuth.sendPasswordResetEmail(strEmail)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ForgotPasswordActivity.this,
                            "Reset Password link has been sent to your registered Email",
                            Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ForgotPasswordActivity.this, SignInActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ForgotPasswordActivity.this,
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    binding.forgetPasswordProgressbar.setVisibility(View.INVISIBLE);
                    binding.btnReset.setVisibility(View.VISIBLE);
                });
    }
}