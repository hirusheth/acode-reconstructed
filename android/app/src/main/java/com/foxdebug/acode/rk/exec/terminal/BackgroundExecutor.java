package com.foxdebug.acode.rk.exec.terminal;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes.dex */
public class BackgroundExecutor extends CordovaPlugin {
    private ProcessManager processManager;
    private final Map<String, Process> processes = new ConcurrentHashMap();
    private final Map<String, OutputStream> processInputs = new ConcurrentHashMap();
    private final Map<String, CallbackContext> processCallbacks = new ConcurrentHashMap();
    private final Map<String, ProcessDetails> processDetails = new ConcurrentHashMap();

    public void initialize(CordovaInterface cordovaInterface, CordovaWebView cordovaWebView) {
        super.initialize(cordovaInterface, cordovaWebView);
        this.processManager = new ProcessManager(cordovaInterface.getContext());
    }

    public boolean execute(String str, JSONArray jSONArray, CallbackContext callbackContext) throws JSONException {
        str.hashCode();
        switch (str) {
            case "listProcesses":
                listProcesses(callbackContext);
                return true;
            case "listAllProcesses":
                listAllProcesses(callbackContext);
                return true;
            case "setProotDebug":
                ProcessManager.prootDebug = jSONArray.getBoolean(0);
                callbackContext.success("PRoot debug ".concat(ProcessManager.prootDebug ? "enabled" : "disabled"));
                return true;
            case "loadLibrary":
                loadLibrary(jSONArray.getString(0), callbackContext);
                return true;
            case "exec":
                exec(jSONArray.getString(0), jSONArray.getString(1).equals("true"), callbackContext);
                return true;
            case "stop":
                stopProcess(jSONArray.getString(0), callbackContext);
                return true;
            case "start":
                startProcess(UUID.randomUUID().toString(), jSONArray.getString(0), jSONArray.getString(1).equals("true"), callbackContext);
                return true;
            case "write":
                writeToProcess(jSONArray.getString(0), jSONArray.getString(1), callbackContext);
                return true;
            case "killProcess":
                killProcess(jSONArray.getInt(0), callbackContext);
                return true;
            case "isRunning":
                isProcessRunning(jSONArray.getString(0), callbackContext);
                return true;
            default:
                callbackContext.error("Unknown action: " + str);
                return false;
        }
    }

    private void exec(final String str, final boolean z, final CallbackContext callbackContext) {
        this.cordova.getThreadPool().execute(new Runnable() { // from class: com.foxdebug.acode.rk.exec.terminal.BackgroundExecutor$$ExternalSyntheticLambda1
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m112lambda$exec$0$comfoxdebugacoderkexecterminalBackgroundExecutor(str, z, callbackContext);
            }
        });
    }

    /* JADX INFO: renamed from: lambda$exec$0$com-foxdebug-acode-rk-exec-terminal-BackgroundExecutor, reason: not valid java name */
    /* synthetic */ void m112lambda$exec$0$comfoxdebugacoderkexecterminalBackgroundExecutor(String str, boolean z, CallbackContext callbackContext) {
        try {
            ProcessManager.ExecResult execResultExecuteCommand = this.processManager.executeCommand(str, z);
            if (execResultExecuteCommand.isSuccess()) {
                callbackContext.success(execResultExecuteCommand.stdout);
            } else {
                callbackContext.error(execResultExecuteCommand.getErrorMessage());
            }
        } catch (Exception e) {
            callbackContext.error("Exception: " + e.getMessage());
        }
    }

    private void startProcess(final String str, final String str2, final boolean z, final CallbackContext callbackContext) {
        this.cordova.getThreadPool().execute(new Runnable() { // from class: com.foxdebug.acode.rk.exec.terminal.BackgroundExecutor$$ExternalSyntheticLambda5
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m117lambda$startProcess$5$comfoxdebugacoderkexecterminalBackgroundExecutor(str2, z, str, callbackContext);
            }
        });
    }

    /* JADX INFO: renamed from: lambda$startProcess$5$com-foxdebug-acode-rk-exec-terminal-BackgroundExecutor, reason: not valid java name */
    /* synthetic */ void m117lambda$startProcess$5$comfoxdebugacoderkexecterminalBackgroundExecutor(String str, boolean z, final String str2, CallbackContext callbackContext) {
        try {
            final Process processStart = this.processManager.createProcessBuilder(str, z).start();
            long pid = ProcessUtils.getPid(processStart);
            this.processes.put(str2, processStart);
            this.processInputs.put(str2, processStart.getOutputStream());
            this.processCallbacks.put(str2, callbackContext);
            this.processDetails.put(str2, new ProcessDetails(str, z, pid));
            sendPluginResult(callbackContext, str2, true);
            new Thread(new Runnable() { // from class: com.foxdebug.acode.rk.exec.terminal.BackgroundExecutor$$ExternalSyntheticLambda2
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.m114lambda$startProcess$2$comfoxdebugacoderkexecterminalBackgroundExecutor(processStart, str2);
                }
            }).start();
            new Thread(new Runnable() { // from class: com.foxdebug.acode.rk.exec.terminal.BackgroundExecutor$$ExternalSyntheticLambda3
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.m116lambda$startProcess$4$comfoxdebugacoderkexecterminalBackgroundExecutor(processStart, str2);
                }
            }).start();
            sendPluginMessage(str2, "exit:" + processStart.waitFor());
            cleanup(str2);
        } catch (Exception e) {
            callbackContext.error("Failed to start process: " + e.getMessage());
        }
    }

    /* JADX INFO: renamed from: lambda$startProcess$2$com-foxdebug-acode-rk-exec-terminal-BackgroundExecutor, reason: not valid java name */
    /* synthetic */ void m114lambda$startProcess$2$comfoxdebugacoderkexecterminalBackgroundExecutor(Process process, final String str) {
        StreamHandler.streamOutput(process.getInputStream(), new StreamHandler.OutputListener() { // from class: com.foxdebug.acode.rk.exec.terminal.BackgroundExecutor$$ExternalSyntheticLambda4
            @Override // com.foxdebug.acode.rk.exec.terminal.StreamHandler.OutputListener
            public final void onLine(String str2) {
                this.f$0.m113lambda$startProcess$1$comfoxdebugacoderkexecterminalBackgroundExecutor(str, str2);
            }
        });
    }

    /* JADX INFO: renamed from: lambda$startProcess$1$com-foxdebug-acode-rk-exec-terminal-BackgroundExecutor, reason: not valid java name */
    /* synthetic */ void m113lambda$startProcess$1$comfoxdebugacoderkexecterminalBackgroundExecutor(String str, String str2) {
        sendPluginMessage(str, "stdout:" + str2);
    }

    /* JADX INFO: renamed from: lambda$startProcess$4$com-foxdebug-acode-rk-exec-terminal-BackgroundExecutor, reason: not valid java name */
    /* synthetic */ void m116lambda$startProcess$4$comfoxdebugacoderkexecterminalBackgroundExecutor(Process process, final String str) {
        StreamHandler.streamOutput(process.getErrorStream(), new StreamHandler.OutputListener() { // from class: com.foxdebug.acode.rk.exec.terminal.BackgroundExecutor$$ExternalSyntheticLambda0
            @Override // com.foxdebug.acode.rk.exec.terminal.StreamHandler.OutputListener
            public final void onLine(String str2) {
                this.f$0.m115lambda$startProcess$3$comfoxdebugacoderkexecterminalBackgroundExecutor(str, str2);
            }
        });
    }

    /* JADX INFO: renamed from: lambda$startProcess$3$com-foxdebug-acode-rk-exec-terminal-BackgroundExecutor, reason: not valid java name */
    /* synthetic */ void m115lambda$startProcess$3$comfoxdebugacoderkexecterminalBackgroundExecutor(String str, String str2) {
        sendPluginMessage(str, "stderr:" + str2);
    }

    private void writeToProcess(String str, String str2, CallbackContext callbackContext) {
        try {
            OutputStream outputStream = this.processInputs.get(str);
            if (outputStream != null) {
                StreamHandler.writeToStream(outputStream, str2);
                callbackContext.success("Written to process");
            } else {
                callbackContext.error("Process not found or closed");
            }
        } catch (IOException e) {
            callbackContext.error("Write error: " + e.getMessage());
        }
    }

    private void stopProcess(String str, CallbackContext callbackContext) {
        Process process = this.processes.get(str);
        if (process != null) {
            ProcessUtils.killProcessTree(process);
            cleanup(str);
            callbackContext.success("Process terminated");
            return;
        }
        callbackContext.error("No such process");
    }

    private void isProcessRunning(String str, CallbackContext callbackContext) {
        Process process = this.processes.get(str);
        if (process != null) {
            String str2 = ProcessUtils.isAlive(process) ? "running" : "exited";
            if (str2.equals("exited")) {
                cleanup(str);
            }
            callbackContext.success(str2);
            return;
        }
        callbackContext.success("not_found");
    }

    private void listProcesses(CallbackContext callbackContext) {
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
        callbackContext.success(jSONArray);
    }

    private void loadLibrary(String str, CallbackContext callbackContext) {
        callbackContext.error("This feature is no longer supported. Loading native libraries directly from JavaScript is no longer allowed due to security reasons.");
    }

    private void sendPluginResult(CallbackContext callbackContext, String str, boolean z) {
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, str);
        pluginResult.setKeepCallback(z);
        callbackContext.sendPluginResult(pluginResult);
    }

    private void sendPluginMessage(String str, String str2) {
        CallbackContext callbackContext = this.processCallbacks.get(str);
        if (callbackContext != null) {
            sendPluginResult(callbackContext, str2, true);
        }
    }

    private void cleanup(String str) {
        this.processes.remove(str);
        this.processInputs.remove(str);
        this.processCallbacks.remove(str);
        this.processDetails.remove(str);
    }

    private void listAllProcesses(CallbackContext callbackContext) {
        try {
            callbackContext.success(ProcessUtils.getAllProcesses());
        } catch (Exception e) {
            callbackContext.error("Failed to list all processes: " + e.getMessage());
        }
    }

    private void killProcess(int i, CallbackContext callbackContext) {
        try {
            ProcessUtils.killProcess(i);
            callbackContext.success("Process terminated");
        } catch (Exception e) {
            callbackContext.error("Failed to kill process: " + e.getMessage());
        }
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
}