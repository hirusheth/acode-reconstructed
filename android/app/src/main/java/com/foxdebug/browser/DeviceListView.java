package com.foxdebug.browser;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import com.foxdebug.system.Ui;

/* JADX INFO: compiled from: Emulator.java */
/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes.dex */
class DeviceListView extends ScrollView {
    Callback callback;
    Context context;
    LinearLayout deviceListLayout;
    DeviceView selectedDeviceView;
    Ui.Theme theme;

    /* JADX INFO: compiled from: Emulator.java */
    public static abstract class Callback {
        public abstract void onSelect(Device device);
    }

    public DeviceListView(Context context, Ui.Theme theme) {
        super(context);
        this.context = context;
        this.theme = theme;
        LinearLayout linearLayout = new LinearLayout(context);
        this.deviceListLayout = linearLayout;
        linearLayout.setOrientation(1);
        this.deviceListLayout.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        addView(this.deviceListLayout);
    }

    public void add(Device... deviceArr) {
        for (Device device : deviceArr) {
            add(device);
        }
    }

    public void select(Device device) {
        DeviceView deviceView = (DeviceView) findViewById(device.id);
        deviceView.select();
        this.selectedDeviceView = deviceView;
    }

    public void add(Device device) {
        DeviceView deviceView = new DeviceView(this.context, device, this.theme);
        this.deviceListLayout.addView(deviceView);
        deviceView.setOnSelect(new DeviceView.Callback() { // from class: com.foxdebug.browser.DeviceListView.1
            @Override // com.foxdebug.browser.DeviceView.Callback
            public void onSelect(DeviceView deviceView2) {
                if (DeviceListView.this.callback == null) {
                    return;
                }
                if (DeviceListView.this.selectedDeviceView != null) {
                    DeviceListView.this.selectedDeviceView.deselect();
                }
                deviceView2.select();
                DeviceListView.this.callback.onSelect(deviceView2.device);
                DeviceListView.this.selectedDeviceView = deviceView2;
            }
        });
    }

    public void setOnSelect(Callback callback) {
        this.callback = callback;
    }
}