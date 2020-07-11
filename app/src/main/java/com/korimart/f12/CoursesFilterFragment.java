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

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class CoursesFilterFragment extends Fragment {
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

        school = view.findViewById(R.id.courses_filter_school);
        department = view.findViewById(R.id.courses_filter_department);

        setViewListeners();
    }

    private void setViewListeners() {
        ArrayAdapter<String> schoolAdapter = setSpinnerAdapter(school);
        ArrayAdapter<String> deptAdapter = setSpinnerAdapter(department);

        coursesViewModel.getSchoolListResult().observe(this, result -> {
            schoolAdapter.clear();
            for (SchoolListFetcher.DeptInfo info : result.schoolToDepts.keySet()){
                schoolAdapter.add(info.name);
            }
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
                        List<String> deptStrings = new ArrayList<>();
                        result.schoolToDepts.get(dept).forEach((info) -> deptStrings.add(info.name));
                        coursesViewModel.getDepartments().setValue(deptStrings);
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
