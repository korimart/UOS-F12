package com.korimart.f12;

import androidx.core.util.Pair;

import org.junit.Test;
import org.w3c.dom.Document;

import java.util.List;

import static org.junit.Assert.*;

public class SyllabusOParserTest {
    SyllabusOParser parser = SyllabusOParser.INSTANCE;
    TestHelper testHelper = TestHelper.INSTANCE;
    XMLHelper xmlHelper = XMLHelper.INSTANCE;

    @Test
    public void testNonZeroRubrics() throws Exception {
        byte[] rawDoc = ("<?xml version=\"1.0\" encoding=\"EUC-KR\"?><root xmlns=\"\">" +
                "<attend_rate><![CDATA[□ 출석 (10)%]]></attend_rate>\n" +
                "<stud_portfolio_rate><![CDATA[□ 학생포트폴리오 (0)%]]></stud_portfolio_rate>\n" +
                "<share_rate><![CDATA[□ 참여도 (0)%]]></share_rate>\n" +
                "<temp_prjt_rate><![CDATA[□ 수시과제 (10)%]]></temp_prjt_rate>\n" +
                "<temp_test_rate><![CDATA[□ 수시시험 (0)%]]></temp_test_rate>\n" +
                "<mid_prjt_rate><![CDATA[□ 중간과제 (40)%]]></mid_prjt_rate>\n" +
                "<mid_test_rate><![CDATA[□ 중간시험 (0)%]]></mid_test_rate>\n" +
                "<end_term_prjt_rate><![CDATA[□ 기말과제 (40)%]]></end_term_prjt_rate>\n" +
                "<end_term_test_rate><![CDATA[□ 기말시험 (0)%]]></end_term_test_rate>\n" +
                "<etc_est_rate><![CDATA[□ 기타 (0)%]]></etc_est_rate>\n" +
                "<drawing_rate><![CDATA[□ 설계평가 (0)%]]></drawing_rate></root>\n").getBytes("euc-kr");

        Document doc = xmlHelper.getDocument(rawDoc);
        List<Pair<String, String>> parsed = null;
        try {
            parsed = parser.getNonZeroRubrics(doc);
        } catch (Exception ignored){
        }

        assertNotNull(parsed);
        assertEquals(4, parsed.size());

        assertEquals("출석", parsed.get(0).first);
        assertEquals("수시과제", parsed.get(1).first);
        assertEquals("중간과제", parsed.get(2).first);
        assertEquals("기말과제", parsed.get(3).first);

        assertEquals("10", parsed.get(0).second);
        assertEquals("10", parsed.get(1).second);
        assertEquals("40", parsed.get(2).second);
        assertEquals("40", parsed.get(3).second);
    }
}
