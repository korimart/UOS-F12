package com.korimart.f12;

import org.w3c.dom.Document;

public enum PersonalInfoParser implements WiseParser {
    INSTANCE;

    private XMLHelper xmlHelper = XMLHelper.INSTANCE;

    public static class Result implements WiseParser.Result {
        int yearLevel;
        String schoolCode;
        String deptCode;
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

        result.schoolCode = xmlHelper.getContentByName(doc, "colg_cd");
        result.deptCode = xmlHelper.getContentByName(doc, "sust_cd");

        if (result.schoolCode == null || result.deptCode == null)
            result.errorInfo = new ErrorInfo(ErrorInfo.ErrorType.parseFailed, new Exception("school code not found"));

        return result;
    }
}
