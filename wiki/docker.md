docker使用了linux的Namespace技术来进行资源隔离，如PID Namespace隔离进程，Mount Namespace隔离文件系统，Network Namespace隔离网络等。一个Network Namespace提供了一份独立的网络环境，包括网卡、路由、iptable规则等都与其他的Network Namespace隔离。一个Docker容器一般会分配一个独立的Network Namespace。

# 网络

## 默认网络

当安装Docker时，它会自动创建三个网络。可以使用`docker network ls`命令列出这些网络。Docker内置这三个网络，运行容器时，可以使用该--network标志来指定容器应连接到哪些网络。

```shell
NETWORK ID     NAME      DRIVER    SCOPE
6d0831c824e8   bridge    bridge    local
ec05ee8f8f98   host      host      local
201c9d708dab   none      null      local
```

## 常见的docker网络主要有以下四种类型

| 网络模式    | 简介  |
|---------|-----|
| Host    |	容器将不会虚拟出自己的网卡，配置自己的IP等，而是使用宿主机的IP和端口。 |
| Bridge  |	此模式会为每一个容器分配、设置IP等，并将容器连接到一个docker0虚拟网桥，通过docker0网桥以及Iptables nat表配置与宿主机通信。|
| None |	该模式关闭了容器的网络功能。|
| Container | 创建的容器不会创建自己的网卡，配置自己的IP，而是和一个指定的容器共享IP、端口范围。|

### Host模式

Host模式相当于vmware中的桥接模式，与宿主机在同一网络中，没有独立的ip地址。如果启动容器的时候使用host模式，那么这个容器将不会获得一个独立的Network Namespace，而是和宿主机共用一个Network Namespace。容器将不会虚拟出自己的网卡，配置自己的IP等，而是使用宿主机的IP和端口。此时容器和宿主机在同一个网络中，没有独立的ip地址，但是，容器的其他方面，如文件系统、进程列表等还是和宿主机隔离的。此模式下容器可以直接监听宿主机端口和请求宿主机上的网络端口。

```shell
docker run -d --network=host --name container_in_host ubuntu
docker inspect container_in_host
```

容器网络配置如下：

```json
{
  "NetworkSettings": {
    "Bridge": "",
    "SandboxID": "567fe9d56d055b991e090d03507954a4a1b17086cd3a132180df3a983be15bb6",
    "HairpinMode": false,
    "LinkLocalIPv6Address": "",
    "LinkLocalIPv6PrefixLen": 0,
    "Ports": {},
    "SandboxKey": "/var/run/docker/netns/default",
    "SecondaryIPAddresses": null,
    "SecondaryIPv6Addresses": null,
    "EndpointID": "",
    "Gateway": "",
    "GlobalIPv6Address": "",
    "GlobalIPv6PrefixLen": 0,
    "IPAddress": "",
    "IPPrefixLen": 0,
    "IPv6Gateway": "",
    "MacAddress": "",
    "Networks": {
      "host": {
        "IPAMConfig": null,
        "Links": null,
        "Aliases": null,
        "NetworkID": "2bc28b3303b817afc9ea4e0003e7bb3f01349df36ab896acb502f61f489af228",
        "EndpointID": "414aa75e2c3c727cb4e071b2553d9dad160689e1b573547dc93557f26870f5d5",
        "Gateway": "",
        "IPAddress": "",
        "IPPrefixLen": 0,
        "IPv6Gateway": "",
        "GlobalIPv6Address": "",
        "GlobalIPv6PrefixLen": 0,
        "MacAddress": "",
        "DriverOpts": null
      }
    }
  }
}
```

通过观察宿主机网络和容器网络，我也可以发现他们的网络是完全一致的。

宿主机
![](https://shinerio.oss-cn-beijing.aliyuncs.com/blog_images/20220212155222.png)

容器：
![](https://shinerio.oss-cn-beijing.aliyuncs.com/blog_images/20220212155938.png)

> host模式仅支持linux主机，不适用于mac和windows。[官方解释](https://docs.docker.com/network/host/)

### bridge模式

bridge是创建容器的默认网络模式，相当与vmware中的NAT模式。容器运行在独立的namespace中，并连接到默认的docker0虚拟网桥。通过docker0网桥以及iptables nat表配置与宿主机通信。

```shell
╰─# docker network inspect bridge
[
    {
        "Name": "bridge",
        "Id": "e38619998c5bace6867be1515140b8064a0063a13e77fb412becf61617d12978",
        "Created": "2022-02-12T15:40:52.832496703+08:00",
        "Scope": "local",
        "Driver": "bridge",
        "EnableIPv6": false,
        "IPAM": {
            "Driver": "default",
            "Options": null,
            "Config": [
                {
                    "Subnet": "172.17.0.0/16",
                    "Gateway": "172.17.0.1"
                }
            ]
        },
        "Internal": false,
        "Attachable": false,
        "Ingress": false,
        "ConfigFrom": {
            "Network": ""
        },
        "ConfigOnly": false,
        "Containers": {},
        "Options": {
            "com.docker.network.bridge.default_bridge": "true",
            "com.docker.network.bridge.enable_icc": "true",
            "com.docker.network.bridge.enable_ip_masquerade": "true",
            "com.docker.network.bridge.host_binding_ipv4": "0.0.0.0",
            "com.docker.network.bridge.name": "docker0",
            "com.docker.network.driver.mtu": "1500"
        },
        "Labels": {}
    }
]

```

当Docker server启动时，会在主机上创建一个名为docker0的虚拟网桥，此主机上启动的Docker容器会连接到这个虚拟网桥上。虚拟网桥的工作方式和物理交换机类似，这样主机上的所有容器就通过交换机连在了一个二层网络中。接下来就要为容器分配IP了，Docker会从RFC1918所定义的私有IP网段中，选择一个和宿主机不同的IP地址和子网分配给docker0，连接到docker0的容器就从这个子网中选择一个未占用的IP使用。如一般Docker会使用172.17.0.0/16这个网段，并将172.17.0.1/16分配给docker0网桥（在主机上使用ifconfig命令是可以看到docker0的，可以认为它是网桥的管理接口，在宿主机上作为一块虚拟网卡使用）

docker完成网络配置的过程大致如下：

1. 在主机上创建一对虚拟网卡veth pair设备。veth设备总是成对出现的，它们组成了一个数据的通道，数据从一个设备进入，就会从另一个设备出来。因此，veth设备常用来连接两个网络设备。

2. Docker将veth pair设备的一端放在新创建的容器中，并命名为eth0。另一端放在主机中，以veth65f9这样类似的名字命名，并将这个网络设备加入到docker0网桥中，可以通过brctl show命令查看。

```shell
╰─# docker run -d --network=bridge --name container_in_bridge2 ubuntu sleep 30
fcfcf2528207d439b7c949da45e25dd6da404b7c55cce895b2592e8721f886e3
╰─# docker inspect fcfcf2
"NetworkSettings": {
            "Bridge": "",
            "SandboxID": "752893cc0701c66bc64a7c15b6ef9e5374f8d8ae2d2d25eeebd4ae82c02be48c",
            "HairpinMode": false,
            "LinkLocalIPv6Address": "",
            "LinkLocalIPv6PrefixLen": 0,
            "Ports": {},
            "SandboxKey": "/var/run/docker/netns/752893cc0701",
            "SecondaryIPAddresses": null,
            "SecondaryIPv6Addresses": null,
            "EndpointID": "9bb6f0a0f2ff2d4a24239c0f1da0d214bb710701a83a68236ed93a3f25061c84",
            "Gateway": "172.17.0.1",
            "GlobalIPv6Address": "",
            "GlobalIPv6PrefixLen": 0,
            "IPAddress": "172.17.0.2",
            "IPPrefixLen": 16,
            "IPv6Gateway": "",
            "MacAddress": "02:42:ac:11:00:02",
            "Networks": {
                "bridge": {
                    "IPAMConfig": null,
                    "Links": null,
                    "Aliases": null,
                    "NetworkID": "e38619998c5bace6867be1515140b8064a0063a13e77fb412becf61617d12978",
                    "EndpointID": "9bb6f0a0f2ff2d4a24239c0f1da0d214bb710701a83a68236ed93a3f25061c84",
                    "Gateway": "172.17.0.1",
                    "IPAddress": "172.17.0.2",
                    "IPPrefixLen": 16,
                    "IPv6Gateway": "",
                    "GlobalIPv6Address": "",
                    "GlobalIPv6PrefixLen": 0,
                    "MacAddress": "02:42:ac:11:00:02",
                    "DriverOpts": null
                }
            }
        }
```

bridge网络的简单模型如下：

![](https://shinerio.oss-cn-beijing.aliyuncs.com/blog_images/20220212164546.png)

使用bridge模式下，同一网桥下面的容器是二层互通的，而使用`docker run -p`时，docker实际是通过iptables做了dnat规则，实现了端口转发功能，使得容器外可以访问容器内。在主机开通了ip_forward的情况，宿主机会对容器访问外部网络的流量做源地址替换。

```shell
╰─# route -n                                                                  
内核 IP 路由表
目标            网关            子网掩码        标志  跃点   引用  使用 接口
# 默认网关，出公网方向流量，交由宿主机网口处理
0.0.0.0         192.168.84.1    0.0.0.0         UG    600    0        0 wlp7s0
169.254.0.0     0.0.0.0         255.255.0.0     U     1000   0        0 wlp7s0
# 访问172.17.0.0/16的容器网络，进入docker0口
172.17.0.0      0.0.0.0         255.255.0.0     U     0      0        0 docker0
192.168.84.0    0.0.0.0         255.255.255.0   U     600    0        0 wlp7s0
```

```shell
╰─# iptables-save
# Generated by iptables-save v1.8.4 on Sat Feb 12 17:23:11 2022
*filter
:INPUT ACCEPT [6722:24118414]
:FORWARD DROP [0:0]
:OUTPUT ACCEPT [6464:12481979]
:DOCKER - [0:0]
:DOCKER-ISOLATION-STAGE-1 - [0:0]
:DOCKER-ISOLATION-STAGE-2 - [0:0]
:DOCKER-USER - [0:0]
-A FORWARD -j DOCKER-USER
-A FORWARD -j DOCKER-ISOLATION-STAGE-1
-A FORWARD -o docker0 -m conntrack --ctstate RELATED,ESTABLISHED -j ACCEPT
-A FORWARD -o docker0 -j DOCKER
-A FORWARD -i docker0 ! -o docker0 -j ACCEPT
-A FORWARD -i docker0 -o docker0 -j ACCEPT
-A DOCKER -d 172.17.0.2/32 ! -i docker0 -o docker0 -p tcp -m tcp --dport 80 -j ACCEPT
-A DOCKER-ISOLATION-STAGE-1 -i docker0 ! -o docker0 -j DOCKER-ISOLATION-STAGE-2
-A DOCKER-ISOLATION-STAGE-1 -j RETURN
-A DOCKER-ISOLATION-STAGE-2 -o docker0 -j DROP
-A DOCKER-ISOLATION-STAGE-2 -j RETURN
-A DOCKER-USER -j RETURN
COMMIT
# Completed on Sat Feb 12 17:23:11 2022
# Generated by iptables-save v1.8.4 on Sat Feb 12 17:23:11 2022
*nat
:PREROUTING ACCEPT [36:8706]
:INPUT ACCEPT [35:8674]
:OUTPUT ACCEPT [239:15661]
:POSTROUTING ACCEPT [239:15661]
:DOCKER - [0:0]
-A PREROUTING -m addrtype --dst-type LOCAL -j DOCKER
-A OUTPUT ! -d 127.0.0.0/8 -m addrtype --dst-type LOCAL -j DOCKER
# 目的口非docker0，源地址172.17.0.0/16，即容器内访问容器外网络，做SNAT，源地址替换为流量走出网口的地址，具体哪个口由路由决定
-A POSTROUTING -s 172.17.0.0/16 ! -o docker0 -j MASQUERADE
-A POSTROUTING -s 172.17.0.2/32 -d 172.17.0.2/32 -p tcp -m tcp --dport 80 -j MASQUERADE
-A DOCKER -i docker0 -j RETURN
# DNAT映射，容器外部访问宿主机80端口，in端口非docker0，目的地址替换为172.17.0.2，端口映射为容器内80端口
-A DOCKER ! -i docker0 -p tcp -m tcp --dport 80 -j DNAT --to-destination 172.17.0.2:80
COMMIT
# Completed on Sat Feb 12 17:23:11 2022
```

使用以下命令确认主机ip_forward功能已经打开。

```shell
╰─# sysctl net.ipv4.ip_forward
net.ipv4.ip_forward = 1
```

### container模式

container模式和host模式类似，这个模式创建的容器和已经存在的一个容器共享一个Network Namespace，而不是和宿主机共享。新创建的容器不会创建自己的网卡，配置自己的IP，而是和一个指定的容器共享IP、端口范围等。同样，两个容器除了网络方面，其他的如文件系统、进程列表等还是隔离的。两个容器的进程可以通过lo网卡设备通信。

```shell
docker run -d --network=container:container1 --name container2 -p 80:81 ubuntu sleep 300
docker run -d --network=container:container1 --name container2 ubuntu sleep 300
```

### None模式

使用None模式，Docker容器拥有自己的`Network Namespace`，但是，并不为Docker容器进行任何网络配置。也就是说，这个Docker容器没有网卡、IP、路由等信息。需要我们自己为 Docker 容器添加网卡、配置 IP 等。

## 自定义网桥

使用如下命令即可新建一个网络。

```shell
╭─root@shinerio /home/shinerio 
╰─# docker network create --subnet=172.10.0.0/16 net_app
7d57ffc40cbcec94d96a81833b2fb786b0646f6e1c897095d3480bdec1969f09
╭─root@shinerio /home/shinerio 
╰─# docker network ls
NETWORK ID     NAME      DRIVER    SCOPE
e38619998c5b   bridge    bridge    local
2bc28b3303b8   host      host      local
7d57ffc40cbc   net_app   bridge    local
5a027306fef6   none      null      local
```

不同网桥之间的流量默认是相互隔离的。

```
Chain DOCKER-ISOLATION-STAGE-1 (1 references)
 pkts bytes target     prot opt in     out     source               destination         
    0     0 DOCKER-ISOLATION-STAGE-2  all  --  br-7d57ffc40cbc !br-7d57ffc40cbc  0.0.0.0/0            0.0.0.0/0           
14060  819K DOCKER-ISOLATION-STAGE-2  all  --  docker0 !docker0  0.0.0.0/0            0.0.0.0/0           
30335   44M RETURN     all  --  *      *       0.0.0.0/0            0.0.0.0/0           

Chain DOCKER-ISOLATION-STAGE-2 (2 references)
 pkts bytes target     prot opt in     out     source               destination         
    0     0 DROP       all  --  *      br-7d57ffc40cbc  0.0.0.0/0            0.0.0.0/0           
    0     0 DROP       all  --  *      docker0  0.0.0.0/0            0.0.0.0/0           
14060  819K RETURN     all  --  *      *       0.0.0.0/0            0.0.0.0/0  
```

# 存储

docker容器启动的时候可以通过指定-v参数将宿主机目录挂载到容器上，参数由（:）分隔的三个字段组成，<卷名>:<容器路径>:<选项列表>。选项列表，如：ro（只读），consistent，delegated，cached，z 和 Z

```shell
# 将宿主机的/root/config目录映射到docker容器中的/root/config目录，冒号前是宿主机目录，冒号后面是容器目录
docker run -d -v /root/config:/root/config --name shinerio-java-tutorial1 -p 81:80 -t java-tutorial:v1
```

也可以给容器直接创建volume

```shell
docker volume create --name v1
docker run -d -v v1:/root/config --name shinerio-java-tutorial1 -p 81:80 -t java-tutorial:v1
```

## z和Z选项

如果使用的`selinux`话，可以添加`z`或者`Z`选项来修改正在装入容器的主机文件或目录的`selinux`标签。这会影响主机本身的文件或目录，并可能导致Docker范围之外的后果。

- `z`选项指示绑定安装内容在多个容器之间共享。
- `Z`选项指示绑定安装内容是私有的和非共享的。

> 需要极端谨慎使用这些选项。绑定一个系统目录，例如 /home或者 /usr 用这个 Z 选项，将会使你的主机无法工作，你可能需要手工重新标记主机文件。

# 参考文献

1. https://www.cnblogs.com/sanduzxcvbnm/p/13370773.html
2. https://www.jianshu.com/p/1dd65ab5b997
3. https://www.jianshu.com/p/beeb6094bcc9
4. https://deepzz.com/post/the-docker-volumes-basic.html
5. https://www.cnblogs.com/ittranslator/p/13352727.html