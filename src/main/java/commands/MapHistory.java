package commands;

import config.Configuration;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import tools.MapManager;

import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/*ToDo: 1.) change .txt store  -> json
        2.) Track map history of multiple servers at a time
        3.) Reset the map history values at 6am (by resetting the json)
 */


public class MapHistory extends ListenerAdapter {

    private String previousMap = null;
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final MapManager mapManager = new MapManager();

    public MapHistory() {
        scheduler.scheduleAtFixedRate(this::updateMapHistory, 0, 30, TimeUnit.SECONDS);
    }


    private void updateMapHistory() {
        try {
            String currentMap = mapManager.getCurrentMap();
            if (currentMap == null || currentMap.equals(previousMap)) return; // Map didn't change!
            mapManager.writeToFile(currentMap);
            previousMap = currentMap;
        } catch (Exception e) {
            System.out.println("Issue with updateMapHistory, most likely internet issue");
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {

        if (event.getGuild() == null) return;

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(new Color(54, 232, 15));

        String guildId = event.getGuild().getId();
        if (!guildId.equals(Configuration.getDISCORD_SERVER_ID()) && !guildId.equals("1338475669000028182")) { // !(A OR B) = NOT A AND NOT B
            event.getChannel().sendMessageEmbeds(embedBuilder.setDescription("This command is not available in this server.").build()).queue();
            return;
        }

        String eventName = event.getName();
        switch (eventName) {
            case "map_history" -> {
                event.deferReply().queue();
                String serverName = event.getOption("server_name").getAsString();
            }
            case "add_server_tracking" -> {
                event.deferReply().queue();
                String gameId = event.getOption("gameId").getAsString();
                String serverName = event.getOption("server_name").getAsString();

            }
            case "remove_server_tracking" -> {
                event.deferReply().queue();
                String serverName = event.getOption("server_name").getAsString();

            }
            default -> {
                System.out.println("Wrong command");
            }
        }

        if (event.getName().equals("map_history")) {
            event.deferReply().queue();
            try {
                BufferedReader br = new BufferedReader(new FileReader(Configuration.getFILE_PATH()));
                StringBuilder history = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    history.append(line).append("\n");
                }
                br.close();
                embedBuilder.setAuthor("Played maps are:");
                embedBuilder.setDescription(history);
                event.getHook().sendMessageEmbeds(embedBuilder.build()).queue();

            } catch (IOException e) {
                e.fillInStackTrace();
            }
        }
    }

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        event.getGuild().upsertCommand(Commands.slash("map_history", "Shows maps history on OPS, history resets every 6:30AM")).queue();
    }
}