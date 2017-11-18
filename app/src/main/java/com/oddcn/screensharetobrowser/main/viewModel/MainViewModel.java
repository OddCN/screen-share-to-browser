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

    public static ObservableField<String> localIpText = new ObservableField<>();
    public static ObservableInt webServerPort = new ObservableInt();
    public static ObservableInt wsServerPort = new ObservableInt();

    public ObservableBoolean isServerRunning = new ObservableBoolean();

    public ObservableInt serverConnCount = new ObservableInt();

    public ObservableBoolean isRecorderRunning = new ObservableBoolean();

    public MainViewModel(Context context) {
        this.context = context;
        webServerPort.set(8123);
        wsServerPort.set(8012);
        serverConnCount.set(0);
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
                    return;
                }
                int randomPort = NetUtil.getRandomPort();
                webServerPort.set(randomPort);
                refreshIp();
            }
        };
    }
}