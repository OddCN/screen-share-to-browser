package com.oddcn.screensharetobrowser.server;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.oddcn.screensharetobrowser.server.wsServer.WsServer;
import com.oddcn.screensharetobrowser.utils.NetUtil;
import com.oddcn.screensharetobrowser.utils.notifier.Notifier;

/**
 * Created by oddzh on 2017/11/1.
 */

public class ServerService extends Service {


    public boolean isRunning() {
        return WsServer.get().isRunning();
    }

    public void startServer(String host, int port) {
        WsServer.init(host, port);
        WsServer.get().runAsync();
        startForeground(1, Notifier.from(this).getNotification("屏幕分享服务", "本机 " + NetUtil.getWifiIp(this) + ":" + port));
    }

    public void stopServer() {
        WsServer.get().stopWithException();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new ServerServiceBinder();
    }

    public class ServerServiceBinder extends Binder {
        public ServerService getServerService() {
            return ServerService.this;
        }
    }

}
