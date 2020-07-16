package com.korimart.f12;

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

import static com.korimart.f12.SchoolListParser.DeptInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;

public class CoursesFragment extends Fragment {
    private RecyclerView recyclerView;
    private RecyclerViewAdapter adapter;
    private CoursesViewModel coursesViewModel;
    private F12ViewModel f12ViewModel;
    private TextView title;
    private TextView systemMessage;
    private Button refreshButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_courses, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initMembers(view);
        coursesViewModel.getCourseListFetchReady().observe(this, this::onCourseListFetchReady);
        coursesViewModel.getCourseListReady().observe(this, this::onCourseListReady);
        fetch(false);
    }

    private void initMembers(View view){
        ViewModelProvider vmp = new ViewModelProvider(getActivity(), new ViewModelProvider.NewInstanceFactory());
        coursesViewModel = vmp.get(CoursesViewModel.class);
        f12ViewModel = vmp.get(F12ViewModel.class);

        coursesViewModel.getFilteredCourses().setValue(null);

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
            fetch(true);
            refreshButton.setVisibility(View.INVISIBLE);
        });
    }

    private void onCourseListFetchReady(Boolean ready){
        if (ready == null || !ready) return;

        coursesViewModel.getCourseListFetchReady().setValue(false);

        PersonalInfoParser.Result persInfo = coursesViewModel.getPersonalInfoParsed();
        SchoolListParser.Result schoolList = coursesViewModel.getSchoolListParsed();

        if (!errorCheckFetchedParsed(
                coursesViewModel.getSchoolListFetched(),
                schoolList))
            return;

        if (!errorCheckFetchedParsed(
                coursesViewModel.getPersonalInfoFetched(),
                schoolList))
            return;

        if (!errorCheckFetchedParsed(
                f12ViewModel.getF12InfoFetched(),
                f12ViewModel.getF12InfoParsed()))
            return;

        boolean departmentNotFound = false;
        if (!coursesViewModel.isInitialized()){
            setupInitialYearLevel(persInfo);
            departmentNotFound = setupInitialFilter(schoolList);
            coursesViewModel.setInitialized(true);
        }

        if (departmentNotFound){
            this.title.setText("기본 필터를 가져오는데 실패했습니다.\n대학원생이신가요?");
            ErrorReporter.INSTANCE.reportError(new Exception("department not found"));
        }
        else
            setTitle();

        coursesViewModel.fetchAndParseCourses(coursesViewModel.isShouldFetchCourses())
                .thenRun(() -> coursesViewModel.getCourseListReady().postValue(true));
        coursesViewModel.setShouldFetchCourses(false);
    }

    private void onCourseListReady(Boolean ready){
        if (ready == null || !ready) return;

        coursesViewModel.getCourseListReady().setValue(false);

        if (!errorCheckFetchedParsed(
                coursesViewModel.getCourseListFetched(),
                coursesViewModel.getCourseListParsed()))
            return;

        coursesViewModel.applyFilter();
        setSystemMessage();
        adapter.notifyDataSetChanged();
    }

    private void fetch(boolean refetch) {
        systemMessage.setText("가져오는 중...");

        coursesViewModel.prepare(
                f12ViewModel.fetchAndParse(false, refetch),
                refetch
        ).thenRun(() -> coursesViewModel.getCourseListFetchReady().postValue(true));
    }

    private boolean errorCheckFetchedParsed(WiseFetcher.Result fetched, WiseParser.Result parsed){
        if (!onFetched(fetched)) return false;
        return onParsed(parsed);
    }

    private boolean onFetched(@NonNull WiseFetcher.Result fetched){
        if (fetched.errorInfo != null){
            onError(fetched.errorInfo);
            return false;
        }

        return true;
    }

    private boolean onParsed(@NonNull WiseParser.Result parsed){
        if (parsed.getErrorInfo() != null){
            onError(parsed.getErrorInfo());
            return false;
        }

        return true;
    }

    private void setSystemMessage(){
        if (coursesViewModel.getFilteredCourses().getValue().size() == 0)
            systemMessage.setText("검색 결과가 없습니다.");
        else
            systemMessage.setText("");
    }

    private void setTitle() {
        if (coursesViewModel.getSchoolYears().getValue() == null)
            return;

        String title = "";
        title += coursesViewModel.getSchoolYears().getValue().get(
                coursesViewModel.getSelections()[0]
        );

        title += "년 ";
        switch (coursesViewModel.getSelections()[1]){
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
                coursesViewModel.getSelections()[3]
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

    private void setupInitialYearLevel(@NonNull PersonalInfoParser.Result parsed) {
        switch (parsed.yearLevel){
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
    }

    private void onError(ErrorInfo errorInfo) {
        if (errorInfo.exception != null)
            ErrorReporter.INSTANCE.reportError(errorInfo.exception);

        switch (errorInfo.type){
            case sessionExpired:
                MainActivity ma = (MainActivity) getActivity();
                ma.goToLoginFrag(1);
                break;

            case timeout:
            case responseFailed:
                systemMessage.setText("불러오기 실패");
                refreshButton.setVisibility(View.VISIBLE);
                adapter.notifyDataSetChanged();
                break;

            default:
                ((MainActivity) getActivity()).goToErrorFrag();
                break;
        }
    }

    private boolean setupInitialFilter(@NonNull SchoolListParser.Result schoolResult) {
        ArrayList<String> schoolYears = new ArrayList<>();
        for (int i = schoolResult.latestSchoolYear; i > schoolResult.latestSchoolYear - 10; i--)
            schoolYears.add(String.valueOf(i));
        coursesViewModel.getSchoolYears().setValue(schoolYears);

        F12InfoParser.Result f12Result = f12ViewModel.getF12InfoParsed();

        ArrayList<StringPair> al = new ArrayList<>();
        schoolResult.schoolToDepts.keySet().forEach((info) -> al.add(new StringPair(info.name, info.code)));
        Collections.sort(al, (o1, o2) -> o1.s1.compareTo(o2.s1));
        coursesViewModel.getSchools().setValue(al);

        List<StringPair> schools = coursesViewModel.getSchools().getValue();
        List<DeptInfo> defaultDepartments = null;

        for (Map.Entry<DeptInfo, List<DeptInfo>> e : schoolResult.schoolToDepts.entrySet()){
            if (e.getKey().code.equals(al.get(0).s2))
                defaultDepartments = e.getValue();

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

                coursesViewModel.setSelections(0, 0);
                coursesViewModel.setSelections(1, semesterPos);
                coursesViewModel.setSelections(2, schoolPos);
                coursesViewModel.setSelections(3, deptPos);
            }
        }

        // department not found; probably graduate student
        if (coursesViewModel.getDepartments().getValue() == null){
            coursesViewModel.setDepartments(defaultDepartments);
            coursesViewModel.setSelections(0, 0);
            coursesViewModel.setSelections(1, 0);
            coursesViewModel.setSelections(2, 0);
            coursesViewModel.setSelections(3, 0);
            return true;
        }

        return false;
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

