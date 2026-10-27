package org.apache.cordova.buildinfo;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes2.dex */
public class BuildInfo extends CordovaPlugin {
    private static final String TAG = "BuildInfo";
    private static JSONObject mBuildInfoCache;

    @Override // org.apache.cordova.CordovaPlugin
    public boolean execute(String str, JSONArray jSONArray, CallbackContext callbackContext) throws JSONException, ClassNotFoundException {
        if (!"init".equals(str)) {
            return false;
        }
        init(1 < jSONArray.length() ? jSONArray.getString(0) : null, callbackContext);
        return true;
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    private void init(String str, CallbackContext callbackContext) throws ClassNotFoundException {
        String str2;
        String str3;
        CharSequence charSequenceLoadLabel;
        Class<?> cls;
        String str4;
        JSONObject jSONObject = mBuildInfoCache;
        if (jSONObject != null) {
            callbackContext.success(jSONObject);
            return;
        }
        AppCompatActivity activity = this.cordova.getActivity();
        String packageName = activity.getPackageName();
        PackageManager packageManager = activity.getPackageManager();
        long j = 0;
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 1);
            str2 = "flavor";
            str3 = "buildType";
            try {
                j = packageInfo.firstInstallTime;
                charSequenceLoadLabel = packageInfo.applicationInfo != null ? packageInfo.applicationInfo.loadLabel(packageManager) : "";
            } catch (PackageManager.NameNotFoundException e) {
                e = e;
                e.printStackTrace();
            }
        } catch (PackageManager.NameNotFoundException e2) {
            e = e2;
            str2 = "flavor";
            str3 = "buildType";
        }
        long j2 = j;
        try {
            cls = Class.forName(str == null ? packageName + ".BuildConfig" : str);
        } catch (ClassNotFoundException unused) {
            cls = null;
        }
        if (cls == null) {
            String name = activity.getClass().getPackage().getName();
            try {
                str4 = name;
                cls = Class.forName(name + ".BuildConfig");
            } catch (ClassNotFoundException e3) {
                callbackContext.error("BuildConfig ClassNotFoundException: " + e3.getMessage());
                return;
            }
        } else {
            str4 = packageName;
        }
        mBuildInfoCache = new JSONObject();
        try {
            boolean classFieldBoolean = getClassFieldBoolean(cls, "DEBUG", false);
            mBuildInfoCache.put("packageName", packageName);
            mBuildInfoCache.put("basePackageName", str4);
            mBuildInfoCache.put("displayName", charSequenceLoadLabel);
            mBuildInfoCache.put("name", charSequenceLoadLabel);
            mBuildInfoCache.put("version", getClassFieldString(cls, "VERSION_NAME", ""));
            mBuildInfoCache.put("versionCode", getClassFieldInt(cls, "VERSION_CODE", 0));
            mBuildInfoCache.put("debug", classFieldBoolean);
            mBuildInfoCache.put("installDate", convertLongToDateTimeString(j2));
            String str5 = str3;
            mBuildInfoCache.put(str5, getClassFieldString(cls, "BUILD_TYPE", ""));
            JSONObject jSONObject2 = mBuildInfoCache;
            String classFieldString = getClassFieldString(cls, "FLAVOR", "");
            String str6 = str2;
            jSONObject2.put(str6, classFieldString);
            if (classFieldBoolean) {
                Log.d(TAG, "packageName    : \"" + mBuildInfoCache.getString("packageName") + "\"");
                Log.d(TAG, "basePackageName: \"" + mBuildInfoCache.getString("basePackageName") + "\"");
                Log.d(TAG, "displayName    : \"" + mBuildInfoCache.getString("displayName") + "\"");
                Log.d(TAG, "name           : \"" + mBuildInfoCache.getString("name") + "\"");
                Log.d(TAG, "version        : \"" + mBuildInfoCache.getString("version") + "\"");
                Log.d(TAG, "versionCode    : " + mBuildInfoCache.getInt("versionCode"));
                Log.d(TAG, "debug          : ".concat(mBuildInfoCache.getBoolean("debug") ? "true" : "false"));
                Log.d(TAG, "buildType      : \"" + mBuildInfoCache.getString(str5) + "\"");
                Log.d(TAG, "flavor         : \"" + mBuildInfoCache.getString(str6) + "\"");
                Log.d(TAG, "installDate    : \"" + mBuildInfoCache.getString("installDate") + "\"");
            }
            callbackContext.success(mBuildInfoCache);
        } catch (JSONException e4) {
            e4.printStackTrace();
            callbackContext.error("JSONException: " + e4.getMessage());
        }
    }

    private static boolean getClassFieldBoolean(Class cls, String str, boolean z) {
        Field classField = getClassField(cls, str);
        if (classField != null) {
            try {
                return classField.getBoolean(cls);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return z;
    }

    private static String getClassFieldString(Class cls, String str, String str2) {
        Field classField = getClassField(cls, str);
        if (classField != null) {
            try {
                return (String) classField.get(cls);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return str2;
    }

    private static int getClassFieldInt(Class cls, String str, int i) {
        Field classField = getClassField(cls, str);
        if (classField != null) {
            try {
                return classField.getInt(cls);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return i;
    }

    private static long getClassFieldLong(Class cls, String str, long j) {
        Field classField = getClassField(cls, str);
        if (classField != null) {
            try {
                return classField.getLong(cls);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return j;
    }

    private static Field getClassField(Class cls, String str) {
        try {
            return cls.getField(str);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String convertLongToDateTimeString(long j) {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(Long.valueOf(j));
    }
}