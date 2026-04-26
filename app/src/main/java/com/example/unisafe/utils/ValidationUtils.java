package com.example.unisafe.utils;

import android.text.TextUtils;
import android.util.Patterns;

/**
 * Stateless validation helper.
 * Call these before any Firebase / network operation to give the user
 * instant, local feedback without burning a network round-trip.
 */
public final class ValidationUtils {

    private ValidationUtils() {} // Prevent instantiation

    /**
     * Validates that an e-mail address is non-empty and matches the standard
     * RFC-5322 pattern provided by {@link Patterns#EMAIL_ADDRESS}.
     */
    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email)
                && Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches();
    }

    /**
     * Validates an Indian mobile number: exactly 10 digits, starting with
     * 6–9. Strips leading "+91" or "0" before checking.
     */
    public static boolean isValidPhone(String phone) {
        if (TextUtils.isEmpty(phone)) return false;
        String cleaned = phone.trim()
                .replaceAll("\\s+", "")
                .replaceAll("^\\+91", "")
                .replaceAll("^0", "");
        return cleaned.matches("[6-9]\\d{9}");
    }

    /**
     * Password must be at least 6 characters (Firebase minimum).
     * For stronger rules in a production environment, raise this to 8+
     * and require mixed case / digits.
     */
    public static boolean isValidPassword(String password) {
        return !TextUtils.isEmpty(password) && password.length() >= 6;
    }

    /**
     * Returns {@code true} if the string is non-null and has at least one
     * non-whitespace character.
     */
    public static boolean isNotEmpty(String value) {
        return !TextUtils.isEmpty(value) && !value.trim().isEmpty();
    }

    /**
     * Room number must be a positive integer (e.g. "204", "12B" is rejected).
     */
    public static boolean isValidRoomNumber(String room) {
        return isNotEmpty(room) && room.trim().matches("\\d+");
    }

    /**
     * Maps a Firebase Auth error code string to a user-friendly English message.
     * Raw error codes must never be surfaced directly to end users.
     */
    public static String getFriendlyAuthError(String errorCode) {
        if (TextUtils.isEmpty(errorCode)) return "An unexpected error occurred. Please try again.";
        switch (errorCode) {
            case "ERROR_INVALID_EMAIL":
            case "ERROR_INVALID_CREDENTIAL":
                return "The email address or password is incorrect.";
            case "ERROR_WRONG_PASSWORD":
                return "Incorrect password. Please try again.";
            case "ERROR_USER_NOT_FOUND":
                return "No account found for this email. Please sign up first.";
            case "ERROR_USER_DISABLED":
                return "This account has been disabled. Please contact support.";
            case "ERROR_EMAIL_ALREADY_IN_USE":
                return "An account already exists with this email. Please sign in.";
            case "ERROR_TOO_MANY_REQUESTS":
                return "Too many failed attempts. Please wait a moment and try again.";
            case "ERROR_NETWORK_REQUEST_FAILED":
                return "No internet connection. Please check your network settings.";
            case "ERROR_OPERATION_NOT_ALLOWED":
                return "This sign-in method is not enabled. Please contact support.";
            case "ERROR_WEAK_PASSWORD":
                return "Password is too weak. Use at least 6 characters.";
            default:
                return "Login failed. Please check your credentials and try again.";
        }
    }
}
