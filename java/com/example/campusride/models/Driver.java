package com.example.campusride.models;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

/**
 * Model class representing a Shuttle Driver
 */
public class Driver {

    // Driver identification
    private String driverId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;

    // Employment information
    private String licenseNumber;
    private Date hireDate;
    private boolean isActive;

    // Current assignment
    private String assignedShuttleId;
    private String assignedShuttleName;
    private DriverStatus status;

    // Shift information
    private boolean onShift;
    private Date shiftStartTime;
    private Date shiftEndTime;

    // Statistics
    private int totalTrips;
    private double totalHours;
    private double rating;

    // Timestamps
    @ServerTimestamp
    private Date lastLogin;
    private Date createdAt;

    /**
     * Enum for driver status
     */
    public enum DriverStatus {
        AVAILABLE("available"),
        ON_DUTY("on_duty"),
        ON_BREAK("on_break"),
        OFF_DUTY("off_duty");

        private final String value;

        DriverStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static DriverStatus fromString(String text) {
            for (DriverStatus status : DriverStatus.values()) {
                if (status.value.equalsIgnoreCase(text)) {
                    return status;
                }
            }
            return OFF_DUTY;
        }
    }

    /**
     * Default constructor required for Firebase
     */
    public Driver() {
        this.isActive = true;
        this.status = DriverStatus.OFF_DUTY;
        this.onShift = false;
        this.totalTrips = 0;
        this.totalHours = 0.0;
        this.rating = 0.0;
    }

    /**
     * Constructor with essential fields
     */
    public Driver(String driverId, String firstName, String lastName, String email) {
        this();
        this.driverId = driverId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.createdAt = new Date();
    }

    // Getters and Setters

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public Date getHireDate() {
        return hireDate;
    }

    public void setHireDate(Date hireDate) {
        this.hireDate = hireDate;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getAssignedShuttleId() {
        return assignedShuttleId;
    }

    public void setAssignedShuttleId(String assignedShuttleId) {
        this.assignedShuttleId = assignedShuttleId;
    }

    public String getAssignedShuttleName() {
        return assignedShuttleName;
    }

    public void setAssignedShuttleName(String assignedShuttleName) {
        this.assignedShuttleName = assignedShuttleName;
    }

    public DriverStatus getStatus() {
        return status;
    }

    public void setStatus(DriverStatus status) {
        this.status = status;
    }

    public String getStatusString() {
        return status != null ? status.getValue() : "off_duty";
    }

    public void setStatusString(String statusString) {
        this.status = DriverStatus.fromString(statusString);
    }

    public boolean isOnShift() {
        return onShift;
    }

    public void setOnShift(boolean onShift) {
        this.onShift = onShift;
    }

    public Date getShiftStartTime() {
        return shiftStartTime;
    }

    public void setShiftStartTime(Date shiftStartTime) {
        this.shiftStartTime = shiftStartTime;
    }

    public Date getShiftEndTime() {
        return shiftEndTime;
    }

    public void setShiftEndTime(Date shiftEndTime) {
        this.shiftEndTime = shiftEndTime;
    }

    public int getTotalTrips() {
        return totalTrips;
    }

    public void setTotalTrips(int totalTrips) {
        this.totalTrips = totalTrips;
    }

    public double getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(double totalHours) {
        this.totalHours = totalHours;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Helper methods
     */

    public boolean hasAssignedShuttle() {
        return assignedShuttleId != null && !assignedShuttleId.isEmpty();
    }

    public void startShift() {
        this.onShift = true;
        this.shiftStartTime = new Date();
        this.status = DriverStatus.ON_DUTY;
    }

    public void endShift() {
        this.onShift = false;
        this.shiftEndTime = new Date();
        this.status = DriverStatus.OFF_DUTY;

        // Calculate and add to total hours
        if (shiftStartTime != null && shiftEndTime != null) {
            long durationMillis = shiftEndTime.getTime() - shiftStartTime.getTime();
            double hours = durationMillis / (1000.0 * 60 * 60);
            this.totalHours += hours;
        }
    }

    public double getCurrentShiftHours() {
        if (!onShift || shiftStartTime == null) return 0.0;

        long durationMillis = new Date().getTime() - shiftStartTime.getTime();
        return durationMillis / (1000.0 * 60 * 60);
    }

    public void incrementTripCount() {
        this.totalTrips++;
    }

    @Override
    public String toString() {
        return "Driver{" +
                "driverId='" + driverId + '\'' +
                ", name='" + getFullName() + '\'' +
                ", status=" + status +
                ", onShift=" + onShift +
                ", assignedShuttle='" + assignedShuttleName + '\'' +
                '}';
    }
}