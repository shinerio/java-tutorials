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

启动

```shell
java -Dserver.port=8081 -Dcsp.sentinel.dashboard.server=localhost:8081 -Dproject.name=sentinel-dashboard -jar sentinel-dashboard*.jar
```

即可访问```http://localhost:8081/#/dashboard/flow/sentinel-dashboard```，默认用户名和密码均为sentinel

> Sentinel 会在客户端首次调用时候进行初始化，开始向控制台发送心跳包。因此需要确保客户端有访问量，才能在控制台上看到监控数据。

## 日志

- 控制台推送规则的日志默认位于控制台机器的 ${user.home}/logs/csp/sentinel-dashboard.log
- 接入端接收规则日志默认位于接入端机器的 ${user.home}/logs/csp/sentinel-record.log.xxx
- 接入端 transport server 日志默认位于接入端机器的 ${user.home}/logs/csp/command-center.log.xxx


## 配置流控

1. 代码埋点，并定义流控异常处
```java
  @SentinelResource(value = "account_find_all", blockHandlerClass = SentinelBlockHandler.class, blockHandler = "blockHandler")
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

也可以通过运行`SentinelZookeeperConfigTests`来推送规则配置到zookeeper中。

## 容器化部署

```shell
PROJECT_BASE=$(pwd)
docker network create --subnet=172.1.0.0/24 net_dashboard
docker build -f $PROJECT_BASE/sentinel/Dockerfile . -t sentinel_dashboard:v1.8.3
docker run -d --net net_dashboard --ip 172.1.0.2 --name sentinel_dashboard sentinel_dashboard:v1.8.3 java -jar -Dserver.port=80 -Dproject.name=sentinel-dashboard -Dcsp.sentinel.dashboard.server=172.1.0.2:80 /sentinel-dashboard.jar
```

> dashboard所在网络不能和app在同一个二层网络，否则无法发现机器

# 参考链接

1. [sentinel](https://github.com/alibaba/Sentinel/wiki)
2. [spring-cloud-starter-alibaba-sentinel](https://github.com/alibaba/spring-cloud-alibaba/wiki/Sentinel)
3. https://www.jianshu.com/p/1ff4b0d526ff