package com.kab.dailyselfie;

import java.util.Calendar;
import android.app.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.util.Log;
import android.app.NotificationManager;

public class AlarmManagerBroadcastReceiver extends BroadcastReceiver {


    final public static String ONE_TIME = "onetime";
    final public static Boolean FIRST_RUN = true;
    SharedPreferences mSettings;
    private static final String TAG = "DailySelfie";

     @Override
    public void onReceive(Context context, Intent intent) {

         mSettings = context.getSharedPreferences("APP_PREFERENCES", Context.MODE_PRIVATE);

       if  (!mSettings.getBoolean("FIRST_RUN", true)) {
             PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
             PowerManager.WakeLock wl = pm.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "Daily Selfie");///работает , но проблема с диалоговым окном
             wl.acquire(10000);

             sendNotif16(context);//16
             Log.i("LAB", "onReceive");
         }
         else {
           mSettings.edit().putBoolean("FIRST_RUN", false).commit();
       }
    }

    void sendNotif16(Context context) {

        String msgText = "Time for another Selfie";

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

        long[] i = new long[]{900, 600, 900};

        Intent resultIntent = new Intent();
        resultIntent.setClassName("com.kab.dailyselfie", "com.kab.dailyselfie.MainActivity");

        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

          PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        Notification.Builder builder = new Notification.Builder(context);
        builder.setContentTitle(context.getResources().getString(R.string.app_name))
                .setContentText(msgText)
                .setSmallIcon(R.drawable.ic_menu_camera)
                .setAutoCancel(true)
                .setTicker(msgText)
                .setVibrate(i)
                .setPriority(Notification.PRIORITY_HIGH)
                .setContentIntent(resultPendingIntent);

        Notification notification = new Notification.BigTextStyle(builder)
                .bigText(msgText).build();

        notificationManager.notify(777, notification);
    }

    public void SetAlarm(Context context)
    {
        Calendar calendar = Calendar.getInstance();

        long startTime = calendar.getTimeInMillis();

        Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);
        intent.putExtra(ONE_TIME, Boolean.FALSE);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

       /* am.setRepeating(AlarmManager.RTC_WAKEUP, startTime,
                24*60*60*1000, pi); */ //one time in day
        am.setRepeating(AlarmManager.RTC_WAKEUP, startTime,
                5*60*1000, pi); // 5 min
    }

    public void CancelAlarm(Context context)
    {
        Log.i("LAB","CancelAlarm");
        Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }


}