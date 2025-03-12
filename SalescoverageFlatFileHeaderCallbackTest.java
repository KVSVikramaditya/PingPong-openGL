package com.msim.seismic_datafeed.jobs.salescoverage;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.Writer;

import static org.mockito.Mockito.verify;

public class SalescoverageFlatFileHeaderCallbackTest {

    private SalescoverageFlatFileHeaderCallback headerCallback;

    @Mock
    private Writer writer;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        headerCallback = new SalescoverageFlatFileHeaderCallback();
        ReflectionTestUtils.setField(headerCallback, "header", "teamId,teamCode,teamName,sfTerritoryId");
    }

    @Test
    public void testWriteHeader() throws IOException {
        headerCallback.writeHeader(writer);
        verify(writer).write("column1,column2,column3,column4,column5");
    }
}
