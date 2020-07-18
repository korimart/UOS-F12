package com.korimart.f12;

public class ErrorInfo extends Throwable {
    public enum ErrorType {
        parseFailed,
        responseFailed,
        sessionExpired,
        timeout,
        loginFailed,
        noLoginFile,
        noOneDisclosedGrade,
        departmentNotFound,
        unknown
    }

    public ErrorType type;
    public Throwable throwable;

    public ErrorInfo(Throwable e){
        this.type = ErrorType.unknown;
        this.throwable = e;
    }

    public ErrorInfo(ErrorType type){
        this.type = type;
        this.throwable = null;
    }

    public ErrorInfo(ErrorType type, Throwable e){
        this.type = type;
        this.throwable = e;
    }
}
