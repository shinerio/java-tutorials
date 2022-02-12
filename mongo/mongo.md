# 启动mongo docker容器

```shell
docker network create --subnet=172.10.0.0/16 net_mongo
docker pull mongo
docker run -d --net net_mongo --ip 172.10.0.100 -p 27017:27017 -v --name shinerio-mongo mongo:latest
```

# 创建账号

## 1. 创建root账号，具有admin角色，只允许本地登录

```shell
> use admin
> db.createUser({user:"root", pwd:"root",roles:[{role:"root","db":"admin"}],authenticationRestrictions:[{clientSource:["127.0.0.1"]}]})
```

## 2. 创建普通管理员账号，可以对所有数据库进行读写数据和索引创建，可远程连接

```shell
> db.createUser({user: 'shinerio', pwd: 'shinerio', roles:["dbAdminAnyDatabase", "readWriteAnyDatabase"]})
```

## 打包脚本到docker容器

上述方式需要容器启动后，手动进入容器创建用户。使用如下方法，可以在docker启动后，自动执行创建用户的脚本。

```shell
PROJECT_BASE=$(pwd)
docker run -d --net net_mongo --ip 172.10.0.100 -p 27017:27017 -v $PROJECT_BASE/mongo/mongo-init.sh:/docker-entrypoint-initdb.d/mongo-init.sh --name shinerio-mongo mongo:latest
```

# 参考连接

1. [mongodb开启远程连接和账号密码登录](https://www.panyanbin.com/article/c602b9e2.html)