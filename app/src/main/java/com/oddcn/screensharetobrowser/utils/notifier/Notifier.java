package com.oddcn.screensharetobrowser.utils.notifier;

import android.content.Context;

/**
 * Created by oddzh on 2017/10/22.
 */

public class Notifier {

    public static NotifierBuilder from(Context context) {
        return new NotifierBuilder(context);
    }

}
