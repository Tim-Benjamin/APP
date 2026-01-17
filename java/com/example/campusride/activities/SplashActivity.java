package com.example.campusride.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.campusride.R;
import com.example.campusride.models.User;

/**
 * Splash Activity - Entry point of the application
 * Displays branding and checks authentication status
 */
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2000; // 2 seconds

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Delay and check authentication
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                checkAuthenticationAndNavigate();
            }
        }, SPLASH_DELAY);
    }

    /**
     * Check if user is logged in and navigate accordingly
     */
    private void checkAuthenticationAndNavigate() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // User is signed in, check their role
            checkUserRoleAndNavigate(currentUser.getUid());
        } else {
            // No user signed in, go to student main activity
            navigateToMainActivity();
        }
    }

    /**
     * Check user role in Firestore and navigate to appropriate screen
     */
    private void checkUserRoleAndNavigate(String userId) {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            // Update last active timestamp
                            user.updateLastActive();
                            db.collection("users").document(userId)
                                    .update("lastActive", user.getLastActive());

                            // Navigate based on user type
                            if (user.isDriver()) {
                                navigateToDriverDashboard();
                            } else if (user.isAdmin()) {
                                // TODO: Navigate to admin dashboard when implemented
                                navigateToMainActivity();
                            } else {
                                navigateToMainActivity();
                            }
                        } else {
                            navigateToMainActivity();
                        }
                    } else {
                        // User document doesn't exist, go to main activity
                        navigateToMainActivity();
                    }
                })
                .addOnFailureListener(e -> {
                    // Error fetching user data, go to main activity
                    navigateToMainActivity();
                });
    }

    /**
     * Navigate to Main Activity (Student Interface)
     */
    private void navigateToMainActivity() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Navigate to Driver Dashboard
     */
    private void navigateToDriverDashboard() {
        Intent intent = new Intent(SplashActivity.this, DriverDashboardActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Navigate to Login Activity
     */
    private void navigateToLogin() {
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}