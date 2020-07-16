package com.korimart.f12;

import org.junit.Test;
import org.w3c.dom.Document;

import static org.junit.Assert.*;

public class SyllabusParserTest {
    SyllabusUabOParser uabOParser = SyllabusUabOParser.INSTANCE;
    SyllabusUabWParser uabWParser = SyllabusUabWParser.INSTANCE;
    SyllabusOParser oParser = SyllabusOParser.INSTANCE;
    SyllabusWParser wParser = SyllabusWParser.INSTANCE;
    TestHelper testHelper = TestHelper.INSTANCE;
    XMLHelper xmlHelper = XMLHelper.INSTANCE;

    @Test
    public void testUabOParser(){
        byte[] rawDoc = testHelper.loadDocument("syllabusUabO.xml");
        Document doc = xmlHelper.getDocument(rawDoc);

        SyllabusUabOParser.Result result = uabOParser.parse(doc);

        assertNull(result.errorInfo);
        assertEquals("강의", result.lecPrac);
        assertEquals("4학년", result.yearLevel);
        assertEquals("전공선택(전공선택)", result.classification);
        assertEquals("3학점(3시간)", result.pointsTime);
        assertEquals("김민호", result.professor);
        assertEquals("컴퓨터과학부", result.professorDept);
        assertEquals("6490-2453", result.professorPhone);
        assertEquals("minhokim@uos.ac.kr", result.professorEmail);
        assertEquals("http://www.minho-kim.com", result.professorWeb);
        assertEquals("", result.counseling);
        assertEquals("상대평가", result.rubricsType);

        assertEquals(4, result.rubrics.size());

        assertEquals("출석", result.rubrics.get(0).first);
        assertEquals("수시과제", result.rubrics.get(1).first);
        assertEquals("중간과제", result.rubrics.get(2).first);
        assertEquals("기말과제", result.rubrics.get(3).first);

        assertEquals(10, (int) result.rubrics.get(0).second);
        assertEquals(10, (int) result.rubrics.get(1).second);
        assertEquals(40, (int) result.rubrics.get(2).second);
        assertEquals(40, (int) result.rubrics.get(3).second);
    }

    @Test
    public void testUabWParser(){
        byte[] rawDoc = testHelper.loadDocument("syllabusUabW.xml");
        Document doc = xmlHelper.getDocument(rawDoc);

        SyllabusUabWParser.Result result = uabWParser.parse(doc);

        assertNull(result.errorInfo);
        assertEquals(
                "-Shading language에 기초한 modern OpenGL을 이해하고 이를 사용하여 실시간 3차원 그래픽스 어플리케이션을 구현한다.\n" +
                "-3차원 렌더링 알고리즘을 이해한다.\n" +
                "-그래픽스 렌더링 파이프라인의 작동원리를 이해한다.\n" +
                "-각종 3차원 그래픽스 관련 도구들의 사용법을 익힌다.", result.summary);
        assertEquals(
                "[optional] “WebGL Programming Guide” (by Kouichi Matsuda and Rodger Lea)",
                result.textbook);
        assertEquals(16, result.weeklyPlans.size());
        assertEquals("introduction to JavaScript", result.weeklyPlans.get(0));
        assertEquals("Ch03 Drawing w2ith OpenGL (1/2)", result.weeklyPlans.get(3));
        assertEquals("Ch05 Viewing Transformations, Clipping, and Feedback", result.weeklyPlans.get(6));
        assertEquals("보강주간", result.weeklyPlans.get(11));
        assertEquals("OpenGL ES & WebGL", result.weeklyPlans.get(13));
        assertEquals("Final presentations", result.weeklyPlans.get(15));
    }

    @Test
    public void testOParser(){
        // 2019년 2학기 경영학부 회계원리
        byte[] rawDoc = testHelper.loadDocument("syllabusO.xml");
        Document doc = xmlHelper.getDocument(rawDoc);

        SyllabusOParser.Result result = oParser.parse(doc);

        assertNull(result.errorInfo);
    }

    @Test
    public void testWParser(){
        // 2019년 2학기 철학과 논리학
        byte[] rawDoc = testHelper.loadDocument("syllabusW.xml");
        Document doc = xmlHelper.getDocument(rawDoc);

        SyllabusWParser.Result result = wParser.parse(doc);

        assertNull(result.errorInfo);

        assertEquals(16, result.weeklyPlans.size());
        assertEquals(
                "과목소개 및 기본개념\n연역적 타당성과 건전성",
                result.weeklyPlans.get(0));
    }
}
