package com.example.campusride.models;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Model class representing a Student/User
 */
public class User {

    // User identification
    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private String studentId;

    // User type
    private UserType userType;

    // Preferences
    private List<String> favoriteStops;
    private String defaultStop;
    private boolean notificationsEnabled;

    // Settings
    private int notificationRadius; // minutes before arrival to notify

    // Timestamps
    @ServerTimestamp
    private Date createdAt;
    private Date lastActive;

    /**
     * Enum for user types
     */
    public enum UserType {
        STUDENT("student"),
        DRIVER("driver"),
        ADMIN("admin");

        private final String value;

        UserType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static UserType fromString(String text) {
            for (UserType type : UserType.values()) {
                if (type.value.equalsIgnoreCase(text)) {
                    return type;
                }
            }
            return STUDENT;
        }
    }

    /**
     * Default constructor required for Firebase
     */
    public User() {
        this.userType = UserType.STUDENT;
        this.favoriteStops = new ArrayList<>();
        this.notificationsEnabled = true;
        this.notificationRadius = 5; // 5 minutes default
    }

    /**
     * Constructor with essential fields
     */
    public User(String userId, String email, String firstName, String lastName) {
        this();
        this.userId = userId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.createdAt = new Date();
    }

    // Getters and Setters

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
        if (firstName == null && lastName == null) return "Anonymous";
        if (firstName == null) return lastName;
        if (lastName == null) return firstName;
        return firstName + " " + lastName;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public String getUserTypeString() {
        return userType != null ? userType.getValue() : "student";
    }

    public void setUserTypeString(String userTypeString) {
        this.userType = UserType.fromString(userTypeString);
    }

    public List<String> getFavoriteStops() {
        return favoriteStops;
    }

    public void setFavoriteStops(List<String> favoriteStops) {
        this.favoriteStops = favoriteStops;
    }

    public String getDefaultStop() {
        return defaultStop;
    }

    public void setDefaultStop(String defaultStop) {
        this.defaultStop = defaultStop;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    public int getNotificationRadius() {
        return notificationRadius;
    }

    public void setNotificationRadius(int notificationRadius) {
        this.notificationRadius = notificationRadius;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getLastActive() {
        return lastActive;
    }

    public void setLastActive(Date lastActive) {
        this.lastActive = lastActive;
    }

    /**
     * Helper methods
     */

    public boolean isStudent() {
        return userType == UserType.STUDENT;
    }

    public boolean isDriver() {
        return userType == UserType.DRIVER;
    }

    public boolean isAdmin() {
        return userType == UserType.ADMIN;
    }

    public void addFavoriteStop(String stopId) {
        if (favoriteStops == null) {
            favoriteStops = new ArrayList<>();
        }
        if (!favoriteStops.contains(stopId)) {
            favoriteStops.add(stopId);
        }
    }

    public void removeFavoriteStop(String stopId) {
        if (favoriteStops != null) {
            favoriteStops.remove(stopId);
        }
    }

    public boolean isFavoriteStop(String stopId) {
        return favoriteStops != null && favoriteStops.contains(stopId);
    }

    public int getFavoriteStopCount() {
        return favoriteStops != null ? favoriteStops.size() : 0;
    }

    public void updateLastActive() {
        this.lastActive = new Date();
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", name='" + getFullName() + '\'' +
                ", email='" + email + '\'' +
                ", userType=" + userType +
                ", favoriteStops=" + (favoriteStops != null ? favoriteStops.size() : 0) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return userId != null && userId.equals(user.userId);
    }

    @Override
    public int hashCode() {
        return userId != null ? userId.hashCode() : 0;
    }
}