package com.korimart.f12;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

public enum ErrorReporter {
    INSTANCE;

    public void reportError(Exception e){
        FirebaseCrashlytics.getInstance().recordException(e);
    }
}
