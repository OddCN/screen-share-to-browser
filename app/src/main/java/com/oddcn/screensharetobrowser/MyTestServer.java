package com.oddcn.screensharetobrowser;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by oddzh on 2017/10/26.
 */

public class MyTestServer {

    private static final String TAG = "MyTestServer";

    private static MyTestServer instance = new MyTestServer();

    public static MyTestServer getInstance() {
        return instance;
    }

    ServerSocket server;
    Socket client;

    Matcher match;

    public void start(int port) throws IOException, NoSuchAlgorithmException {
        server = new ServerSocket(port);
        Log.d(TAG, "start: MyTestServer has started on 127.0.0.1:" + port);
        client = server.accept();
        Log.d(TAG, "start: A client connected.");

        InputStream in = client.getInputStream();
        OutputStream out = client.getOutputStream();

        //translate bytes of request to string
        String data = new Scanner(in, "UTF-8").useDelimiter("\\r\\n\\r\\n").next();

        Log.d(TAG, "start: " + data);

        Matcher get = Pattern.compile("^GET").matcher(data);
        if (get.find()) {
            Log.d(TAG, "start: get " + get);
            match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data);
            Log.d(TAG, "start: match " + match);
            match.find();
            byte[] response = ("HTTP/1.1 101 Switching Protocols\r\n"
                    + "Connection: Upgrade\r\n"
                    + "Upgrade: websocket\r\n"
                    + "Sec-WebSocket-Accept: "
                    + android.util.Base64.encodeToString(
                    MessageDigest
                            .getInstance("SHA-1")
                            .digest((match.group(1) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11")
                                    .getBytes("UTF-8")), 16)
                    + "\r\n")
                    .getBytes("UTF-8");

            out.write(response, 0, response.length);
        } else {
            Log.d(TAG, "start: get not find");
        }
    }

    public void push(String msg) throws IOException, NoSuchAlgorithmException {
        OutputStream out = client.getOutputStream();

        byte[] response = ("HTTP/1.1 101 Switching Protocols\r\n"
                + "Connection: Upgrade\r\n"
                + "Upgrade: websocket\r\n"
                + "Sec-WebSocket-Accept: "
                + android.util.Base64.encodeToString(
                MessageDigest
                        .getInstance("SHA-1")
                        .digest((match.group(1) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11")
                                .getBytes("UTF-8")), 16)
                + "\r\n"
                + msg
                + "\r\n")
                .getBytes("UTF-8");

        out.write(response, 0, response.length);
    }
}