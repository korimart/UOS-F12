package com.korimart.f12;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class CoursesViewModel extends ViewModel {
    public static final String schoolListUrl = "https://wise.uos.ac.kr/uosdoc/ucr.UcrMjTimeInq.do";
    public static final String schoolListParams = "_code_smtList=CMN31&_code_cmpList=UCS12&&_COMMAND_=onload" +
            "&&_XML_=XML&_strMenuId=stud00180&";

    public static final String persInfoUrl = "https://wise.uos.ac.kr/uosdoc/usr.UsrMasterInq.do";
    public static final String persInfoParams = "strStudId=123123&_user_info=userid&&_COMMAND_=list&&_XML_=XML&_strMenuId=stud00040&";

    public static final String courseListUrl = "https://wise.uos.ac.kr/uosdoc/ucr.UcrMjTimeInq.do";
    public static final String courseListParam = "strSchYear=%d&strSmtCd=%s&strUnivCd=%s&" +
            "strSustCd=%s&strCmpDivCd=&strCuriNo=&strClassNo=&strCuriNm=&strGradDivCd=20000&" +
            "strEtcYn=&&_COMMAND_=list&&_XML_=XML&_strMenuId=stud00180&";

    private MutableLiveData<List<CourseListParser.CourseInfo>> filteredCourses = new MutableLiveData<>();
    private MutableLiveData<List<StringPair>> schools = new MutableLiveData<>();
    private MutableLiveData<List<StringPair>> departments = new MutableLiveData<>();
    private MutableLiveData<List<String>> schoolYears = new MutableLiveData<>();
    private MutableLiveData<Boolean> freshman = new MutableLiveData<>();
    private MutableLiveData<Boolean> sophomore = new MutableLiveData<>();
    private MutableLiveData<Boolean> junior = new MutableLiveData<>();
    private MutableLiveData<Boolean> senior = new MutableLiveData<>();
    private MutableLiveData<Boolean> courseListReady = new MutableLiveData<>();
    private MutableLiveData<Boolean> courseListFetchReady = new MutableLiveData();

    private boolean isInitialized = false;
    private boolean shouldFetchCourses = false;
    private WiseFetcher.Result schoolListFetched;
    private SchoolListParser.Result schoolListParsed;
    private WiseFetcher.Result personalInfoFetched;
    private PersonalInfoParser.Result personalInfoParsed;
    private WiseFetcher.Result courseListFetched;
    private CourseListParser.Result courseListParsed;
    private int[] selections = new int[4];

    private CompletableFuture<Void> schoolListFuture;
    private CompletableFuture<Void> personalInfoFuture;
    private CompletableFuture<Void> coursesFuture;
    private CompletableFuture<Void> prepareFuture;

    private WiseFetcher wiseFetcher = WiseFetcher.INSTNACE;
    private SchoolListParser schoolListParser = SchoolListParser.INSTANCE;
    private PersonalInfoParser personalInfoParser = PersonalInfoParser.INSTANCE;
    private CourseListParser courseListParser = CourseListParser.INSTANCE;

    public CoursesViewModel(){
        courseListReady.setValue(false);
        freshman.setValue(false);
        sophomore.setValue(false);
        junior.setValue(false);
        senior.setValue(false);
        courseListFetchReady.setValue(false);
    }

    public CompletableFuture<Void> prepare(CompletableFuture<Void> f12, boolean refetch){
        if (refetch || prepareFuture == null){
            if (refetch)
                setShouldFetchCourses(refetch);

            prepareFuture = CompletableFuture.allOf(
                    fetchAndParsePersInfo(refetch),
                    f12,
                    fetchAndParseSchoolList(refetch));
        }

        return prepareFuture;
    }

    public CompletableFuture<Void> fetchAndParseSchoolList(boolean refetch){
        if (refetch || schoolListFuture == null){
            schoolListFuture = CompletableFuture.runAsync(() -> {
                schoolListFetched = wiseFetcher.fetch(schoolListUrl, schoolListParams);
                if (schoolListFetched.errorInfo != null) return;
                schoolListParsed = schoolListParser.parse(schoolListFetched.document);
            });
        }

        return schoolListFuture;
    }

    public CompletableFuture<Void> fetchAndParsePersInfo(boolean refetch){
        if (refetch || personalInfoFuture == null){
            personalInfoFuture = CompletableFuture.runAsync(() -> {
                personalInfoFetched = wiseFetcher.fetch(persInfoUrl, persInfoParams);
                if (personalInfoFetched.errorInfo != null) return;
                personalInfoParsed = personalInfoParser.parse(personalInfoFetched.document);
            });
        }

        return personalInfoFuture;
    }

    public CompletableFuture<Void> fetchAndParseCourses(boolean refetch){
        if (coursesFuture == null || refetch){
            coursesFuture = CompletableFuture.runAsync(() -> {
                String formattedParams = String.format(
                        Locale.US,
                        courseListParam,
                        Integer.parseInt(schoolYears.getValue().get(selections[0])),
                        getSemesterCode(selections[1]),
                        schools.getValue().get(selections[2]).s2,
                        departments.getValue().get(selections[3]).s2);

                courseListFetched = wiseFetcher.fetch(courseListUrl, formattedParams);
                if (courseListFetched.errorInfo != null) return;
                courseListParsed = courseListParser.parse(courseListFetched.document);

                List<CourseListParser.CourseInfo> filteredCourses = new ArrayList<>(courseListParsed.courseInfos);
                filterCourses(filteredCourses);
                this.filteredCourses.postValue(filteredCourses);
            });
        }

        return coursesFuture;
    }

    public void applyFilter() {
        List<CourseListParser.CourseInfo> filteredCourses = new ArrayList<>(courseListParsed.courseInfos);
        filterCourses(filteredCourses);
        this.filteredCourses.setValue(filteredCourses);
    }

    private void filterCourses(List<CourseListParser.CourseInfo> rawCoursesCopy) {
        boolean[] yearLevels = {
                freshman.getValue(),
                sophomore.getValue(),
                junior.getValue(),
                senior.getValue()};

        rawCoursesCopy.removeIf(courseInfo -> {
            for (int i = 0; i < yearLevels.length; i++){
                if (yearLevels[i]){
                    if (courseInfo.yearLevel.contains(String.valueOf(i + 1)))
                        return false;
                }
            }
            return true;
        });

        Collections.sort(rawCoursesCopy, (o1, o2) -> o1.name.compareTo(o2.name));
    }

    private String getSemesterCode(Integer value) {
        switch (value){
            case 0:
                return "10";
            case 1:
                return "20";
            case 2:
                return "11";
        }
        return null;
    }

    /**
     * This is "set"-Departments not "post"-Departments
     * @param departments
     */
    public void setDepartments(List<SchoolListParser.DeptInfo> departments){
        List<StringPair> deptStrings = new ArrayList<>();
        departments.forEach((info) -> deptStrings.add(new StringPair(info.name, info.code)));
        Collections.sort(deptStrings, (o1, o2) -> o1.s1.compareTo(o2.s1));
        getDepartments().setValue(deptStrings);
    }

    public MutableLiveData<List<StringPair>> getSchools() {
        return schools;
    }

    public MutableLiveData<List<String>> getSchoolYears() {
        return schoolYears;
    }

    public MutableLiveData<List<StringPair>> getDepartments() {
        return departments;
    }

    public MutableLiveData<Boolean> getFreshman() {
        return freshman;
    }

    public MutableLiveData<Boolean> getSophomore() {
        return sophomore;
    }

    public MutableLiveData<Boolean> getJunior() {
        return junior;
    }

    public MutableLiveData<Boolean> getSenior() {
        return senior;
    }

    public MutableLiveData<List<CourseListParser.CourseInfo>> getFilteredCourses() {
        return filteredCourses;
    }

    public MutableLiveData<Boolean> getCourseListReady() {
        return courseListReady;
    }

    public MutableLiveData<Boolean> getCourseListFetchReady() {
        return courseListFetchReady;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public void setInitialized(boolean initialized) {
        isInitialized = initialized;
    }

    public WiseFetcher.Result getSchoolListFetched() {
        return schoolListFetched;
    }

    public SchoolListParser.Result getSchoolListParsed() {
        return schoolListParsed;
    }

    public WiseFetcher.Result getPersonalInfoFetched() {
        return personalInfoFetched;
    }

    public PersonalInfoParser.Result getPersonalInfoParsed() {
        return personalInfoParsed;
    }

    public WiseFetcher.Result getCourseListFetched() {
        return courseListFetched;
    }

    public CourseListParser.Result getCourseListParsed() {
        return courseListParsed;
    }

    public boolean isShouldFetchCourses() {
        return shouldFetchCourses;
    }

    public void setShouldFetchCourses(boolean shouldFetchCourses) {
        this.shouldFetchCourses = shouldFetchCourses;
    }

    public int[] getSelections() {
        return selections;
    }

    public void setSelections(int index, int selection) {
        this.selections[index] = selection;
    }
}
