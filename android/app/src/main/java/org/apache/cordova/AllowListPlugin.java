package org.apache.cordova;

import android.content.Context;
import org.slf4j.Marker;
import org.xmlpull.v1.XmlPullParser;

/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes2.dex */
public class AllowListPlugin extends CordovaPlugin {
    protected static final String LOG_TAG = "CordovaAllowListPlugin";
    public static final String PLUGIN_NAME = "CordovaAllowListPlugin";
    private AllowList allowedIntents;
    private AllowList allowedNavigations;
    private AllowList allowedRequests;

    public AllowListPlugin() {
    }

    public AllowListPlugin(Context context) {
        this(new AllowList(), new AllowList(), null);
        new CustomConfigXmlParser().parse(context);
    }

    public AllowListPlugin(XmlPullParser xmlPullParser) {
        this(new AllowList(), new AllowList(), null);
        new CustomConfigXmlParser().parse(xmlPullParser);
    }

    public AllowListPlugin(AllowList allowList, AllowList allowList2, AllowList allowList3) {
        if (allowList3 == null) {
            allowList3 = new AllowList();
            allowList3.addAllowListEntry("file:///*", false);
            allowList3.addAllowListEntry("data:*", false);
        }
        this.allowedNavigations = allowList;
        this.allowedIntents = allowList2;
        this.allowedRequests = allowList3;
    }

    @Override // org.apache.cordova.CordovaPlugin
    public void pluginInitialize() {
        if (this.allowedNavigations == null) {
            this.allowedNavigations = new AllowList();
            this.allowedIntents = new AllowList();
            this.allowedRequests = new AllowList();
            new CustomConfigXmlParser().parse(this.webView.getContext());
        }
    }

    private class CustomConfigXmlParser extends ConfigXmlParser {
        private CordovaPreferences prefs;

        @Override // org.apache.cordova.ConfigXmlParser
        public void handleEndTag(XmlPullParser xmlPullParser) {
        }

        private CustomConfigXmlParser() {
            this.prefs = new CordovaPreferences();
        }

        @Override // org.apache.cordova.ConfigXmlParser
        public void handleStartTag(XmlPullParser xmlPullParser) {
            String attributeValue;
            String name = xmlPullParser.getName();
            boolean z = false;
            if (name.equals("content")) {
                AllowListPlugin.this.allowedNavigations.addAllowListEntry(xmlPullParser.getAttributeValue(null, "src"), false);
                return;
            }
            if (name.equals("allow-navigation")) {
                String attributeValue2 = xmlPullParser.getAttributeValue(null, "href");
                if (Marker.ANY_MARKER.equals(attributeValue2)) {
                    AllowListPlugin.this.allowedNavigations.addAllowListEntry("http://*/*", false);
                    AllowListPlugin.this.allowedNavigations.addAllowListEntry("https://*/*", false);
                    AllowListPlugin.this.allowedNavigations.addAllowListEntry("data:*", false);
                    return;
                }
                AllowListPlugin.this.allowedNavigations.addAllowListEntry(attributeValue2, false);
                return;
            }
            if (name.equals("allow-intent")) {
                AllowListPlugin.this.allowedIntents.addAllowListEntry(xmlPullParser.getAttributeValue(null, "href"), false);
                return;
            }
            if (!name.equals("access") || (attributeValue = xmlPullParser.getAttributeValue(null, "origin")) == null) {
                return;
            }
            if (Marker.ANY_MARKER.equals(attributeValue)) {
                AllowListPlugin.this.allowedRequests.addAllowListEntry("http://*/*", false);
                AllowListPlugin.this.allowedRequests.addAllowListEntry("https://*/*", false);
                return;
            }
            String attributeValue3 = xmlPullParser.getAttributeValue(null, "subdomains");
            AllowList allowList = AllowListPlugin.this.allowedRequests;
            if (attributeValue3 != null && attributeValue3.compareToIgnoreCase("true") == 0) {
                z = true;
            }
            allowList.addAllowListEntry(attributeValue, z);
        }
    }

    @Override // org.apache.cordova.CordovaPlugin
    public Boolean shouldAllowNavigation(String str) {
        return this.allowedNavigations.isUrlAllowListed(str) ? true : null;
    }

    @Override // org.apache.cordova.CordovaPlugin
    public Boolean shouldAllowRequest(String str) {
        return (Boolean.TRUE.equals(shouldAllowNavigation(str)) || this.allowedRequests.isUrlAllowListed(str)) ? true : null;
    }

    @Override // org.apache.cordova.CordovaPlugin
    public Boolean shouldOpenExternalUrl(String str) {
        return this.allowedIntents.isUrlAllowListed(str) ? true : null;
    }

    public AllowList getAllowedNavigations() {
        return this.allowedNavigations;
    }

    public void setAllowedNavigations(AllowList allowList) {
        this.allowedNavigations = allowList;
    }

    public AllowList getAllowedIntents() {
        return this.allowedIntents;
    }

    public void setAllowedIntents(AllowList allowList) {
        this.allowedIntents = allowList;
    }

    public AllowList getAllowedRequests() {
        return this.allowedRequests;
    }

    public void setAllowedRequests(AllowList allowList) {
        this.allowedRequests = allowList;
    }
}