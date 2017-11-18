package com.oddcn.screensharetobrowser.server.wsServer;

import android.util.Log;

import org.java_websocket.WebSocket;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshake;
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

    public static final int ERROR_TYPE_NORMAL = 0;
    public static final int ERROR_TYPE_PORT_IN_USE = 1;
    public static final int ERROR_TYPE_SERVER_CLOSE_FAIL = 2;

    private WsServerListener wsServerListener;

    public void setListener(WsServerListener listener) {
        wsServerListener = listener;
    }

    public WsServer(InetSocketAddress address) {
        super(address);
    }

    public static WsServer init(String host, int port) {
        return new WsServer(new InetSocketAddress(host, port));
    }

    public void stopWithException() {
        try {
            this.stop();
            running = false;
            wsServerListener.onWsServerStatusChanged(running);
        } catch (IOException e) {
            e.printStackTrace();
            wsServerListener.onWsServerError(ERROR_TYPE_SERVER_CLOSE_FAIL);//关闭服务失败
        } catch (InterruptedException e) {
            e.printStackTrace();
            wsServerListener.onWsServerError(ERROR_TYPE_SERVER_CLOSE_FAIL);//关闭服务失败
        }
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        String connIp = conn.getRemoteSocketAddress().getAddress().toString().replace("/", "");
        connList.add(connIp);
        wsServerListener.onWsServerConnChanged(connList);
        Log.d(TAG, "onOpen: // " + connIp + " //Opened connection number  " + connList.size());
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

    private boolean running = false;

    public boolean isRunning() {
        return running;
    }

    private List<String> connList = new ArrayList<>();

    @Override
    public void onError(WebSocket conn, Exception ex) {
        Log.d(TAG, "onError: " + ex.getMessage());
        ex.printStackTrace();
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("Address already in use")) {
                Log.d(TAG, "ws server: 端口已被占用");
                wsServerListener.onWsServerError(ERROR_TYPE_PORT_IN_USE);//服务启动失败，端口已被占用，请更换端口
                return;
            }
        }
        wsServerListener.onWsServerError(ERROR_TYPE_NORMAL);
    }

    @Override
    public void onClosing(WebSocket conn, int code, String reason, boolean remote) {
        super.onClosing(conn, code, reason, remote);
        String connIp = conn.getRemoteSocketAddress().getAddress().toString().replace("/", "");
        for (String ip : connList) {
            if (ip.equals(connIp)) {
                connList.remove(ip);
                break;
            }
        }
        wsServerListener.onWsServerConnChanged(connList);
        Log.d(TAG, "onClosing: // " + connIp + " //Opened connection number  " + connList.size());
    }

    @Override
    public void onStart() {
        running = true;
        wsServerListener.onWsServerStatusChanged(running);//服务启动成功
        Log.d(TAG, "onStart: ");
    }

}
