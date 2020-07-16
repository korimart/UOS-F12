package com.korimart.f12;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public enum SyllabusUabWParser implements WiseParser {
    INSTANCE;

    private XMLHelper xmlHelper = XMLHelper.INSTANCE;

    public static class Result implements WiseParser.Result {
        public String summary;
        public String textbook;
        public List<String> weeklyPlans;

        public ErrorInfo errorInfo;

        @Override
        public ErrorInfo getErrorInfo() {
            return errorInfo;
        }
    }

    @Override
    public Result parse(Document doc) {
        Result result = new Result();

        result.summary = xmlHelper.getContentByName(doc, "lec_goal_descr");
        result.textbook = xmlHelper.getContentByName(doc, "lec_shbk_descr");

        if (result.summary == null || result.textbook == null){
            result.errorInfo = new ErrorInfo(
                    ErrorInfo.ErrorType.parseFailed,
                    new Exception("summary or textbook was null")
            );
            return result;
        }

        result.summary = result.summary.trim();
        result.textbook = result.textbook.trim();

        result.weeklyPlans = new ArrayList<>();

        NodeList lists = doc.getElementsByTagName("list");
        if (lists.getLength() == 0){
            result.errorInfo = new ErrorInfo(
                    ErrorInfo.ErrorType.parseFailed,
                    new Exception("no list"));
            return result;
        }

        for (int i = 0; i < lists.getLength(); i++){
            Node list = lists.item(i);
            Element listElement = (Element) list;
            String week = xmlHelper.getContentByName(listElement, "week_seq");
            String plan = xmlHelper.getContentByName(listElement, "lec_descr");

            if (week == null || plan == null){
                result.errorInfo = new ErrorInfo(
                        ErrorInfo.ErrorType.parseFailed,
                        new Exception("week or plan was null")
                );
                return result;
            }

            try {
                if (Integer.parseInt(week) != i + 1){
                    result.errorInfo = new ErrorInfo(
                            ErrorInfo.ErrorType.parseFailed,
                            new Exception("weeks are not sequential")
                    );
                    return result;
                }
            } catch (NumberFormatException e){
                result.errorInfo = new ErrorInfo(ErrorInfo.ErrorType.parseFailed, e);
                return result;
            }

            result.weeklyPlans.add(plan.trim());
        }

        return result;
    }
}
