package com.example.campusride.models;

import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

/**
 * Model class representing a Campus Shuttle
 */
public class Shuttle {

    // Shuttle identification
    private String shuttleId;
    private String shuttleName;
    private String plateNumber;

    // Location data
    private GeoPoint currentLocation;
    private double latitude;
    private double longitude;

    // Route information
    private String currentRoute;
    private String nextStop;

    // Status information
    private ShuttleStatus status;
    private int capacity;
    private int currentPassengers;

    // Driver information
    private String driverId;
    private String driverName;

    // Timestamps
    @ServerTimestamp
    private Date lastUpdated;
    private Date shiftStartTime;

    // Calculated fields (not stored in Firebase)
    private transient double distanceToStop;
    private transient int etaMinutes;

    /**
     * Enum for shuttle status
     */
    public enum ShuttleStatus {
        ACTIVE("active"),
        ON_BREAK("on_break"),
        BREAKDOWN("breakdown"),
        OFFLINE("offline");

        private final String value;

        ShuttleStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static ShuttleStatus fromString(String text) {
            for (ShuttleStatus status : ShuttleStatus.values()) {
                if (status.value.equalsIgnoreCase(text)) {
                    return status;
                }
            }
            return OFFLINE;
        }
    }

    /**
     * Default constructor required for Firebase
     */
    public Shuttle() {
    }

    /**
     * Constructor with essential fields
     */
    public Shuttle(String shuttleId, String shuttleName, String plateNumber, int capacity) {
        this.shuttleId = shuttleId;
        this.shuttleName = shuttleName;
        this.plateNumber = plateNumber;
        this.capacity = capacity;
        this.status = ShuttleStatus.OFFLINE;
        this.currentPassengers = 0;
    }

    // Getters and Setters

    public String getShuttleId() {
        return shuttleId;
    }

    public void setShuttleId(String shuttleId) {
        this.shuttleId = shuttleId;
    }

    public String getShuttleName() {
        return shuttleName;
    }

    public void setShuttleName(String shuttleName) {
        this.shuttleName = shuttleName;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public GeoPoint getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(GeoPoint currentLocation) {
        this.currentLocation = currentLocation;
        if (currentLocation != null) {
            this.latitude = currentLocation.getLatitude();
            this.longitude = currentLocation.getLongitude();
        }
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getCurrentRoute() {
        return currentRoute;
    }

    public void setCurrentRoute(String currentRoute) {
        this.currentRoute = currentRoute;
    }

    public String getNextStop() {
        return nextStop;
    }

    public void setNextStop(String nextStop) {
        this.nextStop = nextStop;
    }

    public ShuttleStatus getStatus() {
        return status;
    }

    public void setStatus(ShuttleStatus status) {
        this.status = status;
    }

    public String getStatusString() {
        return status != null ? status.getValue() : "offline";
    }

    public void setStatusString(String statusString) {
        this.status = ShuttleStatus.fromString(statusString);
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getCurrentPassengers() {
        return currentPassengers;
    }

    public void setCurrentPassengers(int currentPassengers) {
        this.currentPassengers = currentPassengers;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Date getShiftStartTime() {
        return shiftStartTime;
    }

    public void setShiftStartTime(Date shiftStartTime) {
        this.shiftStartTime = shiftStartTime;
    }

    public double getDistanceToStop() {
        return distanceToStop;
    }

    public void setDistanceToStop(double distanceToStop) {
        this.distanceToStop = distanceToStop;
    }

    public int getEtaMinutes() {
        return etaMinutes;
    }

    public void setEtaMinutes(int etaMinutes) {
        this.etaMinutes = etaMinutes;
    }

    /**
     * Helper methods
     */

    public boolean isActive() {
        return status == ShuttleStatus.ACTIVE;
    }

    public boolean isAvailable() {
        return status == ShuttleStatus.ACTIVE || status == ShuttleStatus.ON_BREAK;
    }

    public int getAvailableSeats() {
        return capacity - currentPassengers;
    }

    public double getOccupancyPercentage() {
        if (capacity == 0) return 0;
        return (currentPassengers * 100.0) / capacity;
    }

    public String getCapacityString() {
        return currentPassengers + "/" + capacity;
    }

    @Override
    public String toString() {
        return "Shuttle{" +
                "shuttleId='" + shuttleId + '\'' +
                ", shuttleName='" + shuttleName + '\'' +
                ", status=" + status +
                ", route='" + currentRoute + '\'' +
                ", passengers=" + currentPassengers + "/" + capacity +
                '}';
    }
}