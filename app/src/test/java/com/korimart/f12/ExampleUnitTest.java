package com.korimart.f12;

import org.junit.Test;
import org.w3c.dom.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    private F12Fragment frag = new F12Fragment();
    private Document doc;
    private String totPntString;
    private String totalMarksString;
    private String disclosedPntsString;
    private String totalAvgString;
    private DisclosedInfo info;
    private float disclosedMarksFloat;
    private float disclosedPntsWithoutPnp;
    private int hiddenPntsInt;
    private float totPntFloat;
    private float totalMarksFloat;
    private float totalAvgFloat;
    private float disclosedPntsFloat;
    private float hiddenAvgFloat;

    @Test
    public void mineDoc() {
        setUpMembers("mine doc", true);
        assertTrue(Math.abs(9f - totPntFloat) < 0.01);
        assertEquals(3, hiddenPntsInt);
        assertTrue(Math.abs(hiddenAvgFloat - 4) < 0.00001);
        assertTrue(Math.abs(4.33 - totalAvgFloat) < 0.01);
    }

    @Test
    public void nameOnlyDoc(){
        setUpMembers("Name only doc", true);
        assertTrue(Math.abs(6f - totPntFloat) < 0.01);
        assertEquals(3, hiddenPntsInt);
        assertTrue(Math.abs(hiddenAvgFloat - 4) < 0.00001);
        assertTrue(Math.abs(4.25 - totalAvgFloat) < 0.01);
    }

    @Test
    public void parkDoc(){
        // 과목명만 공개된 것이 한 과목
        // 0학점 패논패가 한 과목
        // 2학점짜리 과목 하나가 숨겨져 있는 상황
        setUpMembers("park doc", false);
        assertTrue(Math.abs(7f - totPntFloat) < 0.01);
        assertEquals(2, hiddenPntsInt);
        assertTrue(Math.abs(hiddenAvgFloat - 4) < 0.01);
        assertTrue(Math.abs(4.21 - totalAvgFloat) < 0.01);
    }

    @Test
    public void dotDoc(){
        // 3학점짜리 패논패가 한 과목과
        // 3학점짜리 B+가 공개되어 있고
        // 2학점짜리 B+, 3학점짜리 A+가 숨어있는 상황
        setUpMembers("dot doc", false);
        assertTrue(Math.abs(11f - totPntFloat) < 0.01);
        assertEquals(5, hiddenPntsInt);
        assertTrue(Math.abs(hiddenAvgFloat - 4.1) < 0.01);
        assertTrue(Math.abs(3.88 - totalAvgFloat) < 0.01);
    }

    @Test
    public void S0pntsDoc(){
        setUpMembers("S 0 pnts doc", false);
        assertTrue(Math.abs(6f - totPntFloat) < 0.01);
        assertEquals(3, hiddenPntsInt);
        assertTrue(Math.abs(hiddenAvgFloat - 4) < 0.01);
        assertTrue(Math.abs(4.25 - totalAvgFloat) < 0.01);
    }

    @Test
    public void S0pntsHiddenDoc(){
        setUpMembers("S 0 pnts hidden doc", false);
        assertTrue(Math.abs(9f - totPntFloat) < 0.01);
        assertEquals(3, hiddenPntsInt);
        assertTrue(Math.abs(hiddenAvgFloat - 4) < 0.01);
        assertTrue(Math.abs(4.33 - totalAvgFloat) < 0.01);
    }

    @Test
    public void S3pntsDoc(){
        setUpMembers("S 3 pnts doc", false);
        assertTrue(Math.abs(9f - totPntFloat) < 0.01);
        assertEquals(3, hiddenPntsInt);
        assertTrue(Math.abs(hiddenAvgFloat - 4) < 0.01);
        assertTrue(Math.abs(4.25 - totalAvgFloat) < 0.01);
    }

    @Test
    public void S3pntsHiddenDoc(){
        setUpMembers("S 3 pnts hidden doc", false);
        assertTrue(Math.abs(12f - totPntFloat) < 0.01);
        assertEquals(6, hiddenPntsInt);
        assertTrue(Math.abs(hiddenAvgFloat - 4) < 0.01);
        assertTrue(Math.abs(4.33 - totalAvgFloat) < 0.01);
    }

    private void setUpMembers(String doc, boolean noPnp){
        frag.makeBuilder();
        this.doc = loadDocument(doc);
        totPntString = F12Fragment.getContentByName(this.doc, "tot_pnt");
        totalMarksString = F12Fragment.getContentByName(this.doc, "tot_mrks");
        disclosedPntsString = F12Fragment.getContentByName(this.doc, "sum_pnt");
        totalAvgString = F12Fragment.getContentByName(this.doc, "avg_mrks");

        info = F12Fragment.getInfo(this.doc);

        disclosedMarksFloat = 0;
        disclosedPntsWithoutPnp = 0;
        for (DisclosedGrade dg : info.gradesForDisplay){
            disclosedMarksFloat += dg.getMarks();
            disclosedPntsWithoutPnp += dg.points;
        }

        totPntFloat = Float.parseFloat(totPntString);
        totalMarksFloat = Float.parseFloat(totalMarksString);
        totalAvgFloat = Float.parseFloat(totalAvgString);
        disclosedPntsFloat = Float.parseFloat(disclosedPntsString);

        hiddenPntsInt = F12Fragment.calculateHiddenPnts(totPntFloat, disclosedPntsFloat, info.nameOnlyCoursePnts);

        if (noPnp)
            hiddenAvgFloat = F12Fragment.calculateHiddenAvgNoPnp(totalMarksFloat, disclosedMarksFloat, hiddenPntsInt);
        else
            hiddenAvgFloat = F12Fragment.calculateHiddenAvg(totPntFloat, totalMarksFloat,
                    totalAvgFloat, disclosedMarksFloat, disclosedPntsWithoutPnp);
    }

    private Document loadDocument(String doc){
        try {
            File file = new File(
                    Paths.get(System.getProperty("user.dir"), "src/test/java/com/korimart/f12", doc)
                            .toString());
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            fis.close();
            return frag.getDocument(new String(data, "euc-kr"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}