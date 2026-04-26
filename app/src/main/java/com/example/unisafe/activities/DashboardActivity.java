package com.example.unisafe.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.unisafe.R;
import com.example.unisafe.adapters.ComplaintAdapter;
import com.example.unisafe.models.Complaint;
import com.example.unisafe.utils.AppConstants;
import com.example.unisafe.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private TextView              tvUserName, tvRoomInfo, tvPending, tvProgress, tvCompleted;
    private RecyclerView          rvRecentComplaints;
    private LinearLayout          layoutEmptyState;
    private ComplaintAdapter      adapter;
    private final List<Complaint> complaintList = new ArrayList<>();
    private FirebaseFirestore     db;
    private SessionManager        sessionManager;
    private FloatingActionButton  fabCreate;
    private BottomNavigationView  bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        db             = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(this);

        initViews();
        setupBottomNav();
        loadUserInfo();
        fabCreate.setOnClickListener(v ->
                startActivity(new Intent(this, CreateComplaintActivity.class)));
    }

    // ─── View Initialisation ──────────────────────────────────────────────────

    private void initViews() {
        tvUserName          = findViewById(R.id.tv_user_name);
        tvRoomInfo          = findViewById(R.id.tv_room_info);
        tvPending           = findViewById(R.id.tv_pending_count);
        tvProgress          = findViewById(R.id.tv_progress_count);
        tvCompleted         = findViewById(R.id.tv_completed_count);
        rvRecentComplaints  = findViewById(R.id.rv_recent_complaints);
        layoutEmptyState    = findViewById(R.id.layout_empty_state);
        fabCreate           = findViewById(R.id.fab_create);
        bottomNav           = findViewById(R.id.bottom_nav);

        adapter = new ComplaintAdapter(this, complaintList, complaint -> {
            Intent intent = new Intent(this, ComplaintStatusActivity.class);
            intent.putExtra(AppConstants.EXTRA_COMPLAINT_ID, complaint.getId());
            startActivity(intent);
        });
        rvRecentComplaints.setLayoutManager(new LinearLayoutManager(this));
        rvRecentComplaints.setAdapter(adapter);

        // Wire category card clicks (IDs added in XML)
        wireCardClick(R.id.card_cat_electricity, AppConstants.CATEGORY_ELECTRICITY);
        wireCardClick(R.id.card_cat_wifi,        AppConstants.CATEGORY_WIFI);
        wireCardClick(R.id.card_cat_water,       AppConstants.CATEGORY_WATER);
        wireCardClick(R.id.card_cat_cleaning,    AppConstants.CATEGORY_CLEANING);
    }

    private void wireCardClick(int cardId, String category) {
        View card = findViewById(cardId);
        if (card != null) {
            card.setOnClickListener(v -> {
                Intent intent = new Intent(this, CreateComplaintActivity.class);
                intent.putExtra(AppConstants.EXTRA_CATEGORY, category);
                startActivity(intent);
            });
        }
    }

    private void setupBottomNav() {
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_complaints) {
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

    private void loadUserInfo() {
        String name  = sessionManager.getUserName();
        String room  = sessionManager.getUserRoom();
        String block = sessionManager.getUserBlock();
        tvUserName.setText(name.isEmpty() ? getString(R.string.student) : name);
        tvRoomInfo.setText(getString(R.string.room_info_format, room, block));
    }

    private void loadStats() {
        String userId = sessionManager.getUserId();
        if (userId.isEmpty()) return;

        db.collection(AppConstants.COLLECTION_COMPLAINTS)
                .whereEqualTo(AppConstants.FIELD_USER_ID, userId)
                .get()
                .addOnSuccessListener(docs -> {
                    int pending = 0, progress = 0, completed = 0;
                    for (var doc : docs) {
                        String status = doc.getString(AppConstants.FIELD_STATUS);
                        if (AppConstants.STATUS_PENDING.equals(status))     pending++;
                        else if (AppConstants.STATUS_IN_PROGRESS.equals(status)) progress++;
                        else if (AppConstants.STATUS_COMPLETED.equals(status))   completed++;
                    }
                    tvPending.setText(String.valueOf(pending));
                    tvProgress.setText(String.valueOf(progress));
                    tvCompleted.setText(String.valueOf(completed));
                })
                .addOnFailureListener(e ->
                        Snackbar.make(fabCreate,
                                getString(R.string.error_load_stats),
                                Snackbar.LENGTH_SHORT).show()
                );
    }

    private void loadRecentComplaints() {
        String userId = sessionManager.getUserId();
        if (userId.isEmpty()) return;

        db.collection(AppConstants.COLLECTION_COMPLAINTS)
                .whereEqualTo(AppConstants.FIELD_USER_ID, userId)
                .orderBy(AppConstants.FIELD_TIMESTAMP) // using existing string field for compat
                .limitToLast(5)
                .get()
                .addOnSuccessListener(docs -> {
                    complaintList.clear();
                    for (var doc : docs) {
                        Complaint c = doc.toObject(Complaint.class);
                        if (c != null) {
                            c.setId(doc.getId());
                            complaintList.add(0, c); // reverse for descending order
                        }
                    }
                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                })
                .addOnFailureListener(e ->
                        Snackbar.make(fabCreate,
                                getString(R.string.error_load_complaints),
                                Snackbar.LENGTH_SHORT).show()
                );
    }

    private void updateEmptyState() {
        boolean empty = complaintList.isEmpty();
        if (layoutEmptyState != null) {
            layoutEmptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        }
        rvRecentComplaints.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    @Override
    protected void onResume() {
        super.onResume();
        loadStats();
        loadRecentComplaints();
        bottomNav.setSelectedItemId(R.id.nav_home);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.exit_app))
                .setMessage(getString(R.string.exit_app_message))
                .setPositiveButton(getString(R.string.exit), (d, w) -> finish())
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    // ─── XML onClick compatibility methods ────────────────────────────────────

    /** Called by android:onClick="viewAllCategories" on the dashboard XML. */
    public void viewAllCategories(View v) {
        startActivity(new Intent(this, CategoriesActivity.class));
    }

    /** Called by android:onClick="seeAllComplaints" on the dashboard XML. */
    public void seeAllComplaints(View v) {
        startActivity(new Intent(this, ComplaintListActivity.class));
    }
}
