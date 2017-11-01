package com.oddcn.screensharetobrowser.server;

import android.util.Log;

import com.oddcn.screensharetobrowser.RxBus;
import com.oddcn.screensharetobrowser.main.model.entity.ServerStatusChangedEvent;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * Created by oddzh on 2017/10/27.
 */

public class MyServer extends WebSocketServer {

    private static final String TAG = "MyServer";

    private static int counter = 0;

    private static MyServer myServer;

    public static void init(String host, int port) {
        myServer = new MyServer(new InetSocketAddress(host, port));
    }

    private MyServer(InetSocketAddress address) {
        super(address);
    }

    public static MyServer get() {
        if (myServer == null) {
            myServer = new MyServer(new InetSocketAddress("0.0.0.0", 8123));
            Log.d(TAG, "No port specified. Defaulting to 8123");
        }
        return myServer;
    }

    public void runAsync() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                myServer.run();
            }
        }).start();
    }

    public void stopWithException() {
        try {
            myServer.stop();
            isRunning = false;
            RxBus.getDefault().post(new ServerStatusChangedEvent(isRunning,"关闭服务"));
        } catch (IOException e) {
            e.printStackTrace();
            RxBus.getDefault().post(new ServerStatusChangedEvent(isRunning,"关闭服务失败"));
        } catch (InterruptedException e) {
            e.printStackTrace();
            RxBus.getDefault().post(new ServerStatusChangedEvent(isRunning,"关闭服务失败"));
        }
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        counter++;
        Log.d(TAG, "onOpen: " + conn.getRemoteSocketAddress().getAddress());
        Log.d(TAG, "onOpen: ///////////Opened connection number  " + counter);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        counter--;
        Log.d(TAG, "onClose: " + conn.getRemoteSocketAddress().getAddress());
        Log.d(TAG, "onClose: ///////////Opened connection number  " + counter);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        Log.d(TAG, "onMessage: " + message);
    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {
        Log.d(TAG, "onMessage: buffer");
    }

    private boolean isRunning = false;

    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        Log.d(TAG, "onError: " + ex.getMessage());
        ex.printStackTrace();
        isRunning = false;
        ServerStatusChangedEvent event = new ServerStatusChangedEvent(isRunning, "服务启动失败");
        if (ex.getMessage().contains("Address already in use")) {
            Log.e(TAG, "onError: 端口已被占用");
            event.msg = "服务启动失败，端口已被占用，请更换端口";
        }
        RxBus.getDefault().post(event);
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart: ");
        isRunning = true;
        RxBus.getDefault().post(new ServerStatusChangedEvent(isRunning,"服务启动成功"));
    }

}
