package com.korimart.f12;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;

import static com.korimart.f12.F12Fetcher.f12Params;
import static com.korimart.f12.F12Fetcher.f12URL;
import static com.korimart.f12.F12Fetcher.smtParams;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class F12UnitTest {
    @InjectMocks
    F12Fetcher f12Fetcher = F12Fetcher.INSTANCE;

    @Mock
    WebService webService;

    @Before
    public void setup(){
        String infoResponse = loadDocument("my info response");
        when(webService.sendPost(f12URL, smtParams, "euc-kr")).thenReturn(infoResponse);
    }

    private void mockResponse(String doc) {
        when(webService.sendPost(f12URL, String.format(f12Params, 2020, "10"), "euc-kr"))
                .thenReturn(loadDocument(doc));
    }

    @Test
    public void mineDoc() {
        mockResponse("mine doc");
        F12Fetcher.Result result = f12Fetcher.fetchF12(true);
        assertEquals(9, result.totalPnts);
        assertEquals(3, result.hiddenPnts);
        assertTrue(Math.abs(result.hiddenAvg - 4) < 0.00001);
        assertTrue(Math.abs(4.33 - result.totalAvg) < 0.01);
    }

    @Test
    public void nameOnlyDoc(){
        mockResponse("Name only doc");
        F12Fetcher.Result result = f12Fetcher.fetchF12(true);
        assertEquals(6, result.totalPnts);
        assertEquals(3, result.hiddenPnts);
        assertTrue(Math.abs(result.hiddenAvg - 4) < 0.00001);
        assertTrue(Math.abs(4.25 - result.totalAvg) < 0.01);
    }

    @Test
    public void parkDoc(){
        // 과목명만 공개된 것이 한 과목
        // 0학점 패논패가 한 과목
        // 2학점짜리 과목 하나가 숨겨져 있는 상황
        mockResponse("park doc");
        F12Fetcher.Result result = f12Fetcher.fetchF12(false);
        assertEquals(7, result.totalPnts);
        assertEquals(2, result.hiddenPnts);
        assertTrue(Math.abs(result.hiddenAvg - 4) < 0.00001);
        assertTrue(Math.abs(4.21 - result.totalAvg) < 0.01);
    }

    @Test
    public void dotDoc(){
        // 3학점짜리 패논패가 한 과목과
        // 3학점짜리 B+가 공개되어 있고
        // 2학점짜리 B+, 3학점짜리 A+가 숨어있는 상황
        mockResponse("dot doc");
        F12Fetcher.Result result = f12Fetcher.fetchF12(false);
        assertEquals(11, result.totalPnts);
        assertEquals(5, result.hiddenPnts);
        assertTrue(Math.abs(result.hiddenAvg - 4.1) < 0.00001);
        assertTrue(Math.abs(3.88 - result.totalAvg) < 0.01);
    }

    @Test
    public void S0pntsDoc(){
        mockResponse("S 0 pnts doc");
        F12Fetcher.Result result = f12Fetcher.fetchF12(false);
        assertEquals(6, result.totalPnts);
        assertEquals(3, result.hiddenPnts);
        assertTrue(Math.abs(result.hiddenAvg - 4) < 0.00001);
        assertTrue(Math.abs(4.25 - result.totalAvg) < 0.01);
    }

    @Test
    public void S0pntsHiddenDoc(){
        mockResponse("S 0 pnts hidden doc");
        F12Fetcher.Result result = f12Fetcher.fetchF12(false);
        assertEquals(9, result.totalPnts);
        assertEquals(3, result.hiddenPnts);
        assertTrue(Math.abs(result.hiddenAvg - 4) < 0.00001);
        assertTrue(Math.abs(4.33 - result.totalAvg) < 0.01);
    }

    @Test
    public void S3pntsDoc(){
        mockResponse("S 3 pnts doc");
        F12Fetcher.Result result = f12Fetcher.fetchF12(false);
        assertEquals(9, result.totalPnts);
        assertEquals(3, result.hiddenPnts);
        assertTrue(Math.abs(result.hiddenAvg - 4) < 0.00001);
        assertTrue(Math.abs(4.25 - result.totalAvg) < 0.01);
    }

    @Test
    public void S3pntsHiddenDoc(){
        mockResponse("S 3 pnts hidden doc");
        F12Fetcher.Result result = f12Fetcher.fetchF12(false);
        assertEquals(12, result.totalPnts);
        assertEquals(6, result.hiddenPnts);
        assertTrue(Math.abs(result.hiddenAvg - 4) < 0.00001);
        assertTrue(Math.abs(4.33 - result.totalAvg) < 0.01);
    }

    private String loadDocument(String doc){
        try {
            File file = new File(
                    Paths.get(System.getProperty("user.dir"), "src/test/java/com/korimart/f12", doc)
                            .toString());
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            fis.close();
            return new String(data, "euc-kr");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}