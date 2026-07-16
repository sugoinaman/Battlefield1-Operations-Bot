package config;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Application configuration loaded from a JSON file at startup.
 */
public record Configuration(
        MapProvider provider,
        Discord discord,
        GameTools gameTools,
        Ea ea
) {

    public Configuration {
        provider = Objects.requireNonNull(provider, "provider is required");
        discord = Objects.requireNonNull(discord, "discord configuration is required");

        switch (provider) {
            case GAME_TOOLS -> {
                Objects.requireNonNull(
                        gameTools,
                        "gameTools configuration is required when provider is GAME_TOOLS"
                );
                if (ea != null) {
                    throw new IllegalArgumentException(
                            "ea configuration must be omitted when provider is GAME_TOOLS"
                    );
                }
            }
            case EA -> {
                Objects.requireNonNull(ea, "ea configuration is required when provider is EA");
                if (gameTools != null) {
                    throw new IllegalArgumentException(
                            "gameTools configuration must be omitted when provider is EA"
                    );
                }
            }
        }
    }

    public static Configuration load(Path path) throws IOException {
        return new ConfigurationStore(path).load();
    }

    public Configuration withEaCredentials(Ea updatedEa) {
        if (provider != MapProvider.EA) {
            throw new IllegalStateException("EA credentials cannot be stored for a GameTools configuration");
        }
        return new Configuration(provider, discord, null, updatedEa);
    }

    public enum MapProvider {
        GAME_TOOLS,
        EA
    }

    public record Discord(String token, String serverId, String logChannelId) {

        public Discord {
            token = requireNonBlank(token, "discord.token");
            serverId = requireNonBlank(serverId, "discord.serverId");
            logChannelId = requireNonBlank(logChannelId, "discord.logChannelId");
        }
    }

    public record GameTools(String token, String serverUrl, String groupId, String serverId) {

        public GameTools {
            token = requireNonBlank(token, "gameTools.token");
            serverUrl = requireNonBlank(serverUrl, "gameTools.serverUrl");
            groupId = requireNonBlank(groupId, "gameTools.groupId");
            serverId = requireNonBlank(serverId, "gameTools.serverId");
        }
    }

    /**
     * Persistent EA cookies. SID may be absent on first startup and generated from REMID.
     */
    public record Ea(String remid, String sid) {

        public Ea {
            remid = requireNonBlank(remid, "ea.remid");
            sid = normalizeOptional(sid);
        }

        public boolean hasSid() {
            return sid != null;
        }
    }

    private static String requireNonBlank(String value, String propertyName) {
        Objects.requireNonNull(value, propertyName + " is required");
        if (value.isBlank()) {
            throw new IllegalArgumentException(propertyName + " must not be blank");
        }
        return value;
    }

    private static String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
