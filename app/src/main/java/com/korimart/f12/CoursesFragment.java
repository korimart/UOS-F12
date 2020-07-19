package com.korimart.f12;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;

public class CoursesFragment extends Fragment {
    private RecyclerView recyclerView;
    private RecyclerViewAdapter adapter;
    private CoursesViewModel coursesViewModel;
    private WiseViewModel wiseViewModel;
    private TextView title;
    private TextView systemMessage;
    private Button refreshButton;

    private MainActivity mainActivity;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_courses, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initMembers(view);

        if (coursesViewModel.isFirstOpen()){
            coursesViewModel.setFirstOpen(false);

            wiseViewModel.fetchAndParseMyCourses(false)
                    .thenCompose(ignored -> wiseViewModel.fetchAndParsePersonalInfo(false))
                    .thenRun(this::setUpInitialFilter)
                    .exceptionally(throwable -> {
                        wiseViewModel.errorHandler(throwable, this::onError);
                        return null;
                    });
        }
    }

    private void initMembers(View view){
        ViewModelProvider vmp = new ViewModelProvider(getActivity(), new ViewModelProvider.NewInstanceFactory());
        coursesViewModel = vmp.get(CoursesViewModel.class);
        wiseViewModel = vmp.get(WiseViewModel.class);

        recyclerView = view.findViewById(R.id.courses_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RecyclerViewAdapter();
        recyclerView.setAdapter(adapter);

        DividerItemDecoration did = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(did);

        MainActivity ma = (MainActivity) getActivity();
        view.findViewById(R.id.courses_filterButton).setOnClickListener(v ->
                ma.goToCoursesFilterFrag(ma::goToCoursesFrag));

        title = view.findViewById(R.id.course_desc_title);
        systemMessage = view.findViewById(R.id.courses_system_message);
        refreshButton = view.findViewById(R.id.courses_refresh);

        refreshButton.setOnClickListener(v -> {
//            fetch(true);
            refreshButton.setVisibility(View.INVISIBLE);
        });

        coursesViewModel.getTitle().observe(this, s -> title.setText(s));
        coursesViewModel.getFilteredCourses().observe(this, courses -> {
            if (courses.size() == 0)
                systemMessage.setText("검색 결과가 없습니다.");
            else
                systemMessage.setText("");
        });

        coursesViewModel.getFilterOptions().observe(this, options -> {
            CourseListParser.Result courseList =
                    (CourseListParser.Result) wiseViewModel.getCourseList().getValue();

            if (courseList == null) return;

            coursesViewModel.setTitleFromFilter();
            coursesViewModel.applyFilter(courseList.courseInfos);
        });

        coursesViewModel.getSelections().observe(this, selections -> {
            List<String> schoolYears = coursesViewModel.getSchoolYears().getValue();
            List<StringPair> schools = coursesViewModel.getSchools().getValue();
            List<StringPair> depts = coursesViewModel.getDepartments().getValue();

            if (schoolYears == null || schools == null || depts == null)
                return;

            int schoolYear = Integer.parseInt(schoolYears.get(selections[0]));
            String semester = coursesViewModel.getSemesterString(selections[1]);
            String schoolCode = schools.get(selections[2]).s2;
            String deptCode = depts.get(selections[3]).s2;

            wiseViewModel
                    .fetchAndParseCourses(false, schoolYear, semester, schoolCode, deptCode)
                    .exceptionally(t -> {
                        wiseViewModel.errorHandler(t, this::onError);
                        return null;
                    });
        });
    }

    private void setUpInitialFilter(){
        mainActivity.runOnUiThread(() -> {
            F12InfoParser.Result f12Info =
                    (F12InfoParser.Result) wiseViewModel.getF12Info().getValue();

            SchoolListParser.Result schoolList =
                    (SchoolListParser.Result) wiseViewModel.getSchoolList().getValue();

            PersonalInfoParser.Result personalInfo =
                    (PersonalInfoParser.Result) wiseViewModel.getPersonalInfo().getValue();

            boolean departmentNotFound = coursesViewModel.setUpInitialFilter(
                    f12Info.schoolCode, f12Info.deptCode, schoolList);

            coursesViewModel.setUpInitialYearLevel(personalInfo);

            if (departmentNotFound)
                onError(new ErrorInfo(ErrorInfo.ErrorType.departmentNotFound));
        });
    }

    private void onError(ErrorInfo errorInfo) {
        switch (errorInfo.type){
            case sessionExpired:
                mainActivity.goToLoginFrag(1);
                break;

            case timeout:
            case responseFailed:
                systemMessage.setText("불러오기 실패");
                refreshButton.setVisibility(View.VISIBLE);
                adapter.notifyDataSetChanged();
                break;

            case departmentNotFound:
//                systemMessage.setText("기본 필터를 가져오는데 실패했습니다.\n학부생이 아니신가요?");
                break;

            default:
                mainActivity.goToErrorFrag();
                break;
        }
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(CoursesFragment.this.getContext())
                    .inflate(R.layout.item_course, parent, false);
            view.setOnClickListener(v -> {
                RecyclerView.ViewHolder viewHolder = recyclerView.getChildViewHolder(v);

                int position = viewHolder.getAdapterPosition();

                StringJoiner sj = new StringJoiner("\n");
                LinearLayout timePlaces = ((RecyclerViewAdapter.ViewHolder) viewHolder).timePlace;
                for (int i = 0; i < timePlaces.getChildCount(); i++){
                    TextView child = (TextView) timePlaces.getChildAt(i);
                    sj.add(child.getText());
                }

                MainActivity ma = (MainActivity) getActivity();
                if (ma == null) return;

                ma.goToCourseDescFrag(ma::goToCoursesFrag, position, sj.toString());
            });
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CourseListParser.CourseInfo course = coursesViewModel.getFilteredCourses().getValue().get(position);

            holder.name.setText(course.name);
            holder.classNumber.setText(course.classNumber + "분반");
            holder.classification.setText(course.classification);
            holder.yearLevel.setText(course.yearLevel + "학년");
            holder.professor.setText(course.professor.isEmpty() ? "TBA" : course.professor);
            holder.points.setText(course.points + "학점");
            holder.nonKorean.setVisibility(course.nonKorean ? View.VISIBLE : View.INVISIBLE);

            holder.timePlace.removeAllViews();
            String[] timePlaces = parseTimePlace(course.timePlace);
            for (String s : timePlaces){
                if (s == null) continue;

                TextView timePlaceChild = (TextView) LayoutInflater.from(getContext())
                        .inflate(R.layout.item_course_small_text, holder.timePlace, false);
                timePlaceChild.setText(s.isEmpty() ? "강의실 정보 없음" : s);
                holder.timePlace.addView(timePlaceChild);
            }

            holder.TOYear.setText(String.format(Locale.getDefault(), "%s/%s", course.TOYear, course.TOYearMax));
            holder.TOAll.setText(String.format(Locale.getDefault(), "%s/%s", course.TOAll, course.TOAllMax));
        }

        @Override
        public int getItemCount() {
            List<CourseListParser.CourseInfo> infos = coursesViewModel.getFilteredCourses().getValue();
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
                    "2", "제1공학관",
                    "3", "건설공학관",
                    "4", "창공관",
                    "5", "인문학관",
                    "6", "배봉관",
                    "7", "대학본부",
                    "8", "자연과학관",
                    "10", "경농관",
                    "11", "제2공학관",
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
                    "37", "100주년기념관",
                    "38", "스마트연구동",
                    "41", "실외테니스장",
                    "81", "자동화온실"
            ));

            String ret = "";
            int index = buildings.indexOf(buildingAndRoom[0]) + 1;
            if (index == 0)
                ret += buildingAndRoom[0];
            else
                ret += buildings.get(index);

            ret += "    ";

            // 강의실 없이 건물만 있는 경우가 있음
            // 예시) 수02,03/실외 테니스장
            if (buildingAndRoom.length > 1){
                String[] rooms = buildingAndRoom[1].split("/");

                for (String room : rooms)
                    ret += room + "호 ";
            }

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

            if (currTimeGroup == null){
                currTimeGroup = new TimeGroup();
                currTimeGroup.start = intTimeDivs[intTimeDivs.length - 1];
                timeGroups.add(currTimeGroup);
            }

            currTimeGroup.end = intTimeDivs[intTimeDivs.length - 1];

            StringBuilder ret = new StringBuilder();
            for (int i = 0; i < timeGroups.size(); i++){
                ret.append(String.format(
                        Locale.getDefault(),
                        "%02d",
                        timeGroups.get(i).start + 8))
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
            public TextView TOYear;
            public TextView TOAll;

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
                TOYear = itemView.findViewById(R.id.item_course_TO_year);
                TOAll = itemView.findViewById(R.id.item_course_TO_all);
            }
        }
    }
}

