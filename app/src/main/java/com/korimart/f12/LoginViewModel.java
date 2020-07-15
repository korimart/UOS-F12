package com.korimart.f12;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.concurrent.CompletableFuture;

public class LoginViewModel extends ViewModel {
    public static final String loginParams = "_COMMAND_=LOGIN&strTarget=MAIN&strIpAddr=123.123.123.123" +
            "&strMacAddr=123.123.123.123&login_div_1_nm=%%C7%%D0%%BB%%FD&strLoginId=%s&strLoginPw=%s";
    public static final String loginURL = "https://wise.uos.ac.kr/uosdoc/com.StuLogin.serv";

    private MutableLiveData<Boolean> loginInfoReady = new MutableLiveData<>();
    private MutableLiveData<Boolean> loginTryComplete = new MutableLiveData<>();
    private MutableLiveData<String> message = new MutableLiveData<>();
    private MutableLiveData<Integer> messageColor = new MutableLiveData<>();

    private CompletableFuture<Void> loginInfoFuture;

    private String id;
    private String password;
    private ErrorInfo errorInfo;
    private boolean shouldWriteToFile = true;

    public CompletableFuture<Void> tryLogin(String id, String password){
        errorInfo = null;
        this.id = id;
        this.password = password;

        return CompletableFuture.runAsync(() -> {
            String response = WebService.INSTANCE.sendPost(loginURL, String.format(loginParams, id, password), "euc-kr");
            if (response.isEmpty()){
                errorInfo = new ErrorInfo("timeout", null);
                return;
            }

            if (response.contains("전산실 요청에 의해 제거함")){
                errorInfo = new ErrorInfo("loginFailed", null);
                return;
            }
        });
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
                    errorInfo = new ErrorInfo("noLoginInfo", null);
                } catch (IOException e) {
                    errorInfo = new ErrorInfo("unknownError", buildStackTraceString(e));
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
                errorInfo = new ErrorInfo("unknownError", buildStackTraceString(e));
            }
        });
    }

    private String buildStackTraceString(Exception e){
        StackTraceElement[] stes = e.getStackTrace();
        StringBuilder sb = new StringBuilder();
        sb.append(e.toString()).append("\n");
        for (StackTraceElement ste : stes) {
            sb.append(ste.toString());
            sb.append('\n');
        }

        return sb.toString();
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
