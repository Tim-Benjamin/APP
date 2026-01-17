package com.example.campusride.services;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.example.campusride.utils.NotificationHelper;
import java.util.Map;

/**
 * Service for handling Firebase Cloud Messaging notifications
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCMService";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "Message received from: " + remoteMessage.getFrom());

        // Check if message contains data payload
        if (!remoteMessage.getData().isEmpty()) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            handleDataMessage(remoteMessage.getData());
        }

        // Check if message contains notification payload
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            handleNotificationMessage(remoteMessage.getNotification());
        }
    }

    /**
     * Handle data messages
     */
    private void handleDataMessage(Map<String, String> data) {
        String type = data.get("type");

        if (type == null) {
            return;
        }

        NotificationHelper notificationHelper = new NotificationHelper(this);

        switch (type) {
            case "shuttle_approaching":
                handleShuttleApproaching(data, notificationHelper);
                break;

            case "shuttle_arriving":
                handleShuttleArriving(data, notificationHelper);
                break;

            case "shuttle_breakdown":
                handleShuttleBreakdown(data, notificationHelper);
                break;

            case "shuttle_delay":
                handleShuttleDelay(data, notificationHelper);
                break;

            default:
                Log.d(TAG, "Unknown notification type: " + type);
                break;
        }
    }

    /**
     * Handle shuttle approaching notification
     */
    private void handleShuttleApproaching(Map<String, String> data, NotificationHelper helper) {
        String shuttleName = data.get("shuttleName");
        String etaStr = data.get("eta");

        if (shuttleName != null && etaStr != null) {
            try {
                int eta = Integer.parseInt(etaStr);
                helper.showShuttleApproachingNotification(shuttleName, eta);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Invalid ETA value: " + etaStr);
            }
        }
    }

    /**
     * Handle shuttle arriving notification
     */
    private void handleShuttleArriving(Map<String, String> data, NotificationHelper helper) {
        String shuttleName = data.get("shuttleName");
        String stopName = data.get("stopName");

        if (shuttleName != null && stopName != null) {
            helper.showShuttleArrivingNotification(shuttleName, stopName);
        }
    }

    /**
     * Handle shuttle breakdown notification
     */
    private void handleShuttleBreakdown(Map<String, String> data, NotificationHelper helper) {
        String shuttleName = data.get("shuttleName");
        String routeName = data.get("routeName");

        if (shuttleName != null && routeName != null) {
            helper.showShuttleBreakdownNotification(shuttleName, routeName);
        }
    }

    /**
     * Handle shuttle delay notification
     */
    private void handleShuttleDelay(Map<String, String> data, NotificationHelper helper) {
        String shuttleName = data.get("shuttleName");
        String delayStr = data.get("delay");

        if (shuttleName != null && delayStr != null) {
            try {
                int delay = Integer.parseInt(delayStr);
                helper.showDelayNotification(shuttleName, delay);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Invalid delay value: " + delayStr);
            }
        }
    }

    /**
     * Handle notification payload messages
     */
    private void handleNotificationMessage(RemoteMessage.Notification notification) {
        String title = notification.getTitle();
        String body = notification.getBody();

        if (title != null && body != null) {
            NotificationHelper helper = new NotificationHelper(this);
            // Show generic notification
            // helper.showSimpleNotification(title, body, R.drawable.ic_notifications);
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed token: " + token);

        // Send token to server
        sendTokenToServer(token);
    }

    /**
     * Send FCM token to server
     */
    private void sendTokenToServer(String token) {
        // TODO: Implement sending token to Firestore for user
        // This allows sending targeted notifications to specific users
        Log.d(TAG, "Sending token to server: " + token);

        // Example: Save to user document in Firestore
        // FirebaseAuth auth = FirebaseAuth.getInstance();
        // if (auth.getCurrentUser() != null) {
        //     String userId = auth.getCurrentUser().getUid();
        //     FirebaseFirestore.getInstance()
        //         .collection("users")
        //         .document(userId)
        //         .update("fcmToken", token);
        // }
    }
}