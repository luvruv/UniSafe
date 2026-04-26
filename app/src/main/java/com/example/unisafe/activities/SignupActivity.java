package com.example.unisafe.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.unisafe.R;
import com.example.unisafe.utils.AppConstants;
import com.example.unisafe.utils.SessionManager;
import com.example.unisafe.utils.ValidationUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private TextInputEditText    etName, etEmail, etPhone, etPassword,
                                 etStudentId, etRoom, etDob;
    private AutoCompleteTextView spGender, spBlock;
    private MaterialButton       btnSignUp;
    private TextView             tvLogin;
    private View                 progressBar;
    private FirebaseAuth         mAuth;
    private FirebaseFirestore    db;
    private SessionManager       sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth          = FirebaseAuth.getInstance();
        db             = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(this);

        initViews();
        setupDropdowns();
        setListeners();
    }

    // ─── View Initialisation ──────────────────────────────────────────────────

    private void initViews() {
        etName      = findViewById(R.id.et_name);
        etEmail     = findViewById(R.id.et_email);
        etPhone     = findViewById(R.id.et_phone);
        etPassword  = findViewById(R.id.et_password);
        etStudentId = findViewById(R.id.et_student_id);
        etRoom      = findViewById(R.id.et_room);
        etDob       = findViewById(R.id.et_dob);
        spGender    = findViewById(R.id.sp_gender);
        spBlock     = findViewById(R.id.sp_block);
        btnSignUp   = findViewById(R.id.btn_sign_up);
        tvLogin     = findViewById(R.id.tv_login);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupDropdowns() {
        spGender.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line,
                new String[]{"Male", "Female", "Other"}));

        spBlock.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line,
                new String[]{"Tower 1", "Tower 2", "Tower 3", "Tower 4", "Girls Hostel 1", "Girls Hostel 2"}));
    }

    private void setListeners() {
        btnSignUp.setOnClickListener(v -> registerUser());
        tvLogin.setOnClickListener(v -> finish());
    }

    // ─── Registration ─────────────────────────────────────────────────────────

    private void registerUser() {
        String name      = text(etName);
        String email     = text(etEmail);
        String phone     = text(etPhone);
        String password  = text(etPassword);
        String studentId = text(etStudentId);
        String room      = text(etRoom);
        String dob       = text(etDob);
        String gender    = spGender.getText().toString().trim();
        String block     = spBlock.getText().toString().trim();

        // ✅ Full validation with clear error messages
        if (!ValidationUtils.isNotEmpty(name)) {
            etName.setError(getString(R.string.error_required)); etName.requestFocus(); return;
        }
        if (!ValidationUtils.isValidEmail(email)) {
            etEmail.setError(getString(R.string.error_invalid_email)); etEmail.requestFocus(); return;
        }
        if (!ValidationUtils.isValidPhone(phone)) {
            etPhone.setError(getString(R.string.error_invalid_phone)); etPhone.requestFocus(); return;
        }
        if (!ValidationUtils.isValidPassword(password)) {
            etPassword.setError(getString(R.string.error_password_short)); etPassword.requestFocus(); return;
        }
        if (!ValidationUtils.isNotEmpty(studentId)) {
            etStudentId.setError(getString(R.string.error_required)); etStudentId.requestFocus(); return;
        }
        if (!ValidationUtils.isValidRoomNumber(room)) {
            etRoom.setError(getString(R.string.error_room_number)); etRoom.requestFocus(); return;
        }
        if (!ValidationUtils.isNotEmpty(gender)) {
            showSnackbar(getString(R.string.error_select_gender)); return;
        }
        if (!ValidationUtils.isNotEmpty(block)) {
            showSnackbar(getString(R.string.error_select_block)); return;
        }

        setLoading(true);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();

                    Map<String, Object> userData = new HashMap<>();
                    userData.put("id",           uid);
                    userData.put("name",         name);
                    userData.put("email",        email);
                    userData.put("phone",        phone);
                    userData.put("studentId",    studentId);
                    userData.put("block",        block);
                    userData.put("roomNumber",   room);
                    userData.put("gender",       gender);
                    userData.put("dateOfBirth",  dob);
                    userData.put("role",         AppConstants.ROLE_STUDENT);
                    userData.put("profileImageUrl", "");

                    db.collection(AppConstants.COLLECTION_USERS).document(uid).set(userData)
                            .addOnSuccessListener(unused -> {
                                setLoading(false);
                                sessionManager.createSession(uid, name, email,
                                        AppConstants.ROLE_STUDENT, block, room, studentId);
                                startActivity(new Intent(this, DashboardActivity.class));
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                setLoading(false);
                                // Auth account created but profile save failed — sign out to avoid orphan auth
                                mAuth.signOut();
                                showSnackbar(getString(R.string.error_submit_complaint));
                            });
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    String msg;
                    if (e instanceof FirebaseAuthException) {
                        msg = ValidationUtils.getFriendlyAuthError(
                                ((FirebaseAuthException) e).getErrorCode());
                    } else {
                        msg = ValidationUtils.getFriendlyAuthError(null);
                    }
                    showSnackbar(msg);
                });
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private String text(TextInputEditText field) {
        return (field != null && field.getText() != null)
                ? field.getText().toString().trim() : "";
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSignUp.setEnabled(!loading);
    }

    private void showSnackbar(String message) {
        Snackbar.make(btnSignUp, message, Snackbar.LENGTH_LONG).show();
    }
}