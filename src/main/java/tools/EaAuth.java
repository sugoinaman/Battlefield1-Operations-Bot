package tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.dv8tion.jda.api.events.http.HttpRequestEvent;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class EaAuth {
    String remid;
    String sid;
    HttpClient client = HttpClient.newHttpClient();
    ObjectMapper objectMapper = new ObjectMapper();

    public String access_token;
    public String authCode;
    public String sessionId;

    public String getAccess_token() throws IOException, InterruptedException {
        if(access_token == null){
            getManifest();
        }
        return access_token;
    }

    public String getAuthCode() {
        if(authCode == null) getEaAuthId();
        return authCode;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void getManifest() throws IOException, InterruptedException {

        String uri = "https://accounts.ea.com/connect/auth?client_id=ORIGIN_JS_SDK&response_type=token&redirect_uri=nucleus%3Arest&prompt=none&release_type=prod";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .headers(
                        "User-Agent", "Mozilla/5.0",
                        "Content-Type", "application/json",
                        "Cookie", "remid=" + remid + "; sid=" + sid
                )
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JsonNode node = objectMapper.readTree(response.body());

        if (response.statusCode() == 200) {
            access_token = node.get("access_token").toString();
        }

    }

    public void getEaAuthId() {
        String uri = "https://accounts.ea.com/connect/auth?access_token=" +
                access_token + "&client_id=sparta-backend-as-user-pc&response_type=code&release_type=prod";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .headers( "User-Agent", "EA Download Manager Origin/10.5",
                        "Cookie", "remid=" + remid + "; sid=" + sid,
                        "localeInfo", "en_US",
                        "X-Origin-Platform", "PCWIN")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    }


}
