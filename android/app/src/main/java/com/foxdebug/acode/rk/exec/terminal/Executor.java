package com.foxdebug.acode.rk.exec.terminal;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.net.ServerSocket;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes.dex */
public class Executor extends CordovaPlugin {
    private static final int REQUEST_POST_NOTIFICATIONS = 1001;
    private Activity activity;
    private Context context;
    private boolean isServiceBinding;
    private boolean isServiceBound;
    private CountDownLatch serviceConnectedLatch;
    private Messenger serviceMessenger;
    private final Messenger handlerMessenger = new Messenger(new IncomingHandler());
    private final Map<String, CallbackContext> callbackContextMap = new ConcurrentHashMap();
    private final ServiceConnection serviceConnection = new ServiceConnection() { // from class: com.foxdebug.acode.rk.exec.terminal.Executor.1
        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d("Executor", "Service connected");
            Executor.this.serviceMessenger = new Messenger(iBinder);
            Executor.this.isServiceBound = true;
            Executor.this.isServiceBinding = false;
            if (Executor.this.serviceConnectedLatch != null) {
                Executor.this.serviceConnectedLatch.countDown();
            }
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
            Log.w("Executor", "Service disconnected");
            Executor.this.serviceMessenger = null;
            Executor.this.isServiceBound = false;
            Executor.this.isServiceBinding = false;
            Executor.this.serviceConnectedLatch = new CountDownLatch(1);
        }
    };

    private void askNotificationPermission(Activity activity) {
        if (Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(activity, "android.permission.POST_NOTIFICATIONS") == 0) {
            return;
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, "android.permission.POST_NOTIFICATIONS")) {
            ActivityCompat.requestPermissions(activity, new String[]{"android.permission.POST_NOTIFICATIONS"}, 1001);
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{"android.permission.POST_NOTIFICATIONS"}, 1001);
        }
    }

    public void initialize(CordovaInterface cordovaInterface, CordovaWebView cordovaWebView) {
        super.initialize(cordovaInterface, cordovaWebView);
        this.context = cordovaInterface.getContext();
        AppCompatActivity activity = cordovaInterface.getActivity();
        this.activity = activity;
        askNotificationPermission(activity);
        Log.d("Executor", "Plugin initialized - service will be started when needed");
    }

    private boolean ensureServiceBound(CallbackContext callbackContext) {
        if (this.isServiceBound && this.serviceMessenger != null) {
            return true;
        }
        if (this.isServiceBinding) {
            try {
                CountDownLatch countDownLatch = this.serviceConnectedLatch;
                if (countDownLatch != null && countDownLatch.await(10L, TimeUnit.SECONDS)) {
                    return this.isServiceBound;
                }
                callbackContext.error("Service binding timeout");
                return false;
            } catch (InterruptedException e) {
                callbackContext.error("Service binding interrupted: " + e.getMessage());
                return false;
            }
        }
        Log.d("Executor", "Starting service binding...");
        return bindServiceNow(callbackContext);
    }

    private boolean bindServiceNow(CallbackContext callbackContext) {
        if (this.isServiceBinding) {
            return false;
        }
        this.isServiceBinding = true;
        this.serviceConnectedLatch = new CountDownLatch(1);
        Intent intent = new Intent(this.context, (Class<?>) TerminalService.class);
        this.context.startService(intent);
        if (!this.context.bindService(intent, this.serviceConnection, 1)) {
            Log.e("Executor", "Failed to bind to service");
            this.isServiceBinding = false;
            callbackContext.error("Failed to bind to service");
            return false;
        }
        try {
            if (this.serviceConnectedLatch.await(10L, TimeUnit.SECONDS)) {
                Log.d("Executor", "Service bound successfully");
                return this.isServiceBound;
            }
            Log.e("Executor", "Service binding timeout");
            callbackContext.error("Service binding timeout");
            this.isServiceBinding = false;
            return false;
        } catch (InterruptedException e) {
            Log.e("Executor", "Service binding interrupted: " + e.getMessage());
            callbackContext.error("Service binding interrupted: " + e.getMessage());
            this.isServiceBinding = false;
            return false;
        }
    }

    private class IncomingHandler extends Handler {
        private IncomingHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            Bundle data = message.getData();
            String string = data.getString("id");
            String string2 = data.getString("action");
            String string3 = data.getString("data");
            if (string2.equals("exec_result")) {
                CallbackContext callbackContext = Executor.this.getCallbackContext(string);
                if (callbackContext != null) {
                    if (data.getBoolean("isSuccess", false)) {
                        callbackContext.success(string3);
                    } else {
                        callbackContext.error(string3);
                    }
                    Executor.this.cleanupCallback(string);
                }
                return;
            }
            CallbackContext callbackContext2 = Executor.this.getCallbackContext(string);
            if (callbackContext2 != null) {
                string2.hashCode();
                switch (string2) {
                    case "listProcesses":
                        try {
                            callbackContext2.success(new JSONArray(string3));
                            break;
                        } catch (JSONException e) {
                            callbackContext2.error("Invalid process list: " + e.getMessage());
                        }
                        Executor.this.cleanupCallback(string);
                        break;
                    case "stderr":
                    case "stdout":
                        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, string2 + ":" + string3);
                        pluginResult.setKeepCallback(true);
                        callbackContext2.sendPluginResult(pluginResult);
                        break;
                    case "exit":
                        Executor.this.cleanupCallback(string);
                        callbackContext2.success("exit:" + string3);
                        break;
                    case "isRunning":
                        callbackContext2.success(string3);
                        Executor.this.cleanupCallback(string);
                        break;
                }
            }
        }
    }

    public boolean execute(String str, JSONArray jSONArray, CallbackContext callbackContext) throws JSONException {
        if (str.equals("loadLibrary")) {
            callbackContext.error("This feature is no longer supported. Loading native libraries directly from JavaScript is no longer allowed due to security reasons.");
            return true;
        }
        if (str.equals("stopService")) {
            stopServiceNow();
            callbackContext.success("Service stopped");
            return true;
        }
        if (str.equals("moveToBackground")) {
            Intent intent = new Intent(this.context, (Class<?>) TerminalService.class);
            intent.setAction(TerminalService.MOVE_TO_BACKGROUND);
            this.context.startService(intent);
            callbackContext.success("Service moved to background mode");
            return true;
        }
        if (str.equals("moveToForeground")) {
            Intent intent2 = new Intent(this.context, (Class<?>) TerminalService.class);
            intent2.setAction(TerminalService.MOVE_TO_FOREGROUND);
            this.context.startService(intent2);
            callbackContext.success("Service moved to foreground mode");
            return true;
        }
        if (str.equals("spawn")) {
            try {
                JSONArray jSONArray2 = jSONArray.getJSONArray(0);
                String[] strArr = new String[jSONArray2.length()];
                for (int i = 0; i < jSONArray2.length(); i++) {
                    strArr[i] = jSONArray2.getString(i);
                }
                ServerSocket serverSocket = new ServerSocket(0);
                try {
                    int localPort = serverSocket.getLocalPort();
                    serverSocket.close();
                    new ProcessServer(localPort, strArr).startAndAwait();
                    callbackContext.success(localPort);
                } catch (Throwable th) {
                    try {
                        serverSocket.close();
                    } catch (Throwable th2) {
                        th.addSuppressed(th2);
                    }
                    throw th;
                }
            } catch (Exception e) {
                e.printStackTrace();
                callbackContext.error("Failed to spawn process: " + e.getMessage());
            }
            return true;
        }
        if (str.equals("listAllProcesses")) {
            try {
                callbackContext.success(ProcessUtils.getAllProcesses());
            } catch (Exception e2) {
                callbackContext.error("Failed to list all processes: " + e2.getMessage());
            }
            return true;
        }
        if (str.equals("killProcess")) {
            try {
                ProcessUtils.killProcess(jSONArray.getInt(0));
                callbackContext.success("Process terminated");
            } catch (Exception e3) {
                callbackContext.error("Failed to kill process: " + e3.getMessage());
            }
            return true;
        }
        if (str.equals("setProotDebug")) {
            boolean z = jSONArray.getBoolean(0);
            ProcessManager.prootDebug = z;
            callbackContext.success("PRoot debug ".concat(z ? "enabled" : "disabled"));
            return true;
        }
        if (str.equals("listProcesses") && (!this.isServiceBound || this.serviceMessenger == null)) {
            callbackContext.success(new JSONArray());
            return true;
        }
        if (!ensureServiceBound(callbackContext)) {
            return false;
        }
        str.hashCode();
        switch (str) {
            case "listProcesses":
                String string = UUID.randomUUID().toString();
                this.callbackContextMap.put(string, callbackContext);
                listProcesses(string);
                return true;
            case "exec":
                String string2 = UUID.randomUUID().toString();
                this.callbackContextMap.put(string2, callbackContext);
                exec(string2, jSONArray.getString(0), jSONArray.getString(1));
                return true;
            case "stop":
                stopProcess(jSONArray.getString(0), callbackContext);
                return true;
            case "start":
                String string3 = jSONArray.getString(0);
                String string4 = UUID.randomUUID().toString();
                this.callbackContextMap.put(string4, callbackContext);
                startProcess(string4, string3, jSONArray.getString(1));
                return true;
            case "write":
                writeToProcess(jSONArray.getString(0), jSONArray.getString(1), callbackContext);
                return true;
            case "isRunning":
                String string5 = jSONArray.getString(0);
                this.callbackContextMap.put(string5, callbackContext);
                isProcessRunning(string5);
                return true;
            default:
                callbackContext.error("Unknown action: " + str);
                return false;
        }
    }

    private void stopServiceNow() {
        if (this.isServiceBound) {
            try {
                this.context.unbindService(this.serviceConnection);
                Log.d("Executor", "Service unbound");
            } catch (IllegalArgumentException unused) {
            }
            this.isServiceBound = false;
        }
        this.isServiceBinding = false;
        Log.d("Executor", "Service stop result: " + this.context.stopService(new Intent(this.context, (Class<?>) TerminalService.class)));
        this.serviceMessenger = null;
        if (this.serviceConnectedLatch == null) {
            this.serviceConnectedLatch = new CountDownLatch(1);
        }
    }

    private void startProcess(String str, String str2, String str3) {
        CallbackContext callbackContext = getCallbackContext(str);
        if (callbackContext != null) {
            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, str);
            pluginResult.setKeepCallback(true);
            callbackContext.sendPluginResult(pluginResult);
        }
        Message messageObtain = Message.obtain((Handler) null, 1);
        messageObtain.replyTo = this.handlerMessenger;
        Bundle bundle = new Bundle();
        bundle.putString("id", str);
        bundle.putString("cmd", str2);
        bundle.putString("alpine", str3);
        messageObtain.setData(bundle);
        try {
            this.serviceMessenger.send(messageObtain);
        } catch (RemoteException e) {
            CallbackContext callbackContext2 = getCallbackContext(str);
            if (callbackContext2 != null) {
                callbackContext2.error("Failed to start process: " + e.getMessage());
                cleanupCallback(str);
            }
        }
    }

    private void exec(String str, String str2, String str3) {
        Message messageObtain = Message.obtain((Handler) null, 5);
        messageObtain.replyTo = this.handlerMessenger;
        Bundle bundle = new Bundle();
        bundle.putString("id", str);
        bundle.putString("cmd", str2);
        bundle.putString("alpine", str3);
        messageObtain.setData(bundle);
        try {
            this.serviceMessenger.send(messageObtain);
        } catch (RemoteException e) {
            CallbackContext callbackContext = getCallbackContext(str);
            if (callbackContext != null) {
                callbackContext.error("Failed to execute command: " + e.getMessage());
                cleanupCallback(str);
            }
        }
    }

    private void writeToProcess(String str, String str2, CallbackContext callbackContext) {
        Message messageObtain = Message.obtain((Handler) null, 2);
        Bundle bundle = new Bundle();
        bundle.putString("id", str);
        bundle.putString("input", str2);
        messageObtain.setData(bundle);
        try {
            this.serviceMessenger.send(messageObtain);
            callbackContext.success("Written to process");
        } catch (RemoteException e) {
            callbackContext.error("Write error: " + e.getMessage());
        }
    }

    private void stopProcess(String str, CallbackContext callbackContext) {
        Message messageObtain = Message.obtain((Handler) null, 3);
        Bundle bundle = new Bundle();
        bundle.putString("id", str);
        messageObtain.setData(bundle);
        try {
            this.serviceMessenger.send(messageObtain);
            callbackContext.success("Process terminated");
        } catch (RemoteException e) {
            callbackContext.error("Stop error: " + e.getMessage());
        }
    }

    private void isProcessRunning(String str) {
        Message messageObtain = Message.obtain((Handler) null, 4);
        messageObtain.replyTo = this.handlerMessenger;
        Bundle bundle = new Bundle();
        bundle.putString("id", str);
        messageObtain.setData(bundle);
        try {
            this.serviceMessenger.send(messageObtain);
        } catch (RemoteException e) {
            CallbackContext callbackContext = getCallbackContext(str);
            if (callbackContext != null) {
                callbackContext.error("Check running error: " + e.getMessage());
                cleanupCallback(str);
            }
        }
    }

    private void listProcesses(String str) {
        Message messageObtain = Message.obtain((Handler) null, 6);
        messageObtain.replyTo = this.handlerMessenger;
        Bundle bundle = new Bundle();
        bundle.putString("id", str);
        messageObtain.setData(bundle);
        try {
            this.serviceMessenger.send(messageObtain);
        } catch (RemoteException e) {
            CallbackContext callbackContext = getCallbackContext(str);
            if (callbackContext != null) {
                callbackContext.error("List processes error: " + e.getMessage());
                cleanupCallback(str);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public CallbackContext getCallbackContext(String str) {
        return this.callbackContextMap.get(str);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void cleanupCallback(String str) {
        this.callbackContextMap.remove(str);
    }

    public void onDestroy() {
        super.onDestroy();
    }
}