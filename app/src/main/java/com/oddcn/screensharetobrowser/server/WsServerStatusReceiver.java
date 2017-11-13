package com.oddcn.screensharetobrowser.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by OddCN on 2017/11/13.
 */

public class WsServerStatusReceiver extends BroadcastReceiver {
    private static final String ACTION = "com.oddcn.screen-share-to-browser.WsServerStatusReceiver";

    private static final String CMD_KEY = "CMD_KEY";

    private static final int CMD_VALUE_STATUS_CHANGED = 1;

    public static void serverStatusChanged(Context context, boolean isRunning) {
        Intent broadcast = new Intent(ACTION);
        broadcast.putExtra(CMD_KEY, CMD_VALUE_STATUS_CHANGED);
        
        context.sendBroadcast(broadcast);
    }

    @Override
    public void onReceive(Context context, Intent intent) {

    }
}
