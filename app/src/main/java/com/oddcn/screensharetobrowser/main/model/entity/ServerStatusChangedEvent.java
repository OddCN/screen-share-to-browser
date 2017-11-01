package com.oddcn.screensharetobrowser.main.model.entity;

/**
 * Created by oddzh on 2017/11/1.
 */

public class ServerStatusChangedEvent {
    public boolean isServerRunning;
    public String msg;

    public ServerStatusChangedEvent(boolean isServerRunning, String msg) {
        this.isServerRunning = isServerRunning;
        this.msg = msg;
    }
}
