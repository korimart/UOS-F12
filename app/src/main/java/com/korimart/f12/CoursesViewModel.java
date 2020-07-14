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
    private MutableLiveData<Integer> schoolYearSelection = new MutableLiveData<>();
    private MutableLiveData<Integer> semesterSelection = new MutableLiveData<>();
    private MutableLiveData<Integer> schoolSelection = new MutableLiveData<>();
    private MutableLiveData<Integer> departmentSelection = new MutableLiveData<>();
    private MutableLiveData<Boolean> freshman = new MutableLiveData<>();
    private MutableLiveData<Boolean> sophomore = new MutableLiveData<>();
    private MutableLiveData<Boolean> junior = new MutableLiveData<>();
    private MutableLiveData<Boolean> senior = new MutableLiveData<>();
    private MutableLiveData<Boolean> shouldFetchCourses = new MutableLiveData<>();
    private MutableLiveData<Boolean> shouldApplyFilter = new MutableLiveData<>();
    private MutableLiveData<String> systemMessgae = new MutableLiveData<>();
    private MutableLiveData<Boolean> allFetchedAndParsed = new MutableLiveData();

    private MutableLiveData<WiseFetcher.Result> schoolListFetched = new MutableLiveData<>();
    private MutableLiveData<SchoolListParser.Result> schoolListParsed = new MutableLiveData<>();
    private MutableLiveData<WiseFetcher.Result> personalInfoFetched = new MutableLiveData<>();
    private MutableLiveData<PersonalInfoParser.Result> personalInfoParsed = new MutableLiveData<>();
    private MutableLiveData<WiseFetcher.Result> courseListFetched = new MutableLiveData<>();
    private MutableLiveData<CourseListParser.Result> courseListParsed = new MutableLiveData<>();

    private CompletableFuture<Void> schoolListFuture;
    private CompletableFuture<Void> personalInfoFuture;
    private CompletableFuture<Void> coursesFuture;

    private WiseHelper wiseHelper = WiseHelper.INSTANCE;
    private WiseFetcher wiseFetcher = WiseFetcher.INSTNACE;
    private SchoolListParser schoolListParser = SchoolListParser.INSTANCE;
    private PersonalInfoParser personalInfoParser = PersonalInfoParser.INSTANCE;
    private CourseListParser courseListParser = CourseListParser.INSTANCE;

    public CoursesViewModel(){
        shouldFetchCourses.setValue(false);
        shouldApplyFilter.setValue(false);
        freshman.setValue(false);
        sophomore.setValue(false);
        junior.setValue(false);
        senior.setValue(false);
    }

    public CompletableFuture<Void> fetchAndParseSchoolList(){
        if (schoolListFuture == null){
            schoolListFuture = CompletableFuture.runAsync(() -> {
                SchoolListParser.Result schoolListParsed = new SchoolListParser.Result();

                wiseHelper.fetchAndParse(
                        schoolListUrl, schoolListParams, wiseFetcher, this.schoolListFetched,
                        schoolListParser, this.schoolListParsed, schoolListParsed);
            });
        }

        return schoolListFuture;
    }

    public CompletableFuture<Void> fetchAndParsePersInfo(){
        if (personalInfoFuture == null){
            personalInfoFuture = CompletableFuture.runAsync(() -> {
                PersonalInfoParser.Result persInfoParsed = new PersonalInfoParser.Result();

                wiseHelper.fetchAndParse(
                        persInfoUrl, persInfoParams, wiseFetcher, this.personalInfoFetched,
                        personalInfoParser, this.personalInfoParsed, persInfoParsed);
            });
        }

        return personalInfoFuture;
    }

    public CompletableFuture<Void> fetchCourses(boolean refetch){
        if (coursesFuture == null || refetch){
            getFilteredCourses().setValue(null);
            coursesFuture = CompletableFuture.runAsync(() -> {
                CourseListParser.Result courseListParsed = new CourseListParser.Result();
                String formattedParams = String.format(
                        Locale.US,
                        courseListParam,
                        Integer.parseInt(schoolYears.getValue().get(schoolYearSelection.getValue())),
                        getSemesterCode(semesterSelection.getValue()),
                        schools.getValue().get(schoolSelection.getValue()).s2,
                        departments.getValue().get(departmentSelection.getValue()).s2);

                if (!wiseHelper.fetchAndParse(
                        courseListUrl, formattedParams, wiseFetcher, this.courseListFetched,
                        courseListParser, this.courseListParsed, courseListParsed))
                    return;

                List<CourseListParser.CourseInfo> filteredCourses = new ArrayList<>(courseListParsed.courseInfos);
                filterCourses(filteredCourses);
                this.filteredCourses.postValue(filteredCourses);
            });
        }

        return coursesFuture;
    }

    public void applyFilter() {
        CourseListParser.Result courses = courseListParsed.getValue();
        if (courses == null) return;

        List<CourseListParser.CourseInfo> filteredCourses = new ArrayList<>(courses.courseInfos);
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

    public MutableLiveData<SchoolListParser.Result> getSchoolListParsed() {
        return schoolListParsed;
    }

    public MutableLiveData<PersonalInfoParser.Result> getPersonalInfoParsed() {
        return personalInfoParsed;
    }

    public MutableLiveData<CourseListParser.Result> getCourseListParsed() {
        return courseListParsed;
    }

    public MutableLiveData<Integer> getSchoolYearSelection() {
        return schoolYearSelection;
    }

    public MutableLiveData<Integer> getSemesterSelection() {
        return semesterSelection;
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

    public MutableLiveData<Integer> getDepartmentSelection() {
        return departmentSelection;
    }

    public MutableLiveData<Integer> getSchoolSelection() {
        return schoolSelection;
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

    public MutableLiveData<Boolean> getShouldFetchCourses() {
        return shouldFetchCourses;
    }

    public MutableLiveData<List<CourseListParser.CourseInfo>> getFilteredCourses() {
        return filteredCourses;
    }

    public MutableLiveData<Boolean> getShouldApplyFilter() {
        return shouldApplyFilter;
    }

    public MutableLiveData<String> getSystemMessgae() {
        return systemMessgae;
    }

    public MutableLiveData<WiseFetcher.Result> getSchoolListFetched() {
        return schoolListFetched;
    }

    public MutableLiveData<WiseFetcher.Result> getPersonalInfoFetched() {
        return personalInfoFetched;
    }

    public MutableLiveData<WiseFetcher.Result> getCourseListFetched() {
        return courseListFetched;
    }

    public MutableLiveData<Boolean> getAllFetchedAndParsed() {
        return allFetchedAndParsed;
    }
}
