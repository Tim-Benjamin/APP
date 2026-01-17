package com.example.campusride.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class representing a Shuttle Route
 */
public class Route {

    // Route identification
    private String routeId;
    private String routeName;
    private String description;

    // Route details
    private List<String> stopIds;
    private List<String> stopNames;
    private int frequencyMinutes;

    // Operating hours
    private String startTime;
    private String endTime;
    private boolean weekdayOnly;

    // Status
    private boolean isActive;
    private String color; // Hex color for map display

    /**
     * Default constructor required for Firebase
     */
    public Route() {
        this.stopIds = new ArrayList<>();
        this.stopNames = new ArrayList<>();
        this.isActive = true;
        this.frequencyMinutes = 15;
    }

    /**
     * Constructor with essential fields
     */
    public Route(String routeId, String routeName, int frequencyMinutes) {
        this();
        this.routeId = routeId;
        this.routeName = routeName;
        this.frequencyMinutes = frequencyMinutes;
    }

    // Getters and Setters

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getStopIds() {
        return stopIds;
    }

    public void setStopIds(List<String> stopIds) {
        this.stopIds = stopIds;
    }

    public List<String> getStopNames() {
        return stopNames;
    }

    public void setStopNames(List<String> stopNames) {
        this.stopNames = stopNames;
    }

    public int getFrequencyMinutes() {
        return frequencyMinutes;
    }

    public void setFrequencyMinutes(int frequencyMinutes) {
        this.frequencyMinutes = frequencyMinutes;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public boolean isWeekdayOnly() {
        return weekdayOnly;
    }

    public void setWeekdayOnly(boolean weekdayOnly) {
        this.weekdayOnly = weekdayOnly;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    /**
     * Helper methods
     */

    public void addStop(String stopId, String stopName) {
        if (stopIds == null) stopIds = new ArrayList<>();
        if (stopNames == null) stopNames = new ArrayList<>();

        if (!stopIds.contains(stopId)) {
            stopIds.add(stopId);
            stopNames.add(stopName);
        }
    }

    public void removeStop(String stopId) {
        if (stopIds == null) return;

        int index = stopIds.indexOf(stopId);
        if (index >= 0) {
            stopIds.remove(index);
            if (stopNames != null && index < stopNames.size()) {
                stopNames.remove(index);
            }
        }
    }

    public boolean hasStop(String stopId) {
        return stopIds != null && stopIds.contains(stopId);
    }

    public int getStopCount() {
        return stopIds != null ? stopIds.size() : 0;
    }

    public String getStopsString() {
        if (stopNames == null || stopNames.isEmpty()) {
            return "No stops";
        }
        return String.join(" → ", stopNames);
    }

    public String getFrequencyString() {
        return "Every " + frequencyMinutes + " minutes";
    }

    public String getOperatingHoursString() {
        if (startTime == null || endTime == null) {
            return "Operating hours not set";
        }
        return startTime + " - " + endTime;
    }

    @Override
    public String toString() {
        return "Route{" +
                "routeId='" + routeId + '\'' +
                ", routeName='" + routeName + '\'' +
                ", stops=" + getStopCount() +
                ", frequency=" + frequencyMinutes + " min" +
                '}';
    }
}