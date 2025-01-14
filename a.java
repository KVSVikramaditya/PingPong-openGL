package com.ms.msamg.seismic.client;

import okhttp3.*;
import org.springframework.stereotype.Component;

import javax.inject.Named;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.*;

@Component
@Named
public class CustomOkHttpClient {

    private final OkHttpClient okHttpClient;

    public CustomOkHttpClient() {
        // Custom CookieJar to manage cookies
        CookieJar cookieJar = new CookieJar() {
            private final Map<String, List<Cookie>> cookieStore = new HashMap<>();

            @Override
            public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                cookieStore.put(url.host(), cookies);
            }

            @Override
            public List<Cookie> loadForRequest(HttpUrl url) {
                return cookieStore.getOrDefault(url.host(), new ArrayList<>());
            }
        };

        // Proxy configuration
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy-app.ms.com", 8080));

        // Proxy authentication
        Authenticator proxyAuthenticator = (route, response) -> {
            String credential = Credentials.basic("username", "password");
            return response.request().newBuilder()
                    .header("Proxy-Authorization", credential)
                    .build();
        };

        // Custom interceptors for additional request configuration
        Interceptor requestInterceptor = chain -> {
            Request originalRequest = chain.request();
            Request modifiedRequest = originalRequest.newBuilder()
                    .header("Accept", "application/json")
                    .header("User-Agent", "Custom-OkHttpClient")
                    .build();
            return chain.proceed(modifiedRequest);
        };

        // Configure OkHttpClient with all custom settings
        this.okHttpClient = new OkHttpClient.Builder()
                .cookieJar(cookieJar) // Manage cookies
                .proxy(proxy) // Set proxy
                .proxyAuthenticator(proxyAuthenticator) // Authenticate proxy requests
                .addInterceptor(requestInterceptor) // Add custom headers
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS) // Connection timeout
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS) // Read timeout
                .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS) // Write timeout
                .retryOnConnectionFailure(true) // Retry on connection failure
                .build();
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }
}
