package org.apache.cordova;

/* JADX INFO: compiled from: D8$$SyntheticClass */
/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes2.dex */
public final /* synthetic */ class SystemBarPlugin$$ExternalSyntheticLambda3 implements Runnable {
    public final /* synthetic */ SystemBarPlugin f$0;

    public /* synthetic */ SystemBarPlugin$$ExternalSyntheticLambda3(SystemBarPlugin systemBarPlugin) {
        this.f$0 = systemBarPlugin;
    }

    @Override // java.lang.Runnable
    public final void run() {
        this.f$0.updateSystemBars();
    }
}