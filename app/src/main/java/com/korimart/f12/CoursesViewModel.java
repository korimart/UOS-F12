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

public class CoursesViewModel extends ViewModel {
    public static class FilterOptions {
        boolean[] yearLevels = new boolean[4];
    }

    private MutableLiveData<List<CourseListParser.CourseInfo>> filteredCourses = new MutableLiveData<>();
    private MutableLiveData<List<StringPair>> schools = new MutableLiveData<>();
    private MutableLiveData<List<StringPair>> departments = new MutableLiveData<>();
    private MutableLiveData<List<String>> schoolYears = new MutableLiveData<>();
    private MutableLiveData<FilterOptions> filterOptions = new MutableLiveData<>();
    private MutableLiveData<String> title = new MutableLiveData<>();
    
    private int[] selections = new int[4];
    private boolean firstOpen = true;
    private boolean shouldFetchCourses = false;

    public CoursesViewModel(){
        filterOptions.setValue(new FilterOptions());
    }

    /**
     * should be called in UI thread
     * @param courseInfos
     */
    public void applyFilter(List<CourseListParser.CourseInfo> courseInfos) {
        List<CourseListParser.CourseInfo> filteredCourses = new ArrayList<>(courseInfos);
        boolean[] yearLevels = filterOptions.getValue().yearLevels;

        filteredCourses.removeIf(courseInfo -> {
            for (int i = 0; i < yearLevels.length; i++){
                if (yearLevels[i]){
                    if (courseInfo.yearLevel.contains(String.valueOf(i + 1)))
                        return false;
                }
            }
            return true;
        });

        Collections.sort(filteredCourses, (o1, o2) -> o1.name.compareTo(o2.name));
        this.filteredCourses.setValue(filteredCourses);
    }

    /**
     * should be called in UI thread
     *
     * @param schoolCode my school code
     * @param deptCode my department code
     * @param schoolResult school list parsed
     * @return departmentNotFound
     */
    public boolean setUpInitialFilter(String schoolCode, String deptCode,
                                       @NonNull SchoolListParser.Result schoolResult) {
        ArrayList<String> schoolYears = new ArrayList<>();
        for (int i = schoolResult.latestSchoolYear; i > schoolResult.latestSchoolYear - 10; i--)
            schoolYears.add(String.valueOf(i));
        this.schoolYears.setValue(schoolYears);

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

                int[] selections = this.selections.getValue();
                selections[0] = 0;           // school year (0 for latest)
                selections[1] = semesterPos; // semester
                selections[2] = schoolPos;   // school
                selections[3] = deptPos;     // department
                this.selections.setValue(selections);
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
     * should be called in UI thread
     * @param parsed
     */
    public void setUpInitialYearLevel(@NonNull PersonalInfoParser.Result parsed) {
        FilterOptions options = filterOptions.getValue();

        if (parsed.yearLevel - 1 < options.yearLevels.length)
            options.yearLevels[parsed.yearLevel - 1] = true;

        filterOptions.setValue(options);
    }

    /**
     * should be called in UI thread
     */
    public void setTitleFromFilter() {
        String title = "";
        title += schoolYears.getValue().get(selections[0]);

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
        for (int i = 0; i < filterOptions.getValue().yearLevels.length; i++){
            if (filterOptions.getValue().yearLevels[i])
                sj.add(String.valueOf(i + 1));
        }

        if (!sj.toString().isEmpty()){
            title += sj.toString();
            title += "학년";
        }

        this.title.setValue(title);
    }

    public String getSemesterString(int selection){
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

    public MutableLiveData<String> getTitle() {
        return title;
    }

    public MutableLiveData<FilterOptions> getFilterOptions() {
        return filterOptions;
    }

    public LiveData<List<CourseListParser.CourseInfo>> getFilteredCourses() {
        return filteredCourses;
    }

    public boolean isFirstOpen() {
        return firstOpen;
    }

    public void setFirstOpen(boolean firstOpen) {
        this.firstOpen = firstOpen;
    }

    public void setSelection(int index, int value){
        selections[index] = value;
        shouldFetchCourses = true;
    }

    public int getSelection(int index){
        return selections[index];
    }

    public boolean shouldFetchCourses(){
        return shouldFetchCourses;
    }

    public void setShouldFetchCourses(boolean shouldFetch){
        shouldFetchCourses = shouldFetch;
    }
}
