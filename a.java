package com.ms.msamg.imwebapi.config;

import com.ms.msamg.imwebapi.util.ScvSecretsProviderUtil;
import net.snowflake.client.jdbc.SnowflakeBasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Configuration  // Marks this class as a Spring Configuration class
public class SnowflakeDataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(SnowflakeDataSourceConfig.class);

    // Directly defining Snowflake credentials as variables instead of fetching from properties
    private final String sfAccount = "devbcs.east_us-2.privatelink.snowflakecomputing.com";
    private final String sfProxyHost = "webproxy=sf_nonprod=na.ms.com";
    private final String sfProxyPort = "8080";
    private final String sfWarehouse = "APP_CORE_IH_XS";
    private final String sfDatabase = "WEBT00LSDATADB";
    private final String sfSchema = "public";
    private final String sfRole = "WEBT00LSDATADB_WRITE_ROLE";
    private final String sfUsername = "imwebapid";
    private final String namespace = "im/msamg/imwebapi/dev";
    private final String passphraseKey = "snowflake_passphrase";
    private final String privateKey = "snowflake_private_key";

    // Constructing the JDBC URL manually
    private final String sfJdbcConnectionUrl = String.format(
            "jdbc:snowflake://%s/?useProxy=true&proxyHost=%s&proxyPort=%s",
            sfAccount, sfProxyHost, sfProxyPort
    );

    /**
     * Creates a NamedParameterJdbcTemplate bean for executing SQL queries on Snowflake.
     */
    @Bean(name = "snowflakeJdbcTemplate")
    @Profile({"dev", "windev", "qa", "QA"})  // Bean will be active only in these environments
    public NamedParameterJdbcTemplate getSnowflakeJdbcTemplate() {
        return new NamedParameterJdbcTemplate(snowflakeDataSource());
    }

    /**
     * Configures and returns a Snowflake JDBC DataSource with credentials fetched securely.
     */
    @Bean(name = "snowflakeDataSource")
    @Profile({"dev", "windev", "qa", "QA"})  // Limits this bean to certain profiles
    public SnowflakeBasicDataSource snowflakeDataSource() {
        SnowflakeBasicDataSource dataSource = new SnowflakeBasicDataSource();
        
        // Utility class for securely retrieving private key
        ScvSecretsProviderUtil scvSecretsProviderUtil = new ScvSecretsProviderUtil();

        // Fetch and set private key for Snowflake authentication
        dataSource.setPrivateKey(scvSecretsProviderUtil.fetchPrivateKey(namespace, passphraseKey, privateKey));
        
        // Setting Snowflake connection parameters
        dataSource.setUrl(sfJdbcConnectionUrl);
        dataSource.setWarehouse(sfWarehouse);
        dataSource.setDatabaseName(sfDatabase);
        dataSource.setSchema(sfSchema);
        dataSource.setUser(sfUsername);
        dataSource.setRole(sfRole);
        dataSource.setSsl(true);  // Enforce SSL for security

        return dataSource;
    }
}
public static void main(String[] args) {
    SnowflakeDataSourceConfig config = new SnowflakeDataSourceConfig();
    System.out.println("Snowflake JDBC URL: " + config.snowflakeDataSource().getUrl());
}
