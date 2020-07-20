package com.korimart.f12;

import androidx.lifecycle.LiveData;

import java.util.List;

public interface CourseListViewModel {
    class FilterOptions {
        boolean[] yearLevels = new boolean[4];
    }

    void onViewCreated(WiseViewModel wiseViewModel, MainActivity mainActivity);
    void referesh(WiseViewModel wiseViewModel, MainActivity mainActivity);

    LiveData<List<CourseListParser.CourseInfo>> getFilteredCourses();
    LiveData<String> getTitle();
    LiveData<String> getSystemMessage();
}
