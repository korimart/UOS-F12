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

public class MajorsFilterFragment extends Fragment {
    private Spinner schoolYear;
    private Spinner semester;
    private Spinner school;
    private Spinner department;
    private CheckBox[] yearLevels = new CheckBox[4];
    private MajorsViewModel majorsViewModel;
    private WiseViewModel wiseViewModel;
    private MainActivity mainActivity;

    private boolean shouldFetchCourses;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }

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
        majorsViewModel = vmp.get(MajorsViewModel.class);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        majorsViewModel.setTitleFromFilter();
        if (shouldFetchCourses){
            majorsViewModel.fetchFromFilter(wiseViewModel, mainActivity);
        }
        else {
            majorsViewModel.applyFilter(wiseViewModel);
        }
    }

    private void setViewListeners() {
        StringPairAdapter schoolYearAdapter = setSpinnerAdapter(schoolYear);
        StringPairAdapter semesterAdapter = setSpinnerAdapter(semester);
        StringPairAdapter schoolAdapter = setSpinnerAdapter(school);
        StringPairAdapter deptAdapter = setSpinnerAdapter(department);

        semesterAdapter.add(new StringPair("1학기", "10"));
        semesterAdapter.add(new StringPair("2학기", "20"));
        semesterAdapter.add(new StringPair("계절학기", "11"));

        schoolYear.setSelection(majorsViewModel.getSelection(0));
        semester.setSelection(majorsViewModel.getSelection(1));
        school.setSelection(majorsViewModel.getSelection(2));
        department.setSelection(majorsViewModel.getSelection(3));

        majorsViewModel.getFilterOptions().observe(this, options -> {
            for (int i = 0; i < options.yearLevels.length; i++)
                yearLevels[i].setChecked(options.yearLevels[i]);
        });

        for (int i = 0; i < yearLevels.length; i++){
            int finalI = i;
            yearLevels[i].setOnCheckedChangeListener(((buttonView, isChecked) -> {
                MajorsViewModel.FilterOptions fo = majorsViewModel.getFilterOptions().getValue();
                fo.yearLevels[finalI] = isChecked;
                majorsViewModel.getFilterOptions().postValue(fo);
            }));
        }

        majorsViewModel.getSchoolYears().observe(this, list -> {
            schoolYearAdapter.clear();
            for (String year : list)
                schoolYearAdapter.add(new StringPair(year, null));
        });

        majorsViewModel.getSchools().observe(this, schools -> {
            schoolAdapter.clear();
            schoolAdapter.addAll(schools);
        });

        majorsViewModel.getDepartments().observe(this, departments -> {
            deptAdapter.clear();
            deptAdapter.addAll(departments);
        });

        schoolYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            private int count = 0;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (count > 0){
                    majorsViewModel.setSelection(0, position);
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
                    majorsViewModel.setSelection(1, position);
                    shouldFetchCourses = true;
                }
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
                if (count > 0){
                    majorsViewModel.setSelection(2, position);
                    shouldFetchCourses = true;
                }

                SchoolListParser.Result result = (SchoolListParser.Result) wiseViewModel.getSchoolList().getValue();
                for (SchoolListParser.DeptInfo dept : result.schoolToDepts.keySet())
                    if (dept.name.equals(((TextView) view).getText())){
                        majorsViewModel.setDepartments(result.schoolToDepts.get(dept));
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
                    majorsViewModel.setSelection(3, position);
                    shouldFetchCourses = true;
                }
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