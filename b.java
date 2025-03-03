package com.msim.seismic_datafeed.jobs.salescoverage;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;

@Data
public class SalescoverageData {
    private String sfTeamId;
    private String teamCode;
    private String teamName;
    private String sfTerritoryId;
    private String territoryCode;
    private String territoryName;
    private String internalSalesPersonMsid;
    private String internalSalesPersonFullName;
    private String externalSalesPersonMsid;
    private String externalSalesPersonFullName;
}

@Data
public class SalescoverageDataItem {
    private String sfTeamId;
    private String teamCode;
    private String teamName;
    private String sfTerritoryId;
    private String territoryCode;
    private String territoryName;
    private String internalSalesPersonMsid;
    private String internalSalesPersonFullName;
    private String externalSalesPersonMsid;
    private String externalSalesPersonFullName;
}

@Slf4j
@Component
public class SalescoverageDataReader implements ItemReader<SalescoverageData> {
    private final SalescoverageDataProvider salescoverageDataProvider;
    private List<SalescoverageData> data;
    private int nextIndex;

    public SalescoverageDataReader(SalescoverageDataProvider salescoverageDataProvider) {
        this.salescoverageDataProvider = salescoverageDataProvider;
        this.nextIndex = 0;
    }

    @Override
    public SalescoverageData read() throws Exception {
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
public class SalescoverageDataProcessor implements ItemProcessor<SalescoverageData, SalescoverageDataItem> {
    @Override
    public SalescoverageDataItem process(SalescoverageData item) throws Exception {
        log.info("Processing sales coverage data: {}", item);
        SalescoverageDataItem output = new SalescoverageDataItem();
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

    public List<SalescoverageData> fetchSalescoverageData() {
        log.info("Fetching sales coverage data from DAO");
        return salescoverageDao.getSalesCoverageRecords();
    }
}

@Component
public interface SalescoverageDao {
    List<SalescoverageData> getSalesCoverageRecords();
}

@Slf4j
@Component
public class SalescoverageDaoImpl implements SalescoverageDao {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public SalescoverageDaoImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public List<SalescoverageData> getSalesCoverageRecords() {
        log.info("Executing query to fetch sales coverage data from Snowflake");
        String sql = "SELECT SF_TEAM_ID, TEAM_CODE, TEAM_NAME, SF_TERRITORY_ID, TERRITORY_CODE, TERRITORY_NAME, INTERNAL_SALES_PERSON_MSID, INTERNAL_SALES_PERSON_FULL_NAME, EXTERNAL_SALES_PERSON_MSID, EXTERNAL_SALES_PERSON_FULL_NAME FROM sales_coverage_table";
        List<Map<String, Object>> rows = namedParameterJdbcTemplate.queryForList(sql, Map.of());
        return rows.stream().map(row -> {
            SalescoverageData data = new SalescoverageData();
            data.setSfTeamId((String) row.get("SF_TEAM_ID"));
            data.setTeamCode((String) row.get("TEAM_CODE"));
            data.setTeamName((String) row.get("TEAM_NAME"));
            data.setSfTerritoryId((String) row.get("SF_TERRITORY_ID"));
            data.setTerritoryCode((String) row.get("TERRITORY_CODE"));
            data.setTerritoryName((String) row.get("TERRITORY_NAME"));
            data.setInternalSalesPersonMsid((String) row.get("INTERNAL_SALES_PERSON_MSID"));
            data.setInternalSalesPersonFullName((String) row.get("INTERNAL_SALES_PERSON_FULL_NAME"));
            data.setExternalSalesPersonMsid((String) row.get("EXTERNAL_SALES_PERSON_MSID"));
            data.setExternalSalesPersonFullName((String) row.get("EXTERNAL_SALES_PERSON_FULL_NAME"));
            return data;
        }).toList();
    }
}
