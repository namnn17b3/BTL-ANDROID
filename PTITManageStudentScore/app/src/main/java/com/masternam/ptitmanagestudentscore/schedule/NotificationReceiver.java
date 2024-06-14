package com.masternam.ptitmanagestudentscore.schedule;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.masternam.ptitmanagestudentscore.R;

import java.util.Date;
//import java.util.UUID;

public class NotificationReceiver extends BroadcastReceiver {
    private final static String CHANNEL_ID = "my_chanel";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("notification")) {
            String message = intent.getStringExtra("message");
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "hello", NotificationManager.IMPORTANCE_DEFAULT);
                channel.setDescription("Mieu ta cho kenh Nam");
                notificationManager.createNotificationChannel(channel);
            }
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("PTIT MANAGE SCORE thông báo")
                .setContentText(message)
                .setSmallIcon(R.mipmap.ptit)
                .setColor(Color.RED)
                .setDefaults(NotificationCompat.DEFAULT_SOUND)
                .setCategory(NotificationCompat.CATEGORY_ALARM);
            notificationManager.notify(getNotificationId(), builder.build());
        }
    }

    public int getNotificationId() {
        return (int) new Date().getTime();
    }
}
