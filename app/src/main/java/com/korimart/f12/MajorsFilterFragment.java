package com.korimart.f12;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import org.w3c.dom.Text;

import java.util.Locale;

public class MajorsFilterFragment extends Fragment {
    private Spinner schoolYear;
    private Spinner semester;
    private Spinner school;
    private Spinner department;
    private CheckBox[] yearLevels = new CheckBox[4];
    private MajorsViewModel majorsViewModel;
    private WiseViewModel wiseViewModel;
    private MainActivity mainActivity;
    private Button myMajor;
    private TextView systemMessage;

    private boolean shouldFetchCourses;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_course_list_filter_majors, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewModelProvider vmp = new ViewModelProvider(mainActivity,
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
        myMajor = view.findViewById(R.id.courses_filter_my_major);
        systemMessage = view.findViewById(R.id.courses_filter_system_message);

        setViewListeners();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // if spinners are not initialized don't do anything
        if (majorsViewModel.getSchoolYears().getValue() == null)
            return;

        SharedPreferences prefs = mainActivity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("savedSchoolYear", Integer.parseInt(((StringPair) schoolYear.getSelectedItem()).s1));
        editor.putString("savedSemester", ((StringPair) semester.getSelectedItem()).s2);
        editor.putString("savedSchoolCode", ((StringPair) school.getSelectedItem()).s2);
        editor.putString("savedDeptCode", ((StringPair) department.getSelectedItem()).s2);

        for (int i = 0; i < yearLevels.length; i++)
            editor.putBoolean("savedYear" + (i + 1), yearLevels[i].isChecked());
        editor.apply();

        majorsViewModel.setTitleFromFilter();
        if (shouldFetchCourses){
            majorsViewModel.fetchFromFilter(wiseViewModel, mainActivity);
        }
        else
            majorsViewModel.applyFilter(wiseViewModel);
    }

    private void setViewListeners() {
        myMajor.setOnClickListener(v -> {
            if (!majorsViewModel.setMyFilter(wiseViewModel))
                systemMessage.setVisibility(View.VISIBLE);
        });

        StringPairAdapter schoolYearAdapter = StringPairAdapter.setSpinnerAdapter(getContext(), schoolYear);
        StringPairAdapter semesterAdapter = StringPairAdapter.setSpinnerAdapter(getContext(), semester);
        StringPairAdapter schoolAdapter = StringPairAdapter.setSpinnerAdapter(getContext(), school);
        StringPairAdapter deptAdapter = StringPairAdapter.setSpinnerAdapter(getContext(), department);

        semesterAdapter.add(new StringPair("1학기", "10"));
        semesterAdapter.add(new StringPair("2학기", "20"));
        semesterAdapter.add(new StringPair("계절학기", "11"));

        majorsViewModel.getSelections().observe(this, selections -> {
            schoolYear.setSelection(selections[0]);
            semester.setSelection(selections[1]);
            school.setSelection(selections[2]);
            department.setSelection(selections[3]);
        });

        majorsViewModel.getFilterOptions().observe(this, options -> {
            for (int i = 0; i < options.yearLevels.length; i++)
                yearLevels[i].setChecked(options.yearLevels[i]);
        });

        for (int i = 0; i < yearLevels.length; i++){
            int finalI = i;
            yearLevels[i].setOnCheckedChangeListener(((buttonView, isChecked) -> {
                MajorsViewModel.FilterOptions fo = majorsViewModel.getFilterOptions().getValue();
                fo.yearLevels[finalI] = isChecked;
                majorsViewModel.getFilterOptions().setValue(fo);
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

        schoolYear.setOnItemSelectedListener(new ItemSelectedListener(0));
        semester.setOnItemSelectedListener(new ItemSelectedListener(1));
        school.setOnItemSelectedListener(new ItemSelectedListener(2));
        department.setOnItemSelectedListener(new ItemSelectedListener(3));
    }

    class ItemSelectedListener implements AdapterView.OnItemSelectedListener {
        private int count = 0;
        private int selectionIndex;

        public ItemSelectedListener(int selectionIndex){
            this.selectionIndex = selectionIndex;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (count > 0){
                majorsViewModel.setSelection(wiseViewModel, selectionIndex, position);
                shouldFetchCourses = true;
            }
            count++;
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
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

    public static StringPairAdapter setSpinnerAdapter(Context context, Spinner spinner){
        StringPairAdapter adapter = new StringPairAdapter(context);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        return adapter;
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