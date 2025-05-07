package tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class EaApi {

    HttpClient client = HttpClient.newHttpClient();

    ObjectMapper objectMapper = new ObjectMapper();


    String[] headers = {"User-Agent", "ProtoHttp 1.3/DS 15.1.2.1.0 (Windows)",
            "X-ClientVersion", "release-bf1-lsu35_26385_ad7bf56a_tunguska_all_prod",
            "X-DbId", "Tunguska.Shipping2PC.Win32",
            "X-CodeCL", "3779779",
            "X-DataCL", "3779779",
            "X-SaveGameVersion", "26",
            "X-HostingGameId", "tunguska",
            "X-Sparta-Info", "tenancyRootEnv = unknown;tenancyBlazeEnv = unknown",
            "Connection", "keep-alive"};

    String access_token;

    private HttpResponse<String> sendRequest(String jsonPayLoad) throws ExecutionException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://sparta-gw.battlelog.com/jsonrpc/pc/api"))
                .headers(headers)
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayLoad))
                .build();

        CompletableFuture<HttpResponse<String>> futureResponse = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());

        return futureResponse.get();
    }


    /**
     * Selects a specific level for a persisted game.
     *
     * @param persistedGameId server guid
     * @param levelIndex      index of the map
     */
    public void chooseLevel(String persistedGameId, int levelIndex) throws ExecutionException, InterruptedException {

        String id = UUID.randomUUID().toString();

        String json = """
                {
                    "jsonrpc": "2.0",
                    "method": "RSP.kickPlayer",
                    "params": {
                        "game": "tunguska",
                        "persistedGameId": %s,
                        "levelIndex": %d,
                    },
                     "id": "%s"
                }
                """.formatted(persistedGameId, levelIndex, id);

        HttpResponse<String> response = sendRequest(json);

        System.out.println("This is the response \n" + response);
        if (response.statusCode() == 200) System.out.println("\n ok");
    }

    /**
     * Removes a player from the game specified by the given game ID.
     *
     * @param gameId    the unique identifier of the game from which the player will be removed
     * @param personaId the unique identifier of the player to be removed from the game
     * @param reason    the reason for kick
     */
    public void kickPlayer(int gameId, String personaId, String reason) throws ExecutionException, InterruptedException {

        String id = UUID.randomUUID().toString();

        String json = """
                {
                    "jsonrpc": "2.0",
                    "method": "RSP.kickPlayer",
                    "params": {
                        "game": "tunguska",
                        "personaId": %s,
                        "gameId": %d,
                        "reason": "%s"
                    },
                     "id": "%s"
                }
                """.formatted(personaId, gameId, reason, id);

        HttpResponse<String> response = sendRequest(json);

        System.out.println("This is the response \n" + response);
        if (response.statusCode() == 200) System.out.println("\n ok");
    }

    /**
     * @param player_name name of the player
     * @return personaId of player
     */
    public String getPersonaByName(String player_name) throws IOException, InterruptedException {

        String uri = "https://gateway.ea.com/proxy/identity/personas?namespaceName=cem_ea_id&displayName=" + player_name;

        String auth = "Bearer" + access_token;

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .headers(
                        "Accept", "application/json",
                        "X-Expand-Results", "true",
                        "Authorization", auth,
                        "Accept-Encoding", "deflate")
                .GET()
                .build();

        HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if(response.statusCode()==200) {

            JsonNode jsonNode = objectMapper.readTree(response.body());
        }
        else{
            System.out.println(response);
        }
        return null;
    }

}


// checking if intellij is no longer bugged