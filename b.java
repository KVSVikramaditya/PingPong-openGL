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
