package com.example.unisafe.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.unisafe.R;
import com.example.unisafe.utils.AppConstants;
import com.example.unisafe.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CreateComplaintActivity extends AppCompatActivity {

    private TextInputEditText etSubject, etDescription, etRoom;
    private TextView          tvBlockSelected;
    private LinearLayout      tvCatElec, tvCatWifi, tvCatWater, tvCatClean;
    private MaterialButton    btnLow, btnMedium, btnHigh, btnSubmit;
    private ImageView         ivAttachment;
    private LinearLayout      llUploadArea;
    private View              progressBar;
    private ImageButton       btnBack;

    private SessionManager   sessionManager;
    private FirebaseFirestore db;
    private FirebaseStorage   storage;

    private String selectedCategory = "";
    private String selectedPriority = AppConstants.PRIORITY_MEDIUM;
    private String selectedBlock    = "";
    private Uri    imageUri;

    // ─── Activity Result Launchers ────────────────────────────────────────────

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    displaySelectedImage(imageUri);
                }
            });

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    displaySelectedImage(imageUri);
                }
            });

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openCamera();
                } else {
                    showSnackbar(getString(R.string.error_camera_permission));
                }
            });

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_complaint);

        sessionManager = new SessionManager(this);
        db             = FirebaseFirestore.getInstance();
        storage        = FirebaseStorage.getInstance();

        initViews();
        setListeners();

        // Pre-select category if launched from a category shortcut
        String preCategory = getIntent().getStringExtra(AppConstants.EXTRA_CATEGORY);
        if (preCategory != null && !preCategory.isEmpty()) {
            selectCategory(preCategory);
        }

        // Pre-fill the user's room and block from session
        String room  = sessionManager.getUserRoom();
        String block = sessionManager.getUserBlock();
        if (!room.isEmpty())  etRoom.setText(room);
        if (!block.isEmpty()) {
            tvBlockSelected.setText(block);
            selectedBlock = block;
        }
    }

    // ─── View Initialisation ──────────────────────────────────────────────────

    private void initViews() {
        etSubject      = findViewById(R.id.et_subject);
        etDescription  = findViewById(R.id.et_description);
        etRoom         = findViewById(R.id.et_room);
        tvBlockSelected = findViewById(R.id.tv_block_selected);
        tvCatElec      = findViewById(R.id.tv_cat_electricity);
        tvCatWifi      = findViewById(R.id.tv_cat_wifi);
        tvCatWater     = findViewById(R.id.tv_cat_water);
        tvCatClean     = findViewById(R.id.tv_cat_cleaning);
        btnLow         = findViewById(R.id.btn_priority_low);
        btnMedium      = findViewById(R.id.btn_priority_medium);
        btnHigh        = findViewById(R.id.btn_priority_high);
        btnSubmit      = findViewById(R.id.btn_submit);
        ivAttachment   = findViewById(R.id.iv_attachment);
        llUploadArea   = findViewById(R.id.ll_upload_area);
        progressBar    = findViewById(R.id.progress_bar);
        btnBack        = findViewById(R.id.btn_back);

        // Default priority = medium
        selectPriority(AppConstants.PRIORITY_MEDIUM);
    }

    private void setListeners() {
        btnBack.setOnClickListener(v -> finish());

        tvCatElec.setOnClickListener(v  -> selectCategory(AppConstants.CATEGORY_ELECTRICITY));
        tvCatWifi.setOnClickListener(v  -> selectCategory(AppConstants.CATEGORY_WIFI));
        tvCatWater.setOnClickListener(v -> selectCategory(AppConstants.CATEGORY_WATER));
        tvCatClean.setOnClickListener(v -> selectCategory(AppConstants.CATEGORY_CLEANING));

        btnLow.setOnClickListener(v    -> selectPriority(AppConstants.PRIORITY_LOW));
        btnMedium.setOnClickListener(v -> selectPriority(AppConstants.PRIORITY_MEDIUM));
        btnHigh.setOnClickListener(v   -> selectPriority(AppConstants.PRIORITY_HIGH));

        llUploadArea.setOnClickListener(v  -> showImagePickerDialog());
        ivAttachment.setOnClickListener(v  -> showImagePickerDialog());
        btnSubmit.setOnClickListener(v     -> submitComplaint());
    }

    // ─── Image Picker ─────────────────────────────────────────────────────────

    private void showImagePickerDialog() {
        String[] options = {
                getString(R.string.take_photo),
                getString(R.string.choose_gallery),
                getString(R.string.cancel)
        };
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.attach_image))
                .setItems(options, (d, which) -> {
                    if (which == 0) checkCameraPermission();
                    else if (which == 1) openGallery();
                })
                .show();
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            File photoFile = createImageFile();
            imageUri = FileProvider.getUriForFile(
                    this, getPackageName() + ".fileprovider", photoFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            cameraLauncher.launch(intent);
        } catch (IOException e) {
            showSnackbar(getString(R.string.error_camera_open));
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private File createImageFile() throws IOException {
        String ts  = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File   dir = getExternalCacheDir();
        return File.createTempFile("IMG_" + ts + "_", ".jpg", dir);
    }

    private void displaySelectedImage(Uri uri) {
        if (uri == null) return;
        ivAttachment.setImageURI(uri);
        ivAttachment.setVisibility(View.VISIBLE);
        llUploadArea.setVisibility(View.GONE);
    }

    // ─── Category / Priority Selection ────────────────────────────────────────

    private void selectCategory(String category) {
        selectedCategory = category;
        int normal   = ContextCompat.getColor(this, R.color.background);
        int selected = ContextCompat.getColor(this, R.color.primary_light_blue);

        tvCatElec.setBackgroundColor(normal);
        tvCatWifi.setBackgroundColor(normal);
        tvCatWater.setBackgroundColor(normal);
        tvCatClean.setBackgroundColor(normal);

        if (AppConstants.CATEGORY_ELECTRICITY.equals(category)) tvCatElec.setBackgroundColor(selected);
        else if (AppConstants.CATEGORY_WIFI.equals(category))   tvCatWifi.setBackgroundColor(selected);
        else if (AppConstants.CATEGORY_WATER.equals(category))  tvCatWater.setBackgroundColor(selected);
        else if (AppConstants.CATEGORY_CLEANING.equals(category)) tvCatClean.setBackgroundColor(selected);
    }

    private void selectPriority(String priority) {
        selectedPriority = priority;
        int white    = ContextCompat.getColor(this, R.color.white);
        int textDark = ContextCompat.getColor(this, R.color.text_primary);

        // Reset all buttons
        btnLow.setBackgroundTintList(null);    btnLow.setTextColor(textDark);
        btnMedium.setBackgroundTintList(null); btnMedium.setTextColor(textDark);
        btnHigh.setBackgroundTintList(null);   btnHigh.setTextColor(textDark);

        // Highlight selected
        MaterialButton selected;
        switch (priority) {
            case AppConstants.PRIORITY_LOW:  selected = btnLow;  break;
            case AppConstants.PRIORITY_HIGH: selected = btnHigh; break;
            default:                         selected = btnMedium;
        }
        selected.setBackgroundTintList(
                ContextCompat.getColorStateList(this, R.color.primary_blue));
        selected.setTextColor(white);
    }

    // ─── Submission ───────────────────────────────────────────────────────────

    private void submitComplaint() {
        String subject     = etSubject.getText()     != null ? etSubject.getText().toString().trim()     : "";
        String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
        String room        = etRoom.getText()        != null ? etRoom.getText().toString().trim()        : "";

        // ── Validation ──────────────────────────────────────────────────────
        if (selectedCategory.isEmpty()) {
            showSnackbar(getString(R.string.error_select_category));
            return;
        }
        if (subject.isEmpty()) {
            etSubject.setError(getString(R.string.error_required));
            etSubject.requestFocus();
            return;
        }
        if (description.isEmpty()) {
            etDescription.setError(getString(R.string.error_required));
            etDescription.requestFocus();
            return;
        }
        if (room.isEmpty()) {
            etRoom.setError(getString(R.string.error_required));
            etRoom.requestFocus();
            return;
        }

        setLoading(true);

        // Human-readable display timestamp (kept for legacy display)
        String displayTimestamp = new SimpleDateFormat(
                "MMM dd, yyyy 'at' hh:mm a", Locale.getDefault()).format(new Date());

        if (imageUri != null) {
            uploadImageThenSave(subject, description, room, displayTimestamp);
        } else {
            saveComplaint(subject, description, room, displayTimestamp, "");
        }
    }

    private void uploadImageThenSave(String subject, String desc,
                                     String room, String displayTimestamp) {
        // Get a pre-generated ID to use as the storage path
        String tempId = FirebaseFirestore.getInstance()
                .collection(AppConstants.COLLECTION_COMPLAINTS).document().getId();
        StorageReference ref = storage.getReference()
                .child("complaints/" + tempId + ".jpg");

        ref.putFile(imageUri)
                .addOnSuccessListener(task ->
                        ref.getDownloadUrl().addOnSuccessListener(uri ->
                                saveComplaint(subject, desc, room, displayTimestamp, uri.toString()))
                )
                .addOnFailureListener(e ->
                        // Image upload failed → save complaint without image
                        saveComplaint(subject, desc, room, displayTimestamp, "")
                );
    }

    private void saveComplaint(String subject, String desc,
                               String room, String displayTimestamp, String imageUrl) {
        // ✅ Pre-generate DocumentReference so stored ID == Firestore document ID
        DocumentReference docRef = FirebaseFirestore.getInstance()
                .collection(AppConstants.COLLECTION_COMPLAINTS).document();
        String docId = docRef.getId();

        Map<String, Object> data = new HashMap<>();
        data.put("id",          docId);
        data.put("category",    selectedCategory);
        data.put("subject",     subject);
        data.put("description", desc);
        data.put("status",      AppConstants.STATUS_PENDING);
        data.put("priority",    selectedPriority);
        data.put("imageUri",    imageUrl);
        data.put("timestamp",   displayTimestamp);                // legacy display field
        data.put("createdAt",   FieldValue.serverTimestamp());    // ✅ reliable ordering
        data.put("userId",      sessionManager.getUserId());
        data.put("userName",    sessionManager.getUserName());
        data.put("block",       selectedBlock);
        data.put("roomNumber",  room);
        data.put("adminNote",   "");
        data.put("assignedTo",  "");

        docRef.set(data)
                .addOnSuccessListener(unused -> {
                    setLoading(false);
                    showSnackbar(getString(R.string.complaint_submitted));
                    // Brief delay so user sees the success message before screen closes
                    btnSubmit.postDelayed(this::finish, 1400);
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    showSnackbar(getString(R.string.error_submit_complaint));
                });
    }

    // ─── UI Helpers ───────────────────────────────────────────────────────────

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSubmit.setEnabled(!loading);
    }

    private void showSnackbar(String message) {
        Snackbar.make(btnSubmit, message, Snackbar.LENGTH_LONG).show();
    }
}