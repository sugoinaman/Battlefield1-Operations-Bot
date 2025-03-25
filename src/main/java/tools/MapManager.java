package tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import config.Configuration;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * This class has the following methods available
 * <p> 1.) fetchCurrentMap(): fetches the current running map </p>
 * <p> 2.) WriteToFile: writes to map.txt </p>
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
            System.out.println("Something is wrong with writeToFile method inside MapManager");
        }
    }

    /**
     * This sends the map change command to the API
     */
    public void bfMapChange(int mapNumber) {
        String urlString = "https://manager-api.gametools.network/api/changelevel";
        String token = Configuration.getGTToken();
        String groupId = "0a3488e2-848c-11ee-9ff7-02420a000912";
        String serverId = "d7073b2c-8490-11ee-9ec4-02420a00091d";

        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("accept", "application/json");
            connection.setRequestProperty("token", token);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String jsonInputString = String.format(
                    "{\"groupid\":\"%s\",\"mapnumber\":\"%d\",\"serverid\":\"%s\"}",
                    groupId, mapNumber, serverId
            );

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line.trim());
                }
                System.out.println("Response Body: " + response);
            }

            connection.disconnect();
        } catch (IOException e) {
            System.out.println("The change map API isn't working, GT is possibly down");
        }

    }

    public boolean isCurrentMapPartOfOperation(String currentMap, String previousMap) {
        if ((currentMap.equals("Argonne Forest") && previousMap.equals("Ballroom Blitz")) ||
                (currentMap.equals("Fort De Vaux") && previousMap.equals("Verdun Heights")) ||
                (currentMap.equals("Empire's Edge") && previousMap.equals("Monte Grappa")) ||
                (currentMap.equals("Achi Baba") && previousMap.equals("Cape Helles")) ||
                (currentMap.equals("Rupture") && previousMap.equals("Soissons")) ||
                (currentMap.equals("Tsaritsyn") && previousMap.equals("Volga River")) ||
                (currentMap.equals("Amiens") && previousMap.equals("St Quentin Scar")) ||
                (currentMap.equals("Suez") && previousMap.equals("Fao Fortress")) ||
                (currentMap.equals("Sinai Desert") && previousMap.equals("Suez"))){
            return true;
        }
        return false;
    }
}