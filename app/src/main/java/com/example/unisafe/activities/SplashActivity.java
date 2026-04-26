package com.example.unisafe.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.example.unisafe.R;
import com.example.unisafe.utils.SessionManager;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    private SessionManager sessionManager;
    private MaterialButton btnGetStarted;
    private TextView tvSignInLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Log.d(TAG, "onCreate: Splash started");

        sessionManager = new SessionManager(this);

        ImageView logoIcon = findViewById(R.id.iv_logo);
        TextView tvAppName = findViewById(R.id.tv_app_name);
        TextView tvTagline = findViewById(R.id.tv_tagline);
        TextView tvVersion = findViewById(R.id.tv_version);
        btnGetStarted = findViewById(R.id.btn_get_started);
        tvSignInLink = findViewById(R.id.tv_sign_in_link);

        // Scale animation for logo
        ScaleAnimation scaleAnim = new ScaleAnimation(
                0.5f, 1.0f, 0.5f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnim.setDuration(600);
        scaleAnim.setFillAfter(true);
        logoIcon.startAnimation(scaleAnim);

        // Fade animation for text
        AlphaAnimation fadeAnim = new AlphaAnimation(0f, 1f);
        fadeAnim.setDuration(800);
        fadeAnim.setStartOffset(400);
        fadeAnim.setFillAfter(true);
        tvAppName.startAnimation(fadeAnim);
        tvTagline.startAnimation(fadeAnim);
        tvVersion.startAnimation(fadeAnim);

        // Delayed auto‑navigation (canceled if user taps Get Started or Sign In)
        Handler handler = new Handler();
        Runnable navigateRunnable = () -> {
            Log.d(TAG, "onDelay: Navigating from splash");
            if (sessionManager.isLoggedIn()) {
                if (sessionManager.isAdmin()) {
                    startActivity(new Intent(SplashActivity.this, AdminDashboardActivity.class));
                } else {
                    startActivity(new Intent(SplashActivity.this, DashboardActivity.class));
                }
            } else {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }
            finish();
        };
        handler.postDelayed(navigateRunnable, 2500);

        // Get Started click – go to Signup flow
        btnGetStarted.setOnClickListener(v -> {
            handler.removeCallbacks(navigateRunnable);
            startActivity(new Intent(SplashActivity.this, SignupActivity.class));
        });

        // Sign In link click – go directly to Login
        tvSignInLink.setOnClickListener(v -> {
            handler.removeCallbacks(navigateRunnable);
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart called");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause called");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop called");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy called");
    }
}