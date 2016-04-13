#!/usr/bin/env sh

n=0
if [ -n "$1" ]; then
    n="$1"
fi
tag=latest
if [ -n "$2" ]; then
    tag="$2"
fi

docker run \
  --detach \
  --name gabbler-user-${n} \
  --publish 255${n}:2552 \
  --publish 800${n}:8000 \
  hseeberger/gabbler-user:${tag}
