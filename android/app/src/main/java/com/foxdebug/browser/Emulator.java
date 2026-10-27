package com.foxdebug.browser;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import com.foxdebug.system.Ui;
import java.util.HashMap;

/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes.dex */
public class Emulator extends LinearLayout {
    private Context context;
    private Device customDevice;
    private DeviceListView deviceListView;
    private boolean initialized;
    private Callback listener;
    private View reference;
    private HashMap<String, SeekBar> seekBars;
    private LinearLayout seekBarsLayout;
    private Device selectedDevice;
    private Ui.Theme theme;

    public static abstract class Callback {
        public abstract void onChange(int i, int i2, float f);
    }

    public Emulator(Context context, Ui.Theme theme) {
        super(context);
        this.initialized = false;
        this.seekBars = new HashMap<>();
        this.context = context;
        this.theme = theme;
        this.customDevice = new Device("Custom", 0, 0, Ui.Icons.TUNE, false);
        DeviceListView deviceListView = new DeviceListView(context, theme);
        this.deviceListView = deviceListView;
        deviceListView.setLayoutParams(new LinearLayout.LayoutParams(Ui.dpToPixels(context, 100), -1));
        this.deviceListView.setOnSelect(new DeviceListView.Callback() { // from class: com.foxdebug.browser.Emulator.1
            @Override // com.foxdebug.browser.DeviceListView.Callback
            public void onSelect(Device device) {
                Emulator.this.selectDevice(device);
            }
        });
        this.deviceListView.add(this.customDevice, new Device("iPhone SE", 320, 568, Ui.Icons.PHONE_APPLE), new Device("iPhone 8", 375, 667, Ui.Icons.PHONE_APPLE), new Device("iPhone 8+", 414, 736, Ui.Icons.PHONE_APPLE), new Device("iPhone X", 375, 812, Ui.Icons.PHONE_APPLE), new Device("iPad", 768, 1024, Ui.Icons.TABLET_APPLE), new Device("iPad Pro", 1024, 1366, Ui.Icons.TABLET_APPLE), new Device("Galaxy S5", 360, 640, Ui.Icons.PHONE_ANDROID), new Device("Pixel 2", 411, 731, Ui.Icons.PHONE_ANDROID), new Device("Pixel 2 XL", 411, 823, Ui.Icons.PHONE_ANDROID), new Device("Nexus 5X", 411, 731, Ui.Icons.PHONE_ANDROID), new Device("Nexus 6P", 411, 731, Ui.Icons.PHONE_ANDROID), new Device("Nexus 7", 600, 960, Ui.Icons.TABLET_ANDROID), new Device("Nexus 10", 800, 1280, Ui.Icons.TABLET_ANDROID), new Device("Laptop", 1280, 800, Ui.Icons.LAPTOP), new Device("Laptop L", 1440, 900, Ui.Icons.LAPTOP), new Device("Laptop XL", 1680, 1050, Ui.Icons.LAPTOP), new Device("UHD 4k", 3840, 2160, Ui.Icons.TV));
        this.deviceListView.select(this.customDevice);
        LinearLayout linearLayout = new LinearLayout(context);
        this.seekBarsLayout = linearLayout;
        linearLayout.setPadding(0, 10, 0, 0);
        this.seekBarsLayout.setOrientation(1);
        this.seekBarsLayout.setLayoutParams(new LinearLayout.LayoutParams(-1, -2, 1.0f));
        View view = new View(context);
        view.setBackgroundColor(theme.get("borderColor"));
        view.setLayoutParams(new LinearLayout.LayoutParams(-1, 1));
        LinearLayout linearLayout2 = new LinearLayout(context);
        linearLayout2.setOrientation(0);
        linearLayout2.setBackgroundColor(theme.get("primaryColor"));
        linearLayout2.addView(this.seekBarsLayout);
        linearLayout2.addView(this.deviceListView);
        linearLayout2.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        addControl("width", 50, "Width");
        addControl("height", 50, "Height");
        addControl("scale", 50, "Scale");
        addView(view);
        addView(linearLayout2);
        setOrientation(1);
    }

    public void setChangeListener(Callback callback) {
        this.listener = callback;
    }

    public void setReference(View view) {
        final SeekBar seekBar = this.seekBars.get("width");
        final SeekBar seekBar2 = this.seekBars.get("height");
        final SeekBar seekBar3 = this.seekBars.get("scale");
        final int measuredWidth = view.getMeasuredWidth();
        final int measuredHeight = view.getMeasuredHeight();
        final int progress = seekBar.getProgress();
        final int progress2 = seekBar2.getProgress();
        seekBar.getMin();
        seekBar2.getMin();
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() { // from class: com.foxdebug.browser.Emulator.2
            @Override // android.view.ViewTreeObserver.OnGlobalLayoutListener
            public void onGlobalLayout() {
                Emulator.this.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int height = measuredHeight - Emulator.this.getHeight();
                int i = progress2;
                int i2 = progress;
                seekBar.setMax(measuredWidth);
                seekBar2.setMax(height);
                if (progress > measuredWidth || !Emulator.this.initialized) {
                    seekBar2.setProgress(measuredHeight);
                    i = measuredHeight;
                }
                if (progress2 > height || !Emulator.this.initialized) {
                    seekBar.setProgress(measuredWidth);
                    i2 = measuredWidth;
                }
                Emulator.this.setMaxScale(i2, i);
                seekBar3.setMin(100);
                seekBar3.setProgress(100);
                if (Emulator.this.listener != null) {
                    Emulator.this.listener.onChange(i2, height, 1.0f);
                }
            }
        });
    }

    public int getWidthProgress() {
        return this.seekBars.get("width").getProgress();
    }

    public int getHeightProgress() {
        return this.seekBars.get("height").getProgress();
    }

    public float getScaleProgress() {
        return this.seekBars.get("scale").getProgress() / 100.0f;
    }

    private void addControl(String str, int i, String str2) {
        LinearLayout linearLayout = new LinearLayout(this.context);
        TextView textView = new TextView(this.context);
        SeekBar seekBar = new SeekBar(this.context);
        linearLayout.setOrientation(1);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        seekBar.setMin(300);
        seekBar.setLayoutParams(new LinearLayout.LayoutParams(-1, i, 1.0f));
        textView.setText(String.format(str2, 0));
        textView.setPadding(10, 0, 0, 0);
        linearLayout.addView(textView);
        linearLayout.addView(seekBar);
        this.seekBarsLayout.addView(linearLayout);
        this.seekBars.put(str, seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() { // from class: com.foxdebug.browser.Emulator.3
            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onStartTrackingTouch(SeekBar seekBar2) {
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onStopTrackingTouch(SeekBar seekBar2) {
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onProgressChanged(SeekBar seekBar2, int i2, boolean z) {
                if (!z || Emulator.this.listener == null) {
                    return;
                }
                String str3 = "width";
                if (seekBar2 != Emulator.this.seekBars.get("width")) {
                    str3 = "height";
                    if (seekBar2 != Emulator.this.seekBars.get("height")) {
                        str3 = "scale";
                    }
                }
                Log.d("Emulator", str3);
                int heightProgress = Emulator.this.getHeightProgress();
                int widthProgress = Emulator.this.getWidthProgress();
                float scaleProgress = Emulator.this.getScaleProgress();
                if (str3 != "scale") {
                    Emulator.this.setMaxScale(widthProgress, heightProgress);
                    scaleProgress = 1.0f;
                }
                Emulator.this.listener.onChange(widthProgress, heightProgress, scaleProgress);
                if (str3 == "scale" || Emulator.this.selectedDevice == null || Emulator.this.selectedDevice.id == Emulator.this.customDevice.id) {
                    return;
                }
                Emulator emulator = Emulator.this;
                emulator.selectDevice(emulator.customDevice);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void selectDevice(Device device) {
        Device device2 = this.selectedDevice;
        if (device2 != null) {
            device2.deselect();
        }
        device.select();
        this.selectedDevice = device;
        if (device.id == this.customDevice.id) {
            return;
        }
        SeekBar seekBar = this.seekBars.get("width");
        SeekBar seekBar2 = this.seekBars.get("height");
        SeekBar seekBar3 = this.seekBars.get("scale");
        int max = seekBar.getMax();
        int max2 = seekBar2.getMax();
        int i = device.width;
        int i2 = device.height;
        if (i > max) {
            float f = i2;
            i2 = (int) (f - (((i - max) / i) * f));
        } else {
            max = i;
        }
        if (i2 > max2) {
            float f2 = (i2 - max2) / i2;
            float f3 = max;
            max = (int) (f3 - (f2 * f3));
        } else {
            max2 = i2;
        }
        seekBar.setProgress(max);
        seekBar2.setProgress(max2);
        setMaxScale(max, max2);
        int max3 = seekBar3.getMax();
        seekBar3.setProgress(max3);
        this.listener.onChange(max, max2, max3 / 100.0f);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setMaxScale(int i, int i2) {
        SeekBar seekBar = this.seekBars.get("scale");
        seekBar.setMax((int) (Math.min(this.seekBars.get("width").getMax() / i, this.seekBars.get("height").getMax() / i2) * 100.0f));
        seekBar.setProgress(100);
    }
}