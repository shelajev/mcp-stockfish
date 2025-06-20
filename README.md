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

or you can use the pre-built version:
```shell
docker run -p8080:8080 olegselajev241/mcp-chess:latest
```

This will start the MCP server and expose it on port 8080.

## Connecting to the Server

You can connect to the MCP server via HTTP at:

```
http://localhost:8080/mcp
```

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
