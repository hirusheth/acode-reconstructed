package com.foxdebug.acode.rk.auth;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;
import java.io.IOException;
import java.security.GeneralSecurityException;

/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes.dex */
public class EncryptedPreferenceManager {
    private final SharedPreferences sharedPreferences;

    public EncryptedPreferenceManager(Context context, String str) {
        this(context, str, true);
    }

    public EncryptedPreferenceManager(Context context, String str, boolean z) {
        SharedPreferences sharedPreferences;
        try {
            sharedPreferences = EncryptedSharedPreferences.create(str, MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC), context, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
        } catch (IOException | GeneralSecurityException e) {
            if (!z) {
                throw new IllegalStateException("Encrypted preferences are unavailable", e);
            }
            sharedPreferences = context.getSharedPreferences(str, 0);
        }
        this.sharedPreferences = sharedPreferences;
    }

    public void setString(String str, String str2) {
        this.sharedPreferences.edit().putString(str, str2).apply();
    }

    public boolean setStringSync(String str, String str2) {
        return this.sharedPreferences.edit().putString(str, str2).commit();
    }

    public String getString(String str, String str2) {
        return this.sharedPreferences.getString(str, str2);
    }

    public void setInt(String str, int i) {
        this.sharedPreferences.edit().putInt(str, i).apply();
    }

    public int getInt(String str, int i) {
        return this.sharedPreferences.getInt(str, i);
    }

    public void setBoolean(String str, boolean z) {
        this.sharedPreferences.edit().putBoolean(str, z).apply();
    }

    public boolean getBoolean(String str, boolean z) {
        return this.sharedPreferences.getBoolean(str, z);
    }

    public void remove(String str) {
        this.sharedPreferences.edit().remove(str).apply();
    }

    public boolean removeSync(String str) {
        return this.sharedPreferences.edit().remove(str).commit();
    }

    public boolean exists(String str) {
        return this.sharedPreferences.contains(str);
    }

    public void clear() {
        this.sharedPreferences.edit().clear().apply();
    }
}