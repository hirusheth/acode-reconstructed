package org.apache.cordova.file;

import android.net.Uri;

/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes2.dex */
public class LocalFilesystemURL {
    public static final String CDVFILE_KEYWORD = "__cdvfile_";
    public static final String FILESYSTEM_PROTOCOL = "cdvfile";
    public final String fsName;
    public final boolean isDirectory;
    public final String path;
    public final Uri uri;

    private LocalFilesystemURL(Uri uri, String str, String str2, boolean z) {
        this.uri = uri;
        this.fsName = str;
        this.path = str2;
        this.isDirectory = z;
    }

    public static LocalFilesystemURL parse(Uri uri) {
        int iIndexOf;
        if (!uri.toString().contains(CDVFILE_KEYWORD)) {
            return null;
        }
        String path = uri.getPath();
        if (path.length() < 1 || (iIndexOf = path.indexOf(47, 1)) < 0) {
            return null;
        }
        String strSubstring = path.substring(1, iIndexOf).substring(CDVFILE_KEYWORD.length());
        String strSubstring2 = strSubstring.substring(0, strSubstring.length() - 2);
        String strSubstring3 = path.substring(iIndexOf);
        return new LocalFilesystemURL(uri, strSubstring2, strSubstring3, strSubstring3.charAt(strSubstring3.length() - 1) == '/');
    }

    public static LocalFilesystemURL parse(String str) {
        return parse(Uri.parse(str));
    }

    public static String fsNameToCdvKeyword(String str) {
        return CDVFILE_KEYWORD + str + "__";
    }

    public String toString() {
        return this.uri.toString();
    }
}