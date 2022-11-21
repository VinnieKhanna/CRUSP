#!/usr/bin/env bash
docker run -itd --rm \
    -p 8000:8000 \
    -p 56700-56800:56700-56800/udp \
    --env UDP_PORT_LOWER_BOUND=56700 \
    --env UDP_PORT_UPPER_BOUND=56800 \
    --name measurement-service measurement-service