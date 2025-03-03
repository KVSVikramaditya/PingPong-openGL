package com.msim.seismic_datafeed.jobs.salescoverage;

import com.msim.seismic_datafeed.constants.JobType;
import com.msim.seismic_datafeed.jobs.CometJobTrigger;
import com.msim.seismic_datafeed.jobs.StatisticsLoggerJobExecutionListener;
import com.msim.seismic_datafeed.jobs.salescoverage.model.SalescoverageData;
import com.msim.seismic_datafeed.jobs.salescoverage.model.SalescoverageDataItem;
import com.msim.seismic_datafeed.model.FileItem;
import com.msim.seismic_datafeed.utils.FileItemProcessor;
import com.msim.seismic_datafeed.utils.ListUnpackingItemWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Configuration
@EnableBatchProcessing
@Slf4j
public class SalescoverageBatchConfiguration {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private CometJobTrigger cometJobTrigger;

    @Autowired
    private JobExecutionDecider cometJobDecider;

    @Value("${spring.batch.chunk.size}")
    private int chunkSize;

    @Value("${seismic.output.file.directory:}")
    private String outputFileDirectory;

    @Bean(name = "loadSalescoverageJob")
    public Job loadSalescoverageJob(
            @Qualifier("retrieveSalescoverage") Step retrieveSalescoverage,
            @Qualifier("salescoverageCometJobTriggerStep") Step salescoverageCometJobTriggerStep,
            StatisticsLoggerJobExecutionListener statisticsLoggerJobExecutionListener) {
        return jobBuilderFactory.get("loadSalescoverageJob")
                .listener(statisticsLoggerJobExecutionListener)
                .incrementer(new RunIdIncrementer())
                .preventRestart()
                .flow(retrieveSalescoverage)
                .next(cometJobDecider)
                .on("CONTINUE").to(salescoverageCometJobTriggerStep)
                .from(cometJobDecider)
                .on("BREAK").end()
                .end()
                .build();
    }

    @Bean(name = "retrieveSalescoverage")
    public Step retrieveSalescoverage(
            @Qualifier("salescoverageDataReader") SalescoverageDataReader salescoverageDataReader,
            @Qualifier("salescoverageItemProcessor") CompositeItemProcessor<SalescoverageData, SalescoverageDataItem> salescoverageItemProcessor,
            @Qualifier("salescoverageFlatFileItemWriter") FlatFileItemWriter<SalescoverageDataItem> fileItemWriter,
            @Value("${spring.batch.chunk.size}") int chunkSize) {
        return stepBuilderFactory.get("retrieveSalescoverage")
                .<SalescoverageData, SalescoverageDataItem>chunk(chunkSize)
                .reader(salescoverageDataReader)
                .processor(salescoverageItemProcessor)
                .writer(fileItemWriter)
                .build();
    }

    @Bean(name = "salescoverageDataReader")
    @StepScope
    public SalescoverageDataReader salescoverageDataReader() {
        return new SalescoverageDataReader();
    }

    @Bean(name = "salescoverageDataProvider")
    public SalescoverageDataProvider salescoverageDataProvider(@Qualifier("snowflakeJdbcTemplate") NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        return new SalescoverageDataProvider(namedParameterJdbcTemplate);
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

    @Bean(name = "salescoverageProcessor")
    @StepScope
    public SalescoverageDataProcessor salescoverageProcessor() {
        return new SalescoverageDataProcessor();
    }

    @Bean(name = "salescoverageFlatFileItemWriter")
    @StepScope
    public FlatFileItemWriter<SalescoverageDataItem> salescoverageFlatFileItemWriter(
            @Qualifier("jobScopedStore") JobScopedStore store,
            @Qualifier("salescoverageFlatFileHeaderCallback") SalescoverageFlatFileHeaderCallback headerCallback,
            @Value("#{jobParameters['startTime']}") long startTime,
            @Value("#{jobParameters['asOfDate']}") String asOfDate) {
        String fileName = JobType.SALESCOVERAGE.name() + "_" + asOfDate + "_" + startTime + ".csv";
        String fullFilePath = outputFileDirectory + fileName;
        store.setOutputFileName(fileName);
        
        BeanWrapperFieldExtractor<SalescoverageDataItem> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[]{"sfTeamId", "teamCode", "teamName", "sfTerritoryId", "territoryCode", "territoryName", "internalSalesPersonMsid", "internalSalesPersonFullName", "externalSalesPersonMsid", "externalSalesPersonFullName"});
        
        DelimitedLineAggregator<SalescoverageDataItem> aggregator = new DelimitedLineAggregator<>();
        aggregator.setDelimiter(",");
        aggregator.setFieldExtractor(fieldExtractor);
        
        FlatFileItemWriter<SalescoverageDataItem> writer = new FlatFileItemWriter<>();
        writer.setResource(new FileSystemResource(fullFilePath));
        writer.setHeaderCallback(headerCallback);
        writer.setLineAggregator(aggregator);
        writer.setShouldDeleteIfEmpty(true);
        writer.setShouldDeleteIfExists(true);
        return writer;
    }
}
