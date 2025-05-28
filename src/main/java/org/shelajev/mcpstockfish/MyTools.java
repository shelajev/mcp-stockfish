package org.shelajev.mcpstockfish;

import io.quarkiverse.mcp.server.TextContent;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import io.quarkiverse.mcp.server.ToolResponse;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.Arrays;
import java.util.regex.Pattern;

import static dev.jbang.jash.Jash.$;

@Singleton
public class MyTools {

    @Tool(description = "You have the stockfish chess engine available. This tool can analyze the FEN notation of the chess position and return what stockfish thinks is the best move.")
    ToolResponse findBestmove(@ToolArg(description = "FEN of the position to analyse") String fen) {
        int depth = 15;
        int timeoutSeconds = 3;
        String command = """
                expect -c "spawn stockfish; send \\"uci\\r\\"; send \\"setoption name MultiPV value 1\\r\\"; send \\"position fen %s\\r\\"; send \\"go depth %d\\r\\"; sleep %d; send \\"quit\\r\\"; interact"
                """.formatted(fen, depth, timeoutSeconds);
        String output = $(command).get();
        String bestmove = Arrays.stream(output.split("\n")).filter(s -> s.contains("bestmove")).findFirst().get();
        return ToolResponse.success(
                new TextContent(bestmove));
    }
}