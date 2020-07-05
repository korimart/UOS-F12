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

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class GradesFragment extends Fragment {
    private String f12URL = "https://wise.uos.ac.kr/uosdoc/ugd.UgdOtcmInq.do";
    private String smtParams = "_dept_authDept=auth&_code_smtList=CMN31&&_COMMAND_=onload&&_XML_=XML&_strMenuId=stud00320&";
    private String f12Params = "strSchYear=%d&strSmtCd=%s&strStudId=123123&strDiv=2&&_COMMAND_=list&&_XML_=XML&_strMenuId=stud00320&";
    private DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    private DocumentBuilder builder;

    private TextView loginInfo;
    private TextView totPnt;
    private TextView hiddenPnts;
    private TextView hiddenAvg;
    private TextView totalAvg;
    private TextView systemMessage;
    private Button refreshButton;
    private LinearLayout courseNames;
    private LinearLayout letterGrades;

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
        courseNames = view.findViewById(R.id.grades_courseNames);
        letterGrades = view.findViewById(R.id.grades_letterGrades);

        refreshButton.setOnClickListener((v) -> {
            (new Thread(this::fetchGrades)).start();
        });

        init();

        (new Thread(this::fetchGrades)).start();
    }

    public void init() {
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException ignore) {
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void fetchGrades(){
        try {
            fetchGradesWithErrors();
        } catch (Exception e){
            getActivity().runOnUiThread(() -> {
                StackTraceElement[] stes = e.getStackTrace();
                StringBuilder sb = new StringBuilder();
                sb.append(e.toString());
                for (StackTraceElement ste : stes){
                    sb.append(ste.toString());
                    sb.append('\n');
                }

                ((MainActivity) getActivity()).setErrorText(sb.toString());
                ((MainActivity) getActivity()).goToErrorFrag();
            });
        }
    }

    private void fetchGradesWithErrors() {
        systemMessage.post(() -> {
            refreshButton.setEnabled(false);
            systemMessage.setTextColor(0xFF000000);
            systemMessage.setText("가져오는 중...");
            courseNames.removeAllViews();
            letterGrades.removeAllViews();
        });

        String response = null;
        String smtResponse = null;
        try {
            smtResponse = WebService.sendPost(f12URL, smtParams);
        } catch (Exception ignore) {
        }

        Document smtDoc = getDocument(smtResponse);
        if (smtDoc == null){
            systemMessage.post(() -> {
                systemMessage.setText("성적 불러오기 실패");
                systemMessage.setTextColor(0xFFFF0000);
                refreshButton.setEnabled(true);
            });
            return;
        }

        String smtString = getContentByName(smtDoc, "strSmt");
        LocalDateTime dt = LocalDateTime.now();
        int year = getSchoolYear(dt);

        try {
            response = WebService.sendPost(f12URL, String.format(f12Params, year, smtString));
        } catch (Exception ignore) {
        }

        Document doc = getDocument(response);

        if (doc == null){
            systemMessage.post(() -> {
                systemMessage.setText("성적 불러오기 실패");
                systemMessage.setTextColor(0xFFFF0000);
                refreshButton.setEnabled(true);
            });
            return;
        }

        DisclosedInfo info = getInfo(doc);
        if (info == null){
            systemMessage.post(() -> {
                systemMessage.setText("성적이 하나도 안 떠서 볼 수가 없음");
                systemMessage.setTextColor(0xFFFF0000);
            });
            return;
        }

        float disclosedMarksFloat = 0;
        float disclosedPntsWithoutPnp = 0;
        for (DisclosedGrade dg : info.gradesForDisplay){
            disclosedMarksFloat += dg.getMarks();
            disclosedPntsWithoutPnp += dg.points;
        }

        String loginInfoString = getContentByName(doc, "strMyShreg");
        String totPntString = getContentByName(doc, "tot_pnt");
        String totalMarksString = getContentByName(doc, "tot_mrks");
        String disclosedPntsString = getContentByName(doc, "sum_pnt");
        String totalAvgString = getContentByName(doc, "avg_mrks");

        if (loginInfoString == null || totPntString == null
                || totalMarksString == null || disclosedPntsString == null){
            systemMessage.post(() -> {
                systemMessage.setText("와이즈 시스템 방식이 변경된 듯 (F12가 막혔을 수 있음)");
                systemMessage.setTextColor(0xFFFF0000);
                refreshButton.setEnabled(true);
            });
            return;
        }

        float totPntFloat = Float.parseFloat(totPntString);
        float totalMarksFloat = Float.parseFloat(totalMarksString);
        float totalAvgFloat = Float.parseFloat(totalAvgString);
        float disclosedPntsFloat = Float.parseFloat(disclosedPntsString);
        String hiddenPntString = String.valueOf(calculateHiddenPnts(totPntFloat, disclosedPntsFloat, info.nameOnlyCoursePnts));
        float hiddenAvgFloat = calculateHiddenAvg(totPntFloat, totalMarksFloat,
                totalAvgFloat, disclosedMarksFloat, disclosedPntsWithoutPnp);
        String hiddenAvgString = String.format("%.2f", hiddenAvgFloat);

        systemMessage.post(() -> {
            hiddenPnts.setText(hiddenPntString);
            loginInfo.setText(loginInfoString);
            hiddenAvg.setText(hiddenAvgString);
            totPnt.setText(totPntString);
            totalAvg.setText(totalAvgString);

            systemMessage.setText(
                    String.format("마지막 업데이트 : %d-%d-%d %d시 %d분 %d초",
                            dt.getYear(), dt.getMonthValue(),
                            dt.getDayOfMonth(), dt.getHour(),
                            dt.getMinute(), dt.getSecond())
            );

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
            refreshButton.setEnabled(true);
        });
    }

    public static int calculateHiddenPnts(float totPntFloat, float disclosedPntsFloat, float nameOnlyCoursePnts){
        return (int) (totPntFloat - disclosedPntsFloat + nameOnlyCoursePnts);
    }

    public static float calculateHiddenAvg(float totalPntsWithPnp, float totalMarksFloat, float totalAvgFloat,
                                     float disclosedMarksFloat, float disclosedPntsWithoutPnp) {
        // pass non-pass
        float pnpPntFloat = totalPntsWithPnp - totalMarksFloat / totalAvgFloat;
        float hiddenPntFloat = totalPntsWithPnp - disclosedPntsWithoutPnp - pnpPntFloat;
        return hiddenPntFloat == 0.0f ? 0.0f :
                (totalMarksFloat - disclosedMarksFloat) / hiddenPntFloat;
    }

    private int getSchoolYear(LocalDateTime dt) {
        int month = dt.getMonthValue();
        if (month < 5) return dt.getYear() - 1;
        return dt.getYear();
    }

    public static String getContentByName(Document doc, String name){
        NodeList nl = doc.getElementsByTagName(name);
        Node n = nl.item(0);

        if (n == null) return null;

        Node nc = n.getFirstChild();
        return nc.getNodeValue();
    }

    public static DisclosedInfo getInfo(Document doc){
        DisclosedInfo ret = new DisclosedInfo();
        ret.gradesForDisplay = new ArrayList<>();
        NodeList points = doc.getElementsByTagName("pnt");
        NodeList grades = doc.getElementsByTagName("mrks");
        NodeList courses = doc.getElementsByTagName("curi_nm");
        NodeList letterGrades = doc.getElementsByTagName("conv_grade");

        if (points.getLength() == 0) return null;

        for (int i = 0; i < points.getLength(); i++){
            DisclosedGrade dg = new DisclosedGrade();
            dg.course = courses.item(i).getFirstChild().getNodeValue().trim();
            Node letterGradeNode = letterGrades.item(i).getFirstChild();
            // 과목명만 보이게 해놓고 성적 입력 안 해놓은 경우 ㅋㅋ
            if (letterGradeNode == null){
                ret.nameOnlyCoursePnts += Float.parseFloat(points.item(i).getFirstChild().getNodeValue());
                continue;
            }

            dg.letterGrade = letterGradeNode.getNodeValue().trim();
            if (dg.letterGrade.equals("S") || dg.letterGrade.equals("NS")){
                dg.grade = 0;
                dg.points = 0;
            }
            else {
                dg.grade = Float.parseFloat(grades.item(i).getFirstChild().getNodeValue());
                dg.points = Float.parseFloat(points.item(i).getFirstChild().getNodeValue());
            }

            ret.gradesForDisplay.add(dg);
        }

        return ret;
    }

    public Document getDocument(String xml){
        StringBuilder xmlStringBuilder = new StringBuilder();
        xmlStringBuilder.append(xml);
        ByteArrayInputStream input = null;

        try {
            input = new ByteArrayInputStream(xmlStringBuilder.toString().getBytes("euc-kr"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Document doc = null;
        try {
            doc = builder.parse(input);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return doc;
    }
}

class DisclosedGrade {
    public String course;
    public String letterGrade;
    public float points;
    public float grade;

    public float getMarks(){
        return points * grade;
    }
}

class DisclosedInfo {
    public List<DisclosedGrade> gradesForDisplay;
    public float nameOnlyCoursePnts;
}
