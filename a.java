package com.msim.seismic_datafeed.jobs.salescoverage;

import com.msim.seismic_datafeed.utils.CSVFileUtils;
import com.msim.seismic_datafeed.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.*;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
public class SalescoverageBatchConfiguration {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private JobExecutionDecider cometJobDecider;

    @Autowired
    private CometJobTrigger cometJobTrigger;

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Value("${spring.batch.chunk.size}")
    private int chunkSize;

    @Value("${seismic.output.file.directory}")
    private String outputFileDirectory;

    @Bean(name = "LoadSalescoverageJob")
    public Job loadSalescoverageJob(
            @Qualifier("retrieveSalescoverage") Step retrieveSalescoverage,
            @Qualifier("salescoverageCometJobTriggerStep") Step salescoverageCometJobTriggerStep,
            StatisticsLoggerJobExecutionListener statisticsLoggerJobExecutionListener) {
        return jobBuilderFactory.get("LoadSalescoverageJob")
                .listener(statisticsLoggerJobExecutionListener)
                .incrementer(new RunIdIncrementer())
                .preventRestart()
                .flow(retrieveSalescoverage)
                .next(cometJobDecider)
                .on(CometJobDecider.CONTINUE).to(salescoverageCometJobTriggerStep)
                .from(cometJobDecider)
                .on(CometJobDecider.BREAK).end()
                .end()
                .build();
    }

    @Bean(name = "retrieveSalescoverage")
    public Step retrieveSalescoverage(
            @Qualifier("salescoverageDataReader") SalescoverageDataReader salescoverageDataReader,
            @Qualifier("salescoverageItemProcessor") CompositeItemProcessor<SalescoverageInput, SalescoverageOutput> salescoverageItemProcessor,
            @Qualifier("salescoverageFileItemWriter") FlatFileItemWriter<SalescoverageOutput> salescoverageFileItemWriter) {
        return stepBuilderFactory.get("retrieveSalescoverage")
                .<SalescoverageInput, SalescoverageOutput>chunk(chunkSize)
                .reader(salescoverageDataReader)
                .processor(salescoverageItemProcessor)
                .writer(salescoverageFileItemWriter)
                .build();
    }

    @Bean(name = "salescoverageDataReader")
    @StepScope
    public SalescoverageDataReader salescoverageDataReader(SalescoverageDataProvider salescoverageDataProvider) {
        return new SalescoverageDataReader(salescoverageDataProvider);
    }

    @Bean(name = "salescoverageDataProvider")
    public SalescoverageDataProvider salescoverageDataProvider() {
        return new SalescoverageDataProvider(namedParameterJdbcTemplate);
    }

    @Bean(name = "salescoverageItemProcessor")
    @StepScope
    public CompositeItemProcessor<SalescoverageInput, SalescoverageOutput> salescoverageItemProcessor(
            @Autowired SalescoverageDataProcessor salescoverageDataProcessor) throws Exception {
        List<Object> delegates = new ArrayList<>(1);
        delegates.add(salescoverageDataProcessor);
        CompositeItemProcessor<SalescoverageInput, SalescoverageOutput> compositeItemProcessor = new CompositeItemProcessor<>();
        compositeItemProcessor.setDelegates(delegates);
        compositeItemProcessor.afterPropertiesSet();
        return compositeItemProcessor;
    }

    @Bean(name = "salescoverageFileItemWriter")
    @StepScope
    public FlatFileItemWriter<SalescoverageOutput> salescoverageFileItemWriter(
            @Value("#{jobParameters['asOfDate']}") String asOfDate) {
        String fileName = "SalesCoverage_" + asOfDate + ".csv";
        String fullFilePath = outputFileDirectory + fileName;
        FileSystemResource fileResource = new FileSystemResource(fullFilePath);
        BeanWrapperFieldExtractor<SalescoverageOutput> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[]{"field1", "field2", "field3"}); // Modify as needed
        DelimitedLineAggregator<SalescoverageOutput> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(",");
        lineAggregator.setFieldExtractor(fieldExtractor);
        FlatFileItemWriter<SalescoverageOutput> fileItemWriter = new FlatFileItemWriter<>();
        fileItemWriter.setResource(fileResource);
        fileItemWriter.setLineAggregator(lineAggregator);
        fileItemWriter.setShouldDeleteIfEmpty(true);
        fileItemWriter.setShouldDeleteIfExists(true);
        return fileItemWriter;
    }

}
