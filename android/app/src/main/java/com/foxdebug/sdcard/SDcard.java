package com.foxdebug.sdcard;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.DocumentsContract;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import androidx.documentfile.provider.DocumentFile;
import com.sshtools.common.files.nio.AbstractFilePath;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes.dex */
public class SDcard extends CordovaPlugin {
    private int REQUEST_CODE;
    private Activity activity;
    private CallbackContext activityResultCallback;
    private ContentResolver contentResolver;
    private Context context;
    private int mode;
    private DocumentFile originalRootFile;
    private StorageManager storageManager;
    private WorkspaceIndex workspaceIndex;
    private int SDK_INT = Build.VERSION.SDK_INT;
    private final int ACCESS_INTENT = 6000;
    private final int DOCUMENT_TREE = 6001;
    private final int OPEN_DOCUMENT = 6002;
    private final int PICK_FROM_GALLERY = 6003;
    private final String SEPARATOR = "::";
    private HashMap<String, MyFileObserver> fileObservers = new HashMap<>();

    public void initialize(CordovaInterface cordovaInterface, CordovaWebView cordovaWebView) {
        super.initialize(cordovaInterface, cordovaWebView);
        Objects.requireNonNull(this);
        this.REQUEST_CODE = 6000;
        this.context = cordovaInterface.getContext();
        this.activity = cordovaInterface.getActivity();
        this.contentResolver = this.context.getContentResolver();
        this.workspaceIndex = new WorkspaceIndex(this.context);
        this.storageManager = (StorageManager) this.activity.getSystemService("storage");
    }

    public boolean execute(String str, JSONArray jSONArray, CallbackContext callbackContext) throws JSONException {
        int length = jSONArray.length();
        String string = length > 0 ? jSONArray.getString(0) : null;
        String string2 = length > 1 ? jSONArray.getString(1) : null;
        String string3 = length > 2 ? jSONArray.getString(2) : null;
        str.hashCode();
        switch (str) {
            case "writeText":
                writeText(string, string2, string3, callbackContext);
                return true;
            case "delete":
                delete(formatUri(string), callbackContext);
                return true;
            case "exists":
                exists(formatUri(string), callbackContext);
                return true;
            case "open document file":
                openDocumentFile(string, callbackContext);
                return true;
            case "storage permission":
                getStorageAccess(string, callbackContext);
                return true;
            case "readAsText":
                readAsText(string, string2, callbackContext);
                return true;
            case "rename":
                rename(string, string2, callbackContext);
                return true;
            case "get image":
                getImage(string, callbackContext);
                return true;
            case "workspace cancel":
                this.workspaceIndex.cancel(string);
                callbackContext.success("OK");
                return true;
            case "create file":
                createFile(string, string2, callbackContext);
                return true;
            case "watch file":
                watchFile(string, string2, callbackContext);
                return true;
            case "workspace search":
                this.workspaceIndex.search(jSONArray.optJSONObject(0) == null ? new JSONObject() : jSONArray.optJSONObject(0), callbackContext);
                return true;
            case "workspace update":
                this.workspaceIndex.update(jSONArray.optJSONObject(0) == null ? new JSONObject() : jSONArray.optJSONObject(0), callbackContext);
                return true;
            case "copy":
                copy(string, string2, callbackContext);
                return true;
            case "move":
                move(string, string2, callbackContext);
                return true;
            case "read":
                readFile(string, callbackContext);
                return true;
            case "workspace mark dirty":
                this.workspaceIndex.markDirty(jSONArray.optJSONArray(0));
                callbackContext.success("OK");
                return true;
            case "stats":
                getStats(string, callbackContext);
                return true;
            case "write":
                writeFile(formatUri(string), jSONArray.optString(1), Boolean.valueOf(jSONArray.optBoolean(2)), callbackContext);
                return true;
            case "format uri":
                callbackContext.success(formatUri(string));
                return true;
            case "workspace scan":
                this.workspaceIndex.scan(jSONArray.optJSONObject(0) == null ? new JSONObject() : jSONArray.optJSONObject(0), callbackContext);
                return true;
            case "create directory":
                createDir(string, string2, callbackContext);
                return true;
            case "list directory":
                if (string.contains("::")) {
                    String[] strArrSplit = string.split("::", 2);
                    string = strArrSplit[0];
                    string2 = strArrSplit[1];
                }
                listDir(string, string2, callbackContext);
                return true;
            case "workspace clear":
                this.workspaceIndex.clear(jSONArray.optJSONArray(0));
                callbackContext.success("OK");
                return true;
            case "workspace query":
                this.workspaceIndex.query(jSONArray.optJSONObject(0) == null ? new JSONObject() : jSONArray.optJSONObject(0), callbackContext);
                return true;
            case "list volumes":
                getStorageVolumes(callbackContext);
                return true;
            case "get path":
                getPath(formatUri(string), string2, callbackContext);
                return true;
            case "unwatch file":
                unwatchFile(string);
                return true;
            default:
                return false;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String formatUri(String str) {
        if (!str.contains("::")) {
            return str;
        }
        String[] strArrSplit = str.split("::", 2);
        return getUri(strArrSplit[0], strArrSplit[1]).toString();
    }

    private void watchFile(final String str, final String str2, final CallbackContext callbackContext) {
        this.activity.runOnUiThread(new Runnable() { // from class: com.foxdebug.sdcard.SDcard.1
            @Override // java.lang.Runnable
            public void run() {
                MyFileObserver myFileObserver;
                File file = new File(Uri.parse(str).getPath());
                if (!file.exists()) {
                    callbackContext.error("File not found");
                    return;
                }
                if (SDcard.this.SDK_INT >= 29) {
                    myFileObserver = new MyFileObserver(file, callbackContext);
                } else {
                    myFileObserver = new MyFileObserver(str, callbackContext);
                }
                myFileObserver.startObserving();
                SDcard.this.fileObservers.put(str2, myFileObserver);
            }
        });
    }

    private void unwatchFile(String str) {
        MyFileObserver myFileObserver = this.fileObservers.get(str);
        if (myFileObserver == null) {
            return;
        }
        myFileObserver.stopObserving();
        this.fileObservers.remove(str);
    }

    public void openDocumentFile(String str, CallbackContext callbackContext) {
        Intent intent = new Intent();
        if (str == null) {
            str = "*/*";
        }
        intent.setAction("android.intent.action.OPEN_DOCUMENT");
        intent.addCategory("android.intent.category.OPENABLE");
        intent.setType(str);
        intent.addFlags(67);
        this.activityResultCallback = callbackContext;
        CordovaInterface cordovaInterface = this.cordova;
        Objects.requireNonNull(this);
        cordovaInterface.startActivityForResult(this, intent, 6002);
    }

    public void getImage(String str, CallbackContext callbackContext) {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        if (str == null) {
            str = "image/*";
        }
        intent.addCategory("android.intent.category.OPENABLE");
        intent.setType(str);
        this.activityResultCallback = callbackContext;
        CordovaInterface cordovaInterface = this.cordova;
        Objects.requireNonNull(this);
        cordovaInterface.startActivityForResult(this, intent, 6003);
    }

    public void getStorageVolumes(CallbackContext callbackContext) {
        try {
            JSONArray jSONArray = new JSONArray();
            if (this.SDK_INT >= 24) {
                for (StorageVolume storageVolume : this.storageManager.getStorageVolumes()) {
                    String description = storageVolume.getDescription(this.context);
                    String uuid = storageVolume.getUuid();
                    JSONObject jSONObject = new JSONObject();
                    if (description != null && uuid != null) {
                        jSONObject.put("uuid", uuid);
                        jSONObject.put("name", description);
                        if (this.SDK_INT >= 30) {
                            jSONObject.put("path", storageVolume.getDirectory().getAbsolutePath());
                        }
                        jSONArray.put(jSONObject);
                    }
                }
            } else {
                File externalStorageDirectory = Environment.getExternalStorageDirectory();
                if (Environment.getExternalStorageState(externalStorageDirectory) == "mounted") {
                    String name = externalStorageDirectory.getName();
                    String path = externalStorageDirectory.getPath();
                    String absolutePath = externalStorageDirectory.getAbsolutePath();
                    JSONObject jSONObject2 = new JSONObject();
                    jSONObject2.put("name", name);
                    jSONObject2.put("path", path);
                    jSONObject2.put("absolutePath", absolutePath);
                    jSONArray.put(jSONObject2);
                }
            }
            callbackContext.success(jSONArray);
        } catch (JSONException e) {
            callbackContext.error(e.toString());
        }
    }

    public void getStorageAccess(String str, CallbackContext callbackContext) {
        Intent intent = null;
        if (this.SDK_INT >= 24) {
            StorageVolume storageVolume = null;
            for (StorageVolume storageVolume2 : this.storageManager.getStorageVolumes()) {
                String uuid = storageVolume2.getUuid();
                if (uuid != null && uuid.equals(str)) {
                    storageVolume = storageVolume2;
                }
            }
            if (storageVolume != null) {
                int i = this.SDK_INT;
                if (i < 29) {
                    intent = storageVolume.createAccessIntent(null);
                } else if (i >= 29) {
                    intent = storageVolume.createOpenDocumentTreeIntent();
                }
            }
        }
        if (intent == null) {
            this.REQUEST_CODE = 6001;
            intent = new Intent("android.intent.action.OPEN_DOCUMENT_TREE");
        }
        this.activityResultCallback = callbackContext;
        this.cordova.startActivityForResult(this, intent, this.REQUEST_CODE);
    }

    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        CallbackContext callbackContext = this.activityResultCallback;
        if (callbackContext == null) {
            Log.e("SDcard", "activityResultCallback is null");
            return;
        }
        if (i2 == 0) {
            callbackContext.error("Operation cancelled");
            return;
        }
        if (i == 6003) {
            if (i2 != -1) {
                callbackContext.error("Operation cancelled");
                return;
            }
            if (intent == null) {
                callbackContext.error("No result returned from picker");
                return;
            }
            Uri data = intent.getData();
            if (data == null && intent.getClipData() != null && intent.getClipData().getItemCount() > 0) {
                data = intent.getClipData().getItemAt(0).getUri();
            }
            if (data == null) {
                this.activityResultCallback.error("No file selected");
                return;
            } else {
                this.activityResultCallback.success(data.toString());
                return;
            }
        }
        if (i == 6002) {
            if (i2 == -1) {
                try {
                    if (intent == null) {
                        callbackContext.error("No result returned from picker");
                        return;
                    }
                    Uri data2 = intent.getData();
                    if (data2 == null) {
                        this.activityResultCallback.error("No file selected");
                        return;
                    }
                    boolean zTakePermission = takePermission(data2, intent.getFlags());
                    DocumentFile documentFileFromSingleUri = DocumentFile.fromSingleUri(this.context, data2);
                    JSONObject jSONObject = new JSONObject();
                    jSONObject.put("length", documentFileFromSingleUri.length());
                    jSONObject.put("type", documentFileFromSingleUri.getType());
                    jSONObject.put("filename", documentFileFromSingleUri.getName());
                    jSONObject.put("canWrite", canWrite(documentFileFromSingleUri.getUri()));
                    jSONObject.put("uri", data2.toString());
                    jSONObject.put("persistedUriPermission", zTakePermission);
                    this.activityResultCallback.success(jSONObject);
                    return;
                } catch (JSONException e) {
                    this.activityResultCallback.error(e.toString());
                    return;
                }
            }
            return;
        }
        if (i == 6001 || i == 6000) {
            if (i == 6000 && i2 == 0) {
                callbackContext.error("Canceled");
                return;
            }
            try {
                if (intent == null) {
                    callbackContext.error("No result returned from picker");
                    return;
                }
                Uri data3 = intent.getData();
                if (data3 == null) {
                    this.activityResultCallback.error("Empty uri");
                    return;
                }
                takePermission(data3, intent.getFlags());
                DocumentFile documentFileFromTreeUri = DocumentFile.fromTreeUri(this.context, data3);
                if (documentFileFromTreeUri != null && documentFileFromTreeUri.canWrite()) {
                    this.activityResultCallback.success(data3.toString());
                } else {
                    this.activityResultCallback.error("No write permission: " + data3.toString());
                }
            } catch (Exception e2) {
                this.activityResultCallback.error(e2.toString());
            }
        }
    }

    private void readFile(final String str, final CallbackContext callbackContext) {
        this.cordova.getThreadPool().execute(new Runnable() { // from class: com.foxdebug.sdcard.SDcard.2
            @Override // java.lang.Runnable
            public void run() {
                try {
                    InputStream inputStreamOpenInputStream = SDcard.this.context.getContentResolver().openInputStream(Uri.parse(str));
                    if (inputStreamOpenInputStream == null) {
                        callbackContext.error("File not found");
                        return;
                    }
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    byte[] bArr = new byte[1024];
                    while (true) {
                        int i = inputStreamOpenInputStream.read(bArr, 0, 1024);
                        if (i != -1) {
                            byteArrayOutputStream.write(bArr, 0, i);
                        } else {
                            inputStreamOpenInputStream.close();
                            callbackContext.success(byteArrayOutputStream.toByteArray());
                            return;
                        }
                    }
                } catch (Exception e) {
                    callbackContext.error(e.toString());
                }
            }
        });
    }

    private void readAsText(final String str, final String str2, final CallbackContext callbackContext) {
        this.cordova.getThreadPool().execute(new Runnable() { // from class: com.foxdebug.sdcard.SDcard.3
            @Override // java.lang.Runnable
            public void run() {
                try {
                    Uri uri = Uri.parse(SDcard.this.formatUri(str));
                    String str3 = str2;
                    if (str3 == null || str3.isEmpty() || "auto".equalsIgnoreCase(str3)) {
                        str3 = "UTF-8";
                    }
                    if (!Charset.isSupported(str3)) {
                        callbackContext.error("Charset not supported: " + str3);
                        return;
                    }
                    Charset charsetForName = Charset.forName(str3);
                    InputStream inputStreamOpenInputStream = SDcard.this.context.getContentResolver().openInputStream(uri);
                    if (inputStreamOpenInputStream == null) {
                        callbackContext.error("File not found");
                        return;
                    }
                    StringBuilder sb = new StringBuilder();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStreamOpenInputStream, charsetForName));
                    try {
                        char[] cArr = new char[8192];
                        while (true) {
                            int i = bufferedReader.read(cArr);
                            if (i != -1) {
                                sb.append(cArr, 0, i);
                            } else {
                                bufferedReader.close();
                                callbackContext.success(sb.toString());
                                return;
                            }
                            callbackContext.error(e.toString());
                        }
                    } catch (Throwable th) {
                        try {
                            bufferedReader.close();
                        } catch (Throwable th2) {
                            th.addSuppressed(th2);
                        }
                        throw th;
                    }
                } catch (Exception e) {
                    callbackContext.error(e.toString());
                }
            }
        });
    }

    private void writeText(final String str, final String str2, final String str3, final CallbackContext callbackContext) {
        final Context context = this.context;
        this.cordova.getThreadPool().execute(new Runnable() { // from class: com.foxdebug.sdcard.SDcard.4
            @Override // java.lang.Runnable
            public void run() {
                try {
                    DocumentFile file = SDcard.this.getFile(SDcard.this.formatUri(str));
                    if (file == null) {
                        callbackContext.error("File not found.");
                        return;
                    }
                    if (SDcard.this.canWrite(file.getUri())) {
                        String str4 = str3;
                        if (str4 == null || str4.isEmpty() || "auto".equalsIgnoreCase(str4)) {
                            str4 = "UTF-8";
                        }
                        if (!Charset.isSupported(str4)) {
                            callbackContext.error("Charset not supported: " + str4);
                            return;
                        }
                        Charset charsetForName = Charset.forName(str4);
                        OutputStream outputStreamOpenOutputStream = context.getContentResolver().openOutputStream(file.getUri(), "rwt");
                        try {
                            outputStreamOpenOutputStream.write(str2.getBytes(charsetForName));
                            outputStreamOpenOutputStream.flush();
                            if (outputStreamOpenOutputStream != null) {
                                outputStreamOpenOutputStream.close();
                            }
                            callbackContext.success("OK");
                            return;
                        } catch (Throwable th) {
                            if (outputStreamOpenOutputStream != null) {
                                try {
                                    outputStreamOpenOutputStream.close();
                                } catch (Throwable th2) {
                                    th.addSuppressed(th2);
                                }
                            }
                            throw th;
                        }
                    }
                    callbackContext.error("No write permission");
                } catch (Exception e) {
                    callbackContext.error(e.toString());
                }
            }
        });
    }

    private void writeFile(final String str, final String str2, final Boolean bool, final CallbackContext callbackContext) {
        final Context context = this.context;
        this.cordova.getThreadPool().execute(new Runnable() { // from class: com.foxdebug.sdcard.SDcard.5
            @Override // java.lang.Runnable
            public void run() {
                try {
                    DocumentFile file = SDcard.this.getFile(str);
                    if (file == null) {
                        callbackContext.error("File not found.");
                        return;
                    }
                    if (SDcard.this.canWrite(file.getUri())) {
                        OutputStream outputStreamOpenOutputStream = context.getContentResolver().openOutputStream(file.getUri(), "rwt");
                        PrintWriter printWriter = new PrintWriter(outputStreamOpenOutputStream, true);
                        if (bool.booleanValue()) {
                            outputStreamOpenOutputStream.write(Base64.decode(str2, 0));
                        } else {
                            printWriter.print(str2);
                        }
                        printWriter.flush();
                        printWriter.close();
                        outputStreamOpenOutputStream.close();
                        callbackContext.success("OK");
                        return;
                    }
                    callbackContext.error("No write permission");
                } catch (Exception e) {
                    callbackContext.error(e.toString());
                }
            }
        });
    }

    private void createDir(String str, String str2, CallbackContext callbackContext) {
        create(str, str2, "vnd.android.document/directory", callbackContext);
    }

    private void createFile(String str, String str2, CallbackContext callbackContext) {
        String str3;
        String strGuessContentTypeFromName = URLConnection.guessContentTypeFromName(str2);
        String extension = FilenameUtils.getExtension(str2);
        if (strGuessContentTypeFromName == null && extension != null) {
            str3 = "text/" + extension;
        } else {
            str3 = "text/plain";
        }
        create(str, str2, str3, callbackContext);
    }

    private void create(final String str, final String str2, final String str3, final CallbackContext callbackContext) {
        this.cordova.getThreadPool().execute(new Runnable() { // from class: com.foxdebug.sdcard.SDcard.6
            @Override // java.lang.Runnable
            public void run() {
                String str4;
                Uri uriBuildDocumentUriUsingTree;
                try {
                    if (str.contains("::")) {
                        String[] strArrSplit = str.split("::", 2);
                        str4 = strArrSplit[0];
                        uriBuildDocumentUriUsingTree = SDcard.this.getUri(str4, strArrSplit[1]);
                    } else {
                        str4 = str;
                        Uri uri = Uri.parse(str4);
                        uriBuildDocumentUriUsingTree = DocumentsContract.buildDocumentUriUsingTree(uri, DocumentsContract.getTreeDocumentId(uri));
                    }
                    Uri uriCreateDocument = DocumentsContract.createDocument(SDcard.this.context.getContentResolver(), uriBuildDocumentUriUsingTree, str3, str2);
                    DocumentFile documentFileFromTreeUri = DocumentFile.fromTreeUri(SDcard.this.context, uriCreateDocument);
                    Log.i("SDcard", "Uri: " + uriCreateDocument.toString());
                    if (!str2.equals(documentFileFromTreeUri.getName()) && documentFileFromTreeUri.renameTo(str2)) {
                        uriCreateDocument = documentFileFromTreeUri.getUri();
                    }
                    String documentId = DocumentsContract.getDocumentId(uriCreateDocument);
                    if (uriCreateDocument != null) {
                        callbackContext.success(str4 + "::" + documentId);
                    } else {
                        callbackContext.error("Unable to create " + str);
                    }
                } catch (Exception e) {
                    Log.e("CREATE_FILE", "Unable to create file", e);
                    callbackContext.error(e.toString());
                }
            }
        });
    }

    private void rename(final String str, final String str2, final CallbackContext callbackContext) {
        this.cordova.getThreadPool().execute(new Runnable() { // from class: com.foxdebug.sdcard.SDcard.7
            @Override // java.lang.Runnable
            public void run() {
                String str3;
                Uri uri;
                if (str.contains("::")) {
                    String[] strArrSplit = str.split("::", 2);
                    str3 = strArrSplit[0];
                    uri = SDcard.this.getUri(str3, strArrSplit[1]);
                } else {
                    str3 = str;
                    uri = Uri.parse(str3);
                }
                try {
                    DocumentFile documentFileFromTreeUri = DocumentFile.fromTreeUri(SDcard.this.context, uri);
                    if (str2.equalsIgnoreCase(documentFileFromTreeUri.getName())) {
                        documentFileFromTreeUri.renameTo(str2 + "_temp");
                    }
                    if (documentFileFromTreeUri.renameTo(str2)) {
                        documentFileFromTreeUri.getName();
                        callbackContext.success(str3 + "::" + DocumentsContract.getDocumentId(documentFileFromTreeUri.getUri()));
                        return;
                    }
                    callbackContext.error("Unable to rename: " + str);
                } catch (Exception e) {
                    callbackContext.error(e.getMessage());
                }
            }
        });
    }

    private void delete(final String str, final CallbackContext callbackContext) {
        final ContentResolver contentResolver = this.context.getContentResolver();
        this.cordova.getThreadPool().execute(new Runnable() { // from class: com.foxdebug.sdcard.SDcard.8
            @Override // java.lang.Runnable
            public void run() {
                try {
                    if (DocumentsContract.deleteDocument(contentResolver, Uri.parse(str))) {
                        callbackContext.success(str);
                    } else {
                        callbackContext.error("Unable to delete file " + str);
                    }
                } catch (FileNotFoundException e) {
                    callbackContext.error(e.toString());
                }
            }
        });
    }

    private void move(final String str, String str2, final CallbackContext callbackContext) {
        final ContentResolver contentResolver = this.context.getContentResolver();
        String[] strArrSplit = str.split("::", 2);
        final String str3 = strArrSplit[0];
        final String str4 = strArrSplit[1];
        final String str5 = str2.split("::", 2)[1];
        this.cordova.getThreadPool().execute(new Runnable() { // from class: com.foxdebug.sdcard.SDcard.9
            @Override // java.lang.Runnable
            public void run() {
                try {
                    Uri uriCopy = SDcard.this.copy(str3, str4, str5);
                    if (uriCopy == null) {
                        callbackContext.error("Unable to copy " + str);
                    } else {
                        DocumentsContract.deleteDocument(contentResolver, SDcard.this.getUri(str3, str4));
                        callbackContext.success(str3 + "::" + DocumentsContract.getDocumentId(uriCopy));
                    }
                } catch (Exception e) {
                    callbackContext.error(e.toString());
                }
            }
        });
    }

    private void copy(final String str, String str2, final CallbackContext callbackContext) {
        String[] strArrSplit = str.split("::", 2);
        final String str3 = strArrSplit[0];
        final String str4 = strArrSplit[1];
        final String str5 = str2.split("::", 2)[1];
        this.cordova.getThreadPool().execute(new Runnable() { // from class: com.foxdebug.sdcard.SDcard.10
            @Override // java.lang.Runnable
            public void run() {
                try {
                    Uri uriCopy = SDcard.this.copy(str3, str4, str5);
                    if (uriCopy == null) {
                        callbackContext.error("Unable to copy " + str);
                    } else {
                        callbackContext.success(str3 + "::" + DocumentsContract.getDocumentId(uriCopy));
                    }
                } catch (Exception e) {
                    callbackContext.error(e.toString());
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Uri copy(String str, String str2, String str3) throws IOException {
        Uri uri = getUri(str, str2);
        Uri uri2 = getUri(str, str3);
        DocumentFile file = getFile(uri);
        DocumentFile file2 = getFile(uri2);
        ContentResolver contentResolver = this.context.getContentResolver();
        if (file.isFile()) {
            Uri uriCopyFile = copyFile(file, file2);
            if (uriCopyFile == null) {
                return null;
            }
            return uriCopyFile;
        }
        Uri uriCreateDocument = DocumentsContract.createDocument(contentResolver, uri2, "vnd.android.document/directory", file.getName());
        String documentId = DocumentsContract.getDocumentId(uriCreateDocument);
        Cursor cursorQuery = contentResolver.query(DocumentsContract.buildChildDocumentsUriUsingTree(Uri.parse(str), str2), new String[]{"document_id"}, null, null, null);
        if (cursorQuery != null) {
            while (cursorQuery.moveToNext()) {
                copy(str, cursorQuery.getString(0), documentId);
            }
            cursorQuery.close();
            return uriCreateDocument;
        }
        DocumentsContract.deleteDocument(contentResolver, uriCreateDocument);
        return null;
    }

    private Uri copyFile(DocumentFile documentFile, DocumentFile documentFile2) throws IOException {
        ContentResolver contentResolver = this.context.getContentResolver();
        Uri uriCreateDocument = DocumentsContract.createDocument(contentResolver, documentFile2.getUri(), documentFile.getType(), documentFile.getName());
        DocumentFile file = getFile(uriCreateDocument);
        InputStream inputStreamOpenInputStream = contentResolver.openInputStream(documentFile.getUri());
        OutputStream outputStreamOpenOutputStream = contentResolver.openOutputStream(file.getUri(), "rwt");
        if (inputStreamOpenInputStream == null || outputStreamOpenOutputStream == null) {
            DocumentsContract.deleteDocument(contentResolver, uriCreateDocument);
            return null;
        }
        IOUtils.copy(inputStreamOpenInputStream, outputStreamOpenOutputStream);
        inputStreamOpenInputStream.close();
        outputStreamOpenOutputStream.close();
        if (documentFile.length() == file.length()) {
            return file.getUri();
        }
        DocumentsContract.deleteDocument(contentResolver, uriCreateDocument);
        return null;
    }

    private void listDir(final String str, final String str2, final CallbackContext callbackContext) {
        this.cordova.getThreadPool().execute(new Runnable() { // from class: com.foxdebug.sdcard.SDcard.11
            @Override // java.lang.Runnable
            public void run() {
                ContentResolver contentResolver = SDcard.this.context.getContentResolver();
                String treeDocumentId = str2;
                JSONArray jSONArray = new JSONArray();
                try {
                    Uri uri = Uri.parse(str);
                    if (treeDocumentId == null) {
                        if (!DocumentsContract.isTreeUri(uri)) {
                            Log.w("sdCard", "Cannot list non-tree URI: " + str);
                            callbackContext.error("Cannot read directory.");
                            return;
                        }
                        treeDocumentId = DocumentsContract.getTreeDocumentId(uri);
                    }
                    Cursor cursorQuery = contentResolver.query(DocumentsContract.buildChildDocumentsUriUsingTree(uri, treeDocumentId), new String[]{"document_id", "_display_name", "mime_type"}, null, null, null);
                    if (cursorQuery == null) {
                        callbackContext.error("Cannot read directory.");
                        return;
                    }
                    while (cursorQuery.moveToNext()) {
                        try {
                            try {
                                try {
                                    JSONObject jSONObject = new JSONObject();
                                    String string = cursorQuery.getString(0);
                                    String string2 = cursorQuery.getString(1);
                                    String string3 = cursorQuery.getString(2);
                                    boolean zIsDirectory = SDcard.this.isDirectory(string3);
                                    jSONObject.put("name", string2);
                                    jSONObject.put("mime", string3);
                                    jSONObject.put("isDirectory", zIsDirectory);
                                    jSONObject.put("isFile", !zIsDirectory);
                                    jSONObject.put("uri", str + "::" + string);
                                    jSONObject.put("url", str + "::" + string);
                                    jSONArray.put(jSONObject);
                                } catch (JSONException e) {
                                    callbackContext.error(e.toString());
                                    if (cursorQuery != null) {
                                        try {
                                            cursorQuery.close();
                                            return;
                                        } catch (RuntimeException e2) {
                                            throw e2;
                                        }
                                    }
                                    return;
                                }
                            } catch (Throwable th) {
                                if (cursorQuery != null) {
                                    try {
                                        cursorQuery.close();
                                    } catch (RuntimeException e3) {
                                        throw e3;
                                    } catch (Exception unused) {
                                    }
                                }
                                throw th;
                            }
                        } catch (Exception unused2) {
                            return;
                        }
                    }
                    callbackContext.success(jSONArray);
                    if (cursorQuery != null) {
                        try {
                            cursorQuery.close();
                        } catch (RuntimeException e4) {
                            throw e4;
                        }
                    }
                } catch (Error | IllegalArgumentException | NullPointerException | SecurityException e5) {
                    Log.d("sdCard", "lsDir: " + str);
                    Log.e("sdCard", "lsDir", e5);
                    callbackContext.error("Cannot read directory.");
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isDirectory(String str) {
        return "vnd.android.document/directory".equals(str);
    }

    private void getStats(final String str, final CallbackContext callbackContext) {
        this.cordova.getThreadPool().execute(new Runnable() { // from class: com.foxdebug.sdcard.SDcard.12
            @Override // java.lang.Runnable
            public void run() {
                try {
                    DocumentFile file = SDcard.this.getFile(SDcard.this.formatUri(str));
                    JSONObject jSONObject = new JSONObject();
                    jSONObject.put("exists", file.exists());
                    jSONObject.put("canRead", file.canRead());
                    jSONObject.put("canWrite", SDcard.this.canWrite(file.getUri()));
                    jSONObject.put("name", file.getName());
                    jSONObject.put("length", file.length());
                    jSONObject.put("type", file.getType());
                    jSONObject.put("isFile", file.isFile());
                    jSONObject.put("isDirectory", file.isDirectory());
                    jSONObject.put("isVirtual", file.isVirtual());
                    jSONObject.put("lastModified", file.lastModified());
                    jSONObject.put("url", file.getUri().toString());
                    callbackContext.success(jSONObject);
                } catch (Exception e) {
                    callbackContext.error(e.getMessage());
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Uri getUri(String str, String str2) {
        Uri uri = Uri.parse(str);
        return DocumentsContract.buildDocumentUriUsingTree(DocumentsContract.buildDocumentUriUsingTree(uri, DocumentsContract.getTreeDocumentId(uri)), str2);
    }

    private void exists(String str, CallbackContext callbackContext) {
        DocumentFile documentFileFromSingleUri = DocumentFile.fromSingleUri(this.context, Uri.parse(str));
        if (documentFileFromSingleUri == null) {
            callbackContext.error("Unable to get file");
        } else if (documentFileFromSingleUri.exists()) {
            callbackContext.success("TRUE");
        } else {
            callbackContext.success("FALSE");
        }
    }

    private void error(String str, CallbackContext callbackContext) {
        callbackContext.error("ERROR: " + str);
    }

    private void getPath(String str, String str2, CallbackContext callbackContext) {
        try {
            DocumentFile documentFileGeRelativeDocumentFile = geRelativeDocumentFile(str, str2);
            if (documentFileGeRelativeDocumentFile == null) {
                callbackContext.error("Unable to get file");
                return;
            }
            Uri uri = documentFileGeRelativeDocumentFile.getUri();
            if (uri.getPath() != null) {
                callbackContext.success(uri.toString());
            } else {
                callbackContext.error("Unable to get path");
            }
        } catch (Exception e) {
            callbackContext.error(e.getMessage());
        }
    }

    private DocumentFile geRelativeDocumentFile(String str, String str2) {
        ArrayList arrayList = new ArrayList();
        DocumentFile documentFileFromTreeUri = DocumentFile.fromTreeUri(this.context, Uri.parse(str));
        if (!canWrite(documentFileFromTreeUri.getUri())) {
            throw new RuntimeException("Cannot write file");
        }
        arrayList.addAll(Arrays.asList(str2.split(AbstractFilePath.SEPARATOR)));
        while (arrayList.size() > 0) {
            String str3 = (String) arrayList.remove(0);
            TextUtils.join(AbstractFilePath.SEPARATOR, arrayList);
            if (!str3.equals("") && (documentFileFromTreeUri = documentFileFromTreeUri.findFile(str3)) == null) {
                return null;
            }
        }
        return documentFileFromTreeUri;
    }

    private DocumentFile getFile(Uri uri) {
        return getFile(uri.toString());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public DocumentFile getFile(String str) {
        Uri uri = Uri.parse(str);
        if (str.matches("file:///(.*)")) {
            return DocumentFile.fromFile(new File(uri.getPath()));
        }
        return DocumentFile.fromSingleUri(this.context, uri);
    }

    private boolean takePermission(Uri uri, int i) {
        int i2 = i & 3;
        if (i2 == 0) {
            return false;
        }
        try {
            ContentResolver contentResolver = this.context.getContentResolver();
            this.contentResolver = contentResolver;
            contentResolver.takePersistableUriPermission(uri, i2);
            return true;
        } catch (IllegalArgumentException | SecurityException e) {
            Log.w("SDcard", "Unable to persist URI permission for " + uri, e);
            return false;
        }
    }

    public boolean canWrite(Uri uri) {
        try {
            OutputStream outputStreamOpenOutputStream = this.context.getContentResolver().openOutputStream(uri, "wa");
            if (outputStreamOpenOutputStream == null) {
                return false;
            }
            outputStreamOpenOutputStream.close();
            return true;
        } catch (IOException | IllegalArgumentException | SecurityException unused) {
            return false;
        }
    }
}