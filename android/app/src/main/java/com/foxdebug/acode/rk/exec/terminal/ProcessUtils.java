package com.foxdebug.acode.rk.exec.terminal;

import android.os.Process;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import org.json.JSONArray;
import org.json.JSONObject;

/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes.dex */
public class ProcessUtils {
    public static long getPid(Process process) {
        try {
            Field declaredField = process.getClass().getDeclaredField("pid");
            declaredField.setAccessible(true);
            return declaredField.getLong(process);
        } catch (Exception unused) {
            return -1L;
        }
    }

    public static boolean isAlive(Process process) {
        try {
            process.exitValue();
            return false;
        } catch (IllegalThreadStateException unused) {
            return true;
        }
    }

    public static void killProcessTree(Process process) {
        try {
            long pid = getPid(process);
            if (pid > 0) {
                Runtime.getRuntime().exec(new String[]{"kill", "-9", "-" + pid});
            }
        } catch (Exception e) {
            Log.w("ProcessUtils", "Failed to kill process tree.", e);
        }
        process.destroy();
    }

    public static void killProcess(int i) throws InterruptedException, IOException {
        int iWaitFor = Runtime.getRuntime().exec(new String[]{"kill", "-9", String.valueOf(i)}).waitFor();
        if (iWaitFor != 0) {
            throw new IOException("kill -9 " + i + " exited with code " + iWaitFor);
        }
    }

    private static String readCmdline(File file) {
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            try {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] bArr = new byte[1024];
                do {
                    int i = fileInputStream.read(bArr);
                    if (i == -1) {
                        break;
                    }
                    byteArrayOutputStream.write(bArr, 0, i);
                } while (byteArrayOutputStream.size() <= 8192);
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                if (byteArray.length != 0) {
                    int length = byteArray.length;
                    while (length > 0 && byteArray[length - 1] == 0) {
                        length--;
                    }
                    for (int i2 = 0; i2 < length; i2++) {
                        if (byteArray[i2] == 0) {
                            byteArray[i2] = 32;
                        }
                    }
                    String str = new String(byteArray, 0, length, StandardCharsets.UTF_8);
                    fileInputStream.close();
                    return str;
                }
                fileInputStream.close();
                return "";
            } catch (Throwable th) {
                try {
                    fileInputStream.close();
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
                throw th;
            }
        } catch (IOException unused) {
            return "";
        }
        return "";
    }

    /* JADX WARN: Code duplicated, block: B:68:0x014e  */
    public static JSONArray getAllProcesses() throws Throwable {
        File[] fileArr;
        int i;
        JSONArray jSONArray = new JSONArray();
        int iMyUid = Process.myUid();
        int iMyPid = Process.myPid();
        File[] fileArrListFiles = new File("/proc").listFiles();
        if (fileArrListFiles != null) {
            int length = fileArrListFiles.length;
            int i2 = 0;
            while (i2 < length) {
                File file = fileArrListFiles[i2];
                if (file.isDirectory()) {
                    String name = file.getName();
                    if (name.matches("\\d+")) {
                        int i3 = Integer.parseInt(name);
                        try {
                            File file2 = new File(file, NotificationCompat.CATEGORY_STATUS);
                            if (file2.exists()) {
                                BufferedReader bufferedReader = new BufferedReader(new FileReader(file2));
                                int i4 = -1;
                                long j = 0;
                                fileArr = fileArrListFiles;
                                String strTrim = "";
                                String strTrim2 = strTrim;
                                boolean z = false;
                                while (true) {
                                    try {
                                        String line = bufferedReader.readLine();
                                        if (line == null) {
                                            break;
                                        }
                                        i = length;
                                        try {
                                            if (line.startsWith("Name:")) {
                                                strTrim2 = line.substring(5).trim();
                                            } else if (line.startsWith("State:")) {
                                                strTrim = line.substring(6).trim();
                                            } else if (line.startsWith("PPid:")) {
                                                try {
                                                    i4 = Integer.parseInt(line.substring(5).trim());
                                                } catch (NumberFormatException unused) {
                                                }
                                            } else if (line.startsWith("Uid:")) {
                                                String[] strArrSplit = line.substring(4).trim().split("\\s+");
                                                if (strArrSplit.length > 0 && Integer.parseInt(strArrSplit[0]) == iMyUid) {
                                                    z = true;
                                                }
                                            } else if (line.startsWith("VmRSS:")) {
                                                j = Long.parseLong(line.substring(6).trim().replaceAll("[^0-9]", ""));
                                            }
                                            length = i;
                                        } catch (Throwable th) {
                                            th = th;
                                            Throwable th2 = th;
                                            try {
                                                bufferedReader.close();
                                            } catch (Throwable th3) {
                                                th2.addSuppressed(th3);
                                            }
                                            throw th2;
                                        }
                                    } catch (Throwable th4) {
                                        th = th4;
                                        i = length;
                                    }
                                }
                                i = length;
                                try {
                                    bufferedReader.close();
                                    if (z) {
                                        String cmdline = readCmdline(new File(file, "cmdline"));
                                        if (cmdline.isEmpty()) {
                                            cmdline = strTrim2;
                                        }
                                        JSONObject jSONObject = new JSONObject();
                                        jSONObject.put("pid", i3);
                                        jSONObject.put("ppid", i4);
                                        jSONObject.put("name", strTrim2);
                                        jSONObject.put("command", cmdline);
                                        jSONObject.put("state", strTrim);
                                        jSONObject.put("memory", j);
                                        jSONObject.put("isSelf", i3 == iMyPid);
                                        jSONObject.put("startedAt", file.lastModified());
                                        jSONArray.put(jSONObject);
                                    }
                                } catch (Exception unused2) {
                                    continue;
                                }
                            } else {
                                fileArr = fileArrListFiles;
                                i = length;
                            }
                        } catch (Exception unused3) {
                        }
                    } else {
                        fileArr = fileArrListFiles;
                        i = length;
                    }
                } else {
                    fileArr = fileArrListFiles;
                    i = length;
                }
                i2++;
                fileArrListFiles = fileArr;
                length = i;
            }
        }
        return jSONArray;
    }
}