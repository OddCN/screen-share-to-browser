package com.oddcn.screensharetobrowser;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class RecordService extends Service {
    private static final String TAG = "RecordService";
    private MediaProjection mediaProjection;
    private MediaRecorder mediaRecorder;
    private ImageReader imageReader;
    private VirtualDisplay virtualDisplay;
    private WindowManager mWindowManager;
    private boolean running;
    private int width;
    private int height;
    private int dpi;
    FileOutputStream fos = null;
    Bitmap bitmap = null;
    Image img = null;

    private ScreenHandler screenHandler;
    private ExecutorService executorService;

    private class ScreenHandler extends Handler {
        public ScreenHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new RecordBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        HandlerThread serviceThread = new HandlerThread("service_thread",
                android.os.Process.THREAD_PRIORITY_BACKGROUND);
        serviceThread.start();
        running = false;
        mediaRecorder = new MediaRecorder();

        executorService = Executors.newCachedThreadPool();
        HandlerThread handlerThread = new HandlerThread("Screen Record");
        handlerThread.start();
        screenHandler = new ScreenHandler(handlerThread.getLooper());

        //get the size of the window
        mWindowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        width = mWindowManager.getDefaultDisplay().getWidth() + 40;
        height = mWindowManager.getDefaultDisplay().getHeight();
        //height = 2300;
        Log.i(TAG, "onCreate: w is " + width + " h is " + height);
        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2);
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        stopRecord();
    }

    public void setMediaProject(MediaProjection project) {
        mediaProjection = project;
    }

    public boolean isRunning() {
        return running;
    }

    public void setConfig(int width, int height, int dpi) {
        this.width = width;
        this.height = height;
        this.dpi = dpi;
    }

    public boolean startRecord() {
        if (mediaProjection == null || running) {
            return false;
        }
        createVirtualDisplayForImageReader();
        running = true;
        return true;
    }

    public boolean stopRecord() {
        if (!running) {
            return false;
        }
        running = false;
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            executorService = null;
        }
        if (virtualDisplay != null) {
            virtualDisplay.release();
            virtualDisplay = null;
        }
        if (mediaProjection != null) {
            mediaProjection.stop();
            mediaProjection = null;
        }

        if (imageReader != null)
            imageReader.close();
        return true;
    }

    private void createVirtualDisplayForImageReader() {

        virtualDisplay = mediaProjection.createVirtualDisplay("MainScreen", width, height, dpi
                , DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC, imageReader.getSurface()
                , null, screenHandler);
        imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader imageReader) {
                try {
                    img = imageReader.acquireLatestImage();
                    if (img != null) {
                        Image.Plane[] planes = img.getPlanes();
                        if (planes[0].getBuffer() == null) {
                            return;
                        }
                        int width = img.getWidth();
                        int height = img.getHeight();
                        final ByteBuffer buffer = planes[0].getBuffer();
                        int pixelStride = planes[0].getPixelStride();
                        int rowStride = planes[0].getRowStride();
                        int rowPadding = rowStride - pixelStride * width;
                        Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height,
                                Bitmap.Config.ARGB_8888);
                        bitmap.copyPixelsFromBuffer(buffer);
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        int options_ = 70;//压缩分辨率,比如取值为30，那么压缩了30%
                        bitmap.compress(Bitmap.CompressFormat.JPEG, options_, byteArrayOutputStream);

                        MyWebSocketStreamWork myWebSocketStreamWork =
                                new MyWebSocketStreamWork(byteArrayOutputStream);
                        executorService.execute(myWebSocketStreamWork);
                        //new Thread(socketStreamWork).start();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (null != fos) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (null != bitmap) {
                        bitmap.recycle();
                    }
                    if (null != img) {
                        img.close();
                    }

                }
            }
        }, screenHandler);
    }

    public String getsaveDirectory() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String rootDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "ScreenRecord" + "/";

            File file = new File(rootDir);
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    return null;
                }
            }

            Toast.makeText(getApplicationContext(), rootDir, Toast.LENGTH_SHORT).show();

            return rootDir;
        } else {
            return null;
        }
    }

    public class RecordBinder extends Binder {
        public RecordService getRecordService() {
            return RecordService.this;
        }
    }
}