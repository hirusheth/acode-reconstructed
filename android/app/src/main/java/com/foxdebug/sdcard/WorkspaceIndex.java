package com.foxdebug.sdcard;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.CancellationSignal;
import android.provider.DocumentsContract;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.silkimen.http.HttpRequest;
import com.sshtools.common.files.nio.AbstractFilePath;
import fi.iki.elonen.NanoHTTPD;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import kotlin.UByte;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes.dex */
class WorkspaceIndex {
    private static final int BATCH_SIZE = 200;
    private static final Set<String> BINARY_EXTENSIONS;
    private static final Set<String> BINARY_MIME_TYPES;
    private static final int DB_VERSION = 2;
    private static final int DIRECT_SEARCH_READ_LIMIT_BYTES = 16777216;
    private static final int EXPLICIT_INCLUDE_READ_LIMIT_BYTES = 134217728;
    private static final int INDEX_READ_LIMIT_BYTES = 2097152;
    private static final int MAX_INDEXED_CHARS = 524288;
    private static final int MAX_MATCHES_PER_FILE = 5000;
    private static final int SAMPLE_BYTES = 8192;
    private static final int SEARCH_RESULT_BATCH_MATCHES = 600;
    private static final int SEARCH_RESULT_BATCH_SIZE = 12;
    private static final String SEPARATOR = "::";
    private static final String TAG = "WorkspaceIndex";
    private static final Set<String> TEXT_EXTENSIONS;
    private static final Set<String> TEXT_MIME_TYPES;
    private final Context context;
    private final DB db;
    private final ContentResolver resolver;
    private final ExecutorService indexExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final Map<String, Job> jobs = new ConcurrentHashMap();

    static {
        HashSet hashSet = new HashSet();
        BINARY_EXTENSIONS = hashSet;
        HashSet hashSet2 = new HashSet();
        TEXT_EXTENSIONS = hashSet2;
        HashSet hashSet3 = new HashSet();
        BINARY_MIME_TYPES = hashSet3;
        HashSet hashSet4 = new HashSet();
        TEXT_MIME_TYPES = hashSet4;
        Collections.addAll(hashSet, "3gp", "7z", "aab", "aac", "apk", "avi", "bin", "bmp", "class", "db", "dex", "dll", "doc", "docx", "eot", "exe", "flac", "gif", "gz", "heic", "ico", "jar", "jpeg", "jpg", "keystore", "m4a", "m4v", "mkv", "mov", "mp3", "mp4", "o", "odt", "ogg", "otf", "pdf", "png", "ppt", "pptx", "pyc", "rar", "so", "sqlite", "sqlite3", "tar", "tgz", "ttf", "wav", "webm", "webp", "woff", "woff2", "xls", "xlsx", "xz", "zip");
        Collections.addAll(hashSet2, "astro", "c", "cc", "cfg", "conf", "cpp", "cs", "css", "csv", "cxx", "dart", "env", "go", "graphql", "h", "hpp", "htm", "html", "java", "js", "json", "jsx", "kt", "kts", "less", "lua", "md", "mjs", "php", "properties", "py", "rb", "rs", "sass", "scss", "sh", "sql", "svg", "swift", "toml", "ts", "tsx", "txt", "vue", "xml", "yaml", "yml");
        Collections.addAll(hashSet3, "application/java-archive", "application/java-vm", "application/octet-stream", "application/pdf", "application/vnd.android.package-archive", "application/zip", "application/x-7z-compressed", "application/x-rar-compressed", "application/x-sqlite3", "application/x-tar", "application/x-xz");
        Collections.addAll(hashSet4, "application/javascript", HttpRequest.CONTENT_TYPE_JSON, "application/ld+json", "application/sql", "application/typescript", "application/x-javascript", "application/x-php", "application/x-sh", "application/x-yaml", "application/xhtml+xml", "application/xml", "image/svg+xml");
    }

    WorkspaceIndex(Context context) {
        Context applicationContext = context.getApplicationContext();
        this.context = applicationContext;
        this.resolver = context.getContentResolver();
        this.db = new DB(applicationContext);
    }

    void scan(final JSONObject jSONObject, final CallbackContext callbackContext) {
        final String strOptString = jSONObject.optString("id", UUID.randomUUID().toString());
        final Job job = new Job(strOptString);
        this.jobs.put(strOptString, job);
        this.indexExecutor.execute(new Runnable() { // from class: com.foxdebug.sdcard.WorkspaceIndex$$ExternalSyntheticLambda1
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m179lambda$scan$0$comfoxdebugsdcardWorkspaceIndex(job, jSONObject, callbackContext, strOptString);
            }
        });
    }

    /* JADX INFO: renamed from: lambda$scan$0$com-foxdebug-sdcard-WorkspaceIndex, reason: not valid java name */
    /* synthetic */ void m179lambda$scan$0$comfoxdebugsdcardWorkspaceIndex(Job job, JSONObject jSONObject, CallbackContext callbackContext, String str) {
        try {
            runScan(job, jSONObject, callbackContext);
        } catch (Exception e) {
            sendError(callbackContext, str, e);
        } finally {
            this.jobs.remove(str);
        }
    }

    void update(final JSONObject jSONObject, final CallbackContext callbackContext) {
        this.indexExecutor.execute(new Runnable() { // from class: com.foxdebug.sdcard.WorkspaceIndex$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m181lambda$update$1$comfoxdebugsdcardWorkspaceIndex(callbackContext, jSONObject);
            }
        });
    }

    /* JADX INFO: renamed from: lambda$update$1$com-foxdebug-sdcard-WorkspaceIndex, reason: not valid java name */
    /* synthetic */ void m181lambda$update$1$comfoxdebugsdcardWorkspaceIndex(CallbackContext callbackContext, JSONObject jSONObject) {
        try {
            callbackContext.success(runUpdate(jSONObject));
        } catch (Exception e) {
            callbackContext.error(e.getMessage() == null ? e.toString() : e.getMessage());
        }
    }

    void search(final JSONObject jSONObject, final CallbackContext callbackContext) {
        final String strOptString = jSONObject.optString("id", UUID.randomUUID().toString());
        final Job job = new Job(strOptString);
        this.jobs.put(strOptString, job);
        this.executor.execute(new Runnable() { // from class: com.foxdebug.sdcard.WorkspaceIndex$$ExternalSyntheticLambda2
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m180lambda$search$2$comfoxdebugsdcardWorkspaceIndex(job, jSONObject, callbackContext, strOptString);
            }
        });
    }

    /* JADX INFO: renamed from: lambda$search$2$com-foxdebug-sdcard-WorkspaceIndex, reason: not valid java name */
    /* synthetic */ void m180lambda$search$2$comfoxdebugsdcardWorkspaceIndex(Job job, JSONObject jSONObject, CallbackContext callbackContext, String str) {
        try {
            runSearch(job, jSONObject, callbackContext);
        } catch (Exception e) {
            sendError(callbackContext, str, e);
        } finally {
            this.jobs.remove(str);
        }
    }

    void query(final JSONObject jSONObject, final CallbackContext callbackContext) {
        this.executor.execute(new Runnable() { // from class: com.foxdebug.sdcard.WorkspaceIndex$$ExternalSyntheticLambda3
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m178lambda$query$3$comfoxdebugsdcardWorkspaceIndex(callbackContext, jSONObject);
            }
        });
    }

    /* JADX INFO: renamed from: lambda$query$3$com-foxdebug-sdcard-WorkspaceIndex, reason: not valid java name */
    /* synthetic */ void m178lambda$query$3$comfoxdebugsdcardWorkspaceIndex(CallbackContext callbackContext, JSONObject jSONObject) {
        try {
            callbackContext.success(runQuery(jSONObject));
        } catch (Exception e) {
            callbackContext.error(e.getMessage() == null ? e.toString() : e.getMessage());
        }
    }

    void cancel(String str) {
        Job job = this.jobs.get(str);
        if (job != null) {
            job.cancel();
        }
    }

    void markDirty(JSONArray jSONArray) {
        SQLiteDatabase writableDatabase = this.db.getWritableDatabase();
        for (int i = 0; i < jSONArray.length(); i++) {
            String strOptString = jSONArray.optString(i, null);
            if (strOptString != null && strOptString.length() != 0) {
                writableDatabase.delete("content", "url = ?", new String[]{strOptString});
            }
        }
    }

    void clear(JSONArray jSONArray) {
        SQLiteDatabase writableDatabase = this.db.getWritableDatabase();
        if (jSONArray == null || jSONArray.length() == 0) {
            writableDatabase.delete("content", null, null);
            writableDatabase.delete("files", null, null);
            writableDatabase.delete("workspaces", null, null);
            return;
        }
        for (int i = 0; i < jSONArray.length(); i++) {
            String strOptString = jSONArray.optString(i, null);
            if (strOptString != null && strOptString.length() != 0) {
                writableDatabase.delete("content", "url IN (SELECT url FROM files WHERE root_url = ?)", new String[]{strOptString});
                writableDatabase.delete("files", "root_url = ?", new String[]{strOptString});
                writableDatabase.delete("workspaces", "root_url = ?", new String[]{strOptString});
            }
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r17v0, types: [java.lang.String] */
    /* JADX WARN: Type inference failed for: r17v1 */
    private void runScan(Job job, JSONObject jSONObject, CallbackContext callbackContext) throws Exception {
        JSONArray jSONArray;
        boolean z;
        ScanStats scanStats;
        String str;
        CallbackContext callbackContext2;
        Job job2;
        if (job.cancelled) {
            send(callbackContext, baseEvent(job.id, "cancelled"), false);
            return;
        }
        String string = jSONObject.getString("rootUrl");
        String strOptString = jSONObject.optString("title", basename(string));
        JSONArray jSONArrayOptJSONArray = jSONObject.optJSONArray("excludeFolders");
        boolean zOptBoolean = jSONObject.optBoolean("showHiddenFiles", false);
        SQLiteDatabase sQLiteDatabaseOptString = jSONObject.optString("defaultEncoding", "UTF-8");
        boolean zOptBoolean2 = jSONObject.optBoolean("indexContent", false);
        boolean zOptBoolean3 = jSONObject.optBoolean("emitEntries", true);
        JSONArray jSONArray2 = new JSONArray();
        ScanStats scanStats2 = new ScanStats();
        sendStatus(callbackContext, job.id, "scanning", "Scanning project files", 0, true);
        SQLiteDatabase writableDatabase = this.db.getWritableDatabase();
        writableDatabase.beginTransaction();
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("root_url", string);
            contentValues.put("title", strOptString);
            contentValues.put("indexed_at", Long.valueOf(System.currentTimeMillis()));
            contentValues.put("options_hash", String.valueOf(jSONObject.toString().hashCode()));
            writableDatabase.replace("workspaces", null, contentValues);
            writableDatabase.delete("files", "root_url = ?", new String[]{string});
            try {
                if (!isSafUrl(string)) {
                    jSONArray = jSONArray2;
                    z = zOptBoolean3;
                    scanStats = scanStats2;
                    str = "files";
                    callbackContext2 = callbackContext;
                    job2 = job;
                    scanFileDir(job2, callbackContext2, string, fileFromUrl(string), string, strOptString, strOptString, jSONArrayOptJSONArray, zOptBoolean, sQLiteDatabaseOptString, zOptBoolean2, z, jSONArray, scanStats);
                } else {
                    SafUrl safUrl = parseSafUrl(string);
                    str = "files";
                    scanSafDir(job, callbackContext, safUrl.treeUrl, string, safUrl.docId, string, strOptString, strOptString, jSONArrayOptJSONArray, zOptBoolean, sQLiteDatabaseOptString, zOptBoolean2, zOptBoolean3, jSONArray2, scanStats2);
                    z = zOptBoolean3;
                    jSONArray = jSONArray2;
                    scanStats = scanStats2;
                    job2 = job;
                    callbackContext2 = callbackContext;
                }
                if (job2.cancelled) {
                    send(callbackContext2, baseEvent(job2.id, "cancelled"), false);
                    writableDatabase.endTransaction();
                    return;
                }
                if (z) {
                    flushBatch(callbackContext2, job2.id, jSONArray);
                }
                writableDatabase.setTransactionSuccessful();
                writableDatabase.endTransaction();
                JSONObject jSONObjectBaseEvent = baseEvent(job2.id, "done");
                jSONObjectBaseEvent.put(str, scanStats.files);
                jSONObjectBaseEvent.put("dirs", scanStats.dirs);
                jSONObjectBaseEvent.put("indexed", scanStats.indexed);
                send(callbackContext2, jSONObjectBaseEvent, false);
            } catch (Throwable th) {
                th = th;
                sQLiteDatabaseOptString.endTransaction();
                throw th;
            }
        } catch (Throwable th2) {
            th = th2;
            sQLiteDatabaseOptString = writableDatabase;
        }
    }

    private JSONObject runUpdate(JSONObject jSONObject) throws Exception {
        int iDeleteIndexedSubtree;
        SQLiteDatabase sQLiteDatabase;
        char c;
        int i;
        String str;
        String str2;
        String str3;
        String str4;
        String str5;
        ScanStats scanStats;
        WorkspaceIndex workspaceIndex = this;
        String str6 = "workspaces";
        String string = jSONObject.getString("rootUrl");
        String str7 = "title";
        String strOptString = jSONObject.optString("title", workspaceIndex.getWorkspaceTitle(string));
        String str8 = "removed";
        JSONArray jSONArrayOptJSONArray = jSONObject.optJSONArray("removed");
        String str9 = "added";
        JSONArray jSONArrayOptJSONArray2 = jSONObject.optJSONArray("added");
        JSONArray jSONArrayOptJSONArray3 = jSONObject.optJSONArray("excludeFolders");
        boolean zOptBoolean = jSONObject.optBoolean("showHiddenFiles", false);
        String strOptString2 = jSONObject.optString("defaultEncoding", "UTF-8");
        SQLiteDatabase writableDatabase = workspaceIndex.db.getWritableDatabase();
        writableDatabase.beginTransaction();
        String str10 = "";
        if (jSONArrayOptJSONArray != null) {
            iDeleteIndexedSubtree = 0;
            for (int i2 = 0; i2 < jSONArrayOptJSONArray.length(); i2++) {
                try {
                    String strOptString3 = jSONArrayOptJSONArray.optString(i2, "");
                    if (strOptString3.length() != 0 && !string.equals(strOptString3)) {
                        iDeleteIndexedSubtree += workspaceIndex.deleteIndexedSubtree(writableDatabase, string, strOptString3);
                    }
                } catch (Throwable th) {
                    th = th;
                    sQLiteDatabase = writableDatabase;
                    sQLiteDatabase.endTransaction();
                    throw th;
                }
            }
        } else {
            iDeleteIndexedSubtree = 0;
        }
        if (jSONArrayOptJSONArray2 != null) {
            int i3 = 0;
            int i4 = 0;
            while (i3 < jSONArrayOptJSONArray2.length()) {
                try {
                    JSONObject jSONObjectOptJSONObject = jSONArrayOptJSONArray2.optJSONObject(i3);
                    try {
                        if (jSONObjectOptJSONObject == null) {
                            str = strOptString2;
                            str2 = str6;
                        } else {
                            str = strOptString2;
                            String strOptString4 = jSONObjectOptJSONObject.optString("url", str10);
                            str2 = str6;
                            String strOptString5 = jSONObjectOptJSONObject.optString("parentUrl", str10);
                            if (strOptString4.length() != 0 && strOptString5.length() != 0) {
                                int iDeleteIndexedSubtree2 = iDeleteIndexedSubtree + workspaceIndex.deleteIndexedSubtree(writableDatabase, string, strOptString4);
                                FileEntry entry = workspaceIndex.readEntry(string, strOptString5, strOptString4, strOptString);
                                if (entry == null) {
                                    str3 = str8;
                                } else {
                                    if (zOptBoolean) {
                                        str3 = str8;
                                    } else {
                                        str3 = str8;
                                        if (entry.name.startsWith(".")) {
                                        }
                                        i3++;
                                        workspaceIndex = this;
                                        string = str4;
                                        strOptString = str5;
                                        str6 = str2;
                                        jSONArrayOptJSONArray2 = jSONArrayOptJSONArray2;
                                        str10 = str10;
                                        writableDatabase = writableDatabase;
                                        str8 = str3;
                                        str9 = str9;
                                        str7 = str7;
                                        strOptString2 = str;
                                    }
                                    workspaceIndex.saveFile(entry);
                                    int i5 = i4 + 1;
                                    if (!entry.isDirectory || workspaceIndex.shouldSkipDirectory(string, entry.url, entry.path, jSONArrayOptJSONArray3)) {
                                        str7 = str7;
                                        str9 = str9;
                                        jSONArrayOptJSONArray2 = jSONArrayOptJSONArray2;
                                        i3 = i3;
                                        writableDatabase = writableDatabase;
                                        str10 = str10;
                                        str3 = str3;
                                        str4 = string;
                                        str5 = strOptString;
                                    } else {
                                        Job job = new Job("update-" + UUID.randomUUID());
                                        ScanStats scanStats2 = new ScanStats();
                                        if (workspaceIndex.isSafUrl(strOptString4)) {
                                            String str11 = workspaceIndex.parseSafUrl(string).treeUrl;
                                            String str12 = workspaceIndex.parseSafUrl(strOptString4).docId;
                                            str5 = strOptString;
                                            String str13 = str7;
                                            str4 = string;
                                            writableDatabase = writableDatabase;
                                            str7 = str13;
                                            workspaceIndex.scanSafDir(job, null, str11, str4, str12, strOptString4, entry.path, str5, jSONArrayOptJSONArray3, zOptBoolean, str, false, false, new JSONArray(), scanStats2);
                                            scanStats = scanStats2;
                                        } else {
                                            str7 = str7;
                                            writableDatabase = writableDatabase;
                                            String str14 = strOptString;
                                            JSONArray jSONArray = jSONArrayOptJSONArray3;
                                            boolean z = zOptBoolean;
                                            scanStats = scanStats2;
                                            workspaceIndex.scanFileDir(job, null, string, workspaceIndex.fileFromUrl(strOptString4), strOptString4, entry.path, str14, jSONArray, z, str, false, false, new JSONArray(), scanStats);
                                            str4 = string;
                                            zOptBoolean = z;
                                            jSONArrayOptJSONArray3 = jSONArray;
                                            str5 = str14;
                                        }
                                        i5 += scanStats.files + scanStats.dirs;
                                    }
                                    iDeleteIndexedSubtree = iDeleteIndexedSubtree2;
                                    i4 = i5;
                                    i3++;
                                    workspaceIndex = this;
                                    string = str4;
                                    strOptString = str5;
                                    str6 = str2;
                                    jSONArrayOptJSONArray2 = jSONArrayOptJSONArray2;
                                    str10 = str10;
                                    writableDatabase = writableDatabase;
                                    str8 = str3;
                                    str9 = str9;
                                    str7 = str7;
                                    strOptString2 = str;
                                }
                                iDeleteIndexedSubtree = iDeleteIndexedSubtree2;
                                str3 = str3;
                            }
                            str4 = string;
                            str5 = strOptString;
                            i3++;
                            workspaceIndex = this;
                            string = str4;
                            strOptString = str5;
                            str6 = str2;
                            jSONArrayOptJSONArray2 = jSONArrayOptJSONArray2;
                            str10 = str10;
                            writableDatabase = writableDatabase;
                            str8 = str3;
                            str9 = str9;
                            str7 = str7;
                            strOptString2 = str;
                        }
                        i3++;
                        workspaceIndex = this;
                        string = str4;
                        strOptString = str5;
                        str6 = str2;
                        jSONArrayOptJSONArray2 = jSONArrayOptJSONArray2;
                        str10 = str10;
                        writableDatabase = writableDatabase;
                        str8 = str3;
                        str9 = str9;
                        str7 = str7;
                        strOptString2 = str;
                    } catch (Throwable th2) {
                        th = th2;
                        sQLiteDatabase = writableDatabase;
                        sQLiteDatabase.endTransaction();
                        throw th;
                    }
                    str3 = str8;
                    str4 = string;
                    str5 = strOptString;
                } catch (Throwable th3) {
                    th = th3;
                    writableDatabase = writableDatabase;
                    sQLiteDatabase = writableDatabase;
                    sQLiteDatabase.endTransaction();
                    throw th;
                }
            }
            c = 0;
            i = i4;
        } else {
            c = 0;
            i = 0;
        }
        String str15 = str6;
        String str16 = str8;
        String str17 = str9;
        writableDatabase = writableDatabase;
        String str18 = string;
        int i6 = iDeleteIndexedSubtree;
        ContentValues contentValues = new ContentValues();
        contentValues.put("root_url", str18);
        contentValues.put(str7, strOptString);
        contentValues.put("indexed_at", Long.valueOf(System.currentTimeMillis()));
        String[] strArr = new String[1];
        strArr[c] = str18;
        sQLiteDatabase = writableDatabase;
        try {
            if (sQLiteDatabase.update(str15, contentValues, "root_url = ?", strArr) == 0) {
                sQLiteDatabase.insert(str15, null, contentValues);
            }
            sQLiteDatabase.setTransactionSuccessful();
            sQLiteDatabase.endTransaction();
            JSONObject jSONObject2 = new JSONObject();
            jSONObject2.put(str17, i);
            jSONObject2.put(str16, i6);
            return jSONObject2;
        } catch (Throwable th4) {
            th = th4;
            sQLiteDatabase.endTransaction();
            throw th;
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r15v0 */
    /* JADX WARN: Type inference failed for: r15v1, types: [android.database.Cursor] */
    /* JADX WARN: Type inference failed for: r15v2 */
    /* JADX WARN: Type inference failed for: r15v6, types: [android.database.Cursor] */
    /* JADX WARN: Type inference failed for: r18v0, types: [com.foxdebug.sdcard.WorkspaceIndex] */
    /* JADX WARN: Type inference failed for: r7v1 */
    /* JADX WARN: Type inference failed for: r7v2, types: [android.database.Cursor] */
    /* JADX WARN: Type inference failed for: r7v4, types: [android.database.Cursor] */
    /* JADX WARN: Type inference failed for: r7v6 */
    private FileEntry readEntry(String str, String str2, String str3, String str4) throws Throwable {
        ?? r15;
        FileEntry fileEntry;
        ?? Query;
        String indexedPath = getIndexedPath(str, str2, str4);
        try {
            if (isSafUrl(str3)) {
                try {
                    fileEntry = null;
                    try {
                        Query = this.resolver.query(formatSafUri(str3), new String[]{"_display_name", "mime_type", "_size", "last_modified"}, null, null, null);
                        if (Query != 0) {
                            try {
                                if (Query.moveToFirst()) {
                                    String string = Query.getString(0);
                                    String strNormalizeMime = normalizeMime(string, Query.getString(1));
                                    r15 = Query;
                                    try {
                                        FileEntry fileEntry2 = new FileEntry(str, str2, str3, string, joinPath(indexedPath, string), strNormalizeMime, "vnd.android.document/directory".equals(strNormalizeMime), safeLong(Query, 2), safeLong(Query, 3));
                                        if (r15 != 0) {
                                            r15.close();
                                        }
                                        return fileEntry2;
                                    } catch (Exception e) {
                                        e = e;
                                        Query = r15;
                                    } catch (Throwable th) {
                                        th = th;
                                    }
                                }
                            } catch (Exception e2) {
                                e = e2;
                            }
                            Log.d(TAG, "Unable to refresh SAF index entry " + str3, e);
                            if (Query != 0) {
                                Query.close();
                            }
                            return fileEntry;
                        }
                        if (Query != 0) {
                            Query.close();
                        }
                        return null;
                    } catch (Exception e3) {
                        e = e3;
                        Query = fileEntry;
                    } catch (Throwable th2) {
                        th = th2;
                        r15 = fileEntry;
                    }
                } catch (Exception e4) {
                    e = e4;
                    fileEntry = null;
                } catch (Throwable th3) {
                    th = th3;
                    fileEntry = null;
                }
            } else {
                File fileFileFromUrl = fileFromUrl(str3);
                if (!fileFileFromUrl.exists()) {
                    return null;
                }
                String name = fileFileFromUrl.getName();
                boolean zIsDirectory = fileFileFromUrl.isDirectory();
                return new FileEntry(str, str2, str3, name, joinPath(indexedPath, name), zIsDirectory ? "vnd.android.document/directory" : normalizeMime(name, guessMime(name)), zIsDirectory, fileFileFromUrl.length(), fileFileFromUrl.lastModified());
            }
        } catch (Throwable th4) {
            th = th4;
            r15 = Query;
        }
        if (r15 != 0) {
            r15.close();
        }
        throw th;
    }

    private String getIndexedPath(String str, String str2, String str3) {
        if (!str.equals(str2)) {
            Cursor cursorQuery = null;
            try {
                cursorQuery = this.db.getReadableDatabase().query("files", new String[]{"path"}, "root_url = ? AND url = ?", new String[]{str, str2}, null, null, null, "1");
                if (cursorQuery.moveToFirst()) {
                    return cursorQuery.getString(0);
                }
            } finally {
                if (cursorQuery != null) {
                    cursorQuery.close();
                }
            }
        }
        return str3;
    }

    private String getWorkspaceTitle(String str) {
        Cursor cursorQuery = null;
        try {
            cursorQuery = this.db.getReadableDatabase().query("workspaces", new String[]{"title"}, "root_url = ?", new String[]{str}, null, null, null, "1");
            return cursorQuery.moveToFirst() ? cursorQuery.getString(0) : basename(str);
        } finally {
            if (cursorQuery != null) {
                cursorQuery.close();
            }
        }
    }

    private int deleteIndexedSubtree(SQLiteDatabase sQLiteDatabase, String str, String str2) {
        ArrayDeque arrayDeque = new ArrayDeque();
        HashSet hashSet = new HashSet();
        HashSet<String> hashSet2 = new HashSet();
        arrayDeque.add(str2);
        hashSet2.add(str2);
        while (!arrayDeque.isEmpty()) {
            String str3 = (String) arrayDeque.removeFirst();
            if (hashSet.add(str3)) {
                Cursor cursorQuery = null;
                try {
                    cursorQuery = sQLiteDatabase.query("files", new String[]{"url", "is_directory"}, "root_url = ? AND parent_url = ?", new String[]{str, str3}, null, null, null);
                    while (cursorQuery.moveToNext()) {
                        String string = cursorQuery.getString(0);
                        hashSet2.add(string);
                        if (cursorQuery.getInt(1) != 0) {
                            arrayDeque.add(string);
                        }
                    }
                    if (cursorQuery != null) {
                        cursorQuery.close();
                    }
                } catch (Throwable th) {
                    if (cursorQuery != null) {
                        cursorQuery.close();
                    }
                    throw th;
                }
            }
        }
        int iDelete = 0;
        for (String str4 : hashSet2) {
            sQLiteDatabase.delete("content", "url = ?", new String[]{str4});
            iDelete += sQLiteDatabase.delete("files", "root_url = ? AND url = ?", new String[]{str, str4});
        }
        return iDelete;
    }

    private void scanFileDir(Job job, CallbackContext callbackContext, String str, File file, String str2, String str3, String str4, JSONArray jSONArray, boolean z, String str5, boolean z2, boolean z3, JSONArray jSONArray2, ScanStats scanStats) throws Exception {
        String str6;
        String str7;
        File[] fileArr;
        int i;
        int i2;
        Job job2 = job;
        String str8 = "Skipping unreadable directory ";
        String str9 = TAG;
        if (job2.cancelled || file == null) {
            return;
        }
        try {
            File[] fileArrListFiles = file.listFiles();
            if (fileArrListFiles == null) {
                return;
            }
            int i3 = 0;
            for (int length = fileArrListFiles.length; i3 < length; length = i) {
                File file2 = fileArrListFiles[i3];
                if (job2.cancelled) {
                    return;
                }
                String name = file2.getName();
                if (z || !name.startsWith(".")) {
                    boolean zIsDirectory = file2.isDirectory();
                    String string = Uri.fromFile(file2).toString();
                    String strJoinPath = joinPath(str3, name);
                    FileEntry fileEntry = new FileEntry(str, str2, string, name, strJoinPath, zIsDirectory ? "vnd.android.document/directory" : normalizeMime(name, guessMime(name)), zIsDirectory, file2.length(), file2.lastModified());
                    addEntry(callbackContext, job2.id, jSONArray2, fileEntry, scanStats, z3);
                    if (zIsDirectory) {
                        String str10 = str8;
                        if (shouldSkipDirectory(str, string, strJoinPath, jSONArray)) {
                            str6 = str10;
                        } else {
                            String str11 = str9;
                            fileArr = fileArrListFiles;
                            i = length;
                            i2 = i3;
                            try {
                                scanFileDir(job2, callbackContext, str, file2, string, strJoinPath, str4, jSONArray, z, str5, z2, z3, jSONArray2, scanStats);
                                str7 = str11;
                                str6 = str10;
                            } catch (Exception e) {
                                str6 = str10;
                                str7 = str11;
                                Log.d(str7, str6 + string, e);
                            }
                        }
                    } else {
                        str6 = str8;
                        str7 = str9;
                        fileArr = fileArrListFiles;
                        i = length;
                        i2 = i3;
                        if (z2) {
                            indexFile(fileEntry, str5);
                            scanStats.indexed++;
                        }
                    }
                    i3 = i2 + 1;
                    job2 = job;
                    str8 = str6;
                    str9 = str7;
                    fileArrListFiles = fileArr;
                } else {
                    str6 = str8;
                }
                str7 = str9;
                fileArr = fileArrListFiles;
                i = length;
                i2 = i3;
                i3 = i2 + 1;
                job2 = job;
                str8 = str6;
                str9 = str7;
                fileArrListFiles = fileArr;
            }
        } catch (Exception e2) {
            Log.d(TAG, "Skipping unreadable directory " + file, e2);
        }
    }

    /* JADX WARN: Code duplicated, block: B:109:0x0238  */
    /* JADX WARN: Code duplicated, block: B:113:0x0241  */
    private void scanSafDir(Job job, CallbackContext callbackContext, String str, String str2, String str3, String str4, String str5, String str6, JSONArray jSONArray, boolean z, String str7, boolean z2, boolean z3, JSONArray jSONArray2, ScanStats scanStats) throws Exception {
        CancellationSignal cancellationSignal;
        String str8;
        boolean zEquals;
        String str9;
        String str10;
        String strJoinPath;
        CancellationSignal cancellationSignal2;
        CancellationSignal cancellationSignal3;
        FileEntry fileEntry;
        Cursor cursor;
        String str11;
        int i;
        CancellationSignal cancellationSignal4;
        Job job2 = job;
        String str12 = TAG;
        String str13 = "Skipping unreadable SAF directory ";
        if (job2.cancelled) {
            return;
        }
        Uri uriBuildChildDocumentsUriUsingTree = DocumentsContract.buildChildDocumentsUriUsingTree(Uri.parse(str), str3);
        CancellationSignal cancellationSignal5 = new CancellationSignal();
        CancellationSignal signal = job2.setSignal(cancellationSignal5);
        Cursor cursorQuery = null;
        try {
            int i2 = 0;
            int i3 = 1;
            cursorQuery = this.resolver.query(uriBuildChildDocumentsUriUsingTree, new String[]{"document_id", "_display_name", "mime_type", "_size", "last_modified"}, null, null, null, cancellationSignal5);
            cancellationSignal = cancellationSignal5;
            if (cursorQuery == null) {
                if (cursorQuery != null) {
                    cursorQuery.close();
                }
                job2.restoreSignal(cancellationSignal, signal);
                return;
            }
            while (cursorQuery.moveToNext()) {
                try {
                    try {
                        try {
                            if (job2.cancelled) {
                                if (cursorQuery != null) {
                                    cursorQuery.close();
                                }
                                job2.restoreSignal(cancellationSignal, signal);
                                return;
                            }
                            String string = cursorQuery.getString(i2);
                            String string2 = cursorQuery.getString(i3);
                            String strNormalizeMime = normalizeMime(string2, cursorQuery.getString(2));
                            long jSafeLong = safeLong(cursorQuery, 3);
                            long jSafeLong2 = safeLong(cursorQuery, 4);
                            try {
                                try {
                                    try {
                                        if (!z && string2 != null) {
                                            try {
                                                if (string2.startsWith(".")) {
                                                    continue;
                                                }
                                            } catch (Exception e) {
                                                e = e;
                                                str4 = str4;
                                                signal = signal;
                                                str8 = str12;
                                                job2 = job2;
                                                Log.d(str8, "Stopping unreadable SAF directory " + str4, e);
                                                if (cursorQuery != null) {
                                                    cursorQuery.close();
                                                }
                                                job2.restoreSignal(cancellationSignal, signal);
                                            } catch (Throwable th) {
                                                th = th;
                                                job2 = job2;
                                                if (cursorQuery != null) {
                                                    cursorQuery.close();
                                                }
                                                job2.restoreSignal(cancellationSignal, signal);
                                                throw th;
                                            }
                                        }
                                        addEntry(callbackContext, job2.id, jSONArray2, fileEntry, scanStats, z3);
                                        if (!zEquals) {
                                            job2 = job2;
                                            signal = cancellationSignal4;
                                            str8 = str9;
                                            cancellationSignal3 = cancellationSignal2;
                                            if (z2) {
                                                indexFile(fileEntry, str7);
                                                scanStats.indexed++;
                                            }
                                            job2 = job2;
                                            str12 = str8;
                                            str13 = str11;
                                            cursorQuery = cursor;
                                            i2 = 0;
                                            i3 = i;
                                            cancellationSignal = cancellationSignal3;
                                            signal = signal;
                                        } else if (shouldSkipDirectory(str2, str10, strJoinPath, jSONArray)) {
                                            str13 = str11;
                                            signal = cancellationSignal4;
                                            cursorQuery = cursor;
                                            i2 = 0;
                                            i3 = i;
                                            str12 = str9;
                                            cancellationSignal = cancellationSignal2;
                                        } else {
                                            job2 = job2;
                                            signal = cancellationSignal4;
                                            cancellationSignal3 = cancellationSignal2;
                                            try {
                                                try {
                                                    scanSafDir(job2, callbackContext, str, str2, string, str10, strJoinPath, str6, jSONArray, z, str7, z2, z3, jSONArray2, scanStats);
                                                    str11 = str11;
                                                    str8 = str9;
                                                } catch (Exception e2) {
                                                    try {
                                                        str11 = str11;
                                                        str8 = str9;
                                                        try {
                                                            Log.d(str8, str11 + str10, e2);
                                                        } catch (Exception e3) {
                                                            e = e3;
                                                            cursorQuery = cursor;
                                                            cancellationSignal = cancellationSignal3;
                                                            signal = signal;
                                                            Log.d(str8, "Stopping unreadable SAF directory " + str4, e);
                                                            if (cursorQuery != null) {
                                                                cursorQuery.close();
                                                            }
                                                            job2.restoreSignal(cancellationSignal, signal);
                                                        }
                                                    } catch (Exception e4) {
                                                        e = e4;
                                                        str8 = str9;
                                                    }
                                                }
                                                job2 = job2;
                                                str12 = str8;
                                                str13 = str11;
                                                cursorQuery = cursor;
                                                i2 = 0;
                                                i3 = i;
                                                cancellationSignal = cancellationSignal3;
                                                signal = signal;
                                            } catch (Throwable th2) {
                                                th = th2;
                                                cursorQuery = cursor;
                                                cancellationSignal = cancellationSignal3;
                                                signal = signal;
                                                if (cursorQuery != null) {
                                                    cursorQuery.close();
                                                }
                                                job2.restoreSignal(cancellationSignal, signal);
                                                throw th;
                                            }
                                        }
                                    } catch (Exception e5) {
                                        e = e5;
                                        job2 = job2;
                                        signal = cancellationSignal4;
                                        str8 = str9;
                                        cancellationSignal3 = cancellationSignal2;
                                    } catch (Throwable th3) {
                                        th = th3;
                                        job2 = job2;
                                        signal = cancellationSignal4;
                                        cancellationSignal3 = cancellationSignal2;
                                    }
                                    fileEntry = new FileEntry(str2, str4, str10, string2, strJoinPath, strNormalizeMime, zEquals, jSafeLong, jSafeLong2);
                                    cursor = cursorQuery;
                                    str11 = str13;
                                    i = i3;
                                    cancellationSignal4 = signal;
                                } catch (Exception e6) {
                                    e = e6;
                                    signal = signal;
                                    job2 = job2;
                                    str8 = str9;
                                    cancellationSignal3 = cancellationSignal2;
                                } catch (Throwable th4) {
                                    th = th4;
                                    signal = signal;
                                    job2 = job2;
                                    cancellationSignal3 = cancellationSignal2;
                                }
                                str10 = str + SEPARATOR + string;
                                strJoinPath = joinPath(str5, string2);
                                cancellationSignal2 = cancellationSignal;
                            } catch (Exception e7) {
                                e = e7;
                                str8 = str9;
                                str4 = str4;
                                signal = signal;
                                Log.d(str8, "Stopping unreadable SAF directory " + str4, e);
                                if (cursorQuery != null) {
                                    cursorQuery.close();
                                }
                                job2.restoreSignal(cancellationSignal, signal);
                            }
                            zEquals = "vnd.android.document/directory".equals(strNormalizeMime);
                            str9 = str12;
                        } catch (Throwable th5) {
                            th = th5;
                            signal = signal;
                            job2 = job2;
                        }
                    } catch (Exception e8) {
                        e = e8;
                        str8 = str12;
                    }
                    signal = signal;
                } catch (Exception e9) {
                    e = e9;
                    signal = signal;
                    str8 = str12;
                    job2 = job2;
                    str4 = str4;
                    Log.d(str8, "Stopping unreadable SAF directory " + str4, e);
                    if (cursorQuery != null) {
                        cursorQuery.close();
                    }
                    job2.restoreSignal(cancellationSignal, signal);
                } catch (Throwable th6) {
                    th = th6;
                }
                if (cursorQuery != null) {
                    cursorQuery.close();
                }
                job2.restoreSignal(cancellationSignal, signal);
                throw th;
            }
            CancellationSignal cancellationSignal6 = signal;
            Cursor cursor2 = cursorQuery;
            job2 = job2;
            CancellationSignal cancellationSignal7 = cancellationSignal;
            if (cursor2 != null) {
                cursor2.close();
            }
            cancellationSignal = cancellationSignal7;
            signal = cancellationSignal6;
            job2.restoreSignal(cancellationSignal, signal);
        } catch (Exception e10) {
            signal = signal;
            cancellationSignal = cancellationSignal5;
            str8 = TAG;
            job2 = job2;
            try {
                try {
                    str4 = str4;
                    try {
                        Log.d(str8, "Skipping unreadable SAF directory " + str4, e10);
                        job2.restoreSignal(cancellationSignal, signal);
                    } catch (Exception e11) {
                        e = e11;
                        Log.d(str8, "Stopping unreadable SAF directory " + str4, e);
                        if (cursorQuery != null) {
                            cursorQuery.close();
                        }
                        job2.restoreSignal(cancellationSignal, signal);
                    }
                } catch (Exception e12) {
                    e = e12;
                    str4 = str4;
                    Log.d(str8, "Stopping unreadable SAF directory " + str4, e);
                    if (cursorQuery != null) {
                        cursorQuery.close();
                    }
                    job2.restoreSignal(cancellationSignal, signal);
                }
            } catch (Throwable th7) {
                th = th7;
            }
        } catch (Throwable th8) {
            th = th8;
            signal = signal;
            job2 = job2;
            cancellationSignal = cancellationSignal5;
        }
    }

    private void addEntry(CallbackContext callbackContext, String str, JSONArray jSONArray, FileEntry fileEntry, ScanStats scanStats, boolean z) throws Exception {
        saveFile(fileEntry);
        if (fileEntry.isDirectory) {
            scanStats.dirs++;
        } else {
            scanStats.files++;
        }
        if (z) {
            jSONArray.put(fileEntry.toJSON());
            if (jSONArray.length() >= 200) {
                flushBatch(callbackContext, str, jSONArray);
            }
        }
    }

    private JSONObject runQuery(JSONObject jSONObject) throws Exception {
        String str;
        JSONArray jSONArrayOptJSONArray = jSONObject.optJSONArray("roots");
        String strTrim = jSONObject.optString("text", "").trim();
        String strOptString = jSONObject.optString("url", "");
        boolean z = false;
        boolean zOptBoolean = jSONObject.optBoolean("includeDirectories", false);
        int iMax = Math.max(1, Math.min(jSONObject.optInt("limit", 200), 1000));
        int iMax2 = Math.max(0, jSONObject.optInt("cursor", jSONObject.optInt("offset", 0)));
        StringBuilder sb = new StringBuilder("1 = 1");
        ArrayList arrayList = new ArrayList();
        appendRootsClause(sb, arrayList, jSONArrayOptJSONArray);
        if (strOptString.length() > 0) {
            sb.append(" AND url = ?");
            arrayList.add(strOptString);
        }
        if (!zOptBoolean) {
            sb.append(" AND is_directory = 0");
        }
        if (strTrim.length() > 0) {
            sb.append(" AND (name LIKE ? ESCAPE '\\' OR path LIKE ? ESCAPE '\\')");
            String str2 = "%" + escapeLike(strTrim) + "%";
            arrayList.add(str2);
            arrayList.add(str2);
        }
        if (strTrim.length() > 0) {
            arrayList.add(escapeLike(strTrim) + "%");
            str = "CASE WHEN name LIKE ? ESCAPE '\\' THEN 0 ELSE 1 END, name COLLATE NOCASE, path COLLATE NOCASE";
        } else {
            str = "name COLLATE NOCASE, path COLLATE NOCASE";
        }
        String str3 = "SELECT root_url, parent_url, url, name, path, mime, is_directory, size, modified_date FROM files WHERE " + ((Object) sb) + " ORDER BY " + str + " LIMIT ? OFFSET ?";
        arrayList.add(String.valueOf(iMax + 1));
        arrayList.add(String.valueOf(iMax2));
        JSONArray jSONArray = new JSONArray();
        Cursor cursorRawQuery = null;
        try {
            cursorRawQuery = this.db.getReadableDatabase().rawQuery(str3, (String[]) arrayList.toArray(new String[0]));
            while (cursorRawQuery.moveToNext()) {
                if (jSONArray.length() >= iMax) {
                    z = true;
                    break;
                }
                jSONArray.put(fileEntryFromCursor(cursorRawQuery).toJSON());
            }
            if (cursorRawQuery != null) {
                cursorRawQuery.close();
            }
            JSONObject jSONObject2 = new JSONObject();
            jSONObject2.put("entries", jSONArray);
            jSONObject2.put("cursor", z ? Integer.valueOf(iMax2 + jSONArray.length()) : JSONObject.NULL);
            jSONObject2.put("hasMore", z);
            return jSONObject2;
        } catch (Throwable th) {
            if (cursorRawQuery != null) {
                cursorRawQuery.close();
            }
            throw th;
        }
    }

    private void flushBatch(CallbackContext callbackContext, String str, JSONArray jSONArray) throws JSONException {
        if (jSONArray.length() == 0) {
            return;
        }
        JSONObject jSONObjectBaseEvent = baseEvent(str, "batch");
        jSONObjectBaseEvent.put("entries", new JSONArray(jSONArray.toString()));
        send(callbackContext, jSONObjectBaseEvent, true);
        while (jSONArray.length() > 0) {
            jSONArray.remove(0);
        }
    }

    private void runSearch(Job job, JSONObject jSONObject, CallbackContext callbackContext) throws Exception {
        int i;
        int i2;
        String str;
        boolean z;
        JSONArray jSONArray;
        int length;
        JSONArray jSONArrayOptJSONArray = jSONObject.optJSONArray("files");
        if (jSONArrayOptJSONArray == null) {
            jSONArrayOptJSONArray = new JSONArray();
        }
        JSONArray jSONArrayMergeIndexedFiles = mergeIndexedFiles(jSONArrayOptJSONArray, jSONObject.optJSONArray("roots"));
        String strOptString = jSONObject.optString("search", "");
        String strOptString2 = jSONObject.optString("replace", null);
        String strOptString3 = jSONObject.optString("mode", "search");
        JSONObject jSONObjectOptJSONObject = jSONObject.optJSONObject("options");
        if (jSONObjectOptJSONObject == null) {
            jSONObjectOptJSONObject = new JSONObject();
        }
        JSONObject jSONObject2 = jSONObjectOptJSONObject;
        JSONObject jSONObjectOptJSONObject2 = jSONObject.optJSONObject("overlays");
        if (jSONObjectOptJSONObject2 == null) {
            jSONObjectOptJSONObject2 = new JSONObject();
        }
        JSONObject jSONObject3 = jSONObjectOptJSONObject2;
        String strOptString4 = jSONObject.optString("defaultEncoding", "UTF-8");
        boolean zOptBoolean = jSONObject.optBoolean("useIndex", false);
        boolean zOptBoolean2 = jSONObject.optBoolean("batchResults", false);
        Pattern patternCompileSearchPattern = compileSearchPattern(strOptString, jSONObject2);
        JSONArray jSONArray2 = new JSONArray();
        int length2 = jSONArrayMergeIndexedFiles.length();
        JSONObject jSONObject4 = jSONObject3;
        sendStatus(callbackContext, job.id, "searching", "Searching files", 0, true);
        int i3 = -1;
        int i4 = 0;
        int i5 = 0;
        int i6 = 0;
        while (true) {
            if (i4 < length2) {
                if (job.cancelled) {
                    return;
                }
                int i7 = i4;
                JSONObject jSONObject5 = jSONArrayMergeIndexedFiles.getJSONObject(i7);
                int i8 = length2 != 0 ? (i5 * 100) / length2 : 100;
                if (i8 != i3) {
                    sendProgress(callbackContext, job.id, i8);
                    i = i8;
                } else {
                    i = i3;
                }
                if (isSupportedUrl(jSONObject5.optString("url")) && !shouldSkipSearchFile(jSONObject5, jSONObject2)) {
                    String str2 = strOptString4;
                    i2 = i7;
                    str = str2;
                    z = zOptBoolean;
                    jSONArray = jSONArrayMergeIndexedFiles;
                    JSONObject jSONObject6 = jSONObject4;
                    try {
                        String fileContent = getFileContent(jSONObject5, jSONObject6, str, z, isExplicitlyIncluded(jSONObject5, jSONObject2));
                        jSONObject4 = jSONObject6;
                        if (fileContent == null) {
                            i5++;
                        } else {
                            if ("replace".equals(strOptString3)) {
                                String strReplaceAll = patternCompileSearchPattern.matcher(fileContent).replaceAll(Matcher.quoteReplacement(strOptString2 == null ? "" : strOptString2));
                                JSONObject jSONObjectBaseEvent = baseEvent(job.id, "replace-result");
                                jSONObjectBaseEvent.put("file", jSONObject5);
                                jSONObjectBaseEvent.put("text", strReplaceAll);
                                send(callbackContext, jSONObjectBaseEvent, true);
                            } else {
                                JSONObject jSONObjectSearchInContent = searchInContent(jSONObject5, fileContent, patternCompileSearchPattern);
                                if (jSONObjectSearchInContent != null) {
                                    if (zOptBoolean2) {
                                        jSONArray2.put(jSONObjectSearchInContent);
                                        length = i6 + jSONObjectSearchInContent.getJSONArray("matches").length();
                                        if (jSONArray2.length() >= 12 || length >= SEARCH_RESULT_BATCH_MATCHES) {
                                            flushSearchResultBatch(callbackContext, job.id, jSONArray2);
                                            length = 0;
                                        }
                                    } else {
                                        JSONObject jSONObjectBaseEvent2 = baseEvent(job.id, "search-result");
                                        jSONObjectBaseEvent2.put("data", jSONObjectSearchInContent);
                                        send(callbackContext, jSONObjectBaseEvent2, true);
                                    }
                                }
                                i5++;
                                i6 = length;
                            }
                            length = i6;
                            i5++;
                            i6 = length;
                        }
                    } catch (Exception e) {
                        jSONObject4 = jSONObject6;
                        Log.d(TAG, "Skipping unreadable search file " + jSONObject5.optString("url"), e);
                    }
                } else {
                    i5++;
                    String str3 = strOptString4;
                    i2 = i7;
                    str = str3;
                    z = zOptBoolean;
                    jSONArray = jSONArrayMergeIndexedFiles;
                }
                i4 = i2 + 1;
                strOptString4 = str;
                jSONArrayMergeIndexedFiles = jSONArray;
                i3 = i;
                zOptBoolean = z;
            } else {
                if (zOptBoolean2) {
                    flushSearchResultBatch(callbackContext, job.id, jSONArray2);
                }
                sendProgress(callbackContext, job.id, 100);
                send(callbackContext, baseEvent(job.id, "replace".equals(strOptString3) ? "done-replacing" : "done-searching"), false);
                return;
            }
        }
    }

    private Pattern compileSearchPattern(String str, JSONObject jSONObject) throws PatternSyntaxException {
        boolean zOptBoolean = jSONObject.optBoolean("regExp", false);
        boolean zOptBoolean2 = jSONObject.optBoolean("wholeWord", false);
        boolean zOptBoolean3 = jSONObject.optBoolean("caseSensitive", false);
        if (!zOptBoolean) {
            str = Pattern.quote(str);
        }
        if (zOptBoolean2) {
            str = "\\b" + str + "\\b";
        }
        return Pattern.compile(str, !zOptBoolean3 ? 74 : 8);
    }

    private JSONObject searchInContent(JSONObject jSONObject, String str, Pattern pattern) throws JSONException {
        boolean z;
        Matcher matcher = pattern.matcher(str);
        JSONArray jSONArray = new JSONArray();
        StringBuilder sb = new StringBuilder(jSONObject.optString("name"));
        if (sb.length() > 30) {
            sb = new StringBuilder("..." + sb.substring(sb.length() - 30));
        }
        int i = 0;
        int i2 = 0;
        int i3 = 0;
        while (true) {
            if (!matcher.find()) {
                z = false;
                break;
            }
            if (jSONArray.length() >= 5000) {
                z = true;
                break;
            }
            String strGroup = matcher.group();
            int iStart = matcher.start();
            int iEnd = matcher.end();
            String[] surrounding = getSurrounding(str, strGroup, iStart, iEnd);
            while (i < iStart) {
                if (str.charAt(i) == '\n') {
                    i2++;
                    i3 = 0;
                } else {
                    i3++;
                }
                i++;
            }
            JSONObject jSONObjectLineColumn = lineColumn(i2, i3);
            while (i < iEnd) {
                if (str.charAt(i) == '\n') {
                    i2++;
                    i3 = 0;
                } else {
                    i3++;
                }
                i++;
            }
            JSONObject jSONObjectLineColumn2 = lineColumn(i2, i3);
            JSONObject jSONObject2 = new JSONObject();
            jSONObject2.put("match", strGroup);
            jSONObject2.put("renderText", surrounding[1]);
            jSONObject2.put("line", surrounding[0].trim());
            jSONObject2.put("position", position(jSONObjectLineColumn, jSONObjectLineColumn2));
            jSONArray.put(jSONObject2);
            sb.append("\n\t").append(surrounding[0].trim());
        }
        if (jSONArray.length() == 0) {
            return null;
        }
        JSONObject jSONObject3 = new JSONObject();
        jSONObject3.put("file", jSONObject);
        jSONObject3.put("matches", jSONArray);
        if (z) {
            sb.append("\n\t... result limit reached for this file");
        }
        jSONObject3.put("limited", z);
        jSONObject3.put("text", sb.toString());
        return jSONObject3;
    }

    private void flushSearchResultBatch(CallbackContext callbackContext, String str, JSONArray jSONArray) throws JSONException {
        if (jSONArray.length() == 0) {
            return;
        }
        JSONObject jSONObjectBaseEvent = baseEvent(str, "search-results");
        jSONObjectBaseEvent.put("data", new JSONArray(jSONArray.toString()));
        send(callbackContext, jSONObjectBaseEvent, true);
        while (jSONArray.length() > 0) {
            jSONArray.remove(0);
        }
    }

    private String getFileContent(JSONObject jSONObject, JSONObject jSONObject2, String str, boolean z, boolean z2) throws Exception {
        String strIndexFile;
        String strOptString = jSONObject.optString("url");
        if (jSONObject2.has(strOptString)) {
            return jSONObject2.optString(strOptString, "");
        }
        long jOptLong = jSONObject.optLong("size", 0L);
        long jNormalizeModified = normalizeModified(jSONObject);
        if (z) {
            Cursor cursorQuery = null;
            try {
                cursorQuery = this.db.getReadableDatabase().query("content", new String[]{"text", "size", "modified_date"}, "url = ?", new String[]{strOptString}, null, null, null);
                if (cursorQuery != null && cursorQuery.moveToFirst()) {
                    long j = cursorQuery.getLong(1);
                    long j2 = cursorQuery.getLong(2);
                    if ((jOptLong == 0 || j == jOptLong) && (jNormalizeModified == 0 || j2 == jNormalizeModified)) {
                        String string = cursorQuery.getString(0);
                        if (cursorQuery != null) {
                            cursorQuery.close();
                        }
                        return string;
                    }
                }
                if (cursorQuery != null) {
                    cursorQuery.close();
                }
            } catch (Throwable th) {
                if (cursorQuery != null) {
                    cursorQuery.close();
                }
                throw th;
            }
        }
        FileEntry fileEntryFromJSON = FileEntry.fromJSON(jSONObject);
        if (z && !z2 && (strIndexFile = indexFile(fileEntryFromJSON, str)) != null) {
            return strIndexFile;
        }
        int i = EXPLICIT_INCLUDE_READ_LIMIT_BYTES;
        int i2 = z2 ? EXPLICIT_INCLUDE_READ_LIMIT_BYTES : 16777216;
        if (!z2) {
            i = 16777216;
        }
        return readFileText(fileEntryFromJSON, str, i2, i);
    }

    private String indexFile(FileEntry fileEntry, String str) {
        if (!fileEntry.isDirectory && !isBinary(fileEntry) && fileEntry.size <= 2097152) {
            try {
                String fileText = readFileText(fileEntry, str, 2097152, 524288);
                if (fileText == null) {
                    return null;
                }
                String strNormalizeEncoding = normalizeEncoding(str);
                ContentValues contentValues = new ContentValues();
                contentValues.put("url", fileEntry.url);
                contentValues.put("size", Long.valueOf(fileEntry.size));
                contentValues.put("modified_date", Long.valueOf(fileEntry.modifiedDate));
                contentValues.put("encoding", strNormalizeEncoding);
                contentValues.put("text", fileText);
                contentValues.put("lower_text", fileText.toLowerCase(Locale.ROOT));
                contentValues.put("indexed_at", Long.valueOf(System.currentTimeMillis()));
                this.db.getWritableDatabase().replace("content", null, contentValues);
                return fileText;
            } catch (Exception e) {
                Log.d(TAG, "Unable to index " + fileEntry.url, e);
            }
        }
        return null;
    }

    private String readFileText(FileEntry fileEntry, String str, int i, int i2) throws Exception {
        if (!fileEntry.isDirectory && !isBinary(fileEntry) && fileEntry.size <= i) {
            byte[] bytes = readBytes(fileEntry.url, i + 1);
            if (bytes.length > i) {
                return null;
            }
            String strDetectEncoding = detectEncoding(bytes, str);
            if (isSingleByteEncoding(strDetectEncoding) && looksBinary(bytes)) {
                return null;
            }
            String str2 = new String(bytes, Charset.forName(strDetectEncoding));
            if (str2.length() <= i2 && !hasBinaryChars(str2)) {
                return str2;
            }
        }
        return null;
    }

    private byte[] readBytes(String str, int i) throws Exception {
        InputStream fileInputStream;
        InputStream inputStream = null;
        try {
            if (isSafUrl(str)) {
                fileInputStream = this.resolver.openInputStream(formatSafUri(str));
            } else {
                fileInputStream = new FileInputStream(fileFromUrl(str));
            }
            if (fileInputStream == null) {
                byte[] bArr = new byte[0];
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                return bArr;
            }
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] bArr2 = new byte[8192];
            int i2 = 0;
            while (true) {
                int i3 = fileInputStream.read(bArr2);
                if (i3 == -1) {
                    break;
                }
                i2 += i3;
                if (i2 > i) {
                    byteArrayOutputStream.write(bArr2, 0, i3 - (i2 - i));
                    break;
                }
                byteArrayOutputStream.write(bArr2, 0, i3);
            }
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return byteArray;
        } catch (Throwable th) {
            if (0 != 0) {
                inputStream.close();
            }
            throw th;
        }
    }

    private void saveFile(FileEntry fileEntry) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("url", fileEntry.url);
        contentValues.put("root_url", fileEntry.rootUrl);
        contentValues.put("parent_url", fileEntry.parentUrl);
        contentValues.put("path", fileEntry.path);
        contentValues.put("name", fileEntry.name);
        contentValues.put("mime", fileEntry.mime);
        contentValues.put("is_directory", Integer.valueOf(fileEntry.isDirectory ? 1 : 0));
        contentValues.put("size", Long.valueOf(fileEntry.size));
        contentValues.put("modified_date", Long.valueOf(fileEntry.modifiedDate));
        contentValues.put("indexed_at", Long.valueOf(System.currentTimeMillis()));
        contentValues.put("skipped_reason", isBinary(fileEntry) ? "binary" : null);
        this.db.getWritableDatabase().replace("files", null, contentValues);
    }

    private JSONArray mergeIndexedFiles(JSONArray jSONArray, JSONArray jSONArray2) throws JSONException {
        JSONArray jSONArray3 = new JSONArray();
        HashSet hashSet = new HashSet();
        for (int i = 0; i < jSONArray.length(); i++) {
            JSONObject jSONObjectOptJSONObject = jSONArray.optJSONObject(i);
            if (jSONObjectOptJSONObject != null) {
                String strOptString = jSONObjectOptJSONObject.optString("url", "");
                if (strOptString.length() != 0 && hashSet.add(strOptString)) {
                    jSONArray3.put(jSONObjectOptJSONObject);
                }
            }
        }
        if (jSONArray2 != null && jSONArray2.length() != 0) {
            StringBuilder sb = new StringBuilder("is_directory = 0");
            ArrayList arrayList = new ArrayList();
            appendRootsClause(sb, arrayList, jSONArray2);
            Cursor cursorQuery = null;
            try {
                cursorQuery = this.db.getReadableDatabase().query("files", new String[]{"root_url", "parent_url", "url", "name", "path", "mime", "is_directory", "size", "modified_date"}, sb.toString(), (String[]) arrayList.toArray(new String[0]), null, null, null);
                while (cursorQuery.moveToNext()) {
                    FileEntry fileEntryFileEntryFromCursor = fileEntryFromCursor(cursorQuery);
                    if (hashSet.add(fileEntryFileEntryFromCursor.url)) {
                        jSONArray3.put(fileEntryFileEntryFromCursor.toJSON());
                    }
                }
                if (cursorQuery != null) {
                    cursorQuery.close();
                    return jSONArray3;
                }
            } catch (Throwable th) {
                if (cursorQuery != null) {
                    cursorQuery.close();
                }
                throw th;
            }
        }
        return jSONArray3;
    }

    private FileEntry fileEntryFromCursor(Cursor cursor) {
        return new FileEntry(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.isNull(5) ? null : cursor.getString(5), cursor.getInt(6) != 0, safeLong(cursor, 7), safeLong(cursor, 8));
    }

    private void appendRootsClause(StringBuilder sb, List<String> list, JSONArray jSONArray) {
        if (jSONArray == null || jSONArray.length() == 0) {
            return;
        }
        sb.append(" AND root_url IN (");
        boolean z = false;
        for (int i = 0; i < jSONArray.length(); i++) {
            String strOptString = jSONArray.optString(i, "");
            if (strOptString.length() != 0) {
                if (z) {
                    sb.append(',');
                }
                sb.append('?');
                list.add(strOptString);
                z = true;
            }
        }
        if (!z) {
            sb.append("NULL");
        }
        sb.append(')');
    }

    private String escapeLike(String str) {
        return str.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    private boolean shouldSkipDirectory(String str, String str2, String str3, JSONArray jSONArray) {
        if (isExcluded(str3, jSONArray) || isExcluded(str3 + AbstractFilePath.SEPARATOR, jSONArray)) {
            return true;
        }
        return str != null && str.startsWith("content://com.termux.documents/tree/") && str2 != null && str2.matches(".*::/data/data/com\\.termux/files/home/storage(?:/)?$");
    }

    private boolean shouldSkipSearchFile(JSONObject jSONObject, JSONObject jSONObject2) {
        if (FileEntry.fromJSON(jSONObject).isBinary()) {
            return true;
        }
        String strOptString = jSONObject.optString("path", "");
        if (strOptString.length() == 0) {
            return false;
        }
        JSONArray jSONArraySplitPatterns = splitPatterns(jSONObject2.optString("exclude", ""));
        JSONArray jSONArraySplitPatterns2 = splitPatterns(jSONObject2.optString("include", ""));
        if (jSONArraySplitPatterns2.length() == 0) {
            jSONArraySplitPatterns2.put("**");
        }
        return matchesAny(strOptString, jSONArraySplitPatterns) || !matchesAny(strOptString, jSONArraySplitPatterns2);
    }

    private boolean isExplicitlyIncluded(JSONObject jSONObject, JSONObject jSONObject2) {
        String strOptString = jSONObject.optString("path", "");
        if (strOptString.length() == 0) {
            return false;
        }
        JSONArray jSONArraySplitPatterns = splitPatterns(jSONObject2.optString("include", ""));
        return jSONArraySplitPatterns.length() > 0 && matchesAny(strOptString, jSONArraySplitPatterns);
    }

    private JSONArray splitPatterns(String str) {
        JSONArray jSONArray = new JSONArray();
        if (str != null && str.trim().length() != 0) {
            for (String str2 : str.split(",")) {
                String strTrim = str2.trim();
                if (strTrim.length() > 0) {
                    jSONArray.put(strTrim);
                }
            }
        }
        return jSONArray;
    }

    private boolean isExcluded(String str, JSONArray jSONArray) {
        return matchesAny(str, jSONArray);
    }

    private boolean matchesAny(String str, JSONArray jSONArray) {
        if (jSONArray == null) {
            return false;
        }
        for (int i = 0; i < jSONArray.length(); i++) {
            if (globMatches(str, jSONArray.optString(i, ""))) {
                return true;
            }
        }
        return false;
    }

    private boolean globMatches(String str, String str2) {
        if (str2 == null || str2.length() == 0) {
            return false;
        }
        String strReplace = str.replace('\\', '/');
        String strReplace2 = str2.replace('\\', '/');
        if ("**".equals(strReplace2)) {
            return true;
        }
        Pattern patternCompile = Pattern.compile(globToRegex(strReplace2));
        if (patternCompile.matcher(strReplace).matches()) {
            return true;
        }
        int iLastIndexOf = strReplace.lastIndexOf(47);
        if (iLastIndexOf >= 0) {
            strReplace = strReplace.substring(iLastIndexOf + 1);
        }
        return patternCompile.matcher(strReplace).matches();
    }

    private String globToRegex(String str) {
        StringBuilder sb = new StringBuilder("^");
        int i = 0;
        while (i < str.length()) {
            char cCharAt = str.charAt(i);
            if (cCharAt == '*') {
                int i2 = i + 1;
                if (i2 < str.length() && str.charAt(i2) == '*') {
                    i += 2;
                    if (i < str.length() && str.charAt(i) == '/') {
                        sb.append("(?:.*/)?");
                    } else {
                        sb.append(".*");
                        i = i2;
                    }
                } else {
                    sb.append("[^/]*");
                }
            } else if (cCharAt == '?') {
                sb.append('.');
            } else if ("\\.[]{}()+-^$|".indexOf(cCharAt) >= 0) {
                sb.append('\\').append(cCharAt);
            } else {
                sb.append(cCharAt);
            }
            i++;
        }
        sb.append('$');
        return sb.toString();
    }

    private boolean isBinary(FileEntry fileEntry) {
        return fileEntry.isBinary();
    }

    private String detectEncoding(byte[] bArr, String str) {
        if (bArr.length >= 3 && (bArr[0] & UByte.MAX_VALUE) == 239 && (bArr[1] & UByte.MAX_VALUE) == 187 && (bArr[2] & UByte.MAX_VALUE) == 191) {
            return "UTF-8";
        }
        if (bArr.length >= 2 && (bArr[0] & UByte.MAX_VALUE) == 255 && (bArr[1] & UByte.MAX_VALUE) == 254) {
            return "UTF-16LE";
        }
        if (bArr.length >= 2 && (bArr[0] & UByte.MAX_VALUE) == 254 && (bArr[1] & UByte.MAX_VALUE) == 255) {
            return "UTF-16BE";
        }
        String strDetectUtf16ByNullPattern = detectUtf16ByNullPattern(bArr);
        return strDetectUtf16ByNullPattern != null ? strDetectUtf16ByNullPattern : normalizeEncoding(str);
    }

    private String detectUtf16ByNullPattern(byte[] bArr) {
        int iMin = Math.min(bArr.length, 8192);
        if (iMin < 8) {
            return null;
        }
        int i = iMin / 2;
        int i2 = 0;
        int i3 = 0;
        int i4 = 0;
        while (true) {
            int i5 = i2 + 1;
            if (i5 >= iMin) {
                break;
            }
            if (bArr[i2] == 0) {
                i4++;
            }
            if (bArr[i5] == 0) {
                i3++;
            }
            i2 += 2;
        }
        double d = i3;
        double d2 = i;
        double d3 = 0.35d * d2;
        if (d > d3 && i4 < d2 * 0.05d) {
            return "UTF-16LE";
        }
        if (i4 <= d3 || d >= d2 * 0.05d) {
            return null;
        }
        return "UTF-16BE";
    }

    private boolean isSingleByteEncoding(String str) {
        String upperCase = str == null ? "" : str.toUpperCase(Locale.ROOT);
        return (upperCase.startsWith("UTF-16") || upperCase.startsWith("UTF-32")) ? false : true;
    }

    private boolean looksBinary(byte[] bArr) {
        int iMin = Math.min(bArr.length, 8192);
        if (iMin == 0) {
            return false;
        }
        int i = 0;
        int i2 = 0;
        for (int i3 = 0; i3 < iMin; i3++) {
            int i4 = bArr[i3] & UByte.MAX_VALUE;
            if (i4 == 0) {
                return true;
            }
            if (i4 < 32 && i4 != 9 && i4 != 10 && i4 != 13 && i4 != 12) {
                i++;
            }
            if (i4 >= 128) {
                i2++;
            }
        }
        double d = iMin;
        return ((double) i) > Math.max(8.0d, 0.02d * d) && ((double) i2) < d * 0.5d;
    }

    private String normalizeEncoding(String str) {
        if (str != null && !str.equals("auto") && str.length() != 0) {
            try {
                if (Charset.isSupported(str)) {
                    return str;
                }
            } catch (Exception unused) {
            }
        }
        return "UTF-8";
    }

    private boolean hasBinaryChars(String str) {
        int iMin = Math.min(str.length(), 2048);
        for (int i = 0; i < iMin; i++) {
            char cCharAt = str.charAt(i);
            if ((cCharAt >= 0 && cCharAt <= '\b') || cCharAt == 11) {
                return true;
            }
            if ((cCharAt >= 14 && cCharAt <= 31) || cCharAt == 127) {
                return true;
            }
        }
        return false;
    }

    private JSONObject position(JSONObject jSONObject, JSONObject jSONObject2) throws JSONException {
        JSONObject jSONObject3 = new JSONObject();
        jSONObject3.put("start", jSONObject);
        jSONObject3.put("end", jSONObject2);
        return jSONObject3;
    }

    private JSONObject lineColumn(int i, int i2) throws JSONException {
        JSONObject jSONObject = new JSONObject();
        jSONObject.put("row", i);
        jSONObject.put("column", i2);
        return jSONObject;
    }

    private String[] getSurrounding(String str, String str2, int i, int i2) {
        int iMax;
        int iMin;
        char cCharAt;
        char cCharAt2;
        int i3 = i;
        while (i3 > 0 && (cCharAt2 = str.charAt(i3 - 1)) != '\n' && cCharAt2 != '\r') {
            i3--;
        }
        int i4 = i2;
        while (i4 < str.length() && (cCharAt = str.charAt(i4)) != '\n' && cCharAt != '\r') {
            i4++;
        }
        if (i4 - i3 > 160) {
            int iMax2 = Math.max(0, 160 - Math.max(1, i2 - i));
            int i5 = iMax2 / 2;
            iMax = Math.max(i3, i - i5);
            iMin = Math.min(i4, i2 + (iMax2 - i5));
        } else {
            iMax = i3;
            iMin = i4;
        }
        StringBuilder sb = new StringBuilder();
        if (iMax > i3) {
            sb.append("...");
        }
        sb.append(str.substring(iMax, iMin).trim());
        if (iMin < i4) {
            sb.append("...");
        }
        return new String[]{sb.toString().replaceAll("[\\r\\n]+", " ⏎ "), str2.replaceAll("[\\r\\n]+", " ⏎ ")};
    }

    private void sendStatus(CallbackContext callbackContext, String str, String str2, String str3, int i, boolean z) throws JSONException {
        JSONObject jSONObjectBaseEvent = baseEvent(str, NotificationCompat.CATEGORY_STATUS);
        jSONObjectBaseEvent.put("state", str2);
        jSONObjectBaseEvent.put("message", str3);
        jSONObjectBaseEvent.put(NotificationCompat.CATEGORY_PROGRESS, i);
        send(callbackContext, jSONObjectBaseEvent, z);
    }

    private void sendProgress(CallbackContext callbackContext, String str, int i) throws JSONException {
        JSONObject jSONObjectBaseEvent = baseEvent(str, NotificationCompat.CATEGORY_PROGRESS);
        jSONObjectBaseEvent.put("data", i);
        send(callbackContext, jSONObjectBaseEvent, true);
    }

    private JSONObject baseEvent(String str, String str2) throws JSONException {
        JSONObject jSONObject = new JSONObject();
        jSONObject.put("id", str);
        jSONObject.put("type", str2);
        jSONObject.put("action", str2);
        return jSONObject;
    }

    private void send(CallbackContext callbackContext, JSONObject jSONObject, boolean z) {
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, jSONObject);
        pluginResult.setKeepCallback(z);
        callbackContext.sendPluginResult(pluginResult);
    }

    private void sendError(CallbackContext callbackContext, String str, Exception exc) {
        try {
            JSONObject jSONObjectBaseEvent = baseEvent(str, "error");
            jSONObjectBaseEvent.put("error", exc.getMessage() == null ? exc.toString() : exc.getMessage());
            send(callbackContext, jSONObjectBaseEvent, false);
        } catch (JSONException unused) {
            callbackContext.error(exc.getMessage());
        }
    }

    private boolean isSupportedUrl(String str) {
        if (str != null) {
            return str.startsWith("file:") || str.startsWith("content:");
        }
        return false;
    }

    private boolean isSafUrl(String str) {
        return str != null && str.startsWith("content:");
    }

    private Uri formatSafUri(String str) {
        SafUrl safUrl = parseSafUrl(str);
        return DocumentsContract.buildDocumentUriUsingTree(Uri.parse(safUrl.treeUrl), safUrl.docId);
    }

    private SafUrl parseSafUrl(String str) {
        if (str.contains(SEPARATOR)) {
            String[] strArrSplit = str.split(SEPARATOR, 2);
            return new SafUrl(strArrSplit[0], strArrSplit[1]);
        }
        return new SafUrl(str, DocumentsContract.getTreeDocumentId(Uri.parse(str)));
    }

    private File fileFromUrl(String str) {
        return new File(Uri.parse(str).getPath());
    }

    private long safeLong(Cursor cursor, int i) {
        try {
            if (cursor.isNull(i)) {
                return 0L;
            }
            return cursor.getLong(i);
        } catch (Exception unused) {
            return 0L;
        }
    }

    private long normalizeModified(JSONObject jSONObject) {
        long jOptLong = jSONObject.optLong("modifiedDate", 0L);
        return jOptLong == 0 ? jSONObject.optLong("lastModified", 0L) : jOptLong;
    }

    private String joinPath(String str, String str2) {
        if (str == null || str.length() == 0) {
            return str2;
        }
        if (str2 == null || str2.length() == 0) {
            return str;
        }
        return str.endsWith(AbstractFilePath.SEPARATOR) ? str + str2 : str + AbstractFilePath.SEPARATOR + str2;
    }

    private String basename(String str) {
        if (str == null || str.length() == 0) {
            return "";
        }
        int iLastIndexOf = str.lastIndexOf(47);
        return iLastIndexOf >= 0 ? str.substring(iLastIndexOf + 1) : str;
    }

    private String guessMime(String str) {
        if (str == null) {
            return null;
        }
        String lowerCase = str.toLowerCase(Locale.ROOT);
        if (lowerCase.endsWith(".js")) {
            return "application/javascript";
        }
        if (lowerCase.endsWith(".ts") || lowerCase.endsWith(".tsx")) {
            return "application/typescript";
        }
        if (lowerCase.endsWith(".jsx")) {
            return "application/javascript";
        }
        if (lowerCase.endsWith(".json")) {
            return HttpRequest.CONTENT_TYPE_JSON;
        }
        if (lowerCase.endsWith(".svg")) {
            return "image/svg+xml";
        }
        if (lowerCase.endsWith(".html") || lowerCase.endsWith(".htm")) {
            return NanoHTTPD.MIME_HTML;
        }
        if (lowerCase.endsWith(".css")) {
            return "text/css";
        }
        if (lowerCase.endsWith(".md")) {
            return "text/markdown";
        }
        if (lowerCase.endsWith(".txt")) {
            return "text/plain";
        }
        if (lowerCase.endsWith(".xml")) {
            return "application/xml";
        }
        return null;
    }

    private String normalizeMime(String str, String str2) {
        String strGuessMime = guessMime(str);
        return (strGuessMime == null || !(str2 == null || str2.length() == 0 || "application/octet-stream".equals(str2))) ? str2 : strGuessMime;
    }

    /* JADX INFO: Access modifiers changed from: private */
    static class Job {
        volatile boolean cancelled = false;
        final String id;
        volatile CancellationSignal signal;

        Job(String str) {
            this.id = str;
        }

        void cancel() {
            this.cancelled = true;
            CancellationSignal cancellationSignal = this.signal;
            if (cancellationSignal != null) {
                cancellationSignal.cancel();
            }
        }

        CancellationSignal setSignal(CancellationSignal cancellationSignal) {
            CancellationSignal cancellationSignal2 = this.signal;
            this.signal = cancellationSignal;
            if (this.cancelled) {
                cancellationSignal.cancel();
            }
            return cancellationSignal2;
        }

        void restoreSignal(CancellationSignal cancellationSignal, CancellationSignal cancellationSignal2) {
            if (this.signal != cancellationSignal) {
                return;
            }
            this.signal = cancellationSignal2;
            if (!this.cancelled || cancellationSignal2 == null) {
                return;
            }
            cancellationSignal2.cancel();
        }
    }

    private static class ScanStats {
        int dirs;
        int files;
        int indexed;

        private ScanStats() {
            this.files = 0;
            this.dirs = 0;
            this.indexed = 0;
        }
    }

    private static class SafUrl {
        final String docId;
        final String treeUrl;

        SafUrl(String str, String str2) {
            this.treeUrl = str;
            this.docId = str2;
        }
    }

    private static class FileEntry {
        final boolean isDirectory;
        final String mime;
        final long modifiedDate;
        final String name;
        final String parentUrl;
        final String path;
        final String rootUrl;
        final long size;
        final String url;

        FileEntry(String str, String str2, String str3, String str4, String str5, String str6, boolean z, long j, long j2) {
            this.rootUrl = str;
            this.parentUrl = str2;
            this.url = str3;
            str4 = str4 == null ? "" : str4;
            this.name = str4;
            this.path = str5 == null ? str4 : str5;
            this.mime = str6;
            this.isDirectory = z;
            this.size = Math.max(0L, j);
            this.modifiedDate = Math.max(0L, j2);
        }

        JSONObject toJSON() throws JSONException {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("rootUrl", this.rootUrl);
            jSONObject.put("parent", this.parentUrl);
            jSONObject.put("parentUrl", this.parentUrl);
            jSONObject.put("url", this.url);
            jSONObject.put("uri", this.url);
            jSONObject.put("name", this.name);
            jSONObject.put("path", this.path);
            jSONObject.put("mime", this.mime);
            jSONObject.put("type", this.mime);
            jSONObject.put("isDirectory", this.isDirectory);
            jSONObject.put("isFile", !this.isDirectory);
            jSONObject.put("size", this.size);
            jSONObject.put("modifiedDate", this.modifiedDate);
            return jSONObject;
        }

        static FileEntry fromJSON(JSONObject jSONObject) {
            return new FileEntry(jSONObject.optString("rootUrl", ""), jSONObject.optString("parentUrl", jSONObject.optString("parent", "")), jSONObject.optString("url", ""), jSONObject.optString("name", ""), jSONObject.optString("path", ""), jSONObject.optString("mime", jSONObject.optString("type", null)), jSONObject.optBoolean("isDirectory", false), jSONObject.optLong("size", 0L), jSONObject.optLong("modifiedDate", jSONObject.optLong("lastModified", 0L)));
        }

        boolean isBinary() {
            if (this.isDirectory) {
                return false;
            }
            String str = this.mime;
            String strTrim = str == null ? "" : str.toLowerCase(Locale.ROOT).split(";")[0].trim();
            if (strTrim.startsWith("text/") || WorkspaceIndex.TEXT_MIME_TYPES.contains(strTrim) || isTextExtension(this.name)) {
                return false;
            }
            if (strTrim.startsWith("audio/") || strTrim.startsWith("font/") || strTrim.startsWith("image/") || strTrim.startsWith("model/") || strTrim.startsWith("video/") || WorkspaceIndex.BINARY_MIME_TYPES.contains(strTrim)) {
                return true;
            }
            String lowerCase = this.name.toLowerCase(Locale.ROOT);
            int iLastIndexOf = lowerCase.lastIndexOf(46);
            if (iLastIndexOf < 0) {
                return false;
            }
            if (WorkspaceIndex.BINARY_EXTENSIONS.contains(lowerCase.substring(iLastIndexOf + 1))) {
                return true;
            }
            int iLastIndexOf2 = lowerCase.lastIndexOf(46, iLastIndexOf - 1);
            return iLastIndexOf2 >= 0 && WorkspaceIndex.BINARY_EXTENSIONS.contains(lowerCase.substring(iLastIndexOf2 + 1));
        }

        private boolean isTextExtension(String str) {
            String lowerCase = str == null ? "" : str.toLowerCase(Locale.ROOT);
            int iLastIndexOf = lowerCase.lastIndexOf(46);
            return iLastIndexOf >= 0 && WorkspaceIndex.TEXT_EXTENSIONS.contains(lowerCase.substring(iLastIndexOf + 1));
        }
    }

    private static class DB extends SQLiteOpenHelper {
        DB(Context context) {
            super(context, "acode_workspace_index.db", (SQLiteDatabase.CursorFactory) null, 2);
        }

        @Override // android.database.sqlite.SQLiteOpenHelper
        public void onCreate(SQLiteDatabase sQLiteDatabase) {
            sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS workspaces (root_url TEXT PRIMARY KEY, title TEXT, indexed_at INTEGER, options_hash TEXT)");
            sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS files (url TEXT, root_url TEXT, parent_url TEXT, path TEXT, name TEXT, mime TEXT, is_directory INTEGER, size INTEGER, modified_date INTEGER, indexed_at INTEGER, skipped_reason TEXT, PRIMARY KEY (root_url, url))");
            sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS content (url TEXT PRIMARY KEY, size INTEGER, modified_date INTEGER, encoding TEXT, text TEXT, lower_text TEXT, indexed_at INTEGER)");
            sQLiteDatabase.execSQL("CREATE INDEX IF NOT EXISTS idx_files_root ON files(root_url)");
            sQLiteDatabase.execSQL("CREATE INDEX IF NOT EXISTS idx_files_parent ON files(parent_url)");
        }

        @Override // android.database.sqlite.SQLiteOpenHelper
        public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
            sQLiteDatabase.execSQL("DROP TABLE IF EXISTS content");
            sQLiteDatabase.execSQL("DROP TABLE IF EXISTS files");
            sQLiteDatabase.execSQL("DROP TABLE IF EXISTS workspaces");
            onCreate(sQLiteDatabase);
        }
    }
}