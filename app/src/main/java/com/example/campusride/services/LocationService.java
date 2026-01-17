package com.example.campusride.services;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import com.google.firebase.firestore.GeoPoint;
import com.example.campusride.utils.FirebaseHelper;
import com.example.campusride.utils.LocationHelper;
import com.example.campusride.utils.NotificationHelper;

/**
 * Foreground Service for tracking driver location
 */
public class LocationService extends Service {

    private static final String TAG = "LocationService";
    private static final int NOTIFICATION_ID = 1001;

    private LocationHelper locationHelper;
    private FirebaseHelper firebaseHelper;
    private NotificationHelper notificationHelper;

    private String driverId;
    private String shuttleId;
    private boolean isTracking = false;

    @Override
    public void onCreate() {
        super.onCreate();
        locationHelper = new LocationHelper(this);
        firebaseHelper = new FirebaseHelper();
        notificationHelper = new NotificationHelper(this);

        Log.d(TAG, "LocationService created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            driverId = intent.getStringExtra("driverId");
            shuttleId = intent.getStringExtra("shuttleId");

            if (driverId != null && shuttleId != null) {
                startLocationTracking();
            } else {
                Log.e(TAG, "Missing driverId or shuttleId");
                stopSelf();
            }
        }

        return START_STICKY;
    }

    /**
     * Start location tracking as foreground service
     */
    private void startLocationTracking() {
        // Create foreground notification
        Notification notification = notificationHelper.createLocationServiceNotification(
                "Driver",
                "Shuttle"
        );

        // Start as foreground service
        startForeground(NOTIFICATION_ID, notification);

        // Start location updates
        locationHelper.startLocationUpdates(new LocationHelper.LocationUpdateListener() {
            @Override
            public void onLocationUpdate(Location location) {
                handleLocationUpdate(location);
            }

            @Override
            public void onLocationError(String error) {
                Log.e(TAG, "Location error: " + error);
            }
        });

        isTracking = true;
        Log.d(TAG, "Location tracking started for shuttle: " + shuttleId);
    }

    /**
     * Handle location update
     */
    private void handleLocationUpdate(Location location) {
        if (shuttleId == null) return;

        // Convert to GeoPoint
        GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());

        // Update shuttle location in Firestore
        firebaseHelper.updateShuttleLocation(shuttleId, geoPoint)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Location updated: " + location.getLatitude() + ", " + location.getLongitude());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update location: " + e.getMessage());
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Stop location updates
        if (isTracking) {
            locationHelper.stopLocationUpdates();
            isTracking = false;
        }

        Log.d(TAG, "LocationService destroyed");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}