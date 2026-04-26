package com.example.unisafe.utils;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

/**
 * Singleton accessor for Firestore with offline persistence enabled.
 *
 * Usage:
 *   FirebaseHelper.complaints().get()...
 *   FirebaseHelper.users().document(uid).get()...
 *
 * Offline persistence means Firestore caches data locally so the app
 * remains partially functional without a network connection.
 */
public final class FirebaseHelper {

    private static FirebaseFirestore instance;

    private FirebaseHelper() {}

    /** Returns the Firestore instance with offline persistence enabled. */
    public static FirebaseFirestore db() {
        if (instance == null) {
            instance = FirebaseFirestore.getInstance();
            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)   // ✅ offline cache
                    .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                    .build();
            instance.setFirestoreSettings(settings);
        }
        return instance;
    }

    /** Shortcut to the "complaints" collection. */
    public static CollectionReference complaints() {
        return db().collection(AppConstants.COLLECTION_COMPLAINTS);
    }

    /** Shortcut to the "users" collection. */
    public static CollectionReference users() {
        return db().collection(AppConstants.COLLECTION_USERS);
    }
}
