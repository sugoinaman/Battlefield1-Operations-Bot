package tools;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

/**
 * Small HTTP boundary used by the GameTools map manager.
 */
interface GameToolsTransport {

    Response get(URI uri) throws IOException, InterruptedException;

    Response post(URI uri, Map<String, String> headers, String body)
            throws IOException, InterruptedException;

    record Response(int statusCode, String body) {}
}
