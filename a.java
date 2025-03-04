package com.msim.seismic_datafeed.dao;

import com.msim.seismic_datafeed.jobs.salescoverage.model.SalescoverageData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SalescoverageDaoImpl {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public SalescoverageDaoImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public List<SalescoverageData> getSalesCoverageRecords() {
        log.info("Executing query to fetch sales coverage data from Snowflake");

        String sql = "SELECT SF_TEAM_ID, TEAM_CODE, TEAM_NAME, SF_TERRITORY_ID, TERRITORY_CODE, TERRITORY_NAME," +
                     " INTERNAL_SALES_PERSON_MSID, INTERNAL_SALES_PERSON_FULL_NAME," +
                     " EXTERNAL_SALES_PERSON_MSID, EXTERNAL_SALES_PERSON_FULL_NAME " +
                     "FROM sales_coverage_table";

        List<Map<String, Object>> rows = namedParameterJdbcTemplate.queryForList(sql, new MapSqlParameterSource());
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
        }).collect(Collectors.toList());
    }
}
