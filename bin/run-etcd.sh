#!/usr/bin/env sh

host=192.168.99.100

docker run \
  --detach \
  --name etcd \
  --publish 2379:2379 \
  quay.io/coreos/etcd:v2.3.1 \
  --advertise-client-urls http://${host}:2379 \
  --listen-client-urls http://0.0.0.0:2379
