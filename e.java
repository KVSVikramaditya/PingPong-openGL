package com.msim.seismic_datafeed.jobs.salescoverage;

import com.msim.seismic_datafeed.jobs.salescoverage.model.SalescoverageData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class SalescoverageDaoImpl implements SalescoverageDao {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public SalescoverageDaoImpl(@Qualifier("snowflakeJdbcTemplate") NamedParameterJdbcTemplate snowflakeJdbcTemplate) {
        this.namedParameterJdbcTemplate = snowflakeJdbcTemplate;
    }

    @Override
    public SalescoverageData getSalesCoverageRecords() {
        log.info("Executing query to fetch sales coverage data from Snowflake");
        
        String sql = "SELECT * FROM V_TERRITORY_COVERAGE";
        List<Map<String, Object>> rows = namedParameterJdbcTemplate.queryForList(sql, Map.of());

        // Return the first row mapped to SalescoverageData, or null if no rows
        return rows.stream()
                   .map(row -> {
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
                   })
                   .findFirst()
                   .orElse(null);
    }
}
