package com.msim.seismic_datafeed.jobs.salescoverage;

import com.msim.seismic_datafeed.jobs.salescoverage.model.SalescoverageData;
import com.msim.seismic_datafeed.jobs.salescoverage.model.SalescoverageDataItem;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.*;

public class SalescoverageDataProcessorTest {

    private final SalescoverageDataProcessor processor = new SalescoverageDataProcessor();

    @Test
    public void testProcess() throws Exception {
        SalescoverageData input = new SalescoverageData();
        input.setSfTeamId("ST123");
        input.setTeamCode("TC456");
        input.setTeamName("Team A");
        input.setSfTerritoryId("Terr01");
        input.setTerritoryCode("TCode");
        input.setTerritoryName("Territory A");
        input.setInternalSalesPersonMsid("MSID111");
        input.setInternalSalesPersonFullName("John Doe");
        input.setExternalSalesPersonMsid("EXT123");
        input.setExternalSalesPersonFullName("Jane Doe");

        List<SalescoverageDataItem> result = processor.process(input);

        assertEquals(result.size(), 1);
        SalescoverageDataItem output = result.get(0);
        assertEquals(output.getSTeamId(), "ST123");
        assertEquals(output.getTeamCode(), "TC456");
        assertEquals(output.getTeamName(), "Team A");
        assertEquals(output.getSfTerritoryId(), "Territory123");
        assertEquals(output.getExternalSalesPersonFullName(), "Jane Doe");
    }
}
