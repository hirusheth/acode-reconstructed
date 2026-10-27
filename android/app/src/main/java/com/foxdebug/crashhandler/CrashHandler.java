package com.foxdebug.crashhandler;

import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.util.Log;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;

/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes.dex */
public class CrashHandler extends CordovaPlugin {
    private static final String TAG = "CrashHandler";

    public void initialize(final CordovaInterface cordovaInterface, CordovaWebView cordovaWebView) {
        super.initialize(cordovaInterface, cordovaWebView);
        Log.d(TAG, "Initializing CrashHandler...");
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() { // from class: com.foxdebug.crashhandler.CrashHandler.1
            @Override // java.lang.Thread.UncaughtExceptionHandler
            public void uncaughtException(Thread thread, Throwable th) {
                String string;
                try {
                    Log.e(CrashHandler.TAG, "Uncaught native exception detected!", th);
                    StringWriter stringWriter = new StringWriter();
                    th.printStackTrace(new PrintWriter(stringWriter));
                    String string2 = stringWriter.toString();
                    Context applicationContext = cordovaInterface.getActivity().getApplicationContext();
                    Intent intent = new Intent(applicationContext, (Class<?>) CrashActivity.class);
                    intent.putExtra("error_type", "Native Crash");
                    if (th.getMessage() != null) {
                        string = th.getMessage();
                    } else {
                        string = th.toString();
                    }
                    intent.putExtra("error_message", string);
                    intent.putExtra("stack_trace", string2);
                    intent.addFlags(268468224);
                    applicationContext.startActivity(intent);
                    Process.killProcess(Process.myPid());
                    System.exit(10);
                } catch (Throwable th2) {
                    try {
                        Log.e(CrashHandler.TAG, "Failed to launch CrashActivity", th2);
                    } finally {
                        Process.killProcess(Process.myPid());
                        System.exit(10);
                    }
                }
            }
        });
    }
}