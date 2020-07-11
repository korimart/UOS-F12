package com.korimart.f12;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class CoursesViewModel extends ViewModel {
    private MutableLiveData<SchoolListFetcher.Result> schoolListResult = new MutableLiveData<>();
    private MutableLiveData<PersonalInfoFetcher.Result> personalInfoResult = new MutableLiveData<>();
    private MutableLiveData<CourseListFetcher.Result> courseListResult = new MutableLiveData<>();
    private MutableLiveData<List<CourseListFetcher.CourseInfo>> filteredCourses = new MutableLiveData<>();
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

    public CoursesViewModel(){
        shouldFetchCourses.setValue(false);
        shouldApplyFilter.setValue(false);
        freshman.setValue(false);
        sophomore.setValue(false);
        junior.setValue(false);
        senior.setValue(false);
    }

    public void fetchSchoolList(Runnable onSuccess, Consumer<ErrorInfo> onError, Runnable anyway){
        new Thread(() -> {
            SchoolListFetcher.Result result = SchoolListFetcher.INSTANCE.fetch();
            this.schoolListResult.postValue(result);

            if (result.errorInfo != null)
                onError.accept(result.errorInfo);
            else
                onSuccess.run();

            anyway.run();
        }).start();
    }

    public void fetchPersonalInfo(Runnable onSuccess, Consumer<ErrorInfo> onError, Runnable anyway){
        new Thread(() -> {
            PersonalInfoFetcher.Result result = PersonalInfoFetcher.INSTANCE.fetch();
            this.personalInfoResult.postValue(result);

            if (result.errorInfo != null)
                onError.accept(result.errorInfo);
            else
                onSuccess.run();

            anyway.run();
        }).start();
    }

    public void fetchCourses(Runnable onSuccess, Consumer<ErrorInfo> onError, Runnable anyway){
        new Thread(() -> {
            CourseListFetcher.Result result = CourseListFetcher.INSTANCE.fetch(
                    Integer.parseInt(schoolYears.getValue().get(schoolYearSelection.getValue())),
                    getSemesterCode(semesterSelection.getValue()),
                    schools.getValue().get(schoolSelection.getValue()).s2,
                    departments.getValue().get(departmentSelection.getValue()).s2
            );

            this.courseListResult.postValue(result);
            List<CourseListFetcher.CourseInfo> filteredCourses = new ArrayList<>(result.courseInfos);
            filterCourses(filteredCourses);
            this.filteredCourses.postValue(filteredCourses);

            if (result.errorInfo != null)
                onError.accept(result.errorInfo);
            else
                onSuccess.run();

            anyway.run();
        }).start();
    }

    public void applyFilter() {
        List<CourseListFetcher.CourseInfo> filteredCourses = new ArrayList<>(courseListResult.getValue().courseInfos);
        filterCourses(filteredCourses);
        this.filteredCourses.setValue(filteredCourses);
    }

    private void filterCourses(List<CourseListFetcher.CourseInfo> rawCoursesCopy) {
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
    public void setDepartments(List<SchoolListFetcher.DeptInfo> departments){
        List<StringPair> deptStrings = new ArrayList<>();
        departments.forEach((info) -> deptStrings.add(new StringPair(info.name, info.code)));
        Collections.sort(deptStrings, (o1, o2) -> o1.s1.compareTo(o2.s1));
        getDepartments().setValue(deptStrings);
    }

    public MutableLiveData<SchoolListFetcher.Result> getSchoolListResult() {
        return schoolListResult;
    }

    public MutableLiveData<PersonalInfoFetcher.Result> getPersonalInfoResult() {
        return personalInfoResult;
    }

    public MutableLiveData<CourseListFetcher.Result> getCourseListResult() {
        return courseListResult;
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

    public MutableLiveData<List<CourseListFetcher.CourseInfo>> getFilteredCourses() {
        return filteredCourses;
    }

    public MutableLiveData<Boolean> getShouldApplyFilter() {
        return shouldApplyFilter;
    }
}
