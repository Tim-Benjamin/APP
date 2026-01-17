package com.example.campusride.utils;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.example.campusride.models.Driver;
import com.example.campusride.models.Report;
import com.example.campusride.models.Route;
import com.example.campusride.models.Shuttle;
import com.example.campusride.models.Stop;
import com.example.campusride.models.User;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for Firebase Firestore operations
 */
public class FirebaseHelper {

    private final FirebaseFirestore db;

    // Collection names
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_DRIVERS = "drivers";
    public static final String COLLECTION_SHUTTLES = "shuttles";
    public static final String COLLECTION_STOPS = "stops";
    public static final String COLLECTION_ROUTES = "routes";
    public static final String COLLECTION_REPORTS = "reports";

    public FirebaseHelper() {
        this.db = FirebaseFirestore.getInstance();
    }

    // ========================================
    // USER OPERATIONS
    // ========================================

    /**
     * Create or update user
     */
    public Task<Void> saveUser(User user) {
        return db.collection(COLLECTION_USERS)
                .document(user.getUserId())
                .set(user);
    }

    /**
     * Get user by ID
     */
    public Task<DocumentSnapshot> getUser(String userId) {
        return db.collection(COLLECTION_USERS)
                .document(userId)
                .get();
    }

    /**
     * Update user's favorite stops
     */
    public Task<Void> updateFavoriteStops(String userId, java.util.List<String> favoriteStops) {
        return db.collection(COLLECTION_USERS)
                .document(userId)
                .update("favoriteStops", favoriteStops);
    }

    /**
     * Update user's last active timestamp
     */
    public Task<Void> updateUserLastActive(String userId) {
        return db.collection(COLLECTION_USERS)
                .document(userId)
                .update("lastActive", new Date());
    }

    // ========================================
    // SHUTTLE OPERATIONS
    // ========================================

    /**
     * Get all active shuttles
     */
    public Task<QuerySnapshot> getActiveShuttles() {
        return db.collection(COLLECTION_SHUTTLES)
                .whereIn("status", java.util.Arrays.asList("active", "on_break"))
                .get();
    }

    /**
     * Get shuttle by ID
     */
    public Task<DocumentSnapshot> getShuttle(String shuttleId) {
        return db.collection(COLLECTION_SHUTTLES)
                .document(shuttleId)
                .get();
    }

    /**
     * Update shuttle location
     */
    public Task<Void> updateShuttleLocation(String shuttleId, GeoPoint location) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("currentLocation", location);
        updates.put("latitude", location.getLatitude());
        updates.put("longitude", location.getLongitude());
        updates.put("lastUpdated", new Date());

        return db.collection(COLLECTION_SHUTTLES)
                .document(shuttleId)
                .update(updates);
    }

    /**
     * Update shuttle status
     */
    public Task<Void> updateShuttleStatus(String shuttleId, Shuttle.ShuttleStatus status) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status.getValue());
        updates.put("lastUpdated", new Date());

        return db.collection(COLLECTION_SHUTTLES)
                .document(shuttleId)
                .update(updates);
    }

    /**
     * Update shuttle passenger count
     */
    public Task<Void> updatePassengerCount(String shuttleId, int count) {
        return db.collection(COLLECTION_SHUTTLES)
                .document(shuttleId)
                .update("currentPassengers", count, "lastUpdated", new Date());
    }

    // ========================================
    // STOP OPERATIONS
    // ========================================

    /**
     * Get all active stops
     */
    public Task<QuerySnapshot> getActiveStops() {
        return db.collection(COLLECTION_STOPS)
                .whereEqualTo("isActive", true)
                .get();
    }

    /**
     * Get stop by ID
     */
    public Task<DocumentSnapshot> getStop(String stopId) {
        return db.collection(COLLECTION_STOPS)
                .document(stopId)
                .get();
    }

    /**
     * Get stops for a specific route
     */
    public Task<QuerySnapshot> getStopsForRoute(String routeName) {
        return db.collection(COLLECTION_STOPS)
                .whereArrayContains("routes", routeName)
                .whereEqualTo("isActive", true)
                .get();
    }

    // ========================================
    // ROUTE OPERATIONS
    // ========================================

    /**
     * Get all active routes
     */
    public Task<QuerySnapshot> getActiveRoutes() {
        return db.collection(COLLECTION_ROUTES)
                .whereEqualTo("isActive", true)
                .get();
    }

    /**
     * Get route by ID
     */
    public Task<DocumentSnapshot> getRoute(String routeId) {
        return db.collection(COLLECTION_ROUTES)
                .document(routeId)
                .get();
    }

    // ========================================
    // DRIVER OPERATIONS
    // ========================================

    /**
     * Get driver by user ID
     */
    public Task<DocumentSnapshot> getDriver(String driverId) {
        return db.collection(COLLECTION_DRIVERS)
                .document(driverId)
                .get();
    }

    /**
     * Update driver status
     */
    public Task<Void> updateDriverStatus(String driverId, Driver.DriverStatus status) {
        return db.collection(COLLECTION_DRIVERS)
                .document(driverId)
                .update("status", status.getValue());
    }

    /**
     * Update driver shift
     */
    public Task<Void> updateDriverShift(String driverId, boolean onShift, Date shiftTime) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("onShift", onShift);
        if (onShift) {
            updates.put("shiftStartTime", shiftTime);
            updates.put("status", Driver.DriverStatus.ON_DUTY.getValue());
        } else {
            updates.put("shiftEndTime", shiftTime);
            updates.put("status", Driver.DriverStatus.OFF_DUTY.getValue());
        }

        return db.collection(COLLECTION_DRIVERS)
                .document(driverId)
                .update(updates);
    }

    /**
     * Increment driver trip count
     */
    public Task<Void> incrementDriverTrips(String driverId) {
        return db.collection(COLLECTION_DRIVERS)
                .document(driverId)
                .get()
                .continueWithTask(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Driver driver = task.getResult().toObject(Driver.class);
                        if (driver != null) {
                            driver.incrementTripCount();
                            return db.collection(COLLECTION_DRIVERS)
                                    .document(driverId)
                                    .update("totalTrips", driver.getTotalTrips());
                        }
                    }
                    return null;
                });
    }

    // ========================================
    // REPORT OPERATIONS
    // ========================================

    /**
     * Submit a report
     */
    public Task<DocumentReference> submitReport(Report report) {
        return db.collection(COLLECTION_REPORTS)
                .add(report);
    }

    /**
     * Get pending reports
     */
    public Task<QuerySnapshot> getPendingReports() {
        return db.collection(COLLECTION_REPORTS)
                .whereEqualTo("status", Report.ReportStatus.PENDING.getValue())
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get();
    }

    /**
     * Get reports for a shuttle
     */
    public Task<QuerySnapshot> getReportsForShuttle(String shuttleId) {
        return db.collection(COLLECTION_REPORTS)
                .whereEqualTo("shuttleId", shuttleId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(20)
                .get();
    }

    /**
     * Update report status
     */
    public Task<Void> updateReportStatus(String reportId, Report.ReportStatus status, String response) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status.getValue());
        if (response != null) {
            updates.put("adminResponse", response);
        }
        if (status == Report.ReportStatus.RESOLVED || status == Report.ReportStatus.DISMISSED) {
            updates.put("resolvedAt", new Date());
        }

        return db.collection(COLLECTION_REPORTS)
                .document(reportId)
                .update(updates);
    }

    // ========================================
    // UTILITY METHODS
    // ========================================

    /**
     * Batch update multiple documents
     */
    public void batchUpdate(Map<DocumentReference, Map<String, Object>> updates) {
        com.google.firebase.firestore.WriteBatch batch = db.batch();

        for (Map.Entry<DocumentReference, Map<String, Object>> entry : updates.entrySet()) {
            batch.update(entry.getKey(), entry.getValue());
        }

        batch.commit();
    }

    /**
     * Delete old reports (older than 30 days)
     */
    public Task<Void> cleanupOldReports() {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.add(java.util.Calendar.DAY_OF_MONTH, -30);
        Date thirtyDaysAgo = calendar.getTime();

        return db.collection(COLLECTION_REPORTS)
                .whereLessThan("createdAt", thirtyDaysAgo)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        com.google.firebase.firestore.WriteBatch batch = db.batch();
                        for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                            batch.delete(doc.getReference());
                        }
                        batch.commit();
                    }
                    return null;
                });
    }

    /**
     * Get Firebase Firestore instance
     */
    public FirebaseFirestore getDb() {
        return db;
    }
}