package com.korimart.f12;

import org.w3c.dom.Document;

import java.util.List;

public class SyllabusWParser implements WiseParser {
    public static class Result implements WiseParser.Result {
        public String summary;
        public String textbook;
        public List<String> weeklyPlans;

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
