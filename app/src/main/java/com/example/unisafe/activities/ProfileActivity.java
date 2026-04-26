package com.example.unisafe.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.unisafe.R;
import com.example.unisafe.utils.AppConstants;
import com.example.unisafe.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private TextView       tvName, tvStudentId, tvFullName, tvEmail, tvPhone,
                           tvGender, tvDob, tvBlock, tvRoom,
                           tvTotalComplaints, tvActive, tvResolved;
    private MaterialButton btnLogout;
    private SessionManager sessionManager;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sessionManager = new SessionManager(this);
        db             = FirebaseFirestore.getInstance();

        initViews();
        loadProfile();
        loadStats();

        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());
        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    // ─── View Initialisation ──────────────────────────────────────────────────

    private void initViews() {
        tvName            = findViewById(R.id.tv_name);
        tvStudentId       = findViewById(R.id.tv_student_id);
        tvFullName        = findViewById(R.id.tv_full_name);
        tvEmail           = findViewById(R.id.tv_email);
        tvPhone           = findViewById(R.id.tv_phone);
        tvGender          = findViewById(R.id.tv_gender);
        tvDob             = findViewById(R.id.tv_dob);
        tvBlock           = findViewById(R.id.tv_block);
        tvRoom            = findViewById(R.id.tv_room);
        tvTotalComplaints = findViewById(R.id.tv_total_complaints);
        tvActive          = findViewById(R.id.tv_active);
        tvResolved        = findViewById(R.id.tv_resolved);
        btnLogout         = findViewById(R.id.btn_logout);
    }

    // ─── Data Loading ─────────────────────────────────────────────────────────

    private void loadProfile() {
        String userId = sessionManager.getUserId();
        if (userId.isEmpty()) return;

        db.collection(AppConstants.COLLECTION_USERS)
                .document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    // ✅ Null-safe reads — every field guarded with fallback "—"
                    tvName.setText(safe(doc.getString("name")));
                    tvStudentId.setText("Student ID: " + safe(doc.getString("studentId")));
                    tvFullName.setText(safe(doc.getString("name")));
                    tvEmail.setText(safe(doc.getString("email")));
                    tvPhone.setText(safe(doc.getString("phone")));
                    tvGender.setText(safe(doc.getString("gender")));
                    tvDob.setText(safe(doc.getString("dateOfBirth")));
                    tvBlock.setText(safe(doc.getString("block")));
                    tvRoom.setText(safe(doc.getString("roomNumber")));
                })
                .addOnFailureListener(e ->
                        Snackbar.make(btnLogout,
                                getString(R.string.error_load_complaints),
                                Snackbar.LENGTH_SHORT).show()
                );
    }

    private void loadStats() {
        String userId = sessionManager.getUserId();
        if (userId.isEmpty()) return;

        db.collection(AppConstants.COLLECTION_COMPLAINTS)
                .whereEqualTo(AppConstants.FIELD_USER_ID, userId)
                .get()
                .addOnSuccessListener(docs -> {
                    int total = docs.size(), active = 0, resolved = 0;
                    for (var doc : docs) {
                        String status = doc.getString(AppConstants.FIELD_STATUS);
                        if (AppConstants.STATUS_IN_PROGRESS.equals(status)
                                || AppConstants.STATUS_PENDING.equals(status)) {
                            active++;
                        } else if (AppConstants.STATUS_COMPLETED.equals(status)) {
                            resolved++;
                        }
                    }
                    tvTotalComplaints.setText(String.valueOf(total));
                    tvActive.setText(String.valueOf(active));
                    tvResolved.setText(String.valueOf(resolved));
                })
                .addOnFailureListener(e -> {
                    tvTotalComplaints.setText("—");
                    tvActive.setText("—");
                    tvResolved.setText("—");
                });
    }

    // ─── Logout ───────────────────────────────────────────────────────────────

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.logout))
                .setMessage(getString(R.string.logout_confirm_message))
                .setPositiveButton(getString(R.string.logout), (d, w) -> {
                    FirebaseAuth.getInstance().signOut();
                    sessionManager.logout();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /** Returns the value if non-null/non-empty, otherwise "—". */
    private String safe(String value) {
        return (value != null && !value.isEmpty()) ? value : "—";
    }
}