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

public class SyllabusFragment extends Fragment {
    private SyllabusViewModel syllabusViewModel;
    private CoursesViewModel coursesViewModel;
    private CourseListParser.CourseInfo courseParsed;
    private String timePlace;
    private int position;

    private TextView title;
    private TextView titleClassNumber;
    private LinearLayout courseInfo;
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
        coursesViewModel = vmp.get(CoursesViewModel.class);
        syllabusViewModel = vmp.get(SyllabusViewModel.class);

        position = getArguments().getInt("position");
        timePlace = getArguments().getString("timePlace");

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

        courseParsed = coursesViewModel.getFilteredCourses().getValue().get(position);

        syllabusViewModel.getOwReady().observe(this, this::onOwReady);
        fetch();
    }

    private void fetch(){
        syllabusViewModel.fetchAndParseSyllabus(
                courseParsed.schoolYear,
                courseParsed.semester,
                courseParsed.curriNumber,
                courseParsed.classNumber)
                .thenRun(() -> syllabusViewModel.getOwReady().postValue(true));
    }

    private void onOwReady(Boolean ready){
        if (ready == null || !ready) return;

        syllabusViewModel.getOwReady().setValue(false);

        if (!checkError(syllabusViewModel.getoFetched().errorInfo))
            return;

        if (!checkError(syllabusViewModel.getoParsed().errorInfo))
            return;

        if (!checkError(syllabusViewModel.getwFetched().errorInfo))
            return;

        if (!checkError(syllabusViewModel.getwParsed().errorInfo))
            return;

        onSuccess();
    }

    private void onSuccess() {
        title.setText(courseParsed.name);
        titleClassNumber.setText(courseParsed.classNumber + "분반");

        SyllabusUabOParser.Result oParsed = syllabusViewModel.getoParsed();
        SyllabusUabWParser.Result wParsed = syllabusViewModel.getwParsed();

        LayoutInflater li = LayoutInflater.from(getContext());

        addTextToLinLay(li, courseInfo, oParsed.lecPrac);
        addTextToLinLay(li, courseInfo, oParsed.classification);
        addTextToLinLay(li, courseInfo, oParsed.pointsTime);
        addPermissions(li);
        addTextToLinLay(li, courseInfo, timePlace);

        if (wParsed.summary.isEmpty()){
            this.summary.setText("미입력");
        } else {
            this.summary.setText(wParsed.summary);
        }

        if (wParsed.textbook.isEmpty()){
            textbook.setText("미입력");
        } else {
            textbook.setText(wParsed.textbook);
        }

        if (oParsed.professor.isEmpty()){
            professor.setText("TBA");
        } else {
            professor.setText(oParsed.professor);
            professorDept.setText(oParsed.professorDept);
        }

        addTextToLinLay(li, professorInfo, oParsed.professorPhone);
        addTextToLinLay(li, professorInfo, oParsed.professorEmail);
        addTextToLinLay(li, professorInfo, oParsed.professorWeb);
        addTextToLinLay(li, professorInfo, oParsed.counseling);

        rubricsType.setText(oParsed.rubricsType);
        Collections.sort(oParsed.rubrics,
                Collections.reverseOrder((o1, o2) -> o1.second.compareTo(o2.second)));

        for (Pair<String, Integer> pair : oParsed.rubrics){
            addTextToLinLay(li, rubricsKeys, pair.first);
            addTextToLinLay(li, rubricsValues, pair.second + "%");
        }

        if (oParsed.rubrics.isEmpty())
            addTextToLinLay(li, rubricsKeys, "미입력");

        for (int i = 0; i < wParsed.weeklyPlans.size(); i++)
            addWeeklyPlan(li, i + 1, wParsed.weeklyPlans.get(i));
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
        if (errorInfo.exception != null)
            ErrorReporter.INSTANCE.reportError(errorInfo.exception);

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
        if (!childString.isEmpty()){
            TextView child = (TextView) li.inflate(R.layout.item_syllabus_text, parent, false);

            child.setText(childString);
            parent.addView(child);
        }
    }

    private void addWeeklyPlan(LayoutInflater li, int weekInt, String plan){
        LinearLayout linLay =
                (LinearLayout) li.inflate(R.layout.item_syllabus_weekly_plan, weeklyPlans, false);

        TextView week = linLay.findViewById(R.id.syllabus_plan_week);
        TextView text = linLay.findViewById(R.id.syllabus_plan_text);

        week.setText(weekInt + "주차");

        if (plan.isEmpty())
            text.setText("미입력");
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
