package org.shelajev.mcpstockfish;

import io.quarkiverse.mcp.server.TextContent;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import io.quarkiverse.mcp.server.ToolResponse;
import jakarta.inject.Singleton;

import static dev.jbang.jash.Jash.$;

@Singleton
public class Stockfish {

    @Tool(description = "Use Stockfish to analyze one chess position and return the best move.")
    ToolResponse findBestMove(@ToolArg(description = "FEN of the position to analyse") String fen) {
        String stockfishOutput = runStockfish(fen);
        return ToolResponse.success(
                new TextContent(stockfishOutput));
    }

    private static String runStockfish(String fen) {
        int movetimeInMilliseconds = 1200;
        String command = """
                expect -c "spawn stockfish; send \\"uci\\n\\"; expect \\"uciok\\" ; send \\"setoption name MultiPV value 1\\n\\"; send \\"position fen %s\\n\\"; send \\"go movetime %d\\n\\"; sleep %f; send \\"quit\\n\\"; interact" 
                """.formatted(fen, movetimeInMilliseconds, 1.5);

        String stockfishOutput = $(command).get();
        return stockfishOutput;
    }
}
