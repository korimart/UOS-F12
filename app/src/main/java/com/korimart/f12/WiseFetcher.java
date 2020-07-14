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
            if (result.response.contains("세션타임")) {
                result.errorInfo = new ErrorInfo("sessionExpired", null);
                return result;
            }

            result.document = xmlHelper.getDocument(result.byteResponse);
            if (result.document == null) {
                result.errorInfo = new ErrorInfo("responseFailed", null);
                return result;
            }

        } catch (Exception e) {
            StackTraceElement[] stes = e.getStackTrace();
            StringBuilder sb = new StringBuilder();
            sb.append(e.toString() + "\n");
            for (StackTraceElement ste : stes) {
                sb.append(ste.toString());
                sb.append('\n');
            }
            result.errorInfo = new ErrorInfo("unknownError", sb.toString());
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
