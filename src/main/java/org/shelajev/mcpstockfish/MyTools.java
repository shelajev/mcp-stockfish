package org.shelajev.mcpstockfish;

import io.quarkiverse.mcp.server.TextContent;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import io.quarkiverse.mcp.server.ToolResponse;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import static dev.jbang.jash.Jash.$;

@Singleton
public class MyTools {

    @Tool(description = "You have the stockfish binary on the PATH, run a stockfish command to analyze a position or a game.")
    ToolResponse stockfish(@ToolArg(description = "FEN of the position to analyse") String fen) {
        int depth = 15;
        int timeoutSeconds = 3;
        String command = """
                expect -c "spawn stockfish; send \\"uci\\r\\"; send \\"setoption name MultiPV value 2\\r\\"; send \\"position fen %s\\r\\"; send \\"go depth %d\\r\\"; sleep %d; send \\"quit\\r\\"; interact"
                """.formatted(fen, depth, timeoutSeconds);
        return ToolResponse.success(
                new TextContent($(command).get()));
    }
}