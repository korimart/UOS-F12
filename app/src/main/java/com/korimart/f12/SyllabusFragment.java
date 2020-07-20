package com.korimart.f12;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.Collections;
import java.util.Locale;
import java.util.StringJoiner;

public class SyllabusFragment extends Fragment {
    private int position;
    private String timePlace;
    private boolean major;

    private WiseViewModel wiseViewModel;
    private SyllabusViewModel syllabusViewModel;
    private CourseListViewModel courseListViewModel;
    private CourseListParser.CourseInfo courseParsed;
    private MainActivity mainActivity;

    private TextView title;
    private TextView titleClassNumber;
    private LinearLayout courseInfo;
    private TextView TOYear;
    private TextView TOAll;
    private TextView summary;
    private TextView textbook;
    private TextView professor;
    private TextView professorDept;
    private LinearLayout professorInfo;
    private TextView rubricsType;
    private LinearLayout rubricsKeys;
    private LinearLayout rubricsValues;
    private LinearLayout weeklyPlans;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_syllabus, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        position = getArguments().getInt("position");
        timePlace = getArguments().getString("timePlace");
        major = getArguments().getBoolean("major");

        ViewModelProvider vmp = new ViewModelProvider(mainActivity, new ViewModelProvider.NewInstanceFactory());
        wiseViewModel = vmp.get(WiseViewModel.class);
        syllabusViewModel = vmp.get(SyllabusViewModel.class);

        if (major)
            courseListViewModel = vmp.get(MajorsViewModel.class);
        else
            courseListViewModel = vmp.get(CoresViewModel.class);

        TOYear = view.findViewById(R.id.syllabus_TO_year);
        TOAll = view.findViewById(R.id.syllabus_TO_all);
        title = view.findViewById(R.id.syllabus_title);
        titleClassNumber = view.findViewById(R.id.syllabus_classNumber);
        courseInfo = view.findViewById(R.id.syllabus_course_info);
        summary = view.findViewById(R.id.syllabus_course_summary);
        textbook = view.findViewById(R.id.syllabus_textbook);
        professor = view.findViewById(R.id.syllabus_professor);
        professorDept = view.findViewById(R.id.syllabus_professor_dept);
        professorInfo = view.findViewById(R.id.syllabus_professor_info);
        rubricsType = view.findViewById(R.id.syllabus_rubrics_type);
        rubricsKeys = view.findViewById(R.id.syllabus_rubrics_keys);
        rubricsValues = view.findViewById(R.id.syllabus_rubrics_values);
        weeklyPlans = view.findViewById(R.id.syllabus_plans);

        syllabusViewModel.getSystemMessage().observe(this,
                message -> title.setText(message));

        wiseViewModel.getSyllabus().observe(this, syllabus -> {
            if (syllabus == null) {
                clearDummy();
                return;
            }

            onSuccess((SyllabusFetchParser.Result) syllabus);
        });

        courseParsed = courseListViewModel.getFilteredCourses().getValue().get(position);
        syllabusViewModel.onViewCreated(wiseViewModel, mainActivity,
                courseParsed.schoolYear,
                courseParsed.semester,
                courseParsed.curriNumber,
                courseParsed.classNumber,
                courseParsed.uab,
                courseParsed.certDivCode);
    }

    private void onSuccess(SyllabusFetchParser.Result syllabus) {
        title.setText(courseParsed.name);
        titleClassNumber.setText(courseParsed.classNumber + "분반");

        syllabus.textbook = syllabus.textbook.replace("\n", "\n\n");
        syllabus.summary = syllabus.summary.replace("\n", "\n\n");

        this.TOYear.setText(String.format(
                Locale.getDefault(), "%s/%s", courseParsed.TOYear, courseParsed.TOYearMax));
        this.TOAll.setText(String.format(
                Locale.getDefault(), "%s/%s", courseParsed.TOAll, courseParsed.TOAllMax));

        LayoutInflater li = LayoutInflater.from(getContext());

        StringJoiner sj = new StringJoiner(", ");
        if (!syllabus.yearLevel.isEmpty())
            sj.add(syllabus.yearLevel);
        if (!syllabus.lecPrac.isEmpty())
            sj.add(syllabus.lecPrac);
        if (!syllabus.classification.isEmpty())
            sj.add(syllabus.classification);
        if (!syllabus.pointsTime.isEmpty())
            sj.add(syllabus.pointsTime);

        addTextToLinLay(li, courseInfo, sj.toString());
        addPermissions(li);
        addTextToLinLay(li, courseInfo, timePlace);

        if (syllabus.summary.isEmpty()){
            this.summary.setText("미입력");
            this.summary.setTextColor(0xFF757575);
        } else {
            this.summary.setText(syllabus.summary);
        }

        if (syllabus.textbook.isEmpty()){
            this.textbook.setText("미입력");
            this.textbook.setTextColor(0xFF757575);
        } else {
            this.textbook.setText(syllabus.textbook);
        }

        if (syllabus.professor.isEmpty()){
            this.professor.setText("TBA");
        } else {
            this.professor.setText(syllabus.professor);
            this.professorDept.setText(syllabus.professorDept);
        }

        addTextToLinLay(li, professorInfo, syllabus.professorPhone);
        addTextToLinLay(li, professorInfo, syllabus.professorEmail);
        addTextToLinLay(li, professorInfo, syllabus.professorWeb);
        addTextToLinLay(li, professorInfo, syllabus.counseling);

        this.rubricsType.setText(syllabus.rubricsType);
        Collections.sort(syllabus.rubrics,
                Collections.reverseOrder((o1, o2) -> o1.second.compareTo(o2.second)));

        for (Pair<String, Integer> pair : syllabus.rubrics){
            addTextToLinLay(li, rubricsKeys, pair.first);
            addTextToLinLay(li, rubricsValues, pair.second + "%");
        }

        if (syllabus.rubrics.isEmpty())
            addTextToLinLay(li, rubricsKeys, "미입력", 0xFF757575);

        for (int i = 0; i < syllabus.weeklyPlans.size(); i++)
            addWeeklyPlan(li, i + 1, syllabus.weeklyPlans.get(i));
    }

    private void addPermissions(LayoutInflater li) {
        class Helper {
            public String permString(boolean permission){
                return permission ? "허용" : "비허용";
            }

            public int permColor(boolean permission){
                return permission ? 0xFF1D7C1D : 0xFFFF0000;
            }
        }

        Helper helper = new Helper();

        LinearLayout permissions = (LinearLayout) li.inflate(R.layout.item_syllabus_permissions, courseInfo, false);

        TextView outsiderChild = permissions.findViewById(R.id.syllabus_outsider);
        outsiderChild.setText("타과" + helper.permString(courseParsed.outsider));
        outsiderChild.setTextColor(helper.permColor(courseParsed.outsider));

        TextView doubleChild = permissions.findViewById(R.id.syllabus_doubleMajor);
        doubleChild.setText("복수전공" + helper.permString(courseParsed.doubleMajor));
        doubleChild.setTextColor(helper.permColor(courseParsed.doubleMajor));

        TextView minorChild = permissions.findViewById(R.id.syllabus_minor);
        minorChild.setText("부전공" + helper.permString(courseParsed.minor));
        minorChild.setTextColor(helper.permColor(courseParsed.minor));

        courseInfo.addView(permissions);
    }

    private void addTextToLinLay(LayoutInflater li, LinearLayout parent, String childString){
        addTextToLinLay(li, parent, childString, null);
    }

    private void addTextToLinLay(LayoutInflater li, LinearLayout parent, String childString, Integer color){
        if (!childString.isEmpty()){
            TextView child = (TextView) li.inflate(R.layout.item_syllabus_text, parent, false);

            child.setText(childString);

            if (color != null)
                child.setTextColor(color);

            parent.addView(child);
        }
    }

    private void addWeeklyPlan(LayoutInflater li, int weekInt, String plan){
        LinearLayout linLay =
                (LinearLayout) li.inflate(R.layout.item_syllabus_weekly_plan, weeklyPlans, false);

        TextView week = linLay.findViewById(R.id.syllabus_plan_week);
        TextView text = linLay.findViewById(R.id.syllabus_plan_text);

        week.setText(weekInt + "주차");

        if (plan.isEmpty()){
            text.setText("미입력");
            text.setTextColor(0xFF757575);
        }
        else
            text.setText(plan);

        weeklyPlans.addView(linLay);
    }

    private void clearDummy(){
        title.setText("가져오는 중...");
        titleClassNumber.setText("");
        courseInfo.removeAllViews();
        summary.setText("");
        textbook.setText("");
        professor.setText("");
        professorDept.setText("");
        professorInfo.removeAllViews();
        rubricsType.setText("");
        rubricsKeys.removeAllViews();
        rubricsValues.removeAllViews();
        weeklyPlans.removeAllViews();
    }
}
