package com.korimart.f12;

import androidx.lifecycle.LiveData;

import java.util.List;

public interface CourseListViewModel {
    class FilterOptions {
        boolean[] yearLevels = new boolean[4];
    }

    void onFirstOpen(WiseViewModel wiseViewModel, MainActivity mainActivity);
    boolean isFirstOpen();
    LiveData<List<CourseListParser.CourseInfo>> getFilteredCourses();
    LiveData<String> getTitle();
    LiveData<String> getSystemMessage();
}
