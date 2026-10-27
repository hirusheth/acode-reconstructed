package com.foxdebug.system;

import android.content.Context;
import android.util.Log;
import com.foxdebug.acode.rk.auth.EncryptedPreferenceManager;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import org.json.JSONException;
import org.json.JSONObject;

/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes.dex */
public class RewardPassManager {
    private static final String ADS_PREFS_FILENAME = "ads";
    private static final String KEY_REWARD_STATE = "reward_state";
    private static final long MAX_ACTIVE_PASS_MS = 36000000;
    private static final int MAX_REDEMPTIONS_PER_DAY = 3;
    private static final long ONE_HOUR_MS = 3600000;
    private static final String TAG = "SystemRewardPass";
    private final EncryptedPreferenceManager adsPrefManager;
    private final Random random = new Random();

    public RewardPassManager(Context context) {
        this.adsPrefManager = new EncryptedPreferenceManager(context, ADS_PREFS_FILENAME);
    }

    public String getRewardStatus() throws JSONException {
        JSONObject jSONObjectSyncRewardState = syncRewardState(loadRewardState());
        JSONObject jSONObjectBuildRewardStatus = buildRewardStatus(jSONObjectSyncRewardState);
        if (jSONObjectBuildRewardStatus.optBoolean("hasPendingExpiryNotice")) {
            jSONObjectSyncRewardState.put("expiryNoticePendingUntil", 0L);
        }
        saveRewardState(jSONObjectSyncRewardState);
        return jSONObjectBuildRewardStatus.toString();
    }

    public String redeemReward(String str) throws JSONException {
        JSONObject jSONObjectSyncRewardState = syncRewardState(loadRewardState());
        int iOptInt = jSONObjectSyncRewardState.optInt("redemptionsToday", 0);
        long jCurrentTimeMillis = java.lang.System.currentTimeMillis();
        long jOptLong = jSONObjectSyncRewardState.optLong("adFreeUntil", 0L);
        long jMax = Math.max(0L, jOptLong - jCurrentTimeMillis);
        if (iOptInt >= 3) {
            throw new JSONException("Daily limit reached. You can redeem up to 3 rewards per day.");
        }
        if (jMax >= MAX_ACTIVE_PASS_MS) {
            throw new JSONException("You already have the maximum 10 hours of ad-free time active.");
        }
        long jResolveRewardDuration = resolveRewardDuration(str);
        long jMax2 = Math.max(jCurrentTimeMillis, jOptLong);
        long jMin = Math.min(jMax2 + jResolveRewardDuration, jCurrentTimeMillis + MAX_ACTIVE_PASS_MS);
        long jMax3 = Math.max(0L, jMin - jMax2);
        jSONObjectSyncRewardState.put("adFreeUntil", jMin);
        jSONObjectSyncRewardState.put("lastExpiredRewardUntil", 0L);
        jSONObjectSyncRewardState.put("expiryNoticePendingUntil", 0L);
        jSONObjectSyncRewardState.put("redemptionDay", getTodayKey());
        jSONObjectSyncRewardState.put("redemptionsToday", iOptInt + 1);
        saveRewardState(jSONObjectSyncRewardState);
        JSONObject jSONObjectBuildRewardStatus = buildRewardStatus(jSONObjectSyncRewardState);
        jSONObjectBuildRewardStatus.put("grantedDurationMs", jResolveRewardDuration);
        jSONObjectBuildRewardStatus.put("appliedDurationMs", jMax3);
        jSONObjectBuildRewardStatus.put("offerId", str);
        return jSONObjectBuildRewardStatus.toString();
    }

    private JSONObject loadRewardState() {
        String string = this.adsPrefManager.getString(KEY_REWARD_STATE, "");
        if (string == null || string.isEmpty()) {
            return defaultRewardState();
        }
        try {
            return mergeRewardState(new JSONObject(string));
        } catch (JSONException e) {
            Log.w(TAG, "Failed to parse reward state, resetting.", e);
            return defaultRewardState();
        }
    }

    private JSONObject defaultRewardState() {
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject.put("adFreeUntil", 0L);
            jSONObject.put("lastExpiredRewardUntil", 0L);
            jSONObject.put("expiryNoticePendingUntil", 0L);
            jSONObject.put("redemptionDay", getTodayKey());
            jSONObject.put("redemptionsToday", 0);
        } catch (JSONException unused) {
        }
        return jSONObject;
    }

    private JSONObject mergeRewardState(JSONObject jSONObject) {
        JSONObject jSONObjectDefaultRewardState = defaultRewardState();
        try {
            jSONObjectDefaultRewardState.put("adFreeUntil", jSONObject.optLong("adFreeUntil", 0L));
            jSONObjectDefaultRewardState.put("lastExpiredRewardUntil", jSONObject.optLong("lastExpiredRewardUntil", 0L));
            jSONObjectDefaultRewardState.put("expiryNoticePendingUntil", jSONObject.optLong("expiryNoticePendingUntil", 0L));
            jSONObjectDefaultRewardState.put("redemptionDay", jSONObject.optString("redemptionDay", getTodayKey()));
            jSONObjectDefaultRewardState.put("redemptionsToday", jSONObject.optInt("redemptionsToday", 0));
        } catch (JSONException unused) {
        }
        return jSONObjectDefaultRewardState;
    }

    private void saveRewardState(JSONObject jSONObject) {
        this.adsPrefManager.setString(KEY_REWARD_STATE, jSONObject.toString());
    }

    private JSONObject syncRewardState(JSONObject jSONObject) throws JSONException {
        String todayKey = getTodayKey();
        if (!todayKey.equals(jSONObject.optString("redemptionDay", todayKey))) {
            jSONObject.put("redemptionDay", todayKey);
            jSONObject.put("redemptionsToday", 0);
        }
        long jOptLong = jSONObject.optLong("adFreeUntil", 0L);
        long jCurrentTimeMillis = java.lang.System.currentTimeMillis();
        if (jOptLong > 0 && jOptLong <= jCurrentTimeMillis) {
            if (jSONObject.optLong("expiryNoticePendingUntil", 0L) != jOptLong) {
                jSONObject.put("expiryNoticePendingUntil", jOptLong);
            }
            jSONObject.put("lastExpiredRewardUntil", jOptLong);
            jSONObject.put("adFreeUntil", 0L);
        }
        return jSONObject;
    }

    private JSONObject buildRewardStatus(JSONObject jSONObject) throws JSONException {
        long jCurrentTimeMillis = java.lang.System.currentTimeMillis();
        long jOptLong = jSONObject.optLong("adFreeUntil", 0L);
        int iOptInt = jSONObject.optInt("redemptionsToday", 0);
        long jMax = Math.max(0L, jOptLong - jCurrentTimeMillis);
        int iMax = Math.max(0, 3 - iOptInt);
        JSONObject jSONObject2 = new JSONObject();
        jSONObject2.put("adFreeUntil", jOptLong);
        jSONObject2.put("lastExpiredRewardUntil", jSONObject.optLong("lastExpiredRewardUntil", 0L));
        jSONObject2.put("isActive", jOptLong > jCurrentTimeMillis);
        jSONObject2.put("remainingMs", jMax);
        jSONObject2.put("redemptionsToday", iOptInt);
        jSONObject2.put("remainingRedemptions", iMax);
        jSONObject2.put("maxRedemptionsPerDay", 3);
        jSONObject2.put("maxActivePassMs", MAX_ACTIVE_PASS_MS);
        jSONObject2.put("hasPendingExpiryNotice", jSONObject.optLong("expiryNoticePendingUntil", 0L) > 0);
        jSONObject2.put("expiryNoticePendingUntil", jSONObject.optLong("expiryNoticePendingUntil", 0L));
        jSONObject2.put("canRedeem", iMax > 0 && jMax < MAX_ACTIVE_PASS_MS);
        jSONObject2.put("redeemDisabledReason", getRedeemDisabledReason(iMax, jMax));
        return jSONObject2;
    }

    private String getRedeemDisabledReason(int i, long j) {
        if (i <= 0) {
            return "Daily limit reached. You can redeem up to 3 rewards per day.";
        }
        if (j >= MAX_ACTIVE_PASS_MS) {
            return "You already have the maximum 10 hours of ad-free time active.";
        }
        return "";
    }

    private long resolveRewardDuration(String str) throws JSONException {
        if ("quick".equals(str)) {
            return ONE_HOUR_MS;
        }
        if ("focus".equals(str)) {
            return ((long) (this.random.nextInt(3) + 4)) * ONE_HOUR_MS;
        }
        throw new JSONException("Unknown reward offer.");
    }

    private String getTodayKey() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
    }
}