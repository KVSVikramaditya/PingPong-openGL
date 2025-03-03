package com.msim.seismic_datafeed.jobs.salescoverage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class SalescoverageDataReader implements ItemReader<SalescoverageInput> {
    private final SalescoverageDataProvider salescoverageDataProvider;
    private List<SalescoverageInput> data;
    private int nextIndex;

    public SalescoverageDataReader(SalescoverageDataProvider salescoverageDataProvider) {
        this.salescoverageDataProvider = salescoverageDataProvider;
        this.nextIndex = 0;
    }

    @Override
    public SalescoverageInput read() throws Exception {
        if (data == null) {
            data = salescoverageDataProvider.fetchSalescoverageData();
        }
        if (nextIndex < data.size()) {
            return data.get(nextIndex++);
        } else {
            return null;
        }
    }
}

@Slf4j
@Component
public class SalescoverageDataProcessor implements ItemProcessor<SalescoverageInput, SalescoverageOutput> {
    @Override
    public SalescoverageOutput process(SalescoverageInput item) throws Exception {
        log.info("Processing sales coverage data: {}", item);
        SalescoverageOutput output = new SalescoverageOutput();
        output.setField1(item.getField1());
        output.setField2(item.getField2());
        output.setField3(item.getField3());
        return output;
    }
}

@Slf4j
@Component
public class SalescoverageDataProvider {
    private final SalescoverageDao salescoverageDao;

    public SalescoverageDataProvider(SalescoverageDao salescoverageDao) {
        this.salescoverageDao = salescoverageDao;
    }

    public List<SalescoverageInput> fetchSalescoverageData() {
        log.info("Fetching sales coverage data from DAO");
        return salescoverageDao.getSalesCoverageRecords();
    }
}

@Component
public interface SalescoverageDao {
    List<SalescoverageInput> getSalesCoverageRecords();
}

@Slf4j
@Component
public class SalescoverageDaoImpl implements SalescoverageDao {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public SalescoverageDaoImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public List<SalescoverageInput> getSalesCoverageRecords() {
        log.info("Executing query to fetch sales coverage data from Snowflake");
        String sql = "SELECT field1, field2, field3 FROM sales_coverage_table";
        List<Map<String, Object>> rows = namedParameterJdbcTemplate.queryForList(sql, Map.of());
        return rows.stream().map(row -> {
            SalescoverageInput input = new SalescoverageInput();
            input.setField1((String) row.get("field1"));
            input.setField2((String) row.get("field2"));
            input.setField3((String) row.get("field3"));
            return input;
        }).toList();
    }
}
