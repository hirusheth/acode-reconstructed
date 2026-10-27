package org.apache.cordova.engine;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import org.apache.cordova.CordovaDialogsHelper;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.LOG;

/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes2.dex */
public class SystemWebChromeClient extends WebChromeClient {
    private static final int FILECHOOSER_RESULTCODE = 5173;
    private static final String LOG_TAG = "SystemWebChromeClient";
    private final CordovaDialogsHelper dialogsHelper;
    private View mVideoProgressView;
    protected final SystemWebViewEngine parentEngine;
    private final ActivityResultLauncher<String[]> permissionLauncher;
    private PermissionListener permissionListener;

    /* JADX INFO: Access modifiers changed from: private */
    interface PermissionListener {
        void onPermissionSelect(Boolean bool);
    }

    public SystemWebChromeClient(SystemWebViewEngine systemWebViewEngine) {
        this.parentEngine = systemWebViewEngine;
        this.dialogsHelper = new CordovaDialogsHelper(systemWebViewEngine.webView.getContext());
        this.permissionLauncher = systemWebViewEngine.cordova.getActivity().registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback() { // from class: org.apache.cordova.engine.SystemWebChromeClient$$ExternalSyntheticLambda1
            public final void onActivityResult(Object obj) {
                this.f$0.m639lambda$new$0$orgapachecordovaengineSystemWebChromeClient((Map) obj);
            }
        });
    }

    /* JADX INFO: renamed from: lambda$new$0$org-apache-cordova-engine-SystemWebChromeClient, reason: not valid java name */
    /* synthetic */ void m639lambda$new$0$orgapachecordovaengineSystemWebChromeClient(Map map) {
        if (this.permissionListener != null) {
            Iterator it = map.entrySet().iterator();
            boolean z = true;
            while (it.hasNext()) {
                if (!((Boolean) ((Map.Entry) it.next()).getValue()).booleanValue()) {
                    z = false;
                }
            }
            this.permissionListener.onPermissionSelect(Boolean.valueOf(z));
        }
    }

    @Override // android.webkit.WebChromeClient
    public boolean onJsAlert(WebView webView, String str, String str2, final JsResult jsResult) {
        this.dialogsHelper.showAlert(str2, new CordovaDialogsHelper.Result() { // from class: org.apache.cordova.engine.SystemWebChromeClient$$ExternalSyntheticLambda2
            @Override // org.apache.cordova.CordovaDialogsHelper.Result
            public final void gotResult(boolean z, String str3) {
                SystemWebChromeClient.lambda$onJsAlert$1(jsResult, z, str3);
            }
        });
        return true;
    }

    static /* synthetic */ void lambda$onJsAlert$1(JsResult jsResult, boolean z, String str) {
        if (z) {
            jsResult.confirm();
        } else {
            jsResult.cancel();
        }
    }

    @Override // android.webkit.WebChromeClient
    public boolean onJsConfirm(WebView webView, String str, String str2, final JsResult jsResult) {
        this.dialogsHelper.showConfirm(str2, new CordovaDialogsHelper.Result() { // from class: org.apache.cordova.engine.SystemWebChromeClient$$ExternalSyntheticLambda0
            @Override // org.apache.cordova.CordovaDialogsHelper.Result
            public final void gotResult(boolean z, String str3) {
                SystemWebChromeClient.lambda$onJsConfirm$2(jsResult, z, str3);
            }
        });
        return true;
    }

    static /* synthetic */ void lambda$onJsConfirm$2(JsResult jsResult, boolean z, String str) {
        if (z) {
            jsResult.confirm();
        } else {
            jsResult.cancel();
        }
    }

    @Override // android.webkit.WebChromeClient
    public boolean onJsPrompt(WebView webView, String str, String str2, String str3, final JsPromptResult jsPromptResult) {
        String strPromptOnJsPrompt = this.parentEngine.bridge.promptOnJsPrompt(str, str2, str3);
        if (strPromptOnJsPrompt != null) {
            jsPromptResult.confirm(strPromptOnJsPrompt);
            return true;
        }
        this.dialogsHelper.showPrompt(str2, str3, new CordovaDialogsHelper.Result() { // from class: org.apache.cordova.engine.SystemWebChromeClient$$ExternalSyntheticLambda3
            @Override // org.apache.cordova.CordovaDialogsHelper.Result
            public final void gotResult(boolean z, String str4) {
                SystemWebChromeClient.lambda$onJsPrompt$3(jsPromptResult, z, str4);
            }
        });
        return true;
    }

    static /* synthetic */ void lambda$onJsPrompt$3(JsPromptResult jsPromptResult, boolean z, String str) {
        if (z) {
            jsPromptResult.confirm(str);
        } else {
            jsPromptResult.cancel();
        }
    }

    @Override // android.webkit.WebChromeClient
    public void onExceededDatabaseQuota(String str, String str2, long j, long j2, long j3, WebStorage.QuotaUpdater quotaUpdater) {
        LOG.d(LOG_TAG, "onExceededDatabaseQuota estimatedSize: %d  currentQuota: %d  totalUsedQuota: %d", Long.valueOf(j2), Long.valueOf(j), Long.valueOf(j3));
        quotaUpdater.updateQuota(104857600L);
    }

    @Override // android.webkit.WebChromeClient
    public void onGeolocationPermissionsShowPrompt(String str, GeolocationPermissions.Callback callback) {
        super.onGeolocationPermissionsShowPrompt(str, callback);
        callback.invoke(str, true, false);
        CordovaPlugin plugin = this.parentEngine.pluginManager.getPlugin("Geolocation");
        if (plugin == null || plugin.hasPermisssion()) {
            return;
        }
        plugin.requestPermissions(0);
    }

    @Override // android.webkit.WebChromeClient
    public void onShowCustomView(View view, WebChromeClient.CustomViewCallback customViewCallback) {
        this.parentEngine.getCordovaWebView().showCustomView(view, customViewCallback);
    }

    @Override // android.webkit.WebChromeClient
    public void onHideCustomView() {
        this.parentEngine.getCordovaWebView().hideCustomView();
    }

    @Override // android.webkit.WebChromeClient
    public View getVideoLoadingProgressView() {
        if (this.mVideoProgressView == null) {
            LinearLayout linearLayout = new LinearLayout(this.parentEngine.getView().getContext());
            linearLayout.setOrientation(1);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(-2, -2);
            layoutParams.addRule(13);
            linearLayout.setLayoutParams(layoutParams);
            ProgressBar progressBar = new ProgressBar(this.parentEngine.getView().getContext());
            LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-2, -2);
            layoutParams2.gravity = 17;
            progressBar.setLayoutParams(layoutParams2);
            linearLayout.addView(progressBar);
            this.mVideoProgressView = linearLayout;
        }
        return this.mVideoProgressView;
    }

    @Override // android.webkit.WebChromeClient
    public boolean onShowFileChooser(WebView webView, final ValueCallback<Uri[]> valueCallback, WebChromeClient.FileChooserParams fileChooserParams) {
        Intent intent;
        final Uri uriCreateUriForFile;
        Intent intentCreateIntent = fileChooserParams.createIntent();
        boolean z = false;
        if (fileChooserParams.getMode() == 1) {
            z = true;
        }
        intentCreateIntent.putExtra("android.intent.extra.ALLOW_MULTIPLE", z);
        String[] acceptTypes = fileChooserParams.getAcceptTypes();
        if (acceptTypes.length > 1) {
            intentCreateIntent.setType("*/*");
            intentCreateIntent.putExtra("android.intent.extra.MIME_TYPES", acceptTypes);
        }
        if (fileChooserParams.isCaptureEnabled()) {
            intent = new Intent("android.media.action.IMAGE_CAPTURE");
            Context context = this.parentEngine.getView().getContext();
            if (context.getPackageManager().hasSystemFeature("android.hardware.camera.any") && intent.resolveActivity(context.getPackageManager()) != null) {
                try {
                    File fileCreateTempFile = createTempFile(context);
                    LOG.d(LOG_TAG, "Temporary photo capture file: " + fileCreateTempFile);
                    uriCreateUriForFile = createUriForFile(context, fileCreateTempFile);
                    try {
                        LOG.d(LOG_TAG, "Temporary photo capture URI: " + uriCreateUriForFile);
                        intent.putExtra("output", uriCreateUriForFile);
                    } catch (IOException e) {
                        e = e;
                        LOG.e(LOG_TAG, "Unable to create temporary file for photo capture", e);
                        intent = null;
                    }
                } catch (IOException e2) {
                    e = e2;
                    uriCreateUriForFile = null;
                }
            } else {
                LOG.w(LOG_TAG, "Device does not support photo capture");
                intent = null;
                uriCreateUriForFile = null;
            }
        } else {
            intent = null;
            uriCreateUriForFile = null;
        }
        Intent intentCreateChooser = Intent.createChooser(intentCreateIntent, null);
        if (intent != null) {
            intentCreateChooser.putExtra("android.intent.extra.INITIAL_INTENTS", new Intent[]{intent});
        }
        try {
            LOG.i(LOG_TAG, "Starting intent for file chooser");
            this.parentEngine.cordova.startActivityForResult(new CordovaPlugin() { // from class: org.apache.cordova.engine.SystemWebChromeClient.1
                /* JADX WARN: Code duplicated, block: B:24:0x00a5  */
                @Override // org.apache.cordova.CordovaPlugin
                public void onActivityResult(int i, int i2, Intent intent2) {
                    Uri[] uriArr;
                    if (i2 == -1) {
                        ArrayList arrayList = new ArrayList();
                        if (intent2 != null && intent2.getData() != null) {
                            LOG.v(SystemWebChromeClient.LOG_TAG, "Adding file (single): " + intent2.getData());
                            arrayList.add(intent2.getData());
                        } else if (uriCreateUriForFile != null) {
                            LOG.v(SystemWebChromeClient.LOG_TAG, "Adding camera capture: " + uriCreateUriForFile);
                            arrayList.add(uriCreateUriForFile);
                        } else if (intent2 != null && intent2.getClipData() != null) {
                            ClipData clipData = intent2.getClipData();
                            int itemCount = clipData.getItemCount();
                            for (int i3 = 0; i3 < itemCount; i3++) {
                                Uri uri = clipData.getItemAt(i3).getUri();
                                LOG.v(SystemWebChromeClient.LOG_TAG, "Adding file (multiple): " + uri);
                                if (uri != null) {
                                    arrayList.add(uri);
                                }
                            }
                        }
                        if (arrayList.isEmpty()) {
                            uriArr = null;
                        } else {
                            LOG.d(SystemWebChromeClient.LOG_TAG, "Receive file chooser URL: " + arrayList);
                            uriArr = (Uri[]) arrayList.toArray(new Uri[0]);
                        }
                    } else {
                        uriArr = null;
                    }
                    valueCallback.onReceiveValue(uriArr);
                }
            }, intentCreateChooser, FILECHOOSER_RESULTCODE);
        } catch (ActivityNotFoundException e3) {
            LOG.w(LOG_TAG, "No activity found to handle file chooser intent.", e3);
            valueCallback.onReceiveValue(null);
        }
        return true;
    }

    private File createTempFile(Context context) throws IOException {
        return File.createTempFile("temp", ".jpg", context.getCacheDir());
    }

    private Uri createUriForFile(Context context, File file) throws IOException {
        return FileProvider.getUriForFile(context, context.getPackageName() + ".cdv.core.file.provider", file);
    }

    @Override // android.webkit.WebChromeClient
    public void onPermissionRequest(final PermissionRequest permissionRequest) {
        LOG.d(LOG_TAG, "onPermissionRequest: " + Arrays.toString(permissionRequest.getResources()));
        ArrayList arrayList = new ArrayList();
        if (Arrays.asList(permissionRequest.getResources()).contains("android.webkit.resource.VIDEO_CAPTURE")) {
            arrayList.add("android.permission.CAMERA");
        }
        if (Arrays.asList(permissionRequest.getResources()).contains("android.webkit.resource.AUDIO_CAPTURE")) {
            arrayList.add("android.permission.MODIFY_AUDIO_SETTINGS");
            arrayList.add("android.permission.RECORD_AUDIO");
        }
        if (!arrayList.isEmpty()) {
            String[] strArr = (String[]) arrayList.toArray(new String[0]);
            this.permissionListener = new PermissionListener() { // from class: org.apache.cordova.engine.SystemWebChromeClient$$ExternalSyntheticLambda4
                @Override // org.apache.cordova.engine.SystemWebChromeClient.PermissionListener
                public final void onPermissionSelect(Boolean bool) {
                    SystemWebChromeClient.lambda$onPermissionRequest$4(permissionRequest, bool);
                }
            };
            this.permissionLauncher.launch(strArr);
            return;
        }
        permissionRequest.grant(permissionRequest.getResources());
    }

    static /* synthetic */ void lambda$onPermissionRequest$4(PermissionRequest permissionRequest, Boolean bool) {
        if (bool.booleanValue()) {
            permissionRequest.grant(permissionRequest.getResources());
        } else {
            permissionRequest.deny();
        }
    }

    public void destroyLastDialog() {
        this.dialogsHelper.destroyLastDialog();
    }
}