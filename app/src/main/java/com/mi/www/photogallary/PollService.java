package com.mi.www.photogallary;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;
import java.util.concurrent.TimeUnit;


public class PollService extends IntentService {
   private static final String TAG = "PollService";
   private static final long POLL_INTERVAL_MS = TimeUnit.MINUTES.toMillis(1);
   public static final String ACTION_SHOW_NOTIFICATION = "com.mi.www.photogallary.SHOW_NOTIFICATION";
   public static final String PERM_PRIVATE = "com.mi.www.photogallary.PRIVATE";
   public static final String NOTIFY_ID = "NOTIFY_ID";
   public static final String NOTIFICATION = "NOTIFICATION";

   public static Intent newIntent(Context context){
       return new Intent(context, PollService.class);
   }

    public PollService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (!isNetworkAvailableAndConnected()) {
            return;
        }
        String query = QueryPreferences.getStoreQuery(this);
        String lastResultId = QueryPreferences.getPrefLastResultId(this);//60秒
        List<GalleryItem> items;
        if(TextUtils.isEmpty(query)){
            items = new FlickrFetchr().fetchRecentPhotos();
        }else {
            items = new FlickrFetchr().searchPhotos(query);
        }
        if(items.size() == 0){
            return;
        }
        String resultId = items.get(0).getId();
        if (resultId.equals(lastResultId)) {
            Log.e(TAG, "old result"+ resultId);
        } else {
            Log.e(TAG, "new result"+ resultId);
            Resources resources = getResources();
            Intent i = PhotoGallaryActivity.newIntent(this);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, i, 0);

            Notification notification = new NotificationCompat.Builder(this)
                    .setTicker(resources.getString(R.string.new_pictures_title))
                    .setSmallIcon(android.R.drawable.ic_menu_report_image)
                    .setContentTitle(resources.getString(R.string.new_pictures_title))
                    .setContentText(resources.getString(R.string.new_picture_text))
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)//点击后消息会从抽屉中删除
                    .build();

            //发送带权限的广播，有这个权限的应用才可以接收到此广播
//            sendBroadcast(new Intent(ACTION_SHOW_NOTIFICATION), PERM_PRIVATE);
            showBackgroundNotification(0, notification);
        }
        QueryPreferences.setLastResultId(this, resultId);
    }

    /**
     * 发送有序广播，
     * @param notifyId 通知的标识id
     * @param notification  传给receiver的通知实例
     *    发送有序广播，VisibleFragment里面动态注册的广播先接收到，就返回setResultCode(Activity.RESULT_CANCELED)，
     *    NotificationReceiver最后收到广播，在里面判断resultcode，为OK就发送通知，否则不发送通知。
     */
    private void showBackgroundNotification(int notifyId, Notification notification) {
        Intent intent = new Intent(ACTION_SHOW_NOTIFICATION);
        intent.putExtra(NOTIFY_ID, notifyId);
        intent.putExtra(NOTIFICATION, notification);
        sendOrderedBroadcast(intent, PERM_PRIVATE, null, null,
                Activity.RESULT_OK, null, null);
        //intent, 权限，resultReceiver，支持resultReceiver运行的handler，结果代码初始值
   }

    /**
     * 使用alarmmanager在间隔一段时间后再次运行服务，测试：按back键后退可以，杀死进程不行
     * @param context
     * @param isOn
     */
    public static void setServiceAlarm(Context context, boolean isOn){
       Intent i = PollService.newIntent(context);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, i, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (isOn) {
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),
                    POLL_INTERVAL_MS, pendingIntent);
        } else {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
        //存储alarm的状态让receiver使用
        QueryPreferences.setAlarmOn(context, isOn);
    }

    /**
     * 通过检查pendingIntent是否存在来确认定时器是否激活
     * @param context
     * @return
     */
    public static boolean isServiceAlarmOn(Context context){
        Intent i = PollService.newIntent(context);
        PendingIntent pi = PendingIntent.getService(context, 0, i,
                PendingIntent.FLAG_NO_CREATE);
        return pi != null;
    }


    /**
     * 判断网络是否可用并且是否连接
     * @return
     */
    private boolean isNetworkAvailableAndConnected(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        boolean isNetworkAvailable = cm.getActiveNetworkInfo() != null;
        boolean isNetworkConnected = isNetworkAvailable &&
                cm.getActiveNetworkInfo().isConnected();
        return isNetworkConnected;
    }


}
