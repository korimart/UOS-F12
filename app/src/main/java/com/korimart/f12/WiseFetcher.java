package com.korimart.f12;

import org.w3c.dom.Document;

import java.io.UnsupportedEncodingException;

public enum WiseFetcher {
    INSTNACE;

    private WebService webService = WebService.INSTANCE;
    private XMLHelper xmlHelper = XMLHelper.INSTANCE;

    public static class Result {
        byte[] byteResponse;
        String response;
        Document document;
        ErrorInfo errorInfo;
    }

    public Result fetch(String url, String params) {
        Result result = new Result();

        try {
            fetchResponse(result, url, params);
            if (result.response.isEmpty()){
                result.errorInfo = new ErrorInfo(ErrorInfo.ErrorType.timeout);
                return result;
            }

            if (result.response.contains("세션타임")) {
                result.errorInfo = new ErrorInfo(ErrorInfo.ErrorType.sessionExpired);
                return result;
            }

            result.document = xmlHelper.getDocument(result.byteResponse);
            if (result.document == null) {
                result.errorInfo = new ErrorInfo(ErrorInfo.ErrorType.responseFailed);
                return result;
            }

        } catch (Exception e) {
            result.errorInfo = new ErrorInfo(e);
        }

        return result;
    }

    private void fetchResponse(Result result, String url, String params) {
        result.byteResponse = webService.sendPost(url, params);

        try {
            result.response = new String(result.byteResponse, "euc-kr");
        } catch (UnsupportedEncodingException ignore) {
        }
    }
}
