package com.korimart.f12;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
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
    private CheckBox[] yearLevels = new CheckBox[4];
    private CoursesViewModel coursesViewModel;
    private WiseViewModel wiseViewModel;

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
        wiseViewModel = vmp.get(WiseViewModel.class);

        schoolYear = view.findViewById(R.id.courses_filter_school_year);
        semester = view.findViewById(R.id.courses_filter_semester);
        school = view.findViewById(R.id.courses_filter_school);
        department = view.findViewById(R.id.courses_filter_department);
        yearLevels[0] = view.findViewById(R.id.courses_filter_freshman);
        yearLevels[1] = view.findViewById(R.id.courses_filter_sophomore);
        yearLevels[2] = view.findViewById(R.id.courses_filter_junior);
        yearLevels[3] = view.findViewById(R.id.courses_filter_senior);

        setViewListeners();
    }

    private void setViewListeners() {
        StringPairAdapter schoolYearAdapter = setSpinnerAdapter(schoolYear);
        StringPairAdapter semesterAdapter = setSpinnerAdapter(semester);
        StringPairAdapter schoolAdapter = setSpinnerAdapter(school);
        StringPairAdapter deptAdapter = setSpinnerAdapter(department);

        semesterAdapter.add(new StringPair("1학기", "10"));
        semesterAdapter.add(new StringPair("2학기", "20"));
        semesterAdapter.add(new StringPair("계절학기", "11"));

        schoolYear.setSelection(coursesViewModel.getSelection(0));
        semester.setSelection(coursesViewModel.getSelection(1));
        school.setSelection(coursesViewModel.getSelection(2));
        department.setSelection(coursesViewModel.getSelection(3));

        coursesViewModel.getFilterOptions().observe(this, options -> {
            for (int i = 0; i < options.yearLevels.length; i++)
                yearLevels[i].setChecked(options.yearLevels[i]);
        });

        for (int i = 0; i < yearLevels.length; i++){
            int finalI = i;
            yearLevels[i].setOnCheckedChangeListener(((buttonView, isChecked) -> {
                CoursesViewModel.FilterOptions fo = coursesViewModel.getFilterOptions().getValue();
                fo.yearLevels[finalI] = isChecked;
                coursesViewModel.getFilterOptions().postValue(fo);
            }));
        }

        coursesViewModel.getSchoolYears().observe(this, list -> {
            schoolYearAdapter.clear();
            for (String year : list)
                schoolYearAdapter.add(new StringPair(year, null));
        });

        coursesViewModel.getSchools().observe(this, schools -> {
            schoolAdapter.clear();
            schoolAdapter.addAll(schools);
        });

        coursesViewModel.getDepartments().observe(this, departments -> {
            deptAdapter.clear();
            deptAdapter.addAll(departments);
        });

        schoolYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            private int count = 0;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (count > 0)
                    coursesViewModel.setSelection(0, position);
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
                if (count > 0)
                    coursesViewModel.setSelection(1, position);
                count++;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        school.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            private int count = 0;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (count > 0)
                    coursesViewModel.setSelection(2, position);

                SchoolListParser.Result result = (SchoolListParser.Result) wiseViewModel.getSchoolList().getValue();
                for (SchoolListParser.DeptInfo dept : result.schoolToDepts.keySet())
                    if (dept.name.equals(((TextView) view).getText())){
                        coursesViewModel.setDepartments(result.schoolToDepts.get(dept));
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
                if (count > 0)
                    coursesViewModel.setSelection(3, position);
                count++;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private StringPairAdapter setSpinnerAdapter(Spinner spinner){
        StringPairAdapter adapter = new StringPairAdapter(getContext());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        return adapter;
    }
}

class StringPair {
    String s1;
    String s2;

    public StringPair(String s1, String s2){
        this.s1 = s1;
        this.s2 = s2;
    }
}

class StringPairAdapter extends ArrayAdapter<StringPair> {
    public StringPairAdapter(@NonNull Context context) {
        super(context, 0);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_courses_filter, parent, false);
        }

        TextView textView = convertView.findViewById(R.id.item_courses_filter);
        textView.setText(getItem(position).s1);
        return textView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TextView textView = (TextView) super.getDropDownView(position, convertView, parent);
        textView.setText(getItem(position).s1);
        return textView;
    }
}