package com.shinerio.tutorial;

import org.junit.jupiter.api.Test;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.SecureRandom;

public class MTLSHttpClientTest {

    private KeyStore loadKeyStore(String filePath, String password) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        try (var fis = Path.of(filePath).toFile().exists()
                ? Path.of(filePath).toFile().toURI().toURL().openStream()
                : MTLSHttpClientTest.class.getClassLoader().getResourceAsStream(filePath)) {
            if (fis == null) {
                throw new IOException("KeyStore not found: " + filePath);
            }
            keyStore.load(fis, password.toCharArray());
        }
        return keyStore;
    }

    private SSLParameters createSSLParameters() {
        SSLParameters params = new SSLParameters();
        params.setEndpointIdentificationAlgorithm("HTTPS"); // 验证服务器域名
        return params;
    }

    @Test
    public void testMTLS() throws Exception {
        // 加载客户端证书、私钥
        KeyStore keyStore = loadKeyStore("certificate/client.jks", "changeit");
        KeyStore trustStore = loadKeyStore("certificate/client_truststore.jks", "changeit");

        // 创建KeyManagerFactory
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, "changeit".toCharArray());

        // 创建TrustManagerFactory
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        // 创建SSLContext
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(
                kmf.getKeyManagers(),
                tmf.getTrustManagers(),
                new SecureRandom()
        );

        // 创建带有mTLS配置的HttpClient
        HttpClient client = HttpClient.newBuilder()
                .sslContext(sslContext)
                .sslParameters(createSSLParameters())
                .build();

        // 发送HTTP请求
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://localhost:5000/hello"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // 打印响应
        System.out.println("Status code: " + response.statusCode());
        System.out.println("Response body: " + response.body());
    }
}
