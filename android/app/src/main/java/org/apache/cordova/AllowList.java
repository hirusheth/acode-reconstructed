package org.apache.cordova;

import android.net.Uri;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;
import org.slf4j.Marker;

/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes2.dex */
public class AllowList {
    public static final String TAG = "CordovaAllowList";
    private ArrayList<URLPattern> allowList = new ArrayList<>();

    private static class URLPattern {
        public Pattern host;
        public Pattern path;
        public Integer port;
        public Pattern scheme;

        private String regexFromPattern(String str, boolean z) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < str.length(); i++) {
                char cCharAt = str.charAt(i);
                if (cCharAt == '*' && z) {
                    sb.append(".");
                } else if ("\\.[]{}()^$?+|".indexOf(cCharAt) > -1) {
                    sb.append(IOUtils.DIR_SEPARATOR_WINDOWS);
                }
                sb.append(cCharAt);
            }
            return sb.toString();
        }

        public URLPattern(String str, String str2, String str3, String str4) throws MalformedURLException {
            if (str != null) {
                try {
                    if (Marker.ANY_MARKER.equals(str)) {
                        this.scheme = null;
                    } else {
                        this.scheme = Pattern.compile(regexFromPattern(str, false), 2);
                    }
                } catch (NumberFormatException unused) {
                    throw new MalformedURLException("Port must be a number");
                }
            } else {
                this.scheme = null;
            }
            if (Marker.ANY_MARKER.equals(str2)) {
                this.host = null;
            } else if (str2.startsWith("*.")) {
                this.host = Pattern.compile("([a-z0-9.-]*\\.)?" + regexFromPattern(str2.substring(2), false), 2);
            } else {
                this.host = Pattern.compile(regexFromPattern(str2, false), 2);
            }
            if (str3 == null || Marker.ANY_MARKER.equals(str3)) {
                this.port = null;
            } else {
                this.port = Integer.valueOf(Integer.parseInt(str3, 10));
            }
            if (str4 != null && !"/*".equals(str4)) {
                this.path = Pattern.compile(regexFromPattern(str4, true));
                return;
            }
            this.path = null;
        }

        public boolean matches(Uri uri) {
            Pattern pattern;
            Integer num;
            Pattern pattern2;
            try {
                Pattern pattern3 = this.scheme;
                return (pattern3 == null || pattern3.matcher(uri.getScheme()).matches()) && ((pattern = this.host) == null || pattern.matcher(uri.getHost()).matches()) && (((num = this.port) == null || num.equals(Integer.valueOf(uri.getPort()))) && ((pattern2 = this.path) == null || pattern2.matcher(uri.getPath()).matches()));
            } catch (Exception e) {
                LOG.d(AllowList.TAG, e.toString());
                return false;
            }
        }
    }

    public void addAllowListEntry(String str, boolean z) {
        String str2 = Marker.ANY_MARKER;
        if (this.allowList != null) {
            try {
                if (str.compareTo(Marker.ANY_MARKER) == 0) {
                    LOG.d(TAG, "Unlimited access to network resources");
                    this.allowList = null;
                    return;
                }
                Matcher matcher = Pattern.compile("^((\\*|[A-Za-z-]+):(//)?)?(\\*|((\\*\\.)?[^*/:]+))?(:(\\d+))?(/.*)?").matcher(str);
                if (matcher.matches()) {
                    String strGroup = matcher.group(2);
                    String strGroup2 = matcher.group(4);
                    if ((!"file".equals(strGroup) && !"content".equals(strGroup)) || strGroup2 != null) {
                        str2 = strGroup2;
                    }
                    String strGroup3 = matcher.group(8);
                    String strGroup4 = matcher.group(9);
                    if (strGroup == null) {
                        this.allowList.add(new URLPattern("http", str2, strGroup3, strGroup4));
                        this.allowList.add(new URLPattern("https", str2, strGroup3, strGroup4));
                    } else {
                        this.allowList.add(new URLPattern(strGroup, str2, strGroup3, strGroup4));
                    }
                }
            } catch (Exception unused) {
                LOG.d(TAG, "Failed to add origin %s", str);
            }
        }
    }

    public boolean isUrlAllowListed(String str) {
        if (this.allowList == null) {
            return true;
        }
        Uri uri = Uri.parse(str);
        Iterator<URLPattern> it = this.allowList.iterator();
        while (it.hasNext()) {
            if (it.next().matches(uri)) {
                return true;
            }
        }
        return false;
    }
}