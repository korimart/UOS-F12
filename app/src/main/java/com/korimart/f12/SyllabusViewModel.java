package com.korimart.f12;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class SyllabusViewModel extends ViewModel {
    public static final String uabUrl = "https://wise.uos.ac.kr/uosdoc/uab.UabCoursePlanView.serv";
    public static final String nonUabUrl = "https://wise.uos.ac.kr/uosdoc/ucs.UcsCoursePlanViewPopup.serv";
    public static final String params = "strSchYear=%s&strSmtCd=%s&strCuriNo=%s&strClassNo=%s&" +
            "strCuriNm=%s&strSmtNm=%s&strPgmCd=%s&strViewDiv=%s&&_COMMAND_=list&&_XML_=XML&_strMenuId=stud00180&";

    private MutableLiveData<Boolean> owReady = new MutableLiveData<>();

    private WiseFetcher.Result oFetched;
    private WiseFetcher.Result wFetched;
    private WiseParser.Result oParsed;
    private WiseParser.Result wParsed;

    private WiseFetcher wiseFetcher = WiseFetcher.INSTANCE;
    private SyllabusUabOParser uabOParser = SyllabusUabOParser.INSTANCE;
    private SyllabusUabWParser uabWParser = SyllabusUabWParser.INSTANCE;
    private SyllabusOParser nonUabOParser = SyllabusOParser.INSTANCE;
    private SyllabusWParser nonUabWParser = SyllabusWParser.INSTANCE;

    public CompletableFuture<Void> fetchAndParseSyllabus(String schoolYear, String semester,
                                                        String curriNumber, String classNumber,
                                                        boolean uab, String certDivCode){
        /**
         *          below is from wise javascript
         *
         *
         *         //공학인증 수업계획서
         *         //정규학기 & 공학인증프로그램등록 or 인증구분이 공학인증
         *         if ((varSmtCd == "10" || varSmtCd == "20") && (varUabYn == "Y" || varCertDivCd == "01")) {
         *             //개요
         *             if (varA == "1") {
         *                 varOpenFile = "../uab/UabCoursePlanView.ztm";
         *                 //주별계획
         *             } else if (varA == "2") {
         *                 varOpenFile = "../uab/UabCoursePlanWeekView.ztm";
         *                 //설계계획서
         *             } else if (varA == "d") {
         *                 if (model.get(varPath + "[" + varNowIndex + "]/d_pnt") != "") {
         *                     varOpenFile = "./UcrUabWriteDrawPlanPopUp.ztm";
         *                 } else {
         *                     model.msgbox("이 교과목은 설계교과목이 아닙니다.", "");
         *                     return;
         *                 }
         *             }
         *         } else {
         *             if (Number(varSchYear + varSmtCd) >= 201520 && varGradDivCd == "20000") {
         *                 //개요
         *                 if (varA == "1") {
         *                     varOpenFile = "../ucs/UcsCoursePlanViewPopup.ztm";
         *                     //주별계획
         *                 } else {
         *                     varOpenFile = "../ucs/UcsCoursePlanWeekViewPopup.ztm";
         *                 }
         *             }//자체인증 수업계획서
         *             else if (varCertDivCd == "03" && Number(varSchYear + varSmtCd) >= 201320) {
         *                 //개요
         *                 if (varA == "1") {
         *                     varOpenFile = "../uas/UasCoursePlanView.ztm";
         *
         *                     //주별계획
         *                 } else {
         *                     varOpenFile = "../uas/UasCoursePlanWeekView.ztm";
         *                 }
         *                 //일반 수업계획서
         *             } else {
         *                 varOpenFile = "./UcrCoursePlanView.ztm";
         *             }
         *
         */

        String url;
        WiseParser oParser;
        WiseParser wParser;
        if ((semester.equals("10") || semester.equals("20")) && (uab || certDivCode.equals("01"))){
            url = uabUrl;
            oParser = uabOParser;
            wParser = uabWParser;
        }
        else {
            url = nonUabUrl;
            oParser = nonUabOParser;
            wParser = nonUabWParser;
        }

        return CompletableFuture.runAsync(() -> {
            oFetched = wiseFetcher.fetch(url, buildParams(
                    schoolYear,
                    semester,
                    curriNumber,
                    classNumber,
                    "O"));
            if (oFetched.errorInfo != null) return;

            oParsed = oParser.parse(oFetched.document);
            if (oParsed.getErrorInfo() != null) return;

            wFetched = wiseFetcher.fetch(url, buildParams(
                    schoolYear,
                    semester,
                    curriNumber,
                    classNumber,
                    "W"));
            if (wFetched.errorInfo != null) return;

            wParsed = wParser.parse(wFetched.document);
        });
    }

    private String buildParams(String schoolYear, String semester, String curriNumber,
                               String classNumber, String division){
        return String.format(
                Locale.getDefault(),
                params,
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

    public MutableLiveData<Boolean> getOwReady() {
        return owReady;
    }

    public WiseFetcher.Result getoFetched() {
        return oFetched;
    }

    public WiseFetcher.Result getwFetched() {
        return wFetched;
    }

    public WiseParser.Result getoParsed() {
        return oParsed;
    }

    public WiseParser.Result getwParsed() {
        return wParsed;
    }
}
