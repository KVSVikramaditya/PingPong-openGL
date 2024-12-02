package com.ms.datalink.globalDatalink.config;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

@Configuration
public class SSLConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder)
            throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        // Create custom SSL context
        SSLContext sslContext = SSLContexts.custom()
                .loadTrustMaterial((chain, authType) -> true) // Trust all certificates
                .build();

        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);

        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(csf)
                .build();

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);

        // Build RestTemplate with custom SSL
        return builder
                .requestFactory(() -> requestFactory)
                .setConnectTimeout(30000)
                .setReadTimeout(30000)
                .build();
    }
}

package com.ms.datalink.globalDatalink.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    @Value("${pd.api.url}")
    private String pdApiUrl;

    @Value("${pd.api.client.auth}")
    private String clientAuth; // Base64 encoded client id and secret

    @Value("${pd.api.username}")
    private String username;

    @Value("${pd.api.password}")
    private String password;

    @Value("${jwt.token.url}")
    private String jwtTokenUrl;

    private final RestTemplate restTemplate;

    public String getToken() {
        // Prepare headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Basic " + clientAuth);

        // Prepare form data
        Map<String, String> formData = new HashMap<>();
        formData.put("grant_type", "password");
        formData.put("username", username);
        formData.put("password", password);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(formData, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    jwtTokenUrl, HttpMethod.POST, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonObject jsonObject = JsonParser.parseString(response.getBody()).getAsJsonObject();
                log.info("Successfully retrieved JWT token.");
                return jsonObject.get("access_token").getAsString();
            } else {
                throw new RuntimeException("Failed to retrieve JWT token: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error while fetching JWT token", e);
            throw new RuntimeException("Error while fetching JWT token", e);
        }
    }
}
