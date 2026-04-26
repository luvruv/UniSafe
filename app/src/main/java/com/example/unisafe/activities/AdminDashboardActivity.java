package com.example.unisafe.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.unisafe.R;
import com.example.unisafe.adapters.ComplaintAdapter;
import com.example.unisafe.models.Complaint;
import com.example.unisafe.utils.AppConstants;
import com.example.unisafe.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView             tvAdminName, tvTotal, tvPending, tvProgress, tvCompleted;
    private TextView             tvElecCount, tvWifiCount, tvWaterCount, tvCleanCount;
    private RecyclerView         rvComplaints;
    private LinearLayout         layoutEmptyState;
    private ComplaintAdapter     adapter;
    private final List<Complaint> allComplaints     = new ArrayList<>();
    private final List<Complaint> displayComplaints = new ArrayList<>();
    private FirebaseFirestore    db;
    private SessionManager       sessionManager;
    private BottomNavigationView bottomNav;
    private View                 progressBar;

    // Track the active filter so we can re-apply after data reload
    private String activeFilter = "all";

    // Filter chip TextViews — kept as fields for easy highlight updates
    private TextView chipAll, chipPending, chipProgress, chipCompleted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        db             = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(this);

        initViews();
        setupBottomNav();
        loadAdminData();
    }

    // ─── View Initialisation ──────────────────────────────────────────────────

    private void initViews() {
        tvAdminName      = findViewById(R.id.tv_admin_name);
        tvTotal          = findViewById(R.id.tv_total_count);
        tvPending        = findViewById(R.id.tv_pending_count);
        tvProgress       = findViewById(R.id.tv_progress_count);
        tvCompleted      = findViewById(R.id.tv_completed_count);
        tvElecCount      = findViewById(R.id.tv_elec_count);
        tvWifiCount      = findViewById(R.id.tv_wifi_count);
        tvWaterCount     = findViewById(R.id.tv_water_count);
        tvCleanCount     = findViewById(R.id.tv_clean_count);
        rvComplaints     = findViewById(R.id.rv_complaints);
        layoutEmptyState = findViewById(R.id.layout_empty_state);
        progressBar      = findViewById(R.id.progress_bar);
        bottomNav        = findViewById(R.id.bottom_nav);

        tvAdminName.setText(sessionManager.getUserName());

        adapter = new ComplaintAdapter(this, displayComplaints, complaint -> {
            Intent intent = new Intent(this, ComplaintStatusActivity.class);
            intent.putExtra(AppConstants.EXTRA_COMPLAINT_ID, complaint.getId());
            startActivity(intent);
        });
        rvComplaints.setLayoutManager(new LinearLayoutManager(this));
        rvComplaints.setAdapter(adapter);

        setupFilterChips();
    }

    private void setupFilterChips() {
        chipAll       = findViewById(R.id.chip_all);
        chipPending   = findViewById(R.id.chip_pending);
        chipProgress  = findViewById(R.id.chip_progress);
        chipCompleted = findViewById(R.id.chip_completed);

        chipAll.setOnClickListener(v       -> applyFilter("all"));
        chipPending.setOnClickListener(v   -> applyFilter(AppConstants.STATUS_PENDING));
        chipProgress.setOnClickListener(v  -> applyFilter(AppConstants.STATUS_IN_PROGRESS));
        chipCompleted.setOnClickListener(v -> applyFilter(AppConstants.STATUS_COMPLETED));

        // Initial highlight
        updateChipHighlight("all");
    }

    private void setupBottomNav() {
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home)        return true;
            else if (id == R.id.nav_complaints) {
                startActivity(new Intent(this, ComplaintListActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    // ─── Data Loading ─────────────────────────────────────────────────────────

    private void loadAdminData() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection(AppConstants.COLLECTION_COMPLAINTS)
                .orderBy(AppConstants.FIELD_TIMESTAMP, Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(docs -> {
                    progressBar.setVisibility(View.GONE);
                    allComplaints.clear();

                    int total = 0, pending = 0, progress = 0, completed = 0;
                    int elec  = 0, wifi   = 0, water   = 0, clean     = 0;

                    for (var doc : docs) {
                        Complaint c = doc.toObject(Complaint.class);
                        if (c == null) continue;
                        c.setId(doc.getId());
                        allComplaints.add(c);
                        total++;

                        String status = c.getStatus() != null ? c.getStatus() : "";
                        if (AppConstants.STATUS_PENDING.equals(status))     pending++;
                        else if (AppConstants.STATUS_IN_PROGRESS.equals(status)) progress++;
                        else if (AppConstants.STATUS_COMPLETED.equals(status))   completed++;

                        String cat = c.getCategory() != null ? c.getCategory().toLowerCase() : "";
                        if (cat.contains("elec"))   elec++;
                        else if (cat.contains("wifi"))  wifi++;
                        else if (cat.contains("water")) water++;
                        else if (cat.contains("clean")) clean++;
                    }

                    tvTotal.setText(String.valueOf(total));
                    tvPending.setText(String.valueOf(pending));
                    tvProgress.setText(String.valueOf(progress));
                    tvCompleted.setText(String.valueOf(completed));
                    tvElecCount.setText(String.valueOf(elec));
                    tvWifiCount.setText(String.valueOf(wifi));
                    tvWaterCount.setText(String.valueOf(water));
                    tvCleanCount.setText(String.valueOf(clean));

                    // Re-apply current filter after data reload
                    applyFilter(activeFilter);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Snackbar.make(rvComplaints,
                            getString(R.string.error_load_complaints),
                            Snackbar.LENGTH_LONG).show();
                });
    }

    // ─── Filtering ────────────────────────────────────────────────────────────

    private void applyFilter(String filter) {
        activeFilter = filter;
        updateChipHighlight(filter);

        displayComplaints.clear();
        for (Complaint c : allComplaints) {
            if ("all".equals(filter) || filter.equals(c.getStatus())) {
                displayComplaints.add(c);
            }
        }
        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateChipHighlight(String filter) {
        // Reset all chips to unselected style
        int white       = ContextCompat.getColor(this, R.color.white);
        int blue        = ContextCompat.getColor(this, R.color.primary_blue);
        int textPrimary = ContextCompat.getColor(this, R.color.text_primary);
        int transparent = android.graphics.Color.TRANSPARENT;

        for (TextView chip : new TextView[]{chipAll, chipPending, chipProgress, chipCompleted}) {
            if (chip == null) continue;
            chip.setBackgroundColor(transparent);
            chip.setTextColor(textPrimary);
        }

        // Highlight the active chip
        TextView active = null;
        switch (filter) {
            case "all":                            active = chipAll;       break;
            case AppConstants.STATUS_PENDING:      active = chipPending;   break;
            case AppConstants.STATUS_IN_PROGRESS:  active = chipProgress;  break;
            case AppConstants.STATUS_COMPLETED:    active = chipCompleted; break;
        }
        if (active != null) {
            active.setBackgroundColor(blue);
            active.setTextColor(white);
        }
    }

    private void updateEmptyState() {
        boolean empty = displayComplaints.isEmpty();
        if (layoutEmptyState != null) {
            layoutEmptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        }
        rvComplaints.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    @Override
    protected void onResume() {
        super.onResume();
        loadAdminData();
        bottomNav.setSelectedItemId(R.id.nav_home);
    }

    @Override
    public void onBackPressed() {
        // ✅ Consistent with student dashboard — show exit confirmation, not instant logout
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.exit_app))
                .setMessage(getString(R.string.admin_exit_message))
                .setPositiveButton(getString(R.string.logout), (d, w) -> {
                    android.widget.Toast.makeText(this, getString(R.string.logging_out), android.widget.Toast.LENGTH_SHORT).show();
                    sessionManager.logout();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    /** Called by android:onClick="seeAllComplaints" on the dashboard XML. */
    public void seeAllComplaints(View v) {
        startActivity(new Intent(this, ComplaintListActivity.class));
    }
}