#!/usr/bin/env sh

n=0
if [ -n "$1" ]; then
    n="$1"
fi
tag=latest
if [ -n "$2" ]; then
    tag="$2"
fi
host=192.168.99.100
remote_port=255${n}

docker run \
  --detach \
  --name gabbler-user-${n} \
  --publish ${remote_port}:2552 \
  --publish 800${n}:8000 \
  hseeberger/gabbler-user:${tag} \
  -Dakka.remote.netty.tcp.hostname=${host} \
  -Dakka.remote.netty.tcp.port=${remote_port} \
  -Dcassandra-journal.contact-points.0=${host}:9042 \
  -Dconstructr.coordination.host=${host}
