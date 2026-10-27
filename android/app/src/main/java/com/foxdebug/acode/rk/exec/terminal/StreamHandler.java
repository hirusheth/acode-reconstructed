package com.foxdebug.acode.rk.exec.terminal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes.dex */
public class StreamHandler {

    public interface OutputListener {
        void onLine(String str);
    }

    public static void streamOutput(InputStream inputStream, OutputListener outputListener) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            while (true) {
                try {
                    String line = bufferedReader.readLine();
                    if (line != null) {
                        outputListener.onLine(line);
                    } else {
                        bufferedReader.close();
                        return;
                    }
                } catch (Throwable th) {
                    try {
                        bufferedReader.close();
                    } catch (Throwable th2) {
                        th.addSuppressed(th2);
                    }
                    throw th;
                }
            }
        } catch (IOException unused) {
        }
    }

    public static void writeToStream(OutputStream outputStream, String str) throws IOException {
        outputStream.write((str + "\n").getBytes());
        outputStream.flush();
    }
}