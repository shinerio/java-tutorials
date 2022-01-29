# 启动zookeeper

```shell
docker pull zookeeper
docker run -d -p 12181:2181 --name shinerio-zookeeper zookeeper:latest
```

# zkCli

进入CLI
```shell
./zkCli.sh
# ls路径目录
[zk: localhost:2181(CONNECTED) 1] ls /sentinel_rule_config
[java-tutorial]
# 获取节点数据
[zk: localhost:2181(CONNECTED) 1] ls /sentinel_rule_config
[java-tutorial]
```
