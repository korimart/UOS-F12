package com.korimart.f12;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
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

    private LinearLayout parent;
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
    private Button download;

    private LinearLayout online;
    private TextView onlineRatio;
    private TextView midtermOnline;
    private TextView finalOnline;
    private TextView quizOnline;

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

        parent = view.findViewById(R.id.syllabus_parent);
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
        download = view.findViewById(R.id.syllabus_download);

        online = view.findViewById(R.id.syllabus_online);
        onlineRatio = view.findViewById(R.id.syllabus_online_ratio);
        midtermOnline = view.findViewById(R.id.syllabus_online_midterm);
        finalOnline = view.findViewById(R.id.syllabus_online_final);
        quizOnline = view.findViewById(R.id.syllabus_online_quiz);

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

        StringJoiner sj = new StringJoiner(" / ");
        if (!syllabus.yearLevel.isEmpty())
            sj.add(syllabus.yearLevel);
        if (!syllabus.lecPrac.isEmpty())
            sj.add(syllabus.lecPrac);
        addTextToLinLay(li, courseInfo, sj.toString());

        sj = new StringJoiner(" / ");
        if (!syllabus.classification.isEmpty())
            sj.add(syllabus.classification);
        if (!syllabus.pointsTime.isEmpty())
            sj.add(syllabus.pointsTime);
        addTextToLinLay(li, courseInfo, sj.toString());

        addTextToLinLay(li, courseInfo, timePlace);
        addPermissions(li);

        if (!syllabus.filePath.isEmpty() && !syllabus.fileName.isEmpty()){
            download.setVisibility(View.VISIBLE);
            download.setOnClickListener(v -> {
                download.setEnabled(false);
                new Handler().postDelayed(() -> download.setEnabled(true), 1000);

                if (ActivityCompat.checkSelfPermission(
                        getContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                    Toast.makeText(getContext(), "권한 승인 후 다시시도 하세요", Toast.LENGTH_LONG).show();
                    return;
                }

                DownloadManager downManager =
                        (DownloadManager) getContext().getSystemService(Context.DOWNLOAD_SERVICE);
                Uri uri = Uri.parse(syllabus.filePath);

                DownloadManager.Request request = new DownloadManager.Request(uri);
                request.setTitle(syllabus.fileName);
                request.setDescription("다운로드 중");
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, syllabus.fileName);

                downManager.enqueue(request);
            });
        }

        if (!syllabus.onlineRate.isEmpty()){
            online.setVisibility(View.VISIBLE);
            onlineRatio.setText(
                    String.format("대면 %s / 비대면 %s", syllabus.offlineRate, syllabus.onlineRate));
            midtermOnline.setText(String.format("중간고사 : %s", getOnlineString(syllabus.midtermOnlineCode)));
            finalOnline.setText(String.format("기말고사 : %s", getOnlineString(syllabus.finalOnlineCode)));

            StringJoiner sj2 = new StringJoiner(" 및 ");
            if (syllabus.quizOnlineCode.isEmpty() && syllabus.quizOnlineCode2.isEmpty())
                sj2.add("미입력");
            else {
                if (!syllabus.quizOnlineCode.isEmpty())
                    sj2.add("비대면");
                if (!syllabus.quizOnlineCode2.isEmpty())
                    sj2.add("대면");
            }

            quizOnline.setText(String.format("퀴즈 : %s", sj2.toString()));
        }

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

    private String getOnlineString(String code){
        switch (code){
            case "01":
                return "대면";
            case "02":
                return "비대면";
            case "03":
                return "없음";
            default:
                return "미입력";
        }
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
        download.setVisibility(View.GONE);
        title.setText("가져오는 중...");
        online.setVisibility(View.GONE);
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
