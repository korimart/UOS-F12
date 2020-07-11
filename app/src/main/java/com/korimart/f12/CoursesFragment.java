package com.korimart.f12;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.korimart.f12.SchoolListFetcher.DeptInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CoursesFragment extends Fragment {
    private RecyclerViewAdapter adapter;
    private CoursesViewModel coursesViewModel;
    private F12ViewModel f12ViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_courses, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewModelProvider vmp = new ViewModelProvider(getActivity(), new ViewModelProvider.NewInstanceFactory());
        coursesViewModel = vmp.get(CoursesViewModel.class);
        f12ViewModel = vmp.get(F12ViewModel.class);

        RecyclerView recyclerView = view.findViewById(R.id.courses_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RecyclerViewAdapter(10);
        recyclerView.setAdapter(adapter);

        DividerItemDecoration did = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(did);

        MainActivity ma = (MainActivity) getActivity();
        view.findViewById(R.id.courses_filterButton).setOnClickListener(v ->
                ma.goToCoursesFilterFrag(ma::goToCoursesFrag));

        f12ViewModel.getResult().observe(this, (result -> {
            if (result == null || result.schoolCode == null) return;
            if (coursesViewModel.getSchoolListResult().getValue() != null) return;

            fetchSchoolList();
        }));
    }

    private void fetchSchoolList() {
        coursesViewModel.fetchSchoolList(this::onSuccess, this::onError, this::anyway);
    }

    private void anyway() {
    }

    private void onError(ErrorInfo errorInfo) {
    }

    private void onSuccess() {
        getActivity().runOnUiThread(() -> {
            SchoolListFetcher.Result schoolResult = coursesViewModel.getSchoolListResult().getValue();
            if (schoolResult == null) return;

            F12Fetcher.Result f12Result = f12ViewModel.getResult().getValue();

            ArrayList<String> al = new ArrayList<>();
            schoolResult.schoolToDepts.keySet().forEach((info) -> al.add(info.name));
            Collections.sort(al);
            coursesViewModel.getSchools().setValue(al);

            for (Map.Entry<DeptInfo, List<DeptInfo>> e : schoolResult.schoolToDepts.entrySet()){
                if (e.getKey().code.equals(f12Result.schoolCode)){
                    for (DeptInfo dept : e.getValue()){
                        if (dept.code.equals(f12Result.deptCode)){
                            coursesViewModel.setDepartments(e.getValue());

                            int schoolPos = coursesViewModel.getSchools().getValue().indexOf(e.getKey().name);
                            int deptPos = coursesViewModel.getDepartments().getValue().indexOf(dept.name);

                            coursesViewModel.getSchoolSelection().setValue(schoolPos);
                            coursesViewModel.getDepartmentSelection().setValue(deptPos);
                        }
                    }
                }
            }
        });
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
        private int itemCount;

        public RecyclerViewAdapter(int itemCount){
            this.itemCount = itemCount;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(CoursesFragment.this.getContext())
                    .inflate(R.layout.item_course_desc, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            // nothing yet
        }

        @Override
        public int getItemCount() {
            return itemCount;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }
    }
}

