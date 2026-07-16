package config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfigurationTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void loadsGameToolsWithoutRequiringEaCredentials() throws IOException {
        Configuration config = Configuration.load(write("""
                "provider": "GAME_TOOLS",
                "gameTools": {
                  "token": "gt-token",
                  "serverUrl": "https://example.invalid/servers",
                  "groupId": "group-id",
                  "serverId": "server-id"
                }
                """));

        assertEquals(Configuration.MapProvider.GAME_TOOLS, config.provider());
        assertEquals("gt-token", config.gameTools().token());
        assertNull(config.ea());
    }

    @Test
    void loadsEaWithoutRequiringGameToolsAndAllowsSidBootstrap() throws IOException {
        Configuration config = Configuration.load(write("""
                "provider": "EA",
                "ea": {
                  "remid": "remid",
                  "sid": null
                }
                """));

        assertEquals(Configuration.MapProvider.EA, config.provider());
        assertEquals("remid", config.ea().remid());
        assertFalse(config.ea().hasSid());
        assertNull(config.gameTools());
    }

    @Test
    void rejectsConfigurationForBothProviders() throws IOException {
        Path configFile = write("""
                "provider": "EA",
                "gameTools": {
                  "token": "gt-token",
                  "serverUrl": "https://example.invalid/servers",
                  "groupId": "group-id",
                  "serverId": "server-id"
                },
                "ea": {
                  "remid": "remid",
                  "sid": "sid"
                }
                """);

        assertThrows(IOException.class, () -> Configuration.load(configFile));
    }

    @Test
    void rejectsMissingSelectedProviderConfiguration() throws IOException {
        Path configFile = write("""
                "provider": "EA"
                """);

        assertThrows(IOException.class, () -> Configuration.load(configFile));
    }

    private Path write(String providerFields) throws IOException {
        Path configFile = temporaryDirectory.resolve("config.json");
        Files.writeString(configFile, """
                {
                  %s,
                  "discord": {
                    "token": "discord-token",
                    "serverId": "discord-server",
                    "logChannelId": "log-channel"
                  }
                }
                """.formatted(providerFields));
        return configFile;
    }
}
