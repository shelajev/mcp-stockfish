package org.shelajev.mcpstockfish;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import io.quarkiverse.mcp.server.TextContent;
import io.quarkiverse.mcp.server.ToolResponse;
import io.quarkus.test.junit.QuarkusTest;

/**
 * The tools reject a bad FEN before starting an engine, so these cases never spawn Stockfish or
 * Maia3 and run without either binary installed.
 */
@QuarkusTest
class FenGuardTest {

    private static final String INJECTION_ATTEMPT = "8/8/8/8/8/8/8/8 w - - 0 1\"; send \"echo pwned\\n\"; #";

    @Inject
    Stockfish stockfish;

    @Inject
    Maia maia;

    @Inject
    Lichess lichess;

    @Test
    void stockfishRejectsInjectionAttempt() {
        assertRejected(stockfish.findBestMove(INJECTION_ATTEMPT));
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1500, 5000, 99999 })
    void maiaRejectsInjectionAttempt(int rating) {
        assertRejected(maia.whatMoveWouldHumanPlay(INJECTION_ATTEMPT, rating));
    }

    @Test
    void boardFromFenRejectsInjectionAttempt() {
        assertRejected(lichess.boardFromFen(INJECTION_ATTEMPT));
    }

    @Test
    void boardFromFenStillRendersAValidPosition() {
        ToolResponse response = lichess.boardFromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");

        assertFalse(response.isError(), text(response));
        assertTrue(text(response).contains("r"), text(response));
    }

    private static void assertRejected(ToolResponse response) {
        assertTrue(response.isError(), "expected an error response, got: " + text(response));
        assertTrue(text(response).startsWith("Invalid FEN"), text(response));
    }

    private static String text(ToolResponse response) {
        return ((TextContent) response.firstContent()).text();
    }
}
