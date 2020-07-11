package com.korimart.f12;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class CoursesViewModel extends ViewModel {
    private MutableLiveData<SchoolListFetcher.Result> schoolListResult = new MutableLiveData<>();
    private MutableLiveData<List<String>> schools = new MutableLiveData<>();
    private MutableLiveData<List<String>> departments = new MutableLiveData<>();
    private MutableLiveData<Integer> departmentSelection = new MutableLiveData<>();
    private MutableLiveData<Integer> schoolSelection = new MutableLiveData<>();

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

    /**
     * This is "set"-Departments not "post"-Departments
     * @param departments
     */
    public void setDepartments(List<SchoolListFetcher.DeptInfo> departments){
        List<String> deptStrings = new ArrayList<>();
        departments.forEach((info) -> deptStrings.add(info.name));
        Collections.sort(deptStrings);
        getDepartments().setValue(deptStrings);
    }

    public MutableLiveData<SchoolListFetcher.Result> getSchoolListResult() {
        return schoolListResult;
    }

    public MutableLiveData<List<String>> getSchools() {
        return schools;
    }

    public MutableLiveData<List<String>> getDepartments() {
        return departments;
    }

    public MutableLiveData<Integer> getDepartmentSelection() {
        return departmentSelection;
    }

    public MutableLiveData<Integer> getSchoolSelection() {
        return schoolSelection;
    }
}
