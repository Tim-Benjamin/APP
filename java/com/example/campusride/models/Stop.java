package com.example.campusride.models;

import com.google.firebase.firestore.GeoPoint;
import java.util.ArrayList;
import java.util.List;

/**
 * Model class representing a Bus Stop
 */
public class Stop {

    // Stop identification
    private String stopId;
    private String stopName;
    private String description;

    // Location data
    private GeoPoint location;
    private double latitude;
    private double longitude;

    // Stop information
    private List<String> routes;
    private boolean isActive;
    private int orderInRoute;

    // Metadata
    private String landmark;

    /**
     * Default constructor required for Firebase
     */
    public Stop() {
        this.routes = new ArrayList<>();
        this.isActive = true;
    }

    /**
     * Constructor with essential fields
     */
    public Stop(String stopId, String stopName, double latitude, double longitude) {
        this.stopId = stopId;
        this.stopName = stopName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.location = new GeoPoint(latitude, longitude);
        this.routes = new ArrayList<>();
        this.isActive = true;
    }

    // Getters and Setters

    public String getStopId() {
        return stopId;
    }

    public void setStopId(String stopId) {
        this.stopId = stopId;
    }

    public String getStopName() {
        return stopName;
    }

    public void setStopName(String stopName) {
        this.stopName = stopName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
        if (location != null) {
            this.latitude = location.getLatitude();
            this.longitude = location.getLongitude();
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

    public List<String> getRoutes() {
        return routes;
    }

    public void setRoutes(List<String> routes) {
        this.routes = routes;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public int getOrderInRoute() {
        return orderInRoute;
    }

    public void setOrderInRoute(int orderInRoute) {
        this.orderInRoute = orderInRoute;
    }

    public String getLandmark() {
        return landmark;
    }

    public void setLandmark(String landmark) {
        this.landmark = landmark;
    }

    /**
     * Helper methods
     */

    public void addRoute(String routeName) {
        if (routes == null) {
            routes = new ArrayList<>();
        }
        if (!routes.contains(routeName)) {
            routes.add(routeName);
        }
    }

    public void removeRoute(String routeName) {
        if (routes != null) {
            routes.remove(routeName);
        }
    }

    public boolean servesRoute(String routeName) {
        return routes != null && routes.contains(routeName);
    }

    public int getRouteCount() {
        return routes != null ? routes.size() : 0;
    }

    /**
     * Calculate distance to another stop in kilometers
     */
    public double distanceTo(Stop otherStop) {
        if (otherStop == null) return Double.MAX_VALUE;
        return calculateDistance(this.latitude, this.longitude,
                otherStop.latitude, otherStop.longitude);
    }

    /**
     * Calculate distance to a location in kilometers
     */
    public double distanceTo(double lat, double lon) {
        return calculateDistance(this.latitude, this.longitude, lat, lon);
    }

    /**
     * Haversine formula to calculate distance between two points
     */
    private static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS = 6371; // Radius in kilometers

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    @Override
    public String toString() {
        return "Stop{" +
                "stopId='" + stopId + '\'' +
                ", stopName='" + stopName + '\'' +
                ", routes=" + routes +
                ", location=[" + latitude + ", " + longitude + "]" +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stop stop = (Stop) o;
        return stopId != null && stopId.equals(stop.stopId);
    }

    @Override
    public int hashCode() {
        return stopId != null ? stopId.hashCode() : 0;
    }
}