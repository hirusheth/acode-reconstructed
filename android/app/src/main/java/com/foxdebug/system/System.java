package com.foxdebug.system;

import android.R;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.webkit.WebView;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.core.view.MotionEventCompat;
import androidx.core.view.accessibility.AccessibilityEventCompat;
import androidx.documentfile.provider.DocumentFile;
import com.sshtools.common.ssh.components.jce.JCEAlgorithms;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.concurrent.ExecutorService;
import kotlin.UByte;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes.dex */
public class System extends CordovaPlugin {
    private static final String TAG = "SystemPlugin";
    private Activity activity;
    private Context context;
    private String fileProviderAuthority;
    private CallbackContext intentHandler;
    private CallbackContext requestPermissionCallback;
    private RewardPassManager rewardPassManager;
    private Ui.Theme theme;
    private CordovaWebView webView;
    private int REQ_PERMISSIONS = 1;
    private int REQ_PERMISSION = 2;
    private int systemBarColor = -16777216;

    private int deprecatedFlagUiLightStatusBar() {
        return 8192;
    }

    public void initialize(CordovaInterface cordovaInterface, CordovaWebView cordovaWebView) {
        super.initialize(cordovaInterface, cordovaWebView);
        this.context = cordovaInterface.getContext();
        this.activity = cordovaInterface.getActivity();
        this.webView = cordovaWebView;
        this.rewardPassManager = new RewardPassManager(this.context);
        this.activity.runOnUiThread(new Runnable() { // from class: com.foxdebug.system.System.1
            @Override // java.lang.Runnable
            public void run() {
                System.this.setNativeContextMenuDisabled(false);
            }
        });
    }

    /* JADX WARN: Failed to restore switch over string. Please report as a decompilation issue */
    /* JADX WARN: Multi-variable type inference failed */
    public boolean execute(String str, final JSONArray jSONArray, final CallbackContext callbackContext) throws JSONException {
        boolean zIsExternalStorageManager;
        byte b;
        final String strOptString = jSONArray.optString(0);
        String strOptString2 = jSONArray.optString(1);
        String strOptString3 = jSONArray.optString(2);
        String strOptString4 = jSONArray.optString(3);
        String strOptString5 = jSONArray.optString(4);
        String strOptString6 = jSONArray.optString(5);
        str.hashCode();
        byte b2 = -1;
        switch (str.hashCode()) {
            case -1997789784:
                if (str.equals("set-input-type")) {
                    b2 = 0;
                }
                break;
            case -1904182813:
                if (str.equals("open-in-browser")) {
                    b2 = 1;
                }
                break;
            case -1806355567:
                if (str.equals("is-powersave-mode")) {
                    b2 = 2;
                }
                break;
            case -1776457347:
                if (str.equals("get-global-setting")) {
                    b2 = 3;
                }
                break;
            case -1582038612:
                if (str.equals("shareText")) {
                    b2 = 4;
                }
                break;
            case -1551633219:
                if (str.equals("listChildren")) {
                    b2 = 5;
                }
                break;
            case -1545217310:
                if (str.equals("has-permission")) {
                    b = 6;
                    b2 = b;
                }
                break;
            case -1406334548:
                if (str.equals("writeText")) {
                    b = 7;
                    b2 = b;
                }
                break;
            case -1335717394:
                if (str.equals("decode")) {
                    b = 8;
                    b2 = b;
                }
                break;
            case -1298776554:
                if (str.equals("encode")) {
                    b = 9;
                    b2 = b;
                }
                break;
            case -1072489436:
                if (str.equals("mkdirs")) {
                    b = 10;
                    b2 = b;
                }
                break;
            case -926794121:
                if (str.equals("getRewardStatus")) {
                    b = 11;
                    b2 = b;
                }
                break;
            case -908435507:
                if (str.equals("request-permission")) {
                    b = 12;
                    b2 = b;
                }
                break;
            case -623309455:
                if (str.equals("get-app-info")) {
                    b = 13;
                    b2 = b;
                }
                break;
            case -542559259:
                if (str.equals("getParentPath")) {
                    b = 14;
                    b2 = b;
                }
                break;
            case -351936028:
                if (str.equals("get-cordova-intent")) {
                    b = 15;
                    b2 = b;
                }
                break;
            case -251247454:
                if (str.equals("get-webkit-info")) {
                    b = 16;
                    b2 = b;
                }
                break;
            case -75679540:
                if (str.equals("getArch")) {
                    b = 17;
                    b2 = b;
                }
                break;
            case 19441874:
                if (str.equals("getInstaller")) {
                    b = 18;
                    b2 = b;
                }
                break;
            case 34061167:
                if (str.equals("remove-shortcut")) {
                    b = 19;
                    b2 = b;
                }
                break;
            case 94113703:
                if (str.equals("file-action")) {
                    b = 20;
                    b2 = b;
                }
                break;
            case 140960307:
                if (str.equals("getNativeLibraryPath")) {
                    b = 21;
                    b2 = b;
                }
                break;
            case 188086917:
                if (str.equals("get-available-encodings")) {
                    b = 22;
                    b2 = b;
                }
                break;
            case 262567396:
                if (str.equals("set-intent-handler")) {
                    b = 23;
                    b2 = b;
                }
                break;
            case 363064411:
                if (str.equals("isManageExternalStorageDeclared")) {
                    b = 24;
                    b2 = b;
                }
                break;
            case 503371429:
                if (str.equals("createSymlink")) {
                    b = 25;
                    b2 = b;
                }
                break;
            case 545259783:
                if (str.equals("launch-app")) {
                    b = 26;
                    b2 = b;
                }
                break;
            case 656525039:
                if (str.equals("extractAsset")) {
                    b = 27;
                    b2 = b;
                }
                break;
            case 711277584:
                if (str.equals("checksumText")) {
                    b = 28;
                    b2 = b;
                }
                break;
            case 714625003:
                if (str.equals("redeemReward")) {
                    b = 29;
                    b2 = b;
                }
                break;
            case 906001155:
                if (str.equals("get-android-version")) {
                    b2 = 30;
                }
                break;
            case 925519137:
                if (str.equals("requestStorageManager")) {
                    b = 31;
                    b2 = b;
                }
                break;
            case 1041466358:
                if (str.equals("compare-file-text")) {
                    b = 32;
                    b2 = b;
                }
                break;
            case 1343252492:
                if (str.equals("getFilesDir")) {
                    b = 33;
                    b2 = b;
                }
                break;
            case 1367402175:
                if (str.equals("get-configuration")) {
                    b = 34;
                    b2 = b;
                }
                break;
            case 1404291320:
                if (str.equals("fileExists")) {
                    b = 35;
                    b2 = b;
                }
                break;
            case 1406195155:
                if (str.equals("hasGrantedStorageManager")) {
                    b = 36;
                    b2 = b;
                }
                break;
            case 1495345788:
                if (str.equals("copyToUri")) {
                    b = 37;
                    b2 = b;
                }
                break;
            case 1605356546:
                if (str.equals("clear-cache")) {
                    b = 38;
                    b2 = b;
                }
                break;
            case 1655809406:
                if (str.equals("pin-shortcut")) {
                    b = 39;
                    b2 = b;
                }
                break;
            case 1673351137:
                if (str.equals("set-native-context-menu-disabled")) {
                    b = 40;
                    b2 = b;
                }
                break;
            case 1764172231:
                if (str.equals("deleteFile")) {
                    b = 41;
                    b2 = b;
                }
                break;
            case 1903270470:
                if (str.equals("request-permissions")) {
                    b = 42;
                    b2 = b;
                }
                break;
            case 1946938971:
                if (str.equals("set-ui-theme")) {
                    b = 43;
                    b2 = b;
                }
                break;
            case 1984555027:
                if (str.equals("setExec")) {
                    b = 44;
                    b2 = b;
                }
                break;
            case 2056892926:
                if (str.equals("compare-texts")) {
                    b = 45;
                    b2 = b;
                }
                break;
            case 2079525874:
                if (str.equals("add-shortcut")) {
                    b = 46;
                    b2 = b;
                }
                break;
            case 2089619263:
                if (str.equals("pin-file-shortcut")) {
                    b = 47;
                    b2 = b;
                }
                break;
        }
        String str2 = "false";
        switch (b2) {
            case 0:
                setInputType(strOptString);
                callbackContext.success();
                return true;
            case 1:
            case 2:
            case 3:
            case 6:
            case 8:
            case 9:
            case 12:
            case 13:
            case 16:
            case 18:
            case 19:
            case 20:
            case 22:
            case 26:
            case 27:
            case 28:
            case 30:
            case 32:
            case MotionEventCompat.AXIS_GENERIC_6 /* 37 */:
            case MotionEventCompat.AXIS_GENERIC_8 /* 39 */:
            case MotionEventCompat.AXIS_GENERIC_11 /* 42 */:
            case MotionEventCompat.AXIS_GENERIC_14 /* 45 */:
            case MotionEventCompat.AXIS_GENERIC_15 /* 46 */:
            case MotionEventCompat.AXIS_GENERIC_16 /* 47 */:
                this.cordova.getThreadPool().execute(new AnonymousClass5(str, jSONArray, callbackContext, strOptString, strOptString2, strOptString3, strOptString4, strOptString5, strOptString6));
                return true;
            case 4:
                final String string = jSONArray.getString(0);
                this.cordova.getActivity().runOnUiThread(new Runnable() { // from class: com.foxdebug.system.System$$ExternalSyntheticLambda1
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.m253lambda$execute$0$comfoxdebugsystemSystem(string, callbackContext);
                    }
                });
                return true;
            case 5:
                callbackContext.success(listChildren(jSONArray.getString(0)));
                return true;
            case 7:
                try {
                    Files.write(Paths.get(jSONArray.getString(0), new String[0]), Collections.singleton(jSONArray.getString(1)), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    callbackContext.success("File written successfully");
                    break;
                } catch (Exception e) {
                    callbackContext.error("Failed to write file: " + e.getMessage());
                }
                return true;
            case 10:
                if (new File(jSONArray.getString(0)).mkdirs()) {
                    callbackContext.success();
                } else {
                    callbackContext.error("mkdirs failed");
                }
                return true;
            case 11:
                callbackContext.success(this.rewardPassManager.getRewardStatus());
                return true;
            case 14:
                callbackContext.success(getParentPath(jSONArray.getString(0)));
                return true;
            case 15:
                getCordovaIntent(callbackContext);
                return true;
            case 17:
                callbackContext.success(Build.SUPPORTED_ABIS[0]);
                return true;
            case 21:
                callbackContext.success(getNativeLibraryPath());
                return true;
            case 23:
                setIntentHandler(callbackContext);
                return true;
            case 24:
                try {
                    String[] strArr = this.context.getPackageManager().getPackageInfo(this.context.getPackageName(), 4096).requestedPermissions;
                    if (strArr != null) {
                        for (String str3 : strArr) {
                            if (str3.equals("android.permission.MANAGE_EXTERNAL_STORAGE")) {
                                str2 = "true";
                            }
                        }
                    }
                    callbackContext.success(str2);
                    break;
                } catch (PackageManager.NameNotFoundException e2) {
                    e2.printStackTrace();
                    callbackContext.error(e2.toString());
                }
                return true;
            case 25:
                callbackContext.success(createSymlink(jSONArray.getString(0), jSONArray.getString(1)) ? 1 : 0);
                return true;
            case 29:
                callbackContext.success(this.rewardPassManager.redeemReward(jSONArray.getString(0)));
                return true;
            case 31:
                if (Build.VERSION.SDK_INT >= 30) {
                    try {
                        Intent intent = new Intent("android.settings.MANAGE_APP_ALL_FILES_ACCESS_PERMISSION");
                        intent.setData(Uri.parse("package:" + this.context.getPackageName()));
                        intent.addFlags(268435456);
                        this.context.startActivity(intent);
                        callbackContext.success("true");
                    } catch (Exception unused) {
                        Intent intent2 = new Intent("android.settings.MANAGE_ALL_FILES_ACCESS_PERMISSION");
                        intent2.addFlags(268435456);
                        this.context.startActivity(intent2);
                        callbackContext.success("true");
                    }
                    break;
                } else {
                    callbackContext.success("false");
                }
                return true;
            case MotionEventCompat.AXIS_GENERIC_2 /* 33 */:
                callbackContext.success(getFilesDir());
                return true;
            case 34:
                getConfiguration(callbackContext);
                return true;
            case MotionEventCompat.AXIS_GENERIC_4 /* 35 */:
                callbackContext.success(fileExists(jSONArray.getString(0), jSONArray.getString(1)) ? 1 : 0);
                return true;
            case 36:
                if (Build.VERSION.SDK_INT >= 30) {
                    zIsExternalStorageManager = Environment.isExternalStorageManager();
                } else {
                    zIsExternalStorageManager = ContextCompat.checkSelfPermission(this.context, "android.permission.READ_EXTERNAL_STORAGE") == 0 ? 1 : 0;
                }
                callbackContext.success(String.valueOf(zIsExternalStorageManager));
                return true;
            case MotionEventCompat.AXIS_GENERIC_7 /* 38 */:
                this.cordova.getActivity().runOnUiThread(new Runnable() { // from class: com.foxdebug.system.System.4
                    @Override // java.lang.Runnable
                    public void run() {
                        System.this.clearCache(callbackContext);
                    }
                });
                return true;
            case MotionEventCompat.AXIS_GENERIC_9 /* 40 */:
                this.cordova.getActivity().runOnUiThread(new Runnable() { // from class: com.foxdebug.system.System.2
                    @Override // java.lang.Runnable
                    public void run() {
                        System.this.setNativeContextMenuDisabled(Boolean.parseBoolean(strOptString));
                        callbackContext.success();
                    }
                });
                return true;
            case MotionEventCompat.AXIS_GENERIC_10 /* 41 */:
                if (new File(jSONArray.getString(0)).delete()) {
                    callbackContext.success();
                } else {
                    callbackContext.error("delete failed");
                }
                return true;
            case MotionEventCompat.AXIS_GENERIC_12 /* 43 */:
                this.cordova.getActivity().runOnUiThread(new Runnable() { // from class: com.foxdebug.system.System.3
                    @Override // java.lang.Runnable
                    public void run() {
                        System.this.setUiTheme(strOptString, jSONArray.optJSONObject(1), callbackContext);
                    }
                });
                return true;
            case MotionEventCompat.AXIS_GENERIC_13 /* 44 */:
                if (new File(jSONArray.getString(0)).setExecutable(Boolean.parseBoolean(jSONArray.getString(1)))) {
                    callbackContext.success();
                } else {
                    callbackContext.error("set exec failed");
                }
                return true;
            default:
                return false;
        }
    }

    /* JADX INFO: renamed from: lambda$execute$0$com-foxdebug-system-System, reason: not valid java name */
    /* synthetic */ void m253lambda$execute$0$comfoxdebugsystemSystem(String str, CallbackContext callbackContext) {
        try {
            Intent intent = new Intent("android.intent.action.SEND");
            intent.setType("text/plain");
            intent.putExtra("android.intent.extra.TEXT", str);
            this.cordova.getActivity().startActivity(Intent.createChooser(intent, "Share"));
            callbackContext.success();
        } catch (Exception e) {
            callbackContext.error(e.getMessage());
        }
    }

    /* JADX INFO: renamed from: com.foxdebug.system.System$5, reason: invalid class name */
    class AnonymousClass5 implements Runnable {
        final /* synthetic */ String val$action;
        final /* synthetic */ String val$arg1;
        final /* synthetic */ String val$arg2;
        final /* synthetic */ String val$arg3;
        final /* synthetic */ String val$arg4;
        final /* synthetic */ String val$arg5;
        final /* synthetic */ String val$arg6;
        final /* synthetic */ JSONArray val$args;
        final /* synthetic */ CallbackContext val$callbackContext;

        AnonymousClass5(String str, JSONArray jSONArray, CallbackContext callbackContext, String str2, String str3, String str4, String str5, String str6, String str7) {
            this.val$action = str;
            this.val$args = jSONArray;
            this.val$callbackContext = callbackContext;
            this.val$arg1 = str2;
            this.val$arg2 = str3;
            this.val$arg3 = str4;
            this.val$arg4 = str5;
            this.val$arg5 = str6;
            this.val$arg6 = str7;
        }

        /* JADX WARN: Code duplicated, block: B:215:0x032d A[Catch: IOException -> 0x0329, Exception -> 0x033e, TRY_LEAVE, TryCatch #6 {IOException -> 0x0329, blocks: (B:211:0x0325, B:215:0x032d), top: B:272:0x0325, outer: #7 }] */
        /* JADX WARN: Code duplicated, block: B:272:0x0325 A[EXC_TOP_SPLITTER, SYNTHETIC] */
        /* JADX WARN: Code duplicated, block: B:288:? A[Catch: Exception -> 0x033e, SYNTHETIC, TRY_LEAVE, TryCatch #7 {Exception -> 0x033e, blocks: (B:113:0x0196, B:157:0x027e, B:161:0x0286, B:163:0x028b, B:164:0x0294, B:168:0x02a2, B:172:0x02aa, B:174:0x02ae, B:178:0x02c1, B:181:0x02c6, B:185:0x02d9, B:188:0x02de, B:211:0x0325, B:215:0x032d, B:218:0x033d, B:217:0x0331, B:200:0x0307, B:204:0x030f, B:206:0x0314), top: B:274:0x0196, inners: #4, #6, #9, #10, #11, #12 }] */
        @Override // java.lang.Runnable
        public void run() throws Throwable {
            Throwable th;
            OutputStream outputStream;
            OutputStream outputStream2;
            CallbackContext callbackContext;
            String string;
            DocumentFile documentFileCreateFile;
            String str = this.val$action;
            str.hashCode();
            switch (str) {
                case "open-in-browser":
                    System.this.openInBrowser(this.val$arg1, this.val$callbackContext);
                    return;
                case "is-powersave-mode":
                    System.this.isPowerSaveMode(this.val$callbackContext);
                    return;
                case "get-global-setting":
                    System.this.getGlobalSetting(this.val$arg1, this.val$callbackContext);
                    return;
                case "has-permission":
                    System.this.hasPermission(this.val$arg1, this.val$callbackContext);
                    return;
                case "decode":
                    System.this.decode(this.val$arg1, this.val$arg2, this.val$callbackContext);
                    return;
                case "encode":
                    System.this.encode(this.val$arg1, this.val$arg2, this.val$callbackContext);
                    return;
                case "request-permission":
                    System.this.requestPermission(this.val$arg1, this.val$callbackContext);
                    return;
                case "get-app-info":
                    System.this.getAppInfo(this.val$callbackContext);
                    return;
                case "get-webkit-info":
                    System.this.getWebkitInfo(this.val$callbackContext);
                    return;
                case "getInstaller":
                    try {
                        PackageManager packageManager = System.this.context.getPackageManager();
                        this.val$callbackContext.success(Build.VERSION.SDK_INT >= 30 ? packageManager.getInstallSourceInfo(System.this.context.getPackageName()).getInstallingPackageName() : packageManager.getInstallerPackageName(System.this.context.getPackageName()));
                        return;
                    } catch (Exception e) {
                        this.val$callbackContext.error(e.getMessage());
                        return;
                    }
                case "remove-shortcut":
                    System.this.removeShortcut(this.val$arg1, this.val$callbackContext);
                    return;
                case "file-action":
                    System.this.fileAction(this.val$arg1, this.val$arg2, this.val$arg3, this.val$arg4, this.val$callbackContext);
                    return;
                case "get-available-encodings":
                    System.this.getAvailableEncodings(this.val$callbackContext);
                    return;
                case "launch-app":
                    System.this.launchApp(this.val$arg1, this.val$arg2, this.val$args.optJSONObject(2), this.val$callbackContext);
                    return;
                case "extractAsset":
                    try {
                        System.this.extractAsset(this.val$args.getString(0), this.val$args.getString(1), this.val$callbackContext);
                        return;
                    } catch (Exception e2) {
                        this.val$callbackContext.error("Failed to extract asset: " + e2.getMessage());
                        return;
                    }
                case "checksumText":
                    ExecutorService threadPool = System.this.cordova.getThreadPool();
                    final JSONArray jSONArray = this.val$args;
                    final CallbackContext callbackContext2 = this.val$callbackContext;
                    threadPool.execute(new Runnable() { // from class: com.foxdebug.system.System$5$$ExternalSyntheticLambda0
                        @Override // java.lang.Runnable
                        public final void run() {
                            System.AnonymousClass5.lambda$run$0(jSONArray, callbackContext2);
                        }
                    });
                    return;
                case "get-android-version":
                    System.this.getAndroidVersion(this.val$callbackContext);
                    return;
                case "compare-file-text":
                    System.this.compareFileText(this.val$arg1, this.val$arg2, this.val$arg3, this.val$callbackContext);
                    return;
                case "copyToUri":
                    try {
                        Uri uri = Uri.parse(this.val$args.getString(0));
                        Uri uri2 = Uri.parse(this.val$args.getString(1));
                        String string2 = this.val$args.getString(2);
                        InputStream inputStream = null;
                        try {
                            InputStream fileInputStream = "file".equalsIgnoreCase(uri.getScheme()) ? new FileInputStream(new File(uri.getPath())) : System.this.context.getContentResolver().openInputStream(uri);
                            try {
                                if ("file".equalsIgnoreCase(uri2.getScheme())) {
                                    File file = new File(uri2.getPath());
                                    if (!file.exists()) {
                                        file.mkdirs();
                                    }
                                    DocumentFile documentFileFromFile = DocumentFile.fromFile(file);
                                    DocumentFile documentFileFindFile = documentFileFromFile.findFile(string2);
                                    if (documentFileFindFile != null && documentFileFindFile.exists()) {
                                        documentFileFindFile.delete();
                                    }
                                    documentFileCreateFile = documentFileFromFile.createFile(System.this.getMimeTypeFromExtension(string2), string2);
                                } else {
                                    DocumentFile documentFileFromTreeUri = DocumentFile.fromTreeUri(System.this.context, uri2);
                                    if (documentFileFromTreeUri != null && documentFileFromTreeUri.exists() && documentFileFromTreeUri.isDirectory()) {
                                        DocumentFile documentFileFindFile2 = documentFileFromTreeUri.findFile(string2);
                                        if (documentFileFindFile2 != null && documentFileFindFile2.exists()) {
                                            documentFileFindFile2.delete();
                                        }
                                        documentFileCreateFile = documentFileFromTreeUri.createFile(System.this.getMimeTypeFromExtension(string2), string2);
                                    }
                                    this.val$callbackContext.error("Destination directory does not exist or is not accessible");
                                    if (fileInputStream == null) {
                                        return;
                                    }
                                    try {
                                        fileInputStream.close();
                                        return;
                                    } catch (IOException e3) {
                                        e3.printStackTrace();
                                        callbackContext = this.val$callbackContext;
                                        string = e3.toString();
                                    }
                                }
                                if (documentFileCreateFile == null || !documentFileCreateFile.exists()) {
                                    this.val$callbackContext.error("Failed to create destination file");
                                    if (fileInputStream == null) {
                                        return;
                                    }
                                    try {
                                        fileInputStream.close();
                                        return;
                                    } catch (IOException e4) {
                                        e4.printStackTrace();
                                        callbackContext = this.val$callbackContext;
                                        string = e4.toString();
                                    }
                                } else {
                                    OutputStream outputStreamOpenOutputStream = System.this.context.getContentResolver().openOutputStream(documentFileCreateFile.getUri());
                                    if (fileInputStream == null || outputStreamOpenOutputStream == null) {
                                        this.val$callbackContext.error("uri streams are null");
                                        if (fileInputStream != null) {
                                            try {
                                                fileInputStream.close();
                                            } catch (IOException e5) {
                                                e5.printStackTrace();
                                                callbackContext = this.val$callbackContext;
                                                string = e5.toString();
                                            }
                                        }
                                        if (outputStreamOpenOutputStream != null) {
                                            outputStreamOpenOutputStream.close();
                                            return;
                                        }
                                        return;
                                    }
                                    byte[] bArr = new byte[8192];
                                    while (true) {
                                        int i = fileInputStream.read(bArr);
                                        if (i <= 0) {
                                            outputStreamOpenOutputStream.flush();
                                            this.val$callbackContext.success();
                                            if (fileInputStream != null) {
                                                try {
                                                    fileInputStream.close();
                                                } catch (IOException e6) {
                                                    e6.printStackTrace();
                                                    callbackContext = this.val$callbackContext;
                                                    string = e6.toString();
                                                }
                                            }
                                            if (outputStreamOpenOutputStream != null) {
                                                outputStreamOpenOutputStream.close();
                                                return;
                                            }
                                            return;
                                        }
                                        outputStreamOpenOutputStream.write(bArr, 0, i);
                                    }
                                }
                            } catch (IOException e7) {
                                e = e7;
                                inputStream = fileInputStream;
                                outputStream2 = null;
                                try {
                                    e.printStackTrace();
                                    this.val$callbackContext.error(e.toString());
                                    if (inputStream != null) {
                                        try {
                                            inputStream.close();
                                        } catch (IOException e8) {
                                            e8.printStackTrace();
                                            callbackContext = this.val$callbackContext;
                                            string = e8.toString();
                                        }
                                    }
                                    if (outputStream2 != null) {
                                        outputStream2.close();
                                        return;
                                    }
                                    return;
                                } catch (Throwable th2) {
                                    OutputStream outputStream3 = outputStream2;
                                    th = th2;
                                    outputStream = outputStream3;
                                    if (inputStream != null) {
                                        try {
                                            inputStream.close();
                                        } catch (IOException e9) {
                                            e9.printStackTrace();
                                            this.val$callbackContext.error(e9.toString());
                                            throw th;
                                        }
                                    }
                                    if (outputStream != null) {
                                        throw th;
                                    }
                                    outputStream.close();
                                    throw th;
                                }
                            } catch (Throwable th3) {
                                InputStream inputStream2 = fileInputStream;
                                th = th3;
                                outputStream = null;
                                inputStream = inputStream2;
                                if (inputStream != null) {
                                    inputStream.close();
                                }
                                if (outputStream != null) {
                                    throw th;
                                }
                                outputStream.close();
                                throw th;
                            }
                        } catch (IOException e10) {
                            e = e10;
                            outputStream2 = null;
                        } catch (Throwable th4) {
                            th = th4;
                            outputStream = null;
                        }
                        callbackContext.error(string);
                        return;
                    } catch (Exception e11) {
                        e11.printStackTrace();
                        this.val$callbackContext.error(e11.toString());
                        return;
                    }
                case "pin-shortcut":
                    System.this.pinShortcut(this.val$arg1, this.val$callbackContext);
                    return;
                case "request-permissions":
                    System.this.requestPermissions(this.val$args.optJSONArray(0), this.val$callbackContext);
                    return;
                case "compare-texts":
                    System.this.compareTexts(this.val$arg1, this.val$arg2, this.val$callbackContext);
                    return;
                case "add-shortcut":
                    System.this.addShortcut(this.val$arg1, this.val$arg2, this.val$arg3, this.val$arg4, this.val$arg5, this.val$arg6, this.val$callbackContext);
                    return;
                case "pin-file-shortcut":
                    System.this.pinFileShortcut(this.val$args.optJSONObject(0), this.val$callbackContext);
                    return;
                default:
                    return;
            }
        }

        static /* synthetic */ void lambda$run$0(JSONArray jSONArray, CallbackContext callbackContext) {
            try {
                byte[] bArrDigest = MessageDigest.getInstance(JCEAlgorithms.JCE_SHA256).digest(jSONArray.getString(0).getBytes("UTF-8"));
                StringBuilder sb = new StringBuilder();
                for (byte b : bArrDigest) {
                    String hexString = Integer.toHexString(b & UByte.MAX_VALUE);
                    if (hexString.length() == 1) {
                        sb.append('0');
                    }
                    sb.append(hexString);
                }
                callbackContext.success(sb.toString());
            } catch (Exception e) {
                callbackContext.error(e.getMessage());
            }
        }
    }

    private void sendLogToJavaScript(String str, String str2) {
        final String str3 = "if (typeof window.log === 'function')  window.log(" + JSONObject.quote(str) + ", " + JSONObject.quote(str2) + ");else  console.log(" + JSONObject.quote(str) + ", " + JSONObject.quote(str2) + ");";
        this.cordova.getActivity().runOnUiThread(new Runnable() { // from class: com.foxdebug.system.System$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m254lambda$sendLogToJavaScript$1$comfoxdebugsystemSystem(str3);
            }
        });
    }

    /* JADX INFO: renamed from: lambda$sendLogToJavaScript$1$com-foxdebug-system-System, reason: not valid java name */
    /* synthetic */ void m254lambda$sendLogToJavaScript$1$comfoxdebugsystemSystem(String str) {
        try {
            ((WebView) this.webView.getEngine().getView()).evaluateJavascript(str, null);
        } catch (Exception e) {
            Log.e(TAG, "Failed to send log to JavaScript: " + e.getMessage());
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String getMimeTypeFromExtension(String str) {
        String lowerCase;
        int iLastIndexOf = str.lastIndexOf(46);
        if (iLastIndexOf > 0 && iLastIndexOf < str.length() - 1) {
            lowerCase = str.substring(iLastIndexOf + 1).toLowerCase();
        } else {
            lowerCase = "";
        }
        String mimeTypeFromExtension = MimeTypeMap.getSingleton().getMimeTypeFromExtension(lowerCase);
        return mimeTypeFromExtension != null ? mimeTypeFromExtension : "application/octet-stream";
    }

    private void getConfiguration(CallbackContext callbackContext) {
        try {
            JSONObject jSONObject = new JSONObject();
            Configuration configuration = this.context.getResources().getConfiguration();
            InputMethodManager inputMethodManager = (InputMethodManager) this.context.getSystemService("input_method");
            Method method = InputMethodManager.class.getMethod("getInputMethodWindowVisibleHeight", new Class[0]);
            jSONObject.put("isAcceptingText", inputMethodManager.isAcceptingText());
            jSONObject.put("keyboardHeight", method.invoke(inputMethodManager, new Object[0]));
            jSONObject.put("locale", configuration.locale.toString());
            jSONObject.put("fontScale", configuration.fontScale);
            jSONObject.put("keyboard", configuration.keyboard);
            jSONObject.put("keyboardHidden", configuration.keyboardHidden);
            jSONObject.put("hardKeyboardHidden", configuration.hardKeyboardHidden);
            jSONObject.put("navigationHidden", configuration.navigationHidden);
            jSONObject.put(NotificationCompat.CATEGORY_NAVIGATION, configuration.navigation);
            jSONObject.put("orientation", configuration.orientation);
            callbackContext.success(jSONObject);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException | JSONException e) {
            callbackContext.error(e.toString());
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void decode(String str, String str2, CallbackContext callbackContext) {
        try {
            byte[] bArrDecode = Base64.decode(str, 0);
            if (!Charset.isSupported(str2)) {
                callbackContext.error("Charset not supported: " + str2);
            } else {
                callbackContext.success(String.valueOf(Charset.forName(str2).decode(ByteBuffer.wrap(bArrDecode))));
            }
        } catch (Exception e) {
            callbackContext.error(e.toString());
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void encode(String str, String str2, CallbackContext callbackContext) {
        try {
            if (!Charset.isSupported(str2)) {
                callbackContext.error("Charset not supported: " + str2);
                return;
            }
            ByteBuffer byteBufferEncode = Charset.forName(str2).encode(str);
            byte[] bArr = new byte[byteBufferEncode.remaining()];
            byteBufferEncode.get(bArr);
            callbackContext.success(bArr);
        } catch (Exception e) {
            callbackContext.error(e.toString());
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void compareFileText(String str, String str2, String str3, CallbackContext callbackContext) {
        String string;
        if (str != null) {
            try {
                if (!str.isEmpty()) {
                    if (str2 == null || str2.isEmpty()) {
                        str2 = "UTF-8";
                    }
                    if (!Charset.isSupported(str2)) {
                        callbackContext.error("Charset not supported: " + str2);
                        return;
                    }
                    Uri uriBuildDocumentUriUsingTree = Uri.parse(str);
                    Charset charsetForName = Charset.forName(str2);
                    if (!"file".equalsIgnoreCase(uriBuildDocumentUriUsingTree.getScheme())) {
                        if (!"content".equalsIgnoreCase(uriBuildDocumentUriUsingTree.getScheme())) {
                            callbackContext.error("Unsupported URI scheme: " + uriBuildDocumentUriUsingTree.getScheme());
                            return;
                        }
                        InputStream inputStreamOpenInputStream = null;
                        try {
                            if (str.contains("::")) {
                                try {
                                    String[] strArrSplit = str.split("::", 2);
                                    uriBuildDocumentUriUsingTree = DocumentsContract.buildDocumentUriUsingTree(Uri.parse(strArrSplit[0]), strArrSplit[1]);
                                } catch (Exception e) {
                                    callbackContext.error("SAF_FALLBACK: Invalid SAF URI format - " + e.getMessage());
                                    return;
                                }
                            }
                            inputStreamOpenInputStream = this.context.getContentResolver().openInputStream(uriBuildDocumentUriUsingTree);
                            if (inputStreamOpenInputStream == null) {
                                callbackContext.error("Cannot open file");
                                if (inputStreamOpenInputStream != null) {
                                    try {
                                        inputStreamOpenInputStream.close();
                                        return;
                                    } catch (IOException e2) {
                                        Log.w(TAG, "Failed to close input stream while reading file.", e2);
                                        return;
                                    }
                                }
                                return;
                            }
                            StringBuilder sb = new StringBuilder();
                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStreamOpenInputStream, charsetForName));
                            try {
                                char[] cArr = new char[8192];
                                while (true) {
                                    int i = bufferedReader.read(cArr);
                                    if (i == -1) {
                                        break;
                                    } else {
                                        sb.append(cArr, 0, i);
                                    }
                                }
                                bufferedReader.close();
                                string = sb.toString();
                                if (inputStreamOpenInputStream != null) {
                                    try {
                                        inputStreamOpenInputStream.close();
                                    } catch (IOException e3) {
                                        Log.w(TAG, "Failed to close input stream while reading file.", e3);
                                    }
                                }
                            } catch (Throwable th) {
                                try {
                                    bufferedReader.close();
                                } catch (Throwable th2) {
                                    th.addSuppressed(th2);
                                }
                                throw th;
                            }
                        } catch (Throwable th3) {
                            if (inputStreamOpenInputStream != null) {
                                try {
                                    inputStreamOpenInputStream.close();
                                } catch (IOException e4) {
                                    Log.w(TAG, "Failed to close input stream while reading file.", e4);
                                }
                            }
                            throw th3;
                        }
                        callbackContext.error(e.toString());
                        return;
                    }
                    File file = new File(uriBuildDocumentUriUsingTree.getPath());
                    if (!file.exists()) {
                        callbackContext.error("File does not exist");
                        return;
                    } else if (!file.isFile()) {
                        callbackContext.error("Path is not a file");
                        return;
                    } else {
                        if (!file.canRead()) {
                            callbackContext.error("File is not readable");
                            return;
                        }
                        string = new String(Files.readAllBytes(file.toPath()), charsetForName);
                    }
                    if (string.length() != str3.length()) {
                        callbackContext.success(1);
                        return;
                    } else if (string.equals(str3)) {
                        callbackContext.success(0);
                        return;
                    } else {
                        callbackContext.success(1);
                        return;
                    }
                }
            } catch (Exception e5) {
                callbackContext.error(e5.toString());
                return;
            }
        }
        callbackContext.error("File URI is required");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void compareTexts(String str, String str2, CallbackContext callbackContext) {
        if (str == null) {
            str = "";
        }
        if (str2 == null) {
            str2 = "";
        }
        try {
            if (str.length() != str2.length()) {
                callbackContext.success(1);
            } else if (str.equals(str2)) {
                callbackContext.success(0);
            } else {
                callbackContext.success(1);
            }
        } catch (Exception e) {
            callbackContext.error(e.toString());
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void getAvailableEncodings(CallbackContext callbackContext) {
        try {
            SortedMap<String, Charset> sortedMapAvailableCharsets = Charset.availableCharsets();
            JSONObject jSONObject = new JSONObject();
            for (Map.Entry<String, Charset> entry : sortedMapAvailableCharsets.entrySet()) {
                JSONObject jSONObject2 = new JSONObject();
                Charset value = entry.getValue();
                jSONObject2.put("label", value.displayName());
                jSONObject2.put("aliases", new JSONArray((Collection) value.aliases()));
                jSONObject2.put("name", value.name());
                jSONObject.put(value.name(), jSONObject2);
            }
            callbackContext.success(jSONObject);
        } catch (Exception e) {
            callbackContext.error(e.toString());
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void requestPermissions(JSONArray jSONArray, CallbackContext callbackContext) {
        try {
            String[] strArrCheckPermissions = checkPermissions(jSONArray);
            if (strArrCheckPermissions.length > 0) {
                this.requestPermissionCallback = callbackContext;
                this.cordova.requestPermissions(this, this.REQ_PERMISSIONS, strArrCheckPermissions);
            } else {
                callbackContext.success(new JSONArray());
            }
        } catch (Exception e) {
            callbackContext.error(e.toString());
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void requestPermission(String str, CallbackContext callbackContext) {
        if (str != null || !str.equals("")) {
            if (!this.cordova.hasPermission(str)) {
                this.requestPermissionCallback = callbackContext;
                this.cordova.requestPermission(this, this.REQ_PERMISSION, str);
                return;
            } else {
                callbackContext.success(1);
                return;
            }
        }
        callbackContext.error("No permission passed to request.");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void hasPermission(String str, CallbackContext callbackContext) {
        if (str != null || !str.equals("")) {
            callbackContext.success(this.cordova.hasPermission(str) ? 1 : 0);
        } else {
            callbackContext.error("No permission passed to check.");
        }
    }

    public boolean fileExists(String str, String str2) {
        boolean z = Boolean.parseBoolean(str2);
        Path path = new File(str).toPath();
        try {
            return z ? Files.exists(path, LinkOption.NOFOLLOW_LINKS) : Files.exists(path, new LinkOption[0]);
        } catch (Exception unused) {
            return false;
        }
    }

    public boolean createSymlink(String str, String str2) {
        try {
            return Runtime.getRuntime().exec(new String[]{"ln", "-s", str, str2}).waitFor() == 0;
        } catch (Exception unused) {
        }
    }

    public String getNativeLibraryPath() {
        return this.context.getApplicationInfo().nativeLibraryDir;
    }

    public String getFilesDir() {
        return this.context.getFilesDir().getAbsolutePath();
    }

    public String getParentPath(String str) {
        File parentFile = new File(str).getParentFile();
        if (parentFile != null) {
            return parentFile.getAbsolutePath();
        }
        return null;
    }

    public JSONArray listChildren(String str) throws JSONException {
        File[] fileArrListFiles;
        File file = new File(str);
        JSONArray jSONArray = new JSONArray();
        if (file.exists() && file.isDirectory() && (fileArrListFiles = file.listFiles()) != null) {
            for (File file2 : fileArrListFiles) {
                jSONArray.put(file2.getAbsolutePath());
            }
        }
        return jSONArray;
    }

    public void onRequestPermissionResult(int i, String[] strArr, int[] iArr) {
        CallbackContext callbackContext = this.requestPermissionCallback;
        if (callbackContext == null) {
            return;
        }
        if (i == this.REQ_PERMISSIONS) {
            JSONArray jSONArray = new JSONArray();
            for (int i2 : iArr) {
                if (i2 == -1) {
                    jSONArray.put(0);
                }
                jSONArray.put(1);
            }
            this.requestPermissionCallback.success(jSONArray);
            this.requestPermissionCallback = null;
            return;
        }
        if (iArr.length >= 1 && iArr[0] == -1) {
            callbackContext.success(0);
            this.requestPermissionCallback = null;
        } else {
            callbackContext.success(1);
            this.requestPermissionCallback = null;
        }
    }

    private String[] checkPermissions(JSONArray jSONArray) throws Exception {
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < jSONArray.length(); i++) {
            try {
                String string = jSONArray.getString(i);
                if (string == null || string.equals("")) {
                    throw new Exception("Permission cannot be null or empty");
                }
                if (!this.cordova.hasPermission(string)) {
                    arrayList.add(string);
                }
            } catch (JSONException e) {
                Log.w(TAG, "Invalid permission entry at index " + i, e);
            }
        }
        return (String[]) arrayList.toArray(new String[arrayList.size()]);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void getAndroidVersion(CallbackContext callbackContext) {
        callbackContext.success(Build.VERSION.SDK_INT);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void getWebkitInfo(CallbackContext callbackContext) {
        JSONObject jSONObject = new JSONObject();
        try {
            PackageInfo currentWebViewPackage = WebView.getCurrentWebViewPackage();
            jSONObject.put("packageName", currentWebViewPackage.packageName);
            jSONObject.put("versionName", currentWebViewPackage.versionName);
            jSONObject.put("versionCode", currentWebViewPackage.versionCode);
            callbackContext.success(jSONObject);
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException | JSONException e) {
            callbackContext.error("Cannot determine current WebView engine. (" + e.getMessage() + ")");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void isPowerSaveMode(CallbackContext callbackContext) {
        callbackContext.success(((PowerManager) this.context.getSystemService("power")).isPowerSaveMode() ? 1 : 0);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void pinFileShortcut(JSONObject jSONObject, CallbackContext callbackContext) {
        IconCompat fileShortcutIcon;
        Bitmap bitmap;
        if (jSONObject == null) {
            callbackContext.error("Invalid shortcut data");
            return;
        }
        String strOptString = jSONObject.optString("id", "");
        String strOptString2 = jSONObject.optString("label", "");
        String strOptString3 = jSONObject.optString("description", strOptString2);
        String strOptString4 = jSONObject.optString("icon", "");
        String strOptString5 = jSONObject.optString("uri", "");
        if (strOptString.isEmpty() || strOptString2.isEmpty() || strOptString5.isEmpty()) {
            callbackContext.error("Missing required shortcut fields");
            return;
        }
        if (!ShortcutManagerCompat.isRequestPinShortcutSupported(this.context)) {
            callbackContext.error("Pin shortcut not supported on this launcher");
            return;
        }
        try {
            Uri uri = Uri.parse(strOptString5);
            String packageName = this.context.getPackageName();
            Intent launchIntentForPackage = this.context.getPackageManager().getLaunchIntentForPackage(packageName);
            if (launchIntentForPackage == null) {
                callbackContext.error("Launch intent not found for package: " + packageName);
                return;
            }
            ComponentName component = launchIntentForPackage.getComponent();
            if (component == null) {
                callbackContext.error("ComponentName is null");
                return;
            }
            Intent intent = new Intent("android.intent.action.VIEW");
            intent.setComponent(component);
            intent.setData(uri);
            intent.putExtra("acodeFileUri", strOptString5);
            if (strOptString4 != null && !strOptString4.isEmpty()) {
                try {
                    Uri uri2 = Uri.parse(strOptString4);
                    if (Build.VERSION.SDK_INT >= 28) {
                        bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(this.context.getContentResolver(), uri2));
                    } else {
                        bitmap = MediaStore.Images.Media.getBitmap(this.context.getContentResolver(), uri2);
                    }
                    fileShortcutIcon = IconCompat.createWithBitmap(bitmap);
                } catch (Exception unused) {
                    fileShortcutIcon = getFileShortcutIcon(strOptString2);
                }
            } else {
                fileShortcutIcon = getFileShortcutIcon(strOptString2);
            }
            ShortcutInfoCompat.Builder shortLabel = new ShortcutInfoCompat.Builder(this.context, strOptString).setShortLabel(strOptString2);
            if (strOptString3 != null && !strOptString3.isEmpty()) {
                strOptString2 = strOptString3;
            }
            ShortcutInfoCompat shortcutInfoCompatBuild = shortLabel.setLongLabel(strOptString2).setIcon(fileShortcutIcon).setIntent(intent).build();
            ShortcutManagerCompat.pushDynamicShortcut(this.context, shortcutInfoCompatBuild);
            if (!ShortcutManagerCompat.requestPinShortcut(this.context, shortcutInfoCompatBuild, null)) {
                callbackContext.error("Failed to request pin shortcut");
            } else {
                callbackContext.success();
            }
        } catch (Exception e) {
            callbackContext.error(e.toString());
        }
    }

    private IconCompat getFileShortcutIcon(String str) {
        return IconCompat.createWithAdaptiveBitmap(createFileShortcutBitmap(str));
    }

    private Bitmap createFileShortcutBitmap(String str) {
        float f;
        float fApplyDimension = TypedValue.applyDimension(1, 72.0f, this.context.getResources().getDisplayMetrics());
        int iRound = Math.round(fApplyDimension > 0.0f ? fApplyDimension : 72.0f);
        Bitmap bitmapCreateBitmap = Bitmap.createBitmap(iRound, iRound, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmapCreateBitmap);
        Paint paint = new Paint(7);
        paint.setColor(pickShortcutColor(str));
        float f2 = iRound;
        float f3 = 0.24f * f2;
        canvas.drawRoundRect(new RectF(0.0f, 0.0f, f2, f2), f3, f3, paint);
        paint.setColor(-1);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(Typeface.create("sans-serif-medium", 1));
        String shortcutLabel = getShortcutLabel(str);
        float fMax = Math.max(1, shortcutLabel.length());
        if (fMax > 4.0f) {
            f = 0.22f;
        } else {
            f = fMax > 3.0f ? 0.26f : 0.34f;
        }
        paint.setTextSize(f * f2);
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        canvas.drawText(shortcutLabel, f2 / 2.0f, ((f2 - fontMetrics.bottom) - fontMetrics.top) / 2.0f, paint);
        return bitmapCreateBitmap;
    }

    private String getFileExtension(String str) {
        int iLastIndexOf;
        if (str == null || (iLastIndexOf = str.lastIndexOf(46)) < 0 || iLastIndexOf == str.length() - 1) {
            return "";
        }
        return str.substring(iLastIndexOf + 1).toLowerCase(Locale.getDefault());
    }

    private String getShortcutLabel(String str) {
        String fileExtension = getFileExtension(str);
        if (!fileExtension.isEmpty()) {
            fileExtension.hashCode();
            switch (fileExtension) {
                case "c":
                    return "C";
                case "h":
                case "hpp":
                    return "HDR";
                case "cc":
                case "cpp":
                case "cxx":
                    return "CPP";
                case "go":
                    return "GO";
                case "js":
                case "jsx":
                    return "JS";
                case "kt":
                case "kts":
                    return "KOT";
                case "md":
                case "markdown":
                    return "MD";
                case "py":
                    return "PY";
                case "rb":
                    return "RB";
                case "rs":
                    return "RS";
                case "sh":
                case "bash":
                    return "SH";
                case "ts":
                case "tsx":
                    return "TS";
                case "css":
                    return "CSS";
                case "htm":
                case "html":
                    return "HTML";
                case "php":
                    return "PHP";
                case "txt":
                    return "TXT";
                case "xml":
                    return "XML";
                case "yml":
                case "yaml":
                    return "YML";
                case "java":
                    return "JAVA";
                case "json":
                    return "JSON";
                default:
                    String strReplaceAll = fileExtension.replaceAll("[^A-Za-z0-9]", "");
                    if (!strReplaceAll.isEmpty()) {
                        fileExtension = strReplaceAll;
                    }
                    if (fileExtension.length() > 4) {
                        fileExtension = fileExtension.substring(0, 4);
                    }
                    return fileExtension.toUpperCase(Locale.getDefault());
            }
        }
        if (str != null && !str.trim().isEmpty()) {
            String strReplaceAll2 = str.replaceAll("[^A-Za-z0-9]", "");
            if (!strReplaceAll2.isEmpty()) {
                if (strReplaceAll2.length() > 3) {
                    strReplaceAll2 = strReplaceAll2.substring(0, 3);
                }
                return strReplaceAll2.toUpperCase(Locale.getDefault());
            }
            return str.substring(0, 1).toUpperCase(Locale.getDefault());
        }
        return "FILE";
    }

    private int pickShortcutColor(String str) {
        String fileExtension = getFileExtension(str);
        fileExtension.hashCode();
        switch (fileExtension) {
            case "c":
                return -11243910;
            case "h":
            case "hpp":
                return -7508381;
            case "cc":
            case "cpp":
            case "cxx":
                return -16754276;
            case "go":
                return -16732712;
            case "js":
            case "jsx":
                return -532706;
            case "kt":
            case "kts":
                return -8432897;
            case "md":
            case "markdown":
                return -11243910;
            case "py":
                return -13604456;
            case "rb":
                return -3394515;
            case "rs":
                return -4767474;
            case "sh":
            case "bash":
                return -13070788;
            case "ts":
            case "tsx":
                return -13535034;
            case "css":
                return -14064897;
            case "htm":
            case "html":
                return -765666;
            case "php":
                return -7826753;
            case "txt":
                return -11243910;
            case "xml":
                return -10720320;
            case "yml":
            case "yaml":
                return -9079435;
            case "java":
                return -1282259;
            case "json":
                return -11751600;
            default:
                int[] iArr = {-14776091, -9614271, -16742021, -7461718, -13022805, -16540699, -2614432, -12345273};
                if (!fileExtension.isEmpty()) {
                    str = fileExtension;
                } else if (str == null) {
                    str = "file";
                }
                return iArr[Math.abs(str.hashCode()) % 8];
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void fileAction(String str, String str2, String str3, String str4, CallbackContext callbackContext) {
        Activity activity = this.activity;
        Context context = this.context;
        Uri contentProviderUri = getContentProviderUri(str, str2);
        if (contentProviderUri == null) {
            callbackContext.error("Unable to access file for action " + str3);
            return;
        }
        try {
            Intent intent = new Intent(str3);
            String str5 = str4;
            if (str5.equals("")) {
                str5 = "text/plain";
            }
            String strResolveMimeType = resolveMimeType(str5, contentProviderUri, str2);
            String name = (str2 == null || str2.isEmpty()) ? null : new File(str2).getName();
            if (name == null || name.isEmpty()) {
                name = contentProviderUri.getLastPathSegment();
            }
            if (name == null || name.isEmpty()) {
                name = "shared-file";
            }
            if (str3.equals("android.intent.action.SEND")) {
                intent.setType(strResolveMimeType);
                intent.addFlags(1);
                intent.setClipData(ClipData.newUri(context.getContentResolver(), name, contentProviderUri));
                intent.putExtra("android.intent.extra.STREAM", contentProviderUri);
                intent.putExtra("android.intent.extra.TITLE", name);
                intent.putExtra("android.intent.extra.SUBJECT", name);
                if (str2 != null && !str2.isEmpty()) {
                    intent.putExtra("android.intent.extra.TEXT", str2);
                }
            } else {
                intent.setFlags(str3.equals("android.intent.action.EDIT") ? 67 : 65);
                intent.setDataAndType(contentProviderUri, strResolveMimeType);
                intent.setClipData(ClipData.newUri(context.getContentResolver(), name, contentProviderUri));
                intent.addFlags(1);
                if (!name.equals("shared-file")) {
                    intent.putExtra("android.intent.extra.TITLE", name);
                }
                if (str3.equals("android.intent.action.EDIT")) {
                    intent.putExtra("android.intent.extra.STREAM", contentProviderUri);
                }
            }
            grantUriPermissions(intent, contentProviderUri, str3.equals("android.intent.action.EDIT") ? 3 : 1);
            if (str3.equals("android.intent.action.SEND")) {
                Intent intentCreateChooser = Intent.createChooser(intent, null);
                intentCreateChooser.addFlags(1);
                activity.startActivity(intentCreateChooser);
            } else if (str3.equals("android.intent.action.EDIT") || str3.equals("android.intent.action.VIEW")) {
                Intent intentCreateChooser2 = Intent.createChooser(intent, null);
                intentCreateChooser2.addFlags(1);
                if (str3.equals("android.intent.action.EDIT")) {
                    intentCreateChooser2.addFlags(2);
                }
                activity.startActivity(intentCreateChooser2);
            } else {
                activity.startActivity(intent);
            }
            callbackContext.success(contentProviderUri.toString());
        } catch (Exception e) {
            callbackContext.error(e.getMessage());
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void getAppInfo(CallbackContext callbackContext) {
        JSONObject jSONObject = new JSONObject();
        try {
            PackageManager packageManager = this.activity.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(this.context.getPackageName(), 0);
            ApplicationInfo applicationInfo = this.context.getApplicationInfo();
            int i = applicationInfo.flags & 2;
            jSONObject.put("firstInstallTime", packageInfo.firstInstallTime);
            jSONObject.put("lastUpdateTime", packageInfo.lastUpdateTime);
            jSONObject.put("label", applicationInfo.loadLabel(packageManager).toString());
            jSONObject.put("packageName", packageInfo.packageName);
            jSONObject.put("versionName", packageInfo.versionName);
            jSONObject.put("versionCode", packageInfo.getLongVersionCode());
            jSONObject.put("isDebuggable", i);
            callbackContext.success(jSONObject);
        } catch (JSONException e) {
            callbackContext.error(e.getMessage());
        } catch (Exception e2) {
            callbackContext.error(e2.getMessage());
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void openInBrowser(String str, CallbackContext callbackContext) {
        this.activity.startActivity(new Intent("android.intent.action.VIEW", Uri.parse(str)));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void launchApp(String str, String str2, JSONObject jSONObject, CallbackContext callbackContext) {
        if (str == null || str.equals("")) {
            callbackContext.error("No package name provided.");
            return;
        }
        if (str2 == null || str2.equals("")) {
            callbackContext.error("No activity class name provided.");
            return;
        }
        try {
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.addCategory("android.intent.category.LAUNCHER");
            intent.setPackage(str);
            intent.addFlags(268435456);
            intent.setClassName(str, str2);
            if (jSONObject != null) {
                Iterator<String> itKeys = jSONObject.keys();
                while (itKeys.hasNext()) {
                    String next = itKeys.next();
                    Object obj = jSONObject.get(next);
                    if (obj instanceof Integer) {
                        intent.putExtra(next, (Integer) obj);
                    } else if (obj instanceof Boolean) {
                        intent.putExtra(next, (Boolean) obj);
                    } else if (obj instanceof Double) {
                        intent.putExtra(next, (Double) obj);
                    } else if (obj instanceof Long) {
                        intent.putExtra(next, (Long) obj);
                    } else if (obj instanceof String) {
                        intent.putExtra(next, (String) obj);
                    } else {
                        intent.putExtra(next, obj.toString());
                    }
                }
            }
            this.activity.startActivity(intent);
            callbackContext.success("Launched " + str);
        } catch (Exception e) {
            callbackContext.error(e.toString());
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void addShortcut(String str, String str2, String str3, String str4, String str5, String str6, CallbackContext callbackContext) {
        try {
            IconCompat iconCompatCreateWithBitmap = IconCompat.createWithBitmap(ImageDecoder.decodeBitmap(ImageDecoder.createSource(this.context.getContentResolver(), Uri.parse(str4))));
            Intent launchIntentForPackage = this.activity.getPackageManager().getLaunchIntentForPackage(this.activity.getPackageName());
            launchIntentForPackage.putExtra("action", str5);
            launchIntentForPackage.putExtra("data", str6);
            ShortcutManagerCompat.pushDynamicShortcut(this.context, new ShortcutInfoCompat.Builder(this.context, str).setShortLabel(str2).setLongLabel(str3).setIcon(iconCompatCreateWithBitmap).setIntent(launchIntentForPackage).build());
            callbackContext.success();
        } catch (Exception e) {
            callbackContext.error(e.toString());
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void pinShortcut(String str, CallbackContext callbackContext) {
        ShortcutManager shortcutManager = (ShortcutManager) this.context.getSystemService(ShortcutManager.class);
        if (shortcutManager.isRequestPinShortcutSupported()) {
            ShortcutInfo shortcutInfoBuild = new ShortcutInfo.Builder(this.context, str).build();
            shortcutManager.requestPinShortcut(shortcutInfoBuild, PendingIntent.getBroadcast(this.context, 0, shortcutManager.createShortcutResultIntent(shortcutInfoBuild), AccessibilityEventCompat.TYPE_VIEW_TARGETED_BY_SCROLL).getIntentSender());
            callbackContext.success();
            return;
        }
        callbackContext.error("Not supported");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void removeShortcut(String str, CallbackContext callbackContext) {
        try {
            ArrayList arrayList = new ArrayList();
            arrayList.add(str);
            ShortcutManagerCompat.removeDynamicShortcuts(this.context, arrayList);
            callbackContext.success();
        } catch (Exception e) {
            callbackContext.error(e.toString());
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setUiTheme(String str, JSONObject jSONObject, CallbackContext callbackContext) {
        try {
            try {
                this.systemBarColor = Color.parseColor(str);
                this.theme = new Ui.Theme(jSONObject);
                this.preferences.set("BackgroundColor", this.systemBarColor);
                this.webView.getPluginManager().postMessage("updateSystemBars", (Object) null);
                applySystemBarTheme();
                if (jSONObject != null) {
                    try {
                        SharedPreferences.Editor editorEdit = this.activity.getApplicationContext().getSharedPreferences("acode_theme", 0).edit();
                        Iterator<String> itKeys = jSONObject.keys();
                        while (itKeys.hasNext()) {
                            String next = itKeys.next();
                            editorEdit.putString(next, jSONObject.optString(next));
                        }
                        editorEdit.apply();
                    } catch (Exception unused) {
                    }
                }
                callbackContext.success();
            } catch (IllegalArgumentException unused2) {
                callbackContext.error("Invalid color: " + str);
            }
        } catch (Exception e) {
            callbackContext.error(e.toString());
        }
    }

    private void applySystemBarTheme() {
        Window window = this.activity.getWindow();
        View decorView = window.getDecorView();
        window.clearFlags(201326592);
        window.addFlags(Integer.MIN_VALUE);
        if (Build.VERSION.SDK_INT >= 29) {
            window.setNavigationBarContrastEnforced(false);
            window.setStatusBarContrastEnforced(false);
        }
        decorView.setBackgroundColor(this.systemBarColor);
        View viewFindViewById = this.activity.findViewById(R.id.content);
        if (viewFindViewById != null) {
            viewFindViewById.setBackgroundColor(this.systemBarColor);
        }
        window.setStatusBarColor(this.systemBarColor);
        window.setNavigationBarColor(this.systemBarColor);
        setStatusBarStyle(window);
        setNavigationBarStyle(window);
    }

    private void setStatusBarStyle(Window window) {
        String type = this.theme.getType();
        View decorView = window.getDecorView();
        if (Build.VERSION.SDK_INT <= 30) {
            int deprecatedSystemUiVisibility = getDeprecatedSystemUiVisibility(decorView);
            int iDeprecatedFlagUiLightStatusBar = deprecatedFlagUiLightStatusBar();
            if (type.equals("light")) {
                setDeprecatedSystemUiVisibility(decorView, deprecatedSystemUiVisibility | iDeprecatedFlagUiLightStatusBar);
                return;
            } else {
                setDeprecatedSystemUiVisibility(decorView, (~iDeprecatedFlagUiLightStatusBar) & deprecatedSystemUiVisibility);
                return;
            }
        }
        int systemBarsAppearance = ((WindowInsetsController) Objects.requireNonNull(decorView.getWindowInsetsController())).getSystemBarsAppearance();
        if (type.equals("light")) {
            decorView.getWindowInsetsController().setSystemBarsAppearance(systemBarsAppearance | 8, 8);
        } else {
            decorView.getWindowInsetsController().setSystemBarsAppearance(systemBarsAppearance & (-9), 8);
        }
    }

    private void setNavigationBarStyle(Window window) {
        String type = this.theme.getType();
        View decorView = window.getDecorView();
        if (Build.VERSION.SDK_INT <= 30) {
            int deprecatedSystemUiVisibility = getDeprecatedSystemUiVisibility(decorView);
            if (type.equals("light")) {
                setDeprecatedSystemUiVisibility(decorView, deprecatedSystemUiVisibility | 16);
                return;
            } else {
                setDeprecatedSystemUiVisibility(decorView, deprecatedSystemUiVisibility & (-17));
                return;
            }
        }
        int systemBarsAppearance = ((WindowInsetsController) Objects.requireNonNull(decorView.getWindowInsetsController())).getSystemBarsAppearance();
        if (type.equals("light")) {
            decorView.getWindowInsetsController().setSystemBarsAppearance(systemBarsAppearance | 16, 16);
        } else {
            decorView.getWindowInsetsController().setSystemBarsAppearance(systemBarsAppearance & (-17), 16);
        }
    }

    private int getDeprecatedSystemUiVisibility(View view) {
        return view.getSystemUiVisibility();
    }

    private void setDeprecatedSystemUiVisibility(View view, int i) {
        view.setSystemUiVisibility(i);
    }

    private void getCordovaIntent(CallbackContext callbackContext) {
        Intent intent = this.activity.getIntent();
        if (isReservedAuthIntent(intent)) {
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
        } else {
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, getIntentJson(intent)));
        }
    }

    private void setIntentHandler(CallbackContext callbackContext) {
        this.intentHandler = callbackContext;
        PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
        pluginResult.setKeepCallback(true);
        callbackContext.sendPluginResult(pluginResult);
    }

    public void onNewIntent(Intent intent) {
        if (isReservedAuthIntent(intent) || this.intentHandler == null) {
            return;
        }
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, getIntentJson(intent));
        pluginResult.setKeepCallback(true);
        this.intentHandler.sendPluginResult(pluginResult);
    }

    private boolean isReservedAuthIntent(Intent intent) {
        Uri data = intent != null ? intent.getData() : null;
        if (data != null && "acode".equals(data.getScheme())) {
            String host = data.getHost();
            String path = data.getPath();
            if ("auth".equals(host) && "/callback".equals(path)) {
                return true;
            }
        }
        return false;
    }

    private JSONObject getIntentJson(Intent intent) {
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject.put("action", intent.getAction());
            jSONObject.put("data", intent.getDataString());
            jSONObject.put("type", intent.getType());
            jSONObject.put("package", intent.getPackage());
            jSONObject.put("extras", getExtrasJson(intent.getExtras()));
            return jSONObject;
        } catch (JSONException e) {
            e.printStackTrace();
            return jSONObject;
        }
    }

    private JSONObject getExtrasJson(Bundle bundle) {
        JSONObject jSONObject = new JSONObject();
        if (bundle != null) {
            for (String str : bundle.keySet()) {
                try {
                    Object obj = bundle.get(str);
                    if (obj instanceof String) {
                        jSONObject.put(str, (String) obj);
                    } else if (obj instanceof Integer) {
                        jSONObject.put(str, (Integer) obj);
                    } else if (obj instanceof Long) {
                        jSONObject.put(str, (Long) obj);
                    } else if (obj instanceof Double) {
                        jSONObject.put(str, (Double) obj);
                    } else if (obj instanceof Float) {
                        jSONObject.put(str, (Float) obj);
                    } else if (obj instanceof Boolean) {
                        jSONObject.put(str, (Boolean) obj);
                    } else if (obj instanceof Bundle) {
                        jSONObject.put(str, getExtrasJson((Bundle) obj));
                    } else {
                        jSONObject.put(str, obj.toString());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return jSONObject;
    }

    private Uri getContentProviderUri(String str) {
        return getContentProviderUri(str, "");
    }

    private Uri getContentProviderUri(String str, String str2) {
        Uri uri;
        if (str == null || str.isEmpty() || (uri = Uri.parse(str)) == null) {
            return null;
        }
        if (!"file".equalsIgnoreCase(uri.getScheme())) {
            return uri;
        }
        File file = new File(uri.getPath());
        if (!file.exists()) {
            Log.e("System", "File does not exist for URI: " + str);
            return null;
        }
        String fileProviderAuthority = getFileProviderAuthority();
        if (fileProviderAuthority == null) {
            Log.e("System", "No FileProvider authority available.");
            return null;
        }
        try {
            try {
                return FileProvider.getUriForFile(this.context, fileProviderAuthority, file);
            } catch (Exception e) {
                Log.e("System", "Failed to expose file via FileProvider", e);
                return null;
            }
        } catch (IllegalArgumentException | SecurityException unused) {
            return FileProvider.getUriForFile(this.context, fileProviderAuthority, ensureShareableCopy(file, str2));
        }
    }

    private File ensureShareableCopy(File file, String str) throws IOException {
        File file2 = new File(this.context.getCacheDir(), "shared");
        if (!file2.exists() && !file2.mkdirs()) {
            throw new IOException("Unable to create shared cache directory");
        }
        if (str != null && !str.isEmpty()) {
            str = new File(str).getName();
        }
        if (str == null || str.isEmpty()) {
            str = file.getName();
        }
        if (str == null || str.isEmpty()) {
            str = "shared-file";
        }
        File fileEnsureUniqueFile = ensureUniqueFile(new File(file2, str));
        copyFile(file, fileEnsureUniqueFile);
        return fileEnsureUniqueFile;
    }

    private File ensureUniqueFile(File file) {
        String strSubstring;
        if (!file.exists()) {
            return file;
        }
        String name = file.getName();
        int iLastIndexOf = name.lastIndexOf(46);
        if (iLastIndexOf <= 0) {
            strSubstring = "";
        } else {
            String strSubstring2 = name.substring(0, iLastIndexOf);
            strSubstring = name.substring(iLastIndexOf);
            name = strSubstring2;
        }
        File file2 = file;
        int i = 1;
        while (file2.exists()) {
            file2 = new File(file.getParentFile(), name + "-" + i + strSubstring);
            i++;
        }
        return file2;
    }

    private void copyFile(File file, File file2) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file2);
            try {
                byte[] bArr = new byte[8192];
                while (true) {
                    int i = fileInputStream.read(bArr);
                    if (i != -1) {
                        fileOutputStream.write(bArr, 0, i);
                    } else {
                        fileOutputStream.flush();
                        fileOutputStream.close();
                        fileInputStream.close();
                        return;
                    }
                    try {
                        fileInputStream.close();
                    } catch (Throwable th) {
                        th.addSuppressed(th);
                    }
                    throw th;
                }
            } catch (Throwable th2) {
                try {
                    fileOutputStream.close();
                } catch (Throwable th3) {
                    th2.addSuppressed(th3);
                }
                throw th2;
            }
        } catch (Throwable th4) {
            fileInputStream.close();
            throw th4;
        }
    }

    private void grantUriPermissions(Intent intent, Uri uri, int i) {
        if (uri == null) {
            return;
        }
        Iterator<ResolveInfo> it = this.context.getPackageManager().queryIntentActivities(intent, 65536).iterator();
        while (it.hasNext()) {
            this.context.grantUriPermission(it.next().activityInfo.packageName, uri, i);
        }
    }

    private String resolveMimeType(String str, Uri uri, String str2) {
        String path;
        if (str != null && !str.isEmpty() && !str.equals("*/*")) {
            return str;
        }
        String type = uri != null ? this.context.getContentResolver().getType(uri) : null;
        if ((type == null || type.isEmpty()) && str2 != null) {
            type = getMimeTypeFromExtension(str2);
        }
        if ((type == null || type.isEmpty()) && uri != null && (path = uri.getPath()) != null) {
            type = getMimeTypeFromExtension(path);
        }
        return (type == null || type.isEmpty()) ? "*/*" : type;
    }

    private String getFileProviderAuthority() {
        String str = this.fileProviderAuthority;
        if (str != null && !str.isEmpty()) {
            return this.fileProviderAuthority;
        }
        try {
            PackageInfo packageInfo = this.context.getPackageManager().getPackageInfo(this.context.getPackageName(), 8);
            if (packageInfo.providers != null) {
                for (ProviderInfo providerInfo : packageInfo.providers) {
                    if (providerInfo != null && providerInfo.name != null && providerInfo.name.equals(FileProvider.class.getName())) {
                        this.fileProviderAuthority = providerInfo.authority;
                        break;
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "Unable to inspect package providers for FileProvider authority.", e);
        }
        String str2 = this.fileProviderAuthority;
        if (str2 == null || str2.isEmpty()) {
            this.fileProviderAuthority = this.context.getPackageName() + ".provider";
        }
        return this.fileProviderAuthority;
    }

    private boolean isPackageInstalled(String str, PackageManager packageManager, CallbackContext callbackContext) {
        try {
            packageManager.getPackageInfo(str, 0);
            return true;
        } catch (PackageManager.NameNotFoundException unused) {
            return false;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void getGlobalSetting(String str, CallbackContext callbackContext) {
        callbackContext.success((int) Settings.Global.getFloat(this.context.getContentResolver(), str, -1.0f));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void clearCache(CallbackContext callbackContext) {
        this.webView.clearCache(true);
        callbackContext.success("Cache cleared");
    }

    private void setInputType(String str) {
        int i;
        if (str.equals("NO_SUGGESTIONS")) {
            i = 0;
        } else {
            i = str.equals("NO_SUGGESTIONS_AGGRESSIVE") ? 1 : -1;
        }
        this.webView.setInputType(i);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setNativeContextMenuDisabled(boolean z) {
        CordovaWebView cordovaWebView = this.webView;
        if (cordovaWebView == null) {
            return;
        }
        cordovaWebView.setNativeContextMenuDisabled(z);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Code duplicated, block: B:36:0x003b A[EXC_TOP_SPLITTER, SYNTHETIC] */
    public void extractAsset(String str, String str2, CallbackContext callbackContext) {
        try {
            InputStream inputStreamOpen = this.context.getAssets().open(str);
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(str2);
                try {
                    byte[] bArr = new byte[8192];
                    while (true) {
                        int i = inputStreamOpen.read(bArr);
                        if (i == -1) {
                            break;
                        } else {
                            fileOutputStream.write(bArr, 0, i);
                        }
                        if (inputStreamOpen != null) {
                            try {
                                inputStreamOpen.close();
                            } catch (Throwable th) {
                                th.addSuppressed(th);
                            }
                        }
                        throw th;
                    }
                    fileOutputStream.flush();
                    callbackContext.success();
                    fileOutputStream.close();
                    if (inputStreamOpen != null) {
                        inputStreamOpen.close();
                    }
                } catch (Throwable th2) {
                    try {
                        fileOutputStream.close();
                    } catch (Throwable th3) {
                        th2.addSuppressed(th3);
                    }
                    throw th2;
                }
            } catch (Throwable th4) {
                if (inputStreamOpen != null) {
                    inputStreamOpen.close();
                }
                throw th4;
            }
        } catch (IOException e) {
            StringWriter stringWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(stringWriter));
            callbackContext.error(stringWriter.toString());
        }
    }
}