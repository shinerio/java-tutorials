# 后端部署

为了实现高可用，我们先部署三个后端服务，此处我们通过三个docker容器模拟，对外分别暴露81,82,83端口。

为了使docker容器中的应用可以连接到对应的中间件，需要修改application.yml中middleware-address为主机的真实ip

```shell
docker build -t java-tutorial:v1 -f /path/to/Dockerfile .
docker run -d --name shinerio-java-tutorial1 -p 81:80 -t java-tutorial:v1
docker run -d --name shinerio-java-tutorial2 -p 82:80 -t java-tutorial:v1
docker run -d --name shinerio-java-tutorial3 -p 83:80 -t java-tutorial:v1
```

# 启动lvs docker容器
```shell
docker pull nginx
docker run -d -p 80:80 --name shinerio-nginx nginx:latest
```
