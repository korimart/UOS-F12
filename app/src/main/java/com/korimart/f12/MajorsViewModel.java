package com.korimart.f12;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;

public class MajorsViewModel extends ViewModel implements CourseListViewModel {
    private CourseListCommon commons = new CourseListCommon();
    private MutableLiveData<List<StringPair>> schools = new MutableLiveData<>();
    private MutableLiveData<List<StringPair>> departments = new MutableLiveData<>();
    private MutableLiveData<int[]> selections = new MutableLiveData<>();

    private int mySchoolYear;
    private String mySemester;
    private String mySchoolCode;
    private String myDeptCode;
    private int myYearLevel;
    private boolean departmentNotFound;

    private ErrorReporter errorReporter = ErrorReporter.INSTANCE;

    public MajorsViewModel(){
        selections.setValue(new int[4]);
    }

    @Override
    public void onViewCreated(WiseViewModel wiseViewModel, MainActivity mainActivity) {
        if (!commons.firstOpen) return;

        commons.firstOpen = false;
        fetchFilterAndFromFilter(wiseViewModel, mainActivity);
    }

    @Override
    public void refresh(WiseViewModel wiseViewModel, MainActivity mainActivity) {
        fetchFilterAndFromFilter(wiseViewModel, mainActivity);
    }

    @Override
    public LiveData<List<CourseListParser.CourseInfo>> getFilteredCourses() {
        return commons.filteredCourses;
    }

    @Override
    public LiveData<String> getTitle() {
        return commons.title;
    }

    @Override
    public LiveData<String> getSystemMessage() {
        return commons.systemMessage;
    }

    @Override
    public void applyFilterOnName(WiseViewModel wiseViewModel, String text) {
        commons.filterText = text;
        applyFilter(wiseViewModel);
    }

    @Override
    public String getFilterText() {
        return commons.filterText;
    }

    public void fetchFilterAndFromFilter(WiseViewModel wiseViewModel, MainActivity mainActivity){
        commons.filteredCourses.setValue(null);
        commons.systemMessage.setValue("가져오는 중...");

        CompletableFuture<Void> future;

        SharedPreferences prefs = mainActivity.getPreferences(Context.MODE_PRIVATE);
        int schoolYear = prefs.getInt("savedSchoolYear", 0);
        String semester = prefs.getString("savedSemester", null);
        String schoolCode = prefs.getString("savedSchoolCode", null);
        String deptCode = prefs.getString("savedDeptCode", null);
        boolean[] yearLevels = new boolean[4];

        if (schoolYear != 0 && semester != null && schoolCode != null && deptCode != null){
            yearLevels[0] = prefs.getBoolean("savedYear1", false);
            yearLevels[1] = prefs.getBoolean("savedYear2", false);
            yearLevels[2] = prefs.getBoolean("savedYear3", false);
            yearLevels[3] = prefs.getBoolean("savedYear4", false);

            future = wiseViewModel.fetchAndParseMajors(true, schoolYear, semester, schoolCode, deptCode);
        }
        else {
            schoolCode = null;
            future = wiseViewModel.fetchAndParseMajors(true);
        }

        String finalSchoolCode = schoolCode;
        future.thenCompose(ignored -> wiseViewModel.fetchAndParsePersonalInfo(false))
                .thenRun(() -> onFetchAndParseReady(wiseViewModel, mainActivity, finalSchoolCode, deptCode, yearLevels))
                .exceptionally(throwable -> {
                    errorReporter.backgroundErrorHandler(throwable, errorInfo -> this.onError(errorInfo, mainActivity));
                    return null;
                });
    }

    public void fetchFromFilter(WiseViewModel wiseViewModel, MainActivity mainActivity) {
        commons.filteredCourses.setValue(null);
        commons.systemMessage.setValue("가져오는 중...");

        List<String> schoolYears = commons.schoolYears.getValue();
        List<StringPair> schools = this.schools.getValue();
        List<StringPair> depts = departments.getValue();

        if (schoolYears == null || schools == null || depts == null){
            ErrorInfo errorInfo = new ErrorInfo(new Exception("filter was not set but fetchFromFilter was called"));
            ErrorReporter.INSTANCE.reportError(errorInfo.throwable);
            this.onError(errorInfo, mainActivity);
            return;
        }

        int[] selections = this.selections.getValue();

        int schoolYear = Integer.parseInt(schoolYears.get(selections[0]));
        String semester = getSemesterString(selections[1]);
        String schoolCode = schools.get(selections[2]).s2;
        String deptCode = depts.get(selections[3]).s2;

        wiseViewModel
                .fetchAndParseMajors(true, schoolYear, semester, schoolCode, deptCode)
                .thenRun(() -> {
                    commons.handler.post(() -> applyFilter(wiseViewModel));
                })
                .exceptionally(t -> {
                    errorReporter.backgroundErrorHandler(t, onError -> this.onError(onError, mainActivity));
                    return null;
                });
    }

    /**
     * should be called from UI thread
     */
    public void applyFilter(WiseViewModel wiseViewModel) {
        CourseListParser.Result
                r = (CourseListParser.Result) wiseViewModel.getMajorList().getValue();
        List<CourseListParser.CourseInfo> filtered = commons.applyFilterOnYearLevels(r.courseInfos);

        filtered.removeIf(courseInfo -> !courseInfo.name.contains(commons.filterText));

        if (filtered.isEmpty())
            commons.systemMessage.setValue("검색 결과가 없습니다.");
        else
            commons.systemMessage.setValue("");

        commons.filteredCourses.setValue(filtered);
    }

    private void onError(ErrorInfo errorInfo, MainActivity mainActivity) {
        switch (errorInfo.type){
            case sessionExpired:
                mainActivity.goToLoginFrag(1);
                break;

            case timeout:
            case responseFailed:
                commons.systemMessage.setValue("불러오기 실패");
                break;

            case departmentNotFound:
                commons.title.setValue("기본 필터 가져오기 실패");
                commons.systemMessage.setValue("기본 필터를 가져오는데 실패했습니다.\n학부생이 아니신가요?\n\n(어찌됐건 직접 필터를 설정하여\n검색할 수 있습니다)");
                break;

            default:
                mainActivity.goToErrorFrag(errorInfo.throwable);
                break;
        }
    }

    private void onFetchAndParseReady(WiseViewModel wiseViewModel, MainActivity mainActivity,
                                      String schoolCode, String deptCode, boolean[] yearLevels){
        commons.handler.post(() -> {
            F12InfoParser.Result f12Info =
                    (F12InfoParser.Result) wiseViewModel.getF12Info().getValue();

            SchoolListParser.Result schoolList =
                    (SchoolListParser.Result) wiseViewModel.getSchoolList().getValue();

            PersonalInfoParser.Result personalInfo =
                    (PersonalInfoParser.Result) wiseViewModel.getPersonalInfo().getValue();

            mySchoolYear = schoolList.latestSchoolYear;
            mySemester = schoolList.latestSemester;
            mySchoolCode = f12Info.schoolCode;
            myDeptCode = f12Info.deptCode;
            myYearLevel = personalInfo.yearLevel;

            String filterSchoolCode;
            String filterDeptCode;

            if (schoolCode != null){
                filterSchoolCode = schoolCode;
                filterDeptCode = deptCode;
            }
            else {
                filterSchoolCode = mySchoolCode;
                filterDeptCode = myDeptCode;
                yearLevels[myYearLevel - 1] = true;
            }

            departmentNotFound =
                    setUpInitialFilter(filterSchoolCode, filterDeptCode, schoolList);

            FilterOptions options = commons.filterOptions.getValue();
            options.yearLevels = yearLevels;
            commons.filterOptions.setValue(options);

            if (departmentNotFound){
                onError(new ErrorInfo(ErrorInfo.ErrorType.departmentNotFound), mainActivity);
            }
            else {
                setTitleFromFilter();
                applyFilter(wiseViewModel);
            }
        });
    }

    /**
     * should be called from UI thread
     *
     * @param schoolCode my school code
     * @param deptCode my department code
     * @param schoolResult school list parsed
     * @return departmentNotFound
     */
    private boolean setUpInitialFilter(String schoolCode, String deptCode,
                                       @NonNull SchoolListParser.Result schoolResult) {
        ArrayList<String> schoolYears = new ArrayList<>();
        for (int i = schoolResult.latestSchoolYear; i > schoolResult.latestSchoolYear - 10; i--)
            schoolYears.add(String.valueOf(i));
        commons.schoolYears.setValue(schoolYears);

        ArrayList<StringPair> al = new ArrayList<>();
        schoolResult.schoolToDepts.keySet().forEach((info) -> al.add(new StringPair(info.name, info.code)));
        Collections.sort(al, (o1, o2) -> o1.s1.compareTo(o2.s1));
        schools.setValue(al);

        List<SchoolListParser.DeptInfo> defaultDepartments = null;

        for (Map.Entry<SchoolListParser.DeptInfo, List<SchoolListParser.DeptInfo>>
                e : schoolResult.schoolToDepts.entrySet()){

            if (e.getKey().code.equals(al.get(0).s2))
                defaultDepartments = e.getValue();

            if (!e.getKey().code.equals(schoolCode)) continue;

            for (SchoolListParser.DeptInfo dept : e.getValue()){
                if (!dept.code.equals(deptCode)) continue;

                setDepartments(e.getValue());

                setSelections(
                        schoolResult,
                        schoolResult.latestSchoolYear,
                        schoolResult.latestSemester,
                        e.getKey().code,
                        dept.code);
            }
        }

        // department not found; probably graduate student
        if (departments.getValue() == null){
            setDepartments(defaultDepartments);
            int[] selections = this.selections.getValue();
            Arrays.fill(selections, 0);
            this.selections.setValue(selections);
            return true;
        }

        return false;
    }

    /**
     * should be called from UI thread
     */
    public void setTitleFromFilter() {
        int[] selections = this.selections.getValue();

        String title = "";
        title += commons.schoolYears.getValue().get(selections[0]);

        title += "년 ";
        switch (selections[1]){
            case 0:
                title += "1학기 ";
                break;

            case 1:
                title += "2학기 ";
                break;

            case 2:
                title += "계절학기 ";
                break;
        }

        title += departments.getValue().get(selections[3]).s1;

        title += " ";
        StringJoiner sj = new StringJoiner(" ");
        for (int i = 0; i < commons.filterOptions.getValue().yearLevels.length; i++){
            if (commons.filterOptions.getValue().yearLevels[i])
                sj.add(String.valueOf(i + 1));
        }

        if (!sj.toString().isEmpty()){
            title += sj.toString();
            title += "학년";
        }

        commons.title.setValue(title);
    }

    public boolean setMyFilter(WiseViewModel wiseViewModel){
        if (departmentNotFound)
            return false;

        setSelections((SchoolListParser.Result) wiseViewModel.getSchoolList().getValue(),
                mySchoolYear, mySemester, mySchoolCode, myDeptCode);
        return true;
    }

    private void setSelections(SchoolListParser.Result schoolList,
                               int schoolYear, String semester, String schoolCode, String deptCode){
        int[] selections = this.selections.getValue();

        selections[0] = LinearTimeHelper.INSTANCE.indexOf(
                commons.schoolYears.getValue(),
                schoolYear,
                (s, integer) -> s.compareTo(String.valueOf(integer)));

        selections[1] = getSemesterSelection(semester);

        selections[2] = LinearTimeHelper.INSTANCE.indexOf(
                schools.getValue(),
                schoolCode,
                (stringPair, s) -> stringPair.s2.compareTo(s));

        updateDepartmentList(schoolList, schoolCode);

        selections[3] = LinearTimeHelper.INSTANCE.indexOf(
                departments.getValue(),
                deptCode,
                (stringPair, s) -> stringPair.s2.compareTo(s));

        this.selections.setValue(selections);
    }

    private String getSemesterString(int selection){
        switch (selection){
            case 0:
                return "10";
            case 1:
                return "20";
            case 2:
                return "11";
        }

        return null;
    }

    private int getSemesterSelection(String semester){
        switch (semester){
            case "10":
                return 0;
            case "20":
                return 1;
            case "11":
                return 2;
        }

        return -1;
    }

    private void updateDepartmentList(SchoolListParser.Result schoolList, String schoolCode){
        for (SchoolListParser.DeptInfo dept : schoolList.schoolToDepts.keySet())
            if (dept.code.equals(schoolCode))
                setDepartments(schoolList.schoolToDepts.get(dept));
    }

    /**
     * This is "set"-Departments not "post"-Departments
     * @param departments
     */
    public void setDepartments(List<SchoolListParser.DeptInfo> departments){
        List<StringPair> deptStrings = new ArrayList<>();
        departments.forEach((info) -> deptStrings.add(new StringPair(info.name, info.code)));
        Collections.sort(deptStrings, (o1, o2) -> o1.s1.compareTo(o2.s1));
        this.departments.setValue(deptStrings);
    }

    public LiveData<List<String>> getSchoolYears() {
        return commons.schoolYears;
    }

    public LiveData<List<StringPair>> getSchools() {
        return schools;
    }

    public LiveData<List<StringPair>> getDepartments() {
        return departments;
    }

    public void setSelection(WiseViewModel wiseViewModel, int index, int value){
        // if a new school was selected
        if (index == 2){
            String schoolCode = schools.getValue().get(value).s2;
            updateDepartmentList(
                    (SchoolListParser.Result) wiseViewModel.getSchoolList().getValue(),
                    schoolCode);
        }

        int[] selections = this.selections.getValue();
        selections[index] = value;
        this.selections.setValue(selections);
    }

    public LiveData<int[]> getSelections(){
        return selections;
    }

    public MutableLiveData<FilterOptions> getFilterOptions(){
        return commons.filterOptions;
    }
}
