package com.msim.seismic_datafeed.jobs.salescoverage;

import com.msim.seismic_datafeed.jobs.salescoverage.model.SalescoverageDataItem;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class SalescoverageDataItemTest {

    private SalescoverageDataItem salescoverageDataItem;

    @BeforeMethod
    public void setUp() {
        salescoverageDataItem = new SalescoverageDataItem();

        salescoverageDataItem.setSTeamId("team123");
        salescoverageDataItem.setTeamCode("TC001");
        salescoverageDataItem.setTeamName("Risk Analysis Team");
        salescoverageDataItem.setSfTerritoryId("territory456");
        salescoverageDataItem.setTerritoryCode("TER123");
        salescoverageDataItem.setTerritoryName("North America");
        salescoverageDataItem.setInternalSalesPersonMsid("MSID789");
        salescoverageDataItem.setInternalSalesPersonFullName("John Doe");
        salescoverageDataItem.setExternalSalesPersonMsid("MSID456");
        salescoverageDataItem.setExternalSalesPersonFullName("Jane Smith");
    }

    @Test
    public void testSalescoverageDataItemFields() {
        assertEquals(salescoverageDataItem.getSTeamId(), "team123");
        assertEquals(salescoverageDataItem.getTeamCode(), "TC001");
        assertEquals(salescoverageDataItem.getTeamName(), "Risk Analysis Team");
        assertEquals(salescoverageDataItem.getSfTerritoryId(), "territory456");
        assertEquals(salescoverageDataItem.getTerritoryCode(), "TER123");
        assertEquals(salescoverageDataItem.getTerritoryName(), "North America");
        assertEquals(salescoverageDataItem.getInternalSalesPersonMsid(), "MSID789");
        assertEquals(salescoverageDataItem.getInternalSalesPersonFullName(), "John Doe");
        assertEquals(salescoverageDataItem.getExternalSalesPersonMsid(), "MSID456");
        assertEquals(salescoverageDataItem.getExternalSalesPersonFullName(), "Jane Smith");
    }

    @Test
    public void testSalescoverageDataItemToString() {
        String expectedString = "SalescoverageDataItem(sTeamId=team123, teamCode=TC001, teamName=Risk Analysis Team, sfTerritoryId=territory456, territoryCode=TER123, territoryName=North America, internalSalesPersonMsid=MSID789, internalSalesPersonFullName=John Doe, externalSalesPersonMsid=MSID456, externalSalesPersonFullName=Jane Smith)";

        assertEquals(salescoverageDataItem.toString(), expectedString);
    }
}
