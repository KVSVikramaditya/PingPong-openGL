package com.msim.seismic_datafeed.jobs.salescoverage;

import com.msim.seismic_datafeed.dao.SalescoverageDaoImpl;
import com.msim.seismic_datafeed.jobs.salescoverage.model.SalescoverageData;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class SalescoverageDataProviderTest {

    private SalescoverageDataProvider provider;

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Mock
    private SalescoverageDaoImpl dao;

    @BeforeMethod
    public void setup() {
        MockitoAnnotations.initMocks(this);
        provider = new SalescoverageDataProvider(jdbcTemplate);
    }

    @Test
    public void testFetchSalescoverageData() {
        List<SalescoverageData> mockData = Arrays.asList(new SalescoverageData(), new SalescoverageData());
        when(dao.getSalesCoverageRecords()).thenReturn(mockData);

        List<SalescoverageData> result = provider.fetchSalescoverageData();
        assertEquals(result.size(), 2);
    }
}
