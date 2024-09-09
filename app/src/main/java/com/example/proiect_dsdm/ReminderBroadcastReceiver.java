package com.example.proiect_dsdm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class ReminderBroadcastReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "TaskReminderChannel";
    private static final String GROUP_KEY = "com.example.proiect_dsdm.TASK_REMINDERS";
    @Override
    public void onReceive(Context context, Intent intent) {
        int taskId = intent.getIntExtra("TASK_ID", 0);
        String taskTitle = intent.getStringExtra("TASK_TITLE");
        createNotificationChannel(context);

        Intent launchIntent = new Intent(context, MainActivity.class);
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Task Reminder")
                .setContentText(taskTitle)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setGroup(GROUP_KEY)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                notificationManager.notify(taskId, builder.build());


                Notification summaryNotification = new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setContentTitle("Task Reminders")
                        .setSmallIcon(R.drawable.ic_notification)
                        .setGroup(GROUP_KEY)
                        .setGroupSummary(true)
                        .setStyle(new NotificationCompat.InboxStyle()
                                .addLine(taskTitle)
                                .setBigContentTitle("Task Reminders")
                                .setSummaryText("You have task reminders"))
                        .build();

                notificationManager.notify(0, summaryNotification);


            } else {
                handleNotificationPermissionNotGranted(context, taskTitle);
            }
        } else {
            notificationManager.notify(taskId, builder.build());
        }
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Task Reminders";
            String description = "Channel for Task Reminder";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void handleNotificationPermissionNotGranted(Context context, String taskTitle) {

        Intent launchIntent = new Intent(context, MainActivity.class);
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "task_reminders")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Task Reminder")
                .setContentText("You have a pending task: " + taskTitle)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat.from(context).notify(1, builder.build());
    }
}
