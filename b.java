package com.ms.msamg.imwebapi.dao.taxCalculatorData;

import com.ms.msamg.imwebapi.config.SnowflakeDataSourceConfig;
import com.ms.msamg.imwebapi.model.taxCalculator.TaxState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Component
public class TaxDataDAOImpl implements TaxDataDAO {

    private static final Logger log = LoggerFactory.getLogger(TaxDataDAOImpl.class);

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public TaxDataDAOImpl(@Qualifier(SnowflakeDataSourceConfig.SNOWFLAKE_JDBC_TEMPLATE)
                          NamedParameterJdbcTemplate snowflakeJdbcTemplate) {
        this.namedParameterJdbcTemplate = snowflakeJdbcTemplate;
    }

    @Override
    public String getState(String code) {
        // Example usage, though not required to run a SELECT * query
        final String sql = "SELECT state FROM SomeTable WHERE code = :code";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("code", code);

        // This is just an example: implement your actual logic
        List<String> results = namedParameterJdbcTemplate.queryForList(sql, params, String.class);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public int createState(TaxState taxState) {
        // Example of an INSERT
        final String sql = "INSERT INTO SomeTable (someColumn) VALUES (:someValue)";
        BeanPropertySqlParameterSource source = new BeanPropertySqlParameterSource(taxState);
        return namedParameterJdbcTemplate.update(sql, source);
    }

    @Override
    public int removeState(TaxState taxState) {
        // Example of a DELETE
        final String sql = "DELETE FROM SomeTable WHERE someColumn = :someValue";
        BeanPropertySqlParameterSource source = new BeanPropertySqlParameterSource(taxState);
        return namedParameterJdbcTemplate.update(sql, source);
    }

    // Example method to execute "SELECT * FROM family"
    // This returns a List of Map<String, Object> so you can iterate over each row.
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAllFamilyRecords() {
        log.info("Executing query: SELECT * FROM family");
        final String sql = "SELECT * FROM family";
        return namedParameterJdbcTemplate.queryForList(sql, new MapSqlParameterSource());
    }
}
