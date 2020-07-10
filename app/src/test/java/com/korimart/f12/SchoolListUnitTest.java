package com.korimart.f12;

import org.junit.Test;
import org.w3c.dom.Document;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static com.korimart.f12.SchoolListFetcher.DeptInfo;

public class SchoolListUnitTest {
    TestHelper testHelper = TestHelper.INSTANCE;

    @Test
    public void parserTest() {
        SchoolListFetcher.Result result = new SchoolListFetcher.Result();

        String docString = testHelper.loadDocument("collegeList.xml", "euc-kr");
        Document doc = XMLHelper.INSTANCE.getDocument(docString, "euc-kr");
        SchoolListFetcher.INSTANCE.parse(doc, result);

        HashMap<DeptInfo, List<DeptInfo>> hm = result.schoolToDepts;
        Set<DeptInfo> keys = hm.keySet();

        assertEquals(15, keys.size());
        assertNotNull(findByName(keys, "정경대학"));
        assertNotNull(findByName(keys, "경영대학"));
        assertNotNull(findByName(keys, "공과대학"));
        assertNotNull(findByName(keys, "대학구분없음"));

        assertTrue(hasPair(hm, "공과대학", "컴퓨터과학부"));
    }

    private DeptInfo findByName(Collection<DeptInfo> depts, String name) {
        for (DeptInfo dp : depts)
            if (dp.name.equals(name))
                return dp;

        return null;
    }

    private boolean hasPair(HashMap<DeptInfo, List<DeptInfo>> hm, String school, String dept) {
        DeptInfo schoolInfo = findByName(hm.keySet(), school);
        DeptInfo di = findByName(hm.get(schoolInfo), dept);
        return di != null;
    }
}