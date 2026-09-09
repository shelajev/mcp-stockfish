package org.shelajev.mcpstockfish;

import java.util.regex.Pattern;

/**
 * Validates FEN strings before they are handed to an external chess engine.
 *
 * <p>
 * The Stockfish tool interpolates the FEN into a command string that is executed by a shell and
 * then by expect, so the accepted character set is deliberately an allowlist rather than an escape
 * pass. Quoting a value for two nested interpreters is easy to get subtly wrong; a FEN only ever
 * needs piece letters, digits, slashes, dashes and single spaces, so anything else is rejected and
 * no shell or expect metacharacter can reach the command.
 */
final class Fen {

    static final String INVALID_MESSAGE = "Invalid FEN. Expected a standard FEN such as "
            + "'rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1'.";

    private static final int MAX_LENGTH = 100;

    private static final Pattern VALID = Pattern.compile(
            "([1-8pnbrqkPNBRQK]{1,8}/){7}[1-8pnbrqkPNBRQK]{1,8}" // piece placement, 8 ranks
                    + " [wb]" // side to move
                    + " (-|[KQkqA-Ha-h]{1,4})" // castling rights, including Shredder-FEN files
                    + " (-|[a-h][1-8])" // en passant target square
                    + "( \\d{1,4}( \\d{1,4})?)?"); // optional halfmove and fullmove clocks

    private Fen() {
    }

    /**
     * Trims surrounding whitespace so that clients sending a padded FEN are not rejected.
     */
    static String normalize(String fen) {
        return fen == null ? "" : fen.strip();
    }

    static boolean isValid(String fen) {
        return fen != null && fen.length() <= MAX_LENGTH && VALID.matcher(fen).matches();
    }
}
