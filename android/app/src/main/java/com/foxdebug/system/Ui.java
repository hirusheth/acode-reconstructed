package com.foxdebug.system;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.TypedValue;
import org.json.JSONObject;

/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes.dex */
public class Ui {

    public static class Icons {
        public static final String DESKTOP = "\ue90a";
        public static final String DEVICES = "\ue907";
        public static final String EXIT = "\ue902";
        private static final String FONT_PATH = "font/icon.ttf";
        public static final String LAPTOP = "\ue90d";
        public static final String LOGO = "\ue922";
        public static final String MORE_VERT = "\ue91a";
        public static final String NO_CACHE = "\ue901";
        public static final String OPEN_IN_BROWSER = "\ue91f";
        public static final String PHONE_ANDROID = "\ue90e";
        public static final String PHONE_APPLE = "\ue928";
        public static final String REFRESH = "\ue91b";
        public static final String TABLET_ANDROID = "\ue90f";
        public static final String TABLET_APPLE = "\ue92a";
        public static final String TERMINAL = "\ue923";
        public static final String TUNE = "\ue927";
        public static final String TV = "\ue929";
        private static int color = Color.parseColor("#FFFFFF");
        private static Paint paint = null;
        private static int size = 24;

        public static Bitmap get(Context context, String str, int i, int i2) {
            if (paint == null) {
                Paint paint2 = new Paint();
                paint = paint2;
                paint2.setAntiAlias(true);
                paint.setTypeface(Typeface.createFromAsset(context.getAssets(), FONT_PATH));
                paint.setTextAlign(Paint.Align.CENTER);
            }
            paint.setTextSize(i);
            paint.setColor(i2);
            float f = -paint.ascent();
            int iMeasureText = (int) paint.measureText(str, 0, str.length());
            Bitmap bitmapCreateBitmap = Bitmap.createBitmap(iMeasureText, (int) (paint.descent() + f), Bitmap.Config.ARGB_8888);
            new Canvas(bitmapCreateBitmap).drawText(str, iMeasureText / 2, f, paint);
            return bitmapCreateBitmap;
        }

        public static Bitmap get(Context context, String str, int i, String str2) {
            return get(context, str, i, Color.parseColor(str2));
        }

        public static Bitmap get(Context context, String str) {
            return get(context, str, size, color);
        }

        public static Bitmap get(Context context, String str, String str2) {
            return get(context, str, size, Color.parseColor(str2));
        }

        public static Bitmap get(Context context, String str, int i) {
            return get(context, str, size, i);
        }

        public static void setSize(int i) {
            size = i;
        }

        public static void setColor(int i) {
            color = i;
        }
    }

    public static class Theme {
        private JSONObject theme;

        public Theme(JSONObject jSONObject) {
            this.theme = jSONObject;
        }

        public int get(String str) {
            return get(str, "#000000");
        }

        public int get(String str, String str2) {
            return Color.parseColor(this.theme.optString(str, str2));
        }

        public String getType() {
            return this.theme.optString("type", "light");
        }
    }

    public static int dpToPixels(Context context, int i) {
        return (int) TypedValue.applyDimension(1, i, context.getResources().getDisplayMetrics());
    }
}