package com.oddcn.screensharetobrowser.main.model.entity;

/**
 * Created by oddzh on 2017/11/1.
 */

public class RecorderStatusChangedEvent {
    public boolean isRunning;

    public RecorderStatusChangedEvent(boolean isRunning) {
        this.isRunning = isRunning;
    }
}
