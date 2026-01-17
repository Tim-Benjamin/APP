package com.example.campusride.models;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

/**
 * Model class representing an Issue Report
 */
public class Report {

    // Report identification
    private String reportId;
    private String userId;
    private String userName;

    // Report details
    private String shuttleId;
    private String shuttleName;
    private IssueType issueType;
    private String description;

    // Status tracking
    private ReportStatus status;
    private String adminResponse;

    // Timestamps
    @ServerTimestamp
    private Date createdAt;
    private Date resolvedAt;

    /**
     * Enum for issue types
     */
    public enum IssueType {
        DELAY("delay", "Delay"),
        BREAKDOWN("breakdown", "Breakdown"),
        OVERCROWDING("overcrowding", "Overcrowding"),
        MISSED_STOP("missed_stop", "Missed Stop"),
        DRIVER_BEHAVIOR("driver_behavior", "Driver Behavior"),
        CLEANLINESS("cleanliness", "Cleanliness"),
        OTHER("other", "Other");

        private final String value;
        private final String displayName;

        IssueType(String value, String displayName) {
            this.value = value;
            this.displayName = displayName;
        }

        public String getValue() {
            return value;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static IssueType fromString(String text) {
            for (IssueType type : IssueType.values()) {
                if (type.value.equalsIgnoreCase(text)) {
                    return type;
                }
            }
            return OTHER;
        }
    }

    /**
     * Enum for report status
     */
    public enum ReportStatus {
        PENDING("pending", "Pending"),
        IN_PROGRESS("in_progress", "In Progress"),
        RESOLVED("resolved", "Resolved"),
        DISMISSED("dismissed", "Dismissed");

        private final String value;
        private final String displayName;

        ReportStatus(String value, String displayName) {
            this.value = value;
            this.displayName = displayName;
        }

        public String getValue() {
            return value;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static ReportStatus fromString(String text) {
            for (ReportStatus status : ReportStatus.values()) {
                if (status.value.equalsIgnoreCase(text)) {
                    return status;
                }
            }
            return PENDING;
        }
    }

    /**
     * Default constructor required for Firebase
     */
    public Report() {
        this.status = ReportStatus.PENDING;
    }

    /**
     * Constructor with essential fields
     */
    public Report(String userId, String shuttleId, IssueType issueType, String description) {
        this();
        this.userId = userId;
        this.shuttleId = shuttleId;
        this.issueType = issueType;
        this.description = description;
        this.createdAt = new Date();
    }

    // Getters and Setters

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

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

    public IssueType getIssueType() {
        return issueType;
    }

    public void setIssueType(IssueType issueType) {
        this.issueType = issueType;
    }

    public String getIssueTypeString() {
        return issueType != null ? issueType.getValue() : "other";
    }

    public void setIssueTypeString(String issueTypeString) {
        this.issueType = IssueType.fromString(issueTypeString);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    public String getStatusString() {
        return status != null ? status.getValue() : "pending";
    }

    public void setStatusString(String statusString) {
        this.status = ReportStatus.fromString(statusString);
    }

    public String getAdminResponse() {
        return adminResponse;
    }

    public void setAdminResponse(String adminResponse) {
        this.adminResponse = adminResponse;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Date resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    /**
     * Helper methods
     */

    public boolean isPending() {
        return status == ReportStatus.PENDING;
    }

    public boolean isResolved() {
        return status == ReportStatus.RESOLVED;
    }

    public void markAsResolved(String response) {
        this.status = ReportStatus.RESOLVED;
        this.adminResponse = response;
        this.resolvedAt = new Date();
    }

    public void markAsInProgress() {
        this.status = ReportStatus.IN_PROGRESS;
    }

    public void dismiss(String reason) {
        this.status = ReportStatus.DISMISSED;
        this.adminResponse = reason;
        this.resolvedAt = new Date();
    }

    public long getResolutionTimeHours() {
        if (createdAt == null || resolvedAt == null) return 0;
        long durationMillis = resolvedAt.getTime() - createdAt.getTime();
        return durationMillis / (1000 * 60 * 60);
    }

    @Override
    public String toString() {
        return "Report{" +
                "reportId='" + reportId + '\'' +
                ", issueType=" + issueType +
                ", shuttleName='" + shuttleName + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}