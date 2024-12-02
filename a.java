package com.ms.datalink.globalDatalink.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    @Value("${jwt.token.url}")
    private String jwtTokenUrl;

    @Value("${pd.api.client.auth}")
    private String clientAuth;

    @Value("${pd.api.username}")
    private String username;

    @Value("${pd.api.password}")
    private String password;

    private final RestTemplate restTemplate;

    public String getToken() {
        // Prepare headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Basic " + clientAuth);

        // Prepare form data
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "password");
        formData.add("username", username);
        formData.add("password", password);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    jwtTokenUrl, HttpMethod.POST, requestEntity, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("Successfully retrieved JWT token.");
                // Parse and return the token
                return new com.google.gson.JsonParser()
                        .parse(response.getBody())
                        .getAsJsonObject()
                        .get("access_token")
                        .getAsString();
            } else {
                throw new RuntimeException("Failed to fetch token: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error while fetching JWT token", e);
            throw new RuntimeException("Error while fetching JWT token", e);
        }
    }
}
