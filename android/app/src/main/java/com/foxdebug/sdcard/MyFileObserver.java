package com.foxdebug.sdcard;

import android.os.FileObserver;
import android.util.Log;
import java.io.File;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;

/* JADX INFO: compiled from: SDcard.java */
/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes.dex */
class MyFileObserver extends FileObserver {
    private static final int mask = 3074;
    private CallbackContext listener;

    public MyFileObserver(String str, CallbackContext callbackContext) {
        super(str, mask);
        this.listener = callbackContext;
        Log.d("MyFileObserver", "MyFileObserver: " + str);
    }

    public MyFileObserver(File file, CallbackContext callbackContext) {
        super(file, mask);
        this.listener = callbackContext;
        Log.d("MyFileObserver", "MyFileObserver: " + file.getAbsolutePath());
    }

    @Override // android.os.FileObserver
    public void onEvent(int i, String str) {
        Log.d("MyFileObserver", "onEvent: " + i);
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK);
        pluginResult.setKeepCallback(true);
        this.listener.sendPluginResult(pluginResult);
    }

    public void startObserving() {
        startWatching();
    }

    public void stopObserving() {
        stopWatching();
    }
}