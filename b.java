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
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.TaskletStep;

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

    /**
     * The main job definition for Sales Coverage.
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
                // Start flow with retrieveSalescoverage
                .flow(retrieveSalescoverage)
                .next(cometJobDecider)
                    .on("CONTINUE").to(salescoverageCometJobTriggerStep)
                .from(cometJobDecider)
                    .on("BREAK").end()
                .end()
                .build();
    }

    /**
     * Defines the step that reads, processes, and writes the Salescoverage data.
     */
    @Bean(name = "retrieveSalescoverage")
    public Step retrieveSalescoverage(
            @Qualifier("salescoverageDataReader") SalescoverageDataReader salescoverageDataReader,
            @Qualifier("salescoverageItemProcessor") CompositeItemProcessor<SalescoverageData, SalescoverageDataItem> salescoverageItemProcessor,
            @Qualifier("salescoverageFileItemWriter") FlatFileItemWriter<SalescoverageDataItem> fileItemWriter,
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
     * The reader for Salescoverage data. Step-scoped if you want to use job parameters.
     */
    @Bean(name = "salescoverageDataReader")
    @StepScope
    public SalescoverageDataReader salescoverageDataReader() {
        return new SalescoverageDataReader();
    }

    /**
     * Creates the provider that fetches data, optionally instantiating DAO within itself.
     */
    @Bean(name = "salescoverageDataProvider")
    public SalescoverageDataProvider salescoverageDataProvider(
            @Qualifier("snowflakeJdbcTemplate") org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate namedParameterJdbcTemplate
    ) {
        // Internally creates the DAO (if needed) or references an existing bean
        return new SalescoverageDataProvider(namedParameterJdbcTemplate);
    }

    /**
     * Composes multiple item processors into a chain, if needed.
     * If you only have one processor (SalescoverageDataProcessor), you can omit this composite approach.
     */
    @Bean(name = "salescoverageItemProcessor")
    @StepScope
    public CompositeItemProcessor<SalescoverageData, SalescoverageDataItem> salescoverageItemProcessor(
            @Qualifier("salescoverageProcessor") SalescoverageDataProcessor salescoverageDataProcessor,
            @Autowired FileItemProcessor fileItemProcessor
    ) throws Exception {
        // We can chain multiple processors if needed
        CompositeItemProcessor<SalescoverageData, SalescoverageDataItem> compositeProcessor =
                new CompositeItemProcessor<>();
        var delegates = new java.util.ArrayList<>(2);
        delegates.add(salescoverageDataProcessor);
        delegates.add(fileItemProcessor);
        compositeProcessor.setDelegates(delegates);
        compositeProcessor.afterPropertiesSet();
        return compositeProcessor;
    }

    /**
     * A single processor that transforms SalescoverageData to SalescoverageDataItem.
     */
    @Bean(name = "salescoverageProcessor")
    @StepScope
    public SalescoverageDataProcessor salescoverageProcessor() {
        return new SalescoverageDataProcessor();
    }

    /**
     * Writer that unpacks the FileItem list (if your processor returns a list of items).
     * If your processor returns a single item per read, you can directly use FlatFileItemWriter.
     */
    @Bean(name = "salescoverageFileItemWriter")
    @StepScope
    public FlatFileItemWriter<SalescoverageDataItem> salescoverageFileItemWriter(
            @Value("#{jobParameters['startTime']}") long startTime,
            @Value("#{jobParameters['asOfDate']}") String asOfDate
    ) {
        // Compose the CSV file path
        String fileName = JobType.SALESCOVERAGE.name() + "_" + asOfDate + "_" + startTime + ".csv";
        String fullFilePath = outputFileDirectory + fileName;

        // Create a FlatFileItemWriter for CSV
        FlatFileItemWriter<SalescoverageDataItem> writer = new FlatFileItemWriter<>();
        writer.setResource(new FileSystemResource(fullFilePath));
        writer.setShouldDeleteIfEmpty(true);
        writer.setShouldDeleteIfExists(true);

        // Define how fields will be extracted and aggregated
        BeanWrapperFieldExtractor<SalescoverageDataItem> fieldExtractor = new BeanWrapperFieldExtractor<>();
        // Note: Provide the property names in the order you want them in CSV
        fieldExtractor.setNames(new String[] {
                "sfTeamId", "teamCode", "teamName", "sfTerritoryId", "territoryCode",
                "territoryName", "internalSalesPersonMsid", "internalSalesPersonFullName",
                "externalSalesPersonMsid", "externalSalesPersonFullName"
        });

        DelimitedLineAggregator<SalescoverageDataItem> aggregator = new DelimitedLineAggregator<>();
        aggregator.setDelimiter(",");
        aggregator.setFieldExtractor(fieldExtractor);

        writer.setLineAggregator(aggregator);
        return writer;
    }

    /**
     * Optionally define a header callback if you want column headers.
     */
    @Bean(name = "salescoverageFlatFileHeaderCallback")
    public SalescoverageFlatFileHeaderCallback salescoverageFlatFileHeaderCallback() {
        return new SalescoverageFlatFileHeaderCallback();
    }

    /**
     * A step that triggers CometJob after file generation is complete.
     */
    @Bean(name = "salescoverageCometJobTriggerStep")
    public Step salescoverageCometJobTriggerStep(@Qualifier("jobScopedStore") JobScopedStore store) {
        Tasklet tasklet = (StepContribution contribution, ChunkContext chunkContext) -> {
            cometJobTrigger.submit(store.getOutputFileName());
            return RepeatStatus.FINISHED;
        };
        return stepBuilderFactory.get("salescoverageCometJobTriggerStep").tasklet(tasklet).build();
    }
}
