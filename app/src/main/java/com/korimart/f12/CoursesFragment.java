package com.korimart.f12;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CoursesFragment extends Fragment {
    private RecyclerViewAdapter adapter;
    private CoursesViewModel coursesViewModel;
    private F12ViewModel f12ViewModel;
    private TextView title;

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
        adapter = new RecyclerViewAdapter();
        recyclerView.setAdapter(adapter);

        DividerItemDecoration did = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(did);

        MainActivity ma = (MainActivity) getActivity();
        view.findViewById(R.id.courses_filterButton).setOnClickListener(v ->
                ma.goToCoursesFilterFrag(ma::goToCoursesFrag));

        title = view.findViewById(R.id.courses_title);

        f12ViewModel.getResult().observe(this, (result -> {
            if (result == null || result.schoolCode == null) return;

            if (coursesViewModel.getSchoolListResult().getValue() == null)
                fetchSchoolList();

            if (coursesViewModel.getPersonalInfoResult().getValue() == null)
                fetchPersonalInfo();
        }));

        coursesViewModel.getShouldFetchCourses().observe(this, (b) -> {
            if (b) {
                fetchCourses();
                changeTitle();
                coursesViewModel.getShouldFetchCourses().setValue(false);
            }
        });

        coursesViewModel.getShouldApplyFilter().observe(this, (b) -> {
            if (b) {
                coursesViewModel.applyFilter();
                changeTitle();
                coursesViewModel.getShouldApplyFilter().setValue(false);
            }
        });
    }

    private void changeTitle() {
        String title = "";
        title += coursesViewModel.getSchoolYears().getValue().get(
                coursesViewModel.getSchoolSelection().getValue()
        );

        title += "년 ";
        switch (coursesViewModel.getSemesterSelection().getValue()){
            case 0:
                title += "1학기 ";
                break;

            case 1:
                title += "2학기 ";
                break;

            case 2:
                title += "계절학기 ";
                break;
        }
        title += coursesViewModel.getDepartments().getValue().get(
                coursesViewModel.getDepartmentSelection().getValue()
        ).s1;

        title += " ";
        if (coursesViewModel.getFreshman().getValue())
            title += "1 ";
        if (coursesViewModel.getSophomore().getValue())
            title += "2 ";
        if (coursesViewModel.getJunior().getValue())
            title += "3 ";
        if (coursesViewModel.getSenior().getValue())
            title += "4 ";

        title = title.substring(0, title.length() - 1);
        title += "학년";
        this.title.setText(title);
    }

    private void fetchPersonalInfo() {
        FragmentActivity fa = getActivity();
        coursesViewModel.fetchPersonalInfo(() -> onPersonalInfoFetched(fa), this::onError, () -> {});
    }

    private void fetchCourses(){
        FragmentActivity fa = getActivity();
        coursesViewModel.fetchCourses(() -> onCoursesFetched(fa), this::onError, () -> {});
    }

    private void onCoursesFetched(FragmentActivity fa) {
        fa.runOnUiThread(() -> adapter.notifyDataSetChanged());
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

            ArrayList<StringPair> al = new ArrayList<>();
            schoolResult.schoolToDepts.keySet().forEach((info) -> al.add(new StringPair(info.name, info.code)));
            Collections.sort(al, (o1, o2) -> o1.s1.compareTo(o2.s1));
            coursesViewModel.getSchools().setValue(al);

            List<StringPair> schools = coursesViewModel.getSchools().getValue();

            for (Map.Entry<DeptInfo, List<DeptInfo>> e : schoolResult.schoolToDepts.entrySet()){
                if (!e.getKey().code.equals(f12Result.schoolCode)) continue;

                for (DeptInfo dept : e.getValue()){
                    if (!dept.code.equals(f12Result.deptCode)) continue;

                    coursesViewModel.setDepartments(e.getValue());
                    List<StringPair> depts = coursesViewModel.getDepartments().getValue();

                    int schoolPos = LinearTimeHelper.INSTANCE.indexOf(
                            schools,
                            e.getKey().name,
                            (stringPair, s) -> stringPair.s1.compareTo(s)
                    );

                    int deptPos = LinearTimeHelper.INSTANCE.indexOf(
                            depts,
                            dept.name,
                            (stringPair, s) -> stringPair.s1.compareTo(s)
                    );

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

                    coursesViewModel.getSchoolYearSelection().setValue(0);
                    coursesViewModel.getSchoolSelection().setValue(schoolPos);
                    coursesViewModel.getDepartmentSelection().setValue(deptPos);
                    coursesViewModel.getSemesterSelection().setValue(semesterPos);
                }
            }

            ArrayList<String> schoolYears = new ArrayList<>();
            for (int i = schoolResult.latestSchoolYear; i > schoolResult.latestSchoolYear - 10; i--)
                schoolYears.add(String.valueOf(i));
            coursesViewModel.getSchoolYears().setValue(schoolYears);

            fetchCourses();
        });
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(CoursesFragment.this.getContext())
                    .inflate(R.layout.item_course_desc, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CourseListFetcher.CourseInfo course = coursesViewModel.getFilteredCourses().getValue().get(position);

            holder.name.setText(course.name);
            holder.classNumber.setText(course.classNumber + "분반");
            holder.classification.setText(course.classification);
            holder.yearLevel.setText(course.yearLevel + "학년");
            holder.professor.setText(course.professor);
            holder.points.setText(course.points + "학점");
            holder.nonKorean.setVisibility(course.nonKorean ? View.VISIBLE : View.INVISIBLE);

            holder.timePlace.removeAllViews();
            String[] timePlaces = parseTimePlace(course.timePlace);
            for (String s : timePlaces){
                if (s == null) continue;

                TextView timePlaceChild = new TextView(getContext());
                timePlaceChild.setText(s.isEmpty() ? "강의실 정보 없음" : s);
                holder.timePlace.addView(timePlaceChild);
            }
        }

        @Override
        public int getItemCount() {
            List<CourseListFetcher.CourseInfo> infos = coursesViewModel.getFilteredCourses().getValue();
            return infos == null ? 0 : infos.size();
        }

        private String[] parseTimePlace(String timePlace){
            String[] ret = timePlace.split(", ");
            if (ret.length == 1 && ret[0].isEmpty())
                return ret;

            final ArrayList<String> dayEng
                    = new ArrayList<>(Arrays.asList("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"));
            final ArrayList<String> dayKor
                    = new ArrayList<>(Arrays.asList("월요일", "화요일", "수요일", "목요일", "금요일", "토요일", "일요일"));

            for (int i = 0; i < ret.length; i++){
                String[] dayAndTimePlace = ret[i].split(" ");
                ret[i] = dayKor.get(dayEng.indexOf(dayAndTimePlace[0]));

                String[] timeAndPlace = dayAndTimePlace[1].split("/", 2);
                ret[i] += "    " + parseTime(timeAndPlace[0]);
                ret[i] += "    " + parsePlace(timeAndPlace[1]);
            }

            return ret;
        }

        private String parsePlace(String place) {
            String[] buildingAndRoom = place.split("-");

            final ArrayList<String> buildings = new ArrayList<>(Arrays.asList(
                    "1", "전농관",
                    "2", "제 1공학관",
                    "3", "건설공학관",
                    "4", "창공관",
                    "5", "인문학관",
                    "6", "배봉관",
                    "7", "대학본부",
                    "8", "자연과학관",
                    "10", "경농관",
                    "11", "제 2공학관",
                    "12", "학생회관",
                    "13", "학군단",
                    "14", "과학기술관",
                    "15", "21세기관",
                    "16", "조형관",
                    "18", "자작마루",
                    "19", "정보기술관",
                    "20", "법학관",
                    "21", "중앙도서관",
                    "22", "생활관",
                    "23", "건축구조실험동",
                    "24", "토목구조실험동",
                    "25", "미디어관",
                    "27", "대강당",
                    "28", "운동장",
                    "29", "박물관",
                    "32", "웰니스센터",
                    "33", "미래관",
                    "34", "국제학사",
                    "35", "음악관",
                    "36", "어린이집",
                    "37", "100주년 기념관",
                    "38", "스마트연구동",
                    "41", "실외테니스장",
                    "81", "자동화온실"
            ));

            String ret = "";
            int index = buildings.indexOf(buildingAndRoom[0]) + 1;
            if (index == 0)
                ret += "모르는건물";
            else
                ret += buildings.get(index);

            ret += "    ";

            String[] rooms = buildingAndRoom[1].split("/");
            for (String room : rooms)
                ret += room + "호 ";

            return ret;
        }

        private String parseTime(String time){
            class TimeGroup {
                int start;
                int end;
            }

            String[] timeDivs = time.split(",");
            int[] intTimeDivs = new int[timeDivs.length];
            for (int i = 0; i < intTimeDivs.length; i++){
                intTimeDivs[i] = Integer.parseInt(timeDivs[i]);
            }

            TimeGroup currTimeGroup = null;
            ArrayList<TimeGroup> timeGroups = new ArrayList<>();
            for (int i = 0; i + 1 < intTimeDivs.length; i++){
                if (currTimeGroup == null){
                    currTimeGroup = new TimeGroup();
                    currTimeGroup.start = intTimeDivs[i];
                    timeGroups.add(currTimeGroup);
                }

                if (intTimeDivs[i + 1] - intTimeDivs[i] > 1){
                    currTimeGroup.end = intTimeDivs[i];
                    currTimeGroup = null;
                }
            }

            if (currTimeGroup != null)
                currTimeGroup.end = intTimeDivs[intTimeDivs.length - 1];

            StringBuilder ret = new StringBuilder();
            for (int i = 0; i < timeGroups.size(); i++){
                ret.append(String.format("%02d", timeGroups.get(i).start + 8))
                        .append("시~")
                        .append(timeGroups.get(i).end + 8)
                        .append("시50분");

                if (i < timeGroups.size() - 1)
                    ret.append("    ");
            }

            return ret.toString();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView name;
            public TextView classNumber;
            public TextView classification;
            public TextView yearLevel;
            public TextView professor;
            public TextView points;
            public TextView nonKorean;
            public LinearLayout timePlace;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.item_course_name);
                classNumber = itemView.findViewById(R.id.item_course_class_number);
                classification = itemView.findViewById(R.id.item_course_classification);
                yearLevel = itemView.findViewById(R.id.item_course_year_level);
                professor = itemView.findViewById(R.id.item_course_professor);
                points = itemView.findViewById(R.id.item_course_points);
                nonKorean = itemView.findViewById(R.id.item_course_nonKorean);
                timePlace = itemView.findViewById(R.id.item_course_time_place);
            }
        }
    }
}

