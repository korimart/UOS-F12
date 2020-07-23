package com.korimart.f12;

import androidx.core.util.Pair;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SyllabusFetchParser {
    private AsyncFetchParser uabOFetchParser
            = new AsyncFetchParser(URLStorage.getSyllabusUabUrl(), null, SyllabusUabOParser.INSTANCE);

    private AsyncFetchParser uabWFetchParser
            = new AsyncFetchParser(URLStorage.getSyllabusUabUrl(), null, SyllabusUabWParser.INSTANCE);

    private AsyncFetchParser oFetchParser
            = new AsyncFetchParser(URLStorage.getSyllabusNonUabUrl(), null, SyllabusOParser.INSTANCE);

    private AsyncFetchParser wFetchParser
            = new AsyncFetchParser(URLStorage.getSyllabusNonUabUrl(), null, SyllabusWParser.INSTANCE);

    private AsyncFetchParser oFP;
    private AsyncFetchParser wFP;

    private AsyncFetchParser.ResultCache<WiseParser.Result> pCache = new AsyncFetchParser.ResultCache<>();
    private boolean uab;
    private String oParams;
    private String wParams;

    public static class Result implements WiseParser.Result {
        String lecPrac;
        String yearLevel;
        String classification;
        String pointsTime;
        String professor;
        String professorDept;
        String professorPhone;
        String professorEmail;
        String professorWeb;
        String counseling;
        String rubricsType;
        List<Pair<String, Integer>> rubrics;
        String summary;
        String textbook;
        List<String> weeklyPlans;

        String fileName;
        String filePath;

        // COVID-19
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

    public CompletableFuture<Void> fetch(boolean refetch){
        oFP.setParams(oParams);
        wFP.setParams(wParams);

        return CompletableFuture.allOf(
                oFP.fetch(refetch),
                wFP.fetch(refetch));
    }

    public CompletableFuture<Void> parse(boolean reparse){
        return CompletableFuture.allOf(
                oFP.parse(reparse),
                wFP.parse(reparse)
        ).thenRun(() -> {
            pCache.data = createResult();
            pCache.resultLiveData.postValue(pCache.data);
        });
    }

    public CompletableFuture<Void> fetchAndParse(boolean refetch) {
        return fetch(refetch).thenCompose(ignored -> parse(refetch));
    }

    public AsyncFetchParser.ResultCache<WiseParser.Result> getpCache() {
        return pCache;
    }

    /**
     * must not set again until fetch or parse is complete
     * @param uab
     */
    public void setMode(boolean uab){
        this.uab = uab;
        if (uab){
            oFP = uabOFetchParser;
            wFP = uabWFetchParser;
        }
        else {
            oFP = oFetchParser;
            wFP = wFetchParser;
        }
    }

    public void setParams(String oParams, String wParams){
        this.oParams = oParams;
        this.wParams = wParams;
    }

    private Result createResult(){
        Result result = new Result();

        SyllabusUabOParser.Result oParsed = (SyllabusUabOParser.Result) oFP.getpCache().data;
        WiseParser.Result wParsed = wFP.getpCache().data;

        result.lecPrac = oParsed.lecPrac;
        result.yearLevel = oParsed.yearLevel;
        result.classification = oParsed.classification;
        result.pointsTime = oParsed.pointsTime;
        result.professor = oParsed.professor;
        result.professorDept = oParsed.professorDept;
        result.professorPhone = oParsed.professorPhone;
        result.professorEmail = oParsed.professorEmail;
        result.professorWeb = oParsed.professorWeb;
        result.counseling = oParsed.counseling;
        result.rubricsType = oParsed.rubricsType;
        result.rubrics = oParsed.rubrics;

        if (uab){
            result.summary = ((SyllabusUabWParser.Result) wParsed).summary;
            result.textbook = ((SyllabusUabWParser.Result) wParsed).textbook;
            result.weeklyPlans = ((SyllabusUabWParser.Result) wParsed).weeklyPlans;
            result.fileName = "";
            result.filePath = "";
            result.offlineRate = "";
            result.onlineRate = "";
            result.midtermOnlineCode = "";
            result.finalOnlineCode = "";
            result.quizOnlineCode = "";
        }
        else {
            result.summary = ((SyllabusOParser.Result) oParsed).summary;
            result.textbook = ((SyllabusOParser.Result) oParsed).textbook;
            result.weeklyPlans = ((SyllabusWParser.Result) wParsed).weeklyPlans;
            result.fileName = ((SyllabusOParser.Result) oParsed).fileName;
            result.filePath = ((SyllabusOParser.Result) oParsed).filePath;
            result.offlineRate = ((SyllabusOParser.Result) oParsed).offlineRate;
            result.onlineRate = ((SyllabusOParser.Result) oParsed).onlineRate;
            result.midtermOnlineCode = ((SyllabusOParser.Result) oParsed).midtermOnlineCode;
            result.finalOnlineCode = ((SyllabusOParser.Result) oParsed).finalOnlineCode;
            result.quizOnlineCode = ((SyllabusOParser.Result) oParsed).quizOnlineCode;
            result.quizOnlineCode2 = ((SyllabusOParser.Result) oParsed).quizOnlineCode2;
        }

        if (wParsed.getErrorInfo() != null)
            result.errorInfo = wParsed.getErrorInfo();
        if (oParsed.getErrorInfo() != null)
            result.errorInfo = oParsed.getErrorInfo();

        return result;
    }
}
