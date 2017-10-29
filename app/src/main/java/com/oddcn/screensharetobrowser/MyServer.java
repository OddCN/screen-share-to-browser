package com.oddcn.screensharetobrowser;

import android.util.Log;

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
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
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
        Log.d(TAG, "onClose: ");
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        Log.d(TAG, "onMessage: " + message);
        conn.send("java-WebSocket is  coooool");
    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {
        Log.d(TAG, "onMessage: buffer");
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        Log.d(TAG, "onError: " + ex.getMessage());
        ex.printStackTrace();
        if (ex.getMessage().contains("Address already in use")) {
            Log.e(TAG, "onError: 端口已被占用");
        }
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart: ");
    }

}
