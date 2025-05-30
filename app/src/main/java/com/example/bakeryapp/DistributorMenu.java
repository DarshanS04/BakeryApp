package com.example.bakeryapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.bakeryapp.databinding.ActivityDistributorMenuBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class DistributorMenu extends AppCompatActivity {

    private ActivityDistributorMenuBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference rawMaterialsRef;
    private RawMaterialAdapter adapter;
    private List<RawMaterial> rawMaterials = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("DistributorMenu", "onCreate called");

        // Initialize View Binding
        binding = ActivityDistributorMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // Initialize Firebase Realtime Database
        rawMaterialsRef = FirebaseDatabase.getInstance().getReference("raw");

        // Set up RecyclerView
        adapter = new RawMaterialAdapter();
        binding.rawMaterialsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.rawMaterialsRecyclerView.setAdapter(adapter);

        // Check authentication and fetch data
        if (mAuth.getCurrentUser() != null) {
            fetchRawMaterials();
        } else {
            Log.e("DistributorMenu", "User not authenticated");
            Toast.makeText(this, "Please sign in to view raw materials", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void fetchRawMaterials() {
        rawMaterialsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                rawMaterials.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    RawMaterial rawMaterial = snapshot.getValue(RawMaterial.class);
                    if (rawMaterial != null) {
                        rawMaterials.add(rawMaterial);
                    }
                }
                adapter.setRawMaterials(rawMaterials);
                if (rawMaterials.isEmpty()) {
                    Log.d("DistributorMenu", "No raw materials found");
                    Toast.makeText(DistributorMenu.this, "No raw materials available", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("DistributorMenu", "Fetched " + rawMaterials.size() + " raw materials");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("DistributorMenu", "Database error: " + databaseError.getMessage());
                Toast.makeText(DistributorMenu.this, "Failed to load raw materials: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}