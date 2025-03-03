@Bean(name = "retrieveSalescoverage")
public Step retrieveSalescoverage(
        @Qualifier("salescoverageDataReader") SalescoverageDataReader salescoverageDataReader,
        @Qualifier("salescoverageItemProcessor") SalescoverageDataProcessor salescoverageDataProcessor,
        @Qualifier("salescoverageFileItemWriter") FlatFileItemWriter<SalescoverageDataItem> fileItemWriter,
        @Value("${spring.batch.chunk.size}") int chunkSize
) {
    return stepBuilderFactory.get("retrieveSalescoverage")
            .<SalescoverageData, SalescoverageDataItem>chunk(chunkSize)
            .reader(salescoverageDataReader)
            .processor(salescoverageDataProcessor)
            .writer(fileItemWriter)
            .build();
}




@Bean(name = "salescoverageDataReader")
@StepScope
public SalescoverageDataReader salescoverageDataReader(SalescoverageDataProvider dataProvider) {
    return new SalescoverageDataReader(dataProvider);
}




@Bean(name = "salescoverageDataProvider")
public SalescoverageDataProvider salescoverageDataProvider(
    @Qualifier("snowflakeJdbcTemplate") NamedParameterJdbcTemplate namedParameterJdbcTemplate
) {
    // Here, the Provider internally creates a DAO
    return new SalescoverageDataProvider(namedParameterJdbcTemplate);
}


@Slf4j
public class SalescoverageDataProvider {
    private final SalescoverageDaoImpl dao;

    public SalescoverageDataProvider(NamedParameterJdbcTemplate jdbcTemplate) {
        // Constructing DAO internally
        this.dao = new SalescoverageDaoImpl(jdbcTemplate);
    }

    public List<SalescoverageData> fetchSalescoverageData() {
        log.info("Fetching sales coverage data from DAO (internally created).");
        return dao.getSalesCoverageRecords();
    }
}
