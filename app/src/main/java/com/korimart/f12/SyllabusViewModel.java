package com.korimart.f12;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class SyllabusViewModel extends ViewModel {
    public static final String url = "https://wise.uos.ac.kr/uosdoc/uab.UabCoursePlanView.serv";
    public static final String params = "strSchYear=%s&strSmtCd=%s&strCuriNo=%s&strClassNo=%s&" +
            "strCuriNm=%s&strSmtNm=%s&strPgmCd=%s&strViewDiv=%s&&_COMMAND_=list&&_XML_=XML&_strMenuId=stud00180&";

    private MutableLiveData<Boolean> owReady = new MutableLiveData<>();

    private WiseFetcher.Result oFetched;
    private WiseFetcher.Result wFetched;
    private SyllabusOParser.Result oParsed;
    private SyllabusWParser.Result wParsed;

    private WiseFetcher wiseFetcher = WiseFetcher.INSTNACE;
    private SyllabusOParser oParser = SyllabusOParser.INSTANCE;
    private SyllabusWParser wParser = SyllabusWParser.INSTANCE;

    public CompletableFuture<Void> fetchAndParseSyllabus(String schoolYear, String semester,
                                                            String curriNumber, String classNumber){
        return CompletableFuture.runAsync(() -> {
            oFetched = wiseFetcher.fetch(url, buildParams(
                    schoolYear,
                    semester,
                    curriNumber,
                    classNumber,
                    "O"));
            if (oFetched.errorInfo != null) return;

            oParsed = oParser.parse(oFetched.document);
            if (oParsed.errorInfo != null) return;

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

    public SyllabusOParser.Result getoParsed() {
        return oParsed;
    }

    public SyllabusWParser.Result getwParsed() {
        return wParsed;
    }
}
