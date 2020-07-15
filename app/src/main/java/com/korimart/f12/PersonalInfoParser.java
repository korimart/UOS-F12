package com.korimart.f12;

import org.w3c.dom.Document;

public enum PersonalInfoParser implements WiseParser {
    INSTANCE;

    private XMLHelper xmlHelper = XMLHelper.INSTANCE;

    public static class Result implements WiseParser.Result {
        int yearLevel;
        ErrorInfo errorInfo;

        @Override
        public ErrorInfo getErrorInfo() {
            return errorInfo;
        }
    }

    @Override
    public Result parse(Document doc){
        Result result = new Result();

        try {
            result.yearLevel = Integer.parseInt(xmlHelper.getContentByName(doc, "shyr"));
        } catch (NumberFormatException e){
            result.errorInfo = new ErrorInfo(ErrorInfo.ErrorType.parseFailed, new Exception("no year level"));
        }

        return result;
    }
}
