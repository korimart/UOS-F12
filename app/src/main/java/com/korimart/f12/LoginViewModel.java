package com.korimart.f12;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

public class LoginViewModel extends ViewModel {
    private MutableLiveData<String> systemMeessage = new MutableLiveData<>();

    private CompletableFuture<Void> loginInfoFuture;

    private WebService webService = WebService.INSTANCE;
    private ErrorReporter errorReporter = ErrorReporter.INSTANCE;

    private String id;
    private String password;
    private boolean shouldWriteToFile = true;

    private CompletableFuture<Void> tryLogin(MainActivity mainActivity, String id, String password){
        this.id = id;
        this.password = password;

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

        }).exceptionally(t -> {
            errorReporter.backgroundErrorHandler(t, errorInfo1 -> onError(errorInfo1, mainActivity));
            return null;
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
                mainActivity.goToErrorFrag();
                break;
        }
    }

    public CompletableFuture<Void> fetchLoginInfo(Context appContext){
        errorInfo = null;

        if (loginInfoFuture == null){
            loginInfoFuture = CompletableFuture.runAsync(() -> {
                try {
                    FileInputStream loginFile = appContext.openFileInput("loginInfo.txt");
                    InputStreamReader ist = new InputStreamReader(loginFile);
                    BufferedReader br = new BufferedReader(ist);
                    id = br.readLine();
                    password = br.readLine();
                } catch (FileNotFoundException e) {
                    errorInfo = new ErrorInfo(ErrorInfo.ErrorType.noLoginFile);
                } catch (IOException e) {
                    errorInfo = new ErrorInfo(e);
                }
            });
        }

        return loginInfoFuture;
    }

    public CompletableFuture<Void> writeLoginInfo(Context appContext, String id, String password){
        errorInfo = null;

        return CompletableFuture.runAsync(() -> {
            OutputStreamWriter osw = null;
            try {
                osw = new OutputStreamWriter(
                        appContext.openFileOutput("loginInfo.txt", Context.MODE_PRIVATE));
                osw.write(id + "\n" + password);
                osw.flush();
                osw.close();
            } catch (IOException e) {
                errorInfo = new ErrorInfo(e);
            }
        });
    }

    public String getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }

    public ErrorInfo getErrorInfo() {
        return errorInfo;
    }

    public MutableLiveData<Boolean> getLoginInfoReady() {
        return loginInfoReady;
    }

    public MutableLiveData<Boolean> getLoginTryComplete() {
        return loginTryComplete;
    }

    public boolean isShouldWriteToFile() {
        return shouldWriteToFile;
    }

    public void setShouldWriteToFile(boolean shouldWriteToFile) {
        this.shouldWriteToFile = shouldWriteToFile;
    }

    public MutableLiveData<String> getMessage() {
        return message;
    }

    public MutableLiveData<Integer> getMessageColor() {
        return messageColor;
    }
}
