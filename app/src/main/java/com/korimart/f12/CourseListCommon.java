package com.korimart.f12;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CourseListCommon {
    MutableLiveData<List<String>> schoolYears = new MutableLiveData<>();
    MutableLiveData<List<CourseListParser.CourseInfo>> filteredCourses = new MutableLiveData<>();
    MutableLiveData<MajorsViewModel.FilterOptions> filterOptions = new MutableLiveData<>();
    MutableLiveData<String> title = new MutableLiveData<>();
    MutableLiveData<String> systemMessage = new MutableLiveData<>();

    String filterText = "";
    boolean firstOpen = true;
    Handler handler = new Handler(Looper.getMainLooper());

    public CourseListCommon(){
        filterOptions.setValue(new MajorsViewModel.FilterOptions());
    }

    /**
     * should be called from UI thread
     * @param courseInfos
     */
    public List<CourseListParser.CourseInfo>
    applyFilterOnYearLevels(List<CourseListParser.CourseInfo> courseInfos) {
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

        Collections.sort(filteredCourses, (o1, o2) -> {
            if (o1.name.equals(o2.name))
                return o1.classNumber.compareTo(o2.classNumber);

            return o1.name.compareTo(o2.name);
        });

        return filteredCourses;
    }
}
