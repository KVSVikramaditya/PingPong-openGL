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


@Override
public SalescoverageData read() throws Exception {
    if (data == null) {
        data = salescoverageDataProvider.fetchSalescoverageData();
    }
    if (nextIndex < data.size()) {
        return data.get(nextIndex++);
    } else {
        return null; // Signals the end of reading
    }
}

