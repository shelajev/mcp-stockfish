package org.shelajev.mcpstockfish;

import io.quarkiverse.mcp.server.TextContent;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import io.quarkiverse.mcp.server.ToolResponse;
import jakarta.inject.Singleton;

import static dev.jbang.jash.Jash.$;

@Singleton
public class Stockfish {

    private static final int MOVETIME_MILLISECONDS = 1200;

    // Kept as a literal so the decimal separator never depends on the default locale.
    private static final String WAIT_SECONDS = "1.5";

    @Tool(description = "Use Stockfish to analyze one chess position and return the best move.")
    ToolResponse findBestMove(@ToolArg(description = "FEN of the position to analyse") String fen) {
        String position = Fen.normalize(fen);
        if (!Fen.isValid(position)) {
            return ToolResponse.error(Fen.INVALID_MESSAGE);
        }
        try {
            return ToolResponse.success(
                    new TextContent(runStockfish(position)));
        } catch (RuntimeException e) {
            return ToolResponse.error("Error running Stockfish: " + e.getMessage());
        }
    }

    private static String runStockfish(String fen) {
        String command = """
                expect -c "spawn stockfish; send \\"uci\\n\\"; expect \\"uciok\\" ; send \\"setoption name MultiPV value 1\\n\\"; send \\"position fen %s\\n\\"; send \\"go movetime %d\\n\\"; sleep %s; send \\"quit\\n\\"; interact"
                """.formatted(fen, MOVETIME_MILLISECONDS, WAIT_SECONDS);

        return $(command).get();
    }
}
