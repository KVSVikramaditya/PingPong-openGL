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
        if (data == null) {
            log.info("Fetching sales coverage data from provider...");
            data = salescoverageDataProvider.fetchSalescoverageData();  // Fetch all records once
            nextIndex = 0;
        }

        if (nextIndex < data.size()) {
            SalescoverageData record = data.get(nextIndex++);
            log.info("Reading record: {}", record);
            return record;
        } else {
            log.info("No more data available. Returning null to signal end of reading.");
            return null;  // Signals the end of reading
        }
    }
}




@Bean(name = "retrieveSalescoverage")
public Step retrieveSalescoverage(
        @Qualifier("salescoverageDataReader") SalescoverageDataReader salescoverageDataReader,
        @Qualifier("salescoverageItemProcessor") CompositeItemProcessor<SalescoverageData, SalescoverageDataItem> salescoverageItemProcessor, // ✅ Correct type
        @Qualifier("salescoverageFlatFileItemWriter") FlatFileItemWriter<SalescoverageDataItem> fileItemWriter,
        @Value("${spring.batch.chunk.size}") int chunkSize
) {
    return stepBuilderFactory.get("retrieveSalescoverage")
            .<SalescoverageData, SalescoverageDataItem>chunk(chunkSize) // ✅ Correct data type
            .reader(salescoverageDataReader)
            .processor(salescoverageItemProcessor)
            .writer(fileItemWriter)
            .build();
}



@Bean(name = "salescoverageItemProcessor")
@StepScope
public CompositeItemProcessor<SalescoverageData, SalescoverageDataItem> salescoverageItemProcessor(
        @Qualifier("salescoverageProcessor") SalescoverageDataProcessor salescoverageDataProcessor,
        @Autowired FileItemProcessor fileItemProcessor) throws Exception {
    
    CompositeItemProcessor<SalescoverageData, SalescoverageDataItem> compositeProcessor = new CompositeItemProcessor<>();
    compositeProcessor.setDelegates(java.util.List.of(salescoverageDataProcessor, fileItemProcessor));
    compositeProcessor.afterPropertiesSet();
    
    return compositeProcessor;
}




