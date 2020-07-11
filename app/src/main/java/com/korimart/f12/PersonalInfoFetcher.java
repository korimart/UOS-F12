package com.korimart.f12;

import org.w3c.dom.Document;

public enum PersonalInfoFetcher {
    INSTANCE;

    public static final String url = "https://wise.uos.ac.kr/uosdoc/usr.UsrMasterInq.do";
    public static final String params = "strStudId=123123&_user_info=userid&&_COMMAND_=list&&_XML_=XML&_strMenuId=stud00040&";

    private XMLHelper xmlHelper = XMLHelper.INSTANCE;
    private WebService webService = WebService.INSTANCE;

    public static class Result {
        int yearLevel;
        ErrorInfo errorInfo;
    }

    public Result fetch(){
        Result result = new Result();

        try {
            byte[] response = webService.sendPost(url, params);
            String responseStr = new String(response, "euc-kr");
            if (responseStr.contains("세션타임")){
                result.errorInfo = new ErrorInfo("sessionExpired", null);
                return result;
            }

            Document doc = xmlHelper.getDocument(response);
            if (doc == null){
                result.errorInfo = new ErrorInfo("responseFailed", null);
                return result;
            }

            parse(doc, result);

        } catch (Exception e){
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

    public void parse(Document doc, Result result){
        try {
            result.yearLevel = Integer.parseInt(xmlHelper.getContentByName(doc, "shyr"));
        } catch (NumberFormatException e){
            result.errorInfo = new ErrorInfo("noYearLevel", null);
        }
    }
}
