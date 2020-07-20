package com.korimart.f12;

import java.util.Locale;

public class URLStorage {
    private static final String f12URL = "https://wise.uos.ac.kr/uosdoc/ugd.UgdOtcmInq.do";
    private static final String f12InfoParams = "_dept_authDept=auth&_code_smtList=CMN31&&_COMMAND_=onload&&_XML_=XML&_strMenuId=stud00320&";
    private static final String f12Params = "strSchYear=%d&strSmtCd=%s&strStudId=123123&strDiv=2&&_COMMAND_=list&&_XML_=XML&_strMenuId=stud00320&";

    private static final String schoolListUrl = "https://wise.uos.ac.kr/uosdoc/ucr.UcrMjTimeInq.do";
    private static final String schoolListParams = "_code_smtList=CMN31&_code_cmpList=UCS12&&_COMMAND_=onload" +
            "&&_XML_=XML&_strMenuId=stud00180&";

    private static final String persInfoUrl = "https://wise.uos.ac.kr/uosdoc/usr.UsrMasterInq.do";
    private static final String persInfoParams = "strStudId=123123&_user_info=userid&&_COMMAND_=list&&_XML_=XML&_strMenuId=stud00040&";

    private static final String courseListUrl = "https://wise.uos.ac.kr/uosdoc/ucr.UcrMjTimeInq.do";
    private static final String courseListParam = "strSchYear=%d&strSmtCd=%s&strUnivCd=%s&" +
            "strSustCd=%s&strCmpDivCd=&strCuriNo=&strClassNo=&strCuriNm=&strGradDivCd=20000&" +
            "strEtcYn=&&_COMMAND_=list&&_XML_=XML&_strMenuId=stud00180&";

    private static final String syllabusUabUrl = "https://wise.uos.ac.kr/uosdoc/uab.UabCoursePlanView.serv";
    private static final String syllabusNonUabUrl = "https://wise.uos.ac.kr/uosdoc/ucs.UcsCoursePlanViewPopup.serv";
    private static final String syllabusParams = "strSchYear=%s&strSmtCd=%s&strCuriNo=%s&strClassNo=%s&" +
            "strCuriNm=%s&strSmtNm=%s&strPgmCd=%s&strViewDiv=%s&&_COMMAND_=list&&_XML_=XML&_strMenuId=stud00180&";

    public static String getF12URL() {
        return f12URL;
    }

    public static String getF12InfoParams() {
        return f12InfoParams;
    }

    public static String getF12Params(int schoolYear, String semester) {
        return String.format(Locale.US, f12Params, schoolYear, semester);
    }

    public static String getSchoolListUrl() {
        return schoolListUrl;
    }

    public static String getSchoolListParams() {
        return schoolListParams;
    }

    public static String getPersInfoUrl() {
        return persInfoUrl;
    }

    public static String getPersInfoParams() {
        return persInfoParams;
    }

    public static String getCourseListUrl() {
        return courseListUrl;
    }

    public static String getCourseListParam(int schoolYear, String semester,
                                            String schoolCode, String deptCode) {
        return String.format(
                Locale.US,
                courseListParam,
                schoolYear,
                semester,
                schoolCode,
                deptCode);
    }

    public static String getSyllabusUabUrl() {
        return syllabusUabUrl;
    }

    public static String getSyllabusNonUabUrl() {
        return syllabusNonUabUrl;
    }

    public static String getSyllabusParams(String schoolYear, String semester, String curriNumber,
                                           String classNumber, String division) {
        return String.format(
                Locale.getDefault(),
                syllabusParams,
                schoolYear,
                semester,
                curriNumber,
                classNumber,
                "", // 무시해도 잘됨
                "",
                "",
                division
        );
    }
}
