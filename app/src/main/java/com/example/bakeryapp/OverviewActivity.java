package com.example.bakeryapp;


import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
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

public class OverviewActivity extends AppCompatActivity {
    private TextView todaySalesText, weekSalesText, monthSalesText;
    private BarChart salesChart;
    private RecyclerView lowStockRecyclerView;
    private LowStockAdapter lowStockAdapter;
    private List<LowStockItem> lowStockItems;
    private DatabaseReference ordersRef, inventoryRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        // Initialize UI components
        todaySalesText = findViewById(R.id.overviewTodaySales);
        weekSalesText = findViewById(R.id.overviewWeekSales);
        monthSalesText = findViewById(R.id.overviewMonthSales);
        salesChart = findViewById(R.id.overviewSalesChart);
        lowStockRecyclerView = findViewById(R.id.lowStockRecyclerView);

        // Initialize RecyclerView
        lowStockItems = new ArrayList<>();
        lowStockAdapter = new LowStockAdapter(lowStockItems);
        lowStockRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        lowStockRecyclerView.setAdapter(lowStockAdapter);

        // Initialize Firebase references
        ordersRef = FirebaseDatabase.getInstance().getReference("orders");
        inventoryRef = FirebaseDatabase.getInstance().getReference("inventory");

        // Load data
        renderOverviewChartAndStats();
        loadLowStockNotifications();
    }

    private void renderOverviewChartAndStats() {
        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    List<BarEntry> entries = new ArrayList<>();
                    HashMap<String, Float> salesByDay = new HashMap<>();
                    List<String> labels = new ArrayList<>();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

                    // Initialize last 7 days
                    for (int i = 6; i >= 0; i--) {
                        Calendar cal = Calendar.getInstance();
                        cal.add(Calendar.DAY_OF_MONTH, -i);
                        String dateStr = sdf.format(cal.getTime());
                        labels.add(new SimpleDateFormat("EEE", Locale.US).format(cal.getTime()));
                        salesByDay.put(dateStr, 0f);
                    }

                    // Process completed orders
                    float todaySales = 0f, weekSales = 0f, monthSales = 0f;
                    Calendar oneWeekAgo = Calendar.getInstance();
                    oneWeekAgo.add(Calendar.DAY_OF_MONTH, -7);
                    Calendar oneMonthAgo = Calendar.getInstance();
                    oneMonthAgo.add(Calendar.MONTH, -1);
                    String todayStr = sdf.format(new Date());

                    for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                        String status = orderSnapshot.child("status").getValue(String.class);
                        if ("completed".equals(status)) {
                            String timestamp = orderSnapshot.child("timestamp").getValue(String.class);
                            Float totalAmount = orderSnapshot.child("totalAmount").getValue(Float.class);
                            if (timestamp != null && totalAmount != null) {
                                String orderDate = timestamp.split("T")[0];
                                try {
                                    Date orderDateParsed = sdf.parse(orderDate);
                                    if (orderDateParsed != null) {
                                        // Aggregate sales by day for chart
                                        if (salesByDay.containsKey(orderDate)) {
                                            salesByDay.put(orderDate, salesByDay.get(orderDate) + totalAmount);
                                        }
                                        // Calculate sales stats
                                        if (orderDate.equals(todayStr)) {
                                            todaySales += totalAmount;
                                        }
                                        if (orderDateParsed.after(oneWeekAgo.getTime()) || orderDateParsed.equals(oneWeekAgo.getTime())) {
                                            weekSales += totalAmount;
                                        }
                                        if (orderDateParsed.after(oneMonthAgo.getTime()) || orderDateParsed.equals(oneMonthAgo.getTime())) {
                                            monthSales += totalAmount;
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.e("OverviewActivity", "Error parsing date: " + e.getMessage());
                                }
                            }
                        }
                    }

                    // Update sales stats
                    todaySalesText.setText(String.format("₹%.2f", todaySales));
                    weekSalesText.setText(String.format("₹%.2f", weekSales));
                    monthSalesText.setText(String.format("₹%.2f", monthSales));

                    // Prepare chart data
                    for (int i = 0; i < labels.size(); i++) {
                        Calendar cal = Calendar.getInstance();
                        cal.add(Calendar.DAY_OF_MONTH, -(6 - i));
                        String dateStr = sdf.format(cal.getTime());
                        entries.add(new BarEntry(i, salesByDay.getOrDefault(dateStr, 0f)));
                    }

                    // Setup bar chart
                    BarDataSet dataSet = new BarDataSet(entries, "Sales (₹)");
                    dataSet.setColor(0xFF3498DB); // Same color as web (#3498db)
                    dataSet.setValueTextSize(10f);
                    BarData barData = new BarData(dataSet);
                    barData.setBarWidth(0.4f);

                    salesChart.setData(barData);
                    salesChart.getDescription().setEnabled(false);
                    salesChart.setFitBars(true);
                    salesChart.getLegend().setEnabled(false);

                    // Customize X-axis
                    XAxis xAxis = salesChart.getXAxis();
                    xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                    xAxis.setDrawGridLines(false);

                    // Customize Y-axis
                    salesChart.getAxisLeft().setAxisMinimum(0f);
                    salesChart.getAxisRight().setEnabled(false);

                    salesChart.invalidate(); // Refresh chart
                } else {
                    todaySalesText.setText("₹0.00");
                    weekSalesText.setText("₹0.00");
                    monthSalesText.setText("₹0.00");
                    salesChart.setData(null);
                    salesChart.invalidate();
                    Toast.makeText(OverviewActivity.this, "No orders data found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("OverviewActivity", "Error loading orders: " + error.getMessage());
                Toast.makeText(OverviewActivity.this, "Error loading orders", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadLowStockNotifications() {
        inventoryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                lowStockItems.clear();
                if (snapshot.exists()) {
                    HashMap<String, Integer> productQuantities = new HashMap<>();
                    for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                        String productName = itemSnapshot.child("name").getValue(String.class);
                        // Read quantity as Object to handle both String and Long
                        Object quantityObj = itemSnapshot.child("quantity").getValue();
                        Integer quantity = 0; // Default value
                        if (quantityObj != null) {
                            try {
                                if (quantityObj instanceof String) {
                                    quantity = Integer.parseInt((String) quantityObj);
                                } else if (quantityObj instanceof Long) {
                                    quantity = ((Long) quantityObj).intValue();
                                } else {
                                    Log.e("OverviewActivity", "Unexpected quantity type for " + productName + ": " + quantityObj.getClass().getName());
                                }
                            } catch (NumberFormatException e) {
                                Log.e("OverviewActivity", "Invalid quantity format for " + productName + ": " + quantityObj, e);
                            }
                        } else {
                            Log.w("OverviewActivity", "Quantity is null for " + productName);
                        }
                        if (productName != null) {
                            productQuantities.put(productName, productQuantities.getOrDefault(productName, 0) + quantity);
                        }
                    }
                    // Consider items with total quantity <= 10 as low stock
                    for (String productName : productQuantities.keySet()) {
                        int totalQuantity = productQuantities.get(productName);
                        if (totalQuantity <= 10) {
                            lowStockItems.add(new LowStockItem(productName, totalQuantity));
                        }
                    }
                    if (lowStockItems.isEmpty()) {
                        lowStockItems.add(new LowStockItem("No low stock items at the moment.", 0));
                    }
                } else {
                    lowStockItems.add(new LowStockItem("No inventory data available.", 0));
                }
                lowStockAdapter.updateData(lowStockItems);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("OverviewActivity", "Error loading inventory: " + error.getMessage());
                Toast.makeText(OverviewActivity.this, "Error loading inventory", Toast.LENGTH_SHORT).show();
                lowStockItems.clear();
                lowStockItems.add(new LowStockItem("Error loading inventory.", 0));
                lowStockAdapter.updateData(lowStockItems);
            }
        });
    }
}