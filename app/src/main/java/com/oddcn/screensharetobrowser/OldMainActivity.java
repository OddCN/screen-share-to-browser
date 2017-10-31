package com.oddcn.screensharetobrowser;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Random;

public class OldMainActivity extends AppCompatActivity {
    private static final int RECORD_REQUEST_CODE = 101;

    private int port = 8123;
    private static final int PORT_MIN = 1024;
    private static final int PORT_MAX = 49151;

    private TextView tvIp;

    private MediaProjectionManager projectionManager;
    private MediaProjection mediaProjection;
    private RecordService recordService;
    private Button startBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        projectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        setContentView(R.layout.activity_old_main);

        tvIp = (TextView) findViewById(R.id.tv_ip);
        refreshWifiIp();

        startBtn = (Button) findViewById(R.id.start_record);
        startBtn.setEnabled(false);

        Intent intent = new Intent(this, RecordService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);
    }

    public void changePort(View view) {
        port = new Random().nextInt(PORT_MAX - PORT_MIN) + PORT_MIN;
        refreshWifiIp();
    }

    public void refreshIp(View view) {
        refreshWifiIp();
    }

    public void startServer(View view) {
        MyServer.init("0.0.0.0", port);
        MyServer.get().runAsync();
    }

    public void sendMsg(View view) {
        MyServer.get().broadcast("this is a broadcast");
    }

    public void recordButtonClick(View view) {
        if (recordService.isRunning()) {
            recordService.stopRecord();
            startBtn.setText(R.string.start_record);
        } else {
            Intent captureIntent = projectionManager.createScreenCaptureIntent();
            startActivityForResult(captureIntent, RECORD_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECORD_REQUEST_CODE && resultCode == RESULT_OK) {
            mediaProjection = projectionManager.getMediaProjection(resultCode, data);
            recordService.setMediaProject(mediaProjection);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    recordService.startRecord();
                }
            }).start();
            startBtn.setText(R.string.stop_record);
        }
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            RecordService.RecordBinder binder = (RecordService.RecordBinder) service;
            recordService = binder.getRecordService();
            recordService.setConfig(metrics.widthPixels, metrics.heightPixels, metrics.densityDpi);
            startBtn.setEnabled(true);
            startBtn.setText(recordService.isRunning() ? R.string.stop_record : R.string.start_record);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };

    private void refreshWifiIp() {
        tvIp.setText(Utils.getWifiIp(getApplicationContext()) + ":" + port);
    }

    @Override
    protected void onDestroy() {
        MyServer.get().stopWithException();
        unbindService(connection);
        super.onDestroy();
    }

}
