package com.example.campusride.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.example.campusride.R;
import com.example.campusride.activities.MainActivity;

import androidx.core.content.ContextCompat;

/**
 * Helper class for managing notifications
 */
public class NotificationHelper {

    private static final String CHANNEL_ID = "campus_ride_notifications";
    private static final String CHANNEL_NAME = "Campus Ride";
    private static final String CHANNEL_DESCRIPTION = "Shuttle arrival notifications";

    private static final String LOCATION_CHANNEL_ID = "location_service";
    private static final String LOCATION_CHANNEL_NAME = "Location Service";

    private final Context context;
    private final NotificationManager notificationManager;

    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannels();
    }

    /**
     * Create notification channels (required for Android 8.0+)
     */
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Main notification channel
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            channel.enableVibration(true);
            channel.setShowBadge(true);
            notificationManager.createNotificationChannel(channel);

            // Location service channel
            NotificationChannel locationChannel = new NotificationChannel(
                    LOCATION_CHANNEL_ID,
                    LOCATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_LOW
            );
            locationChannel.setDescription("Driver location tracking");
            locationChannel.setShowBadge(false);
            notificationManager.createNotificationChannel(locationChannel);
        }
    }

    /**
     * Show shuttle approaching notification
     */
    public void showShuttleApproachingNotification(String shuttleName, int etaMinutes) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        String title = "Shuttle Approaching!";
        String message = shuttleName + " will arrive in " + etaMinutes + " minutes";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setVibrate(new long[]{0, 500, 200, 500})
                .setContentIntent(pendingIntent)
                .setColor(context.getResources().getColor(R.color.ucc_blue, null));

        notificationManager.notify(getNotificationId(shuttleName), builder.build());
    }

    /**
     * Show shuttle arrival notification
     */
    public void showShuttleArrivingNotification(String shuttleName, String stopName) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        String title = "Shuttle Arriving Now!";
        String message = shuttleName + " is arriving at " + stopName;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setVibrate(new long[]{0, 1000, 500, 1000})
                .setContentIntent(pendingIntent)
                .setColor(context.getResources().getColor(R.color.success, null));

        notificationManager.notify(getNotificationId(shuttleName), builder.build());
    }

    /**
     * Show shuttle breakdown notification
     */
    public void showShuttleBreakdownNotification(String shuttleName, String routeName) {
        String title = "Shuttle Issue";
        String message = shuttleName + " on " + routeName + " is experiencing issues";

        showSimpleNotification(title, message, R.drawable.ic_report);
    }

    /**
     * Show shuttle delay notification
     */
    public void showDelayNotification(String shuttleName, int delayMinutes) {
        String title = "Shuttle Delayed";
        String message = shuttleName + " is delayed by " + delayMinutes + " minutes";

        showSimpleNotification(title, message, R.drawable.ic_schedule);
    }

    /**
     * Show simple notification
     */
    private void showSimpleNotification(String title, String message, int iconResId) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(iconResId)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    /**
     * Create foreground service notification for location tracking
     */
    public android.app.Notification createLocationServiceNotification(String driverName, String shuttleName) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(context, LOCATION_CHANNEL_ID)
                .setContentTitle("Campus Ride - Driver Mode")
                .setContentText("Tracking " + shuttleName)
                .setSmallIcon(R.drawable.ic_bus)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    /**
     * Cancel notification for a specific shuttle
     */
    public void cancelShuttleNotification(String shuttleName) {
        notificationManager.cancel(getNotificationId(shuttleName));
    }

    /**
     * Cancel all notifications
     */
    public void cancelAllNotifications() {
        notificationManager.cancelAll();
    }

    /**
     * Generate consistent notification ID from shuttle name
     */
    private int getNotificationId(String shuttleName) {
        return shuttleName.hashCode();
    }

    /**
     * Check if notifications are enabled
     */
    public boolean areNotificationsEnabled() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return notificationManager.areNotificationsEnabled();
        }
        return true;
    }

    /**
     * Get notification channel importance
     */
    public int getChannelImportance() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = notificationManager.getNotificationChannel(CHANNEL_ID);
            if (channel != null) {
                return channel.getImportance();
            }
        }
        return NotificationManager.IMPORTANCE_DEFAULT;
    }
}