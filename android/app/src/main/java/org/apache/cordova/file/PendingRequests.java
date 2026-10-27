package org.apache.cordova.file;

import android.util.SparseArray;
import org.apache.cordova.CallbackContext;

/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes2.dex */
class PendingRequests {
    private int currentReqId = 0;
    private SparseArray<Request> requests = new SparseArray<>();

    PendingRequests() {
    }

    public synchronized int createRequest(String str, int i, CallbackContext callbackContext) throws Throwable {
        try {
            try {
                Request request = new Request(str, i, callbackContext);
                this.requests.put(request.requestCode, request);
                return request.requestCode;
            } catch (Throwable th) {
                th = th;
                throw th;
            }
        } catch (Throwable th2) {
            th = th2;
            throw th;
        }
    }

    public synchronized Request getAndRemove(int i) {
        Request request;
        request = this.requests.get(i);
        this.requests.remove(i);
        return request;
    }

    public class Request {
        private int action;
        private CallbackContext callbackContext;
        private String rawArgs;
        private int requestCode;

        private Request(String str, int i, CallbackContext callbackContext) {
            this.rawArgs = str;
            this.action = i;
            this.callbackContext = callbackContext;
            int i2 = PendingRequests.this.currentReqId;
            PendingRequests.this.currentReqId = i2 + 1;
            this.requestCode = i2;
        }

        public int getAction() {
            return this.action;
        }

        public String getRawArgs() {
            return this.rawArgs;
        }

        public CallbackContext getCallbackContext() {
            return this.callbackContext;
        }
    }
}