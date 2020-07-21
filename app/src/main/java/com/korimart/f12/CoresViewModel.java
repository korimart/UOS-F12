package com.korimart.f12;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CoresViewModel extends ViewModel implements CourseListViewModel {
    private CourseListCommon commons = new CourseListCommon();
    private MutableLiveData<List<StringPair>> departments = new MutableLiveData<>();
    private int[] selections = new int[3];

    private ErrorReporter errorReporter = ErrorReporter.INSTANCE;

    @Override
    public void onViewCreated(WiseViewModel wiseViewModel, MainActivity mainActivity) {
        if (!commons.firstOpen) return;

        commons.firstOpen = false;
        fetchLatest(wiseViewModel, mainActivity);
    }

    @Override
    public void refresh(WiseViewModel wiseViewModel, MainActivity mainActivity) {
        fetchLatest(wiseViewModel, mainActivity);
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

    public void fetchFromFilter(WiseViewModel wiseViewModel, MainActivity mainActivity) {
        commons.filteredCourses.setValue(null);
        commons.systemMessage.setValue("가져오는 중...");

        List<String> schoolYears = commons.schoolYears.getValue();

        if (schoolYears == null){
            ErrorInfo errorInfo = new ErrorInfo(new Exception("filter was not set but fetchFromFilter was called"));
            ErrorReporter.INSTANCE.reportError(errorInfo.throwable);
            this.onError(errorInfo, mainActivity);
            return;
        }

        int schoolYear = Integer.parseInt(schoolYears.get(selections[0]));
        String semester = getSemesterString(selections[1]);

        wiseViewModel
                .fetchAndParseCores(true, schoolYear, semester)
                .thenRun(() -> {
                    commons.handler.post(() -> applyFilter(wiseViewModel));
                })
                .exceptionally(throwable -> {
                    errorReporter.backgroundErrorHandler(
                            throwable, errorInfo -> onError(errorInfo, mainActivity));
                    return null;
                });
    }

    public void applyFilter(WiseViewModel wiseViewModel){
        CourseListParser.Result
                r = (CourseListParser.Result) wiseViewModel.getCoreList().getValue();
        List<CourseListParser.CourseInfo> filtered = new ArrayList<>(r.courseInfos);

        if (selections[2] > 0){
            String deptCode = departments.getValue().get(selections[2]).s2;
            filtered.removeIf(courseInfo -> !courseInfo.deptCode.equals(deptCode));
        }

        Collections.sort(filtered, (o1, o2) -> {
            if (o1.name.equals(o2.name))
                return o1.classNumber.compareTo(o2.classNumber);

            return o1.name.compareTo(o2.name);
        });

        if (filtered.isEmpty())
            commons.systemMessage.setValue("검색 결과가 없습니다.");
        else
            commons.systemMessage.setValue("");

        commons.filteredCourses.setValue(filtered);
    }

    public void setTitleFromFilter(){
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

        title += "교선+교필 ";

        title += departments.getValue().get(selections[2]).s1;
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

    private void fetchLatest(WiseViewModel wiseViewModel, MainActivity mainActivity){
        commons.filteredCourses.setValue(null);
        commons.systemMessage.setValue("가져오는 중...");

        wiseViewModel
                .fetchAndParseLatestCores(true)
                .thenRun(() -> onFetchAndParseReady(wiseViewModel))
                .exceptionally(throwable -> {
                    errorReporter.backgroundErrorHandler(
                            throwable, errorInfo -> onError(errorInfo, mainActivity));
                    return null;
                });
    }

    private void onFetchAndParseReady(WiseViewModel wiseViewModel){
        commons.handler.post(() -> {
            setUpInitialFilter((SchoolListParser.Result) wiseViewModel.getSchoolList().getValue());
            setTitleFromFilter();
            applyFilter(wiseViewModel);
        });
    }

    private void setUpInitialFilter(SchoolListParser.Result schoolResult){
        ArrayList<String> schoolYears = new ArrayList<>();
        for (int i = schoolResult.latestSchoolYear; i > schoolResult.latestSchoolYear - 10; i--)
            schoolYears.add(String.valueOf(i));
        commons.schoolYears.setValue(schoolYears);

        List<SchoolListParser.DeptInfo> defaultDepartments = null;

        for (Map.Entry<SchoolListParser.DeptInfo, List<SchoolListParser.DeptInfo>> e : schoolResult.schoolToDepts.entrySet()){
            if (e.getKey().code.equals("20230")){
                defaultDepartments = e.getValue();
                break;
            }
        }

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

        setDepartments(defaultDepartments);

        selections[0] = 0;
        selections[1] = semesterPos;
        selections[2] = 0;
    }

    private void setDepartments(List<SchoolListParser.DeptInfo> departments){
        List<StringPair> deptStrings = new ArrayList<>();
        departments.forEach((info) -> deptStrings.add(new StringPair(info.name, info.code)));
        Collections.sort(deptStrings, (o1, o2) -> o1.s1.compareTo(o2.s1));
        deptStrings.add(0, new StringPair("전체", "-----"));
        this.departments.setValue(deptStrings);
    }

    private void onError(ErrorInfo errorInfo, MainActivity mainActivity){
        switch (errorInfo.type){
            case sessionExpired:
                mainActivity.goToLoginFrag(1);
                break;

            case timeout:
            case responseFailed:
                commons.systemMessage.setValue("불러오기 실패");
                break;

            default:
                mainActivity.goToErrorFrag(errorInfo.throwable);
                break;
        }
    }

    public LiveData<List<String>> getSchoolYears() {
        return commons.schoolYears;
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
}
