package com.korimart.f12;

import java.util.Locale;

public class URLStorage {
    private static final String f12URL = "https://wise.uos.ac.kr/uosdoc/ugd.UgdOtcmInq.do";
    private static final String f12InfoParams = "_dept_authDept=auth&_code_smtList=CMN31&&_COMMAND_=onload&&_XML_=XML&_strMenuId=stud00320&";
    private static final String f12Params = "strSchYear=%d&strSmtCd=%s&strStudId=123123&strDiv=2&&_COMMAND_=list&&_XML_=XML&_strMenuId=stud00320&";

    public static String getF12URL() {
        return f12URL;
    }

    public static String getF12InfoParams() {
        return f12InfoParams;
    }

    public static String getF12Params(int schoolYear, String semester) {
        return String.format(Locale.US, f12Params, schoolYear, semester);
    }
}
