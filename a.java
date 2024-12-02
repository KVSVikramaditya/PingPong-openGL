package com.ms.datalink.globalDatalink.config;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        try {
            SSLContext sslContext = SSLContexts.custom()
                    .loadTrustMaterial((chain, authType) -> true)
                    .build();

            SSLConnectionSocketFactory socketFactory =
                    new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);

            CloseableHttpClient httpClient = HttpClients.custom()
                    .setSSLSocketFactory(socketFactory)
                    .build();

            HttpComponentsClientHttpRequestFactory requestFactory =
                    new HttpComponentsClientHttpRequestFactory(httpClient);

            return new RestTemplate(requestFactory);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create RestTemplate with SSL support", e);
        }
    }
}


@Service
@Slf4j
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

    @Autowired
    public AuthService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Basic " + clientAuth);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "password");
        formData.add("username", username);
        formData.add("password", password);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(jwtTokenUrl, HttpMethod.POST, requestEntity, String.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonObject jsonObject = JsonParser.parseString(response.getBody()).getAsJsonObject();
                return jsonObject.get("access_token").getAsString();
            } else {
                throw new RuntimeException("Failed to fetch token: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error while fetching JWT token", e);
            throw new RuntimeException("Error while fetching JWT token", e);
        }
    }
}
