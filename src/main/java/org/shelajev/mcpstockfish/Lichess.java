package org.shelajev.mcpstockfish;

import chariot.Client;
import chariot.chess.Board;
import chariot.model.Game;
import chariot.model.Many;
import jakarta.inject.Singleton;

import io.quarkiverse.mcp.server.TextContent;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import io.quarkiverse.mcp.server.ToolResponse;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Singleton
public class Lichess {

        private static final int MAX_GAMES = 100;

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

            int max = Math.clamp(n, 1, MAX_GAMES);
            try {
                Many<Game> games = client.games().byUserId(username.trim().toLowerCase(), searchFilter -> searchFilter
                        .max(max)
                        .rated()
                        .finished()
                        .lastFen(true));
                var returnMe = games.stream().toList();
                return ToolResponse.success(
                        new TextContent(returnMe.toString()));
            } catch (RuntimeException e) {
                return ToolResponse.error("Error fetching games from lichess.org: " + e.getMessage());
            }
        }

        @Tool(description = "fetch a random game from lichess.org by a given username")
        public ToolResponse randomGame(
                @ToolArg(description = "the username to fetch the games") String username,
                @ToolArg(description = "how many days back to look") int days) {
            try {
                Many<Game> games = client.games().byUserId(username.trim().toLowerCase(), searchFilter -> searchFilter
                        .max(MAX_GAMES)
                        .since(Instant.now().minus(Duration.ofDays(days)).toEpochMilli())
                        .rated()
                        .lastFen(true)
                        .finished());
                // collect(toList()) rather than toList(): the result is shuffled in place below.
                var returnMe = games.stream().collect(toList());
                Collections.shuffle(returnMe);
                if (returnMe.isEmpty()) {
                    return ToolResponse.success(
                            new TextContent("No games found"));
                }
                return ToolResponse.success(
                        new TextContent(returnMe.getFirst().toString()));
            } catch (RuntimeException e) {
                return ToolResponse.error("Error fetching games from lichess.org: " + e.getMessage());
            }
        }

        @Tool (description = "returns a string which is a visualization of a chess board representation of the chess position given as FEN")
        public ToolResponse boardFromFen(@ToolArg(description = "FEN of the position to display") String fen) {
            String position = Fen.normalize(fen);
            if (!Fen.isValid(position)) {
                return ToolResponse.error(Fen.INVALID_MESSAGE);
            }
            try {
                Board board = Board.fromFEN(position);
                return ToolResponse.success(
                        new TextContent(board.toString()));
            } catch (RuntimeException e) {
                return ToolResponse.error("Could not render the position: " + e.getMessage());
            }
        }
}
