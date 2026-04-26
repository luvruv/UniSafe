package com.example.unisafe.utils;

/**
 * Central repository for all magic strings used across the app.
 * Use these constants instead of raw string literals to prevent typos
 * and make refactoring safe.
 */
public final class AppConstants {

    private AppConstants() {} // Prevent instantiation

    // ─── Firestore Collections ─────────────────────────────────────────────────
    public static final String COLLECTION_USERS      = "users";
    public static final String COLLECTION_COMPLAINTS = "complaints";

    // ─── Complaint Status Values ───────────────────────────────────────────────
    public static final String STATUS_PENDING     = "pending";
    public static final String STATUS_IN_PROGRESS = "in_progress";
    public static final String STATUS_COMPLETED   = "completed";
    public static final String STATUS_REJECTED    = "rejected";

    // ─── User Roles ────────────────────────────────────────────────────────────
    public static final String ROLE_STUDENT = "student";
    public static final String ROLE_ADMIN   = "admin";

    // ─── Complaint Priorities ──────────────────────────────────────────────────
    public static final String PRIORITY_LOW    = "low";
    public static final String PRIORITY_MEDIUM = "medium";
    public static final String PRIORITY_HIGH   = "high";

    // ─── Complaint Categories ──────────────────────────────────────────────────
    public static final String CATEGORY_ELECTRICITY = "Electricity";
    public static final String CATEGORY_WIFI        = "WiFi & Internet";
    public static final String CATEGORY_WATER       = "Water Supply";
    public static final String CATEGORY_CLEANING    = "Cleaning & Hygiene";
    public static final String CATEGORY_MAINTENANCE = "Maintenance";

    // ─── Intent Extras ────────────────────────────────────────────────────────
    public static final String EXTRA_COMPLAINT_ID = "complaint_id";
    public static final String EXTRA_CATEGORY     = "category";

    // ─── Firestore Document Fields ─────────────────────────────────────────────
    public static final String FIELD_USER_ID    = "userId";
    public static final String FIELD_STATUS     = "status";
    public static final String FIELD_CREATED_AT = "createdAt";
    public static final String FIELD_TIMESTAMP  = "timestamp";
    public static final String FIELD_ROLE       = "role";
    public static final String FIELD_CATEGORY   = "category";
}
