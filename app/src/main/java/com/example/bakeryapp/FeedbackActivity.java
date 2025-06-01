package com.example.bakeryapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bakeryapp.databinding.ActivityFeedbackBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FeedbackActivity extends AppCompatActivity {

    private ActivityFeedbackBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference feedbackRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("FeedbackActivity", "onCreate called");

        // Initialize View Binding
        binding = ActivityFeedbackBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        feedbackRef = FirebaseDatabase.getInstance().getReference("feedback");

        // Check authentication
        if (mAuth.getCurrentUser() == null) {
            Log.e("FeedbackActivity", "User not authenticated");
            Toast.makeText(this, "Please sign in to submit feedback", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Submit button listener
        binding.submitFeedbackButton.setOnClickListener(v -> submitFeedback());
    }

    private void submitFeedback() {
        // Validate rating
        float ratingFloat = binding.ratingBar.getRating();
        if (ratingFloat == 0) {
            Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show();
            return;
        }
        int rating = Math.round(ratingFloat); // Convert float to int

        // Validate comment
        String comment = binding.commentsEditText.getText().toString().trim();
        if (comment.isEmpty()) {
            Toast.makeText(this, "Please enter your comment", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prepare feedback
        String userId = mAuth.getCurrentUser().getUid();
        String userEmail = mAuth.getCurrentUser().getEmail();
        String timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(new Date());
        Feedback feedback = new Feedback(userId, userEmail, rating, comment, timestamp);

        // Show progress bar
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.submitFeedbackButton.setEnabled(false);

        // Save to Firebase
        feedbackRef.push().setValue(feedback, (error, ref) -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.submitFeedbackButton.setEnabled(true);

            if (error == null) {
                Toast.makeText(FeedbackActivity.this, "Feedback submitted successfully", Toast.LENGTH_LONG).show();
                // Clear form
                binding.ratingBar.setRating(0);
                binding.commentsEditText.setText("");
                finish(); // Return to dashboard
            } else {
                Log.e("FeedbackActivity", "Error submitting feedback: " + error.getMessage());
                Toast.makeText(FeedbackActivity.this, "Failed to submit feedback", Toast.LENGTH_SHORT).show();
            }
        });
    }
}