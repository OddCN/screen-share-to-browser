package com.oddcn.screensharetobrowser.server;

import java.util.List;

/**
 * Created by OddCN on 2017/11/8.
 */

public interface ServerServiceListener {
    void onServerStatusChanged(boolean isRunning);

    void onWsServerError(int errorType);

    void onWsServerConnChanged(List<String> connList);
}
