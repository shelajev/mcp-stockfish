package org.shelajev.mcpstockfish;


import io.quarkiverse.mcp.server.TextContent;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import io.quarkiverse.mcp.server.ToolResponse;
import jakarta.inject.Singleton;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Singleton
public class Maia {

    @Tool(description = "You have the Maia3 engine to analyze a chess position. Maia3 returns human-like moves rather than the engine-best move. You can set the Elo conditioning strength.")
    ToolResponse whatMoveWouldHumanPlay(@ToolArg(description = "FEN of the position to analyse") String fen,
                              @ToolArg(description = "Elo rating to condition Maia3 with, from 0 to 5000") int rating) {

        try {
            return ToolResponse.success(new TextContent(runMaia3(fen, rating)));
        } catch (Exception e) {
            return ToolResponse.success(new TextContent("Error running Maia3: " + e.getMessage()));
        }
    }

    private static String runMaia3(String fen, int rating) throws IOException, InterruptedException {
        int elo = Math.max(0, Math.min(5000, rating));
        int timeoutSeconds = Integer.parseInt(System.getenv().getOrDefault("MAIA3_TIMEOUT_SECONDS", "60"));

        List<String> command = new ArrayList<>();
        command.add(System.getenv().getOrDefault("MAIA3_UCI", "maia3-uci"));
        command.add("--model");
        command.add(System.getenv().getOrDefault("MAIA3_MODEL", "maia3-5m"));
        command.add("--device");
        command.add(System.getenv().getOrDefault("MAIA3_DEVICE", "cpu"));
        command.add("--no-use-amp");

        String checkpoint = System.getenv("MAIA3_CHECKPOINT");
        if (checkpoint != null && !checkpoint.isBlank()) {
            command.add("--checkpoint-path");
            command.add(checkpoint);
            command.add("--local-files-only");
        }

        Process process = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .start();

        StringBuilder output = new StringBuilder();
        Instant deadline = Instant.now().plus(Duration.ofSeconds(timeoutSeconds));

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8));
             BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {

            send(writer, "uci");
            readUntil(reader, output, "uciok", deadline);

            send(writer, "setoption name Elo value " + elo);
            send(writer, "position fen " + fen);
            send(writer, "go");
            readUntil(reader, output, "bestmove", deadline);

            send(writer, "quit");
        } finally {
            if (!process.waitFor(2, TimeUnit.SECONDS)) {
                process.destroyForcibly();
            }
        }

        return output.toString();
    }

    private static void send(BufferedWriter writer, String command) throws IOException {
        writer.write(command);
        writer.newLine();
        writer.flush();
    }

    private static void readUntil(BufferedReader reader, StringBuilder output, String expected, Instant deadline) throws IOException, InterruptedException {
        while (Instant.now().isBefore(deadline)) {
            if (!reader.ready()) {
                Thread.sleep(50);
                continue;
            }
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            output.append(line).append('\n');
            if (line.contains(expected)) {
                return;
            }
        }
        throw new IOException("Timed out waiting for Maia3 to emit " + expected);
    }
}
