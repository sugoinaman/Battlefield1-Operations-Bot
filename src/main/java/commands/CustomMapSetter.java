package commands;

import config.Configuration;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;
import tools.MapManager;

import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CustomMapSetter extends ListenerAdapter {

    private JDA jda;
    private List<String> mapHolder = new ArrayList<>(); //Contains maps entered by the user from slash commands
    private static ScheduledExecutorService scheduler = null;
    MapManager mapManager = new MapManager();
    private static final String LOG_CHANNEL_ID = Configuration.getLOG_CHANNEL_ID();
    private boolean waitingForCurrentMapToFinish = false;
    private Instant lastMapChangeTime = Instant.EPOCH;
    private boolean sentTheOperationLog = false;
    private boolean isCustomMapLoopOn = false;
    private String currentMapWhenCustomCommand = null;
    private String b = null;
    private int c = 0;
    // availableMaps are starting maps of an OPS because that's what we are allowed to set.
    private final String[] availableMaps = {"Giant's Shadow", "Monte Grappa", "River Somme", "Cape Helles", "Zeebrugge", "Fao Fortress", "Soissons", "Volga River", "St Quentin Scar", "Ballroom Blitz", "Łupków Pass", "Prise de Tahure", "Verdun Heights"};
    // all maps are mapped to an integer value which we get from GameTools, we do it because the map change API uses a map index rather than a name
    private static final HashMap<String, Integer> hashMap = new HashMap<>();

    static {
        hashMap.put("Soissons", 0);
        hashMap.put("Achi Baba", 1);
        hashMap.put("Suez", 2);
        hashMap.put("Fort De Vaux", 3);
        hashMap.put("Giant's Shadow", 4);
        hashMap.put("Monte Grappa", 5);
        hashMap.put("Verdun Heights", 6);
        hashMap.put("River Somme", 7);
        hashMap.put("Cape Helles", 8);
        hashMap.put("Prise de Tahure", 9);
        hashMap.put("Zeebrugge", 10);
        hashMap.put("Empire's Edge", 11);
        hashMap.put("Volga River", 12);
        hashMap.put("Rupture", 13);
        hashMap.put("St Quentin Scar", 14);
        hashMap.put("Amiens", 15);
        hashMap.put("Ballroom Blitz", 16);
        hashMap.put("Galicia", 17);
        hashMap.put("Tsaritsyn", 18);
        hashMap.put("Brusilov Keep", 19);
        hashMap.put("Łupków Pass", 20);
        hashMap.put("Argonne Forest", 21);
        hashMap.put("Sinai Desert", 22);
        hashMap.put("Fao Fortress", 23);
    }

    public CustomMapSetter(JDA jda) {
        this.jda = jda;
    }

    public void changeMap() { // the main function which will run every 5 seconds.

        try {
            if (mapManager.getPlayerCount() < 10) { // stop the custom map when server is dead!
                stopScheduler();
            }
            String currentMapAfterCustomCommand = mapManager.getCurrentMap();
            String a = mapManager.getCurrentMap(); // Updated 5 seconds.

            if (Duration.between(lastMapChangeTime, Instant.now()).getSeconds() < 60) { //wait 60 seconds after changing maps
                return;
            }

            if (waitingForCurrentMapToFinish) {
                if (currentMapWhenCustomCommand.equals(currentMapAfterCustomCommand)) {
                    return;
                } else {
                    waitingForCurrentMapToFinish = false;
                    System.out.println("The current map is: " + a + " and the previous map was: " + b + ". " + "Starting custom rotation");
                    sendLog("Current map is finished, starting custom rotation now...");
                }
            }

            if (!b.equals(a)) {
                if (mapManager.isCurrentMapPartOfOperation(a, b)) {
                    if (!sentTheOperationLog) {
                        // map is part of an operation
                        sendLog("Current map is: " + a + " which is part of  " + b + "'s operation " + " Skipping map change....");
                        sentTheOperationLog = true;
                    }
                    return;
                } else if (a.equals(mapHolder.get(c))) {
                    // map was auto changed to a map which is what we wanted, so we skip the map change here.
                    sendLog("Map is now " + a);
                    b = mapHolder.get(c);
                    c++;
                    sentTheOperationLog = false;
                    lastMapChangeTime = Instant.now(); // Wait 60 more seconds here in case game tools API doesn't update quickly
                    return;
                } else {

                    c = (c + 1) % mapHolder.size();

                    sendLog("Current map: " + a + " | Previous map was: " + b + " | Switching maps to: " + mapHolder.get(c));

                    Thread.sleep(12000); // 12-second delay so the unwanted map loads and players don't get bugged

                    mapManager.bfMapChange(hashMap.get(mapHolder.get(c)));
                    b = mapHolder.get(c);
                    sentTheOperationLog = false;
                    lastMapChangeTime = Instant.now();
                    sendLog("Map changed from " + a + " to " + mapHolder.get(c));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        if (event.getName().equals("custom_map")) {
            List<Command.Choice> options = Stream.of(availableMaps)
                    .filter(word -> word.startsWith(event.getFocusedOption().getValue())) // only display words that start with the user's current input
                    .map(word -> new Command.Choice(word, word)) // map the words to choices
                    .collect(Collectors.toList());
            event.replyChoices(options).queue();
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {

        if (event.getGuild() == null) return;

        String guildId = event.getGuild().getId();
        if (!guildId.equals(Configuration.getDISCORD_SERVER_ID()) && !guildId.equals("1338475669000028182")) { // !(A OR B) = NOT A AND NOT B
            event.reply("This command is not available in this server.").setEphemeral(true).queue();
            return;
        }

        if (event.getName().equals("custom_map")) {
            List<String> customMapsSetByUser = event.getOptions().stream()
                    .map(OptionMapping::getAsString)
                    .collect(Collectors.toList());
            event.reply("Maps selected for rotation are: " + String.join(", ", customMapsSetByUser)).queue();
            sendLog("List of maps to be looped has been updated to: " + (String.join("-> ", customMapsSetByUser)) + ". Waiting for current map to be finished before turning on");

            mapHolder = new ArrayList<>(customMapsSetByUser);
            c = 0; // reset the counter
            // if the command runs again without turning off scheduler first it will queue up another scheduler and pool up unnecessary resources
            try {
                if (scheduler != null && !scheduler.isShutdown()) {
                    scheduler.shutdown();
                    System.out.println("Scheduler was already running");
                }
            } catch (Exception e) {
                System.out.println("Scheduler was stopped");
            }
            startScheduler();
            try {
                currentMapWhenCustomCommand = mapManager.getCurrentMap();
                b = currentMapWhenCustomCommand;
            } catch (Exception e) {
                System.out.println("Error with fetching current map, def issue with GT");
            }

            waitingForCurrentMapToFinish = true;
            System.out.println(mapHolder);
            isCustomMapLoopOn = true;

        }

        if (event.getName().equals("toggle_off")) {
            if (isCustomMapLoopOn) {
                event.reply("Turned off custom map rotation").queue();
                try {
                    mapHolder.clear();
                    isCustomMapLoopOn = false;
                    c = 0;
                } catch (Exception e) {
                    System.out.println("Error clearing mapHolder");
                    sendLog("Error clearing mapHolder");
                }
                stopScheduler();
            } else {
                event.reply("Cannot switch custom map loop because it wasn't ON").queue();
                sendLog("Cannot switch custom map loop because it wasn't ON");
            }
        }

        if (event.getName().equals("custom_map_info")) {

            event.deferReply().queue();
            EmbedBuilder embed = new EmbedBuilder();
            embed.setColor(new Color(76, 3, 154));
            embed.setTitle("Info about `custom_map` command"); // Optional URL


            if (scheduler == null || scheduler.isShutdown())
                embed.addField("custom_map is", "OFF",false);
            else {
                embed.addField("custom_map is", "ON", false);
                embed.addField("List of set maps:", mapHolder.toString(), false);
                embed.addField("Next map:",mapHolder.get(c),false);
            }
            event.getHook().sendMessageEmbeds(embed.build()).queue();
        }
    }

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {

        CommandData commandData = Commands.slash("custom_map", "Sets a custom map from a FIXED list of maps")
                .addOptions(new OptionData(OptionType.STRING, "map1", "Required map to be set", true, true))
                .addOptions(new OptionData(OptionType.STRING, "map2", "Required map to be set", true, true))
                .addOptions(new OptionData(OptionType.STRING, "map3", "Required map to be set", true, true))
                .addOptions(new OptionData(OptionType.STRING, "map4", "Optional Map to be set", false, true))
                .addOptions(new OptionData(OptionType.STRING, "map5", "Optional Map to be set", false, true))
                .addOptions(new OptionData(OptionType.STRING, "map6", "Optional Map to be set", false, true))
                .addOptions(new OptionData(OptionType.STRING, "map7", "Optional Map to be set", false, true))
                .addOptions(new OptionData(OptionType.STRING, "map8", "Optional Map to be set", false, true))
                .addOptions(new OptionData(OptionType.STRING, "map9", "Optional Map to be set", false, true))
                .addOptions(new OptionData(OptionType.STRING, "map10", "Optional Map to be set", false, true))
                .addOptions(new OptionData(OptionType.STRING, "map11", "Optional Map to be set", false, true))
                .addOptions(new OptionData(OptionType.STRING, "map12", "Optional Map to be set", false, true))
                .addOptions(new OptionData(OptionType.STRING, "map13", "Optional Map to be set", false, true))
                .addOptions(new OptionData(OptionType.STRING, "map14", "Optional Map to be set", false, true))
                .addOptions(new OptionData(OptionType.STRING, "map15", "Optional Map to be set", false, true))
                .setContexts(InteractionContextType.GUILD)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.KICK_MEMBERS));

        CommandData loopOffCommand = Commands.slash("toggle_off", "Stops the custom map loop")
                .setContexts(InteractionContextType.GUILD)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.KICK_MEMBERS));

        CommandData infoCommand = Commands.slash("custom_map_info", "shows info about custom map command")
                .setContexts(InteractionContextType.GUILD)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.KICK_MEMBERS));

        event.getGuild().upsertCommand(commandData).queue();
        event.getGuild().upsertCommand(loopOffCommand).queue();
        event.getGuild().upsertCommand(infoCommand).queue();
    }


    public void sendLog(String message) {
        TextChannel logChannel = jda.getTextChannelById(LOG_CHANNEL_ID);
        if (logChannel != null) {
            String currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setColor(new Color(8, 60, 219, 144)).setDescription(currentTime + "   " + message);
            logChannel.sendMessageEmbeds(embedBuilder.build()).queue();

            System.out.println(currentTime + "   " + message);
        } else {
            System.out.println("log channel not found or inaccessible");
        }
    }

    public void startScheduler() {
        try {
            if (scheduler == null || scheduler.isShutdown()) {
                scheduler = Executors.newScheduledThreadPool(1);
            }
        } catch (Exception e) {
            sendLog("Trouble starting scheduler ");
        }
        scheduler.scheduleAtFixedRate(this::changeMap, 1, 4, TimeUnit.SECONDS);
        //This delay helps update the recentMaps which we get from MapHistory class
    }

    public void stopScheduler() {
        try {
            if (scheduler != null && !scheduler.isShutdown()) {
                scheduler.shutdown();
                scheduler = null;
                mapHolder.clear(); // clear the map list
                sendLog("Custom map loop turned off.");
            }
        } catch (Exception e) {
            sendLog("Error turning off scheduler, it is already off");
        }
    }
}





