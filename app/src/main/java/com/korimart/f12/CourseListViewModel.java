package com.korimart.f12;

import androidx.lifecycle.LiveData;

import java.util.List;

public interface CourseListViewModel {
    class FilterOptions {
        boolean[] yearLevels = new boolean[4];
    }

    void onViewCreated(WiseViewModel wiseViewModel, MainActivity mainActivity);
    void refresh(WiseViewModel wiseViewModel, MainActivity mainActivity);

    LiveData<List<CourseListParser.CourseInfo>> getFilteredCourses();
    LiveData<String> getTitle();
    LiveData<String> getSystemMessage();

    void applyFilterOnName(WiseViewModel wiseViewModel, String text);
    String getFilterText();
}
