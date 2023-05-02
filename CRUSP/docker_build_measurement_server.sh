#!/usr/bin/env bash
docker build -f Dockerfile_Server -t measurement-service .
docker tag measurement-service abe/measurement-service