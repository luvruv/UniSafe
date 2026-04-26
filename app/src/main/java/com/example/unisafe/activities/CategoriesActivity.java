package com.example.unisafe.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.unisafe.R;
import com.example.unisafe.adapters.CategoryAdapter;
import com.example.unisafe.models.Complaint;
import java.util.ArrayList;
import java.util.List;

public class CategoriesActivity extends AppCompatActivity {
    private static final String TAG = "CategoriesActivity";
    private RecyclerView rvCategories;
    private CategoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);
        Log.d(TAG, "onCreate");

        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        rvCategories = findViewById(R.id.rv_categories);
        adapter = new CategoryAdapter(this, getCategories(), category -> {
            Intent intent = new Intent(this, CreateComplaintActivity.class);
            intent.putExtra("category", category);
            startActivity(intent);
        });
        rvCategories.setLayoutManager(new LinearLayoutManager(this));
        rvCategories.setAdapter(adapter);
    }

    private List<String[]> getCategories() {
        List<String[]> list = new ArrayList<>();
        list.add(new String[]{"Electricity", "⚡", "Power outage, socket issues, light problems"});
        list.add(new String[]{"WiFi & Internet", "📶", "No connection, slow speed, router issues"});
        list.add(new String[]{"Water Supply", "💧", "No water, low pressure, leakage problems"});
        list.add(new String[]{"Cleaning & Hygiene", "🧹", "Room cleaning, washroom, garbage issues"});
        list.add(new String[]{"Maintenance", "🔧", "Furniture, door, window repairs needed"});
        list.add(new String[]{"Security", "🔒", "Lock issues, security concerns"});
        list.add(new String[]{"Others", "📋", "Any other hostel issues"});
        return list;
    }

    @Override
    protected void onStart() { super.onStart(); Log.d(TAG, "onStart"); }
    @Override
    protected void onDestroy() { super.onDestroy(); Log.d(TAG, "onDestroy"); }
}