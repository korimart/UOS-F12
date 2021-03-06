package com.korimart.f12;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public enum CourseListParser implements WiseParser {
    INSTANCE;

    private XMLHelper xmlHelper = XMLHelper.INSTANCE;

    public static class CourseInfo {
        String schoolYear;
        String semester;
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
        String curriNumber;
        String programCode;
        boolean uab;
        String certDivCode;
        String TOYear;
        String TOYearMax;
        String TOAll;
        String TOAllMax;
        String deptCode;
    }

    public static class Result implements WiseParser.Result {
        List<CourseInfo> courseInfos;
        ErrorInfo errorInfo;

        @Override
        public ErrorInfo getErrorInfo() {
            return errorInfo;
        }
    }

    @Override
    public Result parse(Document doc){
        Result result = new Result();

        result.courseInfos = new ArrayList<>();

        NodeList lists = doc.getElementsByTagName("list");
        for (int i = 0; i < lists.getLength(); i++) {
            Node list = lists.item(i);
            if (list.getNodeType() != Node.ELEMENT_NODE)
                continue;

            Element listEl = (Element) list;
            CourseInfo courseInfo = null;
            try {
                courseInfo = createCourseInfo(listEl);
                result.courseInfos.add(courseInfo);
            } catch (Exception e) {
                result.errorInfo = new ErrorInfo(ErrorInfo.ErrorType.parseFailed, e);
                return result;
            }
        }

        return result;
    }

    private CourseInfo createCourseInfo(Element listEl) throws Exception {
        CourseInfo courseInfo = new CourseInfo();
        courseInfo.schoolYear = xmlHelper.getContentByName(listEl, "sch_year");
        courseInfo.semester = xmlHelper.getContentByName(listEl, "smt_cd");
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
        courseInfo.curriNumber = xmlHelper.getContentByName(listEl, "curi_no");
        courseInfo.programCode = xmlHelper.getContentByName(listEl, "pgm_cd");
        courseInfo.uab = xmlHelper.getContentByName(listEl, "uab_yn").equals("Y");
        courseInfo.certDivCode = xmlHelper.getContentByName(listEl, "cert_div_cd");
        courseInfo.TOYear = xmlHelper.getContentByName(listEl, "tlsn_count");
        courseInfo.TOYearMax = xmlHelper.getContentByName(listEl, "tlsn_limit_count");
        courseInfo.TOAll = xmlHelper.getContentByName(listEl, "tlsn_psn_cnt");
        courseInfo.TOAllMax = xmlHelper.getContentByName(listEl, "tlsn_aply_limit_psn_cnt");
        courseInfo.deptCode = xmlHelper.getContentByName(listEl, "asgn_sust_cd");

        for (Field field : courseInfo.getClass().getDeclaredFields()){
            if (field.get(courseInfo) == null)
                throw new Exception(field.getName() + " is null");
        }

        return courseInfo;
    }
}
