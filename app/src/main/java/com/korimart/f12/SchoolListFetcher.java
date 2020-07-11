package com.korimart.f12;

import android.accounts.NetworkErrorException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public enum SchoolListFetcher {
    INSTANCE;

    public static final String url = "https://wise.uos.ac.kr/uosdoc/ucr.UcrMjTimeInq.do";
    public static final String params = "_code_smtList=CMN31&_code_cmpList=UCS12&&_COMMAND_=onload" +
            "&&_XML_=XML&_strMenuId=stud00180&";

    private WebService webService = WebService.INSTANCE;
    private XMLHelper xmlHelper = XMLHelper.INSTANCE;

    public static class Result {
        String response;
        HashMap<DeptInfo, List<DeptInfo>> schoolToDepts;
        ErrorInfo errorInfo;
    }

    public static class DeptInfo {
        String name;
        String code;
        String parentCode;
    }

    public Result fetch(){
        Result result = new Result();

        try {
            byte[] response = webService.sendPost(url, params);
            result.response = new String(response, "euc-kr");
            if (result.response.contains("세션타임")){
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
