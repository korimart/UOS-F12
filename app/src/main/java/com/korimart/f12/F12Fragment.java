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
import java.time.LocalDateTime;

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
    private Switch pnpSwitch;

    public F12Fragment(MainViewModel mainViewModel, F12ViewModel f12ViewModel){
        this.mainViewModel = mainViewModel;
        this.f12ViewModel = f12ViewModel;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_grades, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loginInfo = view.findViewById(R.id.grades_strMyShreg);
        totPnt = view.findViewById(R.id.grades_tot_pnt);
        hiddenPnts = view.findViewById(R.id.grades_hiddenPnts);
        hiddenAvg = view.findViewById(R.id.grades_hiddenAvg);
        totalAvg = view.findViewById(R.id.grades_avg_mrks);
        systemMessage = view.findViewById(R.id.grades_systemMessage);
        refreshButton = view.findViewById(R.id.grades_refreshButton);
        originalButton = view.findViewById(R.id.grades_showOriginal);
        courseNames = view.findViewById(R.id.grades_courseNames);
        letterGrades = view.findViewById(R.id.grades_letterGrades);
        pnpSwitch = view.findViewById(R.id.grades_pnpSwitch);

        setViewListeners();

        if (((MainActivity) getActivity()).isFirstFetch()){
            fetchF12();
            ((MainActivity) getActivity()).setFirstFetch(false);
        }
    }

    private void setViewListeners() {
        originalButton.setOnClickListener((v) -> ((MainActivity) getActivity()).goToOriginalXMLFrag());

        refreshButton.setOnClickListener((v) -> {
            refreshButton.setEnabled(false);
            systemMessage.setTextColor(0xFF000000);
            systemMessage.setText("가져오는 중...");
            fetchF12();
        });

        // 초기화를 먼저해야 무한 루프를 돌지 않음
        mainViewModel.getNoPnp().observe(this, (b) -> pnpSwitch.setChecked(b));
        pnpSwitch.setOnCheckedChangeListener((v, b) -> {
            f12ViewModel.recalculateHiddenAvg(pnpSwitch.isChecked());
            mainViewModel.getNoPnp().setValue(b);
        });

        f12ViewModel.getMessage().observe(this, (message) -> systemMessage.setText(message));

        f12ViewModel.getDisclosedInfo().observe(this, (info) -> {
            courseNames.removeAllViews();
            letterGrades.removeAllViews();

            if (info == null) return;

            for (DisclosedGrade dg : info.gradesForDisplay){
                TextView courseName = new TextView(getContext());
                courseName.setText(dg.course);
                courseName.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                courseNames.addView(courseName);

                TextView letterGrade = new TextView(getContext());
                letterGrade.setText(dg.letterGrade);
                letterGrade.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                letterGrades.addView(letterGrade);
            }
        });

        f12ViewModel.getStudentInfo().observe(this, (info) -> loginInfo.setText(info));
        f12ViewModel.getTotalPnts().observe(this, (pnts) -> totPnt.setText(String.valueOf(pnts)));
        f12ViewModel.getHiddenPnts().observe(this, (pnts) -> hiddenPnts.setText(String.valueOf(pnts)));
        f12ViewModel.getHiddenAvg().observe(this, (avg) -> {
            boolean noPnp = pnpSwitch.isChecked();
            String avgText;
            if (noPnp)
                avgText = String.format("%.2f", avg);
            else
                avgText = String.format("%.1f", avg);
            hiddenAvg.setText(avgText);
        });
        f12ViewModel.getTotalAvg().observe(this, (avg) -> totalAvg.setText(String.format("%.2f", avg)));
    }

    public void fetchF12(){
        f12ViewModel.fetchF12(pnpSwitch.isChecked(), this::onSuccess, this::onError, this::anyway);
    }

    /**
     * This is a callback from a background thread
     * make sure to run on the UI thread if doing UI stuff
     */
    private void onSuccess(){
        getActivity().runOnUiThread(() -> {
            LocalDateTime dt = LocalDateTime.now();
            f12ViewModel.getMessage().setValue(
                String.format("마지막 새로고침 : %d-%d-%d %d시 %d분 %d초",
                        dt.getYear(), dt.getMonthValue(),
                        dt.getDayOfMonth(), dt.getHour(),
                        dt.getMinute(), dt.getSecond())
            );
        });
    }

    /**
     * This is a callback from a background thread
     * make sure to run on the UI thread if doing UI stuff
     * @param errorInfo
     */
    private void onError(F12Fetcher.ErrorInfo errorInfo){
        getActivity().runOnUiThread(() -> {
            switch (errorInfo.errorType){
                case "sessionExpired":
                    ((MainActivity) getActivity()).goToLoginFrag();
                    break;

                case "infoResponseFailed":
                case "f12ResponseFailed":
                    systemMessage.setText("성적 불러오기 실패");
                    systemMessage.setTextColor(0xFFFF0000);
                    refreshButton.setEnabled(true);
                    break;

                case "noStudentInfo":
                    systemMessage.setText("와이즈 시스템 방식이 변경된 듯 (F12가 막혔을 수 있음)");
                    systemMessage.setTextColor(0xFFFF0000);
                    refreshButton.setEnabled(true);
                    break;

                case "noDisclosedInfo":
                    systemMessage.setText("성적이 하나도 안 떠서 볼 수가 없음");
                    systemMessage.setTextColor(0xFFFF0000);
                    refreshButton.setEnabled(true);
                    break;

                default:
                    ((MainActivity) getActivity()).goToErrorFrag(errorInfo.callStack);
                    break;
            }
        });
    }

    /**
     * This is a callback from a background thread
     * make sure to run on the UI thread if doing UI stuff
     */
    private void anyway(){
        refreshButton.post(() -> refreshButton.setEnabled(true));
    }
}
