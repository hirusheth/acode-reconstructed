package com.foxdebug.browser;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.foxdebug.system.Ui;

/* JADX INFO: compiled from: Emulator.java */
/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes.dex */
class DeviceView extends LinearLayout {
    Context context;
    Device device;
    ImageView icon;
    boolean isSelected;
    TextView label;
    Ui.Theme theme;

    /* JADX INFO: compiled from: Emulator.java */
    public static abstract class Callback {
        public abstract void onSelect(DeviceView deviceView);
    }

    public DeviceView(Context context, Device device, Ui.Theme theme) {
        super(context);
        this.isSelected = false;
        int i = theme.get("primaryTextColor");
        this.theme = theme;
        this.device = device;
        this.context = context;
        device.view = this;
        setId(device.id);
        setClickable(true);
        setPadding(0, 5, 0, 5);
        setOrientation(0);
        ImageView imageView = new ImageView(context);
        this.icon = imageView;
        imageView.setImageBitmap(Ui.Icons.get(context, device.icon, i));
        this.icon.setPadding(0, 0, 5, 0);
        this.icon.setLayoutParams(new LinearLayout.LayoutParams(-2, -2));
        TextView textView = new TextView(context);
        this.label = textView;
        textView.setSingleLine(true);
        this.label.setText(device.name);
        this.label.setTextColor(i);
        addView(this.icon);
        addView(this.label);
    }

    public void setOnSelect(final Callback callback) {
        setOnClickListener(new View.OnClickListener() { // from class: com.foxdebug.browser.DeviceView.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                callback.onSelect(this);
            }
        });
    }

    public void deselect() {
        int i = this.theme.get("primaryTextColor");
        this.icon.setImageBitmap(Ui.Icons.get(this.context, this.device.icon, i));
        this.label.setTextColor(i);
        this.label.setTypeface(null, 0);
        this.isSelected = false;
    }

    public void select() {
        int i = this.theme.get("activeTextColor");
        this.icon.setImageBitmap(Ui.Icons.get(this.context, this.device.icon, i));
        this.label.setTextColor(i);
        this.label.setTypeface(null, 1);
        this.isSelected = true;
    }
}