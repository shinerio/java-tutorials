package com.shinerio.tutorial.sentinel;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.fastjson.JSON;
import com.shinerio.tutorial.JavaTutorialTests;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.List;

public class SentinelZookeeperConfigTests extends JavaTutorialTests {

    @Value("${spring.cloud.sentinel.datasource.ds.zookeeper.server-addr}")
    private String zookeeperAddress;
    @Value("${spring.application.name}")
    private String appName;

    @Test
    public void sendConfigToZookeeper() throws Exception {
        String path = "/sentinel_rule_config/" + appName;
        List<FlowRule> rules = new ArrayList<>();
        FlowRule accountFindAllRule = new FlowRule("account_find_all");
        // set limit qps to 2
        accountFindAllRule.setCount(2);
        accountFindAllRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        accountFindAllRule.setLimitApp("default");
        rules.add(accountFindAllRule);

        FlowRule accountFindByIdRule = new FlowRule("account_find_by_id");
        // set limit qps to 2
        accountFindByIdRule.setCount(2);
        accountFindByIdRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        accountFindByIdRule.setLimitApp("default");
        rules.add(accountFindByIdRule);

        FlowRule accountDelete = new FlowRule("account_delete");
        // set limit qps to 1
        accountDelete.setCount(1);
        accountDelete.setGrade(RuleConstant.FLOW_GRADE_QPS);
        accountDelete.setLimitApp("default");
        rules.add(accountDelete);

        FlowRule accountInsert = new FlowRule("account_insert");
        // set limit qps to 1
        accountInsert.setCount(1);
        accountInsert.setGrade(RuleConstant.FLOW_GRADE_QPS);
        accountInsert.setLimitApp("default");
        rules.add(accountInsert);

        CuratorFramework zkClient = CuratorFrameworkFactory.newClient(zookeeperAddress, new ExponentialBackoffRetry
                (10, 3));
        zkClient.start();
        Stat stat = zkClient.checkExists().forPath(path);
        if (stat == null) {
            zkClient.create().creatingParentContainersIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path, null);
        }
        zkClient.setData().forPath(path, JSON.toJSONString(rules).getBytes());

        zkClient.close();
    }
}
