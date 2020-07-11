package com.korimart.f12;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;
import java.util.function.Consumer;

public class CoursesViewModel extends ViewModel {
    private MutableLiveData<SchoolListFetcher.Result> schoolListResult = new MutableLiveData<>();
    private MutableLiveData<List<String>> departments = new MutableLiveData<>();

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

    public MutableLiveData<SchoolListFetcher.Result> getSchoolListResult() {
        return schoolListResult;
    }

    public MutableLiveData<List<String>> getDepartments() {
        return departments;
    }
}
