package com.korimart.f12;

import org.junit.Test;
import org.w3c.dom.Document;

import static org.junit.Assert.*;

public class SyllabusOParserTest {
    SyllabusOParser parser = SyllabusOParser.INSTANCE;
    TestHelper testHelper = TestHelper.INSTANCE;
    XMLHelper xmlHelper = XMLHelper.INSTANCE;

    @Test
    public void testParser(){
        byte[] rawDoc = testHelper.loadDocument("syllabusO.xml");
        Document doc = xmlHelper.getDocument(rawDoc);

        SyllabusOParser.Result result = parser.parse(doc);

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

        assertEquals("10", result.rubrics.get(0).second);
        assertEquals("10", result.rubrics.get(1).second);
        assertEquals("40", result.rubrics.get(2).second);
        assertEquals("40", result.rubrics.get(3).second);
    }
}
