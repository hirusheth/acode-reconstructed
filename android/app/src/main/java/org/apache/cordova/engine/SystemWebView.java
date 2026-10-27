package org.apache.cordova.engine;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaWebViewEngine;
import org.bouncycastle.asn1.cmp.PKIFailureInfo;

/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes2.dex */
public class SystemWebView extends WebView implements CordovaWebViewEngine.EngineView {
    private int NO_SUGGESTIONS;
    private int NO_SUGGESTIONS_AGGRESSIVE;
    SystemWebChromeClient chromeClient;
    private CordovaInterface cordova;
    private boolean nativeContextMenuDisabled;
    private SystemWebViewEngine parentEngine;
    private int type;
    private SystemWebViewClient viewClient;

    public SystemWebView(Context context) {
        this(context, null);
    }

    public void setInputType(int i) {
        this.type = i;
    }

    public void setNativeContextMenuDisabled(boolean z) {
        this.nativeContextMenuDisabled = z;
    }

    @Override // android.webkit.WebView, android.view.View
    public InputConnection onCreateInputConnection(EditorInfo editorInfo) {
        InputConnection inputConnectionOnCreateInputConnection = super.onCreateInputConnection(editorInfo);
        int i = this.type;
        if (i == this.NO_SUGGESTIONS) {
            editorInfo.inputType |= PKIFailureInfo.signerNotTrusted;
            return inputConnectionOnCreateInputConnection;
        }
        if (i == this.NO_SUGGESTIONS_AGGRESSIVE) {
            editorInfo.inputType = 524432;
            return inputConnectionOnCreateInputConnection;
        }
        editorInfo.inputType = editorInfo.inputType;
        return inputConnectionOnCreateInputConnection;
    }

    @Override // android.view.View
    public ActionMode startActionMode(ActionMode.Callback callback) {
        return suppressActionMode(super.startActionMode(wrapActionModeCallback(callback)));
    }

    @Override // android.view.View
    public ActionMode startActionMode(ActionMode.Callback callback, int i) {
        return suppressActionMode(super.startActionMode(wrapActionModeCallback(callback), i));
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public ActionMode startActionModeForChild(View view, ActionMode.Callback callback) {
        return suppressActionMode(super.startActionModeForChild(view, wrapActionModeCallback(callback)));
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public ActionMode startActionModeForChild(View view, ActionMode.Callback callback, int i) {
        return suppressActionMode(super.startActionModeForChild(view, wrapActionModeCallback(callback), i));
    }

    private ActionMode.Callback wrapActionModeCallback(final ActionMode.Callback callback) {
        return (!this.nativeContextMenuDisabled || callback == null) ? callback : new ActionMode.Callback2() { // from class: org.apache.cordova.engine.SystemWebView.1
            @Override // android.view.ActionMode.Callback
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                boolean zOnCreateActionMode = callback.onCreateActionMode(actionMode, menu);
                if (zOnCreateActionMode) {
                    SystemWebView.this.suppressActionModeUi(actionMode, menu);
                }
                return zOnCreateActionMode;
            }

            @Override // android.view.ActionMode.Callback
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                boolean zOnPrepareActionMode = callback.onPrepareActionMode(actionMode, menu);
                SystemWebView.this.suppressActionModeUi(actionMode, menu);
                return zOnPrepareActionMode;
            }

            @Override // android.view.ActionMode.Callback
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                return callback.onActionItemClicked(actionMode, menuItem);
            }

            @Override // android.view.ActionMode.Callback
            public void onDestroyActionMode(ActionMode actionMode) {
                callback.onDestroyActionMode(actionMode);
            }

            @Override // android.view.ActionMode.Callback2
            public void onGetContentRect(ActionMode actionMode, View view, Rect rect) {
                ActionMode.Callback callback2 = callback;
                if (callback2 instanceof ActionMode.Callback2) {
                    ((ActionMode.Callback2) callback2).onGetContentRect(actionMode, view, rect);
                } else {
                    super.onGetContentRect(actionMode, view, rect);
                }
            }
        };
    }

    private ActionMode suppressActionMode(ActionMode actionMode) {
        if (actionMode != null && this.nativeContextMenuDisabled) {
            suppressActionModeUi(actionMode, actionMode.getMenu());
        }
        return actionMode;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void suppressActionModeUi(final ActionMode actionMode, Menu menu) {
        if (actionMode == null || !this.nativeContextMenuDisabled || menu == null) {
            return;
        }
        menu.clear();
        actionMode.setTitle((CharSequence) null);
        actionMode.setSubtitle((CharSequence) null);
        post(new Runnable() { // from class: org.apache.cordova.engine.SystemWebView$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m641lambda$suppressActionModeUi$0$orgapachecordovaengineSystemWebView(actionMode);
            }
        });
    }

    /* JADX INFO: renamed from: lambda$suppressActionModeUi$0$org-apache-cordova-engine-SystemWebView, reason: not valid java name */
    /* synthetic */ void m641lambda$suppressActionModeUi$0$orgapachecordovaengineSystemWebView(ActionMode actionMode) {
        if (this.nativeContextMenuDisabled) {
            try {
                actionMode.hide(0L);
            } catch (Throwable unused) {
            }
        }
    }

    public SystemWebView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.type = -1;
        this.NO_SUGGESTIONS = 0;
        this.NO_SUGGESTIONS_AGGRESSIVE = 1;
        this.nativeContextMenuDisabled = false;
    }

    void init(SystemWebViewEngine systemWebViewEngine, CordovaInterface cordovaInterface) {
        this.cordova = cordovaInterface;
        this.parentEngine = systemWebViewEngine;
        if (this.viewClient == null) {
            setWebViewClient(new SystemWebViewClient(systemWebViewEngine));
        }
        if (this.chromeClient == null) {
            setWebChromeClient(new SystemWebChromeClient(systemWebViewEngine));
        }
    }

    @Override // org.apache.cordova.CordovaWebViewEngine.EngineView
    public CordovaWebView getCordovaWebView() {
        SystemWebViewEngine systemWebViewEngine = this.parentEngine;
        if (systemWebViewEngine != null) {
            return systemWebViewEngine.getCordovaWebView();
        }
        return null;
    }

    @Override // android.webkit.WebView
    public void setWebViewClient(WebViewClient webViewClient) {
        this.viewClient = (SystemWebViewClient) webViewClient;
        super.setWebViewClient(webViewClient);
    }

    @Override // android.webkit.WebView
    public void setWebChromeClient(WebChromeClient webChromeClient) {
        this.chromeClient = (SystemWebChromeClient) webChromeClient;
        super.setWebChromeClient(webChromeClient);
    }

    @Override // android.webkit.WebView, android.view.ViewGroup, android.view.View
    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        Boolean boolOnDispatchKeyEvent = this.parentEngine.client.onDispatchKeyEvent(keyEvent);
        if (boolOnDispatchKeyEvent != null) {
            return boolOnDispatchKeyEvent.booleanValue();
        }
        return super.dispatchKeyEvent(keyEvent);
    }
}