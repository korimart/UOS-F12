package com.korimart.f12;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class WiseViewModel extends ViewModel {
    private AsyncFetchParser f12InfoFetchParser
            = new AsyncFetchParser(URLStorage.getF12URL(), URLStorage.getF12InfoParams(), F12InfoParser.INSTANCE);

    private AsyncFetchParser schoolListFetchParser
            = new AsyncFetchParser(URLStorage.getSchoolListUrl(), URLStorage.getSchoolListParams(), SchoolListParser.INSTANCE);

    private AsyncFetchParser personalInfoFetchParser
            = new AsyncFetchParser(URLStorage.getPersInfoUrl(), URLStorage.getPersInfoParams(), PersonalInfoParser.INSTANCE);

    private F12FetchParser f12FetchParser = new F12FetchParser(f12InfoFetchParser);

    private CourseListFetchParser majorListFetchParser
            = new CourseListFetchParser(f12InfoFetchParser, schoolListFetchParser);

    private AsyncFetchParser coreListFetchParserRequired
            = new AsyncFetchParser(URLStorage.getCoreListUrl(), null, CourseListParser.INSTANCE);

    private AsyncFetchParser coreListFetchParser
            = new AsyncFetchParser(URLStorage.getCoreListUrl(), null, CourseListParser.INSTANCE);

    private MutableLiveData<WiseParser.Result> coreListLiveData = new MutableLiveData<>();

    private SyllabusFetchParser syllabusFetchParser = new SyllabusFetchParser();

    public CompletableFuture<Void> fetchAndParseF12(boolean refetch, boolean noPnp){
        f12FetchParser.setNoPnp(noPnp);
        return f12FetchParser.fetchAndParse(refetch);
    }

    public CompletableFuture<Void> fetchAndParseMajors(boolean refetch){
        majorListFetchParser.setFetchMine(true);
        majorListFetchParser.setRefetchF12Info(false);
        majorListFetchParser.setRefetchSchoolList(false);
        return majorListFetchParser.fetchAndParse(refetch);
    }

    public CompletableFuture<Void> fetchAndParseMajors(boolean refetch, int schoolYear,
                                                       String semester, String schoolCode,
                                                       String deptCode){
        majorListFetchParser.setFetchMine(false);
        majorListFetchParser.setRefetchF12Info(false);
        majorListFetchParser.setRefetchSchoolList(false);
        majorListFetchParser
                .setParams(URLStorage.getCourseListParam(schoolYear, semester, schoolCode, deptCode));
        return majorListFetchParser.fetchAndParse(refetch);
    }

    public CompletableFuture<Void> fetchAndParseLatestCores(boolean refetch){
        return schoolListFetchParser
                .fetchAndParse(false)
                .thenCompose(ignored -> {
                    SchoolListParser.Result info = (SchoolListParser.Result) schoolListFetchParser.getpCache().data;
                    coreListFetchParserRequired
                            .setParams(URLStorage.getCoreListParam(info.latestSchoolYear, info.latestSemester, true));
                    coreListFetchParser
                            .setParams(URLStorage.getCoreListParam(info.latestSchoolYear, info.latestSemester, false));
                    return CompletableFuture.allOf(
                            coreListFetchParserRequired.fetchAndParse(refetch),
                            coreListFetchParser.fetchAndParse(refetch)
                    );
                })
                .thenRun(this::updateCoreList);
    }

    public CompletableFuture<Void> fetchAndParseCores(boolean refetch, int schoolYear, String semester){
        coreListFetchParserRequired
                .setParams(URLStorage.getCoreListParam(schoolYear, semester, true));
        coreListFetchParser
                .setParams(URLStorage.getCoreListParam(schoolYear, semester, false));

        return CompletableFuture.allOf(
                coreListFetchParserRequired.fetchAndParse(refetch),
                coreListFetchParser.fetchAndParse(refetch))
                .thenRun(this::updateCoreList);
    }

    public CompletableFuture<Void> fetchAndParsePersonalInfo(boolean refetch){
        return personalInfoFetchParser.fetchAndParse(refetch);
    }

    public CompletableFuture<Void> fetchAndParseSyllabus(boolean refetch,
                                                         String schoolYear, String semester,
                                                         String curriNumber, String classNumber,
                                                         boolean uab, String certDivCode){
        /**
         *          below is from Wise javascript
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

        String oParams = URLStorage.getSyllabusParams(schoolYear, semester, curriNumber, classNumber, "O");
        String wParams = URLStorage.getSyllabusParams(schoolYear, semester, curriNumber, classNumber, "W");
        syllabusFetchParser.setParams(oParams, wParams);
        syllabusFetchParser.setMode((semester.equals("10") || semester.equals("20")) && (uab || certDivCode.equals("01")));
        return syllabusFetchParser.fetchAndParse(refetch);
    }

    public void recalculateHiddenAvg(boolean noPnp){
        WiseFetcher.Result fetched = f12FetchParser.getfCache().resultLiveData.getValue();
        F12Parser.Result parsed = (F12Parser.Result) f12FetchParser.getpCache().resultLiveData.getValue();

        if (fetched == null || parsed == null)
            return;

        f12FetchParser.setNoPnp(noPnp);
        parsed.hiddenAvg = ((F12Parser) f12FetchParser.getWiseParser())
                .recalculateHiddenAvg(fetched.response);
        f12FetchParser.getpCache().resultLiveData.setValue(parsed);
    }

    /**
     * call from background thread
     */
    private void updateCoreList(){
        CourseListParser.Result notRequired =
                (CourseListParser.Result) coreListFetchParser.getpCache().data;
        CourseListParser.Result required =
                (CourseListParser.Result) coreListFetchParserRequired.getpCache().data;
        CourseListParser.Result joined = new CourseListParser.Result();
        joined.courseInfos = new ArrayList<>();
        joined.courseInfos.addAll(notRequired.courseInfos);
        joined.courseInfos.addAll(required.courseInfos);
        coreListLiveData.postValue(joined);
    }

    public LiveData<WiseParser.Result> getF12(){
        return f12FetchParser.getpCache().resultLiveData;
    }

    public LiveData<WiseParser.Result> getF12Info() {
        return f12InfoFetchParser.getpCache().resultLiveData;
    }

    public LiveData<WiseParser.Result> getSchoolList() {
        return schoolListFetchParser.getpCache().resultLiveData;
    }

    public LiveData<WiseParser.Result> getPersonalInfo(){
        return personalInfoFetchParser.getpCache().resultLiveData;
    }

    public LiveData<WiseParser.Result> getMajorList() {
        return majorListFetchParser.getpCache().resultLiveData;
    }

    public LiveData<WiseParser.Result> getCoreList() {
        return coreListLiveData;
    }

    public LiveData<WiseParser.Result> getSyllabus(){
        return syllabusFetchParser.getpCache().resultLiveData;
    }

    public void clearSyllabus(){
        syllabusFetchParser.getpCache().resultLiveData.setValue(null);
        syllabusFetchParser.getpCache().data = null;
    }
}
