package com.mi.www.photogallary;

import android.app.Activity;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

public class NotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "receive result:" + getResultCode());
        if (getResultCode() != Activity.RESULT_OK) {
            return;
        }
        int notifyId = intent.getIntExtra(PollService.NOTIFY_ID, 0);
        Notification notification = intent.getParcelableExtra(PollService.NOTIFICATION);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notifyId, notification);
    }
}
