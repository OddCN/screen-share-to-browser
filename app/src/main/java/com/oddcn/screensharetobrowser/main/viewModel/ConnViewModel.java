package com.oddcn.screensharetobrowser.main.viewModel;

import android.databinding.ObservableField;

/**
 * Created by oddzh on 2017/11/1.
 */

public class ConnViewModel {
    public ObservableField<String> connIp = new ObservableField<>();

    public ConnViewModel(String connIp) {
        this.connIp.set(connIp);
    }
}
