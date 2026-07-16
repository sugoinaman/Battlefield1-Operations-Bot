package config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ConfigurationStoreTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void atomicallyPersistsRefreshedEaCookies() throws IOException {
        Path path = temporaryDirectory.resolve("config.json");
        Files.writeString(path, """
                {
                  "provider": "EA",
                  "discord": {
                    "token": "discord-token",
                    "serverId": "discord-server",
                    "logChannelId": "log-channel"
                  },
                  "ea": {
                    "remid": "old-remid",
                    "sid": "old-sid"
                  }
                }
                """);
        ConfigurationStore store = new ConfigurationStore(path);

        store.updateEaCredentials(new Configuration.Ea("new-remid", "new-sid"));

        Configuration persisted = store.load();
        assertEquals("new-remid", persisted.ea().remid());
        assertEquals("new-sid", persisted.ea().sid());
        assertFalse(Files.readString(path).contains("sessionId"));
    }
}
