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
        output.setSfTeamId(item.getSfTeamId());
        output.setTeamCode(item.getTeamCode());
        output.setTeamName(item.getTeamName());
        output.setSfTerritoryId(item.getSfTerritoryId());
        output.setTerritoryCode(item.getTerritoryCode());
        output.setTerritoryName(item.getTerritoryName());
        output.setInternalSalesPersonMsid(item.getInternalSalesPersonMsid());
        output.setInternalSalesPersonFullName(item.getInternalSalesPersonFullName());
        output.setExternalSalesPersonMsid(item.getExternalSalesPersonMsid());
        output.setExternalSalesPersonFullName(item.getExternalSalesPersonFullName());
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
        String sql = "SELECT SF_TEAM_ID, TEAM_CODE, TEAM_NAME, SF_TERRITORY_ID, TERRITORY_CODE, TERRITORY_NAME, INTERNAL_SALES_PERSON_MSID, INTERNAL_SALES_PERSON_FULL_NAME, EXTERNAL_SALES_PERSON_MSID, EXTERNAL_SALES_PERSON_FULL_NAME FROM sales_coverage_table";
        List<Map<String, Object>> rows = namedParameterJdbcTemplate.queryForList(sql, Map.of());
        return rows.stream().map(row -> {
            SalescoverageInput input = new SalescoverageInput();
            input.setSfTeamId((String) row.get("SF_TEAM_ID"));
            input.setTeamCode((String) row.get("TEAM_CODE"));
            input.setTeamName((String) row.get("TEAM_NAME"));
            input.setSfTerritoryId((String) row.get("SF_TERRITORY_ID"));
            input.setTerritoryCode((String) row.get("TERRITORY_CODE"));
            input.setTerritoryName((String) row.get("TERRITORY_NAME"));
            input.setInternalSalesPersonMsid((String) row.get("INTERNAL_SALES_PERSON_MSID"));
            input.setInternalSalesPersonFullName((String) row.get("INTERNAL_SALES_PERSON_FULL_NAME"));
            input.setExternalSalesPersonMsid((String) row.get("EXTERNAL_SALES_PERSON_MSID"));
            input.setExternalSalesPersonFullName((String) row.get("EXTERNAL_SALES_PERSON_FULL_NAME"));
            return input;
        }).toList();
    }
}
