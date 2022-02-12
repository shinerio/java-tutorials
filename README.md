# dependency

- docker
- docker compose
- jdk 17

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
http://172.12.0.100:5000/swagger-ui/index.html
http://172.12.0.101:5000/swagger-ui/index.html
http://172.12.0.102:5000/swagger-ui/index.html
```

for nginx access:

```shell
http://172.12.0.201:5000/swagger-ui/index.html
http://172.12.0.202:5000/swagger-ui/index.html
```

for vip access:

```shell
http://172.12.0.200:5000/swagger-ui/index.html
```