package org.apache.cordova;

import android.content.Context;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes2.dex */
public class ConfigXmlParser {
    private static final String DEFAULT_CONTENT_SRC = "index.html";
    private static String DEFAULT_HOSTNAME = "localhost";
    private static String SCHEME_HTTP = "http";
    private static String SCHEME_HTTPS = "https";
    private static String TAG = "ConfigXmlParser";
    private String contentSrc;
    private String launchUrl;
    private CordovaPreferences prefs = new CordovaPreferences();
    private ArrayList<PluginEntry> pluginEntries = new ArrayList<>(20);
    boolean insideFeature = false;
    String service = "";
    String pluginClass = "";
    String paramType = "";
    boolean onload = false;

    public CordovaPreferences getPreferences() {
        return this.prefs;
    }

    public ArrayList<PluginEntry> getPluginEntries() {
        return this.pluginEntries;
    }

    public String getLaunchUrl() {
        if (this.launchUrl == null) {
            setStartUrl(this.contentSrc);
        }
        return this.launchUrl;
    }

    public void parse(Context context) {
        int identifier = context.getResources().getIdentifier("config", "xml", context.getClass().getPackage().getName());
        if (identifier == 0 && (identifier = context.getResources().getIdentifier("config", "xml", context.getPackageName())) == 0) {
            LOG.e(TAG, "res/xml/config.xml is missing!");
            return;
        }
        this.pluginEntries.add(new PluginEntry(AllowListPlugin.PLUGIN_NAME, "org.apache.cordova.AllowListPlugin", true));
        this.pluginEntries.add(new PluginEntry("SystemBarPlugin", "org.apache.cordova.SystemBarPlugin", true));
        this.pluginEntries.add(new PluginEntry("CordovaSplashScreenPlugin", "org.apache.cordova.SplashScreenPlugin", true));
        parse(context.getResources().getXml(identifier));
    }

    public void parse(XmlPullParser xmlPullParser) {
        int next = -1;
        while (next != 1) {
            if (next == 2) {
                handleStartTag(xmlPullParser);
            } else if (next == 3) {
                handleEndTag(xmlPullParser);
            }
            try {
                next = xmlPullParser.next();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e2) {
                e2.printStackTrace();
            }
        }
        onPostParse();
    }

    private void onPostParse() {
        if (this.contentSrc == null) {
            this.contentSrc = DEFAULT_CONTENT_SRC;
        }
    }

    public void handleStartTag(XmlPullParser xmlPullParser) {
        String name = xmlPullParser.getName();
        if (name.equals("feature")) {
            this.insideFeature = true;
            this.service = xmlPullParser.getAttributeValue(null, "name");
            return;
        }
        if (this.insideFeature && name.equals("param")) {
            String attributeValue = xmlPullParser.getAttributeValue(null, "name");
            this.paramType = attributeValue;
            if (attributeValue.equals("service")) {
                this.service = xmlPullParser.getAttributeValue(null, "value");
                return;
            }
            if (this.paramType.equals("package") || this.paramType.equals("android-package")) {
                this.pluginClass = xmlPullParser.getAttributeValue(null, "value");
                return;
            } else {
                if (this.paramType.equals("onload")) {
                    this.onload = "true".equals(xmlPullParser.getAttributeValue(null, "value"));
                    return;
                }
                return;
            }
        }
        if (name.equals("preference")) {
            this.prefs.set(xmlPullParser.getAttributeValue(null, "name").toLowerCase(Locale.ENGLISH), xmlPullParser.getAttributeValue(null, "value"));
        } else if (name.equals("content")) {
            String attributeValue2 = xmlPullParser.getAttributeValue(null, "src");
            if (attributeValue2 != null) {
                this.contentSrc = attributeValue2;
            } else {
                this.contentSrc = DEFAULT_CONTENT_SRC;
            }
        }
    }

    public void handleEndTag(XmlPullParser xmlPullParser) {
        if (xmlPullParser.getName().equals("feature")) {
            this.pluginEntries.add(new PluginEntry(this.service, this.pluginClass, this.onload));
            this.service = "";
            this.pluginClass = "";
            this.insideFeature = false;
            this.onload = false;
        }
    }

    private String getLaunchUrlPrefix() {
        if (this.prefs.getBoolean("AndroidInsecureFileModeEnabled", false)) {
            return "file:///android_asset/www/";
        }
        String lowerCase = this.prefs.getString("scheme", SCHEME_HTTPS).toLowerCase();
        String lowerCase2 = this.prefs.getString("hostname", DEFAULT_HOSTNAME).toLowerCase();
        if (!lowerCase.contentEquals(SCHEME_HTTP) && !lowerCase.contentEquals(SCHEME_HTTPS)) {
            LOG.d(TAG, "The provided scheme \"" + lowerCase + "\" is not valid. Defaulting to \"" + SCHEME_HTTPS + "\". (Valid Options=" + SCHEME_HTTP + "," + SCHEME_HTTPS + ")");
            lowerCase = SCHEME_HTTPS;
        }
        return lowerCase + "://" + lowerCase2 + IOUtils.DIR_SEPARATOR_UNIX;
    }

    private void setStartUrl(String str) {
        if (Pattern.compile("^[a-z-]+://").matcher(str).find()) {
            this.launchUrl = str;
            return;
        }
        String launchUrlPrefix = getLaunchUrlPrefix();
        if (str.charAt(0) == '/') {
            str = str.substring(1);
        }
        this.launchUrl = launchUrlPrefix + str;
    }
}