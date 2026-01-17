package com.example.campusride.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.campusride.R;
import com.example.campusride.models.User;

/**
 * Login Activity - Authentication screen for drivers and admins
 */
public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private MaterialButton btnLogin;
    private MaterialButton btnContinueStudent;
    private View progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        initializeViews();

        // Setup click listeners
        setupClickListeners();
    }

    /**
     * Initialize all views
     */
    private void initializeViews() {
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        btnContinueStudent = findViewById(R.id.btn_continue_student);
        progressBar = findViewById(R.id.progress_bar);
    }

    /**
     * Setup click listeners
     */
    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> attemptLogin());

        btnContinueStudent.setOnClickListener(v -> {
            // Navigate to main activity as guest/student
            navigateToMainActivity();
        });

        findViewById(R.id.tv_forgot_password).setOnClickListener(v -> {
            // TODO: Implement forgot password
            Toast.makeText(this, "Forgot password feature coming soon", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Attempt to login with email and password
     */
    private void attemptLogin() {
        // Get input values
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate inputs
        if (!validateInputs(email, password)) {
            return;
        }

        // Show progress
        showProgress(true);

        // Attempt Firebase authentication
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            checkUserRoleAndNavigate(user.getUid());
                        }
                    } else {
                        // Sign in failed
                        showProgress(false);
                        Toast.makeText(LoginActivity.this,
                                getString(R.string.login_error),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Validate email and password inputs
     */
    private boolean validateInputs(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email");
            etEmail.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Check user role and navigate to appropriate screen
     */
    private void checkUserRoleAndNavigate(String userId) {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    showProgress(false);

                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            // Update last active
                            user.updateLastActive();
                            db.collection("users").document(userId)
                                    .update("lastActive", user.getLastActive());

                            // Navigate based on user type
                            if (user.isDriver()) {
                                Toast.makeText(this, "Welcome, " + user.getFullName(), Toast.LENGTH_SHORT).show();
                                navigateToDriverDashboard();
                            } else if (user.isAdmin()) {
                                Toast.makeText(this, "Welcome Admin, " + user.getFullName(), Toast.LENGTH_SHORT).show();
                                // TODO: Navigate to admin dashboard
                                navigateToMainActivity();
                            } else {
                                // Regular student
                                navigateToMainActivity();
                            }
                        } else {
                            navigateToMainActivity();
                        }
                    } else {
                        // User document doesn't exist, treat as student
                        navigateToMainActivity();
                    }
                })
                .addOnFailureListener(e -> {
                    showProgress(false);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    navigateToMainActivity();
                });
    }

    /**
     * Show/hide progress indicator
     */
    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!show);
        btnContinueStudent.setEnabled(!show);
        etEmail.setEnabled(!show);
        etPassword.setEnabled(!show);
    }

    /**
     * Navigate to Main Activity
     */
    private void navigateToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Navigate to Driver Dashboard
     */
    private void navigateToDriverDashboard() {
        Intent intent = new Intent(LoginActivity.this, DriverDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is already signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User is already logged in, check role and navigate
            checkUserRoleAndNavigate(currentUser.getUid());
        }
    }
}