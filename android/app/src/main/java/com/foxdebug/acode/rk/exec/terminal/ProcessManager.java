package com.foxdebug.acode.rk.exec.terminal;

import android.content.Context;
import android.content.pm.PackageManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.Map;
import java.util.TimeZone;

/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes.dex */
public class ProcessManager {
    public static boolean prootDebug = false;
    private final Context context;

    public ProcessManager(Context context) {
        this.context = context;
    }

    public ProcessBuilder createProcessBuilder(String str, boolean z) {
        if (z) {
            refreshAxsSymlink();
        }
        if (z) {
            str = "source $PREFIX/init-sandbox.sh " + str;
        }
        ProcessBuilder processBuilder = new ProcessBuilder("sh", "-c", str);
        setupEnvironment(processBuilder.environment());
        return processBuilder;
    }

    private void refreshAxsSymlink() {
        if (isFdroidBuild()) {
            return;
        }
        Path path = Paths.get(this.context.getFilesDir().getAbsolutePath(), "axs");
        Path path2 = Paths.get(this.context.getApplicationInfo().nativeLibraryDir, "libaxs.so");
        if (Files.exists(path2, new LinkOption[0])) {
            try {
                if (Files.isSymbolicLink(path) && Files.readSymbolicLink(path).equals(path2)) {
                    return;
                }
                Files.deleteIfExists(path);
                Files.createSymbolicLink(path, path2, new FileAttribute[0]);
            } catch (Exception unused) {
            }
        }
    }

    private boolean isFdroidBuild() {
        return getTargetSdkVersion() <= 28;
    }

    private int getTargetSdkVersion() {
        try {
            return this.context.getPackageManager().getPackageInfo(this.context.getPackageName(), 0).applicationInfo.targetSdkVersion;
        } catch (PackageManager.NameNotFoundException unused) {
            return 28;
        }
    }

    private void setupEnvironment(Map<String, String> map) {
        map.put("PREFIX", this.context.getFilesDir().getAbsolutePath());
        map.put("NATIVE_DIR", this.context.getApplicationInfo().nativeLibraryDir);
        map.put("ANDROID_TZ", TimeZone.getDefault().getID());
        map.put("FDROID", String.valueOf(isFdroidBuild()));
        if (prootDebug) {
            map.put("PROOT_VERBOSE", "2");
        }
    }

    public static String readStream(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder sb = new StringBuilder();
        while (true) {
            String line = bufferedReader.readLine();
            if (line != null) {
                sb.append(line).append("\n");
            } else {
                return sb.toString();
            }
        }
    }

    public ExecResult executeCommand(String str, boolean z) throws Exception {
        Process processStart = createProcessBuilder(str, z).start();
        return new ExecResult(processStart.waitFor(), readStream(processStart.getInputStream()).trim(), readStream(processStart.getErrorStream()).trim());
    }

    public static class ExecResult {
        public final int exitCode;
        public final String stderr;
        public final String stdout;

        public ExecResult(int i, String str, String str2) {
            this.exitCode = i;
            this.stdout = str;
            this.stderr = str2;
        }

        public boolean isSuccess() {
            return this.exitCode == 0;
        }

        public String getErrorMessage() {
            if (!this.stderr.isEmpty()) {
                return this.stderr;
            }
            return "Command exited with code: " + this.exitCode;
        }
    }
}