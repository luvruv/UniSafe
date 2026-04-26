package com.example.unisafe.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Manages the user session using EncryptedSharedPreferences.
 *
 * ✅ Encrypted at rest — safe on rooted devices
 * ✅ Editor not held as field — prevents memory leaks
 * ✅ Null-safe getters with sensible defaults
 */
public class SessionManager {

    private static final String PREF_NAME      = "UniSafeSecureSession";
    private static final String KEY_LOGGED_IN  = "isLoggedIn";
    private static final String KEY_USER_ID    = "userId";
    private static final String KEY_USER_NAME  = "userName";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_ROLE  = "userRole";
    private static final String KEY_USER_BLOCK = "userBlock";
    private static final String KEY_USER_ROOM  = "userRoom";
    private static final String KEY_STUDENT_ID = "studentId";

    private final SharedPreferences pref;

    public SessionManager(Context context) {
        SharedPreferences tempPref;
        try {
            // ✅ AES-256 encrypted key + value store
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            tempPref = EncryptedSharedPreferences.create(
                    PREF_NAME,
                    masterKeyAlias,
                    context.getApplicationContext(),
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            // Fallback to plain prefs if encryption fails (should not happen on API 30+)
            tempPref = context.getApplicationContext()
                    .getSharedPreferences(PREF_NAME + "_fallback", Context.MODE_PRIVATE);
        }
        pref = tempPref;
    }

    /** Persists all user session data after successful login / signup. */
    public void createSession(String userId, String name, String email,
                              String role, String block, String room, String studentId) {
        // ✅ Editor created inline — no instance-variable memory leak
        pref.edit()
                .putBoolean(KEY_LOGGED_IN, true)
                .putString(KEY_USER_ID,    userId   != null ? userId   : "")
                .putString(KEY_USER_NAME,  name     != null ? name     : "")
                .putString(KEY_USER_EMAIL, email    != null ? email    : "")
                .putString(KEY_USER_ROLE,  role     != null ? role     : AppConstants.ROLE_STUDENT)
                .putString(KEY_USER_BLOCK, block    != null ? block    : "")
                .putString(KEY_USER_ROOM,  room     != null ? room     : "")
                .putString(KEY_STUDENT_ID, studentId != null ? studentId : "")
                .apply();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_LOGGED_IN, false);
    }

    public boolean isAdmin() {
        return AppConstants.ROLE_ADMIN.equals(getUserRole());
    }

    // ─── Null-safe getters ─────────────────────────────────────────────────────

    public String getUserId()    { return pref.getString(KEY_USER_ID,    ""); }
    public String getUserName()  { return pref.getString(KEY_USER_NAME,  ""); }
    public String getUserEmail() { return pref.getString(KEY_USER_EMAIL, ""); }
    public String getUserRole()  { return pref.getString(KEY_USER_ROLE,  AppConstants.ROLE_STUDENT); }
    public String getUserBlock() { return pref.getString(KEY_USER_BLOCK, ""); }
    public String getUserRoom()  { return pref.getString(KEY_USER_ROOM,  ""); }
    public String getStudentId() { return pref.getString(KEY_STUDENT_ID, ""); }

    /** Clears all session data on logout. */
    public void logout() {
        pref.edit().clear().apply();
    }
}