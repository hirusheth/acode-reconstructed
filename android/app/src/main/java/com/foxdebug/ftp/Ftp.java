package com.foxdebug.ftp;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import com.sshtools.common.files.nio.AbstractFilePath;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.parser.ParserInitializationException;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONObject;

/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes.dex */
public class Ftp extends CordovaPlugin {
    Activity activity;
    String connectionID;
    Context context;
    HashMap<String, FTPClient> ftpProfiles = new HashMap<>();

    public void initialize(CordovaInterface cordovaInterface, CordovaWebView cordovaWebView) {
        super.initialize(cordovaInterface, cordovaWebView);
        this.context = cordovaInterface.getContext();
        this.activity = cordovaInterface.getActivity();
    }

    public boolean execute(String str, JSONArray jSONArray, CallbackContext callbackContext) {
        try {
            Method declaredMethod = getClass().getDeclaredMethod(str, JSONArray.class, CallbackContext.class);
            if (declaredMethod == null) {
                return false;
            }
            declaredMethod.invoke(this, jSONArray, callbackContext);
            return true;
        } catch (NoSuchMethodException e) {
            callbackContext.error(e.getMessage());
            return false;
        } catch (SecurityException e2) {
            callbackContext.error(e2.getMessage());
            return false;
        } catch (Exception e3) {
            callbackContext.error(e3.getMessage());
            return false;
        }
    }

    public void connect(JSONArray jSONArray, CallbackContext callbackContext) {
        connect(jSONArray, callbackContext, false);
    }

    public void connect(final JSONArray jSONArray, final CallbackContext callbackContext, final boolean z) {
        this.cordova.getThreadPool().execute(new Runnable() { // from class: com.foxdebug.ftp.Ftp.1
            /* JADX WARN: Code duplicated, block: B:23:0x011b A[Catch: Exception -> 0x019e, IOException -> 0x01d4, TryCatch #5 {IOException -> 0x01d4, Exception -> 0x019e, blocks: (B:3:0x004d, B:5:0x0057, B:21:0x00f5, B:23:0x011b, B:25:0x012c, B:27:0x014f, B:29:0x0170, B:24:0x0124, B:18:0x00d4), top: B:56:0x004d }] */
            /* JADX WARN: Code duplicated, block: B:24:0x0124 A[Catch: Exception -> 0x019e, IOException -> 0x01d4, TryCatch #5 {IOException -> 0x01d4, Exception -> 0x019e, blocks: (B:3:0x004d, B:5:0x0057, B:21:0x00f5, B:23:0x011b, B:25:0x012c, B:27:0x014f, B:29:0x0170, B:24:0x0124, B:18:0x00d4), top: B:56:0x004d }] */
            /* JADX WARN: Code duplicated, block: B:27:0x014f A[Catch: Exception -> 0x019e, IOException -> 0x01d4, TryCatch #5 {IOException -> 0x01d4, Exception -> 0x019e, blocks: (B:3:0x004d, B:5:0x0057, B:21:0x00f5, B:23:0x011b, B:25:0x012c, B:27:0x014f, B:29:0x0170, B:24:0x0124, B:18:0x00d4), top: B:56:0x004d }] */
            /* JADX WARN: Code duplicated, block: B:29:0x0170 A[Catch: Exception -> 0x019e, IOException -> 0x01d4, TRY_LEAVE, TryCatch #5 {IOException -> 0x01d4, Exception -> 0x019e, blocks: (B:3:0x004d, B:5:0x0057, B:21:0x00f5, B:23:0x011b, B:25:0x012c, B:27:0x014f, B:29:0x0170, B:24:0x0124, B:18:0x00d4), top: B:56:0x004d }] */
            /* JADX WARN: Code duplicated, block: B:38:0x01b5  */
            /* JADX WARN: Code duplicated, block: B:41:0x01c0  */
            /* JADX WARN: Code duplicated, block: B:43:0x01ca  */
            /* JADX WARN: Code duplicated, block: B:47:0x01eb  */
            /* JADX WARN: Code duplicated, block: B:50:0x01f6  */
            /* JADX WARN: Code duplicated, block: B:52:0x0200  */
            @Override // java.lang.Runnable
            public void run() {
                FTPClient fTPClient;
                int iOptInt = jSONArray.optInt(1);
                String strOptString = jSONArray.optString(0);
                String strOptString2 = jSONArray.optString(2);
                String strOptString3 = jSONArray.optString(3);
                jSONArray.optString(4);
                jSONArray.optString(5);
                String strOptString4 = jSONArray.optString(6);
                jSONArray.optString(7);
                jSONArray.optString(8);
                String ftpId = Ftp.this.getFtpId(strOptString, iOptInt, strOptString2);
                FTPClient fTPClient2 = null;
                try {
                    if (!Ftp.this.ftpProfiles.containsKey(ftpId)) {
                        Log.d("FTP", "Creating new FTPClient (" + ftpId + ")");
                        FTPClient fTPClient3 = new FTPClient();
                        try {
                            Ftp.this.ftpProfiles.put(ftpId, fTPClient3);
                            fTPClient = fTPClient3;
                            Log.d("FTP", "FTPClient (" + ftpId + ") connecting...");
                            fTPClient.connect(strOptString, iOptInt);
                            fTPClient.setControlKeepAliveTimeout(300L);
                            if (strOptString4.equals("active")) {
                                Log.d("FTP", "Entering Local Active mode");
                                fTPClient.enterLocalActiveMode();
                            } else {
                                Log.d("FTP", "Entering Passive Active mode");
                                fTPClient.enterLocalPassiveMode();
                            }
                            Log.d("FTP", "FTPClient (" + ftpId + ") logging in...");
                            fTPClient.login(strOptString2, strOptString3);
                            if (!FTPReply.isPositiveCompletion(fTPClient.getReplyCode())) {
                                fTPClient.disconnect();
                                Log.d("FTP", "FTPClient (" + ftpId + ") server refused connection.");
                                callbackContext.error("FTP server refused connection.");
                            } else {
                                fTPClient.setListHiddenFiles(true);
                                Ftp.this.ftpProfiles.put(ftpId, fTPClient);
                                Log.d("FTP", "FTPClient (" + ftpId + ") connected");
                                callbackContext.success(ftpId);
                            }
                        } catch (IOException e) {
                            fTPClient2 = fTPClient3;
                            e = e;
                            Log.e("FTP", "FTPClient (" + ftpId + ")", e);
                            if (fTPClient2 != null) {
                                Ftp.this.ftpProfiles.remove(ftpId);
                            }
                            if (z) {
                                callbackContext.error(e.getMessage());
                            } else {
                                Ftp.this.connect(jSONArray, callbackContext, true);
                            }
                        } catch (Exception e2) {
                            fTPClient2 = fTPClient3;
                            e = e2;
                            Log.e("FTP", "FTPClient (" + ftpId + ")", e);
                            if (fTPClient2 != null) {
                                Ftp.this.ftpProfiles.remove(ftpId);
                            }
                            if (z) {
                                callbackContext.error(e.getMessage());
                            } else {
                                Ftp.this.connect(jSONArray, callbackContext, true);
                            }
                        }
                    }
                    FTPClient fTPClient4 = Ftp.this.ftpProfiles.get(ftpId);
                    try {
                        int replyCode = fTPClient4.getReplyCode();
                        if (fTPClient4.isConnected() && FTPReply.isPositiveCompletion(replyCode)) {
                            fTPClient4.setControlEncoding("UTF-8");
                            fTPClient4.setAutodetectUTF8(true);
                            System.setProperty("ftp.client.encoding", "UTF-8");
                            fTPClient4.sendNoOp();
                            Log.d("FTP", "FTPClient (" + ftpId + ") is connected");
                            callbackContext.success(ftpId);
                            return;
                        }
                        Log.d("FTP", "FTPClient (" + ftpId + ") is not connected");
                        fTPClient4.disconnect();
                        Log.d("FTP", "FTPClient (" + ftpId + ") disconnecting...");
                        fTPClient = fTPClient4;
                        Log.d("FTP", "FTPClient (" + ftpId + ") connecting...");
                        fTPClient.connect(strOptString, iOptInt);
                        fTPClient.setControlKeepAliveTimeout(300L);
                        if (strOptString4.equals("active")) {
                            Log.d("FTP", "Entering Local Active mode");
                            fTPClient.enterLocalActiveMode();
                        } else {
                            Log.d("FTP", "Entering Passive Active mode");
                            fTPClient.enterLocalPassiveMode();
                        }
                        Log.d("FTP", "FTPClient (" + ftpId + ") logging in...");
                        fTPClient.login(strOptString2, strOptString3);
                        if (!FTPReply.isPositiveCompletion(fTPClient.getReplyCode())) {
                            fTPClient.disconnect();
                            Log.d("FTP", "FTPClient (" + ftpId + ") server refused connection.");
                            callbackContext.error("FTP server refused connection.");
                        } else {
                            fTPClient.setListHiddenFiles(true);
                            Ftp.this.ftpProfiles.put(ftpId, fTPClient);
                            Log.d("FTP", "FTPClient (" + ftpId + ") connected");
                            callbackContext.success(ftpId);
                        }
                    } catch (IOException e3) {
                        e = e3;
                        fTPClient2 = fTPClient4;
                        Log.e("FTP", "FTPClient (" + ftpId + ")", e);
                        if (fTPClient2 != null) {
                            Ftp.this.ftpProfiles.remove(ftpId);
                        }
                        if (z) {
                            Ftp.this.connect(jSONArray, callbackContext, true);
                        } else {
                            callbackContext.error(e.getMessage());
                        }
                    } catch (Exception e4) {
                        e = e4;
                        fTPClient2 = fTPClient4;
                        Log.e("FTP", "FTPClient (" + ftpId + ")", e);
                        if (fTPClient2 != null) {
                            Ftp.this.ftpProfiles.remove(ftpId);
                        }
                        if (z) {
                            Ftp.this.connect(jSONArray, callbackContext, true);
                        } else {
                            callbackContext.error(e.getMessage());
                        }
                    }
                } catch (IOException e5) {
                    e = e5;
                } catch (Exception e6) {
                    e = e6;
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static String getBaseName(String str) {
        int iLastIndexOf = str.lastIndexOf(47);
        if (iLastIndexOf == str.length() - 1) {
            return getBaseName(str.substring(0, iLastIndexOf));
        }
        return str.substring(iLastIndexOf + 1);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static String getParentPath(String str) {
        int iLastIndexOf = str.lastIndexOf(47);
        if (iLastIndexOf == str.length() - 1) {
            iLastIndexOf = str.substring(0, iLastIndexOf).lastIndexOf(47);
        }
        return str.substring(0, iLastIndexOf);
    }

    public void listDirectory(final JSONArray jSONArray, final CallbackContext callbackContext) {
        this.cordova.getThreadPool().execute(new Runnable() { // from class: com.foxdebug.ftp.Ftp.2
            /* JADX WARN: Multi-variable type inference failed */
            @Override // java.lang.Runnable
            public void run() {
                Object[] objArr;
                String str;
                boolean z;
                try {
                    try {
                        boolean z2 = false;
                        String strOptString = jSONArray.optString(0);
                        boolean z3 = true;
                        String strOptString2 = jSONArray.optString(1);
                        if (strOptString == null || strOptString.isEmpty()) {
                            callbackContext.error("FTP ID is required.");
                            return;
                        }
                        String str2 = AbstractFilePath.SEPARATOR;
                        if (strOptString2 == null || strOptString2.isEmpty()) {
                            strOptString2 = AbstractFilePath.SEPARATOR;
                        }
                        FTPClient fTPClient = Ftp.this.ftpProfiles.get(strOptString);
                        if (fTPClient == null) {
                            callbackContext.error("FTP client not found.");
                            return;
                        }
                        FTPFile[] fTPFileArrListFiles = fTPClient.listFiles(strOptString2);
                        Log.d("FTP", "FTPClient (" + strOptString + ") Listing files in " + strOptString2);
                        Log.d("FTP", "FTPClient (" + strOptString + ") Found " + fTPFileArrListFiles.length + " files.");
                        JSONArray jSONArray2 = new JSONArray();
                        int length = fTPFileArrListFiles.length;
                        int i = 0;
                        while (i < length) {
                            FTPFile fTPFile = fTPFileArrListFiles[i];
                            String name = fTPFile.getName();
                            if (name.equals(".") || name.equals("..")) {
                                String str3 = str2;
                                objArr = z2 ? 1 : 0;
                                str = str3;
                                z = z3;
                            } else {
                                JSONObject jSONObject = new JSONObject();
                                jSONObject.put("name", name);
                                String str4 = str2;
                                jSONObject.put("length", fTPFile.getSize());
                                jSONObject.put("url", Ftp.this.joinPath(strOptString2, name));
                                if (fTPFile.isSymbolicLink()) {
                                    jSONObject.put("isLink", true);
                                    String link = fTPFile.getLink();
                                    jSONObject.put("link", link);
                                    if (!link.startsWith(str4)) {
                                        link = Ftp.this.joinPath(strOptString2, link);
                                    }
                                    try {
                                        FTPFile[] fTPFileArrListFiles2 = fTPClient.listFiles(link);
                                        Object[] objArr2 = z2 ? 1 : 0;
                                        try {
                                            if (fTPFileArrListFiles2.length > 0) {
                                                try {
                                                    FTPFile fTPFile2 = fTPFileArrListFiles2[objArr2 == true ? 1 : 0];
                                                    jSONObject.put("isFile", fTPFile2.isFile());
                                                    jSONObject.put("isDirectory", fTPFile2.isDirectory());
                                                    jSONObject.put("url", link);
                                                } catch (Exception unused) {
                                                    z2 = objArr2 == true ? 1 : 0;
                                                    jSONObject.put("isFile", z2);
                                                    jSONObject.put("isDirectory", z2);
                                                }
                                            } else {
                                                z2 = objArr2 == true ? 1 : 0;
                                                jSONObject.put("isFile", z2);
                                                jSONObject.put("isDirectory", z2);
                                            }
                                        } catch (Exception unused2) {
                                            z2 = false;
                                        }
                                    } catch (Exception unused3) {
                                    }
                                    str = str4;
                                } else {
                                    boolean z4 = z2 ? 1 : 0;
                                    str = str4;
                                    jSONObject.put("isLink", z4);
                                    jSONObject.put("isDirectory", fTPFile.isDirectory());
                                    jSONObject.put("isFile", fTPFile.isFile());
                                    jSONObject.put("link", (Object) null);
                                }
                                jSONObject.put("lastModified", fTPFile.getTimestamp().getTimeInMillis());
                                objArr = null;
                                z = true;
                                jSONObject.put("canWrite", fTPFile.hasPermission(0, 1));
                                jSONObject.put("canRead", fTPFile.hasPermission(0, 0));
                                jSONArray2.put(jSONObject);
                            }
                            i++;
                            Object[] objArr3 = objArr;
                            str2 = str;
                            z2 = objArr3 == true ? 1 : 0;
                            z3 = z;
                        }
                        callbackContext.success(jSONArray2);
                    } catch (Exception e) {
                        callbackContext.error(e.getMessage());
                    }
                } catch (FTPConnectionClosedException e2) {
                    callbackContext.error(e2.getMessage());
                } catch (IOException e3) {
                    callbackContext.error(e3.getMessage());
                } catch (ParserInitializationException e4) {
                    callbackContext.error(e4.getMessage());
                }
            }
        });
    }

    public void exists(final JSONArray jSONArray, final CallbackContext callbackContext) {
        this.cordova.getThreadPool().execute(new Runnable() { // from class: com.foxdebug.ftp.Ftp.3
            @Override // java.lang.Runnable
            public void run() {
                String strOptString = jSONArray.optString(0);
                String strOptString2 = jSONArray.optString(1);
                if (strOptString != null) {
                    try {
                        if (!strOptString.isEmpty()) {
                            if (strOptString2 == null || strOptString2.isEmpty()) {
                                strOptString2 = AbstractFilePath.SEPARATOR;
                            }
                            FTPClient fTPClient = Ftp.this.ftpProfiles.get(strOptString);
                            if (fTPClient == null) {
                                callbackContext.error("FTP client not found.");
                                return;
                            } else if (fTPClient.listFiles(strOptString2).length > 0) {
                                callbackContext.success(1);
                                return;
                            } else {
                                callbackContext.success(0);
                                return;
                            }
                        }
                    } catch (FTPConnectionClosedException e) {
                        Log.e("FTP", "FTPClient (" + strOptString + ") path: " + strOptString2, e);
                        callbackContext.error(e.getMessage());
                        return;
                    } catch (IOException e2) {
                        Log.e("FTP", "FTPClient (" + strOptString + ") path: " + strOptString2, e2);
                        callbackContext.error(e2.getMessage());
                        return;
                    } catch (Exception e3) {
                        Log.e("FTP", "FTPClient (" + strOptString + ") path: " + strOptString2, e3);
                        callbackContext.error(e3.getMessage());
                        return;
                    } catch (ParserInitializationException e4) {
                        Log.e("FTP", "FTPClient (" + strOptString + ") path: " + strOptString2, e4);
                        callbackContext.error(e4.getMessage());
                        Log.e("FTP", "FTPClient (" + strOptString + ") path: " + strOptString2, e4);
                        return;
                    }
                }
                callbackContext.error("FTP ID is required.");
            }
        });
    }

    public void sendNoOp(final JSONArray jSONArray, final CallbackContext callbackContext) {
        this.cordova.getThreadPool().execute(new Runnable() { // from class: com.foxdebug.ftp.Ftp.4
            @Override // java.lang.Runnable
            public void run() {
                try {
                    FTPClient fTPClient = Ftp.this.ftpProfiles.get(jSONArray.optString(0));
                    if (fTPClient == null) {
                        callbackContext.error("FTP client not found.");
                    } else {
                        fTPClient.sendNoOp();
                        callbackContext.success();
                    }
                } catch (Exception e) {
                    callbackContext.error(e.getMessage());
                }
            }
        });
    }

    public void deleteFile(final JSONArray jSONArray, final CallbackContext callbackContext) {
        this.cordova.getThreadPool().execute(new Runnable() { // from class: com.foxdebug.ftp.Ftp.5
            @Override // java.lang.Runnable
            public void run() {
                try {
                    String strOptString = jSONArray.optString(0);
                    String strOptString2 = jSONArray.optString(1);
                    if (strOptString != null && !strOptString.isEmpty()) {
                        if (strOptString2 != null && !strOptString2.isEmpty()) {
                            FTPClient fTPClient = Ftp.this.ftpProfiles.get(strOptString);
                            if (fTPClient == null) {
                                callbackContext.error("FTP client not found.");
                                return;
                            } else {
                                fTPClient.deleteFile(strOptString2);
                                callbackContext.success();
                                return;
                            }
                        }
                        callbackContext.error("Path is required.");
                        return;
                    }
                    callbackContext.error("FTP ID is required.");
                } catch (FTPConnectionClosedException e) {
                    callbackContext.error(e.getMessage());
                } catch (IOException e2) {
                    callbackContext.error(e2.getMessage());
                } catch (Exception e3) {
                    callbackContext.error(e3.getMessage());
                }
            }
        });
    }

    public void deleteDirectory(final JSONArray jSONArray, final CallbackContext callbackContext) {
        this.cordova.getThreadPool().execute(new Runnable() { // from class: com.foxdebug.ftp.Ftp.6
            /* JADX INFO: Thrown type has an unknown type hierarchy: org.apache.commons.net.ftp.FTPConnectionClosedException */
            @Override // java.lang.Runnable
            public void run() {
                try {
                    String strOptString = jSONArray.optString(0);
                    String strOptString2 = jSONArray.optString(1);
                    if (strOptString != null && !strOptString.isEmpty()) {
                        if (strOptString2 != null && !strOptString2.isEmpty()) {
                            FTPClient fTPClient = Ftp.this.ftpProfiles.get(strOptString);
                            if (fTPClient == null) {
                                callbackContext.error("FTP client not found.");
                                return;
                            }
                            Log.d("FTP", "Deleting directory " + strOptString2);
                            Ftp.this.emptyDirectory(strOptString2, fTPClient);
                            callbackContext.success();
                            return;
                        }
                        callbackContext.error("Path is required.");
                        return;
                    }
                    callbackContext.error("FTP ID is required.");
                } catch (Exception e) {
                    callbackContext.error(e.getMessage());
                } catch (FTPConnectionClosedException e2) {
                    callbackContext.error(e2.getMessage());
                } catch (IOException e3) {
                    callbackContext.error(e3.getMessage());
                }
            }
        });
    }

    public void rename(final JSONArray jSONArray, final CallbackContext callbackContext) {
        this.cordova.getThreadPool().execute(new Runnable() { // from class: com.foxdebug.ftp.Ftp.7
            @Override // java.lang.Runnable
            public void run() {
                FTPFile fTPFile;
                try {
                    String strOptString = jSONArray.optString(0);
                    String strOptString2 = jSONArray.optString(1);
                    String strOptString3 = jSONArray.optString(2);
                    if (strOptString != null && !strOptString.isEmpty()) {
                        if (strOptString2 != null && !strOptString2.isEmpty()) {
                            if (strOptString3 != null && !strOptString3.isEmpty()) {
                                FTPClient fTPClient = Ftp.this.ftpProfiles.get(strOptString);
                                if (fTPClient == null) {
                                    callbackContext.error("FTP client not found.");
                                    return;
                                }
                                String parentPath = Ftp.getParentPath(strOptString2);
                                FTPFile[] fTPFileArrListFiles = fTPClient.listFiles(parentPath);
                                Log.d("FTP", "Renaming " + strOptString2 + " to " + strOptString3);
                                fTPClient.rename(strOptString2, strOptString3);
                                if (fTPClient.listFiles(strOptString3).length > 0) {
                                    callbackContext.success(strOptString3);
                                } else {
                                    FTPFile[] fTPFileArrListFiles2 = fTPClient.listFiles(parentPath);
                                    int length = fTPFileArrListFiles2.length;
                                    int i = 0;
                                    loop0: while (true) {
                                        if (i >= length) {
                                            fTPFile = null;
                                            break;
                                        }
                                        fTPFile = fTPFileArrListFiles2[i];
                                        int length2 = fTPFileArrListFiles.length;
                                        int i2 = 0;
                                        while (true) {
                                            if (i2 >= length2) {
                                                break loop0;
                                            } else if (fTPFileArrListFiles[i2].getName().equals(fTPFile.getName())) {
                                                break;
                                            } else {
                                                i2++;
                                            }
                                        }
                                        i++;
                                    }
                                    if (fTPFile != null) {
                                        fTPClient.rename(Ftp.this.joinPath(parentPath, fTPFile.getName()), strOptString2);
                                    }
                                    callbackContext.error("Failed to rename file");
                                }
                                callbackContext.success();
                                return;
                            }
                            callbackContext.error("New path is required.");
                            return;
                        }
                        callbackContext.error("Old path is required.");
                        return;
                    }
                    callbackContext.error("FTP ID is required.");
                } catch (Exception e) {
                    callbackContext.error(e.getMessage());
                } catch (FTPConnectionClosedException e2) {
                    callbackContext.error(e2.getMessage());
                } catch (IOException e3) {
                    callbackContext.error(e3.getMessage());
                }
            }
        });
    }

    public void downloadFile(final JSONArray jSONArray, final CallbackContext callbackContext) {
        this.cordova.getThreadPool().execute(new Runnable() { // from class: com.foxdebug.ftp.Ftp.8
            /* JADX WARN: Code duplicated, block: B:76:0x00d7 A[EXC_TOP_SPLITTER, SYNTHETIC] */
            @Override // java.lang.Runnable
            public void run() {
                try {
                    String strOptString = jSONArray.optString(0);
                    String strOptString2 = jSONArray.optString(1);
                    String strOptString3 = jSONArray.optString(2);
                    if (strOptString != null && !strOptString.isEmpty()) {
                        if (strOptString2 != null && !strOptString2.isEmpty()) {
                            if (strOptString3 != null && !strOptString3.isEmpty()) {
                                File file = new File(new URI(strOptString3));
                                FTPClient fTPClient = Ftp.this.ftpProfiles.get(strOptString);
                                if (fTPClient == null) {
                                    callbackContext.error("FTP client not found.");
                                    return;
                                }
                                fTPClient.setFileType(2);
                                if (file.exists()) {
                                    file.delete();
                                }
                                InputStream inputStreamRetrieveFileStream = fTPClient.retrieveFileStream(strOptString2);
                                try {
                                    if (inputStreamRetrieveFileStream == null) {
                                        Log.d("FTP", "FTPClient (" + strOptString + ") path: " + strOptString2 + " - not found");
                                        callbackContext.error("File not found.");
                                        if (inputStreamRetrieveFileStream != null) {
                                            inputStreamRetrieveFileStream.close();
                                            return;
                                        }
                                        return;
                                    }
                                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                                    try {
                                        byte[] bArr = new byte[1024];
                                        while (true) {
                                            int i = inputStreamRetrieveFileStream.read(bArr);
                                            if (i == -1) {
                                                break;
                                            } else {
                                                fileOutputStream.write(bArr, 0, i);
                                            }
                                            if (inputStreamRetrieveFileStream != null) {
                                                try {
                                                    inputStreamRetrieveFileStream.close();
                                                } catch (Throwable th) {
                                                    th.addSuppressed(th);
                                                }
                                            }
                                            throw th;
                                        }
                                        fileOutputStream.close();
                                        if (inputStreamRetrieveFileStream != null) {
                                            inputStreamRetrieveFileStream.close();
                                        }
                                        if (!fTPClient.completePendingCommand()) {
                                            fTPClient.logout();
                                            fTPClient.disconnect();
                                            callbackContext.error("File transfer failed.");
                                            return;
                                        }
                                        callbackContext.success();
                                        return;
                                    } catch (Throwable th2) {
                                        try {
                                            fileOutputStream.close();
                                        } catch (Throwable th3) {
                                            th2.addSuppressed(th3);
                                        }
                                        throw th2;
                                    }
                                } catch (Throwable th4) {
                                    if (inputStreamRetrieveFileStream != null) {
                                        inputStreamRetrieveFileStream.close();
                                    }
                                    throw th4;
                                }
                            }
                            callbackContext.error("Local file is required.");
                            return;
                        }
                        callbackContext.error("Path is required.");
                        return;
                    }
                    callbackContext.error("FTP ID is required.");
                } catch (IOException e) {
                    callbackContext.error(e.getMessage());
                } catch (URISyntaxException e2) {
                    callbackContext.error(e2.getMessage());
                } catch (Exception e3) {
                    callbackContext.error(e3.getMessage());
                } catch (FTPConnectionClosedException e4) {
                    callbackContext.error(e4.getMessage());
                }
            }
        });
    }

    public void uploadFile(final JSONArray jSONArray, final CallbackContext callbackContext) {
        this.cordova.getThreadPool().execute(new Runnable() { // from class: com.foxdebug.ftp.Ftp.9
            @Override // java.lang.Runnable
            public void run() {
                try {
                    String strOptString = jSONArray.optString(0);
                    String strOptString2 = jSONArray.optString(1);
                    String strOptString3 = jSONArray.optString(2);
                    if (strOptString != null && !strOptString.isEmpty()) {
                        if (strOptString3 != null && !strOptString3.isEmpty()) {
                            if (strOptString2 != null && !strOptString2.isEmpty()) {
                                Log.d("FTPUpload", "uploadFile: " + strOptString2);
                                File file = new File(new URI(strOptString2));
                                FTPClient fTPClient = Ftp.this.ftpProfiles.get(strOptString);
                                if (fTPClient == null) {
                                    callbackContext.error("FTP client not found.");
                                    return;
                                }
                                fTPClient.setFileType(2);
                                Log.d("FTPUpload", "Destination " + strOptString3);
                                FileInputStream fileInputStream = new FileInputStream(file);
                                try {
                                    OutputStream outputStreamStoreFileStream = fTPClient.storeFileStream(strOptString3);
                                    try {
                                        if (outputStreamStoreFileStream == null) {
                                            callbackContext.error("File not found.");
                                            if (outputStreamStoreFileStream != null) {
                                                outputStreamStoreFileStream.close();
                                            }
                                            fileInputStream.close();
                                            return;
                                        }
                                        byte[] bArr = new byte[1024];
                                        while (true) {
                                            int i = fileInputStream.read(bArr);
                                            if (i == -1) {
                                                break;
                                            } else {
                                                outputStreamStoreFileStream.write(bArr, 0, i);
                                            }
                                        }
                                        if (outputStreamStoreFileStream != null) {
                                            outputStreamStoreFileStream.close();
                                        }
                                        fileInputStream.close();
                                        if (fTPClient.completePendingCommand()) {
                                            callbackContext.success();
                                            return;
                                        }
                                        fTPClient.logout();
                                        fTPClient.disconnect();
                                        callbackContext.error("File transfer failed.");
                                        return;
                                    } catch (Throwable th) {
                                        if (outputStreamStoreFileStream != null) {
                                            try {
                                                outputStreamStoreFileStream.close();
                                            } catch (Throwable th2) {
                                                th.addSuppressed(th2);
                                            }
                                        }
                                        throw th;
                                    }
                                } catch (Throwable th3) {
                                    try {
                                        fileInputStream.close();
                                    } catch (Throwable th4) {
                                        th3.addSuppressed(th4);
                                    }
                                    throw th3;
                                }
                            }
                            callbackContext.error("Local file is required.");
                            return;
                        }
                        callbackContext.error("Path is required.");
                        return;
                    }
                    callbackContext.error("FTP ID is required.");
                } catch (FTPConnectionClosedException e) {
                    callbackContext.error(e.getMessage());
                } catch (IOException e2) {
                    callbackContext.error(e2.getMessage());
                } catch (URISyntaxException e3) {
                    callbackContext.error(e3.getMessage());
                } catch (Exception e4) {
                    callbackContext.error(e4.getMessage());
                }
            }
        });
    }

    public void getKeepAlive(final JSONArray jSONArray, final CallbackContext callbackContext) {
        this.cordova.getThreadPool().execute(new Runnable() { // from class: com.foxdebug.ftp.Ftp.10
            @Override // java.lang.Runnable
            public void run() {
                try {
                    String strOptString = jSONArray.optString(0);
                    if (strOptString != null && !strOptString.isEmpty()) {
                        FTPClient fTPClient = Ftp.this.ftpProfiles.get(strOptString);
                        if (fTPClient == null) {
                            callbackContext.error("FTP client not found.");
                            return;
                        } else {
                            callbackContext.success((int) fTPClient.getControlKeepAliveTimeout());
                            return;
                        }
                    }
                    callbackContext.error("FTP ID is required.");
                } catch (Exception e) {
                    callbackContext.error(e.getMessage());
                }
            }
        });
    }

    public void execCommand(final JSONArray jSONArray, final CallbackContext callbackContext) {
        this.cordova.getThreadPool().execute(new Runnable() { // from class: com.foxdebug.ftp.Ftp.11
            @Override // java.lang.Runnable
            public void run() {
                try {
                    String strOptString = jSONArray.optString(0);
                    String strOptString2 = jSONArray.optString(1);
                    if (strOptString != null && !strOptString.isEmpty()) {
                        if (strOptString2 != null && !strOptString2.isEmpty()) {
                            FTPClient fTPClient = Ftp.this.ftpProfiles.get(strOptString);
                            if (fTPClient == null) {
                                callbackContext.error("FTP client not found.");
                                return;
                            }
                            fTPClient.sendCommand(strOptString2);
                            callbackContext.success(fTPClient.getReplyString());
                            return;
                        }
                        callbackContext.error("Command is required.");
                        return;
                    }
                    callbackContext.error("FTP ID is required.");
                } catch (FTPConnectionClosedException e) {
                    callbackContext.error(e.getMessage());
                } catch (Exception e2) {
                    callbackContext.error(e2.getMessage());
                }
            }
        });
    }

    public void isConnected(final JSONArray jSONArray, final CallbackContext callbackContext) {
        this.cordova.getThreadPool().execute(new Runnable() { // from class: com.foxdebug.ftp.Ftp.12
            @Override // java.lang.Runnable
            public void run() {
                try {
                    String strOptString = jSONArray.optString(0);
                    if (strOptString != null && !strOptString.isEmpty()) {
                        FTPClient fTPClient = Ftp.this.ftpProfiles.get(strOptString);
                        if (fTPClient == null) {
                            callbackContext.error("FTP client not found.");
                            return;
                        } else {
                            callbackContext.success(fTPClient.isConnected() ? 1 : 0);
                            return;
                        }
                    }
                    callbackContext.error("FTP ID is required.");
                } catch (Exception e) {
                    Log.e("FTP", "FTPClient", e);
                    callbackContext.error(e.getMessage());
                }
            }
        });
    }

    public void disconnect(final JSONArray jSONArray, final CallbackContext callbackContext) {
        this.cordova.getThreadPool().execute(new Runnable() { // from class: com.foxdebug.ftp.Ftp.13
            @Override // java.lang.Runnable
            public void run() {
                try {
                    String strOptString = jSONArray.optString(0);
                    FTPClient fTPClient = Ftp.this.ftpProfiles.get(strOptString);
                    if (fTPClient != null) {
                        fTPClient.disconnect();
                        Ftp.this.ftpProfiles.remove(strOptString);
                    }
                    callbackContext.success();
                } catch (IOException e) {
                    callbackContext.error(e.getMessage());
                } catch (Exception e2) {
                    callbackContext.error(e2.getMessage());
                }
            }
        });
    }

    public void createDirectory(final JSONArray jSONArray, final CallbackContext callbackContext) {
        this.cordova.getThreadPool().execute(new Runnable() { // from class: com.foxdebug.ftp.Ftp.14
            @Override // java.lang.Runnable
            public void run() {
                try {
                    String strOptString = jSONArray.optString(0);
                    String strOptString2 = jSONArray.optString(1);
                    if (strOptString != null && !strOptString.isEmpty()) {
                        if (strOptString2 != null && !strOptString2.isEmpty()) {
                            FTPClient fTPClient = Ftp.this.ftpProfiles.get(strOptString);
                            if (fTPClient == null) {
                                callbackContext.error("FTP client not found.");
                                return;
                            } else {
                                fTPClient.makeDirectory(strOptString2);
                                callbackContext.success();
                                return;
                            }
                        }
                        callbackContext.error("Path is required.");
                        return;
                    }
                    callbackContext.error("FTP ID is required.");
                } catch (IOException e) {
                    callbackContext.error(e.getMessage());
                } catch (Exception e2) {
                    callbackContext.error(e2.getMessage());
                }
            }
        });
    }

    public void changeDirectory(final JSONArray jSONArray, final CallbackContext callbackContext) {
        this.cordova.getThreadPool().execute(new Runnable() { // from class: com.foxdebug.ftp.Ftp.15
            @Override // java.lang.Runnable
            public void run() {
                try {
                    String strOptString = jSONArray.optString(0);
                    String strOptString2 = jSONArray.optString(1);
                    if (strOptString != null && !strOptString.isEmpty()) {
                        if (strOptString2 != null && !strOptString2.isEmpty()) {
                            FTPClient fTPClient = Ftp.this.ftpProfiles.get(strOptString);
                            if (fTPClient == null) {
                                callbackContext.error("FTP client not found.");
                                return;
                            } else {
                                fTPClient.changeWorkingDirectory(strOptString2);
                                callbackContext.success();
                                return;
                            }
                        }
                        callbackContext.error("Path is required.");
                        return;
                    }
                    callbackContext.error("FTP ID is required.");
                } catch (Exception e) {
                    callbackContext.error(e.getMessage());
                } catch (FTPConnectionClosedException e2) {
                    callbackContext.error(e2.getMessage());
                }
            }
        });
    }

    public void changeToParentDirectory(final JSONArray jSONArray, final CallbackContext callbackContext) {
        this.cordova.getThreadPool().execute(new Runnable() { // from class: com.foxdebug.ftp.Ftp.16
            @Override // java.lang.Runnable
            public void run() {
                try {
                    String strOptString = jSONArray.optString(0);
                    if (strOptString != null && !strOptString.isEmpty()) {
                        FTPClient fTPClient = Ftp.this.ftpProfiles.get(strOptString);
                        if (fTPClient == null) {
                            callbackContext.error("FTP client not found.");
                            return;
                        } else {
                            fTPClient.changeToParentDirectory();
                            callbackContext.success();
                            return;
                        }
                    }
                    callbackContext.error("FTP ID is required.");
                } catch (FTPConnectionClosedException e) {
                    callbackContext.error(e.getMessage());
                } catch (Exception e2) {
                    callbackContext.error(e2.getMessage());
                }
            }
        });
    }

    public void getWorkingDirectory(final JSONArray jSONArray, final CallbackContext callbackContext) {
        this.cordova.getThreadPool().execute(new Runnable() { // from class: com.foxdebug.ftp.Ftp.17
            @Override // java.lang.Runnable
            public void run() {
                try {
                    String strOptString = jSONArray.optString(0);
                    if (strOptString != null && !strOptString.isEmpty()) {
                        FTPClient fTPClient = Ftp.this.ftpProfiles.get(strOptString);
                        if (fTPClient == null) {
                            callbackContext.error("FTP client not found.");
                            return;
                        } else {
                            callbackContext.success(fTPClient.printWorkingDirectory());
                            return;
                        }
                    }
                    callbackContext.error("FTP ID is required.");
                } catch (FTPConnectionClosedException e) {
                    callbackContext.error(e.getMessage());
                } catch (Exception e2) {
                    callbackContext.error(e2.getMessage());
                }
            }
        });
    }

    public void getStat(final JSONArray jSONArray, final CallbackContext callbackContext) {
        this.cordova.getThreadPool().execute(new Runnable() { // from class: com.foxdebug.ftp.Ftp.18
            @Override // java.lang.Runnable
            public void run() {
                try {
                    String strOptString = jSONArray.optString(0);
                    String strOptString2 = jSONArray.optString(1);
                    if (strOptString != null && !strOptString.isEmpty()) {
                        if (strOptString2 != null && !strOptString2.isEmpty()) {
                            FTPClient fTPClient = Ftp.this.ftpProfiles.get(strOptString);
                            if (fTPClient == null) {
                                callbackContext.error("FTP client not found.");
                                return;
                            }
                            FTPFile[] fTPFileArrListFiles = fTPClient.listFiles(strOptString2);
                            if (fTPFileArrListFiles != null && fTPFileArrListFiles.length != 0) {
                                FTPFile fTPFile = fTPFileArrListFiles[0];
                                JSONObject jSONObject = new JSONObject();
                                jSONObject.put("isFile", fTPFile.isFile());
                                jSONObject.put("isValid", fTPFile.isValid());
                                jSONObject.put("isUnknown", fTPFile.isUnknown());
                                jSONObject.put("isDirectory", fTPFile.isDirectory());
                                jSONObject.put("isLink", fTPFile.isSymbolicLink());
                                jSONObject.put("linkCount", fTPFile.getHardLinkCount());
                                jSONObject.put("length", fTPFile.getSize());
                                jSONObject.put("name", Ftp.getBaseName(fTPFile.getName()));
                                jSONObject.put("lastModified", fTPFile.getTimestamp().getTimeInMillis());
                                jSONObject.put("link", fTPFile.getLink());
                                jSONObject.put("group", fTPFile.getGroup());
                                jSONObject.put("user", fTPFile.getUser());
                                jSONObject.put("canWrite", fTPFile.hasPermission(0, 1));
                                jSONObject.put("canRead", fTPFile.hasPermission(0, 0));
                                callbackContext.success(jSONObject);
                                return;
                            }
                            callbackContext.error("File not found.");
                            return;
                        }
                        callbackContext.error("Path is required.");
                        return;
                    }
                    callbackContext.error("FTP ID is required.");
                } catch (ParserInitializationException e) {
                    callbackContext.error(e.getMessage());
                } catch (IOException e2) {
                    callbackContext.error(e2.getMessage());
                } catch (Exception e3) {
                    callbackContext.error(e3.getMessage());
                } catch (FTPConnectionClosedException e4) {
                    callbackContext.error(e4.getMessage());
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String getFtpId(String str, int i, String str2) {
        return str2 + "@" + str + ":" + i;
    }

    private String errMessage(Exception exc) {
        String message = exc.getMessage();
        return (message == null || message.equals("")) ? exc.toString() : message;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: Thrown type has an unknown type hierarchy: org.apache.commons.net.ftp.FTPConnectionClosedException */
    public void emptyDirectory(String str, FTPClient fTPClient) throws FTPConnectionClosedException, IOException {
        for (FTPFile fTPFile : fTPClient.listFiles(str)) {
            String name = fTPFile.getName();
            if (!name.equals(".") && !name.equals("..")) {
                if (fTPFile.isDirectory()) {
                    Log.d("FTP", "Removing directory: " + fTPFile.getName());
                    emptyDirectory(str + AbstractFilePath.SEPARATOR + fTPFile.getName(), fTPClient);
                } else {
                    Log.d("FTP", "Removing file: " + fTPFile.getName());
                    fTPClient.deleteFile(str + AbstractFilePath.SEPARATOR + fTPFile.getName());
                }
            }
        }
        fTPClient.removeDirectory(str);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String joinPath(String str, String str2) {
        if (!str.endsWith(AbstractFilePath.SEPARATOR)) {
            str = str + AbstractFilePath.SEPARATOR;
        }
        return str + str2;
    }
}