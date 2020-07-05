package com.korimart.f12;

import android.os.Environment;

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
    private GradesFragment frag = new GradesFragment();
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
        setUpMembers("mine doc");
        assertTrue(Math.abs(9f - totPntFloat) < 0.01);
        assertEquals(3, hiddenPntsInt);
        assertTrue(Math.abs(hiddenAvgFloat - 4) < 0.02);
        assertTrue(Math.abs(4.33 - totalAvgFloat) < 0.01);
    }

    @Test
    public void nameOnlyDoc(){
        setUpMembers("Name only doc");
        assertTrue(Math.abs(6f - totPntFloat) < 0.01);
        assertEquals(3, hiddenPntsInt);
        assertTrue(Math.abs(hiddenAvgFloat - 4) < 0.02);
        assertTrue(Math.abs(4.25 - totalAvgFloat) < 0.01);
    }

    @Test
    public void S0pntsDoc(){
        setUpMembers("S 0 pnts doc");
        assertTrue(Math.abs(6f - totPntFloat) < 0.01);
        assertEquals(3, hiddenPntsInt);
        assertTrue(Math.abs(hiddenAvgFloat - 4) < 0.02);
        assertTrue(Math.abs(4.25 - totalAvgFloat) < 0.01);
    }

    @Test
    public void S0pntsHiddenDoc(){
        setUpMembers("S 0 pnts hidden doc");
        assertTrue(Math.abs(9f - totPntFloat) < 0.01);
        assertEquals(3, hiddenPntsInt);
        assertTrue(Math.abs(hiddenAvgFloat - 4) < 0.02);
        assertTrue(Math.abs(4.33 - totalAvgFloat) < 0.01);
    }

    @Test
    public void S3pntsDoc(){
        setUpMembers("S 3 pnts doc");
        assertTrue(Math.abs(9f - totPntFloat) < 0.01);
        assertEquals(3, hiddenPntsInt);
        assertTrue(Math.abs(hiddenAvgFloat - 4) < 0.02);
        assertTrue(Math.abs(4.25 - totalAvgFloat) < 0.01);
    }

    @Test
    public void S3pntsHiddenDoc(){
        setUpMembers("S 3 pnts hidden doc");
        assertTrue(Math.abs(12f - totPntFloat) < 0.01);
        assertEquals(6, hiddenPntsInt);
        assertTrue(Math.abs(hiddenAvgFloat - 4) < 0.02);
        assertTrue(Math.abs(4.33 - totalAvgFloat) < 0.01);
    }

    private void setUpMembers(String doc){
        frag.init();
        this.doc = loadDocument(doc);
        totPntString = GradesFragment.getContentByName(this.doc, "tot_pnt");
        totalMarksString = GradesFragment.getContentByName(this.doc, "tot_mrks");
        disclosedPntsString = GradesFragment.getContentByName(this.doc, "sum_pnt");
        totalAvgString = GradesFragment.getContentByName(this.doc, "avg_mrks");

        info = GradesFragment.getInfo(this.doc);

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

        hiddenPntsInt = (int) (totPntFloat - disclosedPntsFloat + info.nameOnlyCoursePnts);
        hiddenAvgFloat = GradesFragment.calculateHiddenAvg(totPntFloat, totalMarksFloat,
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