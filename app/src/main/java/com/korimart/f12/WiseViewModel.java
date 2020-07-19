package com.korimart.f12;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class WiseViewModel extends ViewModel {
    private Handler handler = new Handler(Looper.getMainLooper());

    private AsyncFetchParser f12InfoFetchParser
            = new AsyncFetchParser(URLStorage.getF12URL(), URLStorage.getF12InfoParams(), F12InfoParser.INSTANCE);
    private AsyncFetchParser schoolListFetchParser
            = new AsyncFetchParser(URLStorage.getSchoolListUrl(), URLStorage.getSchoolListParams(), SchoolListParser.INSTANCE);
    private AsyncFetchParser personalInfoFetchParser
            = new AsyncFetchParser(URLStorage.getPersInfoUrl(), URLStorage.getPersInfoParams(), PersonalInfoParser.INSTANCE);
    private F12FetchParser f12FetchParser = new F12FetchParser(f12InfoFetchParser);
    private CourseListFetchParser courseListFetchParser
            = new CourseListFetchParser(f12InfoFetchParser, schoolListFetchParser);

    public CompletableFuture<Void> fetchAndParseF12(boolean refetch, boolean noPnp){
        f12FetchParser.setNoPnp(noPnp);
        return f12FetchParser.fetchAndParse(refetch);
    }

    public CompletableFuture<Void> fetchAndParseMyCourses(boolean refetch){
        courseListFetchParser.setFetchMine(true);
        courseListFetchParser.setRefetchF12Info(refetch);
        courseListFetchParser.setRefetchSchoolList(refetch);
        return courseListFetchParser.fetchAndParse(refetch);
    }

    public CompletableFuture<Void> fetchAndParseCourses(boolean refetch, int schoolYear,
                                                        String semester, String schoolCode,
                                                        String deptCode){
        courseListFetchParser.setFetchMine(false);
        courseListFetchParser.setRefetchF12Info(false);
        courseListFetchParser.setRefetchSchoolList(false);
        courseListFetchParser.setParams(schoolYear, semester, schoolCode, deptCode);
        return courseListFetchParser.fetchAndParse(refetch);
    }

    public CompletableFuture<Void> fetchAndParsePersonalInfo(boolean refetch){
        return personalInfoFetchParser.fetchAndParse(refetch);
    }

    public void errorHandler(Throwable throwable, Consumer<ErrorInfo> onError){
        if (throwable == null) return;

        Throwable cause = throwable.getCause();

        if (cause instanceof ErrorInfo){
            handler.post(() -> onError.accept((ErrorInfo) cause));
            if (((ErrorInfo) cause).throwable != null)
                ErrorReporter.INSTANCE.reportError(((ErrorInfo) cause).throwable);
        }
        else {
            handler.post(() -> onError.accept(new ErrorInfo(cause)));
            ErrorReporter.INSTANCE.reportError(cause);
        }
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

    public LiveData<WiseParser.Result> getCourseList() {
        return courseListFetchParser.getpCache().resultLiveData;
    }
}
