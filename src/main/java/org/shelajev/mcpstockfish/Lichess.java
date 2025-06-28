package org.shelajev.mcpstockfish;

import chariot.Client;
import chariot.model.Game;
import chariot.model.Many;
import chariot.util.Board;
import jakarta.inject.Singleton;

import io.quarkiverse.mcp.server.TextContent;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import io.quarkiverse.mcp.server.ToolResponse;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Singleton
public class Lichess {

        private final Client client;

        public Lichess(@ConfigProperty(name = "lichess.api.token") Optional<String> apiToken) {
            if (apiToken.isPresent() && !apiToken.get().isEmpty()) {
                this.client = Client.basic().withToken(apiToken.get());
            } else {
                this.client = Client.basic();
            }
        }

        @Tool(description = "fetch the last games from lichess.org by a given username")
        public ToolResponse lastGames(
                @ToolArg(description = "the username to fetch the games") String username,
                @ToolArg(description = "how many games to fetch") int n) {

            Many<Game> games = client.games().byUserId(username.trim().toLowerCase(), searchFilter -> {
                searchFilter
                        .max(n)
                        .rated()
                        .finished()
                        .lastFen(true);
            });
            var returnMe = games.stream().collect(toList());
            return ToolResponse.success(
                    new TextContent(returnMe.toString()));
        }

        @Tool(description = "fetch a random game from lichess.org by a given username")
        public ToolResponse randomGame(
                @ToolArg(description = "the username to fetch the games") String username,
                @ToolArg(description = "how many days back to look") int days) {
            Many<Game> games = client.games().byUserId(username.trim().toLowerCase(), searchFilter -> {
                searchFilter
                        .max(100)
                        .since(Instant.now().minus(Duration.ofDays(days)).toEpochMilli())
                        .rated()
                        .lastFen(true)
                        .finished();
            });
            var returnMe = games.stream().collect(toList());
            Collections.shuffle(returnMe);
            if(returnMe.isEmpty()) {
                return ToolResponse.success(
                        new TextContent("No games found"));
            }
            return ToolResponse.success(
                    new TextContent(returnMe.getFirst().toString()));
        }

        @Tool (description = "returns a string which is a visualization of a chess board representation of the chess position given as FEN")
        public ToolResponse boardFromFen(@ToolArg(description = "FEN of the position to display") String fen) {
            Board board = Board.fromFEN(fen);
            return ToolResponse.success(
                    new TextContent(board.toString()));
        }
}
