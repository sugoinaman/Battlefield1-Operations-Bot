package implementation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import config.Configuration;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MapHistoryImpl {

    Dotenv dotenv = Dotenv.configure()
            .directory("./")
            .load();

    private static final Logger log = LoggerFactory.getLogger(MapHistoryImpl.class);
    private static String lastMap = null;
    private static final List<String> mapHistory = new ArrayList<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final String FILE_PATH = Configuration.getFILE_PATH();
    private static final String NEWA_URL = "https://api.gametools.network/bf1/servers/?name=%5Bnew%21%5DAllMap%20Operation%20%7C%20discord.gg%2Fnewa%20%7C%20NoHacker%20HappyGame&platform=pc&limit=10&region=all&lang=en-us";
    private static List<String> recentMaps = new ArrayList<>();

    public MapHistoryImpl() {
        scheduler.scheduleAtFixedRate(this::updateMapHistory, 0, 20, TimeUnit.SECONDS);
    }







    private String fetchCurrentMap() throws IOException, URISyntaxException {
        URI uri = new URI(NEWA_URL);
        JsonNode serversArray = getServersArray(uri.toURL());

        if (serversArray.isArray() && !serversArray.isEmpty()) {
            return serversArray.get(0).path("currentMap").asText();
        }
        return null;
    }

    private void updateMapHistory() {
        try {
            String currentMap = fetchCurrentMap();
            if (currentMap == null || currentMap.equals(lastMap)) return; // insta return

            // write to txt file
            mapHistory.add(currentMap);
            writeToFile(currentMap);

            // add and
            log.info("New Map Detected: {}", currentMap);
            if (lastMap != null) log.info("Last Map was: {}", lastMap);

            lastMap = currentMap;
        } catch (Exception e) {
            log.error("Error updating map history", e);
        }
    }








    public List<String> mapHistory() {
        return new ArrayList<>(mapHistory); // Return a copy of the list
    }

    private JsonNode getServersArray(URL url) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(url);

        return rootNode.path("servers");
        //extracted servers from array and returns empty array if it doesn't exist
    }

    private void writeToFile(String mapName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {

            ZonedDateTime cetTime = ZonedDateTime.now(ZoneId.of("Europe/Berlin")); // CET/CEST timezone
            String timestamp = cetTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"));
            writer.write(mapName + " at " + timestamp);
            writer.newLine();
        } catch (IOException e) {
            log.error("Error writing to file", e);
        }
    }
}

