#!/bin/sh

set -ex

./mvnw verify -Dquarkus.container-image.build=true -DskipTests=true
docker tag shelajev/mcp-chess:0.0.1 olegselajev241/mcp-chess:latest
docker push olegselajev241/mcp-chess:latest