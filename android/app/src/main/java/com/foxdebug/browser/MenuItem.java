package com.foxdebug.browser;

import android.R;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.foxdebug.system.Ui;

/* JADX INFO: compiled from: Menu.java */
/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes.dex */
class MenuItem extends LinearLayout {
    public String action;
    public CheckBox checkBox;
    public Boolean checked;
    private Context context;
    private int iconSize;
    private int imageSize;
    private int itemHeight;
    private int padding;
    private int paddingLeft;
    private int paddingRight;
    private int paddingVertical;
    private int textColor;

    public MenuItem(Menu menu, String str) {
        super(menu.context);
        this.paddingLeft = 0;
        this.paddingRight = 0;
        this.paddingVertical = 0;
        this.action = str;
        this.context = menu.context;
        this.padding = menu.padding;
        this.imageSize = menu.imageSize;
        this.itemHeight = menu.itemHeight;
        this.iconSize = Ui.dpToPixels(this.context, 10);
        this.paddingRight = this.imageSize;
        this.paddingVertical = Ui.dpToPixels(this.context, 5);
        setPadding();
        setClickable(true);
        setLayoutParams(new LinearLayout.LayoutParams(-1, this.itemHeight));
        setGravity(16);
        setOrientation(0);
        addView(new TextView(this.context));
    }

    public void setPadding() {
        int i = this.paddingLeft;
        int i2 = this.paddingVertical;
        setPadding(i, i2, this.paddingRight, i2);
    }

    public void setIcon(String str, int i) {
        ImageView imageView = new ImageView(this.context);
        imageView.setImageBitmap(Ui.Icons.get(this.context, str, i));
        imageView.setBackgroundDrawable(null);
        int i2 = this.imageSize;
        imageView.setLayoutParams(new LinearLayout.LayoutParams(i2, i2));
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setAdjustViewBounds(true);
        int i3 = this.padding;
        imageView.setPadding(i3, i3, i3, i3);
        addView(imageView, 0);
    }

    public void setText(String str, int i) {
        this.textColor = i;
        TextView textView = new TextView(this.context);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -1, 1.0f);
        textView.setText(str);
        textView.setTextColor(this.textColor);
        textView.setGravity(16);
        textView.setLayoutParams(layoutParams);
        addView(textView, 1);
    }

    public void setChecked(boolean z) {
        this.checked = Boolean.valueOf(z);
        CheckBox checkBox = this.checkBox;
        if (checkBox != null) {
            checkBox.setChecked(z);
            return;
        }
        CheckBox checkBox2 = new CheckBox(this.context);
        this.checkBox = checkBox2;
        checkBox2.setChecked(z);
        this.checkBox.setEnabled(false);
        this.checkBox.setClickable(false);
        CheckBox checkBox3 = this.checkBox;
        int[][] iArr = {new int[]{R.attr.state_checked}, new int[]{-16842912}};
        int i = this.textColor;
        checkBox3.setButtonTintList(new ColorStateList(iArr, new int[]{i, i}));
        FrameLayout frameLayout = new FrameLayout(this.context);
        int i2 = this.imageSize;
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(i2, i2);
        layoutParams.gravity = 16;
        frameLayout.setLayoutParams(layoutParams);
        frameLayout.addView(this.checkBox);
        this.paddingRight = 0;
        setPadding();
        addView(frameLayout);
    }

    public void setOnClickListener(final MenuItemCallback menuItemCallback) {
        setOnClickListener(new View.OnClickListener() { // from class: com.foxdebug.browser.MenuItem.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                if (MenuItem.this.checkBox != null) {
                    MenuItem menuItem = MenuItem.this;
                    menuItem.checked = Boolean.valueOf(!menuItem.checked.booleanValue());
                    MenuItem.this.checkBox.setChecked(MenuItem.this.checked.booleanValue());
                }
                menuItemCallback.onClick(this);
            }
        });
    }
}