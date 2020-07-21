package com.korimart.f12;

import androidx.core.util.Pair;

import org.w3c.dom.Document;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum SyllabusOParser implements WiseParser {
    INSTANCE;

    private XMLHelper xmlHelper = XMLHelper.INSTANCE;
    private Pattern ratePattern = Pattern.compile("\\((\\d+)\\)%|\\((\\d+)%\\)");

    public static class Result extends SyllabusUabOParser.Result {
        String summary;
        String textbook;
        String fileName;
        String filePath;
        String offlineRate;
        String onlineRate;
        String midtermOnlineCode;
        String finalOnlineCode;
        String quizOnlineCode;
        String quizOnlineCode2;

        private ErrorInfo errorInfo;

        @Override
        public ErrorInfo getErrorInfo() {
            return errorInfo;
        }
    }

    @Override
    public Result parse(Document doc) {
        Result result = new Result();

        result.lecPrac = xmlHelper.getContentByName(doc, "lec_prac_div_nm");
        result.yearLevel = xmlHelper.getContentByName(doc, "cmp_shyr");
        result.classification = xmlHelper.getContentByName(doc, "cmp_div_nm");
        result.pointsTime = xmlHelper.getContentByName(doc, "pnt");
        result.professor = xmlHelper.getContentByName(doc, "kor_nm");
        result.professorDept = xmlHelper.getContentByName(doc, "sust_section_nm");
        result.professorPhone = xmlHelper.getContentByName(doc, "chag_prof_cont_point");
        result.professorEmail = xmlHelper.getContentByName(doc, "email");
        result.professorWeb = xmlHelper.getContentByName(doc, "chag_prof_homepage");
        result.counseling = xmlHelper.getContentByName(doc, "chag_prof_counsel_time");
        result.rubricsType = xmlHelper.getContentByName(doc, "otcm_est_meth_nm");
        result.summary = xmlHelper.getContentByName(doc, "lec_goal_descr");
        result.textbook = xmlHelper.getContentByName(doc, "shbk_descr");
        result.fileName = xmlHelper.getContentByName(doc, "file_nm");
        result.filePath = xmlHelper.getContentByName(doc, "file_path");

        // COVID-19
        // set default (never null)
        result.offlineRate = "";
        result.onlineRate = "";
        result.midtermOnlineCode = "";
        result.finalOnlineCode = "";
        result.quizOnlineCode = "";
        result.quizOnlineCode2 = "";

        result.offlineRate = xmlHelper.getContentByName(doc, "lsn_offline_rate");
        result.onlineRate = xmlHelper.getContentByName(doc, "lsn_online_rate");
        result.midtermOnlineCode = xmlHelper.getContentByName(doc, "mid_onoffline_cd");
        result.finalOnlineCode = xmlHelper.getContentByName(doc, "end_onoffline_cd");
        result.quizOnlineCode = xmlHelper.getContentByName(doc, "etc_online_cd");
        result.quizOnlineCode2 = xmlHelper.getContentByName(doc, "etc_offline_cd");

        if (result.filePath != null){
            result.filePath = "https://wise.uos.ac.kr/uosdoc/pf_upload/" + result.filePath;
        }

        try {
            result.rubrics = getNonZeroRubrics(doc);
        } catch (Exception e) {
            result.errorInfo = new ErrorInfo(ErrorInfo.ErrorType.parseFailed, e);
            return result;
        }

        for (Field field : result.getClass().getFields()){
            if (field.getName().equals("errorInfo")) continue;

            try {
                if (field.get(result) == null){
                    result.errorInfo = new ErrorInfo(ErrorInfo.ErrorType.parseFailed,
                            new Exception("field " + field.getName() + " is null"));
                    return result;
                }
            } catch (IllegalAccessException e) {
                result.errorInfo = new ErrorInfo(ErrorInfo.ErrorType.unknown, e);
                return result;
            }
        }

        return result;
    }

    public List<Pair<String, Integer>> getNonZeroRubrics(Document doc) throws Exception {
        List<Pair<String, Integer>> ret = new ArrayList<>();

        final String[] tags = {
                "attend_rate_nm",
                "stud_portfolio_rate_nm",
                "share_rate_nm",
                "temp_prjt_rate_nm",
                "temp_test_rate_nm",
                "mid_prjt_rate_nm",
                "mid_test_rate_nm",
                "end_term_prjt_rate_nm",
                "end_term_test_rate_nm"
        };

        for (String tag : tags){
            String content = xmlHelper.getContentByName(doc, tag);
            if (content == null)
                throw new Exception("no tag " + tag);

            String[] nameAndRate = content.split(" ");
            if (nameAndRate.length < 3)
                throw new Exception(content + " doesn't have 3 parts");

            Matcher m = ratePattern.matcher(nameAndRate[nameAndRate.length - 1]);
            if (!m.find())
                throw new Exception(nameAndRate[nameAndRate.length - 1] + " could not match regex");

            String match = m.group(1) == null ? m.group(2) : m.group(1);
            if (!match.equals("0")){
                Pair<String, Integer> pair = new Pair<>(nameAndRate[1], Integer.parseInt(match));
                if (pair.second == null)
                    throw new NullPointerException();
                ret.add(pair);
            }
        }

        return ret;
    }
}
