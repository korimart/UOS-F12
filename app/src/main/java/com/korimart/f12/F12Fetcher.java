package com.korimart.f12;

import android.accounts.NetworkErrorException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public enum F12Fetcher {
    INSTANCE;

    private final String f12URL = "https://wise.uos.ac.kr/uosdoc/ugd.UgdOtcmInq.do";
    private final String smtParams = "_dept_authDept=auth&_code_smtList=CMN31&&_COMMAND_=onload&&_XML_=XML&_strMenuId=stud00320&";
    private final String f12Params = "strSchYear=%d&strSmtCd=%s&strStudId=123123&strDiv=2&&_COMMAND_=list&&_XML_=XML&_strMenuId=stud00320&";
    private final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    private DocumentBuilder builder;

    public static class ErrorInfo {
        public String errorType;
        public String callStack;

        public ErrorInfo(String type, String callStack){
            errorType = type;
            this.callStack = callStack;
        }
    }

    public static class Result {
        String studentInfo;
        String infoResponse;
        String f12Response;
        int totalPnts;
        int hiddenPnts;
        float hiddenAvg;
        float totalAvg;
        DisclosedInfo disclosedInfo;
        ErrorInfo errorInfo;
    }

    public Result fetchF12(boolean noPnp){
        Result result = new Result();

        try {
            try {
                result.infoResponse = fetchInfoResponse();
            } catch (NetworkErrorException e) {
                result.errorInfo = new ErrorInfo("sessionExpired", null);
                return result;
            }

            Document infoDoc = getDocument(result.infoResponse);
            if (infoDoc == null){
                result.errorInfo = new ErrorInfo("infoResponseFailed", null);
                return result;
            }

            result.f12Response = fetchF12Response(infoDoc);
            Document f12Doc = getDocument(result.f12Response);
            if (f12Doc == null){
                result.errorInfo = new ErrorInfo("f12ResponseFailed", null);
                return result;
            }

            result.studentInfo = getContentByName(f12Doc, "strMyShreg");
            if (result.studentInfo == null){
                result.errorInfo = new ErrorInfo("noStudentInfo", null);
                return result;
            }

            result.disclosedInfo = getInfo(f12Doc);
            if (result.disclosedInfo == null){
                result.errorInfo = new ErrorInfo("noDisclosedInfo", null);
                return result;
            }

            parse(f12Doc, result.disclosedInfo, noPnp, result);

        } catch (Exception e){
            StackTraceElement[] stes = e.getStackTrace();
            StringBuilder sb = new StringBuilder();
            sb.append(e.toString());
            for (StackTraceElement ste : stes) {
                sb.append(ste.toString());
                sb.append('\n');
            }
            result.errorInfo = new ErrorInfo("unknownError", sb.toString());
        }

        return result;
    }

    public float recalculateHiddenAvg(String f12Response, boolean noPnp){
        F12Fetcher.Result result = new F12Fetcher.Result();
        Document doc = getDocument(f12Response);
        if (doc == null) return 0f;

        DisclosedInfo info = getInfo(doc);
        if (info == null) return 0f;

        parse(doc, info, noPnp, result);
        return result.hiddenAvg;
    }

    private String fetchInfoResponse() throws NetworkErrorException {
        String infoResponse = WebService.INSTANCE.sendPost(f12URL, smtParams, "euc-kr");

        if (infoResponse.contains("세션타임")){
            throw new NetworkErrorException();
        }

        return infoResponse;
    }

    private String fetchF12Response(Document infoDoc){
        String smtString = getContentByName(infoDoc, "strSmt");
        LocalDateTime dt = LocalDateTime.now();
        int year = getSchoolYear(dt);

        return WebService.INSTANCE.sendPost(f12URL, String.format(f12Params, year, smtString), "euc-kr");
    }

    private void parse(Document f12Doc, DisclosedInfo info, boolean noPnp, Result result){
        float disclosedMarksFloat = 0;
        float disclosedPntsWithoutPnp = 0;
        for (DisclosedGrade dg : info.gradesForDisplay){
            disclosedMarksFloat += dg.getMarks();
            disclosedPntsWithoutPnp += dg.points;
        }

        String totPntString = getContentByName(f12Doc, "tot_pnt");
        String totalMarksString = getContentByName(f12Doc, "tot_mrks");
        String disclosedPntsString = getContentByName(f12Doc, "sum_pnt");
        String totalAvgString = getContentByName(f12Doc, "avg_mrks");

        float totPntFloat = Float.parseFloat(totPntString);
        float totalMarksFloat = Float.parseFloat(totalMarksString);
        float totalAvgFloat = Float.parseFloat(totalAvgString);
        float disclosedPntsFloat = Float.parseFloat(disclosedPntsString);

        int hiddenPntsInt = calculateHiddenPnts(totPntFloat, disclosedPntsFloat, info.nameOnlyCoursePnts);

        float hiddenAvgFloat;
        if (hiddenPntsInt == 0)
            hiddenAvgFloat = 0f;
        else {
            if (noPnp){
                hiddenAvgFloat = calculateHiddenAvgNoPnp(totalMarksFloat, disclosedMarksFloat, hiddenPntsInt);
            }
            else {
                hiddenAvgFloat = calculateHiddenAvg(totPntFloat, totalMarksFloat, totalAvgFloat,
                        disclosedMarksFloat, disclosedPntsWithoutPnp);
            }
        }

        result.totalPnts = (int) totPntFloat;
        result.hiddenPnts = hiddenPntsInt;
        result.hiddenAvg = hiddenAvgFloat;
        result.totalAvg = totalAvgFloat;
    }

    private Document getDocument(String xml){
        StringBuilder xmlStringBuilder = new StringBuilder();
        xmlStringBuilder.append(xml);
        ByteArrayInputStream input = null;

        try {
            input = new ByteArrayInputStream(xmlStringBuilder.toString().getBytes("euc-kr"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Document doc = null;

        if (builder == null){
            try {
                builder = factory.newDocumentBuilder();
            } catch (ParserConfigurationException ignore) {
            }
        }

        try {
            doc = builder.parse(input);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return doc;
    }

    private String getContentByName(Document doc, String name){
        NodeList nl = doc.getElementsByTagName(name);
        Node n = nl.item(0);

        if (n == null) return null;

        Node nc = n.getFirstChild();
        return nc.getNodeValue();
    }

    private int getSchoolYear(LocalDateTime dt) {
        int month = dt.getMonthValue();
        if (month < 5) return dt.getYear() - 1;
        return dt.getYear();
    }

    private DisclosedInfo getInfo(Document doc){
        DisclosedInfo ret = new DisclosedInfo();
        ret.gradesForDisplay = new ArrayList<>();
        NodeList points = doc.getElementsByTagName("pnt");
        NodeList grades = doc.getElementsByTagName("mrks");
        NodeList courses = doc.getElementsByTagName("curi_nm");
        NodeList letterGrades = doc.getElementsByTagName("conv_grade");

        if (points.getLength() == 0) return null;

        for (int i = 0; i < points.getLength(); i++){
            DisclosedGrade dg = new DisclosedGrade();
            dg.course = courses.item(i).getFirstChild().getNodeValue().trim();
            Node letterGradeNode = letterGrades.item(i).getFirstChild();
            // 과목명만 보이게 해놓고 성적 입력 안 해놓은 경우 ㅋㅋ
            if (letterGradeNode == null){
                ret.nameOnlyCoursePnts += Float.parseFloat(points.item(i).getFirstChild().getNodeValue());
                continue;
            }

            dg.letterGrade = letterGradeNode.getNodeValue().trim();
            if (dg.letterGrade.equals("S") || dg.letterGrade.equals("NS")){
                dg.grade = 0;
                dg.points = 0;
            }
            else {
                dg.grade = Float.parseFloat(grades.item(i).getFirstChild().getNodeValue());
                dg.points = Float.parseFloat(points.item(i).getFirstChild().getNodeValue());
            }

            ret.gradesForDisplay.add(dg);
        }

        return ret;
    }

    private int calculateHiddenPnts(float totPntFloat, float disclosedPntsFloat, float nameOnlyCoursePnts){
        return (int) (totPntFloat - disclosedPntsFloat + nameOnlyCoursePnts);
    }

    // returns to 1 decimal place
    private float calculateHiddenAvg(float totalPntsWithPnp, float totalMarksFloat, float totalAvgFloat,
                                           float disclosedMarksFloat, float disclosedPntsWithoutPnp) {
        // pass non-pass
        float pnpPntFloat = totalPntsWithPnp - totalMarksFloat / totalAvgFloat;
        float hiddenPntFloat = totalPntsWithPnp - disclosedPntsWithoutPnp - pnpPntFloat;
        float ret = hiddenPntFloat == 0.0f ? 0.0f :
                (totalMarksFloat - disclosedMarksFloat) / hiddenPntFloat;
        return Math.round(ret * 10f) / 10f;
    }

    private float calculateHiddenAvgNoPnp(float totalMarksFloat, float disclosedMarksFloat, int hiddenPntsInt){
        return (totalMarksFloat - disclosedMarksFloat) / hiddenPntsInt;
    }
}

class DisclosedGrade {
    public String course;
    public String letterGrade;
    public float points;
    public float grade;

    public float getMarks(){
        return points * grade;
    }
}

class DisclosedInfo {
    public List<DisclosedGrade> gradesForDisplay;
    public float nameOnlyCoursePnts;
}