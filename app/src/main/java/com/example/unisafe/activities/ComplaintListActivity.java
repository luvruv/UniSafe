package com.example.unisafe.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.unisafe.R;
import com.example.unisafe.adapters.ComplaintAdapter;
import com.example.unisafe.models.Complaint;
import com.example.unisafe.utils.AppConstants;
import com.example.unisafe.utils.SessionManager;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ComplaintListActivity extends AppCompatActivity {

    private RecyclerView          rvComplaints;
    private ComplaintAdapter      adapter;
    private final List<Complaint> allComplaints      = new ArrayList<>();
    private final List<Complaint> filteredComplaints = new ArrayList<>();
    private TextView              tvAll, tvPending, tvProgress, tvCompleted;
    private View                  progressBar;
    private TextView              tvEmpty;
    private FirebaseFirestore     db;
    private SessionManager        sessionManager;
    private String                currentFilter = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint_list);

        db             = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(this);

        initViews();
        loadComplaints();
    }

    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        tvAll       = findViewById(R.id.tv_filter_all);
        tvPending   = findViewById(R.id.tv_filter_pending);
        tvProgress  = findViewById(R.id.tv_filter_progress);
        tvCompleted = findViewById(R.id.tv_filter_completed);
        progressBar = findViewById(R.id.progress_bar);
        tvEmpty     = findViewById(R.id.tv_empty);
        rvComplaints = findViewById(R.id.rv_complaints);

        adapter = new ComplaintAdapter(this, filteredComplaints, complaint -> {
            Intent intent = new Intent(this, ComplaintStatusActivity.class);
            intent.putExtra(AppConstants.EXTRA_COMPLAINT_ID, complaint.getId());
            startActivity(intent);
        });
        rvComplaints.setLayoutManager(new LinearLayoutManager(this));
        rvComplaints.setAdapter(adapter);

        tvAll.setOnClickListener(v       -> applyFilter("all"));
        tvPending.setOnClickListener(v   -> applyFilter(AppConstants.STATUS_PENDING));
        tvProgress.setOnClickListener(v  -> applyFilter(AppConstants.STATUS_IN_PROGRESS));
        tvCompleted.setOnClickListener(v -> applyFilter(AppConstants.STATUS_COMPLETED));
    }

    private void applyFilter(String filter) {
        currentFilter = filter;
        updateFilterUI(filter);
        filteredComplaints.clear();
        for (Complaint c : allComplaints) {
            if ("all".equals(filter) || filter.equals(c.getStatus())) {
                filteredComplaints.add(c);
            }
        }
        adapter.notifyDataSetChanged();
        if (tvEmpty != null) {
            tvEmpty.setVisibility(filteredComplaints.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    /** ✅ ContextCompat replaces deprecated getColor() */
    private void updateFilterUI(String filter) {
        int blue        = ContextCompat.getColor(this, R.color.primary_blue);
        int white       = ContextCompat.getColor(this, R.color.white);
        int transparent = android.graphics.Color.TRANSPARENT;
        int dark        = ContextCompat.getColor(this, R.color.text_primary);

        for (TextView chip : new TextView[]{tvAll, tvPending, tvProgress, tvCompleted}) {
            if (chip == null) continue;
            chip.setBackgroundColor(transparent);
            chip.setTextColor(dark);
        }

        TextView active = null;
        switch (filter) {
            case "all":                           active = tvAll;       break;
            case AppConstants.STATUS_PENDING:     active = tvPending;   break;
            case AppConstants.STATUS_IN_PROGRESS: active = tvProgress;  break;
            case AppConstants.STATUS_COMPLETED:   active = tvCompleted; break;
        }
        if (active != null) {
            active.setBackgroundColor(blue);
            active.setTextColor(white);
        }
    }

    private void loadComplaints() {
        progressBar.setVisibility(View.VISIBLE);
        String userId = sessionManager.getUserId();
        String role   = sessionManager.getUserRole();
        if (userId.isEmpty()) {
            progressBar.setVisibility(View.GONE);
            return;
        }

        com.google.firebase.firestore.Query query = db.collection(AppConstants.COLLECTION_COMPLAINTS);
        if (!AppConstants.ROLE_ADMIN.equals(role)) {
            query = query.whereEqualTo(AppConstants.FIELD_USER_ID, userId);
        }

        query.get()
                .addOnSuccessListener(docs -> {
                    progressBar.setVisibility(View.GONE);
                    allComplaints.clear();
                    for (var doc : docs) {
                        Complaint c = doc.toObject(Complaint.class);
                        if (c != null) {
                            c.setId(doc.getId());
                            allComplaints.add(c);
                        }
                    }
                    // Sort descending by timestamp (newest first)
                    allComplaints.sort((c1, c2) -> {
                        String t1 = c1.getTimestamp() != null ? c1.getTimestamp() : "";
                        String t2 = c2.getTimestamp() != null ? c2.getTimestamp() : "";
                        return t2.compareTo(t1);
                    });
                    applyFilter(currentFilter);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Snackbar.make(rvComplaints,
                            getString(R.string.error_load_complaints),
                            Snackbar.LENGTH_LONG).show();
                });
    }
}