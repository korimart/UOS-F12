package com.korimart.f12;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.korimart.f12.F12Fetcher.f12Params;
import static com.korimart.f12.F12Fetcher.f12URL;
import static com.korimart.f12.F12Fetcher.smtParams;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class F12UnitTest {
    TestHelper testHelper = TestHelper.INSTANCE;

    @InjectMocks
    F12Fetcher f12Fetcher = F12Fetcher.INSTANCE;

    @Mock
    WebService webService;

    @Before
    public void setup(){
        String infoResponse = testHelper.loadDocument("myInfoResponse.xml", "euc-kr");
        when(webService.sendPost(f12URL, smtParams, "euc-kr")).thenReturn(infoResponse);
    }

    private void mockResponse(String doc) {
        when(webService.sendPost(f12URL, String.format(f12Params, 2020, "10"), "euc-kr"))
                .thenReturn(testHelper.loadDocument(doc, "euc-kr"));
    }

    @Test
    public void mineDoc() {
        mockResponse("mineDoc.xml");
        F12Fetcher.Result result = f12Fetcher.fetch(true);
        assertEquals(9, result.totalPnts);
        assertEquals(3, result.hiddenPnts);
        assertEquals(4f, result.hiddenAvg, 0.00001);
        assertEquals(4.33f, result.totalAvg, 0.01);
    }

    @Test
    public void nameOnlyDoc(){
        mockResponse("nameOnlyDoc.xml");
        F12Fetcher.Result result = f12Fetcher.fetch(true);
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
        mockResponse("parkDoc.xml");
        F12Fetcher.Result result = f12Fetcher.fetch(false);
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
        mockResponse("dotDoc.xml");
        F12Fetcher.Result result = f12Fetcher.fetch(false);
        assertEquals(11, result.totalPnts);
        assertEquals(5, result.hiddenPnts);
        assertEquals(4.1f, result.hiddenAvg, 0.00001);
        assertEquals(3.88f, result.totalAvg, 0.01);
    }

    @Test
    public void S0pntsDoc(){
        mockResponse("S0PntsDoc.xml");
        F12Fetcher.Result result = f12Fetcher.fetch(false);
        assertEquals(6, result.totalPnts);
        assertEquals(3, result.hiddenPnts);
        assertEquals(4f, result.hiddenAvg, 0.00001);
        assertEquals(4.25f, result.totalAvg, 0.01);
    }

    @Test
    public void S0pntsHiddenDoc(){
        mockResponse("S0PntsHiddenDoc.xml");
        F12Fetcher.Result result = f12Fetcher.fetch(false);
        assertEquals(9, result.totalPnts);
        assertEquals(3, result.hiddenPnts);
        assertEquals(4f, result.hiddenAvg, 0.00001);
        assertEquals(4.33f, result.totalAvg, 0.01);
    }

    @Test
    public void S3pntsDoc(){
        mockResponse("S3PntsDoc.xml");
        F12Fetcher.Result result = f12Fetcher.fetch(false);
        assertEquals(9, result.totalPnts);
        assertEquals(3, result.hiddenPnts);
        assertEquals(4f, result.hiddenAvg, 0.0001f);
        assertEquals(4.25f, result.totalAvg, 0.01f);
    }

    @Test
    public void S3pntsHiddenDoc(){
        mockResponse("S3PntsHiddenDoc.xml");
        F12Fetcher.Result result = f12Fetcher.fetch(false);
        assertEquals(12, result.totalPnts);
        assertEquals(6, result.hiddenPnts);
        assertEquals(4f, result.hiddenAvg, 0.00001);
        assertEquals(4.33f, result.totalAvg, 0.01);
    }
}