package com.oddcn.screensharetobrowser.utils.notifier;

import android.app.NotificationManager;
import android.content.Context;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by oddzh on 2017/10/22.
 */

public class Notifier {

    public static NotifierBuilder from(Context context) {
        return new NotifierBuilder(context);
    }

    public static void cancel(Context context, int id) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(id);
        }
    }
}
