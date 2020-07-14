package com.korimart.f12;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.w3c.dom.Document;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class F12UnitTest {
    TestHelper testHelper = TestHelper.INSTANCE;
    F12Parser f12Parser = new F12Parser();
    XMLHelper xmlHelper = XMLHelper.INSTANCE;

    @Test
    public void mineDoc() {
        byte[] rawXML = testHelper.loadDocument("mineDoc.xml");
        Document doc = xmlHelper.getDocument(rawXML);
        f12Parser.setNoPnp(true);
        F12Parser.Result result = f12Parser.parse(doc);

        assertEquals(9, result.totalPnts);
        assertEquals(3, result.hiddenPnts);
        assertEquals(4f, result.hiddenAvg, 0.00001);
        assertEquals(4.33f, result.totalAvg, 0.01);
    }

    @Test
    public void nameOnlyDoc(){
        byte[] rawXML = testHelper.loadDocument("nameOnlyDoc.xml");
        Document doc = xmlHelper.getDocument(rawXML);
        f12Parser.setNoPnp(true);
        F12Parser.Result result = f12Parser.parse(doc);

        assertEquals(6, result.totalPnts);
        assertEquals(3, result.hiddenPnts);
        assertEquals(4f, result.hiddenAvg, 0.00001);
        assertEquals(4.25f, result.totalAvg, 0.01);
    }

    @Test
    public void parkDoc(){
        // 과목명만 공개된 것이 한 과목
        // 0학점 패논패가 한 과목
        // 2학점짜리 과목 하나가 숨겨져 있는 상황
        byte[] rawXML = testHelper.loadDocument("parkDoc.xml");
        Document doc = xmlHelper.getDocument(rawXML);
        f12Parser.setNoPnp(false);
        F12Parser.Result result = f12Parser.parse(doc);

        assertEquals(7, result.totalPnts);
        assertEquals(2, result.hiddenPnts);
        assertEquals(4f, result.hiddenAvg, 0.00001);
        assertEquals(4.21f, result.totalAvg, 0.01);
    }

    @Test
    public void dotDoc(){
        // 3학점짜리 패논패가 한 과목과
        // 3학점짜리 B+가 공개되어 있고
        // 2학점짜리 B+, 3학점짜리 A+가 숨어있는 상황
        byte[] rawXML = testHelper.loadDocument("dotDoc.xml");
        Document doc = xmlHelper.getDocument(rawXML);
        f12Parser.setNoPnp(false);
        F12Parser.Result result = f12Parser.parse(doc);

        assertEquals(11, result.totalPnts);
        assertEquals(5, result.hiddenPnts);
        assertEquals(4.1f, result.hiddenAvg, 0.00001);
        assertEquals(3.88f, result.totalAvg, 0.01);
    }

    @Test
    public void S0pntsDoc(){
        byte[] rawXML = testHelper.loadDocument("S0PntsDoc.xml");
        Document doc = xmlHelper.getDocument(rawXML);
        f12Parser.setNoPnp(false);
        F12Parser.Result result = f12Parser.parse(doc);

        assertEquals(6, result.totalPnts);
        assertEquals(3, result.hiddenPnts);
        assertEquals(4f, result.hiddenAvg, 0.00001);
        assertEquals(4.25f, result.totalAvg, 0.01);
    }

    @Test
    public void S0pntsHiddenDoc(){
        byte[] rawXML = testHelper.loadDocument("S0PntsHiddenDoc.xml");
        Document doc = xmlHelper.getDocument(rawXML);
        f12Parser.setNoPnp(false);
        F12Parser.Result result = f12Parser.parse(doc);

        assertEquals(9, result.totalPnts);
        assertEquals(3, result.hiddenPnts);
        assertEquals(4f, result.hiddenAvg, 0.00001);
        assertEquals(4.33f, result.totalAvg, 0.01);
    }

    @Test
    public void S3pntsDoc(){
        byte[] rawXML = testHelper.loadDocument("S3PntsDoc.xml");
        Document doc = xmlHelper.getDocument(rawXML);
        f12Parser.setNoPnp(false);
        F12Parser.Result result = f12Parser.parse(doc);

        assertEquals(9, result.totalPnts);
        assertEquals(3, result.hiddenPnts);
        assertEquals(4f, result.hiddenAvg, 0.0001f);
        assertEquals(4.25f, result.totalAvg, 0.01f);
    }

    @Test
    public void S3pntsHiddenDoc(){
        byte[] rawXML = testHelper.loadDocument("S3PntsHiddenDoc.xml");
        Document doc = xmlHelper.getDocument(rawXML);
        f12Parser.setNoPnp(false);
        F12Parser.Result result = f12Parser.parse(doc);

        assertEquals(12, result.totalPnts);
        assertEquals(6, result.hiddenPnts);
        assertEquals(4f, result.hiddenAvg, 0.00001);
        assertEquals(4.33f, result.totalAvg, 0.01);
    }
}