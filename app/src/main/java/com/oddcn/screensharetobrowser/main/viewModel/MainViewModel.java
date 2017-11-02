package com.oddcn.screensharetobrowser.main.viewModel;

import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.view.View;
import android.widget.Toast;

import com.oddcn.screensharetobrowser.utils.NetUtil;

import java.util.Random;

/**
 * Created by oddzh on 2017/10/30.
 */

public class MainViewModel {

    private Context context;

    public ObservableField<String> localIpText = new ObservableField<>();
    public static ObservableInt port = new ObservableInt();
    private static final int PORT_MIN = 1024;
    private static final int PORT_MAX = 49151;

    public ObservableBoolean isServerRunning = new ObservableBoolean();

    public ObservableInt serverConnCount = new ObservableInt();

    public ObservableBoolean isRecorderRunning = new ObservableBoolean();

    public MainViewModel(Context context) {
        this.context = context;
        port.set(8123);
        isServerRunning.set(false);
        serverConnCount.set(0);
        isRecorderRunning.set(false);
    }

    public void refreshIp() {
        localIpText.set(NetUtil.getWifiIp(context));
    }

    public View.OnClickListener onImgRefreshIpClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshIp();
                Toast.makeText(context, "已刷新本机IP", Toast.LENGTH_SHORT).show();
            }
        };
    }

    public View.OnClickListener onTextViewRandomChangeIpClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isServerRunning.get()) {
                    Toast.makeText(context, "服务运行中，不可更改", Toast.LENGTH_SHORT).show();
                }
                int randomPort = new Random().nextInt(PORT_MAX - PORT_MIN) + PORT_MIN;
                port.set(randomPort);
                refreshIp();
            }
        };
    }
}