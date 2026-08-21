package org.shelajev.mcpstockfish;

import java.util.List;

import io.quarkus.vertx.web.RouteFilter;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

/**
 * Makes the MCP endpoint compatible with hosted clients that request JSON responses only.
 *
 * The Quarkiverse MCP HTTP handler requires every POST to advertise both JSON and SSE support,
 * even when the response is JSON. OpenAI's hosted MCP connector currently advertises only JSON,
 * so add the missing SSE media type before the request reaches the extension handler.
 */
public class McpAcceptHeaderCompatibilityFilter {

    private static final String MCP_PATH = "/mcp";
    private static final String JSON = "application/json";
    private static final String SSE = "text/event-stream";

    @RouteFilter(100)
    void acceptJsonOnlyMcpClients(RoutingContext context) {
        if (context.request().method() == HttpMethod.POST
                && MCP_PATH.equals(context.normalizedPath())) {
            List<String> acceptedTypes = context.request().headers().getAll(HttpHeaders.ACCEPT);
            if (accepts(acceptedTypes, JSON) && !accepts(acceptedTypes, SSE)) {
                context.request().headers().add(HttpHeaders.ACCEPT, SSE);
            }
        }
        context.next();
    }

    private static boolean accepts(List<String> acceptedTypes, String contentType) {
        return acceptedTypes.stream().anyMatch(value -> value.contains(contentType));
    }
}
