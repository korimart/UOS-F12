package com.korimart.f12;

import org.w3c.dom.Document;

public enum F12InfoParser implements WiseParser {
    INSTANCE;

    private XMLHelper xmlHelper = XMLHelper.INSTANCE;

    @Override
    public void parse(Document doc, WiseParser.Result result) {
        parse(doc, (Result) result);
    }

    public static class Result implements WiseParser.Result {
        int schoolYear;
        String semester;
        String schoolCode;
        String deptCode;
        ErrorInfo errorInfo;

        @Override
        public ErrorInfo getErrorInfo() {
            return errorInfo;
        }
    }

    public void parse(Document doc, Result result){
        try {
            result.schoolYear = Integer.parseInt(xmlHelper.getContentByName(doc, "strYear"));
        } catch (NumberFormatException e){
            result.errorInfo = new ErrorInfo("noUserInfo", null);
            return;
        }

        result.semester = xmlHelper.getContentByName(doc, "strSmt");
        result.schoolCode = xmlHelper.getLastContentByName(doc, "upper_dept_cd");
        result.deptCode = xmlHelper.getLastContentByName(doc, "dept_cd");

        if (result.semester == null || result.schoolCode == null || result.deptCode == null)
            result.errorInfo = new ErrorInfo("noUserInfo", null);
    }
}
