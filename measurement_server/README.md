# Measurement Service

This services uses `Constant Rate Ultra Short Probing` (CRUSP) for measuring the available bandwidth. 

## CRUSP

More information to CRUSP can be found [here](../measurement_shared_rust/README.md).

## Building

To build the docker container, change to the project overview as current working directory.
Then build the container with:

```bash
docker build -f Dockerfile_Server -t measurement_server .
```

## Running

You can run it with `docker-compose` or you can just run this container by:
```bash
docker run -itd --rm \
    -p 8000:8000 \
    -p 56700-56800:56700-56800/udp \
    --env UDP_PORT_LOWER_BOUND=56700 \
    --env UDP_PORT_UPPER_BOUND=56800 \
    --name measurement_server measurement_server
```

## Documentation   

## Overview

Link to Overview: [README-OVERVIEW](../README.md)