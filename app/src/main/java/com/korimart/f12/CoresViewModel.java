package com.korimart.f12;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class CoresViewModel extends ViewModel implements CourseListViewModel {
    private CourseListCommon commons = new CourseListCommon();

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

    public void applyFilter(WiseViewModel wiseViewModel){
        CourseListParser.Result
                r = (CourseListParser.Result) wiseViewModel.getCoreList().getValue();
        List<CourseListParser.CourseInfo> filtered = new ArrayList<>(r.courseInfos);

        if (filtered.isEmpty())
            commons.systemMessage.setValue("검색 결과가 없습니다.");
        else
            commons.systemMessage.setValue("");

        commons.filteredCourses.setValue(filtered);
    }

    private void fetchLatest(WiseViewModel wiseViewModel, MainActivity mainActivity){
        wiseViewModel
                .fetchAndParseLatestCores(false)
                .thenRun(() -> onFetchAndParseReady(wiseViewModel, mainActivity))
                .exceptionally(throwable -> {
                    errorReporter.backgroundErrorHandler(
                            throwable, errorInfo -> onError(errorInfo, mainActivity));
                    return null;
                });
    }

    private void onFetchAndParseReady(WiseViewModel wiseViewModel, MainActivity mainActivity){
        commons.handler.post(() -> applyFilter(wiseViewModel));
    }

    private void onError(ErrorInfo errorInfo, MainActivity mainActivity){

    }
}
