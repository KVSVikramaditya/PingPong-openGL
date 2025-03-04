package com.msim.seismic_datafeed.jobs.salescoverage;

import com.msim.seismic_datafeed.constants.JobType;
import com.msim.seismic_datafeed.jobs.CometJobTrigger;
import com.msim.seismic_datafeed.jobs.FileItemProcessor;
import com.msim.seismic_datafeed.jobs.JobScopedStore;
import com.msim.seismic_datafeed.jobs.StatisticsLoggerJobExecutionListener;
import com.msim.seismic_datafeed.jobs.salescoverage.model.SalescoverageData;
import com.msim.seismic_datafeed.jobs.salescoverage.model.SalescoverageDataItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
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

    /**
     * Reads your CSV column names from a comma-separated property like:
     *   seismic.salescoverage.feedfile.header=sfTeamId,teamCode,teamName,...
     * Then splits into an array at runtime.
     */
    @Value("#{'${seismic.salescoverage.feedfile.header}'.split(',')}")
    private String[] attributeList;

    @Value("${seismic.output.file.directory:}")
    private String outputFileDirectory;

    /**
     * Main job definition for loading Sales Coverage.
     */
    @Bean(name = "loadSalescoverageJob")
    public Job loadSalescoverageJob(
            @Qualifier("retrieveSalescoverage") Step retrieveSalescoverage,
            @Qualifier("salescoverageCometJobTriggerStep") Step salescoverageCometJobTriggerStep,
            StatisticsLoggerJobExecutionListener statisticsLoggerJobExecutionListener
    ) {
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

    /**
     * Step definition that reads, processes, and writes Salescoverage data.
     */
    @Bean(name = "retrieveSalescoverage")
    public Step retrieveSalescoverage(
            @Qualifier("salescoverageDataReader") SalescoverageDataReader salescoverageDataReader,
            @Qualifier("salescoverageItemProcessor") CompositeItemProcessor<SalescoverageData, SalescoverageDataItem> salescoverageItemProcessor,
            @Qualifier("salescoverageFlatFileItemWriter") FlatFileItemWriter<SalescoverageDataItem> fileItemWriter,
            @Value("${spring.batch.chunk.size}") int chunkSize
    ) {
        return stepBuilderFactory.get("retrieveSalescoverage")
                .<SalescoverageData, SalescoverageDataItem>chunk(chunkSize)
                .reader(salescoverageDataReader)
                .processor(salescoverageItemProcessor)
                .writer(fileItemWriter)
                .build();
    }

    /**
     * Reads one SalescoverageData at a time using SalescoverageDataProvider.
     */
    @Bean(name = "salescoverageDataReader")
    @StepScope
    public SalescoverageDataReader salescoverageDataReader() {
        return new SalescoverageDataReader();
    }

    /**
     * Provider that internally constructs the DAO via the given Snowflake JDBC template.
     */
    @Bean(name = "salescoverageDataProvider")
    public SalescoverageDataProvider salescoverageDataProvider(
            @Qualifier("snowflakeJdbcTemplate") NamedParameterJdbcTemplate namedParameterJdbcTemplate
    ) {
        return new SalescoverageDataProvider(namedParameterJdbcTemplate);
    }

    /**
     * Composite processor to chain your domain processor and FileItemProcessor if needed.
     */
    @Bean(name = "salescoverageItemProcessor")
    @StepScope
    public CompositeItemProcessor<SalescoverageData, SalescoverageDataItem> salescoverageItemProcessor(
            @Qualifier("salescoverageProcessor") SalescoverageDataProcessor salescoverageDataProcessor,
            @Autowired FileItemProcessor fileItemProcessor
    ) throws Exception {
        CompositeItemProcessor<SalescoverageData, SalescoverageDataItem> compositeProcessor = new CompositeItemProcessor<>();
        compositeProcessor.setDelegates(java.util.List.of(salescoverageDataProcessor, fileItemProcessor));
        compositeProcessor.afterPropertiesSet();
        return compositeProcessor;
    }

    /**
     * Single processor that converts SalescoverageData into SalescoverageDataItem.
     */
    @Bean(name = "salescoverageProcessor")
    @StepScope
    public SalescoverageDataProcessor salescoverageProcessor() {
        return new SalescoverageDataProcessor();
    }

    /**
     * Writer that outputs SalescoverageDataItem records to CSV.
     * 
     * 'attributeList' is used to specify the columns (via BeanWrapperFieldExtractor).
     */
    @Bean(name = "salescoverageFlatFileItemWriter")
    @StepScope
    public FlatFileItemWriter<SalescoverageDataItem> salescoverageFlatFileItemWriter(
            @Qualifier("jobScopedStore") JobScopedStore store,
            @Qualifier("salescoverageFlatFileHeaderCallback") SalescoverageFlatFileHeaderCallback headerCallback,
            @Value("#{jobParameters['startTime']}") long startTime,
            @Value("#{jobParameters['asOfDate']}") String asOfDate
    ) {
        // Construct the CSV filename
        String fileName = JobType.SALESCOVERAGE.name() + "_" + asOfDate + "_" + startTime + ".csv";
        String fullFilePath = outputFileDirectory + fileName;
        store.setOutputFileName(fileName);

        BeanWrapperFieldExtractor<SalescoverageDataItem> fieldExtractor = new BeanWrapperFieldExtractor<>();
        // 'attributeList' is an array of field names from your property
        fieldExtractor.setNames(attributeList);

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

    /**
     * Step that triggers CometJob after file generation is complete.
     */
    @Bean(name = "salescoverageCometJobTriggerStep")
    public Step salescoverageCometJobTriggerStep(@Qualifier("jobScopedStore") JobScopedStore store) {
        Tasklet tasklet = (contribution, chunkContext) -> {
            cometJobTrigger.submit(store.getOutputFileName());
            return RepeatStatus.FINISHED;
        };
        return stepBuilderFactory.get("salescoverageCometJobTriggerStep").tasklet(tasklet).build();
    }

    /**
     * Header callback for CSV if needed.
     */
    @Bean(name = "salescoverageFlatFileHeaderCallback")
    public SalescoverageFlatFileHeaderCallback salescoverageFlatFileHeaderCallback() {
        return new SalescoverageFlatFileHeaderCallback();
    }
}
