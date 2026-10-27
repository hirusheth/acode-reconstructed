package org.apache.cordova;

/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes2.dex */
@Deprecated
public class PermissionHelper {
    @Deprecated
    public static void requestPermission(CordovaPlugin cordovaPlugin, int i, String str) {
        requestPermissions(cordovaPlugin, i, new String[]{str});
    }

    @Deprecated
    public static void requestPermissions(CordovaPlugin cordovaPlugin, int i, String[] strArr) {
        cordovaPlugin.cordova.requestPermissions(cordovaPlugin, i, strArr);
    }

    @Deprecated
    public static boolean hasPermission(CordovaPlugin cordovaPlugin, String str) {
        return cordovaPlugin.cordova.hasPermission(str);
    }
}