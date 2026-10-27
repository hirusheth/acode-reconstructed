package com.foxdebug.acode.rk.exec.terminal;

import androidx.core.view.PointerIconCompat;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes.dex */
class ProcessServer extends WebSocketServer {
    private final String[] cmd;
    private final CountDownLatch readyLatch;
    private final AtomicReference<Exception> startError;

    private static final class ConnState {
        final Process process;
        final OutputStream stdin;

        ConnState(Process process, OutputStream outputStream) {
            this.process = process;
            this.stdin = outputStream;
        }
    }

    ProcessServer(int i, String[] strArr) {
        super(new InetSocketAddress("127.0.0.1", i));
        this.readyLatch = new CountDownLatch(1);
        this.startError = new AtomicReference<>();
        this.cmd = strArr;
    }

    void startAndAwait() throws Exception {
        start();
        this.readyLatch.await();
        Exception exc = this.startError.get();
        if (exc != null) {
            throw exc;
        }
    }

    public void onStart() {
        this.readyLatch.countDown();
    }

    public void onError(WebSocket webSocket, Exception exc) {
        if (webSocket == null) {
            this.startError.set(exc);
            this.readyLatch.countDown();
        }
    }

    public void onOpen(final WebSocket webSocket, ClientHandshake clientHandshake) {
        try {
            Process processStart = new ProcessBuilder(this.cmd).redirectErrorStream(true).start();
            final InputStream inputStream = processStart.getInputStream();
            webSocket.setAttachment(new ConnState(processStart, processStart.getOutputStream()));
            new Thread(new Runnable() { // from class: com.foxdebug.acode.rk.exec.terminal.ProcessServer$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    ProcessServer.lambda$onOpen$0(inputStream, webSocket);
                }
            }).start();
        } catch (Exception e) {
            webSocket.close(PointerIconCompat.TYPE_COPY, "Failed to start process: " + e.getMessage());
        }
    }

    static /* synthetic */ void lambda$onOpen$0(InputStream inputStream, WebSocket webSocket) {
        try {
            byte[] bArr = new byte[8192];
            while (true) {
                int i = inputStream.read(bArr);
                if (i == -1) {
                    break;
                } else {
                    webSocket.send(ByteBuffer.wrap(bArr, 0, i));
                }
            }
        } catch (Exception unused) {
        }
        webSocket.close(1000, "process exited");
    }

    public void onMessage(WebSocket webSocket, ByteBuffer byteBuffer) {
        try {
            ConnState connState = (ConnState) webSocket.getAttachment();
            connState.stdin.write(byteBuffer.array(), byteBuffer.position(), byteBuffer.remaining());
            connState.stdin.flush();
        } catch (Exception unused) {
        }
    }

    public void onMessage(WebSocket webSocket, String str) {
        try {
            ConnState connState = (ConnState) webSocket.getAttachment();
            connState.stdin.write(str.getBytes(StandardCharsets.UTF_8));
            connState.stdin.flush();
        } catch (Exception unused) {
        }
    }

    public void onClose(WebSocket webSocket, int i, String str, boolean z) {
        try {
            ConnState connState = (ConnState) webSocket.getAttachment();
            if (connState != null) {
                connState.process.destroy();
            }
        } catch (Exception unused) {
        }
        new Thread(new Runnable() { // from class: com.foxdebug.acode.rk.exec.terminal.ProcessServer$$ExternalSyntheticLambda1
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m125lambda$onClose$1$comfoxdebugacoderkexecterminalProcessServer();
            }
        }).start();
    }

    /* JADX INFO: renamed from: lambda$onClose$1$com-foxdebug-acode-rk-exec-terminal-ProcessServer, reason: not valid java name */
    /* synthetic */ void m125lambda$onClose$1$comfoxdebugacoderkexecterminalProcessServer() {
        try {
            stop();
        } catch (Exception unused) {
        }
    }
}