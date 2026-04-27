package com.example.unisafe.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.unisafe.R;
import com.example.unisafe.utils.AppConstants;
import com.example.unisafe.utils.SessionManager;
import com.example.unisafe.utils.ValidationUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private MaterialButton    btnSignIn, btnStudent, btnAdmin;
    private MaterialButtonToggleGroup toggleRole;
    private TextView          tvSignUp, tvForgot;
    private FirebaseAuth      mAuth;
    private FirebaseFirestore db;
    private SessionManager    sessionManager;
    private String            selectedRole = AppConstants.ROLE_STUDENT;
    private View              progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth          = FirebaseAuth.getInstance();
        db             = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(this);

        // Auto-advance if already logged in
        if (sessionManager.isLoggedIn()) {
            navigateByRole(sessionManager.getUserRole());
            return;
        }

        initViews();
        setListeners();
        highlightRoleButton(AppConstants.ROLE_STUDENT);
    }

    // ─── View Initialisation ──────────────────────────────────────────────────

    private void initViews() {
        etEmail     = findViewById(R.id.et_email);
        etPassword  = findViewById(R.id.et_password);
        btnSignIn   = findViewById(R.id.btn_sign_in);
        btnStudent  = findViewById(R.id.btn_student);
        btnAdmin    = findViewById(R.id.btn_admin);
        toggleRole  = findViewById(R.id.toggle_role);
        tvSignUp    = findViewById(R.id.tv_sign_up);
        tvForgot    = findViewById(R.id.tv_forgot);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setListeners() {
        toggleRole.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            if (checkedId == R.id.btn_admin) {
                selectedRole = AppConstants.ROLE_ADMIN;
                highlightRoleButton(AppConstants.ROLE_ADMIN);
            } else {
                selectedRole = AppConstants.ROLE_STUDENT;
                highlightRoleButton(AppConstants.ROLE_STUDENT);
            }
        });

        int checkedId = toggleRole.getCheckedButtonId();
        if (checkedId == R.id.btn_admin) {
            selectedRole = AppConstants.ROLE_ADMIN;
            highlightRoleButton(AppConstants.ROLE_ADMIN);
        } else {
            selectedRole = AppConstants.ROLE_STUDENT;
            highlightRoleButton(AppConstants.ROLE_STUDENT);
        }

        btnSignIn.setOnClickListener(v -> loginUser());

        tvSignUp.setOnClickListener(v ->
                startActivity(new Intent(this, SignupActivity.class)));

        tvForgot.setOnClickListener(v -> sendPasswordReset());
    }

    // ─── Auth ─────────────────────────────────────────────────────────────────

    private void loginUser() {
        String email    = etEmail.getText()    != null ? etEmail.getText().toString().trim()    : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        // ✅ Local validation before hitting Firebase
        if (!ValidationUtils.isValidEmail(email)) {
            etEmail.setError(getString(R.string.error_invalid_email));
            etEmail.requestFocus();
            return;
        }
        if (!ValidationUtils.isValidPassword(password)) {
            etPassword.setError(getString(R.string.error_password_short));
            etPassword.requestFocus();
            return;
        }

        setLoading(true);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    if (authResult.getUser() != null) {
                        fetchUserData(authResult.getUser().getUid(), email);
                    } else {
                        handleAuthFailure(null);
                    }
                })
                .addOnFailureListener(e -> handleAuthFailure(e));
    }

    private void fetchUserData(String uid, String email) {
        db.collection(AppConstants.COLLECTION_USERS).document(uid).get()
                .addOnSuccessListener(doc -> {
                    setLoading(false);
                    if (doc.exists()) {
                        String name      = doc.getString("name");
                        String role      = AppConstants.normalizeRole(doc.getString(AppConstants.FIELD_ROLE));
                        String block     = doc.getString("block");
                        String room      = doc.getString("roomNumber");
                        String studentId = doc.getString("studentId");
                        String expectedRole = AppConstants.normalizeRole(selectedRole);

                        // ✅ Role mismatch — friendly message with actual role
                        if (!expectedRole.equals(role)) {
                            showSnackbar(getString(R.string.error_wrong_role, role.isEmpty() ? getString(R.string.student) : role));
                            mAuth.signOut();
                            setLoading(false);
                            return;
                        }

                        sessionManager.createSession(uid, name, email, role, block, room, studentId);
                        navigateByRole(role);
                    } else {
                        mAuth.signOut();
                        showSnackbar(getString(R.string.error_login_failed));
                    }
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    mAuth.signOut();
                    showSnackbar(getString(R.string.error_login_failed));
                });
    }

    private void sendPasswordReset() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        if (!ValidationUtils.isValidEmail(email)) {
            etEmail.setError(getString(R.string.error_invalid_email));
            etEmail.requestFocus();
            return;
        }
        mAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(u -> showSnackbar(getString(R.string.reset_email_sent)))
                .addOnFailureListener(e -> showSnackbar(getString(R.string.error_login_failed)));
    }

    // ─── UI Helpers ───────────────────────────────────────────────────────────

    private void highlightRoleButton(String role) {
        boolean isStudent = AppConstants.ROLE_STUDENT.equals(role);

        btnStudent.setBackgroundTintList(ContextCompat.getColorStateList(this,
                isStudent ? R.color.primary_blue : android.R.color.transparent));
        btnStudent.setTextColor(ContextCompat.getColor(this,
                isStudent ? R.color.white : R.color.text_primary));

        btnAdmin.setBackgroundTintList(ContextCompat.getColorStateList(this,
                isStudent ? android.R.color.transparent : R.color.primary_blue));
        btnAdmin.setTextColor(ContextCompat.getColor(this,
                isStudent ? R.color.text_primary : R.color.white));
    }

    private void navigateByRole(String role) {
        Intent intent = AppConstants.isAdminRole(role)
                ? new Intent(this, AdminDashboardActivity.class)
                : new Intent(this, DashboardActivity.class);
        startActivity(intent);
        finish();
    }

    private void handleAuthFailure(Exception e) {
        setLoading(false);
        String friendlyMessage;
        if (e instanceof FirebaseAuthException) {
            friendlyMessage = ValidationUtils.getFriendlyAuthError(
                    ((FirebaseAuthException) e).getErrorCode());
        } else if (e != null && e.getMessage() != null
                && e.getMessage().toLowerCase().contains("network")) {
            friendlyMessage = ValidationUtils.getFriendlyAuthError("ERROR_NETWORK_REQUEST_FAILED");
        } else {
            friendlyMessage = ValidationUtils.getFriendlyAuthError(null);
        }
        showSnackbar(friendlyMessage);
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSignIn.setEnabled(!loading);
    }

    private void showSnackbar(String message) {
        Snackbar.make(btnSignIn, message, Snackbar.LENGTH_LONG).show();
    }
}