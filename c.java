package com.msim.seismic_datafeed.jobs.salescoverage;

import com.msim.seismic_datafeed.jobs.salescoverage.model.SalescoverageData;
import com.msim.seismic_datafeed.jobs.salescoverage.model.SalescoverageDataItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SalescoverageDataProcessor implements ItemProcessor<SalescoverageData, SalescoverageDataItem> {

    @Override
    public SalescoverageDataItem process(SalescoverageData item) throws Exception {
        log.info("Processing sales coverage data item: {}", item);

        SalescoverageDataItem output = new SalescoverageDataItem();
        output.setSfTeamId(item.getSfTeamId());
        output.setTeamCode(item.getTeamCode());
        output.setTeamName(item.getTeamName());
        output.setSfTerritoryId(item.getSfTerritoryId());
        output.setTerritoryCode(item.getTerritoryCode());
        output.setTerritoryName(item.getTerritoryName());
        output.setInternalSalesPersonMsid(item.getInternalSalesPersonMsid());
        output.setInternalSalesPersonFullName(item.getInternalSalesPersonFullName());
        output.setExternalSalesPersonMsid(item.getExternalSalesPersonMsid());
        output.setExternalSalesPersonFullName(item.getExternalSalesPersonFullName());

        return output;
    }
}
