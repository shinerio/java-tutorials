package com.shinerio.tutorial.config;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.Watch;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.Charset;

@Configuration
@ConfigurationProperties(prefix = "etcd")
@Setter
@Getter
public class EtcdConfig {
    private String endpoints;
    private String username;
    private String password;

    @Bean
    public Client etcdClient() {
        return Client.builder().endpoints(endpoints.split(","))
                .user(ByteSequence.from(username, Charset.defaultCharset()))
                .password(ByteSequence.from(password, Charset.defaultCharset()))
                .build();
    }

    @Bean
    public KV kvClient(Client client) {
        return client.getKVClient();
    }

    @Bean
    public Watch watchClient(Client client) {
        return client.getWatchClient();
    }
}
