package com.korimart.f12;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

public class CoresFilterFragment extends Fragment {
    private MainActivity mainActivity;
    private WiseViewModel wiseViewModel;
    private CoresViewModel coresViewModel;

    private Spinner schoolYear;
    private Spinner semester;
    private Spinner department;

    private boolean shouldFetchCourses;
    private boolean shouldApplyFilter;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_course_list_filter_cores, container, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // if spinners are not initialized don't do anything
        if (coresViewModel.getSchoolYears().getValue() == null)
            return;

        coresViewModel.setTitleFromFilter();
        if (shouldFetchCourses){
            coresViewModel.fetchFromFilter(wiseViewModel, mainActivity);
        }
        else if (shouldApplyFilter)
            coresViewModel.applyFilter(wiseViewModel);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewModelProvider vmp = new ViewModelProvider(mainActivity,
                new ViewModelProvider.NewInstanceFactory());
        coresViewModel = vmp.get(CoresViewModel.class);
        wiseViewModel = vmp.get(WiseViewModel.class);

        schoolYear = view.findViewById(R.id.core_filter_school_year);
        semester = view.findViewById(R.id.core_filter_semester);
        department = view.findViewById(R.id.core_filter_department);

        StringPairAdapter schoolYearAdapter = StringPairAdapter.setSpinnerAdapter(getContext(), schoolYear);
        StringPairAdapter semesterAdapter = StringPairAdapter.setSpinnerAdapter(getContext(), semester);
        StringPairAdapter deptAdapter = StringPairAdapter.setSpinnerAdapter(getContext(), department);

        semesterAdapter.add(new StringPair("1학기", "10"));
        semesterAdapter.add(new StringPair("2학기", "20"));
        semesterAdapter.add(new StringPair("계절학기", "11"));

        coresViewModel.getSchoolYears().observe(this, list -> {
            schoolYearAdapter.clear();
            for (String year : list)
                schoolYearAdapter.add(new StringPair(year, null));
        });

        coresViewModel.getDepartments().observe(this, departments -> {
            deptAdapter.clear();
            deptAdapter.addAll(departments);
        });

        schoolYear.setSelection(coresViewModel.getSelection(0));
        semester.setSelection(coresViewModel.getSelection(1));
        department.setSelection(coresViewModel.getSelection(2));

        schoolYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            private int count = 0;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (count > 0){
                    coresViewModel.setSelection(0, position);
                    shouldFetchCourses = true;
                }
                count++;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        semester.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            private int count = 0;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (count > 0){
                    coresViewModel.setSelection(1, position);
                    shouldFetchCourses = true;
                }
                count++;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        department.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            private int count = 0;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (count > 0){
                    coresViewModel.setSelection(2, position);
                    shouldApplyFilter = true;
                }
                count++;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
}
