package org.shelajev.mcpstockfish;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class McpAcceptHeaderCompatibilityFilterTest {

    private static final String INITIALIZE_REQUEST_TEMPLATE = """
            {"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"%s","capabilities":{},"clientInfo":{"name":"compatibility-test","version":"1.0"}}}
            """;

    @TestHTTPResource("/mcp")
    URI mcpEndpoint;

    @Test
    void acceptsJsonOnlyHostedClientRequests() throws Exception {
        HttpResponse<String> response = initialize("2026-07-28", "application/json");

        assertEquals(200, response.statusCode(), response.body());
        assertTrue(response.body().contains("\"protocolVersion\":\"2026-07-28\""), response.body());
        assertTrue(response.body().contains("\"serverInfo\""), response.body());
    }

    @Test
    void continuesToAcceptStandardStreamableHttpRequests() throws Exception {
        HttpResponse<String> response = initialize("2026-07-28", "application/json, text/event-stream");

        assertEquals(200, response.statusCode(), response.body());
        assertTrue(response.body().contains("\"protocolVersion\":\"2026-07-28\""), response.body());
    }

    @Test
    void continuesToAcceptPreviousProtocolVersion() throws Exception {
        HttpResponse<String> response = initialize("2025-06-18", "application/json, text/event-stream");

        assertEquals(200, response.statusCode(), response.body());
        assertTrue(response.body().contains("\"protocolVersion\":\"2025-06-18\""), response.body());
    }

    /**
     * The extension matches media types exactly, so wildcard clients such as curl used to be
     * rejected with a 400 even though they would happily read a JSON response.
     */
    @ParameterizedTest
    @ValueSource(strings = { "*/*", "application/*", "application/json;q=0.9", "*/*, text/event-stream" })
    void acceptsWildcardClients(String accept) throws Exception {
        HttpResponse<String> response = initialize("2026-07-28", accept);

        assertEquals(200, response.statusCode(), response.body());
        assertTrue(response.body().contains("\"protocolVersion\":\"2026-07-28\""), response.body());
    }

    private HttpResponse<String> initialize(String protocolVersion, String accept) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(mcpEndpoint)
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .header("Accept", accept)
                .POST(HttpRequest.BodyPublishers.ofString(INITIALIZE_REQUEST_TEMPLATE.formatted(protocolVersion)))
                .build();
        try (HttpClient httpClient = HttpClient.newHttpClient()) {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        }
    }
}
