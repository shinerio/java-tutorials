#!/bin/bash

if [ "$1" == "-c" ];then
  mvn clean package -Dmaven.test.skip=true
fi

iptables -F DOCKER-USER
iptables -A DOCKER-USER -j RETURN

docker compose up &

while [[ ]]; do
    echo "wait for docker start..."
    sleep 3
done

app_br="br-$(docker network ls|grep 'net_app'|awk '{print $1}')"
mongo_br="br-$(docker network ls|grep 'net_mongo'|awk '{print $1}')"
zookeeper_br="br-$(docker network ls|grep 'net_zookeeper'|awk '{print $1}')"
dashboard_br="br-$(docker network ls|grep 'net_dashboard'|awk '{print $1}')"

iptables -I DOCKER-USER -i $app_br -o $mongo_br -p tcp --dport 27017 -j ACCEPT
iptables -I DOCKER-USER -o $app_br -i $mongo_br -p tcp --sport 27017 -j ACCEPT
iptables -I DOCKER-USER -i $app_br -o $zookeeper_br -p tcp --dport 2181 -j ACCEPT
iptables -I DOCKER-USER -o $app_br -i $zookeeper_br -p tcp --sport 2181 -j ACCEPT
iptables -I DOCKER-USER -i $app_br -o $dashboard_br -p tcp --dport 80 -j ACCEPT
iptables -I DOCKER-USER -o $app_br -i $dashboard_br -p tcp --sport 80 -j ACCEPT
iptables -I DOCKER-USER -i $app_br -o $dashboard_br -p tcp --sport 8719 -j ACCEPT
iptables -I DOCKER-USER -o $app_br -i $dashboard_br -p tcp --dport 8719 -j ACCEPT