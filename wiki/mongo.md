# 启动mongo docker容器

```shell
docker pull mongo
docker run -d -p 27017:27017 --name shinerio-mongo mongo:latest
```

# 创建账号

## 创建root账号，具有admin角色，只允许本地登录

```shell
> use admin
> db.createUser({user:"root", pwd:"root",roles:[{role:"root","db":"admin"}],authenticationRestrictions:[{clientSource:["127.0.0.1"]}]})
```

## 创建普通管理员账号，可以对所有数据库进行读写数据和索引创建，可远程连接

```shell
> db.createUser({user: 'shinerio', pwd: 'shinerio', roles:["dbAdminAnyDatabase", "readWriteAnyDatabase"]})
```

# 参考连接

1. [mongodb开启远程连接和账号密码登录](https://www.panyanbin.com/article/c602b9e2.html)