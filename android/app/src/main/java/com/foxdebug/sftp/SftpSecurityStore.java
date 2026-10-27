package com.foxdebug.sftp;

import android.content.Context;
import com.foxdebug.acode.rk.auth.EncryptedPreferenceManager;
import java.security.GeneralSecurityException;
import java.util.UUID;
import org.json.JSONException;
import org.json.JSONObject;

/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes.dex */
final class SftpSecurityStore {
    private static final String HOST_PREFS = "acode_ssh_known_hosts_v1";
    static final String PROFILE_PREFIX = "profile-";
    private static final String PROFILE_PREFS = "acode_sftp_profiles_v1";
    private final EncryptedPreferenceManager knownHosts;
    private final EncryptedPreferenceManager profiles;

    SftpSecurityStore(Context context) {
        this.profiles = new EncryptedPreferenceManager(context, PROFILE_PREFS, false);
        this.knownHosts = new EncryptedPreferenceManager(context, HOST_PREFS, false);
    }

    static boolean isProfileID(String str) {
        return str != null && str.startsWith(PROFILE_PREFIX);
    }

    synchronized String saveProfile(String str, JSONObject jSONObject) throws JSONException, GeneralSecurityException {
        if (!isProfileID(str)) {
            str = PROFILE_PREFIX + UUID.randomUUID();
        }
        if (!this.profiles.setStringSync(str, jSONObject.toString())) {
            throw new GeneralSecurityException("Could not persist SFTP profile");
        }
        return str;
    }

    synchronized JSONObject getProfile(String str) throws JSONException, GeneralSecurityException {
        String string;
        if (!isProfileID(str)) {
            throw new GeneralSecurityException("Invalid SFTP profile ID");
        }
        string = this.profiles.getString(str, null);
        if (string == null) {
            throw new GeneralSecurityException("SFTP profile was not found");
        }
        return new JSONObject(string);
    }

    synchronized void deleteProfile(String str) {
        if (isProfileID(str)) {
            this.profiles.removeSync(str);
        }
    }

    synchronized JSONObject getKnownHost(String str) throws JSONException {
        JSONObject jSONObject;
        jSONObject = null;
        String string = this.knownHosts.getString(str, null);
        if (string != null) {
            jSONObject = new JSONObject(string);
        }
        return jSONObject;
    }

    synchronized void trustHost(String str, String str2, String str3, String str4) throws JSONException {
        JSONObject jSONObject = new JSONObject();
        jSONObject.put("algorithm", str2);
        jSONObject.put("fingerprint", str3);
        jSONObject.put("publicKey", str4);
        if (!this.knownHosts.setStringSync(str, jSONObject.toString())) {
            throw new JSONException("Could not persist trusted SSH host");
        }
    }
}