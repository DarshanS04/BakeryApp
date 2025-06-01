package com.example.bakeryapp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CustomerFeedbackActivity extends AppCompatActivity {

    private static final String TAG = "CustomerFeedbackActivity";
    private ListView feedbackList;
    private Spinner ratingFilter;
    private Button dateFilterButton;
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    private List<Map<String, Object>> feedbackData;
    private ArrayAdapter<Map<String, Object>> feedbackAdapter;
    private long startDateMillis;
    private long endDateMillis = Long.MAX_VALUE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_feedback);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Initialize UI components
        feedbackList = findViewById(R.id.feedback_list);
        ratingFilter = findViewById(R.id.rating_filter);
        dateFilterButton = findViewById(R.id.date_filter_button);

        // Initialize data list
        feedbackData = new ArrayList<>();

        // Set default start date to include May 2025 feedback
        Calendar defaultStart = Calendar.getInstance();
        defaultStart.set(2025, Calendar.MAY, 1, 0, 0, 0);
        startDateMillis = defaultStart.getTimeInMillis();

        // Set up rating filter spinner
        List<String> ratingOptions = new ArrayList<>();
        ratingOptions.add("All Ratings");
        for (int i = 1; i <= 5; i++) {
            ratingOptions.add(i + " Star" + (i > 1 ? "s" : ""));
        }
        ArrayAdapter<String> ratingAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ratingOptions);
        ratingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ratingFilter.setAdapter(ratingAdapter);
        ratingFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadFeedback();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Set up date filter button
        dateFilterButton.setOnClickListener(v -> showDateRangePicker());

        // Set up feedback list
        setupFeedbackList();
    }

    private void setupFeedbackList() {
        feedbackAdapter = new ArrayAdapter<Map<String, Object>>(this, R.layout.item_feedback, feedbackData) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_feedback, parent, false);
                }

                Map<String, Object> feedback = feedbackData.get(position);

                TextView customerName = convertView.findViewById(R.id.customer_name);
                TextView customerEmail = convertView.findViewById(R.id.customer_email);
                TextView feedbackMessage = convertView.findViewById(R.id.feedback_message);
                TextView feedbackRating = convertView.findViewById(R.id.feedback_rating);
                TextView feedbackDate = convertView.findViewById(R.id.feedback_date);

                // Hide customerName since it's not in the database
                customerName.setVisibility(View.GONE);
                customerEmail.setText("Email: " + (feedback.get("customerEmail") != null ? feedback.get("customerEmail") : "N/A"));
                feedbackMessage.setText("Comment: " + (feedback.get("comment") != null ? feedback.get("comment") : "No comment"));
                feedbackRating.setText("Rating: " + (feedback.get("rating") != null ? feedback.get("rating") + " Star(s)" : "N/A"));

                String timestamp = (String) feedback.get("timestamp");
                String feedbackDateStr = timestamp != null && timestamp.length() >= 10 ? timestamp.substring(0, 10) : "Unknown";
                feedbackDate.setText("Date: " + feedbackDateStr);

                return convertView;
            }
        };
        feedbackList.setAdapter(feedbackAdapter);
        loadFeedback();
    }

    private void loadFeedback() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            feedbackData.clear();
            feedbackAdapter.notifyDataSetChanged();
            Toast.makeText(this, "Please log in to view feedback", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseReference.child("feedback").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                feedbackData.clear();
                Log.d(TAG, "Feedback node snapshot: " + snapshot.toString());
                if (!snapshot.exists()) {
                    Log.d(TAG, "Feedback node is empty or does not exist");
                    Toast.makeText(CustomerFeedbackActivity.this, "No feedback found", Toast.LENGTH_SHORT).show();
                    feedbackAdapter.notifyDataSetChanged();
                    return;
                }

                int selectedRating = ratingFilter.getSelectedItemPosition(); // 0 = All, 1-5 = Rating
                for (DataSnapshot data : snapshot.getChildren()) {
                    Map<String, Object> feedback = (Map<String, Object>) data.getValue();
                    if (feedback != null) {
                        Log.d(TAG, "Feedback entry: " + feedback.toString());
                        Long rating = feedback.get("rating") != null ? ((Number) feedback.get("rating")).longValue() : 0;
                        String timestamp = (String) feedback.get("timestamp");
                        long feedbackTime = parseTimestampToMillis(timestamp);

                        // Apply filters
                        boolean ratingMatch = selectedRating == 0 || rating == (selectedRating);
                        boolean dateMatch = feedbackTime >= startDateMillis && feedbackTime <= endDateMillis;

                        if (ratingMatch && dateMatch) {
                            feedback.put("id", data.getKey());
                            feedbackData.add(feedback);
                        }
                    } else {
                        Log.w(TAG, "Null feedback entry for key: " + data.getKey());
                    }
                }
                feedbackAdapter.notifyDataSetChanged();
                if (feedbackData.isEmpty()) {
                    Log.d(TAG, "No feedback entries matched filters");
                    Toast.makeText(CustomerFeedbackActivity.this, "No feedback found", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "Loaded " + feedbackData.size() + " feedback entries");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading feedback: " + error.getMessage());
                Toast.makeText(CustomerFeedbackActivity.this, "Error loading feedback: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private long parseTimestampToMillis(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) {
            Log.w(TAG, "Invalid timestamp: null or empty");
            return 0;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            Date date = sdf.parse(timestamp);
            if (date != null) {
                return date.getTime();
            } else {
                Log.w(TAG, "Failed to parse timestamp: " + timestamp);
                return 0;
            }
        } catch (Exception e) {
            Log.w(TAG, "Error parsing timestamp: " + timestamp, e);
            return 0;
        }
    }

    private void showDateRangePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog startDatePicker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar startCal = Calendar.getInstance();
            startCal.set(year, month, dayOfMonth, 0, 0, 0);
            startDateMillis = startCal.getTimeInMillis();

            // Show end date picker
            DatePickerDialog endDatePicker = new DatePickerDialog(this, (view1, year1, month1, dayOfMonth1) -> {
                Calendar endCal = Calendar.getInstance();
                endCal.set(year1, month1, dayOfMonth1, 23, 59, 59);
                endDateMillis = endCal.getTimeInMillis();

                // Reload feedback with new date range
                loadFeedback();
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            endDatePicker.show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        startDatePicker.show();
    }
}