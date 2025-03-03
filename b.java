@Bean(name = "salescoverageCometJobTriggerStep")
public Step salescoverageCometJobTriggerStep(@Qualifier("jobScopedStore") JobScopedStore store) {
    Tasklet tasklet = (StepContribution contribution, ChunkContext chunkContext) -> {
        cometJobTrigger.submit(store.getOutputFileName());
        return RepeatStatus.FINISHED;
    };
    return stepBuilderFactory.get("salescoverageCometJobTriggerStep").tasklet(tasklet).build();
}
