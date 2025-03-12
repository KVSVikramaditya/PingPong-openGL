package com.msim.seismic_datafeed.jobs.salescoverage;

import com.msim.seismic_datafeed.jobs.salescoverage.model.SalescoverageData;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class SalescoverageDataTest {

    private SalescoverageData salescoverageData;

    @BeforeMethod
    public void setUp() {
        salescoverageData = new SalescoverageData();

        salescoverageData.setSTeamId("team123");
        salescoverageData.setTeamCode("TC001");
        salescoverageData.setTeamName("Risk Analysis Team");
        salescoverageData.setSfTerritoryId("territory456");
        salescoverageData.setTerritoryCode("TER123");
        salescoverageData.setTerritoryName("North America");
        salescoverageData.setInternalSalesPersonMsid("MSID789");
        salescoverageData.setInternalSalesPersonFullName("John Doe");
        salescoverageData.setExternalSalesPersonMsid("MSID456");
        salescoverageData.setExternalSalesPersonFullName("Jane Smith");
    }

    @Test
    public void testSalescoverageDataFields() {
        assertEquals(salescoverageData.getSTeamId(), "team123");
        assertEquals(salescoverageData.getTeamCode(), "TC001");
        assertEquals(salescoverageData.getTeamName(), "Risk Analysis Team");
        assertEquals(salescoverageData.getSfTerritoryId(), "territory456");
        assertEquals(salescoverageData.getTerritoryCode(), "TER123");
        assertEquals(salescoverageData.getTerritoryName(), "North America");
        assertEquals(salescoverageData.getInternalSalesPersonMsid(), "MSID789");
        assertEquals(salescoverageData.getInternalSalesPersonFullName(), "John Doe");
        assertEquals(salescoverageData.getExternalSalesPersonMsid(), "MSID456");
        assertEquals(salescoverageData.getExternalSalesPersonFullName(), "Jane Smith");
    }

    @Test
    public void testSalescoverageDataToString() {
        String expectedString = "SalescoverageData(sTeamId=team123, teamCode=TC001, teamName=Risk Analysis Team, sfTerritoryId=territory456, territoryCode=TER123, territoryName=North America, internalSalesPersonMsid=MSID789, internalSalesPersonFullName=John Doe, externalSalesPersonMsid=MSID456, externalSalesPersonFullName=Jane Smith)";

        assertEquals(salescoverageData.toString(), expectedString);
    }
}
