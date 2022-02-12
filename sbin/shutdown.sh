#!/bin/bash

docker compose down

iptables -F DOCKER-USER
iptables -A DOCKER-USER -j RETURN

if [ "$1" == "clean" ];then
  if [ -n "$2" ];then
    docker images|grep "$2"|awk '{print $1}'|xargs docker rmi
  else
    docker images|grep "java-tutorial"|awk '{print $1}'|xargs docker rmi
  fi
fi