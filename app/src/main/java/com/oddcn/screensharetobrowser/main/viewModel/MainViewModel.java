package com.oddcn.screensharetobrowser.main.viewModel;

import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.view.View;
import android.widget.Toast;

import com.oddcn.screensharetobrowser.utils.NetUtil;

/**
 * Created by oddzh on 2017/10/30.
 */

public class MainViewModel {

    private Context context;

    public ObservableInt mode = new ObservableInt();// 0 wifi , 1 hotspot

    public static ObservableField<String> localIpText = new ObservableField<>();
    public static ObservableInt webServerPort = new ObservableInt();
    public static ObservableInt wsServerPort = new ObservableInt();

    public static ObservableBoolean isServerRunning = new ObservableBoolean();

    public ObservableInt serverConnCount = new ObservableInt();

    public ObservableBoolean isRecorderRunning = new ObservableBoolean();

    public MainViewModel(Context context) {
        this.context = context;
        mode.set(0);
        webServerPort.set(8123);
        wsServerPort.set(8012);
        serverConnCount.set(0);
    }

    public View.OnClickListener onWifiModeViewClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mode.get() != 0) {
                    Toast.makeText(context, "本机连接WIFI后，将自动切换为该模式", Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    public View.OnClickListener onHotspotModeViewClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mode.get() != 1) {
                    Toast.makeText(context, "本机开启热点后，将自动切换为该模式", Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    public void refreshIp() {
        if (mode.get() == 0) {
            localIpText.set(NetUtil.getWifiIp(context));
        } else if (mode.get() == 1) {
            localIpText.set("192.168.43.1");
        }
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
}