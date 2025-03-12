package tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import config.Configuration;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/* This class has the following methods available
    1.) fetchCurrentMap(): fetches the current running map
    2.) API url returns maps which is inside an array called "servers" inside the json string
 */

public class MapManager {

    private final String FILE_PATH = Configuration.getFILE_PATH();
    private static final String NEWA_URL = "https://api.gametools.network/bf1/servers/?name=%5Bnew%21%5DAllMap%20Operation%20%7C%20discord.gg%2Fnewa%20%7C%20NoHacker%20HappyGame&platform=pc&limit=10&region=all&lang=en-us";

    public String fetchCurrentMap() throws IOException, URISyntaxException {

        URI uri = new URI(NEWA_URL);
        JsonNode serversArray = getServersArray(uri.toURL());

        if (serversArray.isArray() && !serversArray.isEmpty()) {
            return serversArray.get(0).path("currentMap").asText();
        }
        return null;
    }

    private JsonNode getServersArray(URL url) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(url);

        return rootNode.path("servers");

    }

    public void writeToFile(String mapName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {

            ZonedDateTime cetTime = ZonedDateTime.now(ZoneId.of("Europe/Berlin")); // CET/CEST timezone
            String timestamp = cetTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"));
            writer.write(mapName + " at " + timestamp);
            writer.newLine();
        } catch (IOException e) {
        }
    }
}
