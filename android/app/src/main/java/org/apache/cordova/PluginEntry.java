package org.apache.cordova;

/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes2.dex */
public final class PluginEntry {
    public final boolean onload;
    public final CordovaPlugin plugin;
    public final String pluginClass;
    public final String service;

    public PluginEntry(String str, CordovaPlugin cordovaPlugin) {
        this(str, cordovaPlugin.getClass().getName(), true, cordovaPlugin);
    }

    public PluginEntry(String str, CordovaPlugin cordovaPlugin, boolean z) {
        this(str, cordovaPlugin.getClass().getName(), z, cordovaPlugin);
    }

    public PluginEntry(String str, String str2, boolean z) {
        this(str, str2, z, null);
    }

    private PluginEntry(String str, String str2, boolean z, CordovaPlugin cordovaPlugin) {
        this.service = str;
        this.pluginClass = str2;
        this.onload = z;
        this.plugin = cordovaPlugin;
    }
}