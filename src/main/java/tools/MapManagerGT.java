package tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import config.Configuration;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Objects;

/**
 * GameTools implementation of map lookup, player-count lookup, and map changes.
 */
public final class MapManagerGT implements MapManager {

    private static final URI CHANGE_LEVEL_URI =
            URI.create("https://manager-api.gametools.network/api/changelevel");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final Configuration.GameTools config;
    private final GameToolsTransport transport;
    private final URI serverLookupUri;

    public MapManagerGT(Configuration.GameTools config) {
        this(config, new JdkGameToolsTransport());
    }

    MapManagerGT(Configuration.GameTools config, GameToolsTransport transport) {
        this.config = Objects.requireNonNull(config);
        this.transport = Objects.requireNonNull(transport);
        try {
            serverLookupUri = URI.create(config.serverUrl());
        } catch (IllegalArgumentException e) {
            throw new MapAccessException("GameTools server URL is invalid", e);
        }
    }

    @Override
    public String getCurrentMap() {
        JsonNode server = fetchServer();
        String currentMap = server.path("currentMap").asText();

        if (currentMap.isBlank()) {
            throw new MapAccessException("GameTools returned no current map");
        }
        return currentMap;
    }

    @Override
    public int getPlayerCount() {
        JsonNode playerAmount = fetchServer().path("playerAmount");
        if (!playerAmount.isIntegralNumber() || !playerAmount.canConvertToInt()) {
            throw new MapAccessException("GameTools returned an invalid player count");
        }

        int playerCount = playerAmount.intValue();
        if (playerCount < 0) {
            throw new MapAccessException("GameTools returned a negative player count");
        }
        return playerCount;
    }

    @Override
    public void changeMap(int mapIndex) {
        if (mapIndex < 0) {
            throw new IllegalArgumentException("Map index must not be negative");
        }

        ObjectNode payload = OBJECT_MAPPER.createObjectNode();
        payload.put("groupid", config.groupId());
        payload.put("mapnumber", mapIndex);
        payload.put("serverid", config.serverId());

        try {
            GameToolsTransport.Response response = transport.post(
                    CHANGE_LEVEL_URI,
                    Map.of(
                            "accept", "application/json",
                            "token", config.token(),
                            "Content-Type", "application/json"
                    ),
                    OBJECT_MAPPER.writeValueAsString(payload)
            );
            requireSuccessfulResponse("change the map", response);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MapAccessException("Interrupted while asking GameTools to change the map", e);
        } catch (IOException e) {
            throw new MapAccessException("Could not ask GameTools to change the map", e);
        }
    }

    private JsonNode fetchServer() {
        try {
            GameToolsTransport.Response response = transport.get(serverLookupUri);
            requireSuccessfulResponse("fetch server information", response);

            JsonNode servers = OBJECT_MAPPER.readTree(response.body()).path("servers");
            if (!servers.isArray() || servers.isEmpty()) {
                throw new MapAccessException("GameTools returned no servers");
            }
            return servers.get(0);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MapAccessException("Interrupted while fetching GameTools server information", e);
        } catch (JsonProcessingException e) {
            throw new MapAccessException("GameTools returned invalid JSON", e);
        } catch (IOException e) {
            throw new MapAccessException("Could not fetch GameTools server information", e);
        }
    }

    private static void requireSuccessfulResponse(
            String operation,
            GameToolsTransport.Response response
    ) {
        int statusCode = response.statusCode();
        if (statusCode >= 200 && statusCode < 300) {
            return;
        }

        String responseBody = response.body() == null
                ? ""
                : response.body().replaceAll("\\s+", " ").trim();
        if (responseBody.length() > 300) {
            responseBody = responseBody.substring(0, 300) + "...";
        }

        String suffix = responseBody.isEmpty() ? "" : ": " + responseBody;
        throw new MapAccessException(
                "GameTools could not " + operation + " (HTTP " + statusCode + ")" + suffix
        );
    }
}
