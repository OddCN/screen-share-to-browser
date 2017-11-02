package com.oddcn.screensharetobrowser.utils.notifier;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import com.oddcn.screensharetobrowser.R;
import com.oddcn.screensharetobrowser.main.view.MainActivity;


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

        builder.setSmallIcon(R.drawable.ic_server_black_24dp)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher))
                .setWhen(System.currentTimeMillis());
    }

    public NotifierBuilder setTitle(String title) {
        builder.setContentTitle(title);
        return this;
    }

    public NotifierBuilder setText(String text) {
        builder.setContentText(text);
        return this;
    }

    public NotifierBuilder setBigText(String text) {
        builder.setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text));
        return this;
    }

    public NotifierBuilder setActivityClass(Class activityClass) {
        Intent activityIntent = new Intent(mContext, activityClass);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        return this;
    }

    public Notification build() {
        return builder.build();
    }

    public void post(int id) {
        notificationManager.notify(id, build());
    }
}
