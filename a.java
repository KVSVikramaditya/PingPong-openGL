package com.msim.seismic_datafeed.jobs.salescoverage;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import net.snowflake.client.jdbc.SnowflakeBasicDataSource;
import org.springframework.stereotype.Component;

@Component
public class SnowflakeDataSourceConfig {
    
    private static final Logger log = LoggerFactory.getLogger(SnowflakeDataSourceConfig.class);
    private static final String ENCR_PRIVATE_KEY_PREFIX = "-----BEGIN ENCRYPTED PRIVATE KEY-----";
    private static final String ENCR_PRIVATE_KEY_SUFFIX = "-----END ENCRYPTED PRIVATE KEY-----";
    private static final String CERT_ALGO = "RSA";

    private final String sfJdbcConnectionUrl = "jdbc:snowflake://devbcs.east-us-2.privatelink-snowflakecomputing.com/?useProxy=true&proxyHost=webproxy-sf-nonprod-na.ms.com&proxyPort=8080";
    private final String sfDatabaseWarehouse = "IM_CDP_DB";
    private final String sfDatabaseServer = "WEBTOOLSDATADB";
    private final String sfDatabaseSchema = "public";
    private final String sfRole = "MKT_CDP_READ";
    private final String sfUsername = "imseismafg";
    private final String namespace = "im/marketing/MSIMSeismic/dev/seismic";
    private final String passphraseKey = "snowflake_dev_password";
    private final String privateKey = "snowflake_private_key";

    @Bean(name = "snowflakeJdbcTemplate")
    @Profile({"dev", "windev", "qa", "QA"})
    public NamedParameterJdbcTemplate getSnowflakeJdbcTemplate() {
        return new NamedParameterJdbcTemplate(snowflakeDataSource());
    }

    @Bean(name = "snowflakeDataSource")
    @Profile({"dev", "windev", "qa", "QA"})
    public SnowflakeBasicDataSource snowflakeDataSource() {
        SnowflakeBasicDataSource dataSource = new SnowflakeBasicDataSource();
        ScvSecretsProviderUtil scvSecretsProviderUtil = new ScvSecretsProviderUtil();
        dataSource.setPrivateKey(scvSecretsProviderUtil.fetchPrivateKey(namespace, passphraseKey, privateKey));
        dataSource.setUrl(sfJdbcConnectionUrl);
        dataSource.setWarehouse(sfDatabaseWarehouse);
        dataSource.setDatabaseName(sfDatabaseServer);
        dataSource.setSchema(sfDatabaseSchema);
        dataSource.setUser(sfUsername);
        dataSource.setRole(sfRole);
        dataSource.setSsl(true);
        return dataSource;
    }

    private static class ScvSecretsProviderUtil {
        public PrivateKey fetchPrivateKey(String namespace, String passphraseKey, String privateKey) {
            try {
                SecureCredentialsVault scv = new SecureCredentialsVault();
                String passphrase = scv.tellKey(namespace, passphraseKey).getDataAsString();
                passphrase = passphrase.replace(ENCR_PRIVATE_KEY_PREFIX, StringUtils.EMPTY)
                                      .replace(ENCR_PRIVATE_KEY_SUFFIX, StringUtils.EMPTY);
                
                String encrypted = scv.tellKey(namespace, privateKey).getDataAsString();
                encrypted = encrypted.replace(ENCR_PRIVATE_KEY_PREFIX, StringUtils.EMPTY)
                                     .replace(ENCR_PRIVATE_KEY_SUFFIX, StringUtils.EMPTY);
                
                EncryptedPrivateKeyInfo pkInfo = new EncryptedPrivateKeyInfo(Base64.getMimeDecoder().decode(encrypted));
                PBEKeySpec keySpec = new PBEKeySpec(passphrase.toCharArray());
                SecretKeyFactory pbeKeyFactory = SecretKeyFactory.getInstance(pkInfo.getAlgName());
                PKCS8EncodedKeySpec encodedKeySpec = pkInfo.getKeySpec(pbeKeyFactory.generateSecret(keySpec));
                KeyFactory keyFactory = KeyFactory.getInstance(CERT_ALGO);
                return keyFactory.generatePrivate(encodedKeySpec);
            } catch (Exception e) {
                log.error("Error while retrieving the secrets from SCV", e);
                return null;
            }
        }
    }
}
