package com.oddcn.screensharetobrowser.server.wsServer;

import android.util.Log;

import com.oddcn.screensharetobrowser.RxBus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


/**
 * Created by kason_zhang on 2/25/2017.
 * Team:TrendMicro VMI
 */

public class MyWebSocketStreamWork implements Runnable {
    private static final String TAG = "MyWebSocketStreamWork";
    private ByteArrayOutputStream byteArrayOutputStream;

    public MyWebSocketStreamWork(ByteArrayOutputStream byteArrayOutputStream) {
        this.byteArrayOutputStream = byteArrayOutputStream;
    }

    @Override
    public void run() {
        try {
            byte[] b = byteArrayOutputStream.toByteArray();
            String base64Str = org.java_websocket.util.Base64.encodeBytes(b);

            RxBus.getDefault().post(base64Str);

            byteArrayOutputStream.flush();
            byteArrayOutputStream.close();
        } catch (IOException e) {
            Log.i(TAG, "Socket Stream work run exception");
            e.printStackTrace();
        }
    }
}
