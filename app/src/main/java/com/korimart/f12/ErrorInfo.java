package com.korimart.f12;

public class ErrorInfo {
    public String errorType;
    public String callStack;

    public ErrorInfo(String type, String callStack){
        errorType = type;
        this.callStack = callStack;
    }
}
