package com.example.campusride.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.example.campusride.R;
import com.example.campusride.models.Driver;
import com.example.campusride.models.Shuttle;
import com.example.campusride.services.LocationService;
import java.util.Date;
import java.util.Locale;

/**
 * Driver Dashboard Activity
 * Main interface for shuttle drivers to manage their shifts and status
 */
public class DriverDashboardActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 1001;

    // Views
    private MaterialToolbar toolbar;
    private TextView tvDriverName;
    private TextView tvShuttleAssignment;
    private TextView tvDriverStatus;
    private TextView tvCurrentRoute;
    private TextView tvPassengerCount;
    private TextView tvTripTime;
    private MaterialButton btnShiftControl;
    private MaterialButton btnBreak;
    private MaterialButton btnReportIssue;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    // Data
    private Driver driver;
    private Shuttle assignedShuttle;
    private boolean isOnShift = false;
    private ListenerRegistration driverListener;
    private ListenerRegistration shuttleListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_dashboard);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            // Not logged in, go to login
            navigateToLogin();
            return;
        }

        // Initialize views
        initializeViews();

        // Setup toolbar
        setupToolbar();

        // Load driver data
        loadDriverData();

        // Setup click listeners
        setupClickListeners();

        // Check location permissions
        checkLocationPermissions();
    }

    /**
     * Initialize all views
     */
    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        tvDriverName = findViewById(R.id.tv_driver_name);
        tvShuttleAssignment = findViewById(R.id.tv_shuttle_assignment);
        tvDriverStatus = findViewById(R.id.tv_driver_status);
        tvCurrentRoute = findViewById(R.id.tv_current_route);
        tvPassengerCount = findViewById(R.id.tv_passenger_count);
        tvTripTime = findViewById(R.id.tv_trip_time);
        btnShiftControl = findViewById(R.id.btn_shift_control);
        btnBreak = findViewById(R.id.btn_break);
        btnReportIssue = findViewById(R.id.btn_report_issue);
    }

    /**
     * Setup toolbar
     */
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.driver_dashboard);
        }
    }

    /**
     * Load driver data from Firestore
     */
    private void loadDriverData() {
        String userId = currentUser.getUid();

        // Listen to driver data changes
        driverListener = db.collection("drivers")
                .document(userId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error loading driver data", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        driver = snapshot.toObject(Driver.class);
                        if (driver != null) {
                            updateDriverUI();

                            // Load assigned shuttle if exists
                            if (driver.hasAssignedShuttle()) {
                                loadAssignedShuttle(driver.getAssignedShuttleId());
                            }
                        }
                    }
                });
    }

    /**
     * Load assigned shuttle data
     */
    private void loadAssignedShuttle(String shuttleId) {
        shuttleListener = db.collection("shuttles")
                .document(shuttleId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        assignedShuttle = snapshot.toObject(Shuttle.class);
                        updateShuttleUI();
                    }
                });
    }

    /**
     * Update driver UI with current data
     */
    private void updateDriverUI() {
        tvDriverName.setText(driver.getFullName());
        tvShuttleAssignment.setText(driver.getAssignedShuttleName() != null ?
                driver.getAssignedShuttleName() : "No shuttle assigned");

        // Update status badge
        Driver.DriverStatus status = driver.getStatus();
        tvDriverStatus.setText(getStatusText(status));
        tvDriverStatus.setBackgroundResource(getStatusBackground(status));

        // Update shift control button
        isOnShift = driver.isOnShift();
        updateShiftButton();
    }

    /**
     * Update shuttle UI with current data
     */
    private void updateShuttleUI() {
        if (assignedShuttle != null) {
            tvCurrentRoute.setText(assignedShuttle.getCurrentRoute() != null ?
                    assignedShuttle.getCurrentRoute() : "No active route");
            tvPassengerCount.setText(String.valueOf(assignedShuttle.getCurrentPassengers()));
        }
    }

    /**
     * Update shift button text and icon based on current state
     */
    private void updateShiftButton() {
        if (isOnShift) {
            btnShiftControl.setText(R.string.end_shift);
            btnShiftControl.setIconResource(R.drawable.ic_pause);
            btnBreak.setEnabled(true);
        } else {
            btnShiftControl.setText(R.string.start_shift);
            btnShiftControl.setIconResource(R.drawable.ic_play);
            btnBreak.setEnabled(false);
        }

        // Update trip time
        if (isOnShift && driver.getShiftStartTime() != null) {
            double hours = driver.getCurrentShiftHours();
            tvTripTime.setText(String.format(Locale.getDefault(), "%.1fh", hours));
        } else {
            tvTripTime.setText("0.0h");
        }
    }

    /**
     * Setup click listeners
     */
    private void setupClickListeners() {
        btnShiftControl.setOnClickListener(v -> {
            if (isOnShift) {
                confirmEndShift();
            } else {
                startShift();
            }
        });

        btnBreak.setOnClickListener(v -> toggleBreak());

        btnReportIssue.setOnClickListener(v -> reportIssue());
    }

    /**
     * Start driver shift
     */
    private void startShift() {
        if (driver == null || !driver.hasAssignedShuttle()) {
            Toast.makeText(this, "No shuttle assigned", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update driver status
        driver.startShift();

        // Update in Firestore
        db.collection("drivers").document(currentUser.getUid())
                .update(
                        "onShift", true,
                        "shiftStartTime", driver.getShiftStartTime(),
                        "status", Driver.DriverStatus.ON_DUTY.getValue()
                )
                .addOnSuccessListener(aVoid -> {
                    // Update shuttle status
                    updateShuttleStatus(Shuttle.ShuttleStatus.ACTIVE);

                    // Start location service
                    startLocationService();

                    Toast.makeText(this, getString(R.string.shift_started), Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to start shift", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Confirm and end driver shift
     */
    private void confirmEndShift() {
        new AlertDialog.Builder(this)
                .setTitle("End Shift")
                .setMessage(getString(R.string.confirm_end_shift))
                .setPositiveButton(R.string.yes, (dialog, which) -> endShift())
                .setNegativeButton(R.string.no, null)
                .show();
    }

    /**
     * End driver shift
     */
    private void endShift() {
        driver.endShift();

        // Update in Firestore
        db.collection("drivers").document(currentUser.getUid())
                .update(
                        "onShift", false,
                        "shiftEndTime", driver.getShiftEndTime(),
                        "totalHours", driver.getTotalHours(),
                        "status", Driver.DriverStatus.OFF_DUTY.getValue()
                )
                .addOnSuccessListener(aVoid -> {
                    // Update shuttle status
                    updateShuttleStatus(Shuttle.ShuttleStatus.OFFLINE);

                    // Stop location service
                    stopLocationService();

                    Toast.makeText(this, getString(R.string.shift_ended), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Toggle break status
     */
    private void toggleBreak() {
        if (driver.getStatus() == Driver.DriverStatus.ON_BREAK) {
            // Resume from break
            driver.setStatus(Driver.DriverStatus.ON_DUTY);
            updateShuttleStatus(Shuttle.ShuttleStatus.ACTIVE);
            Toast.makeText(this, "Break ended", Toast.LENGTH_SHORT).show();
        } else {
            // Start break
            driver.setStatus(Driver.DriverStatus.ON_BREAK);
            updateShuttleStatus(Shuttle.ShuttleStatus.ON_BREAK);
            Toast.makeText(this, "Break started", Toast.LENGTH_SHORT).show();
        }

        // Update in Firestore
        db.collection("drivers").document(currentUser.getUid())
                .update("status", driver.getStatusString());
    }

    /**
     * Update shuttle status
     */
    private void updateShuttleStatus(Shuttle.ShuttleStatus status) {
        if (driver != null && driver.hasAssignedShuttle()) {
            db.collection("shuttles").document(driver.getAssignedShuttleId())
                    .update("status", status.getValue(), "lastUpdated", new Date());
        }
    }

    /**
     * Report an issue
     */
    private void reportIssue() {
        // TODO: Implement issue reporting dialog
        Toast.makeText(this, "Issue reporting coming soon", Toast.LENGTH_SHORT).show();
    }

    /**
     * Check and request location permissions
     */
    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        }
    }

    /**
     * Start location tracking service
     */
    private void startLocationService() {
        Intent serviceIntent = new Intent(this, LocationService.class);
        serviceIntent.putExtra("driverId", currentUser.getUid());
        serviceIntent.putExtra("shuttleId", driver.getAssignedShuttleId());
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    /**
     * Stop location tracking service
     */
    private void stopLocationService() {
        Intent serviceIntent = new Intent(this, LocationService.class);
        stopService(serviceIntent);
    }

    /**
     * Get status text
     */
    private String getStatusText(Driver.DriverStatus status) {
        if (status == null) return "Off Duty";
        switch (status) {
            case ON_DUTY: return "Active";
            case ON_BREAK: return "On Break";
            case AVAILABLE: return "Available";
            default: return "Off Duty";
        }
    }

    /**
     * Get status background resource
     */
    private int getStatusBackground(Driver.DriverStatus status) {
        if (status == null) return R.drawable.badge_danger_background;
        switch (status) {
            case ON_DUTY: return R.drawable.badge_active_background;
            case ON_BREAK: return R.drawable.badge_warning_background;
            default: return R.drawable.badge_danger_background;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.driver_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_profile) {
            // TODO: Show profile
            return true;
        } else if (itemId == R.id.action_settings) {
            // TODO: Show settings
            return true;
        } else if (itemId == R.id.action_logout) {
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Logout
     */
    private void logout() {
        // If on shift, end it first
        if (isOnShift) {
            endShift();
        }

        mAuth.signOut();
        navigateToLogin();
    }

    /**
     * Navigate to login
     */
    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove listeners
        if (driverListener != null) {
            driverListener.remove();
        }
        if (shuttleListener != null) {
            shuttleListener.remove();
        }
    }
}