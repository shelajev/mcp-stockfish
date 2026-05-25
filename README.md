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

The project includes a multi-stage Dockerfile that builds Stockfish and installs Maia3 as part of the container build process.

To build the application and create the Docker image:

```shell
./mvnw verify -Dquarkus.container-image.build=true
```

This command will:
1. Compile the Java application
2. Build the Docker image with Stockfish compiled for the appropriate architecture
3. Install Maia3 and bake the selected Maia3 checkpoint into the image
4. Tag the image as `shelajev/mcp-chess:0.0.1`

The Dockerfile pins Stockfish to `sf_18` and Maia3 to the `maia3-5m` model by default. Override them when needed:

```shell
docker build \
  --build-arg STOCKFISH_REF=sf_18 \
  --build-arg MAIA3_REF=main \
  --build-arg MAIA3_MODEL=maia3-5m \
  --build-arg MAIA3_BAKE_CHECKPOINT=true \
  --build-arg TORCH_INDEX_URL=https://download.pytorch.org/whl/cpu \
  --build-arg TORCH_PYPI_FALLBACK=false \
  -f src/main/docker/Dockerfile.jvm \
  -t shelajev/mcp-chess:0.0.1 .
```

To try the larger Maia3 model, build with `--build-arg MAIA3_MODEL=maia3-79m`. The image uses the PyTorch CPU wheel index by default to avoid pulling CUDA packages into a Cloud Run image. `TORCH_PYPI_FALLBACK=true` can help in restricted build environments, but it may produce a much larger image. `MAIA3_BAKE_CHECKPOINT=false` skips baking the Hugging Face checkpoint; Maia3 will then download its model at runtime unless you provide `MAIA3_CHECKPOINT`. The Java tool reads `MAIA3_MODEL`, `MAIA3_CHECKPOINT`, `MAIA3_DEVICE`, `MAIA3_UCI`, and `MAIA3_TIMEOUT_SECONDS` at runtime, so the model command can be tuned without changing the source.

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
- Use at least 2 CPU and 2 GiB memory for Stockfish plus the default Maia3 5M model. Raise memory for Maia3 79M or if you see OOMs during Maia calls.
- Treat the service as stateless. The container has Stockfish, Maia3, and the selected Maia3 checkpoint baked into the image.
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
   - Description: Uses the Maia3 chess engine to predict what move a human player would make in a given position.
   - Parameters:
     - `fen`: FEN notation of the chess position to analyze.
     - `rating`: Elo rating to condition Maia3 with (from 0 to 5000).
