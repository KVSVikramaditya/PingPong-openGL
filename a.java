package com.ms.datalink.globalDatalink.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import javax.net.ssl.SSLContext;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class AuthService {

    @Value("${pd.api.url}")
    private String pdApiUrl;

    @Value("${pd.api.client.auth}")
    private String clientAuth; // Base64 encoded client ID and secret

    @Value("${oauth.token.url}")
    private String jwtTokenUrl;

    private final RestTemplate restTemplate;

    /**
     * Initializes a RestTemplate with SSL configuration.
     */
    private RestTemplate initRestTemplate() {
        try {
            SSLContext sslContext = SSLContexts.custom()
                    .loadTrustMaterial((chain, authType) -> true) // Trust all certificates
                    .build();

            PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
            connectionManager.setMaxTotal(200);
            connectionManager.setDefaultMaxPerRoute(50);

            CloseableHttpClient httpClient = HttpClients.custom()
                    .setSSLContext(sslContext)
                    .setConnectionManager(connectionManager)
                    .build();

            HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
            factory.setConnectTimeout(30000);
            factory.setReadTimeout(30000);

            return new RestTemplate(factory);
        } catch (GeneralSecurityException e) {
            log.error("Error initializing RestTemplate with SSL: ", e);
            throw new RuntimeException("Failed to initialize RestTemplate with SSL", e);
        }
    }

    /**
     * Fetches a JWT token from the OAuth service.
     *
     * @return The JWT token.
     */
    public String getToken() {
        RestTemplate secureRestTemplate = initRestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Basic " + clientAuth);

        Map<String, String> formData = new HashMap<>();
        formData.put("grant_type", "password");
        formData.put("username", "morganstanley_rest_api_user");
        formData.put("password", "Morst@n!3y");

        HttpEntity<Map<String, String>> request = new HttpEntity<>(formData, headers);

        try {
            ResponseEntity<String> response = secureRestTemplate.exchange(
                    jwtTokenUrl, HttpMethod.POST, request, String.class);

            log.info("Token response: {}", response.getBody());
            JsonObject jsonObject = JsonParser.parseString(response.getBody()).getAsJsonObject();
            return jsonObject.get("access_token").getAsString();
        } catch (Exception e) {
            log.error("Failed to fetch token from OAuth service: ", e);
            throw new RuntimeException("Failed to fetch token", e);
        }
    }
}
