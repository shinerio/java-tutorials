# Sentinel限流

## 依赖
```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
    <version>${spring.cloud.sentinel.version}</version>
</dependency>
```

## Sentinel控制台

Sentinel控制台提供一个轻量级的控制台，它提供机器发现、单机资源实时监控、集群资源汇总，以及规则管理的功能。 Sentinel控制台是一个标准的Spring Boot应用，以Spring Boot的方式运行jar包即可。
可以在[Release](https://github.com/alibaba/Sentinel/releases) 页面下载。

java -Dserver.port=8081 -Dcsp.sentinel.dashboard.server=localhost:8081 -Dproject.name=sentinel-dashboard -jar sentinel-dashboard*.jar

## 配置流控

1. 代码埋点
```java
@SentinelResource("account_find_all")
public Flux<Account> findAll() {
    return accountCrudRepository.findAll();
}
```

2. 控制台添加account_find_all规则，设置QPS为1
![](https://shinerio.oss-cn-beijing.aliyuncs.com/blog_images/uncategory20220129221523.png)

3. 当QPS > 1时，抛出限流异常
![](https://shinerio.oss-cn-beijing.aliyuncs.com/blog_images/uncategory20220129221717.png)

## 规则持久化

Sentinel的规则默认是存储在内存中的，在组件重启后就会丢失。Sentinel提供了基于`Nacos/Zookeeper/Apollo/Redis`的方式配置规则。此处以Zookeeper为例。

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-datasource-zookeeper</artifactId>
    <version>x.y.z</version>
</dependency>
```

从zookeeper中捞取持久化数据

```java
ReadableDataSource<String, List<FlowRule>> flowRuleDataSource =
        new ZookeeperDataSource<>(zookeeperAddress, path,
        source -> JSON.parseObject(source, new TypeReference<>() {
        }));
FlowRuleManager.register2Property(flowRuleDataSource.getProperty());
```

默认DashBoard推送的配置只会存在内存中，如果想通过DashBoard来持久化配置，可以对DashBoard的源码进行修改，自定义`FlowRuleZookeeperProvider`和`FlowRuleZookeeperPublisher`

也可以通过运行`SentinelZookeeperConfigTests`来持久化规则配置到zookeeper中。

# 参考链接

1. [sentinel](https://github.com/alibaba/Sentinel/wiki)
2. [spring-cloud-starter-alibaba-sentinel](https://github.com/alibaba/spring-cloud-alibaba/wiki/Sentinel)