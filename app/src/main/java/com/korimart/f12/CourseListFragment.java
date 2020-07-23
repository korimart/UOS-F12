package com.korimart.f12;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
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

public class CourseListFragment extends Fragment {
    private RecyclerView recyclerView;
    private RecyclerViewAdapter adapter;
    private CourseListViewModel courseListViewModel;
    private WiseViewModel wiseViewModel;
    private TextView title;
    private TextView systemMessage;
    private Button refreshButton;
    private SearchView searchView;

    private MainActivity mainActivity;
    private boolean major;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_course_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initMembers(view);
        courseListViewModel.onViewCreated(wiseViewModel, mainActivity);
    }

    private void initMembers(View view){
        major = getArguments().getBoolean("major");

        ViewModelProvider vmp = new ViewModelProvider(mainActivity, new ViewModelProvider.NewInstanceFactory());
        wiseViewModel = vmp.get(WiseViewModel.class);

        if (major)
            courseListViewModel = vmp.get(MajorsViewModel.class);
        else
            courseListViewModel = vmp.get(CoresViewModel.class);

        recyclerView = view.findViewById(R.id.courses_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(mainActivity));
        adapter = new RecyclerViewAdapter();
        recyclerView.setAdapter(adapter);

        DividerItemDecoration did = new DividerItemDecoration(mainActivity, DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(did);

        view.findViewById(R.id.courses_filterButton).setOnClickListener(v ->
                mainActivity.goToCoursesFilterFrag(() -> mainActivity.goToCoursesFrag(major), major));

        title = view.findViewById(R.id.course_desc_title);
        systemMessage = view.findViewById(R.id.courses_system_message);
        refreshButton = view.findViewById(R.id.courses_refresh);
        searchView = view.findViewById(R.id.courses_search);

        int search_close_btn = getResources().getIdentifier("android:id/search_close_btn", null, null);
        searchView.setQuery(courseListViewModel.getFilterText(), false);
        ImageView closeButton = searchView.findViewById(search_close_btn);
        closeButton.setOnClickListener(v -> searchView.setQuery("", false));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                courseListViewModel.applyFilterOnName(wiseViewModel, newText);
                return true;
            }
        });

        refreshButton.setOnClickListener(v -> {
            courseListViewModel.refresh(wiseViewModel, mainActivity);
            refreshButton.setVisibility(View.INVISIBLE);
        });

        courseListViewModel.getTitle().observe(this, s -> title.setText(s));
        courseListViewModel.getFilteredCourses().observe(this, courses -> {
            adapter.notifyDataSetChanged();
        });

        courseListViewModel.getSystemMessage().observe(this, message -> {
            if (message.equals("불러오기 실패"))
                refreshButton.setVisibility(View.VISIBLE);
            else
                refreshButton.setVisibility(View.INVISIBLE);

            systemMessage.setText(message);
        });
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(CourseListFragment.this.getContext())
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

                mainActivity.goToCourseDescFrag(() -> mainActivity.goToCoursesFrag(major), position, sj.toString(), major);
            });

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CourseListParser.CourseInfo course = CourseListFragment.this.courseListViewModel.getFilteredCourses().getValue().get(position);

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
            List<CourseListParser.CourseInfo> infos = courseListViewModel.getFilteredCourses().getValue();
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
                ret[i] += " " + parseTime(timeAndPlace[0]);
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

            ret += " ";

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
                private int start;
                private int end;
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
                    ret.append(" ");
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

