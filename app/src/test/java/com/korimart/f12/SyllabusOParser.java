package com.korimart.f12;

import org.w3c.dom.Document;

public class SyllabusOParser implements WiseParser {
    public static class Result implements WiseParser.Result {
        public ErrorInfo errorInfo;

        @Override
        public ErrorInfo getErrorInfo() {
            return errorInfo;
        }
    }

    @Override
    public Result parse(Document doc) {
        return null;
    }
}
