package com.example.campusride.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.firestore.GeoPoint;

/**
 * Helper class for location-related operations
 */
public class LocationHelper {

    private static final int UPDATE_INTERVAL = 30000; // 30 seconds
    private static final int FASTEST_INTERVAL = 15000; // 15 seconds
    private static final float MIN_DISTANCE = 10; // 10 meters

    private final Context context;
    private final FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    public interface LocationUpdateListener {
        void onLocationUpdate(Location location);
        void onLocationError(String error);
    }

    public LocationHelper(Context context) {
        this.context = context;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    /**
     * Check if location permissions are granted
     */
    public boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Get last known location
     */
    public void getLastLocation(LocationUpdateListener listener) {
        if (!hasLocationPermission()) {
            listener.onLocationError("Location permission not granted");
            return;
        }

        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            listener.onLocationUpdate(location);
                        } else {
                            listener.onLocationError("Unable to get location");
                        }
                    })
                    .addOnFailureListener(e ->
                            listener.onLocationError("Error getting location: " + e.getMessage()));
        } catch (SecurityException e) {
            listener.onLocationError("Security exception: " + e.getMessage());
        }
    }

    /**
     * Start location updates
     */
    public void startLocationUpdates(LocationUpdateListener listener) {
        if (!hasLocationPermission()) {
            listener.onLocationError("Location permission not granted");
            return;
        }

        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL)
                .setMinUpdateIntervalMillis(FASTEST_INTERVAL)
                .setMinUpdateDistanceMeters(MIN_DISTANCE)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        listener.onLocationUpdate(location);
                    }
                }
            }
        };

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback, Looper.getMainLooper());
        } catch (SecurityException e) {
            listener.onLocationError("Security exception: " + e.getMessage());
        }
    }

    /**
     * Stop location updates
     */
    public void stopLocationUpdates() {
        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    /**
     * Calculate distance between two points using Haversine formula
     * @return distance in kilometers
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS = 6371; // Radius in kilometers

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    /**
     * Calculate distance between Location and GeoPoint
     * @return distance in kilometers
     */
    public static double calculateDistance(Location location, GeoPoint geoPoint) {
        return calculateDistance(
                location.getLatitude(),
                location.getLongitude(),
                geoPoint.getLatitude(),
                geoPoint.getLongitude()
        );
    }

    /**
     * Calculate distance between two GeoPoints
     * @return distance in kilometers
     */
    public static double calculateDistance(GeoPoint point1, GeoPoint point2) {
        return calculateDistance(
                point1.getLatitude(),
                point1.getLongitude(),
                point2.getLatitude(),
                point2.getLongitude()
        );
    }

    /**
     * Calculate ETA based on distance and average speed
     * @param distanceKm distance in kilometers
     * @param averageSpeedKmh average speed in km/h (default 30)
     * @return ETA in minutes
     */
    public static int calculateETA(double distanceKm, double averageSpeedKmh) {
        if (distanceKm <= 0) return 0;
        double hours = distanceKm / averageSpeedKmh;
        return (int) Math.ceil(hours * 60); // Convert to minutes
    }

    /**
     * Calculate ETA with default speed of 30 km/h
     */
    public static int calculateETA(double distanceKm) {
        return calculateETA(distanceKm, 30.0);
    }

    /**
     * Format distance for display
     */
    public static String formatDistance(double distanceKm) {
        if (distanceKm < 1.0) {
            int meters = (int) (distanceKm * 1000);
            return meters + " m";
        } else {
            return String.format("%.1f km", distanceKm);
        }
    }

    /**
     * Format ETA for display
     */
    public static String formatETA(int minutes) {
        if (minutes < 1) {
            return "Arriving now";
        } else if (minutes == 1) {
            return "1 min";
        } else if (minutes < 60) {
            return minutes + " min";
        } else {
            int hours = minutes / 60;
            int remainingMinutes = minutes % 60;
            if (remainingMinutes == 0) {
                return hours + " hr";
            } else {
                return hours + " hr " + remainingMinutes + " min";
            }
        }
    }

    /**
     * Check if location is within UCC campus bounds
     */
    public static boolean isWithinCampusBounds(double latitude, double longitude) {
        // UCC approximate bounds
        final double MIN_LAT = 5.095;
        final double MAX_LAT = 5.115;
        final double MIN_LON = -1.300;
        final double MAX_LON = -1.275;

        return latitude >= MIN_LAT && latitude <= MAX_LAT &&
                longitude >= MIN_LON && longitude <= MAX_LON;
    }

    /**
     * Convert Location to GeoPoint
     */
    public static GeoPoint locationToGeoPoint(Location location) {
        return new GeoPoint(location.getLatitude(), location.getLongitude());
    }

    /**
     * Get bearing between two points (direction in degrees)
     */
    public static float calculateBearing(double lat1, double lon1, double lat2, double lon2) {
        double dLon = Math.toRadians(lon2 - lon1);
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);

        double y = Math.sin(dLon) * Math.cos(lat2Rad);
        double x = Math.cos(lat1Rad) * Math.sin(lat2Rad) -
                Math.sin(lat1Rad) * Math.cos(lat2Rad) * Math.cos(dLon);

        double bearing = Math.toDegrees(Math.atan2(y, x));
        return (float) ((bearing + 360) % 360);
    }
}