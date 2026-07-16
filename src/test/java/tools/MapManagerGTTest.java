package tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import config.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MapManagerGTTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private FakeTransport transport;
    private MapManagerGT mapManager;

    @BeforeEach
    void setUp() {
        transport = new FakeTransport();
        mapManager = new MapManagerGT(
                new Configuration.GameTools(
                        "secret-token",
                        "https://api.gametools.network/bf1/servers/?name=test",
                        "group-id",
                        "server-id"
                ),
                transport
        );
    }

    @Test
    void readsCurrentMapAndPlayerCountFromTheFirstMatchingServer() {
        String response = """
                {
                  "servers": [
                    {
                      "currentMap": "Monte Grappa",
                      "playerAmount": 42
                    }
                  ]
                }
                """;
        transport.getResponses.add(new GameToolsTransport.Response(200, response));
        transport.getResponses.add(new GameToolsTransport.Response(200, response));

        assertEquals("Monte Grappa", mapManager.getCurrentMap());
        assertEquals(42, mapManager.getPlayerCount());
        assertEquals(
                URI.create("https://api.gametools.network/bf1/servers/?name=test"),
                transport.lastGetUri
        );
    }

    @Test
    void sendsTheDocumentedChangeLevelRequest() throws Exception {
        transport.postResponse = new GameToolsTransport.Response(200, "{\"message\":\"changed\"}");

        mapManager.changeMap(13);

        assertEquals(
                URI.create("https://manager-api.gametools.network/api/changelevel"),
                transport.lastPostUri
        );
        assertEquals("secret-token", transport.lastHeaders.get("token"));
        assertEquals("application/json", transport.lastHeaders.get("Content-Type"));

        JsonNode payload = OBJECT_MAPPER.readTree(transport.lastPostBody);
        assertEquals("group-id", payload.path("groupid").asText());
        assertEquals(13, payload.path("mapnumber").asInt());
        assertEquals("server-id", payload.path("serverid").asText());
    }

    @Test
    void reportsNonSuccessfulReadResponses() {
        transport.getResponses.add(
                new GameToolsTransport.Response(503, "{\"detail\":\"maintenance\"}")
        );

        MapAccessException error =
                assertThrows(MapAccessException.class, mapManager::getCurrentMap);

        assertTrue(error.getMessage().contains("HTTP 503"));
    }

    @Test
    void reportsNonSuccessfulMapChangesInsteadOfPretendingTheyWorked() {
        transport.postResponse =
                new GameToolsTransport.Response(401, "{\"detail\":\"invalid token\"}");

        MapAccessException error =
                assertThrows(MapAccessException.class, () -> mapManager.changeMap(13));

        assertTrue(error.getMessage().contains("HTTP 401"));
    }

    @Test
    void reportsMalformedServerResponses() {
        transport.getResponses.add(new GameToolsTransport.Response(200, "not-json"));

        MapAccessException error =
                assertThrows(MapAccessException.class, mapManager::getCurrentMap);

        assertEquals("GameTools returned invalid JSON", error.getMessage());
    }

    private static final class FakeTransport implements GameToolsTransport {
        private final Deque<Response> getResponses = new ArrayDeque<>();
        private Response postResponse;
        private URI lastGetUri;
        private URI lastPostUri;
        private Map<String, String> lastHeaders;
        private String lastPostBody;

        @Override
        public Response get(URI uri) throws IOException {
            lastGetUri = uri;
            if (getResponses.isEmpty()) {
                throw new IOException("No fake GET response configured");
            }
            return getResponses.removeFirst();
        }

        @Override
        public Response post(URI uri, Map<String, String> headers, String body)
                throws IOException {
            lastPostUri = uri;
            lastHeaders = headers;
            lastPostBody = body;
            if (postResponse == null) {
                throw new IOException("No fake POST response configured");
            }
            return postResponse;
        }
    }
}
