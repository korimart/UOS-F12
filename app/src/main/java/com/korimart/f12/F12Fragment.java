package com.korimart.f12;

import android.content.Context;
import android.content.SharedPreferences;
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

import java.util.Locale;

public class F12Fragment extends Fragment {
    private WiseViewModel wiseViewModel;
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

    private MainActivity mainActivity;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_grades, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewModelProvider vmp = new ViewModelProvider(mainActivity, new ViewModelProvider.NewInstanceFactory());
        wiseViewModel = vmp.get(WiseViewModel.class);
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

        f12ViewModel.onViewCreated(wiseViewModel, mainActivity);
    }

    private void setViewListeners() {
        originalButton.setOnClickListener((v) -> {
            mainActivity.goToOriginalXMLFrag(() -> mainActivity.goToF12Frag());
        });

        f12ViewModel.getCanMakeRequest().observe(this, b -> refreshButton.setEnabled(b));
        refreshButton.setOnClickListener((v) -> {
            f12ViewModel.fetch(wiseViewModel, mainActivity, true);
        });

        f12ViewModel.getNoPnp().observe(this, b -> {
            pnpSwitch.setChecked(b);
        });

        pnpSwitch.setOnCheckedChangeListener((v, b) -> {
            wiseViewModel.recalculateHiddenAvg(b);
            f12ViewModel.getNoPnp().setValue(b);

            SharedPreferences prefs = mainActivity.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("noPnp", b);
            editor.apply();
        });

        f12ViewModel.getHideCourse().observe(this, (b) -> hideCourse.setChecked(b));
        hideCourse.setOnCheckedChangeListener((v, b) ->
                courseNames.setVisibility(b ? View.INVISIBLE : View.VISIBLE));

        f12ViewModel.getHideStudent().observe(this, (b) -> hideStudent.setChecked(b));
        hideStudent.setOnCheckedChangeListener((v, b) ->
                loginInfo.setVisibility(b ? View.INVISIBLE : View.VISIBLE));

        f12ViewModel.getMessage().observe(this, (message) -> systemMessage.setText(message));
        f12ViewModel.getMessageColor().observe(this, color -> systemMessage.setTextColor(color));

        wiseViewModel.getF12().observe(this, wiseParsed -> {
            if (wiseParsed == null) return;

            F12Parser.Result parsed = (F12Parser.Result) wiseParsed;
            onSuccess(parsed);
        });
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
}
