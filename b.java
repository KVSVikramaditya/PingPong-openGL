package com.msim.seismic_datafeed.jobs.salescoverage;

import com.msim.seismic_datafeed.jobs.salescoverage.model.SalescoverageData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class SalescoverageDataReader implements ItemReader<SalescoverageData> {

    @Autowired
    private SalescoverageDataProvider salescoverageDataProvider;

    private List<SalescoverageData> data;
    private int nextIndex = 0;

    @Override
    public SalescoverageData read() throws Exception {
        // Fetch once
        if (data == null) {
            log.info("Fetching sales coverage data from provider...");
            data = salescoverageDataProvider.fetchSalescoverageData();  // returns List<SalescoverageData>
            nextIndex = 0;
        }

        // Read one record at a time
        if (nextIndex < data.size()) {
            SalescoverageData record = data.get(nextIndex++);
            log.info("Reading record: {}", record);
            return record;
        } else {
            log.info("No more data. Returning null to signal end of reading.");
            return null; // signals the end of reading to Spring Batch
        }
    }
}
