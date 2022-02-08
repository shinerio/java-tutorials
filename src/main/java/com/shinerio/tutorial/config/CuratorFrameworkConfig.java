package com.shinerio.tutorial.config;

import lombok.Data;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CuratorFrameworkConfig {

    @Data
    @Configuration
    @ConfigurationProperties(prefix = "zookeeper")
    public static class ZookeeperConfig {
        /**
         * 重试次数
         */
        private int retryCount;

        /**
         * 重试间隔时间
         */
        private int elapsedTime;

        /**
         * 连接地址，多个地址用逗号隔开，如ip1:port1,ip2:port2
         */
        private String address;

        /**
         * 绘画过期时间，单位ms
         */
        private int sessionTimeout;

        /**
         * 连接超时时间，单位ms
         */
        private int connectionTimeout;
    }

    @Bean
    public CuratorFramework curatorFramework(ZookeeperConfig zookeeperConfig) {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(zookeeperConfig.getAddress(),
                zookeeperConfig.getSessionTimeout(),
                zookeeperConfig.getConnectionTimeout(),
                new RetryNTimes(zookeeperConfig.retryCount, zookeeperConfig.elapsedTime));
        curatorFramework.start();
        return curatorFramework;
    }

}
