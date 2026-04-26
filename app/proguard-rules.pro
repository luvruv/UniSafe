# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Preserve line numbers for crash stack traces in release builds
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ── UniSafe Model Classes ───────────────────────────────────────────────────────
# Firestore's toObject() uses reflection — all model fields MUST be kept.
-keep class com.example.unisafe.models.** { *; }

# ── Firebase ────────────────────────────────────────────────────────────────────
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# ── Security Crypto (EncryptedSharedPreferences) ────────────────────────────────
-keep class androidx.security.crypto.** { *; }

# ── Material Components ──────────────────────────────────────────────────────────
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

# ── Glide (if added later) ───────────────────────────────────────────────────────
-keep public class * implements com.bumptech.glide.module.GlideModule
-dontwarn com.bumptech.glide.**