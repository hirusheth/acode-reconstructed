package com.foxdebug.acode.rk.exec.terminal;

import android.R;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import androidx.core.app.NotificationCompat;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes.dex */
public class TerminalService extends Service {
    public static final String ACTION_EXIT_SERVICE = "com.foxdebug.acode.ACTION_EXIT_SERVICE";
    public static final String ACTION_TOGGLE_WAKE_LOCK = "com.foxdebug.acode.ACTION_TOGGLE_WAKE_LOCK";
    public static final String CHANNEL_ID = "terminal_exec_channel";
    public static boolean Default_Foreground = true;
    public static final String MOVE_TO_BACKGROUND = "com.foxdebug.acode.MOVE_TO_BACKGROUND";
    public static final String MOVE_TO_FOREGROUND = "com.foxdebug.acode.MOVE_TO_FOREGROUND";
    public static final int MSG_EXEC = 5;
    public static final int MSG_IS_RUNNING = 4;
    public static final int MSG_LIST_PROCESSES = 6;
    public static final int MSG_START_PROCESS = 1;
    public static final int MSG_STOP_PROCESS = 3;
    public static final int MSG_WRITE_TO_PROCESS = 2;
    private ProcessManager processManager;
    private PowerManager.WakeLock wakeLock;
    private final Map<String, Process> processes = new ConcurrentHashMap();
    private final Map<String, OutputStream> processInputs = new ConcurrentHashMap();
    private final Map<String, Messenger> clientMessengers = new ConcurrentHashMap();
    private final Map<String, ProcessDetails> processDetails = new ConcurrentHashMap();
    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    private final Messenger serviceMessenger = new Messenger(new ServiceHandler());
    private boolean isWakeLockHeld = false;

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        this.processManager = new ProcessManager(this);
        if (Default_Foreground) {
            createNotificationChannel();
            updateNotification();
        }
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return this.serviceMessenger.getBinder();
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int i, int i2) {
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_EXIT_SERVICE.equals(action)) {
                stopForeground(true);
                stopSelf();
                return 2;
            }
            if (ACTION_TOGGLE_WAKE_LOCK.equals(action)) {
                toggleWakeLock();
            } else if (MOVE_TO_BACKGROUND.equals(action)) {
                Default_Foreground = false;
                stopForeground(true);
            } else if (MOVE_TO_FOREGROUND.equals(action)) {
                Default_Foreground = true;
                createNotificationChannel();
                updateNotification();
            }
        }
        return 1;
    }

    private class ServiceHandler extends Handler {
        private ServiceHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            Bundle data = message.getData();
            String string = data.getString("id");
            Messenger messenger = message.replyTo;
            switch (message.what) {
                case 1:
                    String string2 = data.getString("cmd");
                    String string3 = data.getString("alpine");
                    TerminalService.this.clientMessengers.put(string, messenger);
                    TerminalService.this.startProcess(string, string2, "true".equals(string3));
                    break;
                case 2:
                    TerminalService.this.writeToProcess(string, data.getString("input"));
                    break;
                case 3:
                    TerminalService.this.stopProcess(string);
                    break;
                case 4:
                    TerminalService.this.isProcessRunning(string, messenger);
                    break;
                case 5:
                    String string4 = data.getString("cmd");
                    String string5 = data.getString("alpine");
                    TerminalService.this.clientMessengers.put(string, messenger);
                    TerminalService.this.exec(string, string4, "true".equals(string5));
                    break;
                case 6:
                    TerminalService.this.listProcesses(string, messenger);
                    break;
            }
        }
    }

    private void toggleWakeLock() {
        if (this.isWakeLockHeld) {
            releaseWakeLock();
        } else {
            acquireWakeLock();
        }
        updateNotification();
    }

    private void acquireWakeLock() {
        if (this.wakeLock == null) {
            this.wakeLock = ((PowerManager) getSystemService("power")).newWakeLock(1, "AcodeTerminal:WakeLock");
        }
        if (this.isWakeLockHeld) {
            return;
        }
        this.wakeLock.acquire();
        this.isWakeLockHeld = true;
    }

    private void releaseWakeLock() {
        PowerManager.WakeLock wakeLock = this.wakeLock;
        if (wakeLock == null || !this.isWakeLockHeld) {
            return;
        }
        wakeLock.release();
        this.isWakeLockHeld = false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startProcess(final String str, final String str2, final boolean z) {
        this.threadPool.execute(new Runnable() { // from class: com.foxdebug.acode.rk.exec.terminal.TerminalService$$ExternalSyntheticLambda6
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m139lambda$startProcess$5$comfoxdebugacoderkexecterminalTerminalService(str2, z, str);
            }
        });
    }

    /* JADX INFO: renamed from: lambda$startProcess$5$com-foxdebug-acode-rk-exec-terminal-TerminalService, reason: not valid java name */
    /* synthetic */ void m139lambda$startProcess$5$comfoxdebugacoderkexecterminalTerminalService(String str, boolean z, final String str2) {
        try {
            final Process processStart = this.processManager.createProcessBuilder(str, z).start();
            long pid = ProcessUtils.getPid(processStart);
            this.processes.put(str2, processStart);
            this.processInputs.put(str2, processStart.getOutputStream());
            this.processDetails.put(str2, new ProcessDetails(str, z, pid));
            this.threadPool.execute(new Runnable() { // from class: com.foxdebug.acode.rk.exec.terminal.TerminalService$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.m135lambda$startProcess$1$comfoxdebugacoderkexecterminalTerminalService(processStart, str2);
                }
            });
            this.threadPool.execute(new Runnable() { // from class: com.foxdebug.acode.rk.exec.terminal.TerminalService$$ExternalSyntheticLambda1
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.m137lambda$startProcess$3$comfoxdebugacoderkexecterminalTerminalService(processStart, str2);
                }
            });
            this.threadPool.execute(new Runnable() { // from class: com.foxdebug.acode.rk.exec.terminal.TerminalService$$ExternalSyntheticLambda2
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.m138lambda$startProcess$4$comfoxdebugacoderkexecterminalTerminalService(processStart, str2);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            sendMessageToClient(str2, "stderr", "Failed to start process: " + e.getMessage());
            sendMessageToClient(str2, "exit", "1");
            cleanup(str2);
        }
    }

    /* JADX INFO: renamed from: lambda$startProcess$1$com-foxdebug-acode-rk-exec-terminal-TerminalService, reason: not valid java name */
    /* synthetic */ void m135lambda$startProcess$1$comfoxdebugacoderkexecterminalTerminalService(Process process, final String str) {
        StreamHandler.streamOutput(process.getInputStream(), new StreamHandler.OutputListener() { // from class: com.foxdebug.acode.rk.exec.terminal.TerminalService$$ExternalSyntheticLambda3
            @Override // com.foxdebug.acode.rk.exec.terminal.StreamHandler.OutputListener
            public final void onLine(String str2) {
                this.f$0.m134lambda$startProcess$0$comfoxdebugacoderkexecterminalTerminalService(str, str2);
            }
        });
    }

    /* JADX INFO: renamed from: lambda$startProcess$0$com-foxdebug-acode-rk-exec-terminal-TerminalService, reason: not valid java name */
    /* synthetic */ void m134lambda$startProcess$0$comfoxdebugacoderkexecterminalTerminalService(String str, String str2) {
        sendMessageToClient(str, "stdout", str2);
    }

    /* JADX INFO: renamed from: lambda$startProcess$3$com-foxdebug-acode-rk-exec-terminal-TerminalService, reason: not valid java name */
    /* synthetic */ void m137lambda$startProcess$3$comfoxdebugacoderkexecterminalTerminalService(Process process, final String str) {
        StreamHandler.streamOutput(process.getErrorStream(), new StreamHandler.OutputListener() { // from class: com.foxdebug.acode.rk.exec.terminal.TerminalService$$ExternalSyntheticLambda5
            @Override // com.foxdebug.acode.rk.exec.terminal.StreamHandler.OutputListener
            public final void onLine(String str2) {
                this.f$0.m136lambda$startProcess$2$comfoxdebugacoderkexecterminalTerminalService(str, str2);
            }
        });
    }

    /* JADX INFO: renamed from: lambda$startProcess$2$com-foxdebug-acode-rk-exec-terminal-TerminalService, reason: not valid java name */
    /* synthetic */ void m136lambda$startProcess$2$comfoxdebugacoderkexecterminalTerminalService(String str, String str2) {
        sendMessageToClient(str, "stderr", str2);
    }

    /* JADX INFO: renamed from: lambda$startProcess$4$com-foxdebug-acode-rk-exec-terminal-TerminalService, reason: not valid java name */
    /* synthetic */ void m138lambda$startProcess$4$comfoxdebugacoderkexecterminalTerminalService(Process process, String str) {
        try {
            sendMessageToClient(str, "exit", String.valueOf(process.waitFor()));
            cleanup(str);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void exec(final String str, final String str2, final boolean z) {
        this.threadPool.execute(new Runnable() { // from class: com.foxdebug.acode.rk.exec.terminal.TerminalService$$ExternalSyntheticLambda4
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m133lambda$exec$6$comfoxdebugacoderkexecterminalTerminalService(str2, z, str);
            }
        });
    }

    /* JADX INFO: renamed from: lambda$exec$6$com-foxdebug-acode-rk-exec-terminal-TerminalService, reason: not valid java name */
    /* synthetic */ void m133lambda$exec$6$comfoxdebugacoderkexecterminalTerminalService(String str, boolean z, String str2) {
        try {
            ProcessManager.ExecResult execResultExecuteCommand = this.processManager.executeCommand(str, z);
            if (execResultExecuteCommand.isSuccess()) {
                sendExecResultToClient(str2, true, execResultExecuteCommand.stdout);
            } else {
                sendExecResultToClient(str2, false, execResultExecuteCommand.getErrorMessage());
            }
            cleanup(str2);
        } catch (Exception e) {
            sendExecResultToClient(str2, false, "Exception: " + e.getMessage());
            cleanup(str2);
        }
    }

    private void sendMessageToClient(String str, String str2, String str3) {
        Messenger messenger = this.clientMessengers.get(str);
        if (messenger != null) {
            try {
                Message messageObtain = Message.obtain();
                Bundle bundle = new Bundle();
                bundle.putString("id", str);
                bundle.putString("action", str2);
                bundle.putString("data", str3);
                messageObtain.setData(bundle);
                messenger.send(messageObtain);
            } catch (RemoteException unused) {
                cleanup(str);
            }
        }
    }

    private void sendExecResultToClient(String str, boolean z, String str2) {
        Messenger messenger = this.clientMessengers.get(str);
        if (messenger != null) {
            try {
                Message messageObtain = Message.obtain();
                Bundle bundle = new Bundle();
                bundle.putString("id", str);
                bundle.putString("action", "exec_result");
                bundle.putString("data", str2);
                bundle.putBoolean("isSuccess", z);
                messageObtain.setData(bundle);
                messenger.send(messageObtain);
            } catch (RemoteException unused) {
                cleanup(str);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void writeToProcess(String str, String str2) {
        try {
            OutputStream outputStream = this.processInputs.get(str);
            if (outputStream != null) {
                StreamHandler.writeToStream(outputStream, str2);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void stopProcess(String str) {
        Process process = this.processes.get(str);
        if (process != null) {
            ProcessUtils.killProcessTree(process);
            cleanup(str);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void listProcesses(String str, Messenger messenger) {
        ProcessDetails processDetails;
        JSONArray jSONArray = new JSONArray();
        for (Map.Entry<String, Process> entry : this.processes.entrySet()) {
            String key = entry.getKey();
            if (ProcessUtils.isAlive(entry.getValue()) && (processDetails = this.processDetails.get(key)) != null) {
                try {
                    JSONObject jSONObject = new JSONObject();
                    jSONObject.put("id", key);
                    jSONObject.put("command", processDetails.command);
                    jSONObject.put("alpine", processDetails.alpine);
                    jSONObject.put("startedAt", processDetails.startedAt);
                    jSONObject.put("pid", processDetails.pid);
                    jSONArray.put(jSONObject);
                } catch (JSONException unused) {
                }
            }
        }
        try {
            Message messageObtain = Message.obtain();
            Bundle bundle = new Bundle();
            bundle.putString("id", str);
            bundle.putString("action", "listProcesses");
            bundle.putString("data", jSONArray.toString());
            messageObtain.setData(bundle);
            messenger.send(messageObtain);
        } catch (RemoteException unused2) {
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void isProcessRunning(String str, Messenger messenger) {
        boolean z = this.processes.containsKey(str) && ProcessUtils.isAlive(this.processes.get(str));
        try {
            Message messageObtain = Message.obtain();
            Bundle bundle = new Bundle();
            bundle.putString("id", str);
            bundle.putString("action", "isRunning");
            bundle.putString("data", z ? "running" : "stopped");
            messageObtain.setData(bundle);
            messenger.send(messageObtain);
        } catch (RemoteException unused) {
        }
    }

    private void cleanup(String str) {
        this.processes.remove(str);
        this.processInputs.remove(str);
        this.clientMessengers.remove(str);
        this.processDetails.remove(str);
    }

    private static class ProcessDetails {
        final boolean alpine;
        final String command;
        final long pid;
        final long startedAt = System.currentTimeMillis();

        ProcessDetails(String str, boolean z, long j) {
            this.command = str;
            this.alpine = z;
            this.pid = j;
        }
    }

    private void createNotificationChannel() {
        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "Terminal Executor Channel", 2);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    private void updateNotification() {
        Intent intent = new Intent(this, (Class<?>) TerminalService.class);
        intent.setAction(ACTION_EXIT_SERVICE);
        PendingIntent service = PendingIntent.getService(this, 0, intent, 201326592);
        Intent intent2 = new Intent(this, (Class<?>) TerminalService.class);
        intent2.setAction(ACTION_TOGGLE_WAKE_LOCK);
        PendingIntent service2 = PendingIntent.getService(this, 1, intent2, 201326592);
        String strConcat = "Executor service".concat(this.isWakeLockHeld ? " (wakelock held)" : "");
        String str = this.isWakeLockHeld ? "Release Wake Lock" : "Acquire Wake Lock";
        int iResolveDrawableId = resolveDrawableId("ic_notification", "ic_launcher_foreground", "ic_launcher");
        startForeground(1, new NotificationCompat.Builder(this, CHANNEL_ID).setContentTitle("Acode Service").setContentText(strConcat).setSmallIcon(iResolveDrawableId).setOngoing(true).addAction(iResolveDrawableId, str, service2).addAction(iResolveDrawableId, "Exit", service).build());
    }

    @Override // android.app.Service
    public void onDestroy() {
        stopForeground(true);
        super.onDestroy();
        releaseWakeLock();
        Iterator<Process> it = this.processes.values().iterator();
        while (it.hasNext()) {
            ProcessUtils.killProcessTree(it.next());
        }
        this.processes.clear();
        this.processInputs.clear();
        this.clientMessengers.clear();
        this.processDetails.clear();
        this.threadPool.shutdown();
    }

    private int resolveDrawableId(String... strArr) {
        for (String str : strArr) {
            int identifier = getResources().getIdentifier(str, "drawable", getPackageName());
            if (identifier != 0) {
                return identifier;
            }
        }
        return R.drawable.sym_def_app_icon;
    }
}