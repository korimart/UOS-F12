package com.korimart.f12;

import org.junit.Test;
import org.w3c.dom.Document;

import java.util.List;

import static org.junit.Assert.*;
import static com.korimart.f12.CourseListParser.*;

public class CourseListUnitTest {
    TestHelper testHelper = TestHelper.INSTANCE;

    @Test
    public void parserTest(){
        // 2019년 2학기 컴퓨터과학부
        byte[] docByte = testHelper.loadDocument("courseList2.xml");
        Document doc = XMLHelper.INSTANCE.getDocument(docByte);

        CourseListParser.Result result = CourseListParser.INSTANCE.parse(doc);

        CourseInfo info1 = new CourseInfo(){{
            schoolYear = "2019";
            semester = "20";
            name = "창의공학기초설계";
            classification = "전공필수";
            classNumber = "02";
            yearLevel = "1";
            points = 3;
            timePlace = "Fri 10,11,12,13/2-114";
            professor = "이동희";
            outsider = true;
            doubleMajor = true;
            minor = false;
            nonKorean = false;
            curriNumber = "30000";
            programCode = "005";
            TOYear = "31";
            TOYearMax = "40";
            TOAll = "31";
            TOAllMax = "40";
        }};

        CourseInfo info2 = new CourseInfo(){{
            schoolYear = "2019";
            semester = "20";
            name = "창의공학기초설계";
            classification = "전공필수";
            classNumber = "03";
            yearLevel = "1";
            points = 3;
            timePlace = "Thu 10,11,12,13/2-114";
            professor = "현철승";
            outsider = false;
            doubleMajor = true;
            minor = false;
            nonKorean = false;
            curriNumber = "30000";
            programCode = "005";
            TOYear = "40";
            TOYearMax = "40";
            TOAll = "40";
            TOAllMax = "40";
        }};

        CourseInfo info3 = new CourseInfo(){{
            schoolYear = "2019";
            semester = "20";
            name = "학업설계상담 Ⅱ";
            classification = "전공필수";
            classNumber = "01";
            yearLevel = "1";
            points = 0;
            timePlace = "";
            professor = "홍의경";
            outsider = false;
            doubleMajor = false;
            minor = false;
            nonKorean = false;
            curriNumber = "71072";
            programCode = "";
            TOYear = "10";
            TOYearMax = "10";
            TOAll = "10";
            TOAllMax = "10";
        }};

        assertNull(result.errorInfo);

        assertHasEntry(result.courseInfos, info1);
        assertHasEntry(result.courseInfos, info2);
        assertHasEntry(result.courseInfos, info3);
    }

    @Test
    public void parserTest2(){
        // 2019년 2학기 전전컴
        byte[] docByte = testHelper.loadDocument("courseList3.xml");
        Document doc = XMLHelper.INSTANCE.getDocument(docByte);

        CourseListParser.Result result = CourseListParser.INSTANCE.parse(doc);

        CourseInfo info1 = new CourseInfo(){{
            schoolYear = "2019";
            semester = "20";
            name = "공학수학Ⅱ";
            classification = "전공필수";
            classNumber = "02";
            yearLevel = "2";
            points = 3;
            timePlace = "Wed 06,07,08/19-B114/115";
            professor = "안도열";
            outsider = false;
            doubleMajor = true;
            minor = false;
            nonKorean = true;
            curriNumber = "30010";
            programCode = "";
            TOYear = "111";
            TOYearMax = "115";
            TOAll = "111";
            TOAllMax = "115";
        }};

        CourseInfo info2 = new CourseInfo(){{
            schoolYear = "2019";
            semester = "20";
            name = "알고리듬";
            classification = "전공선택";
            classNumber = "01";
            yearLevel = "3";
            points = 3;
            timePlace = "Mon 07/19-216/217, Mon 08,09/19-104/105";
            professor = "안영아";
            outsider = false;
            doubleMajor = true;
            minor = false;
            nonKorean = false;
            curriNumber = "30034";
            programCode = "";
            TOYear = "18";
            TOYearMax = "40";
            TOAll = "18";
            TOAllMax = "40";
        }};

        CourseInfo info3 = new CourseInfo(){{
            schoolYear = "2019";
            semester = "20";
            name = "선형대수학";
            classification = "전공선택";
            classNumber = "01";
            yearLevel = "1";
            points = 3;
            timePlace = "Tue 01,02,03/19-B114/115";
            professor = "이준화";
            outsider = false;
            doubleMajor = false;
            minor = false;
            nonKorean = false;
            curriNumber = "30042";
            programCode = "";
            TOYear = "0";
            TOYearMax = "0";
            TOAll = "108";
            TOAllMax = "115";
        }};

        assertNull(result.errorInfo);

        assertHasEntry(result.courseInfos, info1);
        assertHasEntry(result.courseInfos, info2);
        assertHasEntry(result.courseInfos, info3);
    }

    private void assertHasEntry(List<CourseInfo> infos, CourseInfo info){
        CourseInfo infoReal1 = findEntryByNameNumber(infos, info.name, info.classNumber);
        assertNotNull(infoReal1);
        assertCourseInfoSame(info, infoReal1);
    }

    private CourseInfo findEntryByNameNumber(List<CourseInfo> infos, String name, String classNo){
        for (CourseInfo currInfo : infos){
            if (currInfo.name.equals(name) && currInfo.classNumber.equals(classNo))
                return currInfo;
        }
        return null;
    }

    private void assertCourseInfoSame(CourseInfo info1, CourseInfo info2){
       assertEquals(info1.name, info2.name);
       assertEquals(info1.classification, info2.classification);
       assertEquals(info1.classNumber, info2.classNumber);
       assertEquals(info1.yearLevel, info2.yearLevel);
       assertEquals(info1.points, info2.points);
       assertEquals(info1.timePlace, info2.timePlace);
       assertEquals(info1.professor, info2.professor);
       assertEquals(info1.outsider, info2.outsider);
       assertEquals(info1.doubleMajor, info2.doubleMajor);
       assertEquals(info1.minor, info2.minor);
       assertEquals(info1.nonKorean, info2.nonKorean);
       assertEquals(info1.curriNumber, info2.curriNumber);
       assertEquals(info1.TOYear, info2.TOYear);
       assertEquals(info1.TOYearMax, info2.TOYearMax);
       assertEquals(info1.TOAll, info2.TOAll);
       assertEquals(info1.TOAllMax, info2.TOAllMax);
    }
}
