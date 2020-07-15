package com.korimart.f12;

public class ErrorInfo {
    public enum ErrorType {
        parseFailed,
        responseFailed,
        sessionExpired,
        timeout,
        loginFailed,
        noLoginFile,
        noOneDisclosedGrade,
        unknown
    }

    public ErrorType type;
    public Exception exception;

    public ErrorInfo(Exception e){
        this.type = ErrorType.unknown;
        this.exception = e;
    }

    public ErrorInfo(ErrorType type){
        this.type = type;
        this.exception = null;
    }

    public ErrorInfo(ErrorType type, Exception e){
        this.type = type;
        this.exception = e;
    }
}
