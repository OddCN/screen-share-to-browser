package com.oddcn.screensharetobrowser.server.transcodeServer;

import com.eclipsesource.v8.NodeJS;
import com.eclipsesource.v8.V8;

/**
 * Created by oddcn on 18-1-9.
 */

public class Transcoder {
    public Transcoder() {
        V8 runtime = V8.createV8Runtime();
        int result = runtime.executeIntegerScript(""
                + "var hello = 'hello, ';\n"
                + "var world = 'world!';\n"
                + "hello.concat(world).length;\n");
        System.out.println(result);
        runtime.release();
    }
}
