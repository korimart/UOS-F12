package com.korimart.f12;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public enum CourseListParser implements WiseParser {
    INSTANCE;

    private XMLHelper xmlHelper = XMLHelper.INSTANCE;

    @Override
    public void parse(Document doc, WiseParser.Result result) {
        parse(doc, (Result) result);
    }

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

    public static class Result implements WiseParser.Result {
        List<CourseInfo> courseInfos;
        ErrorInfo errorInfo;

        @Override
        public ErrorInfo getErrorInfo() {
            return errorInfo;
        }
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
