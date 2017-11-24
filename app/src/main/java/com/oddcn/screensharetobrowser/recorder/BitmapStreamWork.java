package com.oddcn.screensharetobrowser.recorder;

import android.graphics.Bitmap;
import android.media.Image;

import com.oddcn.screensharetobrowser.RxBus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by OddCN on 2017/11/23.
 */

public class BitmapStreamWork implements Runnable {

    private Bitmap bitmap;

    public BitmapStreamWork(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    @Override
    public void run() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int options_ = 10;
        bitmap.compress(Bitmap.CompressFormat.JPEG, options_, byteArrayOutputStream);

        byte[] b = byteArrayOutputStream.toByteArray();
        String base64Str = org.java_websocket.util.Base64.encodeBytes(b);

        RxBus.getDefault().post(base64Str);

        try {
            byteArrayOutputStream.flush();
            byteArrayOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            bitmap.recycle();
        }
    }
}
