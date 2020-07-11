package com.korimart.f12;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
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

            if (coursesViewModel.getSchoolListResult().getValue() == null)
                fetchSchoolList();

            if (coursesViewModel.getPersonalInfoResult().getValue() == null)
                fetchPersonalInfo();
        }));
    }

    private void fetchPersonalInfo() {
        FragmentActivity fa = getActivity();
        coursesViewModel.fetchPersonalInfo(() -> onPersonalInfoFetched(fa), this::onError, () -> {});
    }

    private void onPersonalInfoFetched(FragmentActivity fa) {
        fa.runOnUiThread(() -> {
            switch (coursesViewModel.getPersonalInfoResult().getValue().yearLevel){
                case 1:
                    coursesViewModel.getFreshman().setValue(true);
                    break;

                case 2:
                    coursesViewModel.getSophomore().setValue(true);
                    break;

                case 3:
                    coursesViewModel.getJunior().setValue(true);
                    break;

                case 4:
                    coursesViewModel.getSenior().setValue(true);
                    break;
            }
        });
    }

    private void fetchSchoolList() {
        FragmentActivity fa = getActivity();
        coursesViewModel.fetchSchoolList(() -> onSchoolListFetched(fa), this::onError, () -> {});
    }

    private void onError(ErrorInfo errorInfo) {
        getActivity().runOnUiThread(() -> {
            switch (errorInfo.errorType){
                case "sessionExpired":
                    MainActivity ma = (MainActivity) getActivity();
                    ma.goToLoginFrag(1);
                    break;

                case "responseFailed":
                    // TODO
                    break;

                default:
                    ((MainActivity) getActivity()).goToErrorFrag(errorInfo.callStack);
                    break;
            }
        });
    }

    private void onSchoolListFetched(FragmentActivity activity) {
        activity.runOnUiThread(() -> {
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
                            int semesterPos = 0;
                            switch (schoolResult.latestSemester){
                                case "10":
                                    semesterPos = 0;
                                    break;
                                case "20":
                                    semesterPos = 1;
                                    break;
                                case "11":
                                    semesterPos = 2;
                                    break;
                            }

                            coursesViewModel.getSchoolSelection().setValue(schoolPos);
                            coursesViewModel.getDepartmentSelection().setValue(deptPos);
                            coursesViewModel.getSemesterSelection().setValue(semesterPos);
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

