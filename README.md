# MCP Server for Chess

This project provides a Model Context Protocol (MCP) server for chess tools over HTTP. It exposes Stockfish analysis, Maia human-like move prediction, Lichess game lookup, and FEN board rendering.

The server uses the Quarkiverse MCP HTTP transport. The Streamable HTTP endpoint is:

```text
http://localhost:8080/mcp
```

## Prerequisites

- Java 25 or later (required for building the application)
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
3. Tag the image as `shelajev/mcp-chess:0.0.1`

## Running the Container

Once the image is built, you can run it with:

```shell
docker run -p8080:8080 shelajev/mcp-chess:0.0.1
```

or you can use the pre-built version:

```shell
docker run -p8080:8080 olegselajev241/mcp-chess:latest
```

This will start the MCP server and expose it on port 8080.

## Connecting to the Server

Connect an MCP client that supports Streamable HTTP to:

```
http://localhost:8080/mcp
```

This project no longer includes the stdio transport. It also does not require the old SSE transport endpoint for normal MCP access.

## Deploying to Cloud Run

This can be deployed to Google Cloud Run meaningfully as an HTTP MCP server. The container binds to `0.0.0.0` and uses the `PORT` environment variable with a local default of `8080`, which matches Cloud Run's container contract.

Build and push an image, then deploy it:

```shell
gcloud builds submit --tag REGION-docker.pkg.dev/PROJECT/REPOSITORY/mcp-chess:0.0.1

gcloud run deploy mcp-chess \
  --image REGION-docker.pkg.dev/PROJECT/REPOSITORY/mcp-chess:0.0.1 \
  --region REGION \
  --memory 2Gi \
  --cpu 2 \
  --concurrency 2 \
  --timeout 60s \
  --set-env-vars LICHESS_API_TOKEN=optional-token
```

Recommended Cloud Run settings:

- Keep concurrency low. Stockfish and Maia analysis are CPU-bound and each request starts an engine process.
- Use at least 2 CPU and 2 GiB memory for Maia plus Stockfish. Raise memory if you see OOMs during Maia calls.
- Treat the service as stateless. The container has all engines and Maia weights baked into the image.
- Consider authentication before exposing it publicly; the tools can consume external Lichess quota and CPU.

## Available Tools

The MCP server provides several tools for chess analysis and interaction with chess platforms:

### Stockfish Tools

1. **findBestMove**
   - Description: Analyzes a chess position using the Stockfish engine to find the best move.
   - Parameters:
     - `fen`: FEN notation of the chess position to analyze.

2. **analyzeGame**
   - Description: Analyzes a sequence of chess moves and returns evaluations for each position.
   - Parameters:
     - `moves`: List of chess moves in SAN (Standard Algebraic Notation).

### Lichess Tools

1. **lastGames**
   - Description: Fetches the last games from lichess.org by a given username.
   - Parameters:
     - `username`: The username to fetch the games for.
     - `n`: How many games to fetch.

2. **randomGame**
   - Description: Fetches a random game from lichess.org by a given username.
   - Parameters:
     - `username`: The username to fetch the games for.
     - `days`: How many days back to look for games.

3. **boardFromFen**
   - Description: Returns a text visualization of a chess board from a position given in FEN notation.
   - Parameters:
     - `fen`: FEN notation of the chess position to display.

### Maia Tools

1. **whatMoveWouldHumanPlay**
   - Description: Uses the Maia chess engine to predict what move a human player would make in a given position.
   - Parameters:
     - `fen`: FEN notation of the chess position to analyze.
     - `rating`: Rating of the Maia engine to use (from 1100 to 1900).
