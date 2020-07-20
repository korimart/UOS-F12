package com.korimart.f12;

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
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;

public class SyllabusFragment extends Fragment {
    private SyllabusViewModel syllabusViewModel;
    private MajorsViewModel majorsViewModel;
    private CourseListParser.CourseInfo courseParsed;
    private String timePlace;
    private int position;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_syllabus, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewModelProvider vmp = new ViewModelProvider(getActivity(), new ViewModelProvider.NewInstanceFactory());
        majorsViewModel = vmp.get(MajorsViewModel.class);
        syllabusViewModel = vmp.get(SyllabusViewModel.class);

        position = getArguments().getInt("position");
        timePlace = getArguments().getString("timePlace");

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

        clearDummy();

        courseParsed = majorsViewModel.getFilteredCourses().getValue().get(position);

        syllabusViewModel.getOwReady().observe(this, this::onOwReady);
        fetch();
    }

    private void fetch(){
        syllabusViewModel.fetchAndParseSyllabus(
                courseParsed.schoolYear,
                courseParsed.semester,
                courseParsed.curriNumber,
                courseParsed.classNumber,
                courseParsed.uab,
                courseParsed.certDivCode)
                .thenRun(() -> syllabusViewModel.getOwReady().postValue(true));
    }

    private void onOwReady(Boolean ready){
        if (ready == null || !ready) return;

        syllabusViewModel.getOwReady().setValue(false);

        if (!checkError(syllabusViewModel.getoFetched().errorInfo))
            return;

        if (!checkError(syllabusViewModel.getoParsed().getErrorInfo()))
            return;

        if (!checkError(syllabusViewModel.getwFetched().errorInfo))
            return;

        if (!checkError(syllabusViewModel.getwParsed().getErrorInfo()))
            return;

        onSuccess();
    }

    private void onSuccess() {
        title.setText(courseParsed.name);
        titleClassNumber.setText(courseParsed.classNumber + "분반");

        SyllabusUabOParser.Result oParsed = (SyllabusUabOParser.Result) syllabusViewModel.getoParsed();
        WiseParser.Result wParsed = syllabusViewModel.getwParsed();

        String lecPrac = oParsed.lecPrac;
        String yearLevel = oParsed.yearLevel;
        String classification = oParsed.classification;
        String pointsTime = oParsed.pointsTime;
        String professor = oParsed.professor;
        String professorDept = oParsed.professorDept;
        String professorPhone = oParsed.professorPhone;
        String professorEmail = oParsed.professorEmail;
        String professorWeb = oParsed.professorWeb;
        String counseling = oParsed.counseling;
        String rubricsType = oParsed.rubricsType;
        List<Pair<String, Integer>> rubrics = oParsed.rubrics;

        String summary;
        String textbook;
        List<String> weeklyPlans;

        if (oParsed instanceof SyllabusOParser.Result){
            summary = ((SyllabusOParser.Result) oParsed).summary;
            textbook = ((SyllabusOParser.Result) oParsed).textbook;
            weeklyPlans = ((SyllabusWParser.Result) wParsed).weeklyPlans;
        }
        else {
            summary = ((SyllabusUabWParser.Result) wParsed).summary;
            textbook = ((SyllabusUabWParser.Result) wParsed).textbook;
            weeklyPlans = ((SyllabusUabWParser.Result) wParsed).weeklyPlans;
        }

        textbook = textbook.replace("\n", "\n\n");

        this.TOYear.setText(String.format(
                Locale.getDefault(), "%s/%s", courseParsed.TOYear, courseParsed.TOYearMax));
        this.TOAll.setText(String.format(
                Locale.getDefault(), "%s/%s", courseParsed.TOAll, courseParsed.TOAllMax));

        LayoutInflater li = LayoutInflater.from(getContext());

        StringJoiner sj = new StringJoiner(", ");
        if (!yearLevel.isEmpty())
            sj.add(yearLevel);
        if (!lecPrac.isEmpty())
            sj.add(lecPrac);
        if (!classification.isEmpty())
            sj.add(classification);
        if (!pointsTime.isEmpty())
            sj.add(pointsTime);

        addTextToLinLay(li, courseInfo, sj.toString());
        addPermissions(li);
        addTextToLinLay(li, courseInfo, timePlace);

        if (summary.isEmpty()){
            this.summary.setText("미입력");
            this.summary.setTextColor(0xFF757575);
        } else {
            this.summary.setText(summary);
        }

        if (textbook.isEmpty()){
            this.textbook.setText("미입력");
            this.textbook.setTextColor(0xFF757575);
        } else {
            this.textbook.setText(textbook);
        }

        if (professor.isEmpty()){
            this.professor.setText("TBA");
        } else {
            this.professor.setText(professor);
            this.professorDept.setText(professorDept);
        }

        addTextToLinLay(li, professorInfo, professorPhone);
        addTextToLinLay(li, professorInfo, professorEmail);
        addTextToLinLay(li, professorInfo, professorWeb);
        addTextToLinLay(li, professorInfo, counseling);

        this.rubricsType.setText(rubricsType);
        Collections.sort(oParsed.rubrics,
                Collections.reverseOrder((o1, o2) -> o1.second.compareTo(o2.second)));

        for (Pair<String, Integer> pair : rubrics){
            addTextToLinLay(li, rubricsKeys, pair.first);
            addTextToLinLay(li, rubricsValues, pair.second + "%");
        }

        if (rubrics.isEmpty())
            addTextToLinLay(li, rubricsKeys, "미입력", 0xFF757575);

        for (int i = 0; i < weeklyPlans.size(); i++)
            addWeeklyPlan(li, i + 1, weeklyPlans.get(i));
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

    private void onError(@NonNull ErrorInfo errorInfo) {
        if (errorInfo.throwable != null)
            ErrorReporter.INSTANCE.reportError(errorInfo.throwable);

        switch (errorInfo.type){
            case sessionExpired:
                ((MainActivity) getActivity()).goToLoginFrag(1);
                break;

            case timeout:
            case responseFailed:
                title.setText("연결 실패 - 인터넷 연결을 확인 후 앱을 재실행 해보세요");
                break;

            case parseFailed:
                title.setText("정보추출 실패 - 개발자에게 문의하세요");
                break;

            default:
                ((MainActivity) getActivity()).goToErrorFrag();
                break;
        }
    }

    private boolean checkError(ErrorInfo errorInfo){
        if (errorInfo != null){
            onError(errorInfo);
            return false;
        }

        return true;
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
