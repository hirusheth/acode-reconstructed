package com.foxdebug.acode.rk.exec.terminal;

import android.content.ComponentName;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Point;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsProvider;
import android.util.Log;
import android.webkit.MimeTypeMap;
import com.foxdebug.acode.R;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Locale;

/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes.dex */
public class AlpineDocumentProvider extends DocumentsProvider {
    private static final String ALL_MIME_TYPES = "*/*";
    private static final String[] DEFAULT_ROOT_PROJECTION = {"root_id", "mime_types", "flags", "icon", "title", "summary", "document_id", "available_bytes"};
    private static final String[] DEFAULT_DOCUMENT_PROJECTION = {"document_id", "mime_type", "_display_name", "last_modified", "flags", "_size"};

    @Override // android.content.ContentProvider
    public boolean onCreate() {
        return true;
    }

    @Override // android.provider.DocumentsProvider
    public Cursor queryRoots(String[] strArr) {
        File file = new File(getContext().getFilesDir(), "public");
        if (!file.exists()) {
            file.mkdirs();
        }
        if (strArr == null) {
            strArr = DEFAULT_ROOT_PROJECTION;
        }
        MatrixCursor matrixCursor = new MatrixCursor(strArr);
        String applicationLabel = getApplicationLabel();
        MatrixCursor.RowBuilder rowBuilderNewRow = matrixCursor.newRow();
        rowBuilderNewRow.add("root_id", getDocIdForFile(file));
        rowBuilderNewRow.add("document_id", getDocIdForFile(file));
        rowBuilderNewRow.add("summary", null);
        rowBuilderNewRow.add("flags", 25);
        rowBuilderNewRow.add("title", applicationLabel);
        rowBuilderNewRow.add("mime_types", ALL_MIME_TYPES);
        rowBuilderNewRow.add("available_bytes", Long.valueOf(file.getFreeSpace()));
        rowBuilderNewRow.add("icon", Integer.valueOf(resolveLauncherIcon()));
        return matrixCursor;
    }

    @Override // android.provider.DocumentsProvider
    public Cursor queryDocument(String str, String[] strArr) throws FileNotFoundException {
        if (strArr == null) {
            strArr = DEFAULT_DOCUMENT_PROJECTION;
        }
        MatrixCursor matrixCursor = new MatrixCursor(strArr);
        includeFile(matrixCursor, str, null);
        return matrixCursor;
    }

    @Override // android.provider.DocumentsProvider
    public Cursor queryChildDocuments(String str, String[] strArr, String str2) throws FileNotFoundException {
        if (strArr == null) {
            strArr = DEFAULT_DOCUMENT_PROJECTION;
        }
        MatrixCursor matrixCursor = new MatrixCursor(strArr);
        File[] fileArrListFiles = getFileForDocId(str).listFiles();
        if (fileArrListFiles != null) {
            for (File file : fileArrListFiles) {
                includeFile(matrixCursor, null, file);
            }
            return matrixCursor;
        }
        Log.e("DocumentsProvider", "Unable to list files in " + str);
        return matrixCursor;
    }

    @Override // android.provider.DocumentsProvider
    public ParcelFileDescriptor openDocument(String str, String str2, CancellationSignal cancellationSignal) throws FileNotFoundException {
        return ParcelFileDescriptor.open(getFileForDocId(str), ParcelFileDescriptor.parseMode(str2));
    }

    @Override // android.provider.DocumentsProvider
    public AssetFileDescriptor openDocumentThumbnail(String str, Point point, CancellationSignal cancellationSignal) throws FileNotFoundException {
        File fileForDocId = getFileForDocId(str);
        return new AssetFileDescriptor(ParcelFileDescriptor.open(fileForDocId, 268435456), 0L, fileForDocId.length());
    }

    @Override // android.provider.DocumentsProvider
    public String createDocument(String str, String str2, String str3) throws FileNotFoundException {
        boolean zCreateNewFile;
        File fileForDocId = getFileForDocId(str);
        File file = new File(fileForDocId, str3);
        int i = 2;
        while (file.exists()) {
            file = new File(fileForDocId, str3 + " (" + i + ")");
            i++;
        }
        try {
            if ("vnd.android.document/directory".equals(str2)) {
                zCreateNewFile = file.mkdir();
            } else {
                zCreateNewFile = file.createNewFile();
            }
            if (!zCreateNewFile) {
                throw new FileNotFoundException("Failed to create document with id " + file.getAbsolutePath());
            }
            return getDocIdForFile(file);
        } catch (IOException unused) {
            throw new FileNotFoundException("Failed to create document with id " + file.getAbsolutePath());
        }
    }

    @Override // android.provider.DocumentsProvider
    public void deleteDocument(String str) throws FileNotFoundException {
        if (!getFileForDocId(str).delete()) {
            throw new FileNotFoundException("Failed to delete document with id " + str);
        }
    }

    @Override // android.provider.DocumentsProvider
    public String renameDocument(String str, String str2) throws FileNotFoundException {
        File fileForDocId = getFileForDocId(str);
        File parentFile = fileForDocId.getParentFile();
        if (parentFile == null) {
            throw new FileNotFoundException("Failed to rename root document with id " + str);
        }
        if (str2 == null || str2.trim().isEmpty()) {
            throw new FileNotFoundException("Failed to rename document with id " + str);
        }
        if (str2.equals(fileForDocId.getName())) {
            return str;
        }
        if (str2.contains(File.separator)) {
            throw new FileNotFoundException("Invalid display name for rename: " + str2);
        }
        File file = new File(parentFile, str2);
        if (file.exists()) {
            throw new FileNotFoundException("Target already exists: " + file.getAbsolutePath());
        }
        if (!fileForDocId.renameTo(file)) {
            throw new FileNotFoundException("Failed to rename document with id " + str);
        }
        return getDocIdForFile(file);
    }

    @Override // android.provider.DocumentsProvider
    public String getDocumentType(String str) throws FileNotFoundException {
        return getMimeType(getFileForDocId(str));
    }

    @Override // android.provider.DocumentsProvider
    public Cursor querySearchDocuments(String str, String str2, String[] strArr) throws FileNotFoundException {
        boolean zStartsWith;
        File file = new File(getContext().getFilesDir(), "public");
        if (!file.exists()) {
            file.mkdirs();
        }
        if (strArr == null) {
            strArr = DEFAULT_DOCUMENT_PROJECTION;
        }
        MatrixCursor matrixCursor = new MatrixCursor(strArr);
        File fileForDocId = getFileForDocId(str);
        LinkedList linkedList = new LinkedList();
        linkedList.add(fileForDocId);
        while (!linkedList.isEmpty() && matrixCursor.getCount() < 50) {
            File file2 = (File) linkedList.removeFirst();
            try {
                zStartsWith = file2.getCanonicalPath().startsWith(file.getCanonicalPath());
            } catch (IOException unused) {
                zStartsWith = true;
            }
            if (zStartsWith) {
                if (file2.isDirectory()) {
                    File[] fileArrListFiles = file2.listFiles();
                    if (fileArrListFiles != null) {
                        Collections.addAll(linkedList, fileArrListFiles);
                    }
                } else if (file2.getName().toLowerCase(Locale.getDefault()).contains(str2)) {
                    includeFile(matrixCursor, null, file2);
                }
            }
        }
        return matrixCursor;
    }

    @Override // android.provider.DocumentsProvider
    public boolean isChildDocument(String str, String str2) {
        return str2.startsWith(str);
    }

    /* JADX WARN: Code duplicated, block: B:13:0x0022  */
    private void includeFile(MatrixCursor matrixCursor, String str, File file) throws FileNotFoundException {
        int i;
        if (str == null) {
            str = getDocIdForFile(file);
        } else {
            file = getFileForDocId(str);
        }
        if (file.isDirectory()) {
            if (file.canWrite()) {
                i = 8;
            } else {
                i = 0;
            }
        } else if (file.canWrite()) {
            i = 2;
        } else {
            i = 0;
        }
        File parentFile = file.getParentFile();
        if (parentFile != null && parentFile.canWrite()) {
            i |= 68;
        }
        String name = file.getName();
        String mimeType = getMimeType(file);
        if (mimeType.startsWith("image/")) {
            i |= 1;
        }
        MatrixCursor.RowBuilder rowBuilderNewRow = matrixCursor.newRow();
        rowBuilderNewRow.add("document_id", str);
        rowBuilderNewRow.add("_display_name", name);
        rowBuilderNewRow.add("_size", Long.valueOf(file.length()));
        rowBuilderNewRow.add("mime_type", mimeType);
        rowBuilderNewRow.add("last_modified", Long.valueOf(file.lastModified()));
        rowBuilderNewRow.add("flags", Integer.valueOf(i));
        rowBuilderNewRow.add("icon", Integer.valueOf(R.mipmap.ic_launcher));
    }

    public static boolean isDocumentProviderEnabled(Context context) {
        int componentEnabledSetting = context.getPackageManager().getComponentEnabledSetting(new ComponentName(context, (Class<?>) AlpineDocumentProvider.class));
        return componentEnabledSetting == 1 || componentEnabledSetting == 0;
    }

    public static void setDocumentProviderEnabled(Context context, boolean z) {
        if (isDocumentProviderEnabled(context) == z) {
            return;
        }
        context.getPackageManager().setComponentEnabledSetting(new ComponentName(context, (Class<?>) AlpineDocumentProvider.class), z ? 1 : 2, 1);
    }

    private static String getDocIdForFile(File file) {
        return file.getAbsolutePath();
    }

    private static File getFileForDocId(String str) throws FileNotFoundException {
        File file = new File(str);
        if (file.exists()) {
            return file;
        }
        throw new FileNotFoundException(file.getAbsolutePath() + " not found");
    }

    private static String getMimeType(File file) {
        if (file.isDirectory()) {
            return "vnd.android.document/directory";
        }
        String name = file.getName();
        int iLastIndexOf = name.lastIndexOf(46);
        if (iLastIndexOf >= 0) {
            String mimeTypeFromExtension = MimeTypeMap.getSingleton().getMimeTypeFromExtension(name.substring(iLastIndexOf + 1).toLowerCase(Locale.getDefault()));
            return mimeTypeFromExtension != null ? mimeTypeFromExtension : "application/octet-stream";
        }
        return "application/octet-stream";
    }

    private int resolveLauncherIcon() {
        int identifier;
        Context context = getContext();
        return (context == null || (identifier = context.getResources().getIdentifier("ic_launcher", "mipmap", context.getPackageName())) == 0) ? android.R.mipmap.sym_def_app_icon : identifier;
    }

    private String getApplicationLabel() {
        Context context = getContext();
        if (context == null) {
            return "Acode";
        }
        try {
            CharSequence applicationLabel = context.getPackageManager().getApplicationLabel(context.getApplicationInfo());
            if (applicationLabel != null) {
                return applicationLabel.toString();
            }
        } catch (Exception unused) {
        }
        return "Acode";
    }
}