import commands.CustomMapSetter;
import commands.DiscordLogSink;
import commands.MapHistory;
import config.Configuration;
import config.ConfigurationStore;
import history.MapHistoryService;
import history.MapHistoryTracker;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import rotation.CustomMapRotationService;
import rotation.ExecutorRotationScheduler;
import ea.EaMapManager;
import tools.MapManager;
import tools.MapManagerGT;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Application composition root. Concrete Discord, GameTools, file, and scheduler adapters are wired here.
 */
public class Main {

    private static final String DEFAULT_CONFIG_PATH = "config.json";

    public static void main(String[] args) {
        if (args.length > 1) {
            throw new IllegalArgumentException(
                    "Expected at most one argument: the path to the JSON configuration file"
            );
        }

        Path configPath = Path.of(args.length == 1 ? args[0] : DEFAULT_CONFIG_PATH);
        ConfigurationStore configStore = new ConfigurationStore(configPath);
        Configuration config;
        try {
            config = configStore.load();
        } catch (IOException e) {
            System.err.println("Could not load configuration from "
                    + configPath.toAbsolutePath());
            e.printStackTrace();
            return;
        }

        JDA jda = JDABuilder.createDefault(config.discord().token()).build();

        MapManager mapManager = switch (config.provider()) {
            case GAME_TOOLS -> new MapManagerGT(config.gameTools());
            case EA -> new EaMapManager();
        };
        MapHistoryService historyService = new MapHistoryService(mapManager);
        new MapHistoryTracker(historyService).start();

        CustomMapRotationService rotation = new CustomMapRotationService(
                mapManager,
                new ExecutorRotationScheduler(),
                new DiscordLogSink(jda, config.discord().logChannelId())
        );

        String guildId = config.discord().serverId();
        jda.addEventListener(new MapHistory(historyService, guildId));
        jda.addEventListener(new CustomMapSetter(rotation, guildId));
    }
}
