package com.msim.seismic_datafeed.jobs.salescoverage;

import com.msim.seismic_datafeed.jobs.salescoverage.model.SalescoverageData;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

public class SalescoverageDataReaderTest {

    private SalescoverageDataReader reader;

    @Mock
    private SalescoverageDataProvider dataProvider;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        reader = new SalescoverageDataReader();
        reader.setSalescoverageDataProvider(dataProvider);
    }

    @Test
    public void testRead() throws Exception {
        when(dataProvider.fetchSalescoverageData()).thenReturn(
                Arrays.asList(new SalescoverageData(), new SalescoverageData())
        );

        assertNotNull(reader.read());
        assertNotNull(reader.read());
        assertNull(reader.read()); // should return null after two items
    }
}
