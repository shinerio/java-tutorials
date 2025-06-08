package com.shinerio.tutorial.config;

import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.zookeeper.ZookeeperDataSource;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SentinelZookeeperConfig {

    @Value("${spring.cloud.sentinel.datasource.ds.zookeeper.server-addr}")
    private String zookeeperAddress;
    @Value("${spring.application.name}")
    private String appName;

    @PostConstruct
    public void loadRules() {
        loadFromZookeeper();
    }

    private void loadFromZookeeper() {
        String path = "/sentinel_rule_config/" + appName;
        ReadableDataSource<String, List<FlowRule>> flowRuleDataSource =
                new ZookeeperDataSource<>(zookeeperAddress, path, source -> JSON.parseObject(source, new TypeReference<>() {}));
        FlowRuleManager.register2Property(flowRuleDataSource.getProperty());
    }
}
