# install dependencies

## docker

```shell
yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
sed -i 's+https://download.docker.com+https://mirrors.tuna.tsinghua.edu.cn/docker-ce+' /etc/yum.repos.d/docker-ce.repo
yum install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
```

# startup

```shell
sbin/startup.sh
```

# shutdown

```shell
sbin/shutdown.sh
```

# usage

for real app access:

```shell
http://172.12.0.100:5000/swagger-ui.html
http://172.12.0.101:5000/swagger-ui.html
http://172.12.0.102:5000/swagger-ui.html
```

for nginx access:

```shell
http://172.12.0.201:5000/swagger-ui.html
http://172.12.0.202:5000/swagger-ui.html
```

for vip access:

```shell
http://172.12.0.200:5000/swagger-ui.html
```

# possible problems


1. docker容器中需要访问外网安装keepalive。如出现容器无法访问外网，宿主机可以访问外网，则是因为缺少了NAT规则，可/etc/docker/daemon.json文件中添加 "ip-masq": true,
"iptables": true

```shell
╰─# iptables -t nat -nvL POSTROUTING
Chain POSTROUTING (policy ACCEPT 1864K packets, 151M bytes)
 pkts bytes target     prot opt in     out     source               destination         
   18  7491 MASQUERADE  all  --  *      !br-ef638379b997  172.1.0.0/24         0.0.0.0/0           
   16  7372 MASQUERADE  all  --  *      !br-3cc1c425c506  172.12.0.0/16        0.0.0.0/0           
   16  7372 MASQUERADE  all  --  *      !br-2b1eade74a4e  172.11.0.0/16        0.0.0.0/0           
   11  7072 MASQUERADE  all  --  *      !br-da290bba1d0f  172.10.0.0/16        0.0.0.0/0           
    0     0 MASQUERADE  all  --  *      !docker0  172.17.0.0/16        0.0.0.0/0  
```
