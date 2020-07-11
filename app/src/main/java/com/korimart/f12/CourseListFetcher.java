package com.korimart.f12;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public enum CourseListFetcher {
    INSTANCE;

    private static final String url = "https://wise.uos.ac.kr/uosdoc/ucr.UcrMjTimeInq.do";
    private static final String params = "strSchYear=%d&strSmtCd=%s&strUnivCd=%s&" +
            "strSustCd=%s&strCmpDivCd=&strCuriNo=&strClassNo=&strCuriNm=&strGradDivCd=20000&" +
            "strEtcYn=&&_COMMAND_=list&&_XML_=XML&_strMenuId=stud00180&";

    private XMLHelper xmlHelper = XMLHelper.INSTANCE;
    private WebService webService = WebService.INSTANCE;

    public static class CourseInfo {
        String name;
        String classification;
        String classNumber;
        String yearLevel;
        int points;
        String timePlace;
        String professor;
        boolean outsider;
        boolean doubleMajor;
        boolean minor;
        boolean nonKorean;
    }

    public static class Result {
        List<CourseInfo> courseInfos;
        ErrorInfo errorInfo;
    }

    public Result fetch(int schoolYear, String semester, String schoolCode, String deptCode){
        String formattedParams = String.format(params, schoolYear, semester, schoolCode, deptCode);
        Result result = new Result();

        try {
            byte[] response = webService.sendPost(url, formattedParams);
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
        result.courseInfos = new ArrayList<>();

        NodeList lists = doc.getElementsByTagName("list");
        for (int i = 0; i < lists.getLength(); i++) {
            Node list = lists.item(i);
            if (list.getNodeType() != Node.ELEMENT_NODE)
                continue;

            Element listEl = (Element) list;
            CourseInfo courseInfo = new CourseInfo();
            courseInfo.name = xmlHelper.getContentByName(listEl, "curi_nm");
            courseInfo.classification = xmlHelper.getContentByName(listEl, "cmp_div_nm");
            courseInfo.classNumber = xmlHelper.getContentByName(listEl, "class_no");
            courseInfo.yearLevel = xmlHelper.getContentByName(listEl, "cmp_shyr");
            courseInfo.points = Integer.parseInt(xmlHelper.getContentByName(listEl, "pnt"));
            courseInfo.timePlace = xmlHelper.getContentByName(listEl, "lec_eng_nm");
            courseInfo.professor = xmlHelper.getContentByName(listEl, "prof_nm");
            courseInfo.outsider = xmlHelper.getContentByName(listEl, "etc_sect_permit_yn").equals("Y");
            courseInfo.doubleMajor = xmlHelper.getContentByName(listEl, "sec_sect_permit_yn").equals("Y");
            courseInfo.minor = xmlHelper.getContentByName(listEl, "minor_sect_permit_yn").equals("Y");
            courseInfo.nonKorean = xmlHelper.getContentByName(listEl, "lsn_type_cd2").equals("외국어강의");

            result.courseInfos.add(courseInfo);
        }
    }
}
