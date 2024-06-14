package com.masternam.ptitmanagestudentscore.schedule;

import android.app.AlarmManager;
//import android.app.Notification;
//import android.app.NotificationChannel;
//import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
//import android.os.Build;

//import androidx.core.app.NotificationCompat;

import java.util.Calendar;

public class NotificationScheduler {

    public static void scheduleNotifications(
            Context context,
            Calendar startDate,
            int hourOfDay,
            int minute,
            int second,
            String message) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Tạo Intent để bắn khi thông báo được nhấp vào
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setAction("notification");
        intent.putExtra("message", message);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Thiết lập thông báo vào lúc 8:00:00 mỗi ngày trong 7 ngày
        Calendar calendar = (Calendar) startDate.clone();
//        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);

        for (int i = 0; i < 7; i++) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
//            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000, pendingIntent);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    public static void sendNotiSingleAtNow(Context context, String message) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setAction("notification");
        intent.putExtra("message", message);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000, pendingIntent);
    }
}

