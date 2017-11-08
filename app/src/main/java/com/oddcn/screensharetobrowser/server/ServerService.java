package com.oddcn.screensharetobrowser.server;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.oddcn.screensharetobrowser.main.view.MainActivity;
import com.oddcn.screensharetobrowser.main.viewModel.MainViewModel;
import com.oddcn.screensharetobrowser.server.wsServer.WsServer;
import com.oddcn.screensharetobrowser.server.wsServer.WsServerListener;
import com.oddcn.screensharetobrowser.utils.NetUtil;
import com.oddcn.screensharetobrowser.utils.notifier.Notifier;

import java.net.InetSocketAddress;
import java.util.List;


/**
 * Created by oddzh on 2017/11/1.
 */

public class ServerService extends Service {

    private ServerServiceListener serverServiceListener;

    public void setListener(ServerServiceListener listener) {
        serverServiceListener = listener;
    }

    private WsServer wsServer;

    public boolean isRunning() {
        return wsServer.isRunning();
    }

    public void startServer() {
        wsServer.runAsync();
    }

    public void stopServer() {
        wsServer.stopWithException();
//        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        notificationManager.cancel(1);
        stopForeground(true);
    }

    public void makeForeground() {
        startForeground(
                1,
                Notifier.from(this)
                        .setTitle("屏幕分享服务")
                        .setText("本机 " + NetUtil.getWifiIp(this) + ":" + MainViewModel.port.get())
                        .setActivityClass(MainActivity.class)
                        .build()
        );
    }

    @Override
    public void onCreate() {
        super.onCreate();
        WsServer.init("0.0.0.0", MainViewModel.port.get());
        wsServer = WsServer.get();
        wsServer.setListener(new WsServerListener() {
            @Override
            public void onWsServerStatusChanged(boolean isRunning) {
                if (isRunning) {
                    makeForeground();
                }
                serverServiceListener.onServerStatusChanged(isRunning);
            }

            @Override
            public void onWsServerError(int errorType) {
                serverServiceListener.onWsServerError(errorType);
            }

            @Override
            public void onWsServerConnChanged(List<String> connList) {
                serverServiceListener.onWsServerConnChanged(connList);
            }
        });
    }

    @Override
    public void onDestroy() {
        stopServer();
        super.onDestroy();
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
