package com.korimart.f12;

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

public class MajorsViewModel extends ViewModel implements CourseListViewModel {
    private CourseListCommon commons = new CourseListCommon();
    private MutableLiveData<List<StringPair>> schools = new MutableLiveData<>();
    private MutableLiveData<List<StringPair>> departments = new MutableLiveData<>();

    private int[] selections = new int[4];

    private ErrorReporter errorReporter = ErrorReporter.INSTANCE;

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

    public void fetchFilterAndFromFilter(WiseViewModel wiseViewModel, MainActivity mainActivity){
        commons.filteredCourses.setValue(null);
        commons.systemMessage.setValue("가져오는 중...");

        wiseViewModel.fetchAndParseMyCourses(true)
                .thenCompose(ignored -> wiseViewModel.fetchAndParsePersonalInfo(false))
                .thenRun(() -> onFetchAndParseReady(wiseViewModel, mainActivity))
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

    private void onFetchAndParseReady(WiseViewModel wiseViewModel, MainActivity mainActivity){
        commons.handler.post(() -> {
            F12InfoParser.Result f12Info =
                    (F12InfoParser.Result) wiseViewModel.getF12Info().getValue();

            SchoolListParser.Result schoolList =
                    (SchoolListParser.Result) wiseViewModel.getSchoolList().getValue();

            PersonalInfoParser.Result personalInfo =
                    (PersonalInfoParser.Result) wiseViewModel.getPersonalInfo().getValue();

            boolean departmentNotFound = setUpInitialFilter(
                    f12Info.schoolCode, f12Info.deptCode, schoolList);

            setUpInitialYearLevel(personalInfo);

            if (departmentNotFound)
                onError(new ErrorInfo(ErrorInfo.ErrorType.departmentNotFound), mainActivity);
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

        List<StringPair> schools = this.schools.getValue();
        List<SchoolListParser.DeptInfo> defaultDepartments = null;

        for (Map.Entry<SchoolListParser.DeptInfo, List<SchoolListParser.DeptInfo>> e : schoolResult.schoolToDepts.entrySet()){
            if (e.getKey().code.equals(al.get(0).s2))
                defaultDepartments = e.getValue();

            if (!e.getKey().code.equals(schoolCode)) continue;

            for (SchoolListParser.DeptInfo dept : e.getValue()){
                if (!dept.code.equals(deptCode)) continue;

                setDepartments(e.getValue());
                List<StringPair> depts = departments.getValue();

                int schoolPos = LinearTimeHelper.INSTANCE.indexOf(
                        schools,
                        e.getKey().name,
                        (stringPair, s) -> stringPair.s1.compareTo(s)
                );

                int deptPos = LinearTimeHelper.INSTANCE.indexOf(
                        depts,
                        dept.name,
                        (stringPair, s) -> stringPair.s1.compareTo(s)
                );

                int semesterPos = 0;
                switch (schoolResult.latestSemester){
                    case "10":
                        semesterPos = 0;
                        break;
                    case "20":
                        semesterPos = 1;
                        break;
                    case "11":
                        semesterPos = 2;
                        break;
                }

                selections[0] = 0;           // school year (0 for latest)
                selections[1] = semesterPos; // semester
                selections[2] = schoolPos;   // school
                selections[3] = deptPos;     // department
            }
        }

        // department not found; probably graduate student
        if (departments.getValue() == null){
            setDepartments(defaultDepartments);
            Arrays.fill(selections, 0);
            return true;
        }

        return false;
    }

    /**
     * should be called from UI thread
     * @param parsed
     */
    private void setUpInitialYearLevel(@NonNull PersonalInfoParser.Result parsed) {
        FilterOptions options = commons.filterOptions.getValue();

        if (parsed.yearLevel - 1 < options.yearLevels.length)
            options.yearLevels[parsed.yearLevel - 1] = true;

        commons.filterOptions.setValue(options);
    }

    /**
     * should be called from UI thread
     */
    public void setTitleFromFilter() {
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

    public void setSelection(int index, int value){
        selections[index] = value;
    }

    public int getSelection(int index){
        return selections[index];
    }

    public MutableLiveData<FilterOptions> getFilterOptions(){
        return commons.filterOptions;
    }
}
