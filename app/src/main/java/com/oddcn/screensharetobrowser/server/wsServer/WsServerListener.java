package com.oddcn.screensharetobrowser.server.wsServer;

import java.util.List;

/**
 * Created by OddCN on 2017/11/8.
 */

public interface WsServerListener {
    void onWsServerStatusChanged(boolean isRunning);

    void onWsServerError(int errorType);

    void onWsServerConnChanged(List<String> connList);
}
