package com.example.unisafe.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.unisafe.R;
import com.example.unisafe.utils.AppConstants;
import com.example.unisafe.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ComplaintStatusActivity extends AppCompatActivity {

    private TextView          tvId, tvStatus, tvCategory, tvLocation,
                              tvSubmittedOn, tvSubject, tvDescription,
                              tvPriority, tvAssignedTo;
    private LinearLayout      llTimeline;
    private LinearLayout      layoutAdminPanel;
    private MaterialButton    btnMarkProgress, btnMarkCompleted, btnReject;
    private TextInputEditText etAdminNote;
    private View              progressBar;

    private FirebaseFirestore db;
    private SessionManager    sessionManager;
    private String            currentComplaintId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint_status);

        db             = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(this);

        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        initViews();

        currentComplaintId = getIntent().getStringExtra(AppConstants.EXTRA_COMPLAINT_ID);
        if (currentComplaintId != null && !currentComplaintId.isEmpty()) {
            loadComplaint(currentComplaintId);
        } else {
            showSnackbar(getString(R.string.error_required));
            finish();
        }
    }

    // ─── View Initialisation ──────────────────────────────────────────────────

    private void initViews() {
        tvId            = findViewById(R.id.tv_complaint_id);
        tvStatus        = findViewById(R.id.tv_status);
        tvCategory      = findViewById(R.id.tv_category);
        tvLocation      = findViewById(R.id.tv_location);
        tvSubmittedOn   = findViewById(R.id.tv_submitted_on);
        tvSubject       = findViewById(R.id.tv_subject);
        tvDescription   = findViewById(R.id.tv_description);
        tvPriority      = findViewById(R.id.tv_priority);
        tvAssignedTo    = findViewById(R.id.tv_assigned_to);
        llTimeline      = findViewById(R.id.ll_timeline);
        progressBar     = findViewById(R.id.progress_bar);
        layoutAdminPanel = findViewById(R.id.layout_admin_panel);
        btnMarkProgress  = findViewById(R.id.btn_mark_progress);
        btnMarkCompleted = findViewById(R.id.btn_mark_completed);
        btnReject        = findViewById(R.id.btn_reject_complaint);
        etAdminNote      = findViewById(R.id.et_admin_note);

        // Show admin panel only for admins
        if (sessionManager.isAdmin() && layoutAdminPanel != null) {
            layoutAdminPanel.setVisibility(View.VISIBLE);
            wireAdminButtons();
        }
    }

    // ─── Admin Status Update ──────────────────────────────────────────────────

    private void wireAdminButtons() {
        btnMarkProgress.setOnClickListener(v ->
                updateStatus(AppConstants.STATUS_IN_PROGRESS));
        btnMarkCompleted.setOnClickListener(v ->
                updateStatus(AppConstants.STATUS_COMPLETED));
        btnReject.setOnClickListener(v ->
                updateStatus(AppConstants.STATUS_REJECTED));
    }

    private void updateStatus(String newStatus) {
        if (currentComplaintId == null) return;

        String adminNote = (etAdminNote != null && etAdminNote.getText() != null)
                ? etAdminNote.getText().toString().trim() : "";

        Map<String, Object> updates = new HashMap<>();
        updates.put(AppConstants.FIELD_STATUS, newStatus);
        updates.put("adminNote",  adminNote);
        updates.put("assignedTo", sessionManager.getUserName());

        setLoadingState(true);
        db.collection(AppConstants.COLLECTION_COMPLAINTS)
                .document(currentComplaintId)
                .update(updates)
                .addOnSuccessListener(unused -> {
                    setLoadingState(false);
                    showSnackbar(getString(R.string.status_updated));
                    // Reload to reflect changes in timeline
                    loadComplaint(currentComplaintId);
                })
                .addOnFailureListener(e -> {
                    setLoadingState(false);
                    showSnackbar(getString(R.string.error_update_status));
                });
    }

    // ─── Data Loading ─────────────────────────────────────────────────────────

    private void loadComplaint(String complaintId) {
        setLoadingState(true);
        db.collection(AppConstants.COLLECTION_COMPLAINTS)
                .document(complaintId)
                .get()
                .addOnSuccessListener(doc -> {
                    setLoadingState(false);
                    if (doc.exists()) {
                        // ✅ Null-safe reads with "—" fallback
                        String id          = safe(doc.getString("id"), complaintId.substring(0, Math.min(6, complaintId.length())));
                        String status      = safe(doc.getString("status"), AppConstants.STATUS_PENDING);
                        String category    = safe(doc.getString("category"), "—");
                        String block       = safe(doc.getString("block"), "—");
                        String room        = safe(doc.getString("roomNumber"), "—");
                        String timestamp   = safe(doc.getString("timestamp"), "—");
                        String subject     = safe(doc.getString("subject"), "—");
                        String description = safe(doc.getString("description"), "—");
                        String priority    = safe(doc.getString("priority"), AppConstants.PRIORITY_MEDIUM);
                        String assignedTo  = safe(doc.getString("assignedTo"), "");
                        String adminNote   = safe(doc.getString("adminNote"), "");

                        tvId.setText("#" + id.substring(0, Math.min(6, id.length())).toUpperCase());
                        tvSubject.setText(subject);
                        tvDescription.setText(description);
                        tvCategory.setText(category);
                        tvLocation.setText("Room " + room + ", " + block);
                        tvSubmittedOn.setText(timestamp);
                        tvPriority.setText(priority.toUpperCase());

                        tvAssignedTo.setText(
                                assignedTo.isEmpty() ? getString(R.string.not_assigned) : assignedTo);

                        // Pre-fill admin note if it exists
                        if (etAdminNote != null && !adminNote.isEmpty()) {
                            etAdminNote.setText(adminNote);
                        }

                        applyStatusBadge(status);
                        buildTimeline(status, timestamp);
                    } else {
                        showSnackbar(getString(R.string.error_load_complaints));
                    }
                })
                .addOnFailureListener(e -> {
                    setLoadingState(false);
                    showSnackbar(getString(R.string.error_load_complaints));
                });
    }

    // ─── Status Badge ─────────────────────────────────────────────────────────

    /** ✅ ContextCompat replaces deprecated getDrawable() and getColor() */
    private void applyStatusBadge(String status) {
        String label;
        int    bgRes;
        int    colorRes;

        switch (status) {
            case AppConstants.STATUS_IN_PROGRESS:
                label = "In Progress";
                bgRes     = R.drawable.bg_status_progress;
                colorRes  = R.color.status_progress;
                break;
            case AppConstants.STATUS_COMPLETED:
                label = "Completed";
                bgRes     = R.drawable.bg_status_completed;
                colorRes  = R.color.status_completed;
                break;
            case AppConstants.STATUS_REJECTED:
                label = "Rejected";
                bgRes     = R.drawable.bg_status_pending;
                colorRes  = R.color.status_rejected;
                break;
            default:
                label = "Pending";
                bgRes     = R.drawable.bg_status_pending;
                colorRes  = R.color.status_pending;
        }

        tvStatus.setText(label);
        tvStatus.setBackground(ContextCompat.getDrawable(this, bgRes));      // ✅ no crash
        tvStatus.setTextColor(ContextCompat.getColor(this, colorRes));        // ✅ no crash
    }

    // ─── Timeline ─────────────────────────────────────────────────────────────

    private void buildTimeline(String status, String timestamp) {
        llTimeline.removeAllViews();

        addTimelineItem("✅", "Complaint Submitted", timestamp,
                "Your complaint has been successfully registered.", true);

        boolean underReview = !AppConstants.STATUS_PENDING.equals(status);
        addTimelineItem(underReview ? "✅" : "⏳", "Under Review",
                underReview ? timestamp : "Pending",
                "Admin will review your complaint.", underReview);

        boolean inProgress = AppConstants.STATUS_IN_PROGRESS.equals(status)
                || AppConstants.STATUS_COMPLETED.equals(status);
        addTimelineItem(inProgress ? "🔄" : "⏳", "In Progress",
                inProgress ? timestamp : "Pending",
                "Technical team is working on resolving the issue.", inProgress);

        boolean completed = AppConstants.STATUS_COMPLETED.equals(status);
        addTimelineItem(completed ? "✅" : "⏳", "Resolved",
                completed ? timestamp : "Pending",
                "Issue has been resolved successfully.", completed);
    }

    private void addTimelineItem(String icon, String title, String time,
                                 String desc, boolean active) {
        android.widget.LinearLayout item = new android.widget.LinearLayout(this);
        item.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        item.setPadding(0, 8, 0, 8);

        TextView tvIcon = new TextView(this);
        tvIcon.setText(icon);
        tvIcon.setTextSize(18);
        android.widget.LinearLayout.LayoutParams iconP =
                new android.widget.LinearLayout.LayoutParams(40, 40);
        tvIcon.setLayoutParams(iconP);
        tvIcon.setGravity(android.view.Gravity.CENTER);

        android.widget.LinearLayout textLayout = new android.widget.LinearLayout(this);
        textLayout.setOrientation(android.widget.LinearLayout.VERTICAL);
        android.widget.LinearLayout.LayoutParams tp =
                new android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        tp.setMarginStart(12);
        textLayout.setLayoutParams(tp);

        int activeColor   = ContextCompat.getColor(this, R.color.text_primary);    // ✅
        int inactiveColor = ContextCompat.getColor(this, R.color.text_secondary);  // ✅

        TextView tvTitle = new TextView(this);
        tvTitle.setText(title);
        tvTitle.setTextSize(14);
        tvTitle.setTextColor(active ? activeColor : inactiveColor);
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView tvTime = new TextView(this);
        tvTime.setText(time);
        tvTime.setTextSize(11);
        tvTime.setTextColor(inactiveColor);

        TextView tvDesc = new TextView(this);
        tvDesc.setText(desc);
        tvDesc.setTextSize(12);
        tvDesc.setTextColor(inactiveColor);

        textLayout.addView(tvTitle);
        textLayout.addView(tvTime);
        textLayout.addView(tvDesc);
        item.addView(tvIcon);
        item.addView(textLayout);
        llTimeline.addView(item);

        View divider = new View(this);
        divider.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 1));
        divider.setBackgroundColor(ContextCompat.getColor(this, R.color.divider)); // ✅
        llTimeline.addView(divider);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /** Returns value if non-null/non-empty, otherwise returns fallback. */
    private String safe(String value, String fallback) {
        return (value != null && !value.isEmpty()) ? value : fallback;
    }

    private void setLoadingState(boolean loading) {
        if (progressBar != null) {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }

    private void showSnackbar(String message) {
        View root = findViewById(android.R.id.content);
        Snackbar.make(root, message, Snackbar.LENGTH_LONG).show();
    }
}