package com.oddcn.screensharetobrowser.utils.notifier;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import com.oddcn.screensharetobrowser.R;


/**
 * Created by oddzh on 2017/10/22.
 */

public class NotifierBuilder {
    private Context mContext;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder builder;

    NotifierBuilder(Context context) {
        mContext = context;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        builder = new NotificationCompat.Builder(context);
    }

    public Notification getNotification(String titleText, String contentText) {
        builder.setSmallIcon(R.drawable.ic_server_black_24dp)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher))
                .setContentTitle(titleText)
//                .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText))
                .setContentText(contentText)
                .setWhen(System.currentTimeMillis());
        return builder.build();
    }

    public void post(String titleText, String contentText) {
        notificationManager.notify(1, getNotification(titleText, contentText));
    }
}
