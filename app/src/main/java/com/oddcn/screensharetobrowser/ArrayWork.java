package com.oddcn.screensharetobrowser;

/**
 * Created by oddzh on 2017/10/29.
 */

public class ArrayWork implements Runnable {
    private byte[] array;

    public ArrayWork(byte[] array) {
        this.array = array;
    }


    @Override
    public void run() {
        MyServer.get().broadcast(array);
    }
}
