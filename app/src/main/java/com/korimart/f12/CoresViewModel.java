package com.korimart.f12;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class CoresViewModel extends ViewModel implements CourseListViewModel {
    private CourseListCommon commons = new CourseListCommon();

    @Override
    public void onViewCreated(WiseViewModel wiseViewModel, MainActivity mainActivity) {

    }

    @Override
    public void referesh(WiseViewModel wiseViewModel, MainActivity mainActivity) {

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
}
