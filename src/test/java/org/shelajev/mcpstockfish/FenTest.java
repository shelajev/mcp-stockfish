package org.shelajev.mcpstockfish;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class FenTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq -",
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0",
            "rnbqkbnr/pp1ppppp/8/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R b Kk c6 1 2",
            "8/8/8/8/8/8/8/8 w - - 0 1",
            "4k3/8/8/8/8/8/8/4K3 b HAha - 12 34",
    })
    void acceptsStandardFen(String fen) {
        assertTrue(Fen.isValid(fen), fen);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            // Every one of these would otherwise reach a shell and an expect interpreter.
            "8/8/8/8/8/8/8/8 w - - 0 1\"; send \"echo pwned\\n\"; #",
            "8/8/8/8/8/8/8/8 w - - 0 1\" ; id ; \"",
            "8/8/8/8/8/8/8/8 w - - 0 1$(id)",
            "8/8/8/8/8/8/8/8 w - - 0 1`id`",
            "8/8/8/8/8/8/8/8 w - - 0 1[exec id]",
            "8/8/8/8/8/8/8/8 w - - 0 1; rm -rf /tmp/nope",
            "8/8/8/8/8/8/8/8 w - - 0 1 | cat /etc/passwd",
            "8/8/8/8/8/8/8/8 w - - 0 1\\n",
            "8/8/8/8/8/8/8/8 w - - 0 1 && id",
    })
    void rejectsShellAndExpectMetacharacters(String fen) {
        assertFalse(Fen.isValid(fen), fen);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            " ",
            "not a fen",
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP w KQkq - 0 1", // only seven ranks
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR x KQkq - 0 1", // bad side to move
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq e9 0 1", // bad en passant square
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR9 w KQkq - 0 1", // digit out of range
    })
    void rejectsMalformedFen(String fen) {
        assertFalse(Fen.isValid(fen), fen);
    }

    @Test
    void rejectsNullAndOverlongInput() {
        assertFalse(Fen.isValid(null));
        assertFalse(Fen.isValid("8/8/8/8/8/8/8/8 w - - 0 " + "1".repeat(200)));
    }

    @Test
    void normalizeTrimsSurroundingWhitespaceAndNull() {
        assertTrue(Fen.isValid(Fen.normalize("  8/8/8/8/8/8/8/8 w - - 0 1\n")));
        assertFalse(Fen.isValid(Fen.normalize(null)));
    }
}
