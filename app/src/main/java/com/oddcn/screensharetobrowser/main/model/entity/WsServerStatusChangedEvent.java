package com.oddcn.screensharetobrowser.main.model.entity;

import java.util.List;

/**
 * Created by oddzh on 2017/11/1.
 */

public class WsServerStatusChangedEvent {

    public boolean isServerRunning;
    public String msg;
    public List<String> connList;

    public WsServerStatusChangedEvent(boolean isServerRunning, String msg, List<String> connList) {
        this.isServerRunning = isServerRunning;
        this.msg = msg;
        this.connList = connList;
    }
}
