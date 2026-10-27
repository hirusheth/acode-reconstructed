package com.foxdebug.browser;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import com.foxdebug.acode.R;
import com.foxdebug.system.Ui;

/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes.dex */
public class Menu extends PopupWindow {
    private Callback callback;
    Context context;
    int imageSize;
    int itemHeight;
    private LinearLayout list;
    int padding;
    private Ui.Theme theme;

    public static abstract class Callback {
        public abstract void onSelect(String str, Boolean bool);
    }

    public Menu(Context context, Ui.Theme theme) {
        super(context);
        this.theme = theme;
        this.context = context;
        this.padding = Ui.dpToPixels(context, 5);
        this.imageSize = Ui.dpToPixels(context, 30);
        this.itemHeight = Ui.dpToPixels(context, 40);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(theme.get("popupBackgroundColor"));
        gradientDrawable.setCornerRadius(Ui.dpToPixels(context, 8));
        LinearLayout linearLayout = new LinearLayout(context);
        this.list = linearLayout;
        linearLayout.setOrientation(1);
        this.list.setBackgroundDrawable(gradientDrawable);
        LinearLayout linearLayout2 = this.list;
        int i = this.padding;
        linearLayout2.setPadding(i, i, i, i);
        this.list.setLayoutParams(new LinearLayout.LayoutParams(-2, -2));
        ScrollView scrollView = new ScrollView(context);
        scrollView.addView(this.list);
        setElevation(10.0f);
        setFocusable(true);
        setContentView(scrollView);
        setBackgroundDrawable(gradientDrawable);
        setAnimationStyle(R.style.MenuAnimation);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void addItem(String str, String str2) {
        addItem(str, str2, null);
    }

    public void addItem(String str, String str2, Boolean bool) {
        int i = this.theme.get("popupTextColor");
        int i2 = this.theme.get("popupBackgroundColor");
        MenuItem menuItem = new MenuItem(this, str2);
        menuItem.setBackgroundColor(i2);
        menuItem.setIcon(str, i);
        menuItem.setText(str2, i);
        menuItem.setOnClickListener(new MenuItemCallback() { // from class: com.foxdebug.browser.Menu.1
            @Override // com.foxdebug.browser.MenuItemCallback
            public void onClick(MenuItem menuItem2) {
                Menu.this.callback.onSelect(menuItem2.action, menuItem2.checked);
                Menu.this.hide();
            }
        });
        if (bool != null) {
            menuItem.setChecked(bool.booleanValue());
        }
        this.list.addView(menuItem);
    }

    public void setChecked(String str, Boolean bool) {
        for (int i = 0; i < this.list.getChildCount(); i++) {
            MenuItem menuItem = (MenuItem) this.list.getChildAt(i);
            if (menuItem.action == str) {
                menuItem.setChecked(bool.booleanValue());
                return;
            }
        }
    }

    public void setVisible(String str, Boolean bool) {
        for (int i = 0; i < this.list.getChildCount(); i++) {
            MenuItem menuItem = (MenuItem) this.list.getChildAt(i);
            if (menuItem.action == str) {
                menuItem.setVisibility(bool.booleanValue() ? 0 : 8);
                return;
            }
        }
    }

    public void show(View view) {
        view.getLeft();
        view.getTop();
        int i = this.padding;
        showAtLocation(view, 53, i, i);
    }

    public void hide() {
        dismiss();
    }
}