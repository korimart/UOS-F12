package com.korimart.f12;

import android.os.Handler;
import android.os.Looper;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.function.Consumer;

public enum ErrorReporter {
    INSTANCE;

    private Handler handler = new Handler(Looper.getMainLooper());

    public void reportError(Throwable e){
        FirebaseCrashlytics.getInstance().recordException(e);
    }

    public void backgroundErrorHandler(Throwable throwable, Consumer<ErrorInfo> onError){
        if (throwable == null) return;

        Throwable cause = throwable.getCause();

        if (cause instanceof ErrorInfo){
            handler.post(() -> onError.accept((ErrorInfo) cause));
        }
        else {
            handler.post(() -> onError.accept(new ErrorInfo(cause)));
        }
    }
}
