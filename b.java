package com.msim.seismic_datafeed;

import com.msim.seismic_datafeed.jobs.salescoverage.model.V_TERRITORY_COVERAGE_dao;
import com.msim.seismic_datafeed.config.DataFeedMainConfiguration;
import lombok.extern.slf4j.Slf4j;
import msjava.base.application.SpringMain;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@SpringBootApplication
public class SeismicDataFeedMain {

    @Autowired
    private V_TERRITORY_COVERAGE_dao territoryCoverageDao;

    public static void main(String[] args) {
        log.info("Starting SeismicDataFeed MAIN Application");

        // 1) Create and run the Spring context using your custom SpringMain
        SpringMain springMain = new SpringMain();
        // NOTE: springMain.run(...) should return an ApplicationContext
        ApplicationContext context = springMain.run("-autoconf", DataFeedMainConfiguration.class.getName());

        log.info("Started SeismicDataFeed MAIN Application");

        // 2) Retrieve *this* main class from the Spring context, so its @Autowired fields are set
        SeismicDataFeedMain mainBean = context.getBean(SeismicDataFeedMain.class);

        // 3) Now you can safely call the DAO method
        var records = mainBean.territoryCoverageDao.getAllFamilyRecords();
        log.info("Fetched {} records from Snowflake.", records.size());

        // Optionally print them
        records.forEach(record -> log.info("Row: {}", record));
    }
}
