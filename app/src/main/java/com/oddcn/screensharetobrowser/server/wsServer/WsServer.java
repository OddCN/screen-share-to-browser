package com.oddcn.screensharetobrowser.server.wsServer;

import android.util.Log;
import android.widget.Toast;

import com.oddcn.screensharetobrowser.RxBus;
import com.oddcn.screensharetobrowser.main.model.entity.WsServerStatusChangedEvent;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by oddzh on 2017/10/27.
 */

public class WsServer extends WebSocketServer {

    private static final String TAG = "WsServer";

    private int counter = 0;

    private static WsServer wsServer;

    public static void init(String host, int port) {
        wsServer = new WsServer(new InetSocketAddress(host, port));
    }

    private WsServer(InetSocketAddress address) {
        super(address);
    }

    public static WsServer get() {
        if (wsServer == null) {
            wsServer = new WsServer(new InetSocketAddress("0.0.0.0", 8123));
            Log.d(TAG, "No port specified. Defaulting to 8123");
        }
        return wsServer;
    }

    public void runAsync() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                wsServer.run();
            }
        }).start();
    }

    public void stopWithException() {
        try {
            wsServer.stop();
            isRunning = false;
            postEvent("已关闭服务");
        } catch (IOException e) {
            e.printStackTrace();
            postEvent("关闭服务失败");
        } catch (InterruptedException e) {
            e.printStackTrace();
            postEvent("关闭服务失败");
        }
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        counter++;
        String connIp = conn.getRemoteSocketAddress().getAddress().toString().replace("/", "");
        connList.add(connIp);
        postEvent("");
        Log.d(TAG, "onOpen: " + connIp);
        Log.d(TAG, "onOpen: ///////////Opened connection number  " + counter);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        Log.d(TAG, "onClose: ");
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

    private List<String> connList = new ArrayList<>();

    @Override
    public void onError(WebSocket conn, Exception ex) {
        Log.d(TAG, "onError: " + ex.getMessage());
        ex.printStackTrace();
        if (ex.getMessage().contains("Address already in use")) {
            Log.e(TAG, "onError: 端口已被占用");
            postEvent("服务启动失败，端口已被占用，请更换端口");
        }
    }

    @Override
    public void onClosing(WebSocket conn, int code, String reason, boolean remote) {
        super.onClosing(conn, code, reason, remote);
        counter--;
        String connIp = conn.getRemoteSocketAddress().getAddress().toString().replace("/", "");
        for (String ip : connList) {
            if (ip.equals(connIp)) {
                connList.remove(ip);
            }
        }
        postEvent("");
        Log.d(TAG, "onClosing: " + connIp);
        Log.d(TAG, "onClosing: ///////////Opened connection number  " + counter);
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart: ");
        isRunning = true;
        postEvent("服务启动成功");
    }

    private void postEvent(String msg) {
        RxBus.getDefault().post(new WsServerStatusChangedEvent(isRunning, msg, connList));
    }

}
