import com.fasterxml.jackson.databind.ObjectMapper;
import msjava.base.scv.SecureCredentialsVault;
import msjava.base.slf4j.ContextLogger;
import org.apache.http.HttpHost;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.*;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

@Component
public class TokenFeed2 {
    private static final Logger LOGGER = ContextLogger.safeLogger();

    public RestTemplate initRestTemplate() {
        // Build OAuth2RestTemplate:
        TrustManager[] trustAllCerts = getTrustManager();

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = getHostNameVerifier();
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (GeneralSecurityException e) {
            LOGGER.error("Exception.", e);
        }

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setOutputStreaming(false);
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(requestFactory);
        return restTemplate;
    }

    protected TrustManager[] getTrustManager() {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }

                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };
        return trustAllCerts;
    }

    protected HostnameVerifier getHostNameVerifier() {
        return (hostname, session) -> hostname != null && session != null;
    }

    public String getToken() throws Exception {
        initRestTemplate();
        String returnedPassword = retrievePassword();
        if (returnedPassword.isEmpty()) {
            throw new Exception("Password retrieval failed.");
        }
        String token = sendTokenRequest(returnedPassword);
        LOGGER.info("Access Token: {}", token);
        return token;
    }

    private String retrievePassword() throws Exception {
        SecureCredentialsVault scv = new SecureCredentialsVault();
        String returnedPassword = new String(scv.getTextCred("im/marketing/MSIMSeismic/prod/seismic", "seismic-password"));
        if (returnedPassword.isEmpty()) {
            LOGGER.error("Retrieved empty password for key: seismic-password");
        }
        String decodedPwd = java.net.URLDecoder.decode(returnedPassword, StandardCharsets.UTF_8.name());
        decodedPwd = decodedPwd.replace("\n", "");
        return decodedPwd;
    }

    private String sendTokenRequest(String decodedPassword) throws Exception {
        StringBuffer authToken = new StringBuffer();
        RestTemplate restTemplate = initRestTemplate();
        String uri = "https://auth.seismic.com/tenants/morganstanley/connect/token";
        String payload = buildPayload(decodedPassword);

        // Prepare Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Build the HTTP entity with payload and headers
        HttpEntity<String> requestEntity = new HttpEntity<>(payload, headers);
        LOGGER.info("POST URL: {}", uri);
        LOGGER.info("Headers: {}", requestEntity.getHeaders());
        LOGGER.info("Body: {}", requestEntity.getBody());

        try {
            // Send POST request using RestTemplate
            ResponseEntity<String> response = restTemplate.exchange(
                    uri,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            // Check the status code
            if (response.getStatusCode() != HttpStatus.OK) {
                LOGGER.error("Failed to retrieve token, status: {}", response.getStatusCode());
                throw new Exception("Failed to retrieve token");
            }

            // Parse the response
            String responseBody = response.getBody();
            ObjectMapper mapper = new ObjectMapper();
            Token token = mapper.readValue(responseBody, Token.class);

            // Return the access token
            authToken.append(token.getTokenType()).append(" ").append(token.getAccessToken());
            return authToken.toString();
        } catch (Exception e) {
            LOGGER.error("Error occurred while fetching token", e);
            throw e;
        }
    }

    private String buildPayload(String password) {
        return "grant_type=password" +
                "&client_id=3490eed2-e3f8-431d-ac32-3c7897cfbafa" +
                "&client_secret=4c9a9f4b-eca6-4c00-b4c5-c28e96275f8a" +
                "&user_name=SeismicAPI@seismic.com" +
                "&password=" + password +
                "&scope=library%20download%20reporting";
    }

    public static void main(String[] args) {
        try {
            TokenFeed2 tokenFeed = new TokenFeed2();
            String token = tokenFeed.getToken();
            System.out.println("Access Token: " + token);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
