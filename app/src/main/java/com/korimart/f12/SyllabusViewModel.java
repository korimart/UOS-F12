package com.korimart.f12;

import androidx.lifecycle.ViewModel;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class SyllabusViewModel extends ViewModel {
    public static final String url = "https://wise.uos.ac.kr/uosdoc/uab.UabCoursePlanView.serv";
    public static final String params = "strSchYear=%s&strSmtCd=%s&strCuriNo=%s&strClassNo=%s&" +
            "strCuriNm=%s&strSmtNm=%s&strPgmCd=%s&strViewDiv=%s&&_COMMAND_=list&&_XML_=XML&_strMenuId=stud00180&";

    private WiseFetcher wiseFetcher = WiseFetcher.INSTNACE;

    public CompletableFuture<Void> fetchAndParseSyllabus(String schoolYear, String semester,
                                                            String curriNumber, String classNumber){
        return CompletableFuture.runAsync(() -> {
            WiseFetcher.Result Ofetched = wiseFetcher.fetch(url, buildParams(
                    schoolYear,
                    semester,
                    curriNumber,
                    classNumber,
                    "O"));

            if (Ofetched.errorInfo != null) return;

            WiseFetcher.Result Wfetched = wiseFetcher.fetch(url, buildParams(
                    schoolYear,
                    semester,
                    curriNumber,
                    classNumber,
                    "W"));

            if (Wfetched.errorInfo != null) return;
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
}
