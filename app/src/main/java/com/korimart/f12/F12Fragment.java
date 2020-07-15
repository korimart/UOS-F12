package com.korimart.f12;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.time.LocalDateTime;
import java.util.Locale;

public class F12Fragment extends Fragment {
    private MainViewModel mainViewModel;
    private F12ViewModel f12ViewModel;
    private TextView loginInfo;
    private TextView totPnt;
    private TextView hiddenPnts;
    private TextView hiddenAvg;
    private TextView totalAvg;
    private TextView systemMessage;
    private Button refreshButton;
    private Button originalButton;
    private LinearLayout courseNames;
    private LinearLayout letterGrades;
    private LinearLayout points;
    private Switch pnpSwitch;
    private Switch hideCourse;
    private Switch hideStudent;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_grades, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewModelProvider vmp = new ViewModelProvider(getActivity(), new ViewModelProvider.NewInstanceFactory());
        mainViewModel = vmp.get(MainViewModel.class);
        f12ViewModel = vmp.get(F12ViewModel.class);

        loginInfo = view.findViewById(R.id.f12_strMyShreg);
        totPnt = view.findViewById(R.id.f12_totalPnts);
        hiddenPnts = view.findViewById(R.id.f12_hiddenPnts);
        hiddenAvg = view.findViewById(R.id.f12_hiddenAvg);
        totalAvg = view.findViewById(R.id.f12_totalAvg);
        systemMessage = view.findViewById(R.id.f12_systemMessage);
        refreshButton = view.findViewById(R.id.f12_refreshButton);
        originalButton = view.findViewById(R.id.f12_showOriginal);
        courseNames = view.findViewById(R.id.f12_courseNames);
        letterGrades = view.findViewById(R.id.f12_letterGrades);
        points = view.findViewById(R.id.f12_points);
        pnpSwitch = view.findViewById(R.id.f12_pnpSwitch);
        hideCourse = view.findViewById(R.id.f12_hideCourse);
        hideStudent = view.findViewById(R.id.f12_hideStudent);

        setViewListeners();
        fetch(false);
    }

    private void setViewListeners() {
        originalButton.setOnClickListener((v) -> {
            MainActivity ma = (MainActivity) getActivity();
            ma.goToOriginalXMLFrag(() -> ma.goToF12Frag());
        });

        refreshButton.setOnClickListener((v) -> {
            refreshButton.setEnabled(false);
            systemMessage.setTextColor(0xFF000000);
            f12ViewModel.getMessage().setValue("가져오는 중...");
            fetch(true);
        });

        mainViewModel.getNoPnp().observe(this, (b) -> pnpSwitch.setChecked(b));

        pnpSwitch.setOnCheckedChangeListener((v, b) -> {
            f12ViewModel.recalculateHiddenAvg(b);
            mainViewModel.getNoPnp().setValue(b);

            if (f12ViewModel.getF12Parsed() != null)
                setHiddenAvg(f12ViewModel.getF12Parsed().hiddenAvg);
        });

        f12ViewModel.getHideCourse().observe(this, (b) -> hideCourse.setChecked(b));
        hideCourse.setOnCheckedChangeListener((v, b) ->
                courseNames.setVisibility(b ? View.INVISIBLE : View.VISIBLE));

        f12ViewModel.getHideStudent().observe(this, (b) -> hideStudent.setChecked(b));
        hideStudent.setOnCheckedChangeListener((v, b) ->
                loginInfo.setVisibility(b ? View.INVISIBLE : View.VISIBLE));

        f12ViewModel.getMessage().observe(this, (message) -> systemMessage.setText(message));

        f12ViewModel.getF12Ready().observe(this, this::onF12Ready);
    }

    private void onF12Ready(Boolean ready){
        if (ready == null || !ready) return;

        f12ViewModel.getF12Ready().setValue(false);
        whenDone();

        if (f12ViewModel.getF12InfoFetched().errorInfo != null){
            onError(f12ViewModel.getF12InfoFetched().errorInfo);
            return;
        }

        if (f12ViewModel.getF12InfoParsed().errorInfo != null){
            onError(f12ViewModel.getF12InfoParsed().errorInfo);
            return;
        }

        if (f12ViewModel.getF12Fetched().errorInfo != null){
            onError(f12ViewModel.getF12Fetched().errorInfo);
            return;
        }

        if (f12ViewModel.getF12Parsed().errorInfo != null){
            onError(f12ViewModel.getF12Parsed().errorInfo);
            return;
        }

        onSuccess(f12ViewModel.getF12Parsed());
    }

    private void fetch(boolean refetch){
        f12ViewModel.fetchAndParse(pnpSwitch.isChecked(), refetch)
                .thenRun(() -> f12ViewModel.getF12Ready().postValue(true));
    }

    private void onSuccess(F12Parser.Result parsed) {
        // 2 = 1 (description) + 1 (space)
        courseNames.removeViews(2, courseNames.getChildCount() - 2);
        letterGrades.removeViews(2, letterGrades.getChildCount() - 2);
        points.removeViews(2, points.getChildCount() - 2);

        if (parsed.disclosedInfo != null){
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            for (DisclosedGrade dg : parsed.disclosedInfo.gradesForDisplay){
                TextView courseName = new TextView(getContext());
                courseName.setText(dg.course);
                courseName.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                courseNames.addView(courseName);

                TextView letterGrade = new TextView(getContext());
                letterGrade.setText(dg.letterGrade);
                letterGrade.setLayoutParams(lp);
                letterGrades.addView(letterGrade);

                TextView point = new TextView(getContext());
                point.setText(String.valueOf((int) dg.points));
                point.setLayoutParams(lp);
                points.addView(point);
            }
        }

        loginInfo.setText(parsed.studentInfo);
        totPnt.setText(String.valueOf(parsed.totalPnts));
        hiddenPnts.setText(String.valueOf(parsed.hiddenPnts));
        totalAvg.setText(String.format(Locale.US, "%.2f", parsed.totalAvg));

        setHiddenAvg(parsed.hiddenAvg);

        LocalDateTime dt = LocalDateTime.now();
        f12ViewModel.getMessage().setValue(
                String.format(
                        Locale.getDefault(),
                        "마지막 새로고침 : %d-%d-%d %d시 %d분 %d초",
                        dt.getYear(), dt.getMonthValue(),
                        dt.getDayOfMonth(), dt.getHour(),
                        dt.getMinute(), dt.getSecond())
        );
    }

    private void setHiddenAvg(float hiddenAvgFloat){
        boolean noPnp = pnpSwitch.isChecked();
        String avgText;
        if (noPnp)
            avgText = String.format(Locale.US, "%.2f", hiddenAvgFloat);
        else
            avgText = String.format(Locale.US, "%.1f", hiddenAvgFloat);
        hiddenAvg.setText(avgText);
    }

    private void onError(ErrorInfo errorInfo){
        if (errorInfo.exception != null)
            ErrorReporter.INSTANCE.reportError(errorInfo.exception);

        switch (errorInfo.type){
            case sessionExpired:
                ((MainActivity) getActivity()).goToLoginFrag(0);
                break;

            case timeout:
            case responseFailed:
                f12ViewModel.getMessage().setValue("성적 불러오기 실패");
                systemMessage.setTextColor(0xFFFF0000);
                refreshButton.setEnabled(true);
                break;

            case parseFailed:
                f12ViewModel.getMessage().setValue("와이즈 시스템 방식이 변경된 듯 (F12가 막혔을 수 있음)");
                systemMessage.setTextColor(0xFFFF0000);
                refreshButton.setEnabled(true);
                break;

            case noOneDisclosedGrade:
                f12ViewModel.getMessage().setValue("성적이 하나도 안 떠서 볼 수가 없음");
                systemMessage.setTextColor(0xFFFF0000);
                refreshButton.setEnabled(true);
                break;

            default:
                ((MainActivity) getActivity()).goToErrorFrag();
                break;
        }
    }

    private void whenDone(){
        refreshButton.setEnabled(true);
    }
}
