package commands;
import config.Configuration;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import tools.MapManager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MapHistory extends ListenerAdapter {

    Dotenv dotenv = Dotenv.configure()
            .directory("./")
            .load();

    private String previousMap = null;
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final MapManager mapManager =  new MapManager();
    private static final List<String> recentMaps = new ArrayList<>();

    public MapHistory() {
        scheduler.scheduleAtFixedRate(this::updateMapHistory, 0, 10, TimeUnit.SECONDS);
    }

    public final List <String> getRecentMaps(){
        return new ArrayList<>(recentMaps);
    }

    private void updateMapHistory() {
        try {
            String currentMap = mapManager.fetchCurrentMap();
            if (currentMap == null || currentMap.equals(previousMap)) return; // Map didn't change!

            mapManager.writeToFile(currentMap);
            previousMap = currentMap;

            if (recentMaps.size() == 2) {
                recentMaps.removeFirst();
                recentMaps.add(currentMap);
            } else recentMaps.add(currentMap);
        } catch (Exception e) {
        }
    }
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {

        if (event.getGuild() == null) return;


        if (event.getName().equals("maphistory")) {
            event.deferReply().queue();
            try {
                BufferedReader br = new BufferedReader(new FileReader(Configuration.getFILE_PATH()));
                StringBuilder history = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    history.append(line).append("\n");
                }
                br.close();
                event.getHook().sendMessage("Played maps: " + history).queue();

            } catch (IOException e) {
                e.fillInStackTrace();
            }
        }
    }
//    @Override
//    public void onGuildReady(@NotNull GuildReadyEvent event) {
//        List<CommandData> commandData = new ArrayList<>();
//        commandData.add(Commands.slash("maphistory", "Shows maps played, history resets every 6:30AM"));
//        event.getGuild().updateCommands().addCommands(commandData).queue();
//    }

}

