package com.korimart.f12;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        coresViewModel = vmp.get(CoresViewModel.class);
        wiseViewModel = vmp.get(WiseViewModel.class);

        schoolYear = view.findViewById(R.id.core_filter_school_year);
        semester = view.findViewById(R.id.core_filter_semester);
        department = view.findViewById(R.id.core_filter_department);

        StringPairAdapter schoolYearAdapter = StringPairAdapter.setSpinnerAdapter(getContext(), schoolYear);
        StringPairAdapter semesterAdapter = StringPairAdapter.setSpinnerAdapter(getContext(), semester);
        StringPairAdapter deptAdapter = StringPairAdapter.setSpinnerAdapter(getContext(), department);
    }
}
