# MCP Server for Stockfish

This project provides a Model Control Protocol (MCP) server for Stockfish, a powerful open-source chess engine. It allows you to interact with Stockfish through HTTP requests.

## Prerequisites

- Java 21 or later (required for building the application)
- Docker (required for running the containerized application)

## Building the Application

The project includes a multi-stage Dockerfile that builds Stockfish as part of the container build process.

To build the application and create the Docker image:

```shell
./mvnw verify -Dquarkus.container-image.build=true
```

This command will:
1. Compile the Java application
2. Build the Docker image with Stockfish compiled for the appropriate architecture
3. Tag the image as `shelajev/mcp-stockfish:0.0.1`

## Running the Container

Once the image is built, you can run it with:

```shell
docker run -p8080:8080 shelajev/mcp-stockfish:0.0.1
```

This will start the MCP server and expose it on port 8080.

## Connecting to the Server

You can connect to the MCP server via HTTP at:

```
http://localhost:8080/mcp
```
