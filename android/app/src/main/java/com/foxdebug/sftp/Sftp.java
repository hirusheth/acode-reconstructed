package com.foxdebug.sftp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import androidx.documentfile.provider.DocumentFile;
import com.sshtools.client.SessionChannelNG;
import com.sshtools.client.SshClient;
import com.sshtools.client.SshClientContext;
import com.sshtools.client.sftp.SftpClient;
import com.sshtools.client.sftp.SftpFile;
import com.sshtools.client.sftp.TransferCancelledException;
import com.sshtools.common.files.nio.AbstractFilePath;
import com.sshtools.common.knownhosts.HostKeyVerification;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.common.policy.FileSystemPolicy;
import com.sshtools.common.publickey.InvalidPassphraseException;
import com.sshtools.common.publickey.SshKeyUtils;
import com.sshtools.common.sftp.SftpFileAttributes;
import com.sshtools.common.sftp.SftpStatusException;
import com.sshtools.common.ssh.Channel;
import com.sshtools.common.ssh.ChannelEventListener;
import com.sshtools.common.ssh.SshException;
import com.sshtools.common.ssh.components.SshPublicKey;
import com.sshtools.common.ssh.components.jce.JCEProvider;
import com.sshtools.common.util.UnsignedInteger32;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes.dex */
public class Sftp extends CordovaPlugin {
    private static final long DEFAULT_CONNECT_TIMEOUT_MS = 10000;
    private static final long MAX_CONNECT_TIMEOUT_MS = 30000;
    private static final long MIN_CONNECT_TIMEOUT_MS = 1000;
    private static final long SFTP_MAX_WINDOW_SIZE = 1048576;
    private static final long SFTP_MIN_WINDOW_SIZE = 131072;
    private static final String TAG = "SFTP";
    private static boolean cryptoProviderConfigured;
    private Activity activity;
    private String connectionID;
    private Context context;
    private SftpSecurityStore securityStore;
    private SftpClient sftp;
    private SshClient ssh;
    private final Object connectionLock = new Object();
    private final Map<String, ConnectionAttempt> connectionAttempts = new ConcurrentHashMap();
    private final Map<String, RemoteShell> remoteShells = new ConcurrentHashMap();

    /* JADX INFO: Access modifiers changed from: private */
    final class ConnectionAttempt {
        private final CallbackContext callback;
        private final AtomicBoolean cancelled;
        private final AtomicBoolean completed;
        private volatile Future<?> future;

        private ConnectionAttempt(CallbackContext callbackContext) {
            this.cancelled = new AtomicBoolean(false);
            this.completed = new AtomicBoolean(false);
            this.callback = callbackContext;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void setFuture(Future<?> future) {
            this.future = future;
            if (this.cancelled.get()) {
                future.cancel(true);
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public boolean isCancelled() {
            return this.cancelled.get();
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void cancel() {
            this.cancelled.set(true);
            Future<?> future = this.future;
            if (future != null) {
                future.cancel(true);
            }
            error(Sftp.connectionCancelledError());
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void success() {
            if (this.completed.compareAndSet(false, true)) {
                this.callback.success();
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void success(String str) {
            if (this.completed.compareAndSet(false, true)) {
                this.callback.success(str);
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void error(String str) {
            if (this.completed.compareAndSet(false, true)) {
                this.callback.error(str);
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void error(JSONObject jSONObject) {
            if (this.completed.compareAndSet(false, true)) {
                this.callback.error(jSONObject);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    final class ConnectionSecurity {
        private volatile JSONObject failure;
        private final String hostname;
        private final int port;

        private ConnectionSecurity(String str, int i) {
            this.hostname = str;
            this.port = i;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void configure(SshClientContext sshClientContext) {
            Sftp.configureClient(sshClientContext);
            sshClientContext.setHostKeyVerification(new HostKeyVerification() { // from class: com.foxdebug.sftp.Sftp.ConnectionSecurity.1
                @Override // com.sshtools.common.knownhosts.HostKeyVerification
                public boolean verifyHost(String str, SshPublicKey sshPublicKey) throws SshException {
                    try {
                        String fingerprint = sshPublicKey.getFingerprint();
                        String algorithm = sshPublicKey.getAlgorithm();
                        String strEncodeToString = Base64.encodeToString(sshPublicKey.getEncoded(), 2);
                        String str2 = ConnectionSecurity.this.hostname + ":" + ConnectionSecurity.this.port;
                        JSONObject knownHost = Sftp.this.securityStore.getKnownHost(str2);
                        if (knownHost == null) {
                            if (Sftp.this.confirmUnknownHost(str2, algorithm, fingerprint)) {
                                Sftp.this.securityStore.trustHost(str2, algorithm, fingerprint, strEncodeToString);
                                return true;
                            }
                            ConnectionSecurity.this.failure = Sftp.hostKeyFailure("HOST_KEY_REJECTED", str2, fingerprint, null);
                            return false;
                        }
                        String strOptString = knownHost.optString("fingerprint");
                        String strOptString2 = knownHost.optString("publicKey");
                        if ((strOptString2.isEmpty() || strEncodeToString.equals(strOptString2)) && (!strOptString2.isEmpty() || fingerprint.equals(strOptString))) {
                            return true;
                        }
                        ConnectionSecurity.this.failure = Sftp.hostKeyFailure("HOST_KEY_CHANGED", str2, fingerprint, strOptString);
                        Sftp.this.showChangedHostKey(str2, strOptString, fingerprint);
                        return false;
                    } catch (InterruptedException | JSONException e) {
                        if (e instanceof InterruptedException) {
                            Thread.currentThread().interrupt();
                        }
                        throw new SshException("Could not verify the SSH host key", 61440, e);
                    }
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        public boolean report(CallbackContext callbackContext) {
            JSONObject jSONObjectTakeFailure = takeFailure();
            if (jSONObjectTakeFailure == null) {
                return false;
            }
            callbackContext.error(jSONObjectTakeFailure);
            return true;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public JSONObject takeFailure() {
            JSONObject jSONObject = this.failure;
            this.failure = null;
            return jSONObject;
        }
    }

    private final class RemoteShell {
        private final SessionChannelNG channel;
        private final SshClient client;
        private final AtomicBoolean finished;
        private final String id;
        private final ExecutorService inputWriter;
        private final CallbackContext streamCallback;

        private RemoteShell(String str, SshClient sshClient, SessionChannelNG sessionChannelNG, CallbackContext callbackContext) {
            this.finished = new AtomicBoolean(false);
            this.inputWriter = Executors.newSingleThreadExecutor();
            this.id = str;
            this.client = sshClient;
            this.channel = sessionChannelNG;
            this.streamCallback = callbackContext;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void sendData(ByteBuffer byteBuffer) {
            if (this.finished.get() || byteBuffer == null || !byteBuffer.hasRemaining()) {
                return;
            }
            ByteBuffer byteBufferAsReadOnlyBuffer = byteBuffer.asReadOnlyBuffer();
            byte[] bArr = new byte[byteBufferAsReadOnlyBuffer.remaining()];
            byteBufferAsReadOnlyBuffer.get(bArr);
            try {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("type", "data");
                jSONObject.put("data", Base64.encodeToString(bArr, 2));
                Sftp.sendShellEvent(this.streamCallback, jSONObject, true);
            } catch (JSONException e) {
                finish(null, e.getMessage());
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void finish(Integer num, String str) {
            if (this.finished.compareAndSet(false, true)) {
                Sftp.this.remoteShells.remove(this.id, this);
                this.inputWriter.shutdownNow();
                try {
                    this.channel.close();
                } catch (Exception e) {
                    Log.w(Sftp.TAG, "Failed to close SSH shell channel " + this.id, e);
                }
                try {
                    this.client.close();
                } catch (IOException e2) {
                    Log.w(Sftp.TAG, "Failed to close SSH shell connection " + this.id, e2);
                }
                try {
                    JSONObject jSONObject = new JSONObject();
                    jSONObject.put("type", str == null ? "exit" : "error");
                    if (num != null) {
                        jSONObject.put("exitCode", num);
                    }
                    if (str != null) {
                        jSONObject.put("message", str);
                    }
                    Sftp.sendShellEvent(this.streamCallback, jSONObject, false);
                } catch (JSONException e3) {
                    this.streamCallback.error(Sftp.this.errMessage(e3));
                }
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void write(final String str, final CallbackContext callbackContext) {
            if (this.finished.get()) {
                callbackContext.error("SSH shell is not connected");
                return;
            }
            try {
                this.inputWriter.execute(new Runnable() { // from class: com.foxdebug.sftp.Sftp.RemoteShell.1
                    @Override // java.lang.Runnable
                    public void run() {
                        try {
                            RemoteShell.this.channel.getOutputStream().write(str.getBytes(StandardCharsets.UTF_8));
                            RemoteShell.this.channel.getOutputStream().flush();
                            callbackContext.success();
                        } catch (IOException e) {
                            RemoteShell remoteShell = RemoteShell.this;
                            remoteShell.finish(null, Sftp.this.errMessage(e));
                            callbackContext.error(Sftp.this.errMessage(e));
                        }
                    }
                });
            } catch (RejectedExecutionException unused) {
                callbackContext.error("SSH shell is not connected");
            }
        }
    }

    public void initialize(CordovaInterface cordovaInterface, CordovaWebView cordovaWebView) {
        super.initialize(cordovaInterface, cordovaWebView);
        this.context = cordovaInterface.getContext();
        this.activity = cordovaInterface.getActivity();
        this.securityStore = new SftpSecurityStore(this.context);
        System.setProperty("maverick.log.nothread", "true");
        configureCryptoProvider();
    }

    private static synchronized void configureCryptoProvider() {
        if (cryptoProviderConfigured) {
            return;
        }
        Security.removeProvider("BC");
        Security.insertProviderAt(new BouncyCastleProvider(), 1);
        JCEProvider.enableBouncyCastle(true);
        cryptoProviderConfigured = true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void configureClient(SshClientContext sshClientContext) {
        FileSystemPolicy fileSystemPolicy = (FileSystemPolicy) sshClientContext.getPolicy(FileSystemPolicy.class);
        fileSystemPolicy.setSftpMaxWindowSize(new UnsignedInteger32(SFTP_MAX_WINDOW_SIZE));
        fileSystemPolicy.setSftpMinWindowSize(new UnsignedInteger32(SFTP_MIN_WINDOW_SIZE));
        fileSystemPolicy.setMaximumNumberofAsyncSFTPRequests(4);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void closeConnectionQuietly() {
        SftpClient sftpClient = this.sftp;
        SshClient sshClient = this.ssh;
        this.sftp = null;
        this.ssh = null;
        this.connectionID = null;
        if (sftpClient != null) {
            try {
                sftpClient.quit();
            } catch (Exception e) {
                Log.w(TAG, "Failed to close the SFTP subsystem", e);
            }
        }
        if (sshClient != null) {
            try {
                sshClient.close();
            } catch (Exception e2) {
                Log.w(TAG, "Failed to close the SSH connection", e2);
            }
        }
    }

    private boolean establishConnection(SshClient.SshClientBuilder sshClientBuilder, String str, final ConnectionSecurity connectionSecurity, ConnectionAttempt connectionAttempt) throws SshException, PermissionDeniedException, IOException {
        synchronized (this.connectionLock) {
            closeConnectionQuietly();
            Objects.requireNonNull(connectionSecurity);
            this.ssh = sshClientBuilder.onConfigure(new SshClient.OnConfiguration() { // from class: com.foxdebug.sftp.Sftp$$ExternalSyntheticLambda0
                @Override // com.sshtools.client.SshClient.OnConfiguration
                public final void accept(SshClientContext sshClientContext) {
                    connectionSecurity.configure(sshClientContext);
                }
            }).build();
            if (connectionAttempt.isCancelled()) {
                closeConnectionQuietly();
                return false;
            }
            if (!this.ssh.isConnected()) {
                closeConnectionQuietly();
                return false;
            }
            this.connectionID = str;
            try {
                this.sftp = SftpClient.SftpClientBuilder.create().withClient(this.ssh).build();
                if (connectionAttempt.isCancelled()) {
                    closeConnectionQuietly();
                    return false;
                }
                try {
                    this.sftp.getSubsystemChannel().setCharsetEncoding("UTF-8");
                } catch (SshException | UnsupportedEncodingException e) {
                    Log.w(TAG, "Failed to set UTF-8 encoding, using the default", e);
                }
                return true;
            } catch (PermissionDeniedException e2) {
                e = e2;
                closeConnectionQuietly();
                throw e;
            } catch (SshException e3) {
                e = e3;
                closeConnectionQuietly();
                throw e;
            } catch (IOException e4) {
                e = e4;
                closeConnectionQuietly();
                throw e;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void sendShellEvent(CallbackContext callbackContext, JSONObject jSONObject, boolean z) {
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, jSONObject);
        pluginResult.setKeepCallback(z);
        callbackContext.sendPluginResult(pluginResult);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static JSONObject hostKeyFailure(String str, String str2, String str3, String str4) throws JSONException {
        JSONObject jSONObject = new JSONObject();
        jSONObject.put("code", str);
        jSONObject.put("host", str2);
        jSONObject.put("fingerprint", str3);
        if (str4 != null) {
            jSONObject.put("expectedFingerprint", str4);
        }
        return jSONObject;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static JSONObject connectionCancelledError() {
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject.put("code", "SFTP_CONNECT_CANCELLED");
            jSONObject.put("message", "SFTP connection cancelled");
            jSONObject.put("cancelled", true);
            jSONObject.put("nonRetryable", true);
        } catch (JSONException unused) {
        }
        return jSONObject;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean confirmUnknownHost(final String str, final String str2, final String str3) throws InterruptedException {
        Activity activity = this.activity;
        if (activity == null || activity.isFinishing()) {
            return false;
        }
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        this.activity.runOnUiThread(new Runnable() { // from class: com.foxdebug.sftp.Sftp$$ExternalSyntheticLambda10
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m204lambda$confirmUnknownHost$4$comfoxdebugsftpSftp(str, str2, str3, countDownLatch, atomicBoolean);
            }
        });
        countDownLatch.await();
        return atomicBoolean.get();
    }

    /* JADX INFO: renamed from: lambda$confirmUnknownHost$4$com-foxdebug-sftp-Sftp, reason: not valid java name */
    /* synthetic */ void m204lambda$confirmUnknownHost$4$comfoxdebugsftpSftp(String str, String str2, String str3, final CountDownLatch countDownLatch, final AtomicBoolean atomicBoolean) {
        AlertDialog alertDialogCreate = new AlertDialog.Builder(this.activity).setTitle("Unknown SSH host").setMessage("This is the first connection to " + str + ".\n\nKey type: " + str2 + "\nFingerprint: " + str3 + "\n\nVerify this fingerprint before trusting the host.").setNegativeButton("Cancel", new DialogInterface.OnClickListener() { // from class: com.foxdebug.sftp.Sftp$$ExternalSyntheticLambda2
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                countDownLatch.countDown();
            }
        }).setPositiveButton("Trust and connect", new DialogInterface.OnClickListener() { // from class: com.foxdebug.sftp.Sftp$$ExternalSyntheticLambda3
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                Sftp.lambda$confirmUnknownHost$2(atomicBoolean, countDownLatch, dialogInterface, i);
            }
        }).create();
        alertDialogCreate.setOnCancelListener(new DialogInterface.OnCancelListener() { // from class: com.foxdebug.sftp.Sftp$$ExternalSyntheticLambda4
            @Override // android.content.DialogInterface.OnCancelListener
            public final void onCancel(DialogInterface dialogInterface) {
                countDownLatch.countDown();
            }
        });
        alertDialogCreate.show();
    }

    static /* synthetic */ void lambda$confirmUnknownHost$2(AtomicBoolean atomicBoolean, CountDownLatch countDownLatch, DialogInterface dialogInterface, int i) {
        atomicBoolean.set(true);
        countDownLatch.countDown();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showChangedHostKey(final String str, final String str2, final String str3) {
        Activity activity = this.activity;
        if (activity == null || activity.isFinishing()) {
            return;
        }
        this.activity.runOnUiThread(new Runnable() { // from class: com.foxdebug.sftp.Sftp$$ExternalSyntheticLambda5
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m207lambda$showChangedHostKey$5$comfoxdebugsftpSftp(str, str2, str3);
            }
        });
    }

    /* JADX INFO: renamed from: lambda$showChangedHostKey$5$com-foxdebug-sftp-Sftp, reason: not valid java name */
    /* synthetic */ void m207lambda$showChangedHostKey$5$comfoxdebugsftpSftp(String str, String str2, String str3) {
        new AlertDialog.Builder(this.activity).setTitle("SSH host key changed").setMessage("The identity of " + str + " has changed. The connection was blocked.\n\nExpected: " + str2 + "\nReceived: " + str3).setPositiveButton("Close", (DialogInterface.OnClickListener) null).show();
    }

    private void closeRemoteShells() {
        Iterator<RemoteShell> it = this.remoteShells.values().iterator();
        while (it.hasNext()) {
            it.next().finish(null, null);
        }
        this.remoteShells.clear();
    }

    private void cancelConnectionAttempts() {
        Iterator<ConnectionAttempt> it = this.connectionAttempts.values().iterator();
        while (it.hasNext()) {
            it.next().cancel();
        }
        this.connectionAttempts.clear();
    }

    public void onReset() {
        cancelConnectionAttempts();
        closeRemoteShells();
        super.onReset();
    }

    public void onDestroy() {
        cancelConnectionAttempts();
        closeRemoteShells();
        super.onDestroy();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public SshClient.SshClientBuilder buildProfileBuilder(JSONObject jSONObject) throws JSONException, InvalidPassphraseException, IOException {
        return buildProfileBuilder(jSONObject, DEFAULT_CONNECT_TIMEOUT_MS);
    }

    private SshClient.SshClientBuilder buildProfileBuilder(JSONObject jSONObject, long j) throws JSONException, InvalidPassphraseException, IOException {
        SshClient.SshClientBuilder sshClientBuilderWithUsername = SshClient.SshClientBuilder.create().withHostname(jSONObject.getString("hostname")).withPort(jSONObject.optInt("port", 22)).withConnectTimeout(j).withUsername(jSONObject.getString("username"));
        if ("key".equals(jSONObject.optString("authType"))) {
            sshClientBuilderWithUsername.withIdentities(SshKeyUtils.getPrivateKey(new ByteArrayInputStream(Base64.decode(jSONObject.getString("privateKey"), 2)), jSONObject.optString("passphrase")));
            return sshClientBuilderWithUsername;
        }
        sshClientBuilderWithUsername.withPassword(jSONObject.optString("password"));
        return sshClientBuilderWithUsername;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public byte[] readUri(String str) throws IOException {
        if (str == null || str.isEmpty()) {
            throw new IOException("Private key file is required");
        }
        InputStream inputStreamOpenInputStream = this.context.getContentResolver().openInputStream(Uri.parse(str));
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try {
                if (inputStreamOpenInputStream == null) {
                    throw new IOException("Could not open key file");
                }
                byte[] bArr = new byte[8192];
                while (true) {
                    int i = inputStreamOpenInputStream.read(bArr);
                    if (i == -1) {
                        break;
                    }
                    byteArrayOutputStream.write(bArr, 0, i);
                }
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                byteArrayOutputStream.close();
                if (inputStreamOpenInputStream != null) {
                    inputStreamOpenInputStream.close();
                }
                return byteArray;
            } catch (Throwable th) {
                try {
                    byteArrayOutputStream.close();
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
                throw th;
            }
        } catch (Throwable th3) {
            if (inputStreamOpenInputStream != null) {
                try {
                    inputStreamOpenInputStream.close();
                } catch (Throwable th4) {
                    th3.addSuppressed(th4);
                }
            }
            throw th3;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void openRemoteShell(SshClient sshClient, int i, int i2, CallbackContext callbackContext) throws SshException, JSONException, IOException {
        if (!sshClient.isConnected()) {
            sshClient.close();
            throw new IOException("Failed to establish SSH connection");
        }
        String string = UUID.randomUUID().toString();
        try {
            final SessionChannelNG sessionChannelNGOpenSessionChannel = sshClient.openSessionChannel(true);
            final RemoteShell remoteShell = new RemoteShell(string, sshClient, sessionChannelNGOpenSessionChannel, callbackContext);
            this.remoteShells.put(string, remoteShell);
            sessionChannelNGOpenSessionChannel.addEventListener(new ChannelEventListener() { // from class: com.foxdebug.sftp.Sftp.1
                @Override // com.sshtools.common.ssh.ChannelEventListener
                public void onChannelDataIn(Channel channel, ByteBuffer byteBuffer) {
                    remoteShell.sendData(byteBuffer);
                }

                @Override // com.sshtools.common.ssh.ChannelEventListener
                public void onChannelExtendedData(Channel channel, ByteBuffer byteBuffer, int i3) {
                    remoteShell.sendData(byteBuffer);
                }

                @Override // com.sshtools.common.ssh.ChannelEventListener
                public void onChannelClose(Channel channel) {
                    int exitCode = sessionChannelNGOpenSessionChannel.getExitCode();
                    remoteShell.finish(exitCode == Integer.MIN_VALUE ? null : Integer.valueOf(exitCode), null);
                }

                @Override // com.sshtools.common.ssh.ChannelEventListener
                public void onChannelError(Channel channel, Throwable th) {
                    remoteShell.finish(null, th == null ? "SSH shell error" : th.toString());
                }
            });
            if (!sessionChannelNGOpenSessionChannel.allocatePseudoTerminal("xterm-256color", i, i2).waitFor(MAX_CONNECT_TIMEOUT_MS).isSuccess()) {
                remoteShell.finish(null, "Remote server rejected PTY allocation");
                return;
            }
            if (remoteShell.finished.get()) {
                return;
            }
            if (!sessionChannelNGOpenSessionChannel.startShell().waitFor(MAX_CONNECT_TIMEOUT_MS).isSuccess()) {
                remoteShell.finish(null, "Remote server rejected the interactive shell");
            } else {
                if (remoteShell.finished.get()) {
                    return;
                }
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("type", "ready");
                jSONObject.put("sessionId", string);
                sendShellEvent(callbackContext, jSONObject, true);
            }
        } catch (SshException e) {
            sshClient.close();
            throw e;
        }
    }

    public void openShellUsingProfile(JSONArray jSONArray, CallbackContext callbackContext) {
        this.cordova.getThreadPool().execute(new AnonymousClass2(jSONArray, callbackContext));
    }

    /* JADX INFO: renamed from: com.foxdebug.sftp.Sftp$2, reason: invalid class name */
    class AnonymousClass2 implements Runnable {
        final /* synthetic */ JSONArray val$args;
        final /* synthetic */ CallbackContext val$callback;

        AnonymousClass2(JSONArray jSONArray, CallbackContext callbackContext) {
            this.val$args = jSONArray;
            this.val$callback = callbackContext;
        }

        @Override // java.lang.Runnable
        public void run() {
            final ConnectionSecurity connectionSecurity;
            Exception e;
            try {
                try {
                    String strOptString = this.val$args.optString(0);
                    int iMax = Math.max(1, this.val$args.optInt(1, 80));
                    int iMax2 = Math.max(1, this.val$args.optInt(2, 24));
                    JSONObject profile = Sftp.this.securityStore.getProfile(strOptString);
                    connectionSecurity = new ConnectionSecurity(profile.getString("hostname"), profile.optInt("port", 22));
                    try {
                        Sftp sftp = Sftp.this;
                        SshClient.SshClientBuilder sshClientBuilderBuildProfileBuilder = sftp.buildProfileBuilder(profile);
                        Objects.requireNonNull(connectionSecurity);
                        sftp.openRemoteShell(sshClientBuilderBuildProfileBuilder.onConfigure(new SshClient.OnConfiguration() { // from class: com.foxdebug.sftp.Sftp$2$$ExternalSyntheticLambda0
                            @Override // com.sshtools.client.SshClient.OnConfiguration
                            public final void accept(SshClientContext sshClientContext) {
                                connectionSecurity.configure(sshClientContext);
                            }
                        }).build(), iMax, iMax2, this.val$callback);
                    } catch (Exception e2) {
                        e = e2;
                        if (connectionSecurity == null || !connectionSecurity.report(this.val$callback)) {
                            this.val$callback.error("Failed to open SSH shell: " + Sftp.this.errMessage(e));
                            Log.e(Sftp.TAG, "Failed to open SSH shell from profile", e);
                        }
                    }
                } catch (Exception e3) {
                    connectionSecurity = null;
                    e = e3;
                }
            } catch (InvalidPassphraseException unused) {
                this.val$callback.error("Invalid passphrase for stored key");
            }
        }
    }

    public void saveProfile(final JSONArray jSONArray, final CallbackContext callbackContext) {
        this.cordova.getThreadPool().execute(new Runnable() { // from class: com.foxdebug.sftp.Sftp.3
            @Override // java.lang.Runnable
            public void run() {
                try {
                    String strOptString = jSONArray.optString(0, null);
                    if (strOptString == null || strOptString.isEmpty()) {
                        String strOptString2 = jSONArray.optString(4, "password");
                        JSONObject jSONObject = new JSONObject();
                        jSONObject.put("hostname", jSONArray.getString(1));
                        jSONObject.put("port", jSONArray.optInt(2, 22));
                        jSONObject.put("username", jSONArray.getString(3));
                        jSONObject.put("authType", strOptString2);
                        if (!"key".equals(strOptString2)) {
                            jSONObject.put("password", jSONArray.optString(5));
                        } else {
                            jSONObject.put("privateKey", Base64.encodeToString(Sftp.this.readUri(jSONArray.optString(6)), 2));
                            jSONObject.put("passphrase", jSONArray.optString(7));
                        }
                        callbackContext.success(Sftp.this.securityStore.saveProfile(strOptString, jSONObject));
                        return;
                    }
                    callbackContext.error("Legacy profile import cannot replace a saved profile");
                } catch (Exception e) {
                    callbackContext.error("Could not securely save SFTP profile: " + Sftp.this.errMessage(e));
                    Log.e(Sftp.TAG, "Could not save SFTP profile", e);
                }
            }
        });
    }

    public void editProfile(final JSONArray jSONArray, final CallbackContext callbackContext) {
        this.cordova.getThreadPool().execute(new Runnable() { // from class: com.foxdebug.sftp.Sftp.4
            @Override // java.lang.Runnable
            public void run() {
                try {
                    String strNullableProfileID = Sftp.nullableProfileID(jSONArray);
                    JSONObject profile = strNullableProfileID == null ? null : Sftp.this.securityStore.getProfile(strNullableProfileID);
                    String strTrim = jSONArray.getString(1).trim();
                    int iOptInt = jSONArray.optInt(2, 22);
                    String strTrim2 = jSONArray.getString(3).trim();
                    String strOptString = jSONArray.optString(4, "password");
                    if (strTrim.isEmpty()) {
                        throw new IllegalArgumentException("Hostname is required");
                    }
                    if (strTrim2.isEmpty()) {
                        throw new IllegalArgumentException("Username is required");
                    }
                    if (iOptInt < 1 || iOptInt > 65535) {
                        throw new IllegalArgumentException("Port must be between 1 and 65535");
                    }
                    JSONObject jSONObject = new JSONObject();
                    jSONObject.put("hostname", strTrim);
                    jSONObject.put("port", iOptInt);
                    jSONObject.put("username", strTrim2);
                    jSONObject.put("authType", strOptString);
                    if ("key".equals(strOptString)) {
                        String strOptString2 = jSONArray.optString(6);
                        if (strOptString2.isEmpty()) {
                            if (profile != null && "key".equals(profile.optString("authType"))) {
                                jSONObject.put("privateKey", profile.getString("privateKey"));
                                jSONObject.put("passphrase", profile.optString("passphrase"));
                            } else {
                                throw new IllegalArgumentException("Select a private key file");
                            }
                        } else {
                            jSONObject.put("privateKey", Base64.encodeToString(Sftp.this.readUri(strOptString2), 2));
                            jSONObject.put("passphrase", jSONArray.optString(7));
                        }
                    } else {
                        String strOptString3 = jSONArray.optString(5);
                        if (strOptString3.isEmpty() && profile != null && "password".equals(profile.optString("authType"))) {
                            strOptString3 = profile.optString("password");
                        }
                        jSONObject.put("password", strOptString3);
                    }
                    callbackContext.success(Sftp.profileInfo(Sftp.this.securityStore.saveProfile(strNullableProfileID, jSONObject), jSONObject));
                } catch (Exception e) {
                    callbackContext.error("Could not securely save SFTP profile: " + Sftp.this.errMessage(e));
                    Log.e(Sftp.TAG, "Could not save SFTP profile", e);
                }
            }
        });
    }

    public void getProfileInfo(final JSONArray jSONArray, final CallbackContext callbackContext) {
        this.cordova.getThreadPool().execute(new Runnable() { // from class: com.foxdebug.sftp.Sftp.5
            @Override // java.lang.Runnable
            public void run() {
                try {
                    String string = jSONArray.getString(0);
                    callbackContext.success(Sftp.profileInfo(string, Sftp.this.securityStore.getProfile(string)));
                } catch (Exception e) {
                    callbackContext.error("Could not read SFTP profile: " + Sftp.this.errMessage(e));
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static JSONObject profileInfo(String str, JSONObject jSONObject) throws JSONException {
        JSONObject jSONObject2 = new JSONObject();
        jSONObject2.put("profileId", str);
        jSONObject2.put("hostname", jSONObject.getString("hostname"));
        jSONObject2.put("port", jSONObject.optInt("port", 22));
        jSONObject2.put("username", jSONObject.getString("username"));
        jSONObject2.put("authType", jSONObject.optString("authType", "password"));
        return jSONObject2;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static String nullableProfileID(JSONArray jSONArray) {
        String strOptString;
        if (jSONArray.length() == 0 || jSONArray.isNull(0) || (strOptString = jSONArray.optString(0, null)) == null) {
            return null;
        }
        String strTrim = strOptString.trim();
        if (strTrim.isEmpty() || "null".equalsIgnoreCase(strTrim) || "undefined".equalsIgnoreCase(strTrim)) {
            return null;
        }
        return strTrim;
    }

    public void deleteProfile(JSONArray jSONArray, final CallbackContext callbackContext) {
        final String strOptString = jSONArray.optString(0);
        try {
            JSONObject profile = this.securityStore.getProfile(strOptString);
            final String str = profile.optString("username") + "@" + profile.optString("hostname");
            this.activity.runOnUiThread(new Runnable() { // from class: com.foxdebug.sftp.Sftp$$ExternalSyntheticLambda9
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.m206lambda$deleteProfile$9$comfoxdebugsftpSftp(str, callbackContext, strOptString);
                }
            });
        } catch (Exception e) {
            callbackContext.error("Could not delete SFTP profile: " + errMessage(e));
        }
    }

    /* JADX INFO: renamed from: lambda$deleteProfile$9$com-foxdebug-sftp-Sftp, reason: not valid java name */
    /* synthetic */ void m206lambda$deleteProfile$9$comfoxdebugsftpSftp(String str, final CallbackContext callbackContext, final String str2) {
        AlertDialog alertDialogCreate = new AlertDialog.Builder(this.activity).setTitle("Delete saved SSH credentials?").setMessage("Remove the encrypted SFTP/SSH profile for " + str + "?").setNegativeButton("Cancel", new DialogInterface.OnClickListener() { // from class: com.foxdebug.sftp.Sftp$$ExternalSyntheticLambda6
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                callbackContext.error("Profile deletion was cancelled");
            }
        }).setPositiveButton("Delete", new DialogInterface.OnClickListener() { // from class: com.foxdebug.sftp.Sftp$$ExternalSyntheticLambda7
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                this.f$0.m205lambda$deleteProfile$7$comfoxdebugsftpSftp(str2, callbackContext, dialogInterface, i);
            }
        }).create();
        alertDialogCreate.setOnCancelListener(new DialogInterface.OnCancelListener() { // from class: com.foxdebug.sftp.Sftp$$ExternalSyntheticLambda8
            @Override // android.content.DialogInterface.OnCancelListener
            public final void onCancel(DialogInterface dialogInterface) {
                callbackContext.error("Profile deletion was cancelled");
            }
        });
        alertDialogCreate.show();
    }

    /* JADX INFO: renamed from: lambda$deleteProfile$7$com-foxdebug-sftp-Sftp, reason: not valid java name */
    /* synthetic */ void m205lambda$deleteProfile$7$comfoxdebugsftpSftp(String str, CallbackContext callbackContext, DialogInterface dialogInterface, int i) {
        this.securityStore.deleteProfile(str);
        callbackContext.success();
    }

    public void writeShell(JSONArray jSONArray, CallbackContext callbackContext) {
        RemoteShell remoteShell = this.remoteShells.get(jSONArray.optString(0));
        if (remoteShell == null || remoteShell.finished.get()) {
            callbackContext.error("SSH shell is not connected");
        } else {
            remoteShell.write(jSONArray.optString(1), callbackContext);
        }
    }

    public void resizeShell(JSONArray jSONArray, CallbackContext callbackContext) {
        RemoteShell remoteShell = this.remoteShells.get(jSONArray.optString(0));
        if (remoteShell == null || remoteShell.finished.get()) {
            callbackContext.error("SSH shell is not connected");
        } else {
            remoteShell.channel.changeTerminalDimensions(Math.max(1, jSONArray.optInt(1, 80)), Math.max(1, jSONArray.optInt(2, 24)), 0, 0);
            callbackContext.success();
        }
    }

    public void closeShell(JSONArray jSONArray, CallbackContext callbackContext) {
        RemoteShell remoteShell = this.remoteShells.get(jSONArray.optString(0));
        if (remoteShell != null) {
            remoteShell.finish(null, null);
        }
        callbackContext.success();
    }

    public boolean execute(String str, JSONArray jSONArray, CallbackContext callbackContext) {
        if (!isAllowedAction(str)) {
            callbackContext.error("SFTP action is not available: " + str);
            return false;
        }
        try {
            Method declaredMethod = getClass().getDeclaredMethod(str, JSONArray.class, CallbackContext.class);
            if (declaredMethod == null) {
                return false;
            }
            declaredMethod.invoke(this, jSONArray, callbackContext);
            return true;
        } catch (NoSuchMethodException unused) {
            callbackContext.error("Method not found: " + str);
            return false;
        } catch (SecurityException e) {
            callbackContext.error("Security exception: " + e.getMessage());
            return false;
        } catch (Exception e2) {
            callbackContext.error("Exception: " + e2.getMessage());
            return false;
        }
    }

    private static boolean isAllowedAction(String str) {
        str.hashCode();
        switch (str) {
            case "closeShell":
            case "connectUsingProfile":
            case "cancelConnection":
            case "rename":
            case "resizeShell":
            case "testProfile":
            case "writeShell":
            case "deleteProfile":
            case "getProfileInfo":
            case "putFile":
            case "getFile":
            case "rm":
            case "pwd":
            case "exec":
            case "stat":
            case "openShellUsingProfile":
            case "close":
            case "lsDir":
            case "mkdir":
            case "saveProfile":
            case "isConnected":
            case "createFile":
            case "editProfile":
                return true;
            default:
                return false;
        }
    }

    public void connectUsingProfile(JSONArray jSONArray, CallbackContext callbackContext) {
        startProfileConnection(jSONArray, callbackContext, false);
    }

    public void testProfile(JSONArray jSONArray, CallbackContext callbackContext) {
        startProfileConnection(jSONArray, callbackContext, true);
    }

    public void cancelConnection(JSONArray jSONArray, CallbackContext callbackContext) {
        ConnectionAttempt connectionAttempt = this.connectionAttempts.get(jSONArray.optString(0));
        if (connectionAttempt != null) {
            connectionAttempt.cancel();
        }
        callbackContext.success();
    }

    private void startProfileConnection(final JSONArray jSONArray, CallbackContext callbackContext, final boolean z) {
        final String strOptString = jSONArray.optString(1);
        if (strOptString.isEmpty()) {
            strOptString = UUID.randomUUID().toString();
        }
        final long jMax = Math.max(MIN_CONNECT_TIMEOUT_MS, Math.min(jSONArray.optLong(2, DEFAULT_CONNECT_TIMEOUT_MS), MAX_CONNECT_TIMEOUT_MS));
        final ConnectionAttempt connectionAttempt = new ConnectionAttempt(callbackContext);
        if (this.connectionAttempts.putIfAbsent(strOptString, connectionAttempt) != null) {
            callbackContext.error("An SFTP connection with this request ID is already running");
            return;
        }
        FutureTask<Void> futureTask = new FutureTask<Void>(new Callable() { // from class: com.foxdebug.sftp.Sftp$$ExternalSyntheticLambda1
            @Override // java.util.concurrent.Callable
            public final Object call() {
                return this.f$0.m208lambda$startProfileConnection$10$comfoxdebugsftpSftp(jSONArray, jMax, z, connectionAttempt);
            }
        }) { // from class: com.foxdebug.sftp.Sftp.6
            @Override // java.util.concurrent.FutureTask
            protected void done() {
                Sftp.this.connectionAttempts.remove(strOptString, connectionAttempt);
            }
        };
        connectionAttempt.setFuture(futureTask);
        this.cordova.getThreadPool().execute(futureTask);
    }

    /* JADX INFO: renamed from: lambda$startProfileConnection$10$com-foxdebug-sftp-Sftp, reason: not valid java name */
    /* synthetic */ Void m208lambda$startProfileConnection$10$comfoxdebugsftpSftp(JSONArray jSONArray, long j, boolean z, ConnectionAttempt connectionAttempt) throws Exception {
        ConnectionSecurity connectionSecurity;
        boolean zEstablishConnection;
        String strPwd;
        String strOptString = jSONArray.optString(0);
        try {
            try {
                JSONObject profile = this.securityStore.getProfile(strOptString);
                connectionSecurity = new ConnectionSecurity(profile.getString("hostname"), profile.optInt("port", 22));
                try {
                    SshClient.SshClientBuilder sshClientBuilderBuildProfileBuilder = buildProfileBuilder(profile, j);
                    if (z) {
                        synchronized (this.connectionLock) {
                            zEstablishConnection = establishConnection(sshClientBuilderBuildProfileBuilder, strOptString, connectionSecurity, connectionAttempt);
                            if (!zEstablishConnection || connectionAttempt.isCancelled()) {
                                strPwd = null;
                            } else {
                                SftpClient sftpClient = this.sftp;
                                if (sftpClient == null) {
                                    throw new IOException("SFTP connection was closed before validation");
                                }
                                strPwd = sftpClient.pwd();
                            }
                        }
                    } else {
                        zEstablishConnection = establishConnection(sshClientBuilderBuildProfileBuilder, strOptString, connectionSecurity, connectionAttempt);
                        strPwd = null;
                    }
                    if (zEstablishConnection) {
                        if (connectionAttempt.isCancelled()) {
                            return null;
                        }
                        if (z) {
                            connectionAttempt.success(strPwd);
                        } else {
                            connectionAttempt.success();
                        }
                        return null;
                    }
                    if (connectionAttempt.isCancelled()) {
                        return null;
                    }
                    JSONObject jSONObjectTakeFailure = connectionSecurity.takeFailure();
                    if (jSONObjectTakeFailure != null) {
                        connectionAttempt.error(jSONObjectTakeFailure);
                        return null;
                    }
                    connectionAttempt.error("Failed to establish SSH connection");
                    return null;
                } catch (Exception e) {
                    e = e;
                    if (connectionAttempt.isCancelled()) {
                        return null;
                    }
                    JSONObject jSONObjectTakeFailure2 = connectionSecurity == null ? null : connectionSecurity.takeFailure();
                    if (jSONObjectTakeFailure2 != null) {
                        connectionAttempt.error(jSONObjectTakeFailure2);
                        return null;
                    }
                    connectionAttempt.error("Failed to connect SFTP profile: " + errMessage(e));
                    Log.e(TAG, "Failed to connect SFTP profile", e);
                }
            } catch (Exception e2) {
                e = e2;
                connectionSecurity = null;
            }
        } catch (InvalidPassphraseException unused) {
            if (!connectionAttempt.isCancelled()) {
                connectionAttempt.error("Invalid passphrase for stored key");
            }
        } catch (OutOfMemoryError unused2) {
            synchronized (this.connectionLock) {
                closeConnectionQuietly();
                if (!connectionAttempt.isCancelled()) {
                    connectionAttempt.error("Not enough memory to initialize SFTP");
                }
            }
        }
    }

    public void exec(final JSONArray jSONArray, final CallbackContext callbackContext) {
        this.cordova.getThreadPool().execute(new Runnable() { // from class: com.foxdebug.sftp.Sftp.7
            @Override // java.lang.Runnable
            public void run() {
                try {
                    String strOptString = jSONArray.optString(0);
                    if (Sftp.this.ssh != null) {
                        JSONObject jSONObject = new JSONObject();
                        StringBuffer stringBuffer = new StringBuffer();
                        int iExecuteCommandWithResult = Sftp.this.ssh.executeCommandWithResult(strOptString, stringBuffer);
                        String string = stringBuffer.toString();
                        jSONObject.put("code", iExecuteCommandWithResult);
                        jSONObject.put("result", string);
                        callbackContext.success(jSONObject);
                        return;
                    }
                    callbackContext.error("Not connected");
                } catch (IOException | JSONException e) {
                    callbackContext.error(Sftp.this.errMessage(e));
                }
            }
        });
    }

    public void getFile(final JSONArray jSONArray, final CallbackContext callbackContext) {
        this.cordova.getThreadPool().execute(new Runnable() { // from class: com.foxdebug.sftp.Sftp.8
            /* JADX WARN: Code duplicated, block: B:51:0x0083 A[EXC_TOP_SPLITTER, SYNTHETIC] */
            @Override // java.lang.Runnable
            public void run() {
                try {
                    String strOptString = jSONArray.optString(0);
                    String strOptString2 = jSONArray.optString(1);
                    if (Sftp.this.ssh == null || Sftp.this.sftp == null) {
                        Log.d("getFile", "ssh or sftp is null");
                        callbackContext.error("Not connected");
                        return;
                    }
                    new URI(strOptString2);
                    Uri uri = DocumentFile.fromSingleUri(Sftp.this.context, Uri.parse(strOptString2)).getUri();
                    ContentResolver contentResolver = Sftp.this.context.getContentResolver();
                    try {
                        InputStream inputStream = Sftp.this.sftp.getInputStream(strOptString);
                        try {
                            OutputStream outputStreamOpenOutputStream = contentResolver.openOutputStream(uri, "wt");
                            try {
                                byte[] bArr = new byte[32768];
                                while (true) {
                                    int i = inputStream.read(bArr);
                                    if (i == -1) {
                                        break;
                                    } else {
                                        outputStreamOpenOutputStream.write(bArr, 0, i);
                                    }
                                    if (inputStream != null) {
                                        try {
                                            inputStream.close();
                                        } catch (Throwable th) {
                                            th.addSuppressed(th);
                                        }
                                    }
                                    throw th;
                                }
                                outputStreamOpenOutputStream.flush();
                                callbackContext.success();
                                if (outputStreamOpenOutputStream != null) {
                                    outputStreamOpenOutputStream.close();
                                }
                                if (inputStream != null) {
                                    inputStream.close();
                                }
                            } catch (Throwable th2) {
                                if (outputStreamOpenOutputStream != null) {
                                    try {
                                        outputStreamOpenOutputStream.close();
                                    } catch (Throwable th3) {
                                        th2.addSuppressed(th3);
                                    }
                                }
                                throw th2;
                            }
                        } catch (Throwable th4) {
                            if (inputStream != null) {
                                inputStream.close();
                            }
                            throw th4;
                        }
                    } catch (SftpStatusException e) {
                        callbackContext.error("SFTP transfer error: " + Sftp.this.errMessage(e));
                    }
                } catch (SshException | IOException | SecurityException | URISyntaxException e2) {
                    Log.e("getFile", "Error downloading file", e2);
                    callbackContext.error("File transfer error: " + Sftp.this.errMessage(e2));
                }
            }
        });
    }

    public void putFile(final JSONArray jSONArray, final CallbackContext callbackContext) {
        this.cordova.getThreadPool().execute(new Runnable() { // from class: com.foxdebug.sftp.Sftp.9
            @Override // java.lang.Runnable
            public void run() {
                try {
                    String strOptString = jSONArray.optString(0);
                    String strOptString2 = jSONArray.optString(1);
                    if (Sftp.this.ssh != null && Sftp.this.sftp != null) {
                        if (strOptString != null && !strOptString.isEmpty()) {
                            if (strOptString2 == null || strOptString2.isEmpty()) {
                                callbackContext.error("Local filename is required");
                                return;
                            }
                            try {
                                File file = new File(new URI(strOptString2));
                                if (!file.exists() || !file.canRead()) {
                                    callbackContext.error("Local file does not exist or is not readable");
                                    return;
                                }
                                try {
                                    Sftp.this.sftp.put(file.getAbsolutePath(), strOptString);
                                    callbackContext.success("File uploaded successfully");
                                    return;
                                } catch (IOException e) {
                                    callbackContext.error("Error uploading file: " + Sftp.this.errMessage(e));
                                    return;
                                }
                            } catch (URISyntaxException e2) {
                                callbackContext.error("Invalid local URI: " + Sftp.this.errMessage(e2));
                                return;
                            }
                        }
                        callbackContext.error("Remote filename is required");
                        return;
                    }
                    callbackContext.error("Not connected");
                } catch (Exception e3) {
                    callbackContext.error(Sftp.this.errMessage(e3));
                }
            }
        });
    }

    public void lsDir(final JSONArray jSONArray, final CallbackContext callbackContext) {
        this.cordova.getThreadPool().execute(new Runnable() { // from class: com.foxdebug.sftp.Sftp.10
            @Override // java.lang.Runnable
            public void run() {
                SftpClient sftpClient = null;
                try {
                    String strOptString = jSONArray.optString(0);
                    synchronized (Sftp.this.connectionLock) {
                        try {
                            sftpClient = Sftp.this.sftp;
                            if (Sftp.this.ssh != null && Sftp.this.ssh.isConnected() && sftpClient != null && !sftpClient.isClosed()) {
                                JSONArray jSONArray2 = new JSONArray();
                                for (SftpFile sftpFile : sftpClient.ls(strOptString)) {
                                    String filename = sftpFile.getFilename();
                                    if (!filename.equals(".") && !filename.equals("..")) {
                                        SftpFileAttributes sftpFileAttributesAttributes = sftpFile.attributes();
                                        JSONObject jSONObject = new JSONObject();
                                        jSONObject.put("name", filename);
                                        jSONObject.put("exists", true);
                                        if (sftpFileAttributesAttributes != null) {
                                            String permissionsString = sftpFileAttributesAttributes.toPermissionsString();
                                            boolean z = permissionsString.charAt(1) == 'r';
                                            boolean z2 = permissionsString.charAt(2) == 'w';
                                            jSONObject.put("canRead", z);
                                            jSONObject.put("canWrite", z2);
                                            jSONObject.put("permissions", permissionsString);
                                            jSONObject.put("length", sftpFileAttributesAttributes.size());
                                            jSONObject.put("url", sftpFile.getAbsolutePath());
                                            jSONObject.put("lastModified", sftpFileAttributesAttributes.lastModifiedTime());
                                            if (permissionsString.charAt(0) == 'l') {
                                                jSONObject.put("isLink", true);
                                                try {
                                                    String symbolicLinkTarget = sftpClient.getSymbolicLinkTarget(sftpFile.getAbsolutePath());
                                                    jSONObject.put("linkTarget", symbolicLinkTarget);
                                                    SftpFileAttributes sftpFileAttributesStat = sftpClient.stat(symbolicLinkTarget);
                                                    jSONObject.put("isFile", sftpFileAttributesStat.isFile());
                                                    jSONObject.put("isDirectory", sftpFileAttributesStat.isDirectory());
                                                } catch (SftpStatusException | SshException unused) {
                                                    jSONObject.put("isFile", false);
                                                    jSONObject.put("isDirectory", false);
                                                    jSONObject.put("isLink", false);
                                                }
                                            } else {
                                                jSONObject.put("isLink", false);
                                                jSONObject.put("isDirectory", sftpFileAttributesAttributes.isDirectory());
                                                jSONObject.put("isFile", sftpFileAttributesAttributes.isFile());
                                            }
                                        }
                                        jSONArray2.put(jSONObject);
                                    }
                                }
                                callbackContext.success(jSONArray2);
                                return;
                            }
                            callbackContext.error("Not connected");
                        } catch (Throwable th) {
                            throw th;
                        }
                    }
                } catch (SftpStatusException e) {
                    e = e;
                    callbackContext.error(Sftp.this.errMessage(e));
                } catch (SshException e2) {
                    e = e2;
                    Sftp.this.invalidateSftpConnection(sftpClient, e);
                    callbackContext.error("SFTP connection was interrupted. Reconnect and try again.");
                } catch (RuntimeException e3) {
                    e = e3;
                    Sftp.this.invalidateSftpConnection(sftpClient, e);
                    callbackContext.error("SFTP connection was interrupted. Reconnect and try again.");
                } catch (JSONException e4) {
                    e = e4;
                    callbackContext.error(Sftp.this.errMessage(e));
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void invalidateSftpConnection(SftpClient sftpClient, Exception exc) {
        synchronized (this.connectionLock) {
            if (sftpClient != null) {
                if (this.sftp == sftpClient) {
                    Log.w(TAG, "Invalidating failed SFTP connection", exc);
                    closeConnectionQuietly();
                }
            }
        }
    }

    public void stat(final JSONArray jSONArray, final CallbackContext callbackContext) {
        this.cordova.getThreadPool().execute(new Runnable() { // from class: com.foxdebug.sftp.Sftp.11
            @Override // java.lang.Runnable
            public void run() {
                try {
                    try {
                        String strSanitizePath = Sftp.this.sanitizePath(jSONArray.optString(0));
                        if (Sftp.this.ssh != null && Sftp.this.sftp != null) {
                            URI uri = new URI(strSanitizePath);
                            JSONObject jSONObject = new JSONObject();
                            try {
                                SftpFileAttributes sftpFileAttributesStat = Sftp.this.sftp.stat(uri.getPath());
                                if (sftpFileAttributesStat != null) {
                                    String permissionsString = sftpFileAttributesStat.toPermissionsString();
                                    boolean z = permissionsString.charAt(1) == 'r';
                                    boolean z2 = permissionsString.charAt(2) == 'w';
                                    jSONObject.put("exists", true);
                                    jSONObject.put("canRead", z);
                                    jSONObject.put("canWrite", z2);
                                    jSONObject.put("isLink", sftpFileAttributesStat.isLink());
                                    jSONObject.put("isDirectory", sftpFileAttributesStat.isDirectory());
                                    jSONObject.put("isFile", sftpFileAttributesStat.isFile());
                                    jSONObject.put("length", sftpFileAttributesStat.size());
                                    jSONObject.put("permissions", sftpFileAttributesStat.toPermissionsString());
                                    jSONObject.put("lastModified", sftpFileAttributesStat.lastModifiedTime());
                                    String[] strArrSplit = uri.getPath().split(AbstractFilePath.SEPARATOR);
                                    jSONObject.put("name", strArrSplit[strArrSplit.length - 1]);
                                    jSONObject.put("url", uri.getPath());
                                    if (permissionsString.charAt(0) == 'l') {
                                        jSONObject.put("isLink", true);
                                        try {
                                            String symbolicLinkTarget = Sftp.this.sftp.getSymbolicLinkTarget(uri.getPath());
                                            jSONObject.put("linkTarget", symbolicLinkTarget);
                                            SftpFileAttributes sftpFileAttributesStat2 = Sftp.this.sftp.stat(symbolicLinkTarget);
                                            jSONObject.put("isFile", sftpFileAttributesStat2.isFile());
                                            jSONObject.put("isDirectory", sftpFileAttributesStat2.isDirectory());
                                        } catch (SftpStatusException | SshException unused) {
                                            jSONObject.put("isFile", false);
                                            jSONObject.put("isDirectory", false);
                                            jSONObject.put("isLink", false);
                                            jSONObject.put("exists", false);
                                        }
                                    } else {
                                        jSONObject.put("isLink", false);
                                        jSONObject.put("isDirectory", sftpFileAttributesStat.isDirectory());
                                        jSONObject.put("isFile", sftpFileAttributesStat.isFile());
                                    }
                                }
                            } catch (SftpStatusException unused2) {
                                jSONObject.put("exists", false);
                                jSONObject.put("url", uri.getPath());
                            }
                            callbackContext.success(jSONObject);
                            return;
                        }
                        callbackContext.error("Not connected");
                    } catch (SshException e) {
                        e = e;
                        callbackContext.error(Sftp.this.errMessage(e));
                    }
                } catch (URISyntaxException e2) {
                    e = e2;
                    callbackContext.error(Sftp.this.errMessage(e));
                } catch (JSONException e3) {
                    e = e3;
                    callbackContext.error(Sftp.this.errMessage(e));
                }
            }
        });
    }

    public void mkdir(final JSONArray jSONArray, final CallbackContext callbackContext) {
        this.cordova.getThreadPool().execute(new Runnable() { // from class: com.foxdebug.sftp.Sftp.12
            @Override // java.lang.Runnable
            public void run() {
                try {
                    String strOptString = jSONArray.optString(0);
                    if (Sftp.this.ssh != null && Sftp.this.sftp != null) {
                        Sftp.this.sftp.mkdir(strOptString);
                        callbackContext.success();
                    } else {
                        callbackContext.error("Not connected");
                    }
                } catch (SftpStatusException | SshException e) {
                    callbackContext.error(Sftp.this.errMessage(e));
                }
            }
        });
    }

    public void rm(final JSONArray jSONArray, final CallbackContext callbackContext) {
        this.cordova.getThreadPool().execute(new Runnable() { // from class: com.foxdebug.sftp.Sftp.13
            @Override // java.lang.Runnable
            public void run() {
                try {
                    String strOptString = jSONArray.optString(0);
                    boolean zOptBoolean = jSONArray.optBoolean(1, false);
                    boolean zOptBoolean2 = jSONArray.optBoolean(2, false);
                    if (Sftp.this.ssh != null && Sftp.this.sftp != null) {
                        Sftp.this.sftp.rm(strOptString, zOptBoolean, zOptBoolean2);
                        callbackContext.success();
                    } else {
                        callbackContext.error("Not connected");
                    }
                } catch (SftpStatusException | SshException e) {
                    callbackContext.error(Sftp.this.errMessage(e));
                }
            }
        });
    }

    public void createFile(final JSONArray jSONArray, final CallbackContext callbackContext) {
        this.cordova.getThreadPool().execute(new Runnable() { // from class: com.foxdebug.sftp.Sftp.14
            @Override // java.lang.Runnable
            public void run() {
                ByteArrayInputStream byteArrayInputStream;
                try {
                    try {
                        String strOptString = jSONArray.optString(0);
                        String strOptString2 = jSONArray.optString(1, "");
                        if (Sftp.this.ssh != null && Sftp.this.sftp != null) {
                            try {
                                SftpFileAttributes sftpFileAttributesStat = Sftp.this.sftp.stat(strOptString);
                                if (sftpFileAttributesStat != null && sftpFileAttributesStat.isFile()) {
                                    callbackContext.error("File already exists");
                                    return;
                                }
                            } catch (SftpStatusException unused) {
                            }
                            if (strOptString2.isEmpty()) {
                                byteArrayInputStream = new ByteArrayInputStream(new byte[0]);
                            } else {
                                byteArrayInputStream = new ByteArrayInputStream(strOptString2.getBytes(StandardCharsets.UTF_8));
                            }
                            Sftp.this.sftp.put(byteArrayInputStream, strOptString);
                            callbackContext.success();
                            return;
                        }
                        callbackContext.error("Not connected");
                    } catch (SftpStatusException e) {
                        e = e;
                        callbackContext.error(Sftp.this.errMessage(e));
                    }
                } catch (TransferCancelledException e2) {
                    e = e2;
                    callbackContext.error(Sftp.this.errMessage(e));
                } catch (SshException e3) {
                    e = e3;
                    callbackContext.error(Sftp.this.errMessage(e));
                }
            }
        });
    }

    public void rename(final JSONArray jSONArray, final CallbackContext callbackContext) {
        this.cordova.getThreadPool().execute(new Runnable() { // from class: com.foxdebug.sftp.Sftp.15
            @Override // java.lang.Runnable
            public void run() {
                try {
                    String strOptString = jSONArray.optString(0);
                    String strOptString2 = jSONArray.optString(1);
                    if (Sftp.this.ssh != null && Sftp.this.sftp != null) {
                        Sftp.this.sftp.rename(strOptString, strOptString2);
                        callbackContext.success();
                    } else {
                        callbackContext.error("Not connected");
                    }
                } catch (SftpStatusException | SshException e) {
                    callbackContext.error(Sftp.this.errMessage(e));
                }
            }
        });
    }

    public void pwd(JSONArray jSONArray, final CallbackContext callbackContext) {
        this.cordova.getThreadPool().execute(new Runnable() { // from class: com.foxdebug.sftp.Sftp.16
            @Override // java.lang.Runnable
            public void run() {
                try {
                    if (Sftp.this.ssh != null && Sftp.this.sftp != null) {
                        callbackContext.success(Sftp.this.sftp.pwd());
                    } else {
                        callbackContext.error("Not connected");
                    }
                } catch (SftpStatusException | SshException e) {
                    callbackContext.error(Sftp.this.errMessage(e));
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String sanitizePath(String str) {
        try {
            return URLEncoder.encode(URLDecoder.decode(str, StandardCharsets.UTF_8.toString()), StandardCharsets.UTF_8.toString()).replace("+", "%20").replace("%2F", AbstractFilePath.SEPARATOR).replace("%5C", "\\");
        } catch (UnsupportedEncodingException unused) {
            return str;
        }
    }

    public void close(JSONArray jSONArray, final CallbackContext callbackContext) {
        this.cordova.getThreadPool().execute(new Runnable() { // from class: com.foxdebug.sftp.Sftp.17
            @Override // java.lang.Runnable
            public void run() {
                synchronized (Sftp.this.connectionLock) {
                    if (Sftp.this.ssh == null && Sftp.this.sftp == null) {
                        callbackContext.success();
                        return;
                    }
                    Sftp.this.closeConnectionQuietly();
                    callbackContext.success();
                }
            }
        });
    }

    public void isConnected(JSONArray jSONArray, final CallbackContext callbackContext) {
        this.cordova.getThreadPool().execute(new Runnable() { // from class: com.foxdebug.sftp.Sftp.18
            @Override // java.lang.Runnable
            public void run() {
                synchronized (Sftp.this.connectionLock) {
                    if (Sftp.this.ssh != null && Sftp.this.ssh.isConnected() && Sftp.this.sftp != null && !Sftp.this.sftp.isClosed()) {
                        callbackContext.success(Sftp.this.connectionID);
                    } else {
                        callbackContext.success(0);
                    }
                }
            }
        });
    }

    public String errMessage(Exception exc) {
        String message = exc.getMessage();
        return (message == null || message.equals("")) ? exc.toString() : message;
    }
}