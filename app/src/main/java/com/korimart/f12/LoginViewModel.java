package com.korimart.f12;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class LoginViewModel extends ViewModel {
    private MutableLiveData<String> systemMeessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> fetching = new MutableLiveData<>();

    private WebService webService = WebService.INSTANCE;
    private ErrorReporter errorReporter = ErrorReporter.INSTANCE;

    public void onViewCreated(MainActivity mainActivity, Runnable onSuccess){
        SharedPreferences prefs = mainActivity.getPreferences(Context.MODE_PRIVATE);
        String id = prefs.getString("id", null);
        String password = prefs.getString("password", null);

        if (id != null && password != null)
            login(mainActivity, id, password, false, onSuccess);
    }

    public void login(MainActivity mainActivity, String id, String password, boolean save, Runnable onSuccess){
        fetching.setValue(true);
        tryLogin(id, password)
                .whenComplete((ignored1, ignored2) -> fetching.postValue(false))
                .thenRun(() -> {
                    if (save){
                        SharedPreferences prefs = mainActivity.getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("id", id);
                        editor.putString("password", password);
                        editor.apply();
                    }

                    mainActivity.runOnUiThread(onSuccess);
                })
                .exceptionally(t -> {
                    errorReporter.backgroundErrorHandler(t, errorInfo1 -> onError(errorInfo1, mainActivity));
                    return null;
                });
    }

    private CompletableFuture<Void> tryLogin(String id, String password){
        return CompletableFuture.runAsync(() -> {
            String response = webService.sendPost(
                    URLStorage.getLoginURL(),
                    URLStorage.getLoginParams(id, password),
                    "euc-kr");

            if (response.isEmpty()){
                throw new CompletionException(new ErrorInfo(ErrorInfo.ErrorType.timeout));
            }

            if (response.contains("전산실 요청에 의해 제거함")){
                throw new CompletionException(new ErrorInfo(ErrorInfo.ErrorType.loginFailed));
            }
        });
    }

    private void onError(ErrorInfo errorInfo, MainActivity mainActivity) {
        switch (errorInfo.type){
            case timeout:
            case responseFailed:
                systemMeessage.setValue("포털 연결 실패");
                break;

            case loginFailed:
                systemMeessage.setValue("로그인 실패");
                break;

            case noLoginFile:
                break;

            default:
                mainActivity.goToErrorFrag(errorInfo.throwable);
                break;
        }
    }

    public LiveData<String> getMessage() {
        return systemMeessage;
    }

    public LiveData<Boolean> getFetching() {
        return fetching;
    }
}
