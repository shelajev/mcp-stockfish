package org.shelajev.mcpstockfish;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import jakarta.inject.Inject;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.quarkiverse.mcp.server.TextContent;
import io.quarkiverse.mcp.server.ToolResponse;
import io.quarkus.test.junit.QuarkusTest;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Exercises the real engine. Skipped when stockfish and expect are not on the PATH, so a plain
 * checkout still builds; the container image installs both, which is where this matters.
 */
@QuarkusTest
class StockfishEngineTest {

    @Inject
    Stockfish stockfish;

    @BeforeAll
    static void requireEngine() {
        assumeTrue(onPath("stockfish") && onPath("expect"), "stockfish and expect are not installed");
    }

    @Test
    void returnsABestMoveForTheStartingPosition() {
        ToolResponse response = stockfish.findBestMove("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");

        assertFalse(response.isError(), text(response));
        assertTrue(text(response).contains("bestmove"), text(response));
    }

    /**
     * The FEN is interpolated into a string that a shell and then expect both parse, so a payload
     * that escapes the quoting used to run as an arbitrary command. Fen rejects it before the
     * command is built; this asserts the payload leaves no trace on the filesystem.
     */
    @Test
    void rejectsAnInjectionPayloadWithoutRunningIt(@TempDir Path tempDir) {
        Path marker = tempDir.resolve("injection-marker");
        ToolResponse response = stockfish.findBestMove(
                "8/8/8/8/8/8/8/8 w - - 0 1\"; exec touch " + marker + "; send \"quit\\n\"; #");

        assertTrue(response.isError(), text(response));
        assertFalse(Files.exists(marker), "the injection payload executed and created " + marker);
    }

    private static boolean onPath(String executable) {
        try {
            return new ProcessBuilder("sh", "-c", "command -v " + executable)
                    .redirectErrorStream(true)
                    .start()
                    .waitFor() == 0;
        } catch (IOException e) {
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private static String text(ToolResponse response) {
        return ((TextContent) response.firstContent()).text();
    }
}
