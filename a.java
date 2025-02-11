package com.ms.msamg.seismic.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import javax.inject.Named;
import javax.net.ssl.*;

import okhttp3.OkHttpClient;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.protocol.HttpContext;
import org.springframework.stereotype.Component;

@Named
@Component
public class ApacheHttpClient extends CloseableHttpClient {

    private CloseableHttpClient httpClient;

    // Trust all certificates implementation
    protected TrustManager[] getTrustManagers() {
        return new TrustManager[]{
            new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    // Trust all clients
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    // Trust all servers
                }
            }
        };
    }

    public ApacheHttpClient() throws Exception {
        // Create a trust-all SSL context
        TrustManager[] trustAllCerts = getTrustManagers();
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

        // Create SSL socket factory
        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
            sslContext,
            NoopHostnameVerifier.INSTANCE
        );

        // Configure Proxy if needed
        HttpHost proxy = new HttpHost("proxy-app.ms.com", 8080);

        // Setup Cookie Store and Credentials Provider
        CookieStore cookieStore = new BasicCookieStore();
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

        // Define request configuration
        RequestConfig defaultRequestConfig = RequestConfig.custom()
            .setCookieSpec(CookieSpecs.DEFAULT)
            .setExpectContinueEnabled(true)
            .setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM, AuthSchemes.DIGEST))
            .setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC))
            .setProxy(proxy)
            .build();

        // Build HTTP client
        httpClient = HttpClients.custom()
            .setDefaultCookieStore(cookieStore)
            .setDefaultCredentialsProvider(credentialsProvider)
            .setSSLSocketFactory(sslSocketFactory)
            .setDefaultRequestConfig(defaultRequestConfig)
            .setProxy(proxy)
            .build();
    }

    @Override
    @SuppressWarnings("deprecation")
    public org.apache.http.params.HttpParams getParams() {
        return httpClient.getParams();
    }

    @Override
    @SuppressWarnings("deprecation")
    public ClientConnectionManager getConnectionManager() {
        return httpClient.getConnectionManager();
    }

    @Override
    public void close() throws IOException {
        httpClient.close();
    }

    @Override
    protected CloseableHttpResponse doExecute(HttpHost target, HttpRequest request, HttpContext context)
            throws IOException, ClientProtocolException {
        return httpClient.execute(target, request, context);
    }

    public CloseableHttpClient getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }
}
