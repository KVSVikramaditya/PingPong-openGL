package com.msim.seismic_datafeed.jobs.salescoverage;

import com.msim.seismic_datafeed.jobs.salescoverage.model.SalescoverageData;
import com.msim.seismic_datafeed.jobs.salescoverage.model.SalescoverageDataItem;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;

public class SalescoverageDataProcessorTest {

    private final SalescoverageDataProcessor processor = new SalescoverageDataProcessor();

    @Test
    public void testProcess() throws Exception {
        SalescoverageData data = new SalescoverageData();
        data.setSfTeamId("team1");
        data.setTeamCode("code123");
        data.setTeamName("Test Team");
        data.setSfTerritoryId("territory1");
        data.setTerritoryCode("TER123");
        data.setTerritoryName("US");
        data.setInternalSalesPersonMsid("INT001");
        data.setInternalSalesPersonFullName("John Internal");
        data.setExternalSalesPersonMsid("EXT002");
        data.setExternalSalesPersonFullName("Jane External");

        List<SalescoverageDataItem> result = processor.process(data);
        assertEquals(result.size(), 1);
        SalescoverageDataItem item = result.get(0);
        assertEquals(item.getSTeamId(), "team1");
        assertEquals(item.getTeamCode(), "code123");
        assertEquals(item.getTeamName(), "Test Team");
        assertEquals(item.getTerritoryName(), "US");
        assertEquals(item.getInternalSalesPersonMsid(), "INT001");
        assertEquals(item.getExternalSalesPersonFullName(), "Jane External");
    }
}
