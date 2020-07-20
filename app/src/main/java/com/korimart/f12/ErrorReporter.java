package com.korimart.f12;

import android.os.Handler;
import android.os.Looper;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.concurrent.CompletionException;
import java.util.function.Consumer;

public enum ErrorReporter {
    INSTANCE;

    private Handler handler = new Handler(Looper.getMainLooper());

    public void reportError(Throwable e){
        FirebaseCrashlytics.getInstance().recordException(e);
    }

    public void backgroundErrorHandler(Throwable throwable, Consumer<ErrorInfo> onError){
        if (throwable == null) return;

        if (throwable instanceof CompletionException)
            throwable = throwable.getCause();

        if (throwable instanceof ErrorInfo){
            Throwable finalThrowable = throwable;
            handler.post(() -> onError.accept((ErrorInfo) finalThrowable));
        }
        else {
            Throwable finalThrowable1 = throwable;
            handler.post(() -> onError.accept(new ErrorInfo(finalThrowable1)));
        }
    }
}
