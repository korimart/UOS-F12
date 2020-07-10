package com.korimart.f12;

import org.junit.Test;
import org.w3c.dom.Document;

public class SchoolListUnitTest {
    TestHelper testHelper = TestHelper.INSTANCE;

    @Test
    public void parserTest(){
        SchoolListFetcher.Result result = new SchoolListFetcher.Result();

        String docString = testHelper.loadDocument("collegeList.xml", "euc-kr");
        Document doc = XMLHelper.INSTANCE.getDocument(docString, "euc-kr");
        SchoolListFetcher.INSTANCE.parse(doc, result);
    }
}
