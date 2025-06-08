# 本地安装zookeeper

访问[release](https://zookeeper.apache.org/releases.html)下载最新安装包
```shell
tar -zxvf apache-zookeeper-*
cd apache-zookeeper-3.9.3-bin/conf/
cp zoo_sample.cfg zoo.cfg
bash ../bin/zkServer.sh start
```

```shell
# vim zoo.cfg
# the port at which the clients will connect
clientPort=2181
# serverPort默认端口8080，修改端口为2182
admin.serverPort=2182
```


# 启动zookeeper

```shell
docker network create --subnet=172.11.0.0/16 net_zookeeper
docker pull zookeeper
docker run -d --net net_zookeeper --ip 172.11.0.100 -p 2181:2181 --name shinerio-zookeeper zookeeper:latest
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

# 基于zookeeper的分布式锁

## 依赖引入

```xml
<dependency>
    <groupId>org.apache.curator</groupId>
    <artifactId>curator-recipes</artifactId>
    <version>${curator.version}</version>
</dependency>
```

## 分布式锁的选择，redis or zookeeper

（1）优点：ZooKeeper分布式锁（如InterProcessMutex），能有效的解决分布式问题，不可重入问题，使用起来也较为简单。

（2）缺点：ZooKeeper实现的分布式锁，性能并不太高。
因为每次在创建锁和释放锁的过程中，都要动态创建、销毁瞬时节点来实现锁功能。大家知道，ZK中创建和删除节点只能通过Leader服务器来执行，然后Leader服务器还需要将数据同不到所有的Follower机器上，这样频繁的网络通信，性能的短板是非常突出的。

总之，在高性能，高并发的场景下，不建议使用ZooKeeper的分布式锁。而由于ZooKeeper的高可用特性，所以在并发量不是太高的场景，推荐使用ZooKeeper的分布式锁。

在目前分布式锁实现方案中，比较成熟、主流的方案有两种：

（1）基于Redis的分布式锁

（2）基于ZooKeeper的分布式锁

两种锁，分别适用的场景为：

（1）基于ZooKeeper的分布式锁，适用于高可靠（高可用）而并发量不是太大的场景；

（2）基于Redis的分布式锁，适用于并发量很大、性能要求很高的、而可靠性问题可以通过其他方案去弥补的场景，比如通过数据库乐观锁进行兜底。