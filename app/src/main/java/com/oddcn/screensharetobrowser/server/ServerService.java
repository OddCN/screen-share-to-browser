package com.oddcn.screensharetobrowser.server;

import android.app.Service;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.oddcn.screensharetobrowser.RxBus;
import com.oddcn.screensharetobrowser.main.view.MainActivity;
import com.oddcn.screensharetobrowser.main.viewModel.MainViewModel;
import com.oddcn.screensharetobrowser.server.transcodeServer.Transcoder;
import com.oddcn.screensharetobrowser.server.webServer.WebServer;
import com.oddcn.screensharetobrowser.server.wsServer.WsServer;
import com.oddcn.screensharetobrowser.server.wsServer.WsServerListener;
import com.oddcn.screensharetobrowser.utils.NetUtil;
import com.oddcn.screensharetobrowser.utils.notifier.Notifier;
import com.yanzhenjie.andserver.Server;

import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by oddzh on 2017/11/1.
 */

public class ServerService extends Service {
    private static final String TAG = "ServerService";

    private WsServer wsServer;

    private Server webServer;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private AssetManager mAssetManager;

    private ServerServiceListener serverServiceListener;

    public void setListener(ServerServiceListener listener) {
        serverServiceListener = listener;
    }

    public void removeListener() {
        serverServiceListener = null;
    }

    public void makeForeground() {
        startForeground(
                1,
                Notifier.from(this)
                        .setTitle("屏幕分享服务")
                        .setActivityClass(MainActivity.class)
                        .build()
        );
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Disposable disposable =
                RxBus.getDefault()
                        .toObservable(String.class)
                        .subscribeOn(Schedulers.io())
                        .doOnNext(new Consumer<String>() {
                            @Override
                            public void accept(String s) throws Exception {
                                wsServer.broadcast(s);
                            }
                        })
                        .subscribe();
        compositeDisposable.add(disposable);

        mAssetManager = getAssets();
    }

    private void createWebServer() {
        webServer = WebServer.init(mAssetManager, MainViewModel.webServerPort.get(), new Server.Listener() {
            @Override
            public void onStarted() {
                Log.d(TAG, "web server onStarted: ");
                if (serverServiceListener != null) {
                    serverServiceListener.onServerStatusChanged(true);
                    makeForeground();
                }
            }

            @Override
            public void onStopped() {
                Log.d(TAG, "web server onStopped: ");
                if (serverServiceListener != null) {
                    serverServiceListener.onServerStatusChanged(false);
                }
            }

            @Override
            public void onError(Exception e) {
                Log.d(TAG, "web server onError: ");
                e.printStackTrace();
                if (e.getMessage() != null) {
                    if (e.getMessage().contains("Address already in use")) {
                        int randomPort = NetUtil.getRandomPort();
                        MainViewModel.webServerPort.set(randomPort);
                        createWebServer();
                        Log.e(TAG, "onWebServerError: already change random port " + randomPort);
                        webServer.start();
                        if (serverServiceListener != null) {
                            serverServiceListener.onWebServerError(WebServer.ERROR_TYPE_PORT_IN_USE);
                        }
                        return;
                    }
                }
                if (serverServiceListener != null) {
                    serverServiceListener.onWebServerError(WebServer.ERROR_TYPE_NORMAL);
                }
            }
        });
    }

    private void createWsServer() {
        wsServer = WsServer.init("0.0.0.0", MainViewModel.wsServerPort.get());
        wsServer.setListener(new WsServerListener() {
            @Override
            public void onWsServerStatusChanged(boolean isRunning) {
            }

            @Override
            public void onWsServerError(int errorType) {
                if (errorType == WsServer.ERROR_TYPE_PORT_IN_USE) {
                    int randomPort = NetUtil.getRandomPort();
                    MainViewModel.wsServerPort.set(randomPort);
                    createWsServer();
                    Log.e(TAG, "onWsServerError: already change random port " + randomPort);
                    wsServer.start();
                    return;
                }
                if (serverServiceListener != null) {
                    serverServiceListener.onWsServerError(errorType);
                }
            }

            @Override
            public void onWsServerConnChanged(List<String> connList) {
                if (serverServiceListener != null) {
                    serverServiceListener.onWsServerConnChanged(connList);
                }
            }
        });
    }

    public boolean isRunning() {
        return webServer != null && webServer.isRunning();
    }

    public void startServer() {
        createWebServer();
        webServer.start();
        createWsServer();
        wsServer.start();
        Transcoder transcoder = new Transcoder();
    }

    public void stopServer() {
        if (webServer != null) {
            webServer.stop();
        }
        if (wsServer != null) {
            wsServer.stopWithException();
        }
        stopForeground(true);
    }

    @Override
    public void onDestroy() {
        stopServer();
        compositeDisposable.dispose();
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