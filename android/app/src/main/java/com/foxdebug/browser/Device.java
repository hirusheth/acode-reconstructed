package com.foxdebug.browser;

import android.view.View;
import com.foxdebug.system.Ui;

/* JADX INFO: compiled from: Emulator.java */
/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes.dex */
class Device {
    public int height;
    public String icon;
    public int id;
    public boolean isDesktop;
    public String name;
    public DeviceView view;
    public int width;

    public Device(String str, int i, int i2, String str2, boolean z) {
        this.id = View.generateViewId();
        this.name = str;
        this.icon = str2;
        this.width = i;
        this.height = i2;
        this.isDesktop = z;
    }

    public Device(String str, int i, int i2, String str2) {
        this(str, i, i2, str2, true);
    }

    public Device(String str, int i, int i2) {
        this(str, i, i2, Ui.Icons.TUNE, true);
    }

    public void select() {
        DeviceView deviceView = this.view;
        if (deviceView != null) {
            deviceView.select();
        }
    }

    public void deselect() {
        DeviceView deviceView = this.view;
        if (deviceView != null) {
            deviceView.deselect();
        }
    }
}