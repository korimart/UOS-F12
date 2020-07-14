package com.korimart.f12;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public enum SchoolListParser implements WiseParser {
    INSTANCE;

    private XMLHelper xmlHelper = XMLHelper.INSTANCE;

    @Override
    public void parse(Document doc, WiseParser.Result result) {
        parse(doc, (Result) result);
    }

    public static class Result implements WiseParser.Result {
        HashMap<DeptInfo, List<DeptInfo>> schoolToDepts;
        int latestSchoolYear;
        String latestSemester;
        ErrorInfo errorInfo;

        @Override
        public ErrorInfo getErrorInfo() {
            return errorInfo;
        }
    }

    public static class DeptInfo {
        String name;
        String code;
        String parentCode;
    }

    public void parse(Document doc, Result result){
        Element univList = xmlHelper.getElementByName(doc, "univList");
        NodeList lists = univList.getChildNodes();

        HashMap<DeptInfo, List<DeptInfo>> ret = new HashMap<>();

        for (int i = 0; i < lists.getLength(); i++){
            Node list = lists.item(i);
            if (list.getNodeType() != Node.ELEMENT_NODE)
                continue;

            DeptInfo schoolOrDept = createDeptInfo(list);
            if (isSchool(list)){
                DeptInfo schoolFromHash = findKeyByCode(ret, schoolOrDept.code);
                if (schoolFromHash == null){
                    ret.put(schoolOrDept, new ArrayList<>());
                }
                else {
                    schoolFromHash.name = schoolOrDept.name;
                    schoolFromHash.parentCode = schoolOrDept.parentCode;
                }
            }
            // if department
            else {
                DeptInfo schoolFromHash = findKeyByCode(ret, schoolOrDept.parentCode);
                if (schoolFromHash == null){
                    DeptInfo newSchool = new DeptInfo();
                    newSchool.code = schoolOrDept.parentCode;
                    ret.put(newSchool, new ArrayList<>());
                }
                else {
                    ret.get(schoolFromHash).add(schoolOrDept);
                }
            }
        }

        Object[] schools = ret.keySet().toArray();
        for (Object s : schools){
            DeptInfo school = (DeptInfo) s;
            if (school.name == null)
                ret.remove(findKeyByCode(ret, school.code));
        }

        result.schoolToDepts = ret;

        try {
            result.latestSchoolYear = Integer.parseInt(xmlHelper.getContentByName(doc, "strSchYear"));
        } catch (NumberFormatException e){
            result.latestSchoolYear = -1;
        }
        result.latestSemester = xmlHelper.getContentByName(doc, "strSmtCd");

        if (result.schoolToDepts.isEmpty())
            result.errorInfo = new ErrorInfo("noSchoolFound", null);
    }

    private boolean isSchool(Node list){
        return getParentCode(list).equals("20000");
    }

    private String getParentCode(Node list){
        return xmlHelper.getContentByName((Element) list, "upper_dept_cd");
    }

    private String getDeptCode(Node list){
        return xmlHelper.getContentByName((Element) list, "dept_cd");
    }

    private DeptInfo findKeyByCode(HashMap<DeptInfo, List<DeptInfo>> hm, String code){
        for (DeptInfo di : hm.keySet()){
            if (di.code.equals(code))
                return di;
        }

        return null;
    }

    private DeptInfo createDeptInfo(Node list){
        DeptInfo ret = new DeptInfo();
        ret.code = getDeptCode(list);
        ret.name = xmlHelper.getContentByName((Element) list, "dept_nm");
        ret.parentCode = getParentCode(list);
        return ret;
    }
}
