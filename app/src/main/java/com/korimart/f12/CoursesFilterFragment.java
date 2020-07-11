package com.korimart.f12;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

public class CoursesFilterFragment extends Fragment {
    private Spinner schoolYear;
    private Spinner semester;
    private Spinner school;
    private Spinner department;
    private CoursesViewModel coursesViewModel;
    private F12ViewModel f12ViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_courses_filter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewModelProvider vmp = new ViewModelProvider(getActivity(),
                new ViewModelProvider.NewInstanceFactory());
        coursesViewModel = vmp.get(CoursesViewModel.class);
        f12ViewModel = vmp.get(F12ViewModel.class);

        schoolYear = view.findViewById(R.id.courses_filter_school_year);
        semester = view.findViewById(R.id.courses_filter_semester);
        school = view.findViewById(R.id.courses_filter_school);
        department = view.findViewById(R.id.courses_filter_department);

        setViewListeners();
    }

    private void setViewListeners() {
        ArrayAdapter<String> schoolYearAdapter = setSpinnerAdapter(schoolYear);
        ArrayAdapter<String> semesterAdapter = setSpinnerAdapter(semester);
        ArrayAdapter<String> schoolAdapter = setSpinnerAdapter(school);
        ArrayAdapter<String> deptAdapter = setSpinnerAdapter(department);

        coursesViewModel.getSchoolYearSelection().observe(this, i -> schoolYear.setSelection(i));
        coursesViewModel.getSemesterSelection().observe(this, i -> semester.setSelection(i));
        coursesViewModel.getSchoolSelection().observe(this, i -> school.setSelection(i));
        coursesViewModel.getDepartmentSelection().observe(this, i -> department.setSelection(i));

        f12ViewModel.getResult().observe(this, result -> {
            schoolYearAdapter.clear();
            for (int i = result.schoolYear; i > result.schoolYear - 10; i--)
                schoolYearAdapter.add(String.valueOf(i));
        });

        semesterAdapter.add("1학기");
        semesterAdapter.add("2학기");
        semesterAdapter.add("계절학기");

        coursesViewModel.getSchools().observe(this, schools -> {
            schoolAdapter.clear();
            schoolAdapter.addAll(schools);
        });

        coursesViewModel.getDepartments().observe(this, departments -> {
            deptAdapter.clear();
            deptAdapter.addAll(departments);
        });

        school.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SchoolListFetcher.Result result = coursesViewModel.getSchoolListResult().getValue();
                for (SchoolListFetcher.DeptInfo dept : result.schoolToDepts.keySet())
                    if (dept.name.equals(((TextView) view).getText())){
                        coursesViewModel.setDepartments(result.schoolToDepts.get(dept));
                    }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // nothing
            }
        });
    }

    private ArrayAdapter<String> setSpinnerAdapter(Spinner spinner){
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.item_courses_filter);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        return adapter;
    }
}
