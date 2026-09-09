package org.shelajev.mcpstockfish;

import java.util.List;

import io.quarkus.vertx.web.RouteFilter;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

/**
 * Makes the MCP endpoint compatible with hosted clients that request JSON responses only.
 * <p>
 * The Quarkiverse MCP HTTP handler requires every POST to advertise both JSON and SSE support,
 * even when the response is JSON. OpenAI's hosted MCP connector currently advertises only JSON,
 * and plain HTTP clients such as curl default to a wildcard, so add the missing concrete media
 * types before the request reaches the extension handler. The handler matches media types exactly,
 * so a wildcard alone is not enough and JSON has to be spelled out as well.
 */
public class McpAcceptHeaderCompatibilityFilter {

    private static final String MCP_PATH = "/mcp";
    private static final String JSON = "application/json";
    private static final String SSE = "text/event-stream";
    private static final String ANY = "*/*";
    private static final String ANY_APPLICATION = "application/*";

    @RouteFilter(100)
    void acceptJsonOnlyMcpClients(RoutingContext context) {
        if (context.request().method() == HttpMethod.POST
                && MCP_PATH.equals(context.normalizedPath())) {
            List<String> acceptedTypes = context.request().headers().getAll(HttpHeaders.ACCEPT);
            if (acceptsJson(acceptedTypes)) {
                boolean missingJson = !accepts(acceptedTypes, JSON);
                boolean missingSse = !accepts(acceptedTypes, SSE);
                if (missingJson) {
                    context.request().headers().add(HttpHeaders.ACCEPT, JSON);
                }
                if (missingSse) {
                    context.request().headers().add(HttpHeaders.ACCEPT, SSE);
                }
            }
        }
        context.next();
    }

    private static boolean acceptsJson(List<String> acceptedTypes) {
        return accepts(acceptedTypes, JSON)
                || accepts(acceptedTypes, ANY)
                || accepts(acceptedTypes, ANY_APPLICATION);
    }

    private static boolean accepts(List<String> acceptedTypes, String contentType) {
        return acceptedTypes.stream().anyMatch(value -> value.contains(contentType));
    }
}
